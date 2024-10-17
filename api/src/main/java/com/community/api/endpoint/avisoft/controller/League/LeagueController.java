package com.community.api.endpoint.avisoft.controller.League;

import com.community.api.entity.ErrorResponse;
import com.community.api.entity.League;
import com.community.api.entity.Participation;
import com.community.api.services.LeagueService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.util.List;

import static com.community.api.services.ResponseService.generateErrorResponse;

@RestController
@RequestMapping("/leagues")
public class LeagueController {
    @Autowired
    private LeagueService leagueService;

    @Autowired
    private ResponseService responseService;

    @Autowired
    ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private EntityManager em;

    @GetMapping("/getAllLeagues")
    public ResponseEntity<?> getAllLeagues() {
       try{
           List<League> leagues = leagueService.getAllLeagues();
           return ResponseEntity.ok(leagues);
       }catch (Exception e){
           exceptionHandling.handleException(e);
           return ResponseService.generateErrorResponse("Error joining Tournament", HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    @GetMapping("/getLeagueById")
    public ResponseEntity<?> getLeagueById(@RequestParam(value = "id") Long id) {
      try{
          League league = leagueService.getLeagueById(id);
          if (league == null) {
              return ResponseService.generateErrorResponse("League not found", HttpStatus.NOT_FOUND);
          }
          return ResponseService.generateSuccessResponse("League found", league, HttpStatus.OK);
      }catch (Exception e){
          exceptionHandling.handleException(e);
          return ResponseService.generateErrorResponse("Error joining Tournament", HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }

    @PostMapping("/createLeague")
    public ResponseEntity<?> createLeague(@Valid @RequestBody League league) {
       try{

           League existingLeague = em.createQuery(
                           "SELECT l FROM League l WHERE l.name = :name", League.class)
                   .setParameter("name", league.getName())
                   .getResultStream()
                   .findFirst()
                   .orElse(null);

           if (existingLeague != null) {
               return ResponseService.generateErrorResponse("A league with the name '" + league.getName() + "' already exists.", HttpStatus.BAD_REQUEST);
           }

           League createdLeague = leagueService.createLeague(league);
           return ResponseEntity.status(HttpStatus.CREATED).body(createdLeague);
       }catch (Exception e){
           exceptionHandling.handleException(e);
           return ResponseService.generateErrorResponse("Error joining Tournament", HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    @PutMapping("/updateLeague")
    public ResponseEntity<?> updateLeague(@RequestParam(value = "id") Long id, @Valid @RequestBody League leagueDetails) {
       try{
           League updatedLeague = leagueService.updateLeague(id, leagueDetails);
           if (updatedLeague == null) {
               return ResponseService.generateErrorResponse("League not found", HttpStatus.NOT_FOUND);
           }
           return ResponseService.generateSuccessResponse("League updated successfully", updatedLeague, HttpStatus.OK);
       }catch (Exception e){
           exceptionHandling.handleException(e);
           return ResponseService.generateErrorResponse("Error joining Tournament", HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    @DeleteMapping("/deleteLeague")
    public ResponseEntity<?> deleteLeague(@RequestParam(value = "id") Long id) {
       try{

           League league = leagueService.getLeagueById(id);
           if (league == null) {
               return ResponseService.generateErrorResponse("League not found", HttpStatus.NOT_FOUND);
           }
           leagueService.deleteLeague(id);
           return generateErrorResponse("League has been deleted", HttpStatus.OK);

       }catch (Exception e){
           exceptionHandling.handleException(e);
           return ResponseService.generateErrorResponse("Error joining Tournament", HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }
}
