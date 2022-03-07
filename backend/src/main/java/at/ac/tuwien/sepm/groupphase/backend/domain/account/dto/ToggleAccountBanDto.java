package at.ac.tuwien.sepm.groupphase.backend.domain.account.dto;

import at.ac.tuwien.sepm.groupphase.backend.common.validation.NotNullWhenAdding;
import at.ac.tuwien.sepm.groupphase.backend.common.validation.NullOrNotBlank;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.Account;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;


@Getter
@Setter
public class ToggleAccountBanDto {

    @Size(max = Account.MAX_LENGTH_EMAIL)
    @Email
    @NotNullWhenAdding
    private String email;

    @NullOrNotBlank
    private String banReason;
}
