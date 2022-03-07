package at.ac.tuwien.sepm.groupphase.backend.domain.participation.dto.sse;

import at.ac.tuwien.sepm.groupphase.backend.common.sse.SseDto;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;


@Data
@JsonTypeName(ArrivalStatisticsDto.EVENT_TYPE)
public class ArrivalStatisticsDto implements SseDto {

    protected final static String EVENT_TYPE = "arrivals";

    private Long arrivedParticipants;
    private Long arrivedFirstGroupParticipants;
    private Long arrivedSecondGroupParticipants;


    @Override
    public String getEventType() {
        return ArrivalStatisticsDto.EVENT_TYPE;
    }
}
