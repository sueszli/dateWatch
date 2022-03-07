package at.ac.tuwien.sepm.groupphase.backend.domain.participation.service;

import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Pairing;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.event.ParticipationStatusChangedEvent;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.Participation;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.ParticipationStatus;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final ApplicationEventPublisher eventPublisher;


    /**
     * Sets the {@link ParticipationStatus status} of a {@link Participation} and optionally saves the entity and/or
     * publishes a {@link ParticipationStatusChangedEvent} afterwards. </br>
     * Does nothing if the given {@link Participation} already has the given {@link ParticipationStatus status}.
     *
     * @param participation      The {@link ParticipationStatus} whose {@link ParticipationStatus status} is to be changed.
     * @param status             The new {@link ParticipationStatus status}.
     * @param pairing            The pairing to propagate to the {@link ParticipationStatusChangedEvent}
     * @param notifyOrganizer    Whether a {@link ParticipationStatusChangedEvent} should be published to the organizer afterwards.
     * @param notifyParticipant  Whether a {@link ParticipationStatusChangedEvent} should be published to the participant afterwards.
     * @param saveToDbAfterwards Whether the given
     *                           {@link at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event}
     *                           should be persisted in the database after the operation.
     */
    public void setParticipationStatus(Participation participation,
                                       ParticipationStatus status,
                                       Pairing pairing,
                                       boolean notifyOrganizer,
                                       boolean notifyParticipant,
                                       boolean saveToDbAfterwards) {
        if (participation.hasStatus(status)) {
            return;
        }
        participation.setStatus(status);

        if (saveToDbAfterwards) {
            participationRepository.saveAndFlush(participation);
        }
        eventPublisher.publishEvent(new ParticipationStatusChangedEvent(participation, pairing, notifyOrganizer, notifyParticipant));
    }
}
