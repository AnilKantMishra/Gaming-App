package com.community.api.endpoint.avisoft.controller.Tournament;

import com.community.api.component.Constant;
import com.community.api.dto.MatchDTO;
import com.community.api.dto.PrizeDistributionDTO;
import com.community.api.dto.TournamentDTO;
import com.community.api.entity.*;
import com.community.api.services.ApiConstants;
import com.community.api.services.ResponseService;
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
import java.util.Map;

import static com.community.api.services.ResponseService.generateErrorResponse;
import static com.community.api.services.ResponseService.generateSuccessResponse;

@RestController
@RequestMapping("/tournaments")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    ExceptionHandlingImplement exceptionHandling;

    @GetMapping("/getActiveTournaments")
    public ResponseEntity<?> getActiveTournaments() {
        try {
            List<TournamentDTO> activeTournaments = tournamentService.getActiveTournaments();
            return ResponseService.generateSuccessResponse("Active Tournaments found", activeTournaments, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return generateErrorResponse("Error fetching active tournaments", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getTournamentById")
    public ResponseEntity<?> getTournamentById(@RequestParam Long id) {
        try {
            Tournament tournament = tournamentService.getTournamentById(id);
            if (tournament == null) {
                return ResponseService.generateErrorResponse("Tournament not found", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("Tournament found by Id", tournament, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return generateErrorResponse("Error fetching tournament", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PostMapping("/createTournament")
    public ResponseEntity<?> createTournament(@Valid @RequestBody Tournament tournament) {
        try {
            Tournament createdTournament = tournamentService.createTournament(tournament);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTournament);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return generateErrorResponse("Error creating tournament", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/endTournament")
    public ResponseEntity<?> endTournament(@RequestParam Long id) {
        try {
            Tournament endedTournament = tournamentService.endTournament(id);
            return ResponseEntity.ok().body(endedTournament);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error ending tournament", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/joinTournament")
    public ResponseEntity<?> joinTournament(@RequestBody Map<String, Object> tournamentDetails) {
        try {
            if (tournamentDetails == null) {
                return ResponseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

            Long tournamentId = (Long) tournamentDetails.get("tournamentId");
            Integer customerId = (Integer) tournamentDetails.get("customerId");

            if (tournamentId == null || tournamentId <= 0) {
                return ResponseService.generateErrorResponse("Invalid tournament ID", HttpStatus.BAD_REQUEST);
            }

            Tournament tournament = entityManager.find(Tournament.class, tournamentId);
            if (tournament == null) {
                return ResponseService.generateErrorResponse("Tournament with this ID does not exist: " + tournamentId, HttpStatus.NOT_FOUND);
            }

            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customerId);
            if (customCustomer == null) {
                return ResponseService.generateErrorResponse("Customer with this ID does not exist: " + customerId, HttpStatus.NOT_FOUND);
            }

            Participation participation = tournamentService.joinTournament(tournamentId, customCustomer);
            return ResponseService.generateSuccessResponse("Customer with ID " + customerId + " has joined the tournament.", participation, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error joining tournament", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/deleteTournament")
    public ResponseEntity<?> deleteTournament(@RequestParam Long id) {
        try {
            Tournament tournament = tournamentService.getTournamentById(id);
            if (tournament == null) {
                return ResponseService.generateErrorResponse("Tournament not found", HttpStatus.NOT_FOUND);
            }
            tournamentService.deleteTournament(id);
            return ResponseService.generateSuccessResponse("Tournament has been deleted", null, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return generateErrorResponse("Error deleting tournament", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/distributePrizes")
    public ResponseEntity<?> distributePrizes(@Valid @RequestBody List<PrizeDistributionDTO> prizeDistributions) {
        try {
            for (PrizeDistributionDTO prizeDistribution : prizeDistributions) {
                tournamentService.distributePrizes(prizeDistribution);
            }

            return generateSuccessResponse("Prizes distributed successfully.", null,HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return generateErrorResponse("Error distributing prizes", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
