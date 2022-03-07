package at.ac.tuwien.sepm.groupphase.backend.domain.event.dto;

import at.ac.tuwien.sepm.groupphase.backend.common.validation.NullOrNotBlank;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Feedback;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Getter
@Setter
public class FeedbackDto {

    @NullOrNotBlank
    private String eventAccessToken;

    @NullOrNotBlank
    private String eventTitle;

    @Size(max = Feedback.MAX_LENGTH_MESSAGE)
    @NotNull
    private String message;
}
