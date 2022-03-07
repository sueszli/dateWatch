package at.ac.tuwien.sepm.groupphase.backend.domain.participation.event;

import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Pairing;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.Participation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * Triggered when a participation's {@link Participation#getStatus() status} changes and handled by
 * {@link at.ac.tuwien.sepm.groupphase.backend.domain.participation.event.listener.ParticipationStatusChangedListener}.
 */
@Getter
@RequiredArgsConstructor
public class ParticipationStatusChangedEvent {
    private final Participation participation;
    private final Pairing pairing;
    private final boolean notifyOrganizer;
    private final boolean notifyParticipant;
}
