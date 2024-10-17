package com.community.api.endpoint.avisoft.controller.Team;

import com.community.api.dto.TeamDTO;
import com.community.api.entity.Team;
import com.community.api.entity.Tournament;
import com.community.api.services.ResponseService;
import com.community.api.services.TeamService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.community.api.services.ResponseService.generateErrorResponse;

@RestController
@RequestMapping("/teams")
public class TeamController {
    @Autowired
    private TeamService teamService;

    @Autowired
    private ExceptionHandlingService exceptionHandling;

    @GetMapping(value = "/getAllTeams")
    public ResponseEntity<?> getAllTeams() {
        try{
            List<Team> teams = teamService.getAllTeams();
            return ResponseService.generateSuccessResponse("Team found", teams, HttpStatus.OK);

        }catch (Exception e){
            exceptionHandling.handleException(e);
            return generateErrorResponse("Error joining Tournament", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "/getTeamById", method = RequestMethod.GET)
    public ResponseEntity<?> getTeamById(@RequestParam Long id) {
        try {
            Team team = teamService.getTeamById(id);
            if (team == null) {
                return ResponseService.generateErrorResponse("Team not found", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("Team found", team, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);

        }
        return null;
    }


    @PostMapping("/createTeam")
    public ResponseEntity<?> createTeam(@Validated @RequestBody Team team) {
        try {
            TeamDTO createdTeam = teamService.createTeam(team);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTeam);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error creating team", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/updateTeam")
    public ResponseEntity<?> updateTeam(@RequestParam Long id, @Valid @RequestBody Team teamDetails) {
            try{


                TeamDTO updatedTeam = teamService.updateTeam(id, teamDetails);
                if (updatedTeam == null) {
                    return ResponseService.generateErrorResponse("Team not found", HttpStatus.NOT_FOUND);
                }
                return ResponseService.generateSuccessResponse("Team updated successfully", updatedTeam, HttpStatus.OK);
            }catch (Exception e){
                exceptionHandling.handleException(e);
                return generateErrorResponse("Error joining Tournament", HttpStatus.INTERNAL_SERVER_ERROR);

            }
    }

    @DeleteMapping("/deleteTeam")
    public ResponseEntity<?> deleteTeam(@RequestParam Long id) {
            try{

                Team team = teamService.getTeamById(id);
                if (team == null) {
                    return ResponseService.generateErrorResponse("Team not found", HttpStatus.NOT_FOUND);
                }
                teamService.deleteTeam(id);
                return generateErrorResponse("Team has been deleted", HttpStatus.OK);

            }catch (Exception e){
                exceptionHandling.handleException(e);
                return generateErrorResponse("Error joining Tournament", HttpStatus.INTERNAL_SERVER_ERROR);

            }
    }
}
