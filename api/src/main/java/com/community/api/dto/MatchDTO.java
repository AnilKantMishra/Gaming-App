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
public class MatchDTO {
    private Long id;
    private Long tournamentId;
    private Long team1Id;
    private Long team2Id;
    private LocalDateTime matchDate;
    private String result;

}
