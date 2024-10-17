package com.community.api.services;


import com.community.api.dto.MatchDTO;
import com.community.api.entity.Match;
import com.community.api.entity.Team;
import com.community.api.entity.Tournament;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MatchService {
    @Autowired
    private EntityManager entityManager;

    @Transactional
    public Match createMatch(MatchDTO matchRequest) {
        Tournament tournament = entityManager.find(Tournament.class, matchRequest.getTournamentId());
        Team team1 = entityManager.find(Team.class, matchRequest.getTeam1Id());
        Team team2 = entityManager.find(Team.class, matchRequest.getTeam2Id());

        if (tournament == null || team1 == null || team2 == null) {
            throw new IllegalArgumentException("Invalid tournament or team IDs.");
        }

        Match match = new Match();
        match.setTournament(tournament);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setMatchDate(matchRequest.getMatchDate());
        match.setResult(matchRequest.getResult());

        entityManager.persist(match);
        return match;
    }

    @Transactional
    public List<Match> getAllMatches() {
        try {
            List<Match> matches = entityManager.createQuery("SELECT m FROM Match m", Match.class).getResultList();
            return matches != null ? matches : new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching matches: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Match getMatchById(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("Match ID cannot be null.");
            }
            Match match = entityManager.find(Match.class, id);
            if (match == null) {
                throw new NoResultException("Match not found for ID: " + id);
            }
            return match;
        } catch (NoResultException e) {
            throw new RuntimeException("Match not found: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching match by ID: " + e.getMessage(), e);
        }
    }
}
