package at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.ParticipantAccount;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;
import java.util.Optional;


@Getter
@Setter
@Entity
public class Pairing {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private ParticipantAccount initiator;

    @ManyToOne
    @JoinColumn(name = "datee_id", nullable = false)
    private ParticipantAccount pairedPerson;

    @Column(nullable = false)
    private boolean initiatorApprovedMatch = false;

    @Column(nullable = false)
    private boolean pairedPersonApprovedMatch = false;

    /**
     * All {@link Pairing}s are confirmed automatically when the organizer ends the corresponding {@link Event}.
     */
    @Column(nullable = false)
    private boolean confirmed = false;


    @ManyToOne
    private PairingRound pairingRound;

    public Optional<ParticipantAccount> getPartnerOf(final ParticipantAccount participantAccount) {
        if (Objects.equals(getInitiator().getId(), participantAccount.getId())) {
            return Optional.of(getPairedPerson());
        }
        if (Objects.equals(getPairedPerson().getId(), participantAccount.getId())) {
            return Optional.of(getInitiator());
        }
        return Optional.empty();
    }

    public boolean wasMatch() {
        return initiatorApprovedMatch && pairedPersonApprovedMatch;
    }
}
