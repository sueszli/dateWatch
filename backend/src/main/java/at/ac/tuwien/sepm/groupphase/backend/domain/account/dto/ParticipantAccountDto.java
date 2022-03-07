package at.ac.tuwien.sepm.groupphase.backend.domain.account.dto;

import at.ac.tuwien.sepm.groupphase.backend.common.validation.NotNullWhenAdding;
import at.ac.tuwien.sepm.groupphase.backend.common.validation.NullOrNotBlank;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.*;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


@Getter
@Setter
@JsonTypeName("participant")
@EqualsAndHashCode(callSuper = true)
public class ParticipantAccountDto extends AccountDto {

    @Size(max = ParticipantAccount.MAX_LENGTH_NICKNAME)
    @NullOrNotBlank()
    @NotNullWhenAdding
    private String nickname;

    @Size(min = ParticipantAccount.MIN_LENGTH_PHONE,
        max = ParticipantAccount.MAX_LENGTH_PHONE)
    @NullOrNotBlank()
    @Pattern(regexp = "^(?:00|\\+)[0-9\\s]{14,16}$", message = "Wrong Format! e.g. +43 120 1234567")
    private String phone;
}
