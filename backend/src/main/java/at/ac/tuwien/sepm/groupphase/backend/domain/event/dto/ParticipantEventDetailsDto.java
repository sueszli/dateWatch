package at.ac.tuwien.sepm.groupphase.backend.domain.event.dto;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.OrganizerAccountDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.dto.ParticipationDto;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonTypeName("participantDetails")
public class ParticipantEventDetailsDto extends EventDetailsDto {

    private OrganizerAccountDto organizer;

    private ParticipationDto participation;
}
