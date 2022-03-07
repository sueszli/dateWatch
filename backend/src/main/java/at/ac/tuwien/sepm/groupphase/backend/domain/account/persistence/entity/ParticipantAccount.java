package at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity;

import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.Participation;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@ToString
@Entity
@Table(name = "account_participant")
@DiscriminatorValue("participant")
public class ParticipantAccount extends Account {

    public static final int MAX_LENGTH_NICKNAME = 100;
    public static final int MAX_LENGTH_PHONE = 17;
    public static final int MIN_LENGTH_PHONE = 15;


    @Column(nullable = false, length = MAX_LENGTH_NICKNAME)
    private String nickname;

    @Column(length = MAX_LENGTH_PHONE)
    private String phone;

    private String pairingTokenForCurrentEvent;

    @ToString.Exclude
    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
    private List<Participation> registeredForEvents = new ArrayList<>();
}
