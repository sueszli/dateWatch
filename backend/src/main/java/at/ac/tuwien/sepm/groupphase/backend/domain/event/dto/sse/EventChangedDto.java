package at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.sse;

import at.ac.tuwien.sepm.groupphase.backend.common.sse.SseDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@JsonTypeName(EventChangedDto.EVENT_TYPE)
@AllArgsConstructor
public class EventChangedDto implements SseDto {

    protected static final String EVENT_TYPE = "eventChanged";

    private String accessToken;


    @Override
    public String getEventType() {
        return EventChangedDto.EVENT_TYPE;
    }

    public static EventChangedDto fromEvent(final Event event) {
        return new EventChangedDto(event.getAccessToken());
    }
}

