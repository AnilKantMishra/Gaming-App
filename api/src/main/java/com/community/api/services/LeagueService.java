package com.community.api.services;

import com.community.api.entity.League;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class LeagueService {
    @PersistenceContext
    private EntityManager entityManager;

    public List<League> getAllLeagues() {
        return entityManager.createQuery("SELECT l FROM League l", League.class).getResultList();
    }

    public League getLeagueById(Long leagueId) {
        return entityManager.find(League.class, leagueId);
    }

    @Transactional
    public League createLeague(League league) {
        entityManager.persist(league);
        return league;
    }

    @Transactional
    public League updateLeague(Long leagueId, League leagueDetails) {
        League league = getLeagueById(leagueId);
        if (league != null) {
            if(leagueDetails.getName()!=null && !leagueDetails.getName().isEmpty()){
                league.setName(leagueDetails.getName());

            }
            if(leagueDetails.getTeams()!=null && !leagueDetails.getTeams().isEmpty()){
                league.setTeams(leagueDetails.getTeams());
            }

            return entityManager.merge(league);
        }
        return null;
    }

    @Transactional
    public void deleteLeague(Long leagueId) {
        League league = getLeagueById(leagueId);
        if (league != null) {
            entityManager.remove(league);
        }
    }
}
