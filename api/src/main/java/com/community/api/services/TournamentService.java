package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.dto.*;
import com.community.api.entity.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TournamentService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public List<TournamentDTO> getActiveTournaments() {
        List<Tournament> tournaments = entityManager.createQuery("SELECT t FROM Tournament t WHERE t.status = :status", Tournament.class)
                .setParameter("status", "ONGOING")
                .getResultList();

        return tournaments.stream()
                .map(t -> {
                    TournamentDTO dto = new TournamentDTO();
                    dto.setId(t.getId());
                    dto.setName(t.getName());
                    dto.setStatus(t.getStatus());
                    dto.setStartDate(t.getStartDate());
                    dto.setEndDate(t.getEndDate());

                    List<ParticipationDTO> participationDTOs = t.getParticipations() != null && !t.getParticipations().isEmpty() ?
                            t.getParticipations().stream()
                                    .map(p -> {
                                        ParticipationDTO participationDTO = new ParticipationDTO();
                                        participationDTO.setId(p.getId());
                                        participationDTO.setTournamentId(p.getTournament().getId());
                                        participationDTO.setCustomerId(p.getCustomCustomer().getId());
                                        participationDTO.setTeamId(p.getTeam().getId());
                                        participationDTO.setStatus(p.getStatus());
                                        return participationDTO;
                                    })
                                    .collect(Collectors.toList()) : null;

                    List<Prize> prizes = entityManager.createQuery("SELECT p FROM Prize p WHERE p.tournament.id = :tournamentId", Prize.class)
                            .setParameter("tournamentId", t.getId())
                            .getResultList();

                    List<PrizeDistributionDTO> prizeDistributionDTOs = prizes.stream()
                            .map(prize -> {
                                PrizeDistributionDTO prizeDistributionDTO = new PrizeDistributionDTO();
                                prizeDistributionDTO.setPrize(prize.getPrizeName());
                                prizeDistributionDTO.setAmount(prize.getAmount());
                                return prizeDistributionDTO; // Return the PrizeDistributionDTO here
                            })
                            .collect(Collectors.toList());

                    dto.setPrizeDistributions(prizeDistributionDTOs.isEmpty() ? null : prizeDistributionDTOs);


                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Transactional
    public Tournament getTournamentById(Long tournamentId) {
        return entityManager.find(Tournament.class, tournamentId);
    }

    @Transactional
    public Tournament createTournament(Tournament tournament) {
        if (tournament.getStatus() == null ||
                (!tournament.getStatus().equals(Constant.PLANNED) &&
                        !tournament.getStatus().equals(Constant.ONGOING))) {
            throw new IllegalArgumentException("Tournament must be either ongoing or not started (PLANNED).");
        }

        if (tournament.getPrizes() != null) {
            for (Prize prize : tournament.getPrizes()) {
                prize.setTournament(tournament);
            }
        }

        entityManager.persist(tournament);
        return tournament;
    }


    @Transactional
    public Participation joinTournament(Long tournamentId, CustomCustomer customCustomer) {
        Tournament tournament = entityManager.find(Tournament.class, tournamentId);
        Participation participation = new Participation();
        participation.setTournament(tournament);
        participation.setCustomCustomer(customCustomer);
        participation.setStatus("active");
        entityManager.persist(participation);
        return participation;
    }

    @Transactional
    public void deleteTournament(Long tournamentId) {
        Tournament tournament = entityManager.find(Tournament.class, tournamentId);
        if (tournament != null) {
            entityManager.remove(tournament);
        }
    }

    @Transactional
    public Tournament endTournament(Long tournamentId) {
        Tournament tournament = entityManager.find(Tournament.class, tournamentId);
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament not found.");
        }

        if (tournament.getStatus() != Constant.ONGOING) {
            throw new IllegalArgumentException("Tournament must be ongoing to be ended.");
        }

        tournament.setStatus(Constant.COMPLETED);
        entityManager.merge(tournament);
        return tournament;
    }
    @Transactional
    public MatchResponseDTO createMatch(MatchDTO matchRequest) {
        Tournament tournament = entityManager.find(Tournament.class, matchRequest.getTournamentId());


        if (tournament == null) {
            throw new IllegalArgumentException("Invalid tournament ID.");
        }

        Match match = new Match();
        match.setTournament(tournament);
        match.setMatchDate(matchRequest.getMatchDate());
        match.setResult(matchRequest.getResult());

        Long team1Id = matchRequest.getTeam1Id();
        Long team2Id = matchRequest.getTeam2Id();

        Team team1 = entityManager.find(Team.class, team1Id);
        Team team2 = entityManager.find(Team.class, team2Id);

        if (team1 == null || team2 == null) {
            throw new IllegalArgumentException("Invalid team IDs.");
        }

        match.setTeam1(team1);
        match.setTeam2(team2);

        entityManager.persist(match);

        MatchResponseDTO createdMatchDTO = new MatchResponseDTO();
        createdMatchDTO.setId(match.getId());
        createdMatchDTO.setTournamentId(tournament.getId());

        TeamDTO team1DTO = new TeamDTO();
        team1DTO.setId(team1.getId());
        team1DTO.setName(team1.getName());
        team1DTO.setLeagueId(team1.getLeague().getId());
        team1DTO.setLeagueName(team1.getLeague().getName());
        team1DTO.setCreatedate(team1.getCreatedate());

        TeamDTO team2DTO = new TeamDTO();
        team2DTO.setId(team2.getId());
        team2DTO.setName(team2.getName());
        team2DTO.setLeagueId(team2.getLeague().getId());
        team2DTO.setLeagueName(team2.getLeague().getName());
        team2DTO.setCreatedate(team2.getCreatedate());

        createdMatchDTO.setTeam1(team1DTO);
        createdMatchDTO.setTeam2(team2DTO);
        createdMatchDTO.setMatchDate(match.getMatchDate());
        createdMatchDTO.setResult(match.getResult());

        return createdMatchDTO;
    }

/*    @Transactional
    public MatchDTO createMatch(MatchDTO matchRequest) {
        try {
            Tournament tournament = entityManager.find(Tournament.class, matchRequest.getTournamentId());
            if (tournament == null) {
                throw new IllegalArgumentException("Invalid tournament ID.");
            }

            Match match = new Match();
            match.setTournament(tournament);
            match.setMatchDate(matchRequest.getMatchDate());
            match.setResult(matchRequest.getResult());

            Long team1Id = matchRequest.getTeam1Id();
            Long team2Id = matchRequest.getTeam2Id();

            if (team1Id == null || team2Id == null) {
                throw new IllegalArgumentException("Invalid team IDs in the request.");
            }

            Team team1 = entityManager.find(Team.class, team1Id);
            Team team2 = entityManager.find(Team.class, team2Id);

            if (team1 == null || team2 == null) {
                throw new IllegalArgumentException("One or both team IDs are invalid.");
            }

            match.setTeam1(team1);
            match.setTeam2(team2);

            entityManager.persist(match);

            MatchDTO createdMatchDTO = new MatchDTO();
            createdMatchDTO.setId(match.getId());
            createdMatchDTO.setTournamentId(tournament.getId());
            createdMatchDTO.setTeam1Id(team1.getId());
            createdMatchDTO.setTeam2Id(team2.getId());
            createdMatchDTO.setMatchDate(match.getMatchDate());
            createdMatchDTO.setResult(match.getResult());

            return createdMatchDTO;
        } catch (Exception e) {
            throw new RuntimeException("Error creating match: " + e.getMessage(), e);
        }
    }*/




    @Transactional
    public void distributePrizes(PrizeDistributionDTO prizeDistribution) {
        // Find the tournament by ID
        Tournament tournament = entityManager.find(Tournament.class, prizeDistribution.getTournamentId());
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament not found for ID: " + prizeDistribution.getTournamentId());
        }

        Participation participation = entityManager.createQuery(
                        "SELECT p FROM Participation p WHERE p.tournament.id = :tournamentId AND p.team.id = :teamId", Participation.class)
                .setParameter("tournamentId", prizeDistribution.getTournamentId())
                .setParameter("teamId", prizeDistribution.getTeamId())
                .getSingleResult();

        if (participation == null) {
            throw new IllegalArgumentException("No participation record found for team ID " + prizeDistribution.getTeamId() + " in tournament ID " + prizeDistribution.getTournamentId());
        }

        Prize prize = new Prize();
        prize.setTournament(tournament);
        prize.setParticipation(participation);
        prize.setPrizeName(prizeDistribution.getPrize());
        prize.setAmount(prizeDistribution.getAmount());
        entityManager.persist(prize);
    }


    @Transactional
    public List<Prize> getPrizesForTournament(Long tournamentId) {
        return entityManager.createQuery(
                        "SELECT p FROM Prize p WHERE p.tournament.id = :tournamentId", Prize.class)
                .setParameter("tournamentId", tournamentId)
                .getResultList();
    }


    // New method for distributing prizes
    /*@Transactional
    public void distributePrizes(PrizeDistributionDTO prizeDistributionDTO) {
        Tournament tournament = entityManager.find(Tournament.class, prizeDistributionDTO.getTournamentId());
        if (tournament == null) {
            throw new IllegalArgumentException("Tournament not found.");
        }

        for (Prize prize : prizeDistributionDTO.getPrize()) {
            Participation participation = entityManager.find(Participation.class, prize.getParticipation());
            if (participation != null) {
                // Assuming Participation has a field to store prize amount
                prize.setAmount(prize.getAmount());
                // You might want to set the prize name or other details as needed
                prize.setPrizeName(prize.getPrizeName());
                entityManager.merge(participation);
            }
        }
    }
*/
}
