package at.ac.tuwien.sepm.groupphase.backend.domain.participation.event.listener;

import at.ac.tuwien.sepm.groupphase.backend.common.sse.SseConnectionManager;
import at.ac.tuwien.sepm.groupphase.backend.common.sse.SseDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.service.EventExecutionService;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.dto.sse.ArrivalStatisticsDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.dto.sse.PairingStatisticsDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.dto.sse.ParticipationStatusChangedDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.event.ParticipationStatusChangedEvent;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.ParticipationStatus;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.service.EventArrivalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;


@Slf4j
@Component
@RequiredArgsConstructor
public class ParticipationStatusChangedListener {

    private final EventArrivalService eventArrivalService;
    private final EventExecutionService eventExecutionService;

    private final SseConnectionManager sseConnectionManager;

    private final Map<ParticipationStatus, Function<Event, SseDto>> HANDLERS = Map.of(
        ParticipationStatus.AT_EVENT_PAIRED, this::getPairingStatisticsDto,
        ParticipationStatus.AT_EVENT_NOT_PAIRED, this::getArrivalStatisticsDto
    );


    /**
     * Handles {@link ParticipationStatusChangedEvent}s which indicate that the status of a
     * {@link at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.Participation} changed.
     *
     * @param participationStatusChangedEvent The {@link ParticipationStatusChangedEvent} in question.
     */
    @Async
    @EventListener(ParticipationStatusChangedEvent.class)
    public void handleParticipationStatusChangedEvent(ParticipationStatusChangedEvent participationStatusChangedEvent) {
        var participation = participationStatusChangedEvent.getParticipation();
        var participationStatus = participation.getStatus();
        if (!HANDLERS.containsKey(participationStatus)) {
            return;
        }

        final var event = participation.getEvent();
        if (participationStatusChangedEvent.isNotifyOrganizer()) {
            final var update = HANDLERS.get(participationStatus).apply(event);
            this.sendSseUpdateToOrganizer(event, update);
        }
        if (participationStatusChangedEvent.isNotifyParticipant()) {
            this.sendSseUpdateToParticipant(participationStatusChangedEvent);
        }
    }

    /**
     * Sends a {@link SseDto} to the organizer of the given {@link Event} via sse, given, that he or she first
     * established a sse connection
     * (see {@link at.ac.tuwien.sepm.groupphase.backend.domain.event.service.EventService#subscribeToEventUpdates(String)}).
     *
     * @param event  The event whose organizer is to receive an update about the number of currently arrived participants.
     * @param update The {@link SseDto} to send to the organizer of the given {@link Event}.
     */
    private void sendSseUpdateToOrganizer(final Event event, SseDto update) {
        final var organizer = event.getOrganizer();
        final var success = sseConnectionManager.sendMessageToUser(organizer.getId(), update);
        if (!success) {
            log.error("Unable to send update for event with access token '{}' to organizer with email '{}'",
                event.getAccessToken(), organizer.getEmailLowercase());
        }
    }

    /**
     * Send the {@link ParticipationStatusChangedDto} to the participant which is effected by the status change directly.
     * This means, participants which are in pairing with this participant will not receive a message from this method.
     *
     * @param participationStatusChangedEvent The event to handle
     */
    private void sendSseUpdateToParticipant(final ParticipationStatusChangedEvent participationStatusChangedEvent) {
        final var participation = participationStatusChangedEvent.getParticipation();
        final var participant = participation.getParticipant();
        log.debug("send participation update to participant with id {}", participant.getId());
        final var participationStatus = new ParticipationStatusChangedDto(participation.getStatus().getId(), null, null);
        if (participation.hasStatus(ParticipationStatus.AT_EVENT_PAIRED)) {
            log.debug("participant {} is now in a pairing, fetching the pairing token of their partner", participant.getId());
            final var pairing = participationStatusChangedEvent.getPairing();
            if (pairing == null) {
                log.debug("pairing is null for participation status changed event {}", participationStatusChangedEvent);
            } else {
                final var partner = pairing.getPartnerOf(participant);
                partner.ifPresent(participantAccount -> {
                    participationStatus.setOtherPersonsNickname(participantAccount.getNickname());
                    participationStatus.setOtherPersonsPairingToken(participantAccount.getPairingTokenForCurrentEvent());
                });
            }
        }
        final var success = sseConnectionManager.sendMessageToUser(participant.getId(), participationStatus);
        if (!success) {
            log.error("Unable to send update for participation status to participant with email '{}'", participant.getEmailLowercase());
        }
    }

    private ArrivalStatisticsDto getArrivalStatisticsDto(Event event) {
        return eventArrivalService.getEventArrivalStatistics(event);
    }

    private PairingStatisticsDto getPairingStatisticsDto(Event event) {
        return eventExecutionService.getEventPairingStatistics(event);
    }
}