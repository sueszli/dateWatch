package at.ac.tuwien.sepm.groupphase.backend.domain.event.event.handler;

import at.ac.tuwien.sepm.groupphase.backend.common.sse.SseConnectionManager;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.ParticipantAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.sse.EventStatusChangedDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.event.EventStatusChangedEvent;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Pairing;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.repository.EventRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.repository.PairingRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.Participation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class EventFinishedHandler {

    private final MessageSource messages;
    private final JavaMailSender mailSender;

    private final EventRepository eventRepository;
    private final PairingRepository pairingRepository;

    private final SseConnectionManager sseConnectionManager;


    /**
     * Confirms all {@link Pairing}s (see {@link Pairing#isConfirmed()}) of the finished {@link Event} and sends out
     * emails to all {@link ParticipantAccount participants}, informing them about how many new matches they made.
     *
     * @param eventStatusChangedEvent The {@link EventStatusChangedEvent} indicating the event that finished.
     */
    public void handleEventFinished(EventStatusChangedEvent eventStatusChangedEvent) {
        var event = eventStatusChangedEvent.getEvent();
        var pairings = pairingRepository.getAllByEventId(event.getId());
        pairings.forEach(pairing -> pairing.setConfirmed(true));
        pairingRepository.saveAll(pairings);

        var arrivedParticipants = eventRepository.getArrivedParticipants(event.getId());
        this.sseConnectionManager.sendMessagesToUsers(arrivedParticipants, participantAccount
            -> new EventStatusChangedDto(event.getAccessToken(), event.getStatus().getId(), LocalDateTime.now()));

        var matchesForEachParticipant = getMatchesForEachParticipant(event, pairings);
        sendMatchNotificationEmails(matchesForEachParticipant);
    }


    /**
     * @param event    The {@link Event} for whose {@link ParticipantAccount participants} all individual matches
     *                 should be retrieved.
     * @param pairings The {@link Pairing}s that happened during the {@link Event}.
     *
     * @return A {@link java.util.Map} containing all the {@link Event}'s {@link ParticipantAccount participants} along
     *         with their individual matches.
     */
    private Map<ParticipantAccount, List<ParticipantAccount>> getMatchesForEachParticipant(Event event, List<Pairing> pairings) {
        return event.getParticipations()
            .stream()
            .filter(Participation::getArrivedAtEvent)
            .map(Participation::getParticipant)
            .collect(Collectors.toMap(
                participant -> participant,
                participant -> pairings
                    .stream()
                    .filter(Pairing::wasMatch)
                    .map(pairing -> pairing.getPartnerOf(participant))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList())));
    }

    /**
     * Sends an email to every participant informing them about how many matches they made.<br>
     * The texts for those emails are retrieved via a {@link MessageSource}.
     *
     * @param matchesForEachParticipant All {@link ParticipantAccount participants} along with their individual matches.
     */
    private void sendMatchNotificationEmails(Map<ParticipantAccount, List<ParticipantAccount>> matchesForEachParticipant) {
        SimpleMailMessage email;
        ParticipantAccount participant;
        List<ParticipantAccount> matches;
        for (var participantsMatches : matchesForEachParticipant.entrySet()) {
            participant = participantsMatches.getKey();
            matches = participantsMatches.getValue();

            email = new SimpleMailMessage();
            email.setTo(participant.getEmailLowercase());
            email.setSubject(messages.getMessage("matchNotificationSubject", null, Locale.getDefault()));
            email.setText(getMatchNotificationText(matches));
            mailSender.send(email);
        }
    }

    /**
     *
     * @param matches All {@link ParticipantAccount participants} that matched with a certain
     * {@link ParticipantAccount participant}.
     *
     * @return The text for the email notifying the {@link ParticipantAccount participant} in question about his matches.
     */
    private String getMatchNotificationText(List<ParticipantAccount> matches) {
        if (matches.isEmpty()) {
            return messages.getMessage("noMatchesText", null, Locale.getDefault());
        } else if (matches.size() == 1) {
            return messages.getMessage("oneMatchText", new Object[]{matches.get(0).getNickname()}, Locale.getDefault());
        } else {
            return messages.getMessage("multipleMatchesText", new Object[]{matches.size()}, Locale.getDefault());
        }
    }
}
