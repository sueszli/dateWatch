package at.ac.tuwien.sepm.groupphase.backend.domain.account.controller;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.*;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.service.AccountService;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.dto.ParticipationStatusDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("${routes.rest.v1}/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;


    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("registration")
    @Operation(summary = "Register an account")
    public AccountDto registerAccount(@Valid @RequestBody AccountDto accountDto,
                                      @RequestParam String confirmationUrl,
                                      HttpServletRequest request) {
        log.info("POST registerAccount body: {}", accountDto);
        return accountService.registerAccount(accountDto, confirmationUrl, request.getLocale());
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("confirmation")
    @Operation(summary = "Confirm registration")
    public void confirmRegistration(@RequestParam String token) {
        log.info("POST confirmRegistration token: {}", token);
        accountService.confirmRegistration(token);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping()
    @Operation(summary = "Get profile info")
    public AccountDto getAccount(HttpServletRequest request) {
        log.info("Get account with email: {}", getAccountEmail(request));
        return accountService.findAccountDtoByEmail(getAccountEmail(request));
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/all")
    @Operation(summary = "Get non admin accounts")
    @RolesAllowed({"ADMIN"})
    public List<AccountDto> getNonAdminAccounts() {
        log.info("Get all organizer and participant accounts");
        return accountService.getNonAdminAccounts();
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping
    @Operation(summary = "Update Organizer account")
    public AccountDto updateAccount(HttpServletRequest request, @Valid @RequestBody AccountDto accountDto) {
        log.info("Update account with email: {}", getAccountEmail(request));
        return accountService.updateAccount(accountDto, getAccountEmail(request));
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/deactivate")
    @Operation(summary = "Deactivate organizer account")
    @RolesAllowed("ORGANIZER")
    public void deactivateOrganizerAccount(HttpServletRequest request) {
        String email;
        log.info("DELETE deactivateOrganizerAccount: {}", email = getAccountEmail(request));
        accountService.deactivateOrganizerAccount(email);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete")
    @Operation(summary = "Delete participant account")
    @RolesAllowed("PARTICIPANT")
    public void deleteParticipantAccount(HttpServletRequest request) {
        String email;
        log.info("DELETE deleteParticipantAccount: {}", email = getAccountEmail(request));
        accountService.deleteParticipantAccount(email);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/forgot-password")
    @Operation(summary = "Forgot password")
    public AccountDto forgotPasswordUserAccount(HttpServletRequest request,
                                                @Valid @RequestBody ForgotPwdDto forgotPwdDto) {
        log.info("POST forgotPasswordUserAccount body: {}", forgotPwdDto);
        return accountService.forgotPasswordUser(forgotPwdDto, request.getLocale());
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/password-confirmation")
    @Operation(summary = "Confirm password reset")
    public void confirmPwdReset(@Valid @RequestBody ResetPwdDto resetPwdDto) {
        log.info("PUT confirmPwdReset body: {}", resetPwdDto);
        accountService.resetNewPassword(resetPwdDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/password-change")
    @Operation(summary = "Get a token for changing password")
    public ChangePwdDto getTokenForPasswordChange(@RequestParam String email) {
        log.info("GET getTokenForPasswordChange: {}", email);
        return accountService.getTokenForPasswordChange(email);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/ban")
    @Operation(summary = "Ban or unban account")
    @RolesAllowed("ADMIN")
    public AccountDto toggleBanStatus(@Valid @RequestBody ToggleAccountBanDto toggleAccountBanDto) {
        log.info("PUT toggleBanStatus: {}", toggleAccountBanDto);
        return accountService.toggleBanStatus(toggleAccountBanDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/status")
    @Operation(summary = "Fetch current user status")
    @RolesAllowed({"ORGANIZER", "PARTICIPANT"})
    public UserStatusDto getUserStatus(final Principal user) {
        return accountService.getUserStatus(user.getName());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/participation")
    @Operation(summary = "Fetch participation status for current event")
    @RolesAllowed("PARTICIPANT")
    public ParticipationStatusDto getParticipationStatus(final Principal user) {
        return accountService.getParticipationStatus(user.getName());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/approved-pairings/{email}")
    @Operation(summary = "Revoke Pairing-Approval.")
    @RolesAllowed("PARTICIPANT")
    public void revokePairingApprovals(@PathVariable("email") String matchedUserEmail, Principal user) {
        accountService.revokePairingApprovals(matchedUserEmail, user.getName());
    }


    private String getAccountEmail(HttpServletRequest request) {
        return request.getUserPrincipal().getName();
    }
}
