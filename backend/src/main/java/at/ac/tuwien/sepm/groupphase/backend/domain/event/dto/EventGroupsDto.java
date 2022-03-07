package at.ac.tuwien.sepm.groupphase.backend.domain.event.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@Getter
@Setter
public class EventGroupsDto {

    @Valid
    @NotNull
    private EventGroupDto firstGroup;

    @Valid
    @NotNull
    private EventGroupDto secondGroup;
}
