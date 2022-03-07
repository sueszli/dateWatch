package at.ac.tuwien.sepm.groupphase.backend.domain.account.dto;

import at.ac.tuwien.sepm.groupphase.backend.common.validation.NotNullWhenAdding;
import at.ac.tuwien.sepm.groupphase.backend.common.validation.NullOrNotBlank;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.OrganizerAccount;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;


@Getter
@Setter
@JsonTypeName("organizer")
@EqualsAndHashCode(callSuper = true)
public class OrganizerAccountDto extends AccountDto {

    @Size(max = OrganizerAccount.MAX_LENGTH_ORGANIZATION_NAME)
    @NullOrNotBlank()
    private String organizationName;

    @Size(max = OrganizerAccount.MAX_LENGTH_CONTACT_PERSON_FIRST_NAME)
    @NullOrNotBlank()
    @NotNullWhenAdding
    private String contactPersonFirstName;

    @Size(max = OrganizerAccount.MAX_LENGTH_CONTACT_PERSON_LAST_NAME)
    @NullOrNotBlank()
    @NotNullWhenAdding
    private String contactPersonLastName;

    private boolean isDeactivated = false;
}
