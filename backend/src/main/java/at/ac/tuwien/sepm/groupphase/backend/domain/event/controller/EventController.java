package at.ac.tuwien.sepm.groupphase.backend.domain.event.controller;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.MatchDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.*;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.eventFilter.EventFilterDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.FilterTypeConverter;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Slf4j
@PermitAll
@RestController
@RequestMapping("${routes.rest.v1}/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;


    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{token}")
    @Operation(summary = "Get event details")
    @PreAuthorize("isAuthenticated()")
    public EventDetailsDto getEvent(@PathVariable("token") String accessToken, Principal user) {
        return eventService.getEventDtoByAccessToken(accessToken, user.getName());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping()
    @Operation(summary = "Get events by current user.")
    @RolesAllowed({"PARTICIPANT", "ORGANIZER"})
    public List<EventDetailsDto> getAllEvents(Principal user) {
        // returns all events that the user is either registered to or that the user organizes (based on user role)
        return eventService.getAllEvents(user.getName());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/filter")
    @Operation(summary = "Get filtered events of an organizer.")
    @RolesAllowed({"PARTICIPANT", "ORGANIZER"})
    public List<EventDetailsDto> getFilteredEvents(String titleSubstring,
                                                   @RequestParam(required = false) String startDateAndTime,
                                                   @RequestParam(required = false) String endDateAndTime,
                                                   @RequestParam(required = false) String citySubstring,
                                                   @RequestParam(required = false) String isPublic,
                                                   @RequestParam String filterType,
                                                   @RequestParam(required = false) String organizerNameSubstring, Principal user) {
        FilterTypeConverter filterTypeConverter = new FilterTypeConverter();
        EventFilterDto eventFilterDto = new EventFilterDto((titleSubstring != null && !titleSubstring.equals("null")) ? titleSubstring : null,
            (startDateAndTime != null && !startDateAndTime.equals("null")) ? LocalDateTime.parse(startDateAndTime, DateTimeFormatter.ISO_DATE_TIME) : null,
            (endDateAndTime != null && !endDateAndTime.equals("null")) ? LocalDateTime.parse(endDateAndTime, DateTimeFormatter.ISO_DATE_TIME) : null,
            (citySubstring != null && !citySubstring.equals("null")) ? citySubstring : null,
            (organizerNameSubstring != null && !organizerNameSubstring.equals("null")) ? organizerNameSubstring : null,
            filterTypeConverter.convertToEntityAttribute(Integer.parseInt(filterType)),
            (isPublic != null && !isPublic.equals("null")) ? Boolean.parseBoolean(isPublic) : null);
        return eventService.getFilteredEvents(eventFilterDto, user.getName());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping()
    @Operation(summary = "Create an event")
    @RolesAllowed("ORGANIZER")
    public EventDetailsDto createEvent(@Valid @RequestBody AddUpdateEventDto addUpdateEventDto, Principal organizer) {
        return eventService.createEvent(addUpdateEventDto, organizer.getName());
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{token}/update")
    @Operation(summary = "Update an event")
    @RolesAllowed("ORGANIZER")
    public EventDetailsDto updateEvent(@Valid @RequestBody AddUpdateEventDto addUpdateEventDto, @PathVariable String token, Principal organizer) {
        return eventService.updateEvent(token, addUpdateEventDto, organizer.getName());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/feedback")
    @Operation(summary = "Give feedback for an event")
    @RolesAllowed("PARTICIPANT")
    public void giveFeedback(@Valid @RequestBody FeedbackDto feedbackDto, Principal participant) {
        eventService.giveFeedback(feedbackDto, participant.getName());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/feedback")
    @Operation(summary = "Get all feedback for all events")
    @RolesAllowed("ORGANIZER")
    public List<FeedbackDto> getAllFeedback(Principal organizer) {
        return eventService.getAllFeedback(organizer.getName());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/subscription", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "subscribe to event entrance for organizers")
    @RolesAllowed({"PARTICIPANT", "ORGANIZER"})
    public SseEmitter subscribeToEventUpdates(final Principal user) {
        final var emitter = this.eventService.subscribeToEventUpdates(user.getName());
        log.debug("created an emitter for user {}", user.getName());
        return emitter;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/matches")
    @Operation(summary = "Get all matches of current user.")
    @RolesAllowed("PARTICIPANT")
    public List<MatchDto> getAllMatches(Principal user) {
        return eventService.getAllMatches(user.getName());
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{token}/cancel")
    @Operation(summary = "Cancel an event")
    @RolesAllowed("ORGANIZER")
    public void cancelEvent(@PathVariable("token") String accessToken, Principal user) {
        eventService.cancelEventByAccessToken(accessToken, user.getName());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{token}/statistics")
    @Operation(summary = "Get event statistics")
    @RolesAllowed("ORGANIZER")
    public AfterEventStatisticsDto getAfterEventStatistics(@PathVariable("token") String accessToken, Principal user) {
        log.info("getAfterEventStatistics w/ accessToken: {}, user: {}", accessToken, user.getName());

        return eventService.getAfterEventStatistics(accessToken, user.getName());
    }
}
