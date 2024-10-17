package com.community.api.services;

import com.community.api.dto.ParticipationDTO;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.Participation;
import com.community.api.entity.Team;
import com.community.api.entity.Tournament;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class PartcipationService {
    @PersistenceContext
    private EntityManager entityManager;

    public List<ParticipationDTO> getAllParticipations() {
        List<Participation> participations = entityManager.createQuery("SELECT p FROM Participation p", Participation.class).getResultList();

        List<ParticipationDTO> participationDTOs = new ArrayList<>();

        for (Participation participation : participations) {
            ParticipationDTO dto = new ParticipationDTO();
            dto.setId(participation.getId());
            dto.setTournamentId(participation.getTournament() != null ? participation.getTournament().getId() : null);
            dto.setCustomerId(participation.getCustomCustomer() != null ? participation.getCustomCustomer().getId() : null);
            dto.setTeamId(participation.getTeam() != null ? participation.getTeam().getId() : null);
            dto.setStatus(participation.getStatus());

            participationDTOs.add(dto);
        }

        return participationDTOs;
    }


    public ParticipationDTO getParticipationById(Long participationId) {
        Participation participation = entityManager.find(Participation.class, participationId);

        if (participation == null) {
            throw new IllegalArgumentException("Participation not found with ID: " + participationId);
        }

        ParticipationDTO dto = new ParticipationDTO();
        dto.setId(participation.getId());
        dto.setTournamentId(participation.getTournament() != null ? participation.getTournament().getId() : null);
        dto.setCustomerId(participation.getCustomCustomer() != null ? participation.getCustomCustomer().getId() : null);
        dto.setTeamId(participation.getTeam() != null ? participation.getTeam().getId() : null);
        dto.setStatus(participation.getStatus());

        return dto;
    }


    @Transactional
    public ParticipationDTO createParticipation(Participation participation) {


        if (participation.getTournament() == null || participation.getTournament().getId() == null) {
            throw new IllegalArgumentException("Tournament must be provided");
        }

        if (participation.getTournament() != null && participation.getTournament().getId() != null) {
            Tournament tournament = entityManager.find(Tournament.class, participation.getTournament().getId());
            if (tournament == null) {
                throw new IllegalArgumentException("Tournament not found");
            }
            participation.setTournament(tournament);
        } else {
            throw new IllegalArgumentException("Tournament must be provided");
        }

        if (participation.getTeam() != null && participation.getTeam().getId() != null) {
            Team team = entityManager.find(Team.class, participation.getTeam().getId());
            if (team == null) {
                throw new IllegalArgumentException("Team not found");
            }
            participation.setTeam(team);
        } else {
            throw new IllegalArgumentException("Team must be provided");
        }

        if (participation.getCustomCustomer() != null && participation.getCustomCustomer().getId() != null) {
            CustomCustomer customer = entityManager.find(CustomCustomer.class, participation.getCustomCustomer().getId());
            if (customer == null) {
                throw new IllegalArgumentException("Customer not found");
            }
            participation.setCustomCustomer(customer);
        } else {
            throw new IllegalArgumentException("Customer must be provided");
        }

        entityManager.persist(participation);

        ParticipationDTO dto = new ParticipationDTO();
        dto.setId(participation.getId());
        dto.setTournamentId(participation.getTournament().getId());
        dto.setCustomerId(participation.getCustomCustomer().getId());
        dto.setTeamId(participation.getTeam().getId());
        dto.setStatus(participation.getStatus());

        return dto;

    }


    @Transactional
    public ParticipationDTO updateParticipation(Long participationId, Participation participationDetails) {
        Participation existingParticipation = entityManager.find(Participation.class, participationId);

        if (existingParticipation == null) {
            throw new IllegalArgumentException("Participation not found");
        }

        if (participationDetails.getTournament() != null && participationDetails.getTournament().getId() != null) {
            Tournament tournament = entityManager.find(Tournament.class, participationDetails.getTournament().getId());
            if (tournament == null) {
                throw new IllegalArgumentException("Tournament not found");
            }
            existingParticipation.setTournament(tournament);
        }

        if (participationDetails.getTeam() != null && participationDetails.getTeam().getId() != null) {
            Team team = entityManager.find(Team.class, participationDetails.getTeam().getId());
            if (team == null) {
                throw new IllegalArgumentException("Team not found");
            }
            existingParticipation.setTeam(team);
        }

        if (participationDetails.getCustomCustomer() != null && participationDetails.getCustomCustomer().getId() != null) {
            CustomCustomer customer = entityManager.find(CustomCustomer.class, participationDetails.getCustomCustomer().getId());
            if (customer == null) {
                throw new IllegalArgumentException("Customer not found");
            }
            existingParticipation.setCustomCustomer(customer);
        }

        existingParticipation.setStatus(participationDetails.getStatus());

        entityManager.merge(existingParticipation);

        ParticipationDTO dto = new ParticipationDTO();
        dto.setId(existingParticipation.getId());
        dto.setTournamentId(existingParticipation.getTournament() != null ? existingParticipation.getTournament().getId() : null);
        dto.setCustomerId(existingParticipation.getCustomCustomer() != null ? existingParticipation.getCustomCustomer().getId() : null);
        dto.setTeamId(existingParticipation.getTeam() != null ? existingParticipation.getTeam().getId() : null);
        dto.setStatus(existingParticipation.getStatus());

        return dto;
    }


    @Transactional
    public void deleteParticipation(Long participationId) {
        ParticipationDTO participation = getParticipationById(participationId);
        if (participation != null) {
            entityManager.remove(participation);
        }
    }
}
