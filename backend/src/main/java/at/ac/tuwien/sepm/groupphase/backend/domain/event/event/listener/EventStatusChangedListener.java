package at.ac.tuwien.sepm.groupphase.backend.domain.event.event.listener;

import at.ac.tuwien.sepm.groupphase.backend.common.sse.SseConnectionManager;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.sse.EventStatusChangedDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.sse.NewUpcomingRoundDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.event.EventStatusChangedEvent;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.event.handler.CancelEventHandler;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.event.handler.EventFinishedHandler;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.event.handler.RegistrationClosedHandler;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.EventStatus;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.repository.EventRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.service.EventService;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.Participation;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.ParticipationStatus;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.service.ParticipationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;


@Slf4j
@Component
@RequiredArgsConstructor
public class EventStatusChangedListener {

    private final EventService eventService;
    private final ParticipationService participationService;

    private final EventRepository eventRepository;

    private final SseConnectionManager sseConnectionManager;

    private final CancelEventHandler cancelEventHandler;
    private final EventFinishedHandler eventFinishedHandler;
    private final RegistrationClosedHandler registrationClosedHandler;

    private final Timer timer = new Timer();

    private final Map<EventStatus, Consumer<EventStatusChangedEvent>> HANDLERS = Map.of(
        EventStatus.CANCELED, this::handleCancelEvent,
        EventStatus.REGISTRATION_CLOSED, this::handleRegistrationClosed,
        EventStatus.ONGOING_BUT_NO_UPCOMING_ROUND, this::handleRoundStarted,
        EventStatus.UPCOMING_ROUND_ABOUT_TO_START, this::handlePairingPhaseForNextRoundStarted,
        EventStatus.ROUND_ONGOING, this::handleRoundStarted,
        EventStatus.FINISHED, this::handleEventFinished
    );


    /**
     * Handles {@link EventStatusChangedEvent}s which indicate that the status of an {@link Event} has changed.
     *
     * @param eventStatusChangedEvent The {@link EventStatusChangedEvent} containing the event in question.
     */
    @Async
    @EventListener(EventStatusChangedEvent.class)
    public void handleEventStatusChanged(final EventStatusChangedEvent eventStatusChangedEvent) {
        var event = eventStatusChangedEvent.getEvent();
        var status = event.getStatus();
        if (HANDLERS.containsKey(status)) {
            HANDLERS.get(status).accept(eventStatusChangedEvent);
        }
        this.sseConnectionManager.sendMessageToUser(event.getOrganizer().getId(), EventStatusChangedDto.fromEvent(event));
    }


    /**
     * Sends an {@link EventStatusChangedDto} to all arrived participants via sse (see {@link SseConnectionManager})
     * and starts the timer for the round.
     *
     * @param eventStatusChangedEvent The {@link EventStatusChangedEvent} with the event {@link Event} (with new status {@link EventStatus#ONGOING_BUT_NO_UPCOMING_ROUND}) and old status.
     */
    private void handleRoundStarted(final EventStatusChangedEvent eventStatusChangedEvent) {
        final var event = eventStatusChangedEvent.getEvent();
        var arrivedParticipantsIds = eventRepository.getIdsOfArrivedParticipants(event.getId());
        timer.schedule(roundClosingTask(event), event.getRoundDurationInSeconds() * 1000);
        this.sseConnectionManager.sendMessageToUsers(arrivedParticipantsIds, EventStatusChangedDto.fromEvent(event));
    }

    /**
     * Sends an individual {@link at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.sse.NewUpcomingRoundDto} to each arrived participant via sse
     * (see {@link SseConnectionManager}).
     *
     * @param eventStatusChangedEvent The {@link EventStatusChangedEvent} with the event {@link Event} in it, in which an upcoming round was announced.
     */
    private void handlePairingPhaseForNextRoundStarted(EventStatusChangedEvent eventStatusChangedEvent) {
        var arrivedParticipants = eventRepository.getArrivedParticipants(eventStatusChangedEvent.getEvent().getId());
        this.sseConnectionManager.sendMessagesToUsers(arrivedParticipants,
            participant -> new NewUpcomingRoundDto(eventStatusChangedEvent.getEvent().getAccessToken(), participant.getPairingTokenForCurrentEvent()));
    }

    private void handleRegistrationClosed(EventStatusChangedEvent eventStatusChangedEvent) {
        registrationClosedHandler.handleRegistrationClosed(eventStatusChangedEvent);
    }

    private void handleCancelEvent(EventStatusChangedEvent eventStatusChangedEvent) {
        cancelEventHandler.handleCancelEvent(eventStatusChangedEvent);
    }

    private void handleEventFinished(EventStatusChangedEvent eventStatusChangedEvent) {
        eventFinishedHandler.handleEventFinished(eventStatusChangedEvent);
    }

    /**
     * Create a task for closing the event round.
     *
     * @param event the event whose round should be closed.
     * @return the task
     */
    private TimerTask roundClosingTask(final Event event) {
        return new TimerTask() {
            @Override
            public void run() {
                log.debug("closing the round for event {}", event.getId());
                eventService.setEventStatus(event, EventStatus.ONGOING_BUT_NO_UPCOMING_ROUND, true);
                event.getParticipations()
                    .stream() // parallel stream leads to deadlock!
                    .filter(Participation::isPresentAtEvent)
                    .forEach(participation -> participationService.setParticipationStatus(
                        participation, ParticipationStatus.AT_EVENT_NOT_PAIRED, null, true, true, true));
            }
        };
    }
}
