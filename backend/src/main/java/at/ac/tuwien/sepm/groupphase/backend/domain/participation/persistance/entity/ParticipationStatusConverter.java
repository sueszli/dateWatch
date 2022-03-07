package at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;


/**
 * Converts {@link ParticipationStatus participation statuses} to their database representations and vice versa. </br>
 * See https://www.baeldung.com/jpa-persisting-enums-in-jpa for the benefits.
 */
@Converter
public class ParticipationStatusConverter implements AttributeConverter<ParticipationStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ParticipationStatus status) {
        return status == null ? null : status.getId();
    }

    @Override
    public ParticipationStatus convertToEntityAttribute(Integer id) {
        return id == null
            ? null
            : Stream.of(ParticipationStatus.values())
                .filter(status -> status.getId() == id)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
