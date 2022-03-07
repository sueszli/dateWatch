package at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.sse;

import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.EventStatus;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;


@EqualsAndHashCode(callSuper = true)
@JsonTypeName(EventStatusChangedDto.EVENT_TYPE)
@Getter
public class NewUpcomingRoundDto extends EventStatusChangedDto {


    /**
     * The {@link at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.ParticipantAccount#getPairingTokenForCurrentEvent() pairing token}
     * of the {@link at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.ParticipantAccount participant}
     * receiving this event.
     */
    private final String pairingToken;

    public NewUpcomingRoundDto(final String accessToken, final String pairingToken) {
        super(accessToken, EventStatus.UPCOMING_ROUND_ABOUT_TO_START.getId(), LocalDateTime.now());
        this.pairingToken = pairingToken;
    }
}
