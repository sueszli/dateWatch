package at.ac.tuwien.sepm.groupphase.backend.domain.event.event.handler;

import at.ac.tuwien.sepm.groupphase.backend.domain.event.event.EventStatusChangedEvent;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.EventStatus;
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
public class CancelEventHandler {

    private final MessageSource messages;
    private final JavaMailSender mailSender;


    public void handleCancelEvent(final EventStatusChangedEvent eventStatusChangedEvent) {
        final var participations = eventStatusChangedEvent.getEvent().getParticipations();
        if (eventStatusChangedEvent.getOldEventStatus().equals(EventStatus.REGISTRATION_OPEN)) {
            participations.parallelStream().filter(Participation::isRegistered).forEach(this::sendCancelEventMail);
        } else if (eventStatusChangedEvent.getOldEventStatus().equals(EventStatus.REGISTRATION_CLOSED)) {
            participations.parallelStream().filter(Participation::isConfirmed).forEach(this::sendCancelEventMail);
        }
    }

    /**
     * Send the mail which tells the participant that the event was canceled.
     *
     * @param participation The registration to handle.
     */
    private void sendCancelEventMail(final Participation participation) {
        final var event = participation.getEvent();
        final var email = new SimpleMailMessage();
        email.setTo(participation.getParticipant().getEmailLowercase());
        email.setSubject(messages.getMessage("eventCanceledSubject", null, Locale.getDefault()));
        email.setText(messages.getMessage("eventCanceledText", new Object[]{
            event.getTitle(),
            MailUtility.formatDateTimeForMail(event)
        }, Locale.getDefault()));
        mailSender.send(email);
    }
}
