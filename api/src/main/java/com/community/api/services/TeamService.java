package com.community.api.services;

import com.community.api.dto.TeamDTO;
import com.community.api.entity.League;
import com.community.api.entity.Team;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class TeamService {
    @PersistenceContext
    private EntityManager entityManager;

    public List<Team> getAllTeams() {
        return entityManager.createQuery("SELECT t FROM Team t", Team.class).getResultList();
    }

    public Team getTeamById(Long teamId) {
        return entityManager.find(Team.class, teamId);
    }

    @Transactional
    public TeamDTO createTeam(Team team) {
        if (team.getLeague() != null && team.getLeague().getId() != null) {
            League league = entityManager.find(League.class, team.getLeague().getId());
            if (league != null) {
                team.setLeague(league);
                boolean teamExists = entityManager.createQuery(
                                "SELECT COUNT(t) > 0 FROM Team t WHERE t.name = :name AND t.league.id = :leagueId", Boolean.class)
                        .setParameter("name", team.getName())
                        .setParameter("leagueId", league.getId())
                        .getSingleResult();

                if (teamExists) {
                    throw new IllegalArgumentException("Team with this name already exists in the league");
                }
            } else {
                throw new IllegalArgumentException("League not found");
            }
        } else {
            throw new IllegalArgumentException("League ID must be provided");
        }

        entityManager.persist(team);

        return new TeamDTO(
                team.getId(),
                team.getName(),
                team.getLeague() != null ? team.getLeague().getId() : null,
                team.getLeague() != null ? team.getLeague().getName() : null,
                team.getCreatedate()
        );
    }





    @Transactional
    public TeamDTO updateTeam(Long teamId, Team teamDetails) {
        Team team = getTeamById(teamId);
        if (team != null) {
            team.setName(teamDetails.getName());
            team.setLeague(teamDetails.getLeague());

             entityManager.merge(team);

        }
        return new TeamDTO(
                team.getId(),
                team.getName(),
                team.getLeague() != null ? team.getLeague().getId() : null,
                team.getLeague() != null ? team.getLeague().getName() : null,
                team.getCreatedate()
        );
    }

    @Transactional
    public void deleteTeam(Long teamId) {
        Team team = getTeamById(teamId);
        if (team != null) {
            entityManager.remove(team);
        }
    }
}

