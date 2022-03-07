package at.ac.tuwien.sepm.groupphase.backend.domain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePwdDto {

    @Email
    @NotNull
    private String email;

    @NotBlank
    private String token;
}
