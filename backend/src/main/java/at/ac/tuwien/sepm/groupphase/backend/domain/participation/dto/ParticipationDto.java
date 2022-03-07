package at.ac.tuwien.sepm.groupphase.backend.domain.participation.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ParticipationDto {

    private String group;
    private boolean isConfirmed;
    private boolean isRegistered;
}
