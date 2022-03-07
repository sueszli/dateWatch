package at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.ParticipantAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.EventGroup;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "participant_id"}))
public class Participation {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "event_id")
    @ToString.Exclude
    private Event event;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "group_id")
    private EventGroup group;

    @ManyToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "participant_id")
    @ToString.Exclude
    private ParticipantAccount participant;

    @Convert(converter = ParticipationStatusConverter.class)
    private ParticipationStatus status = ParticipationStatus.UNCONFIRMED_REGISTRATION;

    @Column(nullable = false)
    private Boolean arrivedAtEvent = false; // Boolean for better auto generated getter name

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime created_at;


    /**
     * @param status The {@link ParticipationStatus} to check for.
     * @return True if the given {@link ParticipationStatus} matches this participation's status, false otherwise.
     */
    public boolean hasStatus(ParticipationStatus status) {
        return this.status.equals(status);
    }

    /**
     * @return True if the registration for the event was confirmed, false otherwise.
     */
    public boolean isConfirmed() {
        return status.getId() >= ParticipationStatus.CONFIRMED_REGISTRATION.getId();
    }

    /**
     * @return True if the registration for the event was confirmed, false otherwise.
     */
    public boolean isRegistered() {
        return status.getId() >= ParticipationStatus.UNCONFIRMED_REGISTRATION.getId();
    }

    /**
     * @return True if the corresponding participant currently is present at the ongoing event, false otherwise.
     */
    public boolean isPresentAtEvent() {
        return status.equals(ParticipationStatus.AT_EVENT_PAIRED) ||
            status.equals(ParticipationStatus.AT_EVENT_NOT_PAIRED);
    }
}
