package at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Indicates the status for a filter search.
 */
public enum FilterType {
    EVENTS_ORGANIZER(10),
    EVENTS_PARTICIPANT_PUBLIC(20),
    EVENTS_PARTICIPANT_PLANNED(30),
    EVENTS_PARTICIPANT_VISITED(40);

    private final int id;

    FilterType(int id) {
        this.id = id;
    }

    @JsonCreator
    public static FilterType fromInt(int id) {
        for (FilterType f : FilterType.values()) {
            if (f.getId() == id) {
                return f;
            }
        }
        throw new IllegalArgumentException();
    }

    @JsonValue
    public int getId() {
        return id;
    }
}
