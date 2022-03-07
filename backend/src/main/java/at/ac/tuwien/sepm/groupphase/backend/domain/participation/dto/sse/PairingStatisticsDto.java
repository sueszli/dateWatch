package at.ac.tuwien.sepm.groupphase.backend.domain.participation.dto.sse;

import at.ac.tuwien.sepm.groupphase.backend.common.sse.SseDto;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName(PairingStatisticsDto.EVENT_TYPE)
public class PairingStatisticsDto implements SseDto {

    protected final static String EVENT_TYPE = "pairings";

    private Long formedPairingsForUpcomingRound;

    private Long maximumPairingsPossible;

    @Override
    public String getEventType() {
        return PairingStatisticsDto.EVENT_TYPE;
    }
}
