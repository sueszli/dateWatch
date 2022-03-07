package at.ac.tuwien.sepm.groupphase.backend.domain.account.dto;

import at.ac.tuwien.sepm.groupphase.backend.common.validation.NotNullWhenAdding;
import at.ac.tuwien.sepm.groupphase.backend.common.validation.NullOrNotBlank;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.AdminAccount;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;


@Getter
@Setter
@JsonTypeName("admin")
public class AdminAccountDto extends AccountDto {

    @Size(max = AdminAccount.MAX_LENGTH_NAME)
    @NullOrNotBlank()
    @NotNullWhenAdding
    private String name;
}
