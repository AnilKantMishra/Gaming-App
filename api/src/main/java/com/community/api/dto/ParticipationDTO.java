package com.community.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ParticipationDTO {
    private Long id;
    private Long tournamentId;
    private Long customerId;
    private Long teamId;
    private String status;
}