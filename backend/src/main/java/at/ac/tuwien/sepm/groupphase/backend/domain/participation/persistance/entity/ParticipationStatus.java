package at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity;

import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Indicates the status of a
 * {@link at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.ParticipantAccount participant} at an
 * {@link at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event}.
 * New values may be added and existing values renamed, but existing {@link ParticipationStatus#getId() ids} <b>must not change</b>,
 * because they are persisted in the database and used for identification in jackson (de)serialization. </br>
 * A status id > 20 indicates that the participant in question arrived at the event in question at some point.
 */
public enum ParticipationStatus {
    UNCONFIRMED_REGISTRATION(10),
    CONFIRMED_REGISTRATION(20),
    AT_EVENT_NOT_PAIRED(30),
    AT_EVENT_PAIRED(40),
    LEFT_EVENT(25),
    TURNED_DOWN_CONFIRMED_REGISTRATION(8);

    private final int id;

    ParticipationStatus(int id) {
        this.id = id;
    }

    @JsonValue
    public int getId() {
        return id;
    }
}
