package at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity;

import at.ac.tuwien.sepm.groupphase.backend.common.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.OrganizerAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.Participation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Event {

    public static final int MAX_LENGTH_TITLE = 300;
    public static final int MAX_LENGTH_DESCRIPTION = 1000;
    public static final int MAX_LENGTH_DURATION = 5;
    public static final int MAX_LENGTH_ROUND_DURATION = 4;
    public static final int MAX_LENGTH_STREET = 100;
    public static final int MAX_LENGTH_POSTCODE = 4;
    public static final int MIN_LENGTH_POSTCODE = 4;
    public static final int MAX_LENGTH_CITY = 50;
    public static final int MAX_LENGTH_TOKEN = 10;
    public static final int MAX_LENGTH_GROUP_TITLE = 100;
    public static final int MAX_LENGTH_GROUP_DESCRIPTION = 300;


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = MAX_LENGTH_TITLE)
    private String title;

    @Column(nullable = false, length = MAX_LENGTH_DESCRIPTION)
    private String description;

    @Column(nullable = false)
    private LocalDateTime startDateAndTime;

    @Column(nullable = false, length = MAX_LENGTH_DURATION)
    private Integer durationInMinutes;

    @Column(nullable = false, length = MAX_LENGTH_ROUND_DURATION)
    private Integer roundDurationInSeconds;

    @Column(nullable = false, length = MAX_LENGTH_STREET)
    private String street;

    @Column(nullable = false, length = MAX_LENGTH_POSTCODE)
    private String postcode;

    @Column(nullable = false, length = MAX_LENGTH_CITY)
    private String city;

    @Column(nullable = false)
    private Integer maxParticipants;

    @Column(unique = true, nullable = false, length = MAX_LENGTH_TOKEN)
    private String accessToken;

    @Column(unique = true, nullable = false, length = MAX_LENGTH_TOKEN)
    private String entranceToken;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_collection_id")
    @Cascade(org.hibernate.annotations.CascadeType.PERSIST)
    private EventGroups groups;

    @Column(nullable = false)
    private boolean isPublic;

    @Convert(converter = EventStatusConverter.class)
    private EventStatus status = EventStatus.REGISTRATION_OPEN;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organizer_id", nullable = false)
    private OrganizerAccount organizer;

    @ToString.Exclude
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Participation> participations = new HashSet<>();

    @ToString.Exclude
    @OneToMany
    @JoinColumn(name = "event_id")
    private List<PairingRound> pairingRounds = new ArrayList<>();


    public void addParticipation(Participation participation) {
        participation.setEvent(this);
        this.participations.add(participation);
    }

    public void addPairingRound(PairingRound pairingRound) {
        pairingRounds.add(pairingRound);
    }

    /**
     * @param status The {@link EventStatus} to check for.
     * @return True if the given {@link EventStatus} matches this event's status, false otherwise.
     */
    public boolean hasStatus(EventStatus status) {
        return this.status.equals(status);
    }

    /**
     * @return True if the event is currently taking place, false otherwise.
     */
    public boolean isOngoing() {
        return status.equals(EventStatus.ONGOING_BUT_NO_UPCOMING_ROUND) ||
            status.equals(EventStatus.UPCOMING_ROUND_ABOUT_TO_START) ||
            status.equals(EventStatus.ROUND_ONGOING);
    }

    /**
     * @return True if the event is currently taking place, false otherwise.
     */
    public boolean isFinished() {
        return status.equals(EventStatus.FINISHED);
    }

    /**
     * @return True if the registration for the event was already closed, false otherwise.
     */
    public boolean getHasRegistrationClosed() {
        return status.equals(EventStatus.REGISTRATION_CLOSED);
    }

    /**
     * @return True if the event is active (not canceled), false otherwise.
     */
    public boolean isActive() {
        return status.getId() >= EventStatus.REGISTRATION_OPEN.getId();
    }

    /**
     * @return True if the event was already started by the organizer, false otherwise.
     */
    public boolean wasStarted() {
        return status.getId() >= EventStatus.ONGOING_BUT_NO_UPCOMING_ROUND.getId();
    }

    /**
     * Returns whether this event consists of groups or not.
     *
     * @return 'true' if this event consists of groups, 'false' otherwise.
     */
    public boolean hasGroups() {
        return groups != null;
    }

    public EventGroup getGroupByTitle(String groupTitle) {
        if (hasGroups()) {
            var firstGroup = groups.getFirstGroup();
            if (firstGroup.getTitle().equals(groupTitle)) {
                return firstGroup;
            }
            var secondGroup = groups.getSecondGroup();
            if (secondGroup.getTitle().equals(groupTitle)) {
                return secondGroup;
            }
        }

        throw new NotFoundException("Event does not contain a group with title '" + groupTitle + "'.");
    }

    /**
     * Checks an {@link Event} against this {@link Event} for changes.
     * Only the variables where the value changes, are set with a value.
     *
     * @param updated is an {@link Event} with new values.
     * @return an {@link Event} with the changed values; null when nothing changed
     */
    public Event getDiff(Event updated) {
        Event changes = new Event();
        changes.title = (this.title.equals(updated.title)) ? null : updated.title;
        changes.description = (this.description.equals(updated.description)) ? null : updated.description;
        changes.startDateAndTime = (this.startDateAndTime.equals(updated.startDateAndTime)) ? null : updated.startDateAndTime;
        changes.durationInMinutes = (this.durationInMinutes.equals(updated.durationInMinutes)) ? null : updated.durationInMinutes;
        if (this.street.equals(updated.street) && this.postcode.equals(updated.postcode) && this.city.equals(updated.city)) {
            changes.street = null;
            changes.postcode = null;
            changes.city = null;
        } else {
            changes.street = updated.street;
            changes.postcode = updated.postcode;
            changes.city = updated.city;
        }
        changes.maxParticipants = (this.maxParticipants.equals(updated.maxParticipants)) ? null : updated.maxParticipants;
        changes.groups = (this.groups == null || this.groups.equals(updated.groups)) ? null : updated.groups;

        return changes.mainValuesAreNUll() ? null : changes;
    }

    /**
     * @return 'true' if the values which could be changed are all null, 'false' otherwise.
     */
    private boolean mainValuesAreNUll() {
        return title == null && description == null && startDateAndTime == null
            && durationInMinutes == null && street == null && postcode == null
            && city == null && maxParticipants == null && groups == null;
    }
}
