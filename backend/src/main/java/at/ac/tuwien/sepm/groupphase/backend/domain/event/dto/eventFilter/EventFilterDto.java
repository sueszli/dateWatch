package at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.eventFilter;

import at.ac.tuwien.sepm.groupphase.backend.common.validation.NullOrNotBlank;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.FilterType;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
public class EventFilterDto {

    @NullOrNotBlank
    private String titleSubstring;

    @DateTimeFormat
    private LocalDateTime startDateAndTime;

    @DateTimeFormat
    private LocalDateTime endDateAndTime;

    @NullOrNotBlank
    private String citySubstring;

    @NullOrNotBlank
    private String organizerNameSubstring;

    private FilterType filterType;

    private Boolean isPublic;

}
