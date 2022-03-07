package at.ac.tuwien.sepm.groupphase.backend.domain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPwdDto {

    @NotBlank
    private String password;

    @NotBlank
    private String token;
}
