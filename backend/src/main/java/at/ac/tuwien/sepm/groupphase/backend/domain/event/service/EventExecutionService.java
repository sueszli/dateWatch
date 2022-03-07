package at.ac.tuwien.sepm.groupphase.backend.domain.event.service;

import at.ac.tuwien.sepm.groupphase.backend.common.exception.AlreadyExistsException;
import at.ac.tuwien.sepm.groupphase.backend.common.exception.ForbiddenException;
import at.ac.tuwien.sepm.groupphase.backend.common.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.common.exception.PreconditionRequiredException;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.ParticipantAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.repository.ParticipantAccountRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.exception.NotEventOrganizerException;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.EventStatus;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Pairing;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.PairingRound;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.repository.EventRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.repository.PairingRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.repository.PairingRoundRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.dto.sse.PairingStatisticsDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.Participation;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.ParticipationStatus;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.repository.ParticipationRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.service.ParticipationService;
import at.ac.tuwien.sepm.groupphase.backend.domain.token.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EventExecutionService {

    private final EventService eventService;
    private final TokenService tokenService;
    private final ParticipationService participationService;

    private final EventRepository eventRepository;
    private final PairingRepository pairingRepository;
    private final PairingRoundRepository pairingRoundRepository;
    private final ParticipationRepository participationRepository;
    private final ParticipantAccountRepository participantAccountRepository;


    /**
     * Start an event in order to allow beginning with the rounds and pairings.
     * Only the organizer of an event is allowed to do this.
     *
     * @param accessToken the access token of the event to start
     * @param email       the username of the principal to start the event
     */
    public void startEvent(final String accessToken, final String email) {
        final var event = this.eventService.getEventFromOrganizer(accessToken, email);
        if (!event.hasStatus(EventStatus.REGISTRATION_CLOSED)) {
            log.debug("{} tried to start the not-closed event {}", email, accessToken);
            throw new PreconditionRequiredException("An event can only start after it's registration-phase has concluded.");
        }
        log.debug("{} starts event {}", email, accessToken);
        this.createDatingTokensForParticipants(event);
        eventService.setEventStatus(event, EventStatus.ONGOING_BUT_NO_UPCOMING_ROUND, true);
    }

    /**
     * Creates the {@link this#getUpcomingPairingRound(Event) next pairing round} of the {@link Event} identified by the
     * given {@link Event#getAccessToken() access token}.
     *
     * @param eventAccessToken    The {@link Event#getAccessToken() access token} identifying the event in question.
     * @param requestingUserEmail The email of the user requesting the operation. Must match the email of the event's organizer.
     * @throws NotFoundException             If the event in question was not found.
     * @throws NotEventOrganizerException    If the event in question was not created by the organizer with the given email.
     * @throws PreconditionRequiredException If the event in question did not yet start, an upcoming round already exists,
     *                                       or a round is currently ongoing.
     */
    public void createNextPairingRound(String eventAccessToken, String requestingUserEmail) {
        Event event = getOngoingEventFromOrganizer(eventAccessToken, requestingUserEmail);
        if (!event.hasStatus(EventStatus.ONGOING_BUT_NO_UPCOMING_ROUND)) {
            throw new PreconditionRequiredException("An upcoming round may only be created for an ongoing event" +
                "with no already existing upcoming round and no ongoing round.");
        }

        var round = new PairingRound();
        this.pairingRoundRepository.save(round);
        event.addPairingRound(round);
        eventService.setEventStatus(event, EventStatus.UPCOMING_ROUND_ABOUT_TO_START, true);
    }

    /**
     * Forms a new {@link Pairing} between an initiator and another person at an event for the upcoming round.
     * This has to be confirmed the other way around in order to generate a confirmed paring.
     *
     * @param eventAccessToken The {@link Event#getAccessToken() access token} identifying the event in question.
     * @param initiatorEmail   The {@link ParticipantAccount#getEmailLowercase() email} of the
     *                         {@link ParticipantAccount participant} trying to initiate the pairing.
     * @param pairingToken     The {@link ParticipantAccount#getPairingTokenForCurrentEvent() pairing token} of
     *                         the {@link ParticipantAccount participant} with whom the initiator wants to enter
     *                         a {@link Pairing}.
     * @throws NotFoundException             If the {@link Event} in question was not found.
     * @throws ForbiddenException            If the initiator tries to enter a pairing with himself.
     * @throws PreconditionRequiredException If the {@link Event} in question did not yet start, no participant with
     *                                       the given email or pairing token is currently present at the event, there
     *                                       is no upcoming round, both participants belong to the same group, or one
     *                                       of them already is in a pairing for the upcoming round.
     */
    public void enterPairing(String eventAccessToken, String initiatorEmail, String pairingToken) {
        var event = getOngoingEvent(eventAccessToken);
        var initiatorParticipation = getParticipationByEmailIfArrived(event.getId(), initiatorEmail);
        var pairedPersonParticipation = getParticipationByPairingTokenIfArrived(event.getId(), pairingToken);

        checkDistinctGroups(event, initiatorParticipation, pairedPersonParticipation);

        var initiator = initiatorParticipation.getParticipant();
        var pairedPerson = pairedPersonParticipation.getParticipant();
        checkPairingWithThemself(initiator, pairedPerson);

        checkReadyForPairing(initiatorParticipation, pairedPersonParticipation);
        var upcomingRound = getNextPairingRound(event);
        createPairing(upcomingRound, initiatorParticipation, pairedPersonParticipation);
    }

    /**
     * Called to indicate that the participant with the given {@link ParticipantAccount#getEmailLowercase() email}
     * approved his or her pairing with the participant identified by the given
     * {@link ParticipantAccount#getPairingTokenForCurrentEvent() pairing token}.
     *
     * @param eventAccessToken         The {@link Event#getAccessToken() access token} identifying the event in question.
     * @param requestingUserEmail      The {@link ParticipantAccount#getEmailLowercase() email} of the participant
     *                                 approving a pairing.
     * @param otherPersonsPairingToken The {@link ParticipantAccount#getPairingTokenForCurrentEvent() pairing token} of
     *                                 the participant who was paired with the user approving the pairing.
     */
    public void approvePairing(String eventAccessToken, String requestingUserEmail, String otherPersonsPairingToken) {
        var event = getOngoingEvent(eventAccessToken);
        var pairing = getLatestPairingRound(event)
            .flatMap(round -> pairingRepository.findUserPairing(
                round.getId(), requestingUserEmail.toLowerCase(), otherPersonsPairingToken))
            .orElseThrow(() -> new NotFoundException(
                "No Pairing found between User with email '" + requestingUserEmail +
                    "' and user with pairing token '" + otherPersonsPairingToken + "'."));

        if (pairing.getInitiator().getEmailLowercase().equalsIgnoreCase(requestingUserEmail)) {
            pairing.setInitiatorApprovedMatch(true);
        } else {
            pairing.setPairedPersonApprovedMatch(true);
        }

        pairingRepository.save(pairing);
    }

    /**
     * Terminate the current pairing regarding the upcoming round of the event.
     *
     * @param eventAccessToken the event access token where to terminate the pairing
     * @param name             the username of the participant who wants to terminate the pairing
     */
    public void terminatePairing(final String eventAccessToken, final String name) {
        final var pairingRound = this.getUpcomingPairingRound(this.getOngoingEvent(eventAccessToken)).orElseThrow(() -> new NotFoundException("No upcoming round found"));
        final var event = pairingRound.getEvent();
        final var participation = this.getParticipationByEmailIfArrived(event.getId(), name);
        final var pairing = this.pairingRepository.findByRoundAndParticipant(pairingRound.getId(), participation.getParticipant().getId())
            .orElseThrow(() -> new NotFoundException("No pairing found"));
        final var initiator = this.participationRepository.findParticipationByEmail(event.getId(), pairing.getInitiator().getEmailLowercase())
            .orElseThrow(() -> new NotFoundException("Cannot find initiator"));
        final var pairedPerson = this.participationRepository.findParticipationByEmail(event.getId(), pairing.getPairedPerson().getEmailLowercase())
            .orElseThrow(() -> new NotFoundException("Cannot find paired person"));
        this.pairingRepository.delete(pairing);
        this.participationService.setParticipationStatus(initiator, ParticipationStatus.AT_EVENT_NOT_PAIRED, null,
            false, true, true);
        this.participationService.setParticipationStatus(pairedPerson, ParticipationStatus.AT_EVENT_NOT_PAIRED,
            null, true, true, true);
    }

    /**
     * Provides information about how many {@link Pairing pairings} have already formed and maximum possible
     * for the upcoming {@link PairingRound round} at an {@link Event}.
     *
     * @param event The {@link Event} in question.
     * @return A {@link PairingStatisticsDto} containing information about how many pairings have already formed
     * for the upcoming round at the event.
     */
    public PairingStatisticsDto getEventPairingStatistics(Event event) {
        var pairingStatisticsDto = new PairingStatisticsDto();
        pairingStatisticsDto.setFormedPairingsForUpcomingRound(
            getUpcomingPairingRound(event)
                .map(round -> (long) round.getPairings().size())
                .orElse(0L)
        );
        var presentParticipations = event.getParticipations().stream().filter(Participation::isPresentAtEvent).collect(Collectors.toList());
        if (event.hasGroups()) {
            var firstGroupParticipants = presentParticipations.stream().filter(p -> Objects.equals(p.getGroup().getId(), event.getGroups().getFirstGroup().getId())).count();
            var secondGroupParticipants = presentParticipations.stream().filter(p -> Objects.equals(p.getGroup().getId(), event.getGroups().getSecondGroup().getId())).count();
            pairingStatisticsDto.setMaximumPairingsPossible(Math.max(firstGroupParticipants, secondGroupParticipants));
        } else {
            pairingStatisticsDto.setMaximumPairingsPossible((long) presentParticipations.size());
        }
        return pairingStatisticsDto;
    }

    /**
     * Starts the {@link this#getUpcomingPairingRound(Event) next pairing round} of the {@link Event} identified by the
     * given {@link Event#getAccessToken() access token}.
     *
     * @param eventAccessToken    The {@link Event#getAccessToken() access token} identifying the event in question.
     * @param requestingUserEmail The email of the user requesting the operation. Must match the email of the event's organizer.
     * @throws NotFoundException             If the event in question was not found.
     * @throws NotEventOrganizerException    If the event in question was not created by the organizer with the given email.
     * @throws PreconditionRequiredException If the event in question did not yet start or no upcoming round exists yet.
     */
    public void startNextPairingRound(String eventAccessToken, String requestingUserEmail) {
        Event event = getOngoingEventFromOrganizer(eventAccessToken, requestingUserEmail);
        var upcomingRound = getNextPairingRound(event);
        upcomingRound.setStartedAt(LocalDateTime.now());
        pairingRoundRepository.save(upcomingRound);
        eventService.setEventStatus(event, EventStatus.ROUND_ONGOING, true);
    }

    /**
     * Closes the {@link Event} and notify the organizer and the participants.
     *
     * @param eventAccessToken    The {@link Event#getAccessToken() access token} identifying the event in question.
     * @param requestingUserEmail The email of the user requesting the operation. Must match the email of the event's organizer.
     * @throws NotEventOrganizerException If the event in question was not created by the organizer with the given email.
     */
    public void closeEvent(String eventAccessToken, String requestingUserEmail) {
        // try if it is an organizer
        final var event = getOngoingEventFromOrganizer(eventAccessToken, requestingUserEmail);
        eventService.setEventStatus(event, EventStatus.FINISHED, true);
    }

    /**
     * Closes the {@link Event} and notify the organizer and the participants.
     *
     * @param eventAccessToken    The {@link Event#getAccessToken() access token} identifying the event in question.
     * @param requestingUserEmail The email of the user requesting the operation. Must match the email of the event's organizer.
     * @throws NotEventOrganizerException If the event in question was not created by the organizer with the given email.
     */
    public void closeEventParticipant(String eventAccessToken, String requestingUserEmail) {
        var event = getFinishedEvent(eventAccessToken);
        final var participation = getParticipationByEmailIfArrived(event.getId(), requestingUserEmail);
        participationService.setParticipationStatus(participation, ParticipationStatus.LEFT_EVENT,
            null, false, false, true);
    }


    /**
     * Checks whether the participants are in distinct groups if the event contains groups.
     *
     * @param event                     The event where to check the groups
     * @param initiatorParticipation    The participation of the participant who initiated the pairing
     * @param pairedPersonParticipation The participation of the participant who is the target of the pairing
     * @throws PreconditionRequiredException if the event contains groups and the participants are in the same group
     */
    private void checkDistinctGroups(final Event event, final Participation initiatorParticipation, final Participation pairedPersonParticipation) {
        if (event.hasGroups() && initiatorParticipation.getGroup().equals(pairedPersonParticipation.getGroup())) {
            throw new PreconditionRequiredException("Participants of the same group may not enter a pairing.");
        }
    }

    /**
     * Check whether the initiator is also the target participant during a pairing request.
     *
     * @param initiator    The initiator of the pairing
     * @param pairedPerson The target of the pairing
     * @throws ForbiddenException when both are the same participant
     */
    private void checkPairingWithThemself(final ParticipantAccount initiator, final ParticipantAccount pairedPerson) {
        if (initiator.getEmailLowercase().equals(pairedPerson.getEmailLowercase())) {
            throw new ForbiddenException("A participant can't enter a pairing with himself");
        }
    }

    /**
     * Checks if both participants are ready for a pairing.
     *
     * @param initiatorParticipation    The participation of the participant who initiated the pairing
     * @param pairedPersonParticipation The participation of the participant who is the target of the pairing
     * @throws AlreadyExistsException if at least one of the participants is not ready for a pairing
     */
    private void checkReadyForPairing(final Participation initiatorParticipation, final Participation pairedPersonParticipation) {
        if (!initiatorParticipation.hasStatus(ParticipationStatus.AT_EVENT_NOT_PAIRED)) {
            throw new AlreadyExistsException("Initiator must participate on the event and not already be in a pairing");
        }
        if (!pairedPersonParticipation.hasStatus(ParticipationStatus.AT_EVENT_NOT_PAIRED)) {
            throw new AlreadyExistsException("Pairing target must participate on the event and not already be in a pairing");
        }
    }

    /**
     * Create a pairing with the participants associated to the participations.
     * Does not enforce any checks regarding the current status of any participant.
     *
     * @param pairingRound              The round the pairing should be created
     * @param initiatorParticipation    The participation of the participant who initiated the pairing
     * @param pairedPersonParticipation The participation of the participant who is the target of the pairing
     */
    private void createPairing(final PairingRound pairingRound, final Participation initiatorParticipation, final Participation pairedPersonParticipation) {
        var pairing = new Pairing();
        pairing.setInitiator(initiatorParticipation.getParticipant());
        pairing.setPairedPerson(pairedPersonParticipation.getParticipant());
        pairing.setPairingRound(pairingRound);
        pairingRepository.save(pairing);

        participationService.setParticipationStatus(
            pairedPersonParticipation, ParticipationStatus.AT_EVENT_PAIRED, pairing, false, true, true);
        participationService.setParticipationStatus(
            initiatorParticipation, ParticipationStatus.AT_EVENT_PAIRED, pairing, true, true, true);
    }

    /**
     * @param event The {@link Event} whose upcoming (the next to start) {@link PairingRound} should be retrieved.
     * @return The {@link PairingRound} of the given {@link Event} that is next to start.
     * @throws PreconditionRequiredException If no upcoming round exists for the given event yet.
     */
    private PairingRound getNextPairingRound(Event event) {
        return getUpcomingPairingRound(event)
            .orElseThrow(() -> new PreconditionRequiredException("No upcoming round exists for the given event."));
    }

    /**
     * @param eventAccessToken The {@link Event#getAccessToken() access token} identifying the event in question.
     * @return The {@link Event} identified by the given {@link Event#getAccessToken() access token} if it already started.
     * Otherwise, a {@link PreconditionRequiredException} is thrown.
     * @throws NotFoundException             If the event in question was not found.
     * @throws PreconditionRequiredException If the event in question did not yet start.
     */
    private Event getOngoingEvent(String eventAccessToken) {
        return getOngoingEvent(() -> eventService.getEventByAccessToken(eventAccessToken));
    }

    /**
     * @param eventAccessToken The {@link Event#getAccessToken() access token} identifying the event in question.
     * @param organizerEmail   The email identifying the user requesting the event. Must match the email of the event's organizer.
     * @return The {@link Event} identified by the given {@link Event#getAccessToken() access token} if it is ongoing
     * and was created by the organizer with the given email.
     * @throws NotFoundException             If the event in question was not found.
     * @throws NotEventOrganizerException    If the event in question was not created by the organizer with the given email.
     * @throws PreconditionRequiredException If the event in question is not ongoing.
     */
    private Event getOngoingEventFromOrganizer(String eventAccessToken, String organizerEmail) {
        return getOngoingEvent(() -> eventService.getEventFromOrganizer(eventAccessToken, organizerEmail));
    }

    /**
     * @param eventGetter The method to retrieve the event before checking if it is ongoing.
     * @return The {@link Event} identified retrieved by the given method if it is ongoing.
     * Otherwise, a {@link PreconditionRequiredException} is thrown.
     * @throws PreconditionRequiredException If the event in question did is not ongoing.
     */
    private Event getOngoingEvent(Supplier<Event> eventGetter) {
        var event = eventGetter.get();
        if (!event.isOngoing()) {
            throw new PreconditionRequiredException("The specified event has not yet started.");
        }
        return event;
    }

    /**
     * @param eventAccessToken The {@link Event#getAccessToken() access token} identifying the event in question.
     * @return The {@link Event} identified by the given {@link Event#getAccessToken() access token} if it already finished.
     * Otherwise, a {@link PreconditionRequiredException} is thrown.
     * @throws NotFoundException             If the event in question was not found.
     * @throws PreconditionRequiredException If the event in question is not yet finished.
     */
    private Event getFinishedEvent(String eventAccessToken) {
        return getFinishedEvent(() -> eventService.getEventByAccessToken(eventAccessToken));
    }

    /**
     * @param eventGetter The method to retrieve the event before checking if it is finished.
     * @return The {@link Event} identified retrieved by the given method if it is finished.
     * Otherwise, a {@link PreconditionRequiredException} is thrown.
     * @throws PreconditionRequiredException If the event in question did is not finished.
     */
    private Event getFinishedEvent(Supplier<Event> eventGetter) {
        var event = eventGetter.get();
        if (!event.isFinished()) {
            throw new PreconditionRequiredException("The specified event has not yet finished.");
        }
        return event;
    }

    /**
     * @param eventId          The {@link Event#getId() id} of the {@link Event} in question.
     * @param participantEmail The {@link ParticipantAccount#getEmailLowercase() email} of the
     *                         {@link ParticipantAccount participant} in question.
     * @return The {@link ParticipantAccount participant} with the given
     * {@link ParticipantAccount#getEmailLowercase() email} who is currently at the given {@link Event}.
     * @throws PreconditionRequiredException If no participant with the given email is currently at the event in question.
     */
    private Participation getParticipationByEmailIfArrived(Long eventId, String participantEmail) {
        return participationRepository.findParticipationByEmail(eventId, participantEmail)
            .filter(Participation::isPresentAtEvent)
            .orElseThrow(() -> new PreconditionRequiredException(
                "No participant with the given email is currently at the event in question."));
    }

    /**
     * @param eventId      The {@link Event#getId() id} of the {@link Event} in question.
     * @param pairingToken The {@link ParticipantAccount#getPairingTokenForCurrentEvent() pairing token} of the
     *                     {@link ParticipantAccount participant} in question.
     * @return The {@link ParticipantAccount participant} who is currently at the given {@link Event} and was assigned
     * the given {@link ParticipantAccount#getPairingTokenForCurrentEvent() pairing token}.
     * @throws PreconditionRequiredException If no participant with the given pairing token is currently at the event
     *                                       in question.
     */
    private Participation getParticipationByPairingTokenIfArrived(Long eventId, String pairingToken) {
        return participationRepository.findParticipationByPairingToken(eventId, pairingToken)
            .filter(Participation::isPresentAtEvent)
            .orElseThrow(() -> new PreconditionRequiredException(
                "No participant with the given pairing token is currently at the event in question."));
    }

    /**
     * @param event The {@link Event} for which the upcoming {@link PairingRound} should be retrieved.
     * @return The {@link PairingRound} of this event that will start next,
     * or {@link Optional#empty()} if no such round exists yet.
     */
    private Optional<PairingRound> getUpcomingPairingRound(Event event) {
        return pairingRoundRepository.findUpcomingPairingRound(event.getId());
    }

    /**
     * @param event The {@link Event} for which the latest {@link PairingRound} should be retrieved.
     * @return The {@link PairingRound} of the given {@link Event} that started last or {@link java.util.Optional#empty()}
     * if no {@link PairingRound rounds} at all exist yet.
     */
    public Optional<PairingRound> getLatestPairingRound(Event event) {
        var pairingRounds = event.getPairingRounds();
        if (pairingRounds.isEmpty()) {
            return Optional.empty();
        }

        pairingRounds.sort(Comparator.nullsLast(Comparator.comparing(PairingRound::getStartedAt).reversed()));
        return Optional.of(pairingRounds.get(0));
    }

    /**
     * Uses {@link TokenService#generateUniqueSimpleTokens(int)} to generate individual
     * {@link ParticipantAccount#getPairingTokenForCurrentEvent() pairing tokens} for each participant and saves them
     * to the database.
     *
     * @param event The {@link Event} whose {@link ParticipantAccount participants} should receive
     *              {@link ParticipantAccount#getPairingTokenForCurrentEvent() pairing tokens}.
     */
    private void createDatingTokensForParticipants(Event event) {
        var arrivedParticipants = eventRepository.getArrivedParticipants(event.getId());
        var pairingTokens = tokenService.generateUniqueSimpleTokens(arrivedParticipants.size());

        int index = 0;
        for (var participant : arrivedParticipants) {
            participant.setPairingTokenForCurrentEvent(pairingTokens.get(index++));
        }

        participantAccountRepository.saveAll(arrivedParticipants);
    }
}
