package at.ac.tuwien.sepm.groupphase.backend.domain.event.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonTypeName("organizerDetails")
public class OrganizerEventDetailsDto extends EventDetailsDto {

    private String entranceToken;
}
