package at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.sse;

import at.ac.tuwien.sepm.groupphase.backend.common.sse.SseDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonTypeName(EventStatusChangedDto.EVENT_TYPE)
@AllArgsConstructor
public class EventStatusChangedDto implements SseDto {

    protected static final String EVENT_TYPE = "eventStatusChanged";

    private String accessToken;

    private Integer status;

    private LocalDateTime firedAt;

    @Override
    public String getEventType() {
        return EventStatusChangedDto.EVENT_TYPE;
    }

    public static EventStatusChangedDto fromEvent(final Event event) {
        return new EventStatusChangedDto(event.getAccessToken(), event.getStatus().getId(), LocalDateTime.now());
    }
}

