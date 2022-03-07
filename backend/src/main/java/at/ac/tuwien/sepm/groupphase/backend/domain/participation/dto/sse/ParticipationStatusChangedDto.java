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
@JsonTypeName(ParticipationStatusChangedDto.EVENT_TYPE)
public class ParticipationStatusChangedDto implements SseDto {

    protected final static String EVENT_TYPE = "participationStatus";

    private Integer status;

    private String otherPersonsNickname;
    private String otherPersonsPairingToken;


    @Override
    public String getEventType() {
        return ParticipationStatusChangedDto.EVENT_TYPE;
    }
}