package at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;


/**
 * Converts {@link EventStatus event statuses} to their database representations and vice versa. </br>
 * See https://www.baeldung.com/jpa-persisting-enums-in-jpa for the benefits.
 */
@Converter
public class EventStatusConverter implements AttributeConverter<EventStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(EventStatus status) {
        return status == null ? null : status.getId();
    }

    @Override
    public EventStatus convertToEntityAttribute(Integer id) {
        return id == null
            ? null
            : Stream.of(EventStatus.values())
                .filter(status -> status.getId() == id)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
