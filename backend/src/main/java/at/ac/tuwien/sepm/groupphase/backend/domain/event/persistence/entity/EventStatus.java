package at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity;

import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Indicates the status of an {@link at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event}.
 * New values may be added and existing values renamed, but existing {@link EventStatus#getId() ids} <b>must not change</b>,
 * because they are persisted in the database and used for identification in jackson (de)serialization. </br>
 * A status id >= 10 indicates that the event in question is active. </br>
 * A status id > 20 indicates that the event in question already started at this point. </br>
 * A status id > 25 indicates that the event in question is currently ongoing.
 */
public enum EventStatus {
    CANCELED(5),
    REGISTRATION_OPEN(10),
    REGISTRATION_CLOSED(20),
    ONGOING_BUT_NO_UPCOMING_ROUND(30),
    UPCOMING_ROUND_ABOUT_TO_START(40),
    ROUND_ONGOING(50),
    FINISHED(25);

    private final int id;

    EventStatus(int id) {
        this.id = id;
    }

    @JsonValue
    public int getId() {
        return id;
    }
}
