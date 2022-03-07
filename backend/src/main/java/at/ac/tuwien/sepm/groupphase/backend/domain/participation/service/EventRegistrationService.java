package at.ac.tuwien.sepm.groupphase.backend.domain.participation.service;

import at.ac.tuwien.sepm.groupphase.backend.common.exception.ForbiddenException;
import at.ac.tuwien.sepm.groupphase.backend.common.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.ParticipantAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.service.AccountService;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.EventStatus;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.service.EventService;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.Participation;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.repository.EventRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.ParticipationStatus;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EventRegistrationService {

    private final EventService eventService;
    private final AccountService accountService;

    private final EventRepository eventRepository;
    private final ParticipationRepository participationRepository;


    /**
     * Registers a {@link ParticipantAccount participant} for an event.
     * If the registration already exists nothing happens.
     *
     * @param userEmail The e-mail of the user trying to create the event.
     *                  Must be from a {@link ParticipantAccount participant}.
     */
    public void registerForEvent(String accessToken, String groupTitle, String userEmail) {
        var event = eventService.getEventByAccessToken(accessToken);
        if (!event.hasStatus(EventStatus.REGISTRATION_OPEN)) {
            throw new ForbiddenException("Registration for this event has already been closed.");
        }

        var participant = accountService.findAccountByEmail(userEmail, ParticipantAccount.class);
        if (participationRepository.existsByEventIdAndParticipantId(event.getId(), participant.getId())) {
            return;
        }

        var participation = new Participation();
        participation.setParticipant(participant);

        if (event.hasGroups()) {
            if (groupTitle != null) {
                participation.setGroup(event.getGroupByTitle(groupTitle));
            } else {
                throw new ForbiddenException("Registration for an event with groups must be group specific.");
            }
        }

        event.addParticipation(participation);
        eventRepository.save(event);
    }

    /**
     * Deregisters a {@link ParticipantAccount participant} for an event.
     *
     * @param userEmail The e-mail of the user trying to create the event.
     *                  Must be from a {@link ParticipantAccount participant}.
     * @throws NotFoundException if the registration does not exist.
     */
    public void deregisterForEvent(String accessToken, String userEmail) {
        var event = eventService.getEventByAccessToken(accessToken);
        var participant = accountService.findAccountByEmail(userEmail, ParticipantAccount.class);

        if (!participationRepository.existsByEventIdAndParticipantId(event.getId(), participant.getId())) {
            throw new NotFoundException("No registration was found for this event and participant.");
        }

        if (event.hasStatus(EventStatus.REGISTRATION_OPEN)) {
            deregisterForOpenEvent(event, participant);
        }

        if (event.hasStatus(EventStatus.REGISTRATION_CLOSED)) {
            deregisterForClosedEvent(event, participant);
        }

    }

    /**
     * Closes the registration phase of an {@link Event} meaning that no more {@link ParticipantAccount participants}
     * can register for the event. Also confirms all prior registrations if the event has no groups or as many
     * of them as possible while ensuring that the groups are evenly matched.
     *
     * @param accessToken    The access token identifying the event.
     * @param organizerEmail The e-mail of the user trying to close the events' registration phase.
     */
    public void closeRegistration(String accessToken, String organizerEmail) {
        var event = eventService.getEventFromOrganizer(accessToken, organizerEmail);
        if (event.hasStatus(EventStatus.REGISTRATION_CLOSED)) {
            return;
        }

        confirmParticipations(event);
        eventService.setEventStatus(event, EventStatus.REGISTRATION_CLOSED, false);
        eventRepository.save(event);
    }


    /**
     * Confirms either all {@link Participation participations} for the given {@link Event} if it has no
     * {@link Event#getGroups() groups} or all {@link Participation participations} of the {@link Event#getGroups() group}
     * with fewer {@link Participation participations} and as many of the {@link Event#getGroups() group} with more
     * {@link Participation participations}.
     *
     * @param event The {@link Event} whose {@link Participation participations} should be confirmed/declined.
     */
    private void confirmParticipations(Event event) {

        if (!event.hasGroups()) {
            event.getParticipations()
                .parallelStream()
                .sorted(Comparator.comparing(Participation::getCreated_at))
                .limit(event.getMaxParticipants())
                .forEach(participation -> participation.setStatus(ParticipationStatus.CONFIRMED_REGISTRATION));
            return;
        }

        Map<String, List<Participation>> participationsPerGroup = event.getParticipations().stream()
            .collect(Collectors.groupingBy(participation -> participation.getGroup().getTitle()));

        long leastGroupParticipants;
        if (participationsPerGroup.size() < 2) {
            leastGroupParticipants = 0L;
        } else {
            leastGroupParticipants = participationsPerGroup.values()
                .stream()
                .mapToLong(List::size)
                .min()
                .orElse(0);
        }

        participationsPerGroup.values().parallelStream().forEach(groupParticipations -> {
            groupParticipations.sort(Comparator.comparing(Participation::getCreated_at));
            long confirmedRegistrations = 0L;
            long maxGroupLimit = event.getMaxParticipants()/2;
            long limit = Math.min(leastGroupParticipants, maxGroupLimit);
            for (var participation : groupParticipations) {
                if (confirmedRegistrations < limit) {
                    participation.setStatus(ParticipationStatus.CONFIRMED_REGISTRATION);
                    confirmedRegistrations++;
                }
            }
        });
    }

    /**
     * Deletes the Participation-entry for the given event and participant.
     *
     * @param event              for which the entry should get deleted.
     * @param participantAccount for which the entry should get deleted.
     */
    private void deregisterForOpenEvent(Event event, ParticipantAccount participantAccount) {
        participationRepository.deleteByEventIdAndParticipantId(event.getId(), participantAccount.getId());
    }

    /**
     * Alters the state of the Participation-entry to TURNED_DOWN_CONFIRMED_REGISTRATION
     * for the given event and participant.
     *
     * @param event              for which the entry should get altered.
     * @param participantAccount for which the entry should get altered.
     */
    private void deregisterForClosedEvent(Event event, ParticipantAccount participantAccount) {
        var participation = participationRepository.findParticipation(event.getAccessToken(), participantAccount.getId()).get();
        participation.setStatus(ParticipationStatus.TURNED_DOWN_CONFIRMED_REGISTRATION);
    }
}