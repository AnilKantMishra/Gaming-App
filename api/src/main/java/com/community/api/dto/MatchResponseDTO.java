package com.community.api.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MatchResponseDTO {
    private Long id;
    private Long tournamentId;
    private TeamDTO team1; // Full team details
    private TeamDTO team2; // Full team details
    private LocalDateTime matchDate;
    private String result;

}
