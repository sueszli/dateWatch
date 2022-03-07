package at.ac.tuwien.sepm.groupphase.backend.domain.event.event.handler;

import at.ac.tuwien.sepm.groupphase.backend.domain.event.event.EventChangedEvent;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;
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
public class EventChangedHandler {

    private final MessageSource messages;
    private final JavaMailSender mailSender;


    public void handleEventChanged(final EventChangedEvent eventChangedEvent) {
        final var participations = eventChangedEvent.getEvent().getParticipations();
        if (!participations.isEmpty()) {
            String changes = getFormatChanges(eventChangedEvent);
            participations.parallelStream().forEach(p -> sendEventChangedMail(p, changes));
        }
    }

    /**
     * Send the mail which tells the participant whether his registration was confirmed or not.
     *
     * @param participation The registration to handle.
     */
    private void sendEventChangedMail(final Participation participation, final String changes) {
        final var event = participation.getEvent();
        final var email = new SimpleMailMessage();
        email.setTo(participation.getParticipant().getEmailLowercase());
        email.setSubject(messages.getMessage("eventChangedSubject", null, Locale.getDefault()));
        email.setText(messages.getMessage("eventChangedTextGeneral", new Object[]{
            event.getTitle(),
            MailUtility.formatDateTimeForMail(event),
            changes
        }, Locale.getDefault()));
        mailSender.send(email);
    }

    /**
     * Format the changes of two events for an email.
     *
     * @param eventChangedEvent the events whose changes should be formatted
     * @return the formatted changes
     */
    private String getFormatChanges(final EventChangedEvent eventChangedEvent) {
        String changes = "";
        Event newDiffEvent = eventChangedEvent.getNewDiffEvent();
        Event oldDiffEvent = eventChangedEvent.getOldDiffEvent();

        if (newDiffEvent.getTitle() != null) {
            changes += messages.getMessage("eventChangedTextTitle", new Object[]{
                oldDiffEvent.getTitle(),
                newDiffEvent.getTitle()
            }, Locale.getDefault());
        }

        if (newDiffEvent.getDescription() != null) {
            changes += messages.getMessage("eventChangedTextDescription", new Object[]{
                oldDiffEvent.getDescription(),
                newDiffEvent.getDescription()
            }, Locale.getDefault());
        }

        if (newDiffEvent.getStartDateAndTime() != null)
            changes += messages.getMessage("eventChangedTextStartDateAndTime", new Object[]{
                MailUtility.formatDateTimeForMail(oldDiffEvent),
                MailUtility.formatDateTimeForMail(newDiffEvent)
            }, Locale.getDefault());

        if (newDiffEvent.getDurationInMinutes() != null)
            changes += messages.getMessage("eventChangedTextDuration", new Object[]{
                MailUtility.formatDuration(oldDiffEvent),
                MailUtility.formatDuration(newDiffEvent)
            }, Locale.getDefault());

        if (!(newDiffEvent.getStreet() == null &&
            newDiffEvent.getPostcode() == null &&
            newDiffEvent.getCity() == null))
            changes += messages.getMessage("eventChangedTextAddress", new Object[]{
                MailUtility.formatAddressForMail(oldDiffEvent),
                MailUtility.formatAddressForMail(newDiffEvent)
            }, Locale.getDefault());

        if (newDiffEvent.getMaxParticipants() != null) {
            changes += messages.getMessage("eventChangedTextMaxPart", new Object[]{
                oldDiffEvent.getMaxParticipants(),
                newDiffEvent.getMaxParticipants()
            }, Locale.getDefault());
        }

        if (newDiffEvent.hasGroups()) {
            changes += messages.getMessage("eventChangedTextGroup", new Object[]{
                oldDiffEvent.getGroups().getFirstGroup().getTitle(),
                oldDiffEvent.getGroups().getFirstGroup().getDescription(),
                oldDiffEvent.getGroups().getSecondGroup().getTitle(),
                oldDiffEvent.getGroups().getSecondGroup().getDescription(),
                newDiffEvent.getGroups().getFirstGroup().getTitle(),
                newDiffEvent.getGroups().getFirstGroup().getDescription(),
                newDiffEvent.getGroups().getSecondGroup().getTitle(),
                newDiffEvent.getGroups().getSecondGroup().getDescription()
            }, Locale.getDefault());
        }

        return changes;
    }
}
