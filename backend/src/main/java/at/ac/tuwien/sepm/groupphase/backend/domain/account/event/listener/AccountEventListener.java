package at.ac.tuwien.sepm.groupphase.backend.domain.account.event.listener;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.event.AccountActionEvent;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.event.ForgotPasswordEvent;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.event.RegistrationCompleteEvent;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventListener {

    private final MessageSource messages;
    private final JavaMailSender mailSender;


    @Async
    @EventListener(RegistrationCompleteEvent.class)
    public void handleAccountRegistrationEvent(RegistrationCompleteEvent event) {
        log.debug("Handling RegistrationCompleteEvent for user {}", event.getAccount().getId());
        sendVerificationMail(event);
    }

    @Async
    @EventListener(ForgotPasswordEvent.class)
    public void handleForgotPasswordEvent(ForgotPasswordEvent event) {
        log.debug("Handling ForgotPasswordEvent for user {}", event.getAccount().getId());
        sendVerificationMail(event);
    }

    private void sendVerificationMail(RegistrationCompleteEvent event) {
        sendVerificationMail(event, "regConfirmSubject", "regConfirmText",
            event.getAccount().getVerificationToken().toString());
    }

    private void sendVerificationMail(ForgotPasswordEvent event) {
        sendVerificationMail(event, "forgotPwdSubject", "forgotPwdText",
            event.getAccount().getForgotPasswordToken().toString());
    }

    private void sendVerificationMail(AccountActionEvent event, String subjectCode, String textCode, String verificationToken) {
        Account account = event.getAccount();
        String confirmationUrl = event.getBaseUrl() + verificationToken;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(account.getEmailLowercase());
        email.setSubject(messages.getMessage(subjectCode, null, event.getLocale()));
        email.setText(messages.getMessage(textCode, new Object[]{confirmationUrl}, event.getLocale()));
        mailSender.send(email);
    }
}
