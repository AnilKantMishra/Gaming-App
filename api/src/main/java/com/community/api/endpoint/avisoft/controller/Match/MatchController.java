package com.community.api.endpoint.avisoft.controller.Match;

import com.community.api.dto.MatchDTO;
import com.community.api.dto.MatchResponseDTO;
import com.community.api.dto.TeamDTO;
import com.community.api.entity.Match;
import com.community.api.services.MatchService;
import com.community.api.services.TournamentService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import static com.community.api.services.ResponseService.generateErrorResponse;

@RestController
@RequestMapping("/match")
public class MatchController {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    ExceptionHandlingImplement exceptionHandling;

    @Autowired
    MatchService matchService;

    @Transactional
    @PostMapping("/createMatch")
    public ResponseEntity<?> createMatch(@Valid @RequestBody MatchDTO matchRequest) {
        try {

            MatchResponseDTO createdMatch = tournamentService.createMatch(matchRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMatch);
        } catch (IllegalArgumentException e) {
            return generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return generateErrorResponse("Error creating match", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
    @GetMapping("/getAllMatches")
    public ResponseEntity<?> getAllMatches() {
        try {
            List<Match> matches = matchService.getAllMatches();

            List<MatchDTO> matchDTOs = matches.stream()
                    .map(this::convertToMatchDTO)  // Extracted to a method for clarity
                    .collect(Collectors.toList());

            return ResponseEntity.ok(matchDTOs);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return generateErrorResponse("Error fetching matches", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private MatchDTO convertToMatchDTO(Match match) {
        MatchDTO matchDTO = new MatchDTO();
        matchDTO.setId(match.getId());
        matchDTO.setTournamentId(match.getTournament().getId());

        matchDTO.setTeam1Id(match.getTeam1().getId());
        matchDTO.setTeam2Id(match.getTeam2().getId());

        matchDTO.setMatchDate(match.getMatchDate());
        matchDTO.setResult(match.getResult());

        return matchDTO;
    }


    @GetMapping("/getMatchById")
    public ResponseEntity<?> getMatchById(@RequestParam Long id) {
        try {
            Match match = matchService.getMatchById(id);
            if (match == null) {
                return generateErrorResponse("Match not found", HttpStatus.NOT_FOUND);
            }

            MatchDTO matchDTO = convertToMatchDTO(match);
            return ResponseEntity.ok(matchDTO);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return generateErrorResponse("Error fetching match", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
