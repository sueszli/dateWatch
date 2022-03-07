package at.ac.tuwien.sepm.groupphase.backend.domain.event.service;

import at.ac.tuwien.sepm.groupphase.backend.common.exception.ForbiddenException;
import at.ac.tuwien.sepm.groupphase.backend.common.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.common.exception.NotVerifiedException;
import at.ac.tuwien.sepm.groupphase.backend.common.sse.SseConnectionManager;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.*;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.Account;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.OrganizerAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.ParticipantAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.service.AccountService;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.AddUpdateEventDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.EventDetailsDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.*;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.OrganizerEventDetailsDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.eventFilter.*;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.event.EventChangedEvent;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.event.EventStatusChangedEvent;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.exception.NotEventOrganizerException;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.*;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.repository.EventRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.repository.*;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.repository.PairingRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.service.mapper.EventMapper;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.service.mapper.*;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.ParticipationStatus;
import at.ac.tuwien.sepm.groupphase.backend.domain.token.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
public class EventService {

    private final EventMapper eventMapper;
    private final FeedbackMapper feedbackMapper;

    private final TokenService tokenService;
    private final AccountService accountService;

    private final EventRepository eventRepository;
    private final PairingRepository pairingRepository;
    private final FeedbackRepository feedbackRepository;

    private final SseConnectionManager sseConnectionManager;
    private final ApplicationEventPublisher eventPublisher;


    public EventService(EventMapper eventMapper,
                        FeedbackMapper feedbackMapper,
                        TokenService tokenService,
                        @Lazy AccountService accountService,
                        EventRepository eventRepository,
                        PairingRepository pairingRepository,
                        FeedbackRepository feedbackRepository,
                        SseConnectionManager sseConnectionManager,
                        ApplicationEventPublisher eventPublisher) {
        this.eventMapper = eventMapper;
        this.feedbackMapper = feedbackMapper;
        this.tokenService = tokenService;
        this.accountService = accountService;
        this.eventRepository = eventRepository;
        this.pairingRepository = pairingRepository;
        this.feedbackRepository = feedbackRepository;
        this.sseConnectionManager = sseConnectionManager;
        this.eventPublisher = eventPublisher;
    }


    /**
     * Gets details about an event based on its access token and depending on the account type of the requesting user.</br>
     *
     * @param accessToken The access token identifying the event.
     * @param email       The email address of the user requesting the event details.
     * @return An {@link EventDetailsDto} containing all relevant data for the user that requested the event details.
     */
    public EventDetailsDto getEventDtoByAccessToken(String accessToken, String email) {
        Event event = getEventByAccessToken(accessToken);
        Account userAccount = accountService.findAccountByEmail(email);
        return eventMapper.toDto(event, userAccount);
    }

    /**
     * Find an event based on the invite token of the event.
     *
     * @param accessToken the invite token identifying the event.
     * @return the event identified by the provided token.
     * @throws NotFoundException If the event in question was not found.
     */
    public Event getEventByAccessToken(String accessToken) {
        return eventRepository.findByAccessToken(accessToken)
            .orElseThrow(
                () -> new NotFoundException(String.format("Could not find the event with the token %s", accessToken)));
    }

    public List<EventDetailsDto> getAllEvents(String email) {
        Account userAccount = accountService.findAccountByEmail(email);

        List<Event> events;
        if (userAccount instanceof OrganizerAccount) {
            events = eventRepository.getAllByOrganizer(email);
        } else if (userAccount instanceof ParticipantAccount) {
            events = eventRepository.getAllByParticipant(email);
        } else {
            throw new ForbiddenException("Requesting user must either be a participant or an organizer.");
        }

        return eventMapper.toDtos(events, userAccount);
    }

    /**
     * filter all events depending on filtertype for an account
     *
     * @param filterDto with all possible filters and filtertype
     * @param email     of participant or organizer
     * @return all matching events
     */
    public List<EventDetailsDto> getFilteredEvents(@Valid EventFilterDto filterDto, String email) {
        Account account = accountService.findAccountByEmail(email);
        if (!(account instanceof OrganizerAccount || account instanceof ParticipantAccount) ||
            (account instanceof OrganizerAccount && filterDto.getFilterType() != FilterType.EVENTS_ORGANIZER) ||
            (account instanceof ParticipantAccount && filterDto.getFilterType() == FilterType.EVENTS_ORGANIZER)) {
            throw new ForbiddenException("Requesting user must either be a participant or an organizer.");
        }

        List<Event> events = eventRepository.findAll().stream()
            .filter(event ->
                // filter if for Organizer: organizer name and event status
                filterDto.getFilterType() != FilterType.EVENTS_ORGANIZER ||
                    (event.getOrganizer().equals(account) &&
                        (event.hasStatus(EventStatus.REGISTRATION_OPEN) || event.hasStatus(EventStatus.REGISTRATION_CLOSED)))
            )
            .filter(event ->
                // filter if for Participant: event status
                filterDto.getFilterType() != FilterType.EVENTS_PARTICIPANT_PUBLIC ||
                    ((event.hasStatus(EventStatus.REGISTRATION_OPEN) || event.hasStatus(EventStatus.REGISTRATION_CLOSED)) &&
                        (filterDto.getIsPublic() == null || (event.isPublic() == filterDto.getIsPublic())))
            )
            .filter(event ->
                // filter if for Participant planned: participations and event status
                filterDto.getFilterType() != FilterType.EVENTS_PARTICIPANT_PLANNED ||
                    (((event.hasStatus(EventStatus.REGISTRATION_OPEN) || event.hasStatus(EventStatus.REGISTRATION_CLOSED))) &&
                        event.getParticipations().stream().anyMatch(participation ->
                            participation.getParticipant().equals(account) &&
                                (participation.hasStatus(ParticipationStatus.CONFIRMED_REGISTRATION) ||
                                    participation.hasStatus(ParticipationStatus.UNCONFIRMED_REGISTRATION))))
            )
            .filter(event ->
                // filter if for Participant previously visited: participations and event status
                filterDto.getFilterType() != FilterType.EVENTS_PARTICIPANT_VISITED ||
                    (event.hasStatus(EventStatus.FINISHED) &&
                        (event.getParticipations().stream().anyMatch(participation ->
                            participation.getParticipant().equals(account) && participation.hasStatus(ParticipationStatus.LEFT_EVENT))))
            )
            .filter(event ->
                // filter for entered data
                (filterDto.getOrganizerNameSubstring() == null || event.getOrganizer().getFullName().toLowerCase(Locale.ROOT).contains(filterDto.getOrganizerNameSubstring().toLowerCase(Locale.ROOT))) &&
                    (filterDto.getTitleSubstring() == null || event.getTitle().toLowerCase(Locale.ROOT).contains(filterDto.getTitleSubstring().toLowerCase(Locale.ROOT))) &&
                    (filterDto.getCitySubstring() == null || event.getCity().toLowerCase(Locale.ROOT).contains(filterDto.getCitySubstring().toLowerCase(Locale.ROOT))) &&
                    ((filterDto.getStartDateAndTime() == null || filterDto.getEndDateAndTime() == null) ||
                        !(event.getStartDateAndTime().isBefore(filterDto.getStartDateAndTime()) ||
                            event.getStartDateAndTime().isAfter(filterDto.getEndDateAndTime())))
            )
            .collect(Collectors.toList());

        return eventMapper.toDtos(events, account);
    }

    /**
     * Creates a new {@link Event}. Only possible for verified {@link OrganizerAccount organizers}.
     *
     * @param addUpdateEventDto The data from which the {@link Event} should be created.
     * @param organizerEMail    The e-mail of the user trying to create the {@link Event}.
     * @return An {@link OrganizerEventDetailsDto} representing the newly created {@link Event}.
     * @throws NotVerifiedException If a not yet verified {@link OrganizerAccount organizer} tries to create the {@link Event}.
     */
    public OrganizerEventDetailsDto createEvent(AddUpdateEventDto addUpdateEventDto, String organizerEMail) {

        var organizerAccount = accountService.findAccountByEmail(organizerEMail, OrganizerAccount.class);
        if (!organizerAccount.isVerified()) {
            throw new NotVerifiedException(
                String.format("A not yet verified user (%s) is not allowed to create an event.", organizerEMail));
        }

        Event event = eventMapper.toEntity(addUpdateEventDto);
        event.setAccessToken(tokenService.generateUniqueSimpleToken(eventRepository::existsByAccessToken));
        event.setEntranceToken(tokenService.generateUniqueSimpleToken(eventRepository::existsByEntranceToken));
        event.setOrganizer(organizerAccount);
        event = eventRepository.save(event);

        return eventMapper.toOrganizerDetailsDto(event);
    }

    /**
     * Update an {@link Event} with new data. Only possible for verified {@link OrganizerAccount organizers}.
     *
     * @param token             the access token identifying the {@link Event}.
     * @param addUpdateEventDto The data with which the given {@link Event} should be updated.
     * @param email             The e-mail of the user trying to create the {@link Event}.
     * @return An {@link OrganizerEventDetailsDto} representing the newly updated {@link Event}.
     * @throws NotVerifiedException If a not yet verified {@link OrganizerAccount organizer} tries to create the {@link Event}.
     * @throws NotFoundException    If it is not possible to find an {@link Event} with the given token for the {@link OrganizerAccount organizer}.
     * @throws ForbiddenException   If the {@link EventStatus status} of the given event is not {@link EventStatus#REGISTRATION_OPEN}
     */
    public OrganizerEventDetailsDto updateEvent(String token, AddUpdateEventDto addUpdateEventDto, String email) {
        var organizerAccount = accountService.findAccountByEmail(email, OrganizerAccount.class);
        if (!organizerAccount.isVerified()) {
            throw new NotVerifiedException(
                String.format("A not yet verified user (%s) is not allowed to update an event.", email));
        }

        Event oldEvent = eventRepository.findEventFromOrganizer(email, token).orElseThrow(
            () -> new NotFoundException(String.format("Could not find the event with the token %s for this organizer %s.", token, email)));

        if (!oldEvent.getStatus().equals(EventStatus.REGISTRATION_OPEN)) {
            throw new ForbiddenException("If the registration of the event is not open anymore, it is not possible to change data from this event.");
        }

        Event newEvent = eventMapper.toEntity(addUpdateEventDto);

        if (!oldEvent.equals(newEvent)) {
            Event newDiffEvent = oldEvent.getDiff(newEvent);
            if (newDiffEvent != null) {
                Event oldDiffEvent = newEvent.getDiff(oldEvent);
                EventChangedEvent eventChangedEvent = new EventChangedEvent(oldEvent, oldDiffEvent, newDiffEvent);
                eventPublisher.publishEvent(eventChangedEvent);
                newEvent = changeEvent(oldEvent, newEvent);
                eventRepository.save(newEvent);
            } else if (!oldEvent.getRoundDurationInSeconds().equals(newEvent.getRoundDurationInSeconds())) {
                newEvent = changeEvent(oldEvent, newEvent);
                eventRepository.save(newEvent);
            }
        }
        return eventMapper.toOrganizerDetailsDto(newEvent);
    }

    /**
     * Add feedback from a participant to an {@link Event}. Only possible for verified {@link ParticipantAccount participants}.
     *
     * @param feedbackDto The feedback which shall be added to the {@link Event}.
     * @param email       The e-mail of the user trying to create the {@link Event}.
     */
    public void giveFeedback(FeedbackDto feedbackDto, String email) {
        var participantAccount = accountService.findAccountByEmail(email, ParticipantAccount.class);
        if (!participantAccount.isVerified()) {
            throw new NotVerifiedException(
                String.format("A not yet verified user (%s) is not allowed to give feedback to an event.", email));
        }

        Event event = eventRepository.findEventWithParticipant(email, feedbackDto.getEventAccessToken()).orElseThrow(
            () -> new NotFoundException(
                String.format("Could not find the event with the token %s and with participating participant %s.",
                    feedbackDto.getEventAccessToken(), email)));

        var feedback = new Feedback();
        feedback.setMessage(feedbackDto.getMessage());
        feedback.setEventTitle(event.getTitle());
        feedback.setOrganizerEmailLowercase(event.getOrganizer().getEmailLowercase());
        feedback.setEventAccessToken(feedbackDto.getEventAccessToken());
        feedbackRepository.save(feedback);
    }

    /**
     * Get all feedback for all {@link Event events} of an {@link OrganizerAccount organizer}.
     *
     * @param email The e-mail of the organizer for the feedback.
     */
    public List<FeedbackDto> getAllFeedback(String email) {
        var organizerAccount = accountService.findAccountByEmail(email, OrganizerAccount.class);
        if (!organizerAccount.isVerified()) {
            throw new NotVerifiedException(
                String.format("A not yet verified user (%s) is not allowed to view feedback of events.", email));
        }
        List<Feedback> feedbacks = feedbackRepository.getAllByOrganizer(organizerAccount.getEmailLowercase());
        return feedbackMapper.toDtos(feedbacks);
    }

    /**
     * Changes the given {@link Event} with new data.
     *
     * @param event    the {@link Event} which should be changed.
     * @param newEvent is an {@link Event} with only some data which is needed for the change.
     * @return the changed {@link Event}.
     * @throws ForbiddenException If the groups or isPublic value should get changed.
     */
    private Event changeEvent(Event event, Event newEvent) {
        if (event.hasGroups()) {
            if (newEvent.hasGroups()) {
                event.setGroups(newEvent.getGroups());
            } else {
                throw new ForbiddenException("If the old event has groups the new event must have groups too.");
            }
        } else {
            if (newEvent.hasGroups()) {
                throw new ForbiddenException("If the old event has no groups the new event is not allowed to have groups.");
            }
        }
        if (event.isPublic() != newEvent.isPublic()) {
            throw new ForbiddenException("It is not allowed to change private to public or the other way around.");
        }

        event.setTitle(newEvent.getTitle());
        event.setDescription(newEvent.getDescription());
        event.setStartDateAndTime(newEvent.getStartDateAndTime());
        event.setDurationInMinutes(newEvent.getDurationInMinutes());
        event.setRoundDurationInSeconds(newEvent.getRoundDurationInSeconds());
        event.setStreet(newEvent.getStreet());
        event.setPostcode(newEvent.getPostcode());
        event.setCity(newEvent.getCity());

        return event;
    }

    /**
     * Cancels an event with the given values
     *
     * @param accessToken The email of the organizer how wants to cancel an event
     * @param email       The access token of the event to cancel.
     * @throws ForbiddenException if was already started.
     */
    public void cancelEventByAccessToken(String accessToken, String email) {
        Event event = getEventFromOrganizer(accessToken, email);
        if (event.wasStarted()) {
            throw new ForbiddenException("A already started event can not be canceled.");
        }
        setEventStatus(event, EventStatus.CANCELED, true);
    }

    /**
     * Sets the {@link EventStatus status} of an {@link Event} and publishes an {@link EventStatusChangedEvent}
     * afterwards. </br>
     * Does nothing if the given {@link Event} already has the given {@link EventStatus status}.
     *
     * @param event               The {@link Event} whose {@link EventStatus status} is to be changed.
     * @param status              The new {@link EventStatus status}.
     * @param saveEventAfterwards Whether the given {@link Event} should be persisted in the database after the operation.
     */
    public void setEventStatus(Event event, EventStatus status, boolean saveEventAfterwards) {
        if (event.hasStatus(status)) {
            return;
        }
        var oldStatus = event.getStatus();
        event.setStatus(status);
        eventPublisher.publishEvent(new EventStatusChangedEvent(oldStatus, event));
        if (saveEventAfterwards) {
            eventRepository.save(event);
        }
    }

    /**
     * Subscribe to all event(as participation) based events(as application internal events).
     *
     * @param email the email of the user to subscribe
     * @return the emitter of all application internal events
     */
    public SseEmitter subscribeToEventUpdates(final String email) {
        log.debug("{} tries to subscribe to events", email);
        final var account = this.accountService.findAccountByEmail(email);
        return this.sseConnectionManager.startUserConnection(account.getId());
    }

    /**
     * Checks whether the provided email is the email of the organizer of the provided event.
     *
     * @param email       The email of the organizer to check.
     * @param accessToken The access token of the event to check.
     * @return the corresponding event to the access token
     * @throws NotEventOrganizerException when the provided email is not the email of the organizer of the event.
     */
    public Event getEventFromOrganizer(final String accessToken, final String email) {
        return this.eventRepository.findEventFromOrganizer(email, accessToken)
            .orElseThrow(NotEventOrganizerException::new);
    }

    public List<MatchDto> getAllMatches(String email) { // works
        var participant = accountService.findAccountByEmail(email, ParticipantAccount.class);
        return getMatchesFromPairings(
            pairingRepository.getAllMatchPairingsForParticipant(participant.getId()), participant);
    }

    /**
     * Calculates for organizer after-event-statistic such as
     *  #Participants
     *  #Total matches
     *  #Date-coverage
     *  #Match-ratio
     *
     * @param accessToken    The {@link Event#getAccessToken() access token} identifying the event in question.
     * @param organizerEmail The {@link Account#getEmailLowercase() email} of the user requesting the data.
     *                       Must be the event organizer.
     * @return the dto with integers (100% reference value) given in description and the name of the event.
     * @throws ForbiddenException when the user is not allowed to watch this statistic.
     */
    public AfterEventStatisticsDto getAfterEventStatistics(String accessToken, String organizerEmail) {
        // Only organizer of this event can check the post-statistics.
        Event event = getEventFromOrganizer(accessToken, organizerEmail);

        var numberOfParticipations = event.getParticipations().size();
        var numberOfPairings = pairingRepository.countAllByEventId(event.getId());
        var numberOfMatches = pairingRepository.countAllMatchesByEventId(event.getId());
        var maxNumberOfPairings = numberOfParticipations *
            (event.hasGroups() ? numberOfParticipations : (numberOfParticipations - 1));

        AfterEventStatisticsDto dto = new AfterEventStatisticsDto();
        dto.setParticipants(numberOfParticipations);
        dto.setMatchRatioPercentage(100 * numberOfMatches / numberOfPairings);
        dto.setDateCoveragePercentage(100 * numberOfPairings / maxNumberOfPairings);
        dto.setTotalMatches(numberOfMatches);
        dto.setEventName(event.getTitle());
        return dto;
    }


    private List<MatchDto> getMatchesFromPairings(List<Pairing> pairings, ParticipantAccount participant) {
        return pairings.stream()
            .map(p ->
                new MatchDto(p.getPairingRound().getEvent().getTitle(), p.getPartnerOf(participant).orElseThrow()))
            .collect(Collectors.toList());
    }
}