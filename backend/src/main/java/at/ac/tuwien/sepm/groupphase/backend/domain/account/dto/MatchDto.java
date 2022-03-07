package at.ac.tuwien.sepm.groupphase.backend.domain.account.dto;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.ParticipantAccount;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchDto {

    public MatchDto(String eventTitle, ParticipantAccount participantAccount) {
        this.eventTitle = eventTitle;
        this.email = participantAccount.getEmailLowercase();
        this.nickname = participantAccount.getNickname();
        this.phone = participantAccount.getPhone();
    }

    private String eventTitle;

    private String email;

    private String nickname;

    private String phone;
}
