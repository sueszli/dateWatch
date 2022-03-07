package at.ac.tuwien.sepm.groupphase.backend.domain.event.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AfterEventStatisticsDto {

    private Integer participants;

    private Integer totalMatches;

    private Integer dateCoveragePercentage;

    private Integer matchRatioPercentage;

    private String eventName;
}
