package at.ac.tuwien.sepm.groupphase.backend.domain.participation.service;

import at.ac.tuwien.sepm.groupphase.backend.common.exception.ForbiddenException;
import at.ac.tuwien.sepm.groupphase.backend.common.exception.IllegalUserArgumentException;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.OrganizerAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.ParticipantAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.repository.ParticipantAccountRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.service.AccountService;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.repository.EventRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.service.EventService;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.dto.sse.ArrivalStatisticsDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.Participation;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.ParticipationStatus;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.repository.ParticipationRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.token.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class EventArrivalService {

    private final TokenService tokenService;
    private final EventService eventService;
    private final AccountService accountService;
    private final ParticipationService participationService;

    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;
    private final ParticipantAccountRepository participantAccountRepository;


    /**
     * Provides information about how many {@link ParticipantAccount participants} have already arrived at an
     * {@link Event}.
     *
     * @param eventAccessToken The {@link Event#getAccessToken()} identifying the {@link Event} in question.
     * @param organizerEmail   The email of the {@link OrganizerAccount organizer} who requested the statistics.
     * @return An {@link ArrivalStatisticsDto} containing information about how many participants have already arrived
     * at the event.
     * @throws ForbiddenException If the requesting {@link OrganizerAccount organizer} is not the owner of the specified
     *                            event.
     */
    public ArrivalStatisticsDto getEventArrivalStatistics(String eventAccessToken, String organizerEmail) {
        var event = eventService.getEventFromOrganizer(eventAccessToken, organizerEmail);
        return getEventArrivalStatistics(event);
    }

    /**
     * Provides information about how many {@link ParticipantAccount participants} have already arrived at an
     * {@link Event}.
     *
     * @param event The {@link Event} in question.
     * @return An {@link ArrivalStatisticsDto} containing information about how many participants have already arrived
     * at the event.
     */
    public ArrivalStatisticsDto getEventArrivalStatistics(Event event) {
        final var eventId = event.getId();
        final var statistic = new ArrivalStatisticsDto();
        if (event.hasGroups()) {
            var groups = event.getGroups();
            var firstGroupTitle = groups.getFirstGroup().getTitle();
            var secondGroupTitle = groups.getSecondGroup().getTitle();
            statistic.setArrivedFirstGroupParticipants(eventRepository.countGroupArrivals(eventId, firstGroupTitle));
            statistic.setArrivedSecondGroupParticipants(eventRepository.countGroupArrivals(eventId, secondGroupTitle));
        } else {
            statistic.setArrivedParticipants(eventRepository.countArrivals(eventId));
        }
        return statistic;
    }

    /**
     * Indicates that a {@link ParticipantAccount participant} arrived at an {@link Event}.
     *
     * @param eventAccessToken   The {@link Event#getAccessToken()} identifying the {@link Event} in question.
     * @param eventEntranceToken The {@link Event#getEntranceToken()} required to confirm arrival at the event.
     * @param participantEmail   The email of the {@link ParticipantAccount participant} who indicates arrival.
     *                           He or she must be a confirmed participant for this {@link Event}.
     * @throws ForbiddenException           If the requesting {@link ParticipantAccount participant} is not a confirmed
     *                                      participant of the event.
     * @throws IllegalUserArgumentException If the provided entrance token is incorrect.
     */
    public void participantArrivedAtEvent(String eventAccessToken, String eventEntranceToken, String participantEmail) {
        var participant = accountService.findAccountByEmail(participantEmail, ParticipantAccount.class);
        Participation participation = participationRepository
            .findParticipation(eventAccessToken, participant.getId())
            .filter(Participation::isConfirmed)
            .orElseThrow(() -> new ForbiddenException("User is not a confirmed participant of the event."));

        if (!participation.getEvent().getEntranceToken().equals(eventEntranceToken)) {
            throw new IllegalUserArgumentException("The given entrance token does not match the event's entrance token");
        }

        var event = participation.getEvent();
        if (event.isOngoing()) {
            generateUniqueDatingToken(event, participant);
        }

        participation.setArrivedAtEvent(true);
        participationService.setParticipationStatus(
            participation, ParticipationStatus.AT_EVENT_NOT_PAIRED, null, true, true, true);
    }


    private void generateUniqueDatingToken(Event event, ParticipantAccount participant) {
        var tokensOfArrivedParticipants = eventRepository.getArrivedParticipants(event.getId())
            .stream()
            .map(ParticipantAccount::getPairingTokenForCurrentEvent)
            .collect(Collectors.toList());

        var uniqueDatingToken = tokenService.generateUniqueSimpleToken(tokensOfArrivedParticipants);
        participant.setPairingTokenForCurrentEvent(uniqueDatingToken);
        participantAccountRepository.save(participant);
    }
}
