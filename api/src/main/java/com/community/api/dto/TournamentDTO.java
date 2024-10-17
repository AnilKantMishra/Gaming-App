package com.community.api.dto;

import com.community.api.entity.Prize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Component
public class TournamentDTO {
    private Long id;
    private String name;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
//    private List<ParticipationDTO> participations;

    private List<PrizeDistributionDTO> prizeDistributions;

}
