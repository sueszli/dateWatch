package at.ac.tuwien.sepm.groupphase.backend.domain.participation.controller;

import at.ac.tuwien.sepm.groupphase.backend.domain.participation.service.EventRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import java.security.Principal;


@Slf4j
@PermitAll
@RestController
@RequestMapping("${routes.rest.v1}/event/{token}/registration")
@RequiredArgsConstructor
public class EventRegistrationController {

    private final EventRegistrationService eventRegistrationService;


    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping
    @Operation(summary = "Close event-registration")
    @RolesAllowed("ORGANIZER")
    public void closeRegistration(@PathVariable("token") String accessToken, Principal user) {
        eventRegistrationService.closeRegistration(accessToken, user.getName());
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    @Operation(summary = "Register for event")
    @RolesAllowed("PARTICIPANT")
    public void registerForEvent(@PathVariable("token") String accessToken,
                                 @RequestBody(required = false) String groupTitle,
                                 Principal user) {
        eventRegistrationService.registerForEvent(accessToken, groupTitle, user.getName());
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping
    @Operation(summary = "Deregister for event")
    @RolesAllowed("PARTICIPANT")
    public void deregisterForEvent(@PathVariable("token") String accessToken,
                                   Principal user) {
        eventRegistrationService.deregisterForEvent(accessToken, user.getName());
    }


}
