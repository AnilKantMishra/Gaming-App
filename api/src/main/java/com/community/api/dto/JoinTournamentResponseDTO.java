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
public class JoinTournamentResponseDTO {
    private Long tournamentId;
    private Long teamId;
    private Long participationId;
    private String message;
}
