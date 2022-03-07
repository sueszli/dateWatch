package at.ac.tuwien.sepm.groupphase.backend.domain.event.dto;

import at.ac.tuwien.sepm.groupphase.backend.common.validation.NotNullWhenAdding;
import at.ac.tuwien.sepm.groupphase.backend.common.validation.NullOrNotBlank;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;


@Getter
@Setter
@AllArgsConstructor
public class EventGroupDto {

    @Size(max = Event.MAX_LENGTH_GROUP_TITLE)
    @NullOrNotBlank
    @NotNullWhenAdding
    private String title;

    @Size(max = Event.MAX_LENGTH_GROUP_DESCRIPTION)
    @NullOrNotBlank
    @NotNullWhenAdding
    private String description;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long numberOfRegistrations;
}
