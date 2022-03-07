package at.ac.tuwien.sepm.groupphase.backend.domain.event.service.mapper;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.exception.NoMapperForOperationException;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.Account;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.OrganizerAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.ParticipantAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.*;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.EventGroup;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.EventGroups;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.dto.ParticipationDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;


@Mapper
@Transactional
@RequiredArgsConstructor
public abstract class EventMapper {

    @Autowired
    protected ParticipationRepository participationRepository;


    public abstract Event toEntity(AddUpdateEventDto addUpdateEventDto);

    public abstract EventGroupDto toDto(EventGroup group);

    public abstract EventGroupsDto toDto(EventGroups groups);

    @Mapping(target = "status", source = "event.status.id")
    public abstract OrganizerEventDetailsDto toOrganizerDetailsDto(Event event);

    @Mapping(target = "organizer.email", source = "event.organizer.emailLowercase")
    @Mapping(target = "status", source = "event.status.id")
    public abstract ParticipantEventDetailsDto toParticipantDetailsDto(Event event, Long participantId);

    public EventDetailsDto toDto(Event event, Account account) {
        if (account instanceof ParticipantAccount) {
            return toParticipantDetailsDto(event, account.getId());
        } else if (account instanceof OrganizerAccount) {
            return toOrganizerDetailsDto(event);
        }

        throw new NoMapperForOperationException(
            "There is no mapper to map event details for an account of type " + account.getClass().getName()
                + ". Add a mapping to EventMapper.toDto(Event event, Account account)."
        );
    }

    public List<EventDetailsDto> toDtos(List<Event> events, Account account) {
        return events.stream().map(event -> toDto(event, account)).collect(Collectors.toList());
    }

    @AfterMapping
    protected void setNumberOfRegistrations(Event event, @MappingTarget EventDetailsDto dto) {
        var eventId = event.getId();
        if (event.hasGroups()) {
            setNumberOfRegistrationsForGroup(eventId, dto.getGroups().getFirstGroup());
            setNumberOfRegistrationsForGroup(eventId, dto.getGroups().getSecondGroup());
        } else {
            dto.setNumberOfRegistrations(participationRepository.countAllByEventId(eventId));
        }
    }

    @BeforeMapping
    protected void setParticipantRegistrationDetails(Event event, Long participantId,
                                                     @MappingTarget ParticipantEventDetailsDto dto) {
        var participation =
            participationRepository.findParticipation(event.getAccessToken(), participantId);

        if (participation.isPresent()) {
            var participationDto = new ParticipationDto();
            participationDto.setConfirmed(participation.get().isConfirmed());
            participationDto.setRegistered(participation.get().isRegistered());
            if (event.hasGroups()) {
                participationDto.setGroup(participation.get().getGroup().getTitle());
            } else {
                participationDto.setGroup(null);
            }
            dto.setParticipation(participationDto);
        }
    }

    protected void setNumberOfRegistrationsForGroup(Long eventId, EventGroupDto groupDto) {
        groupDto.setNumberOfRegistrations(
            participationRepository.countAllByEventIdAndGroupTitle(eventId, groupDto.getTitle()));
    }
}

