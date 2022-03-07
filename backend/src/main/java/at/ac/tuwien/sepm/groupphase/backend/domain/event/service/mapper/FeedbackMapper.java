package at.ac.tuwien.sepm.groupphase.backend.domain.event.service.mapper;

import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.*;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Feedback;
import lombok.RequiredArgsConstructor;
import org.mapstruct.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;


@Mapper
@Transactional
@RequiredArgsConstructor
public abstract class FeedbackMapper {

    public abstract FeedbackDto toDto(Feedback feedback);

    public List<FeedbackDto> toDtos(List<Feedback> feedbacks) {
        return feedbacks.stream().map(this::toDto).collect(Collectors.toList());
    }
}
