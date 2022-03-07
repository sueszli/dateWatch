package at.ac.tuwien.sepm.groupphase.backend.mail;

import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;


public final class MailUtility {

    /**
     * Format the address of an event for an email.
     *
     * @param event the event whose address should be formatted
     * @return the formatted address
     */
    public static String formatAddressForMail(final Event event) {
        return String.format("%s, %s %s", event.getStreet(), event.getPostcode(), event.getCity());
    }

    /**
     * Format the date and time of an event for an email.
     *
     * @param event the event whose date and time should be formatted
     * @return the formatted date and time
     */
    public static String formatDateTimeForMail(final Event event) {
        return event.getStartDateAndTime().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT));
    }

    /**
     * Format the duration of an event for an email.
     *
     * @param event the event whose duration should be formatted
     * @return the formatted date and time
     */
    public static String formatDuration(final Event event) {
        int duration = event.getDurationInMinutes();
        if (duration < 60) {
            return String.format("%d min", duration);
        } else if (duration % 60 == 0) {
            return String.format("%d h", duration / 60);
        } else {
            return String.format("%d h %d min", duration / 60, duration % 60);
        }
    }
}
