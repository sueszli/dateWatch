package at.ac.tuwien.sepm.groupphase.backend.domain.event.event.handler;

import at.ac.tuwien.sepm.groupphase.backend.domain.event.event.EventStatusChangedEvent;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.Participation;
import at.ac.tuwien.sepm.groupphase.backend.mail.MailUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Locale;


@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationClosedHandler {

    private final MessageSource messages;
    private final JavaMailSender mailSender;


    public void handleRegistrationClosed(final EventStatusChangedEvent eventStatusChangedEvent) {
        final var participations = eventStatusChangedEvent.getEvent().getParticipations();
        participations.parallelStream().forEach(this::sendRegistrationClosedMail);
    }

    /**
     * Send the mail which tells the participant whether his registration was confirmed or not.
     *
     * @param participation The registration to handle.
     */
    private void sendRegistrationClosedMail(final Participation participation) {
        final var event = participation.getEvent();
        final var subjectCode = participation.isConfirmed() ? "eventRegistrationConfirmedSubject" : "eventClosedUnconfirmedSubject";
        final var textCode = participation.isConfirmed() ? "eventRegistrationConfirmedText" : "eventClosedUnconfirmedText";
        final var email = new SimpleMailMessage();
        email.setTo(participation.getParticipant().getEmailLowercase());
        email.setSubject(messages.getMessage(subjectCode, null, Locale.getDefault()));
        email.setText(messages.getMessage(textCode, new Object[]{
            event.getTitle(),
            MailUtility.formatDateTimeForMail(event),
            MailUtility.formatAddressForMail(event)
        }, Locale.getDefault()));
        mailSender.send(email);
    }
}
