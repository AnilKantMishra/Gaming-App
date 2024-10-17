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
public class UserDetailDTO {
    private Long userId;
    private String userName; // Assuming you have a name field in CustomCustomer
    private Long teamId;
    private String teamName;
    private Long leagueId;
    private String leagueName;
    private List<TournamentDTO> tournaments;

//    private List<Prize> prizes;

}

