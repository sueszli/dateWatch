package at.ac.tuwien.sepm.groupphase.backend.domain.account.service;

import at.ac.tuwien.sepm.groupphase.backend.common.exception.AlreadyExistsException;
import at.ac.tuwien.sepm.groupphase.backend.common.exception.IllegalUserArgumentException;
import at.ac.tuwien.sepm.groupphase.backend.common.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.AccountDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.ChangePwdDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.ForgotPwdDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.ResetPwdDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.ToggleAccountBanDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.UserStatusDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.event.ForgotPasswordEvent;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.event.RegistrationCompleteEvent;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.exception.TokenExpiredException;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.Account;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.OrganizerAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.ParticipantAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.repository.AccountRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.repository.AdminAccountRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.repository.OrganizerAccountRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.repository.ParticipantAccountRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.service.mapper.AccountMapper;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.repository.EventRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.repository.PairingRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.service.EventExecutionService;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.service.EventService;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.dto.ParticipationStatusDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.repository.ParticipationRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.token.persistence.entity.VerificationToken;
import at.ac.tuwien.sepm.groupphase.backend.domain.token.persistence.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    public final String PARTICIPANT_ROLE = "ROLE_PARTICIPANT";
    public final String ORGANIZER_ROLE = "ROLE_ORGANIZER";
    public final String ADMIN_ROLE = "ROLE_ADMIN";
    public final String USER_ROLE = "ROLE_USER";

    private final AccountMapper accountMapper;

    private final EventRepository eventRepository;
    private final AccountRepository accountRepository;
    private final AdminAccountRepository adminAccountRepository;
    private final OrganizerAccountRepository organizerAccountRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final ParticipantAccountRepository participantAccountRepository;
    private final PairingRepository pairingRepository;
    private final ParticipationRepository participationRepository;

    private final EventService eventService;
    private final EventExecutionService eventExecutionService;

    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;


    /**
     * Find a user in the context of Spring Security based on the email address
     * <br>
     * For more information have a look at this tutorial:
     * https://www.baeldung.com/spring-security-authentication-with-a-database
     *
     * @param email the email address
     * @return a Spring Security user
     * @throws UsernameNotFoundException is thrown if the specified user does not exist
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Load a user by email");
        try {
            Account account = findAccountByEmail(email);
            List<GrantedAuthority> grantedAuthorities = getAuthorities(account.getId());
            return new User(account.getEmailLowercase(), account.getPassword(), grantedAuthorities);
        } catch (NotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage(), e);
        }
    }

    /**
     * Find an account based on it's email address.
     *
     * @param email the email address
     * @return an application user
     */
    public Account findAccountByEmail(String email) {
        log.debug("Find application user by email");
        return accountRepository.findFirstByEmailLowercase(email)
            .orElseThrow(
                () -> new NotFoundException(String.format("Could not find the user with the email address %s", email)));
    }

    public <T extends Account> T findAccountByEmail(String email, Class<T> accountType) {
        var account = findAccountByEmail(email);
        if (!account.getClass().equals(accountType)) {
            throw new NotFoundException(
                String.format("The account associated with the email address '%s' is not of type '%s'",
                    email, accountType.getName()));
        }

        @SuppressWarnings("unchecked")
        T typedAccount = (T) account;
        return typedAccount;
    }

    public AccountDto findAccountDtoByEmail(String email) {
        return accountMapper.toDto(findAccountByEmail(email));
    }

    public List<AccountDto> getNonAdminAccounts () {
        return accountMapper.toDtos(accountRepository.findNonAdminAccounts());
    }

    /**
     * Register an organizer account with the help of a confirm mail.
     *
     * @param accountDto the account to register
     * @return the registered yet unconfirmed account
     */
    public AccountDto registerAccount(AccountDto accountDto,
                                      String confirmationBaseUrl,
                                      Locale locale) {
        log.debug("Register a new account");
        if (accountRepository.existsByEmailLowercase(accountDto.getEmail())) {
            throw new AlreadyExistsException();
        }
        var hashedPassword = passwordEncoder.encode(accountDto.getPassword());
        var account = accountMapper.toEntity(accountDto, hashedPassword);
        createRegistrationTokenForAccount(account);
        var persistedAccount = accountRepository.save(account);

        eventPublisher.publishEvent(new RegistrationCompleteEvent(persistedAccount, confirmationBaseUrl, locale));
        return accountMapper.toDto(persistedAccount);
    }

    @Transactional
    public void confirmRegistration(String token) {
        var account = accountRepository.findByVerificationToken(token)
            .orElseThrow(
                () -> new NotFoundException(String.format("Could not find a user with verification-token '%s'", token)));

        if (account.getVerificationToken().isExpired()) {
            throw new TokenExpiredException();
        }

        account.setVerified(true);
        account.setVerificationToken(null);
        accountRepository.save(account);
        verificationTokenRepository.deleteByToken(token);
    }

    /**
     * Revokes the calling {@link ParticipantAccount participant's} approval of all {@link at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Pairing}s with the other
     * {@link ParticipantAccount participant} identified by his {@link Account#getEmailLowercase() email}.<br>
     * If the caller already doesn't approve a {@link at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Pairing}, nothing changes for this {@link at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Pairing}.
     *
     * @param matchedUserEmail The {@link Account#getEmailLowercase() email} of the user requesting the operation.
     * @param userEmail        The {@link Account#getEmailLowercase() email} of the other user in question.
     */
    public void revokePairingApprovals(String matchedUserEmail, String userEmail) {
        var pairings = pairingRepository.findUserPairings(matchedUserEmail, userEmail);
        for (var pairing : pairings) {
            if (pairing.getInitiator().getEmailLowercase().equalsIgnoreCase(userEmail)) {
                pairing.setInitiatorApprovedMatch(false);
            } else {
                pairing.setPairedPersonApprovedMatch(false);
            }
        }
        pairingRepository.saveAll(pairings);
    }

    /**
     * Initiate forgot password of a user by sending a mail.
     *
     * @param forgotPwdDto contains email of the account who forgot password
     * @return the new registered account
     */
    public AccountDto forgotPasswordUser(ForgotPwdDto forgotPwdDto, Locale locale) {
        log.debug("Send forgot password link for a participant account");
        Account account = findAccountByEmail(forgotPwdDto.getEmail());
        createForgotPasswordTokenForAccount(account);
        accountRepository.save(account);
        eventPublisher.publishEvent(
            new ForgotPasswordEvent(account, forgotPwdDto.getResetPasswordBaseUrl(), locale));
        return accountMapper.toDto(account);
    }

    @Transactional
    public void resetNewPassword(ResetPwdDto resetPwdDto) {
        var account = accountRepository.findByForgotPasswordToken(resetPwdDto.getToken())
            .orElseThrow(
                () -> new NotFoundException(
                    String.format("Could not find a user with verification-token '%s'", resetPwdDto.getToken())));

        if (account.getForgotPasswordToken().isExpired()) {
            throw new TokenExpiredException();
        }
        var hashedPassword = passwordEncoder.encode(resetPwdDto.getPassword());
        account.setPassword(hashedPassword);
        account.setForgotPasswordToken(null);
        accountRepository.save(account);
        verificationTokenRepository.deleteByToken(resetPwdDto.getToken());
    }

    /**
     * Initiate change password of a user by generating a token.
     *
     * @param email of the account who wants to change password
     * @return the dto with the token
     */
    public ChangePwdDto getTokenForPasswordChange(String email) {
        log.debug("Send forgot password link for a participant account");
        Account account = findAccountByEmail(email);
        createForgotPasswordTokenForAccount(account);
        accountRepository.save(account);

        return new ChangePwdDto(email, account.getForgotPasswordToken().toString());
    }

    /**
     * Updates organizer with given values.
     * These values are validated by mapper in AccountController.
     *
     * @param accountDto   The data to update the account with.
     * @param accountEmail The email identifying the account to update.
     * @return The representation of the updated account.
     */
    public AccountDto updateAccount(AccountDto accountDto, String accountEmail) {
        var account = findAccountByEmail(accountEmail);
        var newEmailLowerCase = accountDto.getEmail().toLowerCase(Locale.ROOT);
        if (!account.getEmailLowercase().equals(newEmailLowerCase)
            && accountRepository.existsByEmailLowercase(newEmailLowerCase)) {
            throw new IllegalUserArgumentException("Email already taken!");
        }

        accountMapper.updateEntity(account, accountDto);
        return accountMapper.toDto(accountRepository.save(account));
    }

    /**
     * Deactivate organizer account by email.
     * All future events by this organizer will be deleted.
     * All previous events contain as before the organizer info.
     *
     * @param email of organizer to be deactivated.
     */
    public void deactivateOrganizerAccount(String email) {
        log.debug("deactivateOrganizerAccount({})", email);
        var events = eventRepository.getAllFutureEventsByOrganizer(email, LocalDateTime.now());
        events.forEach(e -> eventService.cancelEventByAccessToken(e.getAccessToken(), email));
        OrganizerAccount account = findAccountByEmail(email, OrganizerAccount.class);
        account.setDeactivated(true);
        accountRepository.save(account);
    }

    /**
     * Delete participant account by email.
     *
     * @param email of participant to be delete.
     */
    public void deleteParticipantAccount(String email) {
        log.debug("deleteParticipantAccount({})", email);
        Account account = findAccountByEmail(email);
        accountRepository.delete(account);
    }

    /**
     * Ban or unban an account by email.
     *
     * @param toggleAccountBanDto to be banned or unbanned.
     */
    public AccountDto toggleBanStatus(ToggleAccountBanDto toggleAccountBanDto) {
        log.debug("toggleBanStatus({})", toggleAccountBanDto);
        Account account = findAccountByEmail(toggleAccountBanDto.getEmail());
        account.setBanned(!account.isBanned());
        if (account.isBanned()) {
            account.setBanReason(toggleAccountBanDto.getBanReason());
        } else {
            account.setBanReason(null);
        }
        return accountMapper.toDto(accountRepository.save(account));
    }

    /**
     * record login failure
     * ban user if user has five or more attempts
     *
     * @param email of user with failed login.
     */
    public AccountDto recordLoginFailure(String email) {
        log.debug("recordLoginFailure({})", email);
        Account account = findAccountByEmail(email);
        int loginFailCount = account.getLoginFailCount() + 1;
        account.setLoginFailCount(loginFailCount);
        if (loginFailCount >= 5) {
            account.setLoginFailCount(0);
            account.setBanned(true);
            account.setBanReason("Five or more failed login attempts");
        }
        return accountMapper.toDto(accountRepository.save(account));
    }

    /**
     * reset login failure
     *
     * @param email of user with failed login.
     */
    public void resetLoginFailure(String email) {
        log.debug("resetLoginFailure({})", email);
        Account account = findAccountByEmail(email);
        account.setLoginFailCount(0);
        accountRepository.save(account);
    }

    /**
     * @param email The {@link Account#getEmailLowercase() email} of the user requesting his or her
     *              {@link UserStatusDto status}.
     * @return The {@link UserStatusDto status} of the user with the given {@link Account#getEmailLowercase() email},
     *         indicating whether he or she is currently at an ongoing event.
     */
    public UserStatusDto getUserStatus(String email) {
        Optional<Event> event = findEventUserIsCurrentlyPresentAtIfAny(email);

        var userStatus = new UserStatusDto();
        userStatus.setCurrentlyAtEvent(event.isPresent());
        userStatus.setEventAccessToken(event.map(Event::getAccessToken).orElse(null));
        return userStatus;
    }

    /**
     * Retrieve the current participation status of a participant.
     * @param email the email of the user whose status will be retrieved
     * @return the participation status of the user, 'null' if they do not have a status
     */
    @Transactional
    public ParticipationStatusDto getParticipationStatus(final String email) {
        var participant = findAccountByEmail(email, ParticipantAccount.class);
        return findEventUserIsCurrentlyPresentAtIfAny(email)
            .flatMap(event -> participationRepository.findParticipation(event.getAccessToken(), participant.getId()))
            .map(participation -> {
                final var participationStatus = new ParticipationStatusDto();
                participationStatus.setOwnPairingToken(participant.getPairingTokenForCurrentEvent());
                participationStatus.setStatus(participation.getStatus().getId());

                var event = participation.getEvent();
                var pairingRoundOptional = eventExecutionService.getLatestPairingRound(event);
                if (pairingRoundOptional.isPresent()) {
                    var pairingRound = pairingRoundOptional.get();
                    participationStatus.setRoundStartedAt(pairingRound.getStartedAt());

                    var pairing = pairingRepository.findByRoundAndParticipant(pairingRound.getId(), participant.getId())
                        .orElse(null);

                    if (pairing != null) {
                        var otherPerson = pairing.getPartnerOf(participant).orElseThrow();
                        participationStatus.setOtherPersonsNickname(otherPerson.getNickname());
                        participationStatus.setOtherPersonsPairingToken(otherPerson.getPairingTokenForCurrentEvent());
                    }
                }
                return participationStatus;
            })
            .orElse(null);
    }


    /**
     * @param email The {@link Account#getEmailLowercase() email} of the user in question.
     * @return The ongoing {@link Event} the user with the given email is currently at, if such an {@link Event} exists,
     *         or {@link java.util.Optional#empty()} otherwise.
     */
    private Optional<Event> findEventUserIsCurrentlyPresentAtIfAny(String email) {
        var user = findAccountByEmail(email);
        Optional<Event> event;
        if (user instanceof OrganizerAccount) {
            event = eventRepository.findOngoingEventFromOrganizer(email);
        } else {
            event = eventRepository.findOngoingEventWithArrivedParticipant(email);
        }
        return event;
    }

    private void createRegistrationTokenForAccount(Account account) {
        account.setVerificationToken(createAndSaveToken());
    }

    private void createForgotPasswordTokenForAccount(Account account) {
        account.setForgotPasswordToken(createAndSaveToken());
    }

    private VerificationToken createAndSaveToken() {
        var token = new VerificationToken();
        return verificationTokenRepository.save(token);
    }

    /**
     * Retrieve all roles for an account with the given id.
     * The roles are classifications of the account types as of now.
     *
     * @param id the id to look for
     * @return all authorities for that account
     */
    private List<GrantedAuthority> getAuthorities(final Long id) {
        log.trace("Retrieve all authorities for user with id {}", id);

        final var account = this.accountRepository.findById(id);
        final var authorities = new ArrayList<GrantedAuthority>();
        if (account.isPresent()) {
            authorities.add(new SimpleGrantedAuthority(USER_ROLE));
            if (account.get().isVerified()) {
                if (this.organizerAccountRepository.existsById(id)) {
                    authorities.add(new SimpleGrantedAuthority(ORGANIZER_ROLE));
                } else if (this.participantAccountRepository.existsById(id)) {
                    authorities.add(new SimpleGrantedAuthority(PARTICIPANT_ROLE));
                } else if (this.adminAccountRepository.existsById(id)) {
                    authorities.add(new SimpleGrantedAuthority(ADMIN_ROLE));
                }
            }
        }
        return authorities;
    }
}
