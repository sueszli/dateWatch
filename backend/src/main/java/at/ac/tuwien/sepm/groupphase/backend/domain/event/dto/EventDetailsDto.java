package at.ac.tuwien.sepm.groupphase.backend.domain.event.dto;

import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.EventStatus;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "detailsType")
@JsonSubTypes({
    @JsonSubTypes.Type(OrganizerEventDetailsDto.class),
    @JsonSubTypes.Type(ParticipantEventDetailsDto.class)
})
public abstract class EventDetailsDto {

    private String title;

    private String description;

    private LocalDateTime startDateAndTime;

    private Integer durationInMinutes;

    private Integer roundDurationInSeconds;

    private String street;

    private String postcode;

    private String city;

    private Integer maxParticipants;

    private EventGroupsDto groups;

    private boolean isPublic;

    private Long numberOfRegistrations;

    private boolean isActive;

    private boolean hasRegistrationClosed;

    private boolean isOngoing;

    private String accessToken;

    private Integer status;
}
