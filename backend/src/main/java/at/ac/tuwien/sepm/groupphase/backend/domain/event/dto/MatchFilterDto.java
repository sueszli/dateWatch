package at.ac.tuwien.sepm.groupphase.backend.domain.event.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchFilterDto {

    private String eventAccessToken;

    private String nickNameSubstring;

}
