package at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Entity
public class PairingRound {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private LocalDateTime startedAt;

    @ToString.Exclude
    @OneToMany(mappedBy = "pairingRound")
    @Setter(AccessLevel.NONE)
    private List<Pairing> pairings = new ArrayList<>();

    @ToString.Exclude
    @ManyToOne
    @Setter(AccessLevel.NONE)
    private Event event;

    public void addPairing(Pairing pairing) {
        pairings.add(pairing);
    }
}
