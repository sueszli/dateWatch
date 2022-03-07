package at.ac.tuwien.sepm.groupphase.backend.domain.event.controller;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.OrganizerAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.service.AccountService;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.service.EventExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import java.security.Principal;


@Slf4j
@PermitAll
@RestController
@RequestMapping("${routes.rest.v1}/event/{token}")
@RequiredArgsConstructor
public class EventExecutionController {

    private final EventExecutionService eventExecutionService;
    private final AccountService accountService;


    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/start")
    @Operation(summary = "Start event")
    @RolesAllowed("ORGANIZER")
    public void startEvent(@PathVariable("token") String eventAccessToken, Principal user) {
        eventExecutionService.startEvent(eventAccessToken, user.getName());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/round")
    @Operation(summary = "Prepare new round")
    @RolesAllowed("ORGANIZER")
    public void prepareNewRound(@PathVariable("token") String eventAccessToken, Principal user) {
        eventExecutionService.createNextPairingRound(eventAccessToken, user.getName());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/round")
    @Operation(summary = "Prepare new round")
    @RolesAllowed("ORGANIZER")
    public void startNextRound(@PathVariable("token") String eventAccessToken, Principal user) {
        eventExecutionService.startNextPairingRound(eventAccessToken, user.getName());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/pairing", consumes = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Enter a pairing")
    @RolesAllowed("PARTICIPANT")
    public void enterPairing(@PathVariable("token") String eventAccessToken,
                             @RequestBody String otherPersonsPairingToken,
                             Principal user) {
        eventExecutionService.enterPairing(eventAccessToken, user.getName(), otherPersonsPairingToken);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "/pairing", consumes = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Approve a pairing")
    @RolesAllowed("PARTICIPANT")
    public void approvePairing(@PathVariable("token") String eventAccessToken,
                               @RequestBody String otherPersonsPairingToken,
                               Principal user) {
        eventExecutionService.approvePairing(eventAccessToken, user.getName(), otherPersonsPairingToken);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/pairing")
    @Operation(summary = "Terminate the current pairing.")
    @RolesAllowed("PARTICIPANT")
    public void terminatePairing(@PathVariable("token") final String eventAccessToken, final Principal user) {
        eventExecutionService.terminatePairing(eventAccessToken, user.getName());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/close")
    @Operation(summary = "Close the event")
    @RolesAllowed({"ORGANIZER", "PARTICIPANT"})
    public void closeEvent(@PathVariable("token") String eventAccessToken, Principal user) {
        if (accountService.findAccountByEmail(user.getName()).getClass().equals(OrganizerAccount.class)) {
            eventExecutionService.closeEvent(eventAccessToken, user.getName());
        } else {
            eventExecutionService.closeEventParticipant(eventAccessToken, user.getName());
        }
    }
}
