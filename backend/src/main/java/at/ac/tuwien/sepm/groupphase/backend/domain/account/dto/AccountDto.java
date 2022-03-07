package at.ac.tuwien.sepm.groupphase.backend.domain.account.dto;

import at.ac.tuwien.sepm.groupphase.backend.common.validation.NotNullWhenAdding;
import at.ac.tuwien.sepm.groupphase.backend.common.validation.NullOrNotBlank;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.Account;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;


@Getter
@Setter
@ToString(exclude = "password")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "accountType")
@JsonSubTypes({
    @JsonSubTypes.Type(OrganizerAccountDto.class),
    @JsonSubTypes.Type(ParticipantAccountDto.class),
    @JsonSubTypes.Type(AdminAccountDto.class)
})
public class AccountDto {

    @Size(max = Account.MAX_LENGTH_EMAIL)
    @Email
    @NotNullWhenAdding
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NullOrNotBlank
    @NotNullWhenAdding
    private String password;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Setter(AccessLevel.NONE)
    private boolean verified;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Setter(AccessLevel.NONE)
    private boolean currentlyPresentAtAnEvent;

    private boolean isBanned = false;

    @NullOrNotBlank
    private String banReason;
}
