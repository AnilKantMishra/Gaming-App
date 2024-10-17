package com.community.api.endpoint.avisoft.controller.Partcipation;

import com.community.api.dto.ParticipationDTO;
import com.community.api.entity.Participation;
import com.community.api.entity.Team;
import com.community.api.services.PartcipationService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.community.api.services.ResponseService.generateErrorResponse;

@RestController
@RequestMapping("/participations")
public class ParticipationController {
    @Autowired
    private PartcipationService participationService;

    @Autowired
    private ResponseService responseService;

    @Autowired
    ExceptionHandlingImplement exceptionHandling;

    @GetMapping("/getAllParticipations")
    public ResponseEntity<?> getAllParticipations() {
            try {
                List<ParticipationDTO> participations = participationService.getAllParticipations();
                return ResponseService.generateSuccessResponse("Participants found", participations, HttpStatus.OK);

            }
        catch (Exception e){
            exceptionHandling.handleException(e);
            return generateErrorResponse("Error joining Tournament", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getParticipationById")
    public ResponseEntity<?> getParticipationById(@RequestParam Long id) {
       try{
           ParticipationDTO participation = participationService.getParticipationById(id);
           if (participation == null) {
               return ResponseService.generateErrorResponse("Participation not found", HttpStatus.NOT_FOUND);
           }
           return ResponseService.generateSuccessResponse("Participation found", participation, HttpStatus.OK);
       }catch (Exception e){
           exceptionHandling.handleException(e);
           return generateErrorResponse("Error joining Tournament", HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    @PostMapping("/createParticipation")
    public ResponseEntity<?> createParticipation(@Valid @RequestBody Participation participation) {
       try{
           ParticipationDTO createdParticipation = participationService.createParticipation(participation);
           return ResponseEntity.status(HttpStatus.CREATED).body(createdParticipation);
       }catch (Exception e){
           exceptionHandling.handleException(e);
           return generateErrorResponse("Error joining Tournament", HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    @PutMapping("updateParticipation")
    public ResponseEntity<?> updateParticipation(@RequestParam Long id, @Valid @RequestBody Participation participationDetails) {
       try{
           ParticipationDTO updatedParticipation = participationService.updateParticipation(id, participationDetails);
           if (updatedParticipation == null) {
               return ResponseService.generateErrorResponse("Participation not found", HttpStatus.NOT_FOUND);
           }
           return ResponseService.generateSuccessResponse("Participation updated successfully", updatedParticipation, HttpStatus.OK);
       }catch (Exception e){
           exceptionHandling.handleException(e);
           return generateErrorResponse("Error joining Tournament", HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    @DeleteMapping("/deleteParticipation")
    public ResponseEntity<?> deleteParticipation(@RequestParam(value = "id") Long id) {
       try{


           ParticipationDTO participation = participationService.getParticipationById(id);
           if (participation == null) {
               return ResponseService.generateErrorResponse("participation not found", HttpStatus.NOT_FOUND);
           }
           participationService.deleteParticipation(id);
           return generateErrorResponse("Participation has been deleted", HttpStatus.OK);

       }catch (Exception e){
           exceptionHandling.handleException(e);
           return generateErrorResponse("Error joining Tournament", HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }
}
