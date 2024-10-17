package com.community.api.dto;
import com.community.api.entity.Prize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PrizeDistributionDTO {
    private Long tournamentId;
    private Long participationId;
    private Long teamId;
    private String prize; // e.g., "Champion", "Runner-up"
    private Double amount;
}