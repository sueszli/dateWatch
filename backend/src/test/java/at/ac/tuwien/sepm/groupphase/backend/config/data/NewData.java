package at.ac.tuwien.sepm.groupphase.backend.config.data;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.OrganizerAccountDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.ParticipantAccountDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.ParticipantAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.AddUpdateEventDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.EventGroupDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.EventGroupsDto;

import java.time.LocalDateTime;

public class NewData {

    public static AddUpdateEventDto createAddUpdateEventDto(boolean groups) {
        AddUpdateEventDto addUpdateEventDto = new AddUpdateEventDto();
        addUpdateEventDto.setTitle("A");
        addUpdateEventDto.setDescription("B");
        addUpdateEventDto.setStartDateAndTime(LocalDateTime.now().minusMinutes(5));
        addUpdateEventDto.setDurationInMinutes(120);
        addUpdateEventDto.setRoundDurationInSeconds(120);
        addUpdateEventDto.setStreet("C");
        addUpdateEventDto.setPostcode("1475");
        addUpdateEventDto.setCity("D");
        addUpdateEventDto.setMaxParticipants(20);
        addUpdateEventDto.setPublic(true);
        if (groups) {
            EventGroupDto eventGroupDtoOne = new EventGroupDto("One", "First Group", null);
            EventGroupDto eventGroupDtoTwo = new EventGroupDto("Two", "Second Group", null);

            EventGroupsDto eventGroupsDto = new EventGroupsDto();
            eventGroupsDto.setFirstGroup(eventGroupDtoOne);
            eventGroupsDto.setSecondGroup(eventGroupDtoTwo);

            addUpdateEventDto.setGroups(eventGroupsDto);
        }

        return addUpdateEventDto;
    }

    public static OrganizerAccountDto createOrganizerAccountDto() {
        OrganizerAccountDto organizerAccountDto = new OrganizerAccountDto();
        organizerAccountDto.setEmail("no@no");
        organizerAccountDto.setPassword("123");
        organizerAccountDto.setContactPersonFirstName("no");
        organizerAccountDto.setContactPersonLastName("no");

        return organizerAccountDto;
    }

    public static ParticipantAccountDto createParticipantAccountDto() {
        ParticipantAccountDto participantAccountDto = new ParticipantAccountDto();
        participantAccountDto.setEmail("no@no");
        participantAccountDto.setPassword("123");
        participantAccountDto.setNickname("no");

        return participantAccountDto;
    }
}
