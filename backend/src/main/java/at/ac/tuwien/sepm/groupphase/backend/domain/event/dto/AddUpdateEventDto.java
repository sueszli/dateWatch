package at.ac.tuwien.sepm.groupphase.backend.domain.event.dto;

import at.ac.tuwien.sepm.groupphase.backend.common.validation.NotNullWhenAdding;
import at.ac.tuwien.sepm.groupphase.backend.common.validation.NullOrNotBlank;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.validation.IsEven;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;


@Getter
@Setter
public class AddUpdateEventDto {

    @Size(max = Event.MAX_LENGTH_TITLE)
    @NullOrNotBlank
    @NotNullWhenAdding(message = "The title is not allowed to be null.")
    private String title;

    @Size(max = Event.MAX_LENGTH_DESCRIPTION)
    @NullOrNotBlank
    @NotNullWhenAdding(message = "The description is not allowed to be null.")
    private String description;

    @DateTimeFormat
    @NotNullWhenAdding(message = "The start-date is not allowed to be null.")
    @Future
    private LocalDateTime startDateAndTime;

    @NotNullWhenAdding(message = "The duration is not allowed to be null.")
    @Positive
    private Integer durationInMinutes;

    @NotNullWhenAdding(message = "The round duration is not allowed to be null.")
    @Positive
    private Integer roundDurationInSeconds;

    @Size(max = Event.MAX_LENGTH_STREET)
    @NullOrNotBlank
    @NotNullWhenAdding(message = "The street is not allowed to be null.")
    private String street;

    @Size(max = Event.MAX_LENGTH_POSTCODE, min = Event.MIN_LENGTH_POSTCODE, message = "The postcode must have 4 digits.")
    @NullOrNotBlank
    @NotNullWhenAdding(message = "The postcode is not allowed to be null.")
    private String postcode;

    @Size(max = Event.MAX_LENGTH_CITY)
    @NullOrNotBlank
    @NotNullWhenAdding(message = "The city is not allowed to be null.")
    private String city;

    @NotNullWhenAdding(message = "The number of possible participants is not allowed to be null.")
    @Positive(message = "The number of possible participants must be positive.")
    @IsEven(message = "The number of possible participants must be even.")
    private Integer maxParticipants;

    @Valid
    private EventGroupsDto groups;

    private boolean isPublic;
}
