package at.ac.tuwien.sepm.groupphase.backend.domain.participation.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;


@Getter
@Setter
public class ParticipationStatusDto {

    private Integer status;

    private String otherPersonsNickname;
    private String otherPersonsPairingToken;

    private String ownPairingToken;

    private LocalDateTime roundStartedAt;
}