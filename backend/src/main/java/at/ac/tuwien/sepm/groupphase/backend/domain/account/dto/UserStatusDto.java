package at.ac.tuwien.sepm.groupphase.backend.domain.account.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserStatusDto {
    private boolean currentlyAtEvent;
    private String eventAccessToken;
}
