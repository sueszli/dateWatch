package at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;


/**
 * Converts {@link FilterType filtertypes} to their database representations and vice versa. </br>
 * See https://www.baeldung.com/jpa-persisting-enums-in-jpa for the benefits.
 */
@Converter
public class FilterTypeConverter implements AttributeConverter<FilterType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(FilterType status) {
        return status == null ? null : status.getId();
    }

    @Override
    public FilterType convertToEntityAttribute(Integer id) {
        return id == null
            ? null
            : Stream.of(FilterType.values())
            .filter(filterType -> filterType.getId() == id)
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
