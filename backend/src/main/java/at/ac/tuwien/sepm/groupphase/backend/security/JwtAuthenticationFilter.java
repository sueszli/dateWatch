package at.ac.tuwien.sepm.groupphase.backend.security;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.AccountDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.LoginDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.Account;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.OrganizerAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.service.AccountService;
import at.ac.tuwien.sepm.groupphase.backend.security.config.properties.SecurityProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtTokenizer jwtTokenizer;
    private final AuthenticationManager authenticationManager;
    private final AccountService accountService;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
                                   SecurityProperties securityProperties,
                                   JwtTokenizer jwtTokenizer,
                                   AccountService accountService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenizer = jwtTokenizer;
        setFilterProcessesUrl(securityProperties.getLoginUri());
        this.accountService = accountService;
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException {

        LoginDto user = null;
        try {
            user = new ObjectMapper().readValue(request.getInputStream(), LoginDto.class);
            return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
        } catch (BadCredentialsException exception) {
            AccountDto accountDto = null;
            if (user != null && user.getEmail() != null) {
                log.error("Unsuccessful authentication attempt for user {}", user.getEmail());
                // record login failures
                accountDto = accountService.recordLoginFailure(user.getEmail());
            }
            if (accountDto != null && accountDto.isBanned()) {
                throw new LockedException("The account with this email is banned due to many failed login attempts. " +
                    "If you think it was a mistake, please contact administration under " +
                    "'datewatch.test@gmail.com'.");
            } else {
                throw exception;
            }
        } catch (IOException exception) {
            throw new BadCredentialsException("Wrong API request or JSON schema", exception);
        }
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        if (failed instanceof LockedException) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(failed.getMessage());
            log.info("Invalid authentication attempt: User {} is banned", failed.getMessage());
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(failed.getMessage());
            log.debug("Invalid authentication attempt: {}", failed.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException {
        User user = ((User) authResult.getPrincipal());

        List<String> roles = user.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());
        OrganizerAccount organizerAccount;
        Account account;
        if (roles.contains("ROLE_ORGANIZER") &&
            (organizerAccount = accountService.findAccountByEmail(user.getUsername(), OrganizerAccount.class)).getAccountType().equals("organizer") &&
            organizerAccount.isDeactivated()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Your account is deactivated. If you think it was a mistake," +
                " please contact administration under " +
                "'datewatch.test@gmail.com'.");
            log.info("User {} is deactivated", user.getUsername());
        } else if ((account = accountService.findAccountByEmail(user.getUsername())) != null && account.isBanned()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Your account is banned. If you think it was a mistake," +
                " please contact administration under " +
                "'datewatch.test@gmail.com'.");
            log.info("User {} is banned", user.getUsername());
        } else {
            response.getWriter().write(jwtTokenizer.getAuthToken(user.getUsername(), roles));
            log.info("Successfully authenticated user {}", user.getUsername());
            // reset any failed login attempts
            accountService.resetLoginFailure(user.getUsername());
        }
    }
}
