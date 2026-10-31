package com.skillbridge.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.skillbridge.model.*;
import com.skillbridge.repo.UserRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MatchingServiceTest {

    private final Skill java = skill(1L, "Java");
    private final Skill mysql = skill(2L, "MySQL");
    private final Skill jdbc = skill(3L, "JDBC");
    private final UserRepository repo = mock(UserRepository.class);
    private final MatchingService svc = new MatchingService(repo);

    private static Skill skill(Long id, String name) {
        Skill s = new Skill(name);
        s.setId(id);
        return s;
    }

    private static User user(Long id, String name, Skill... skills) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        for (Skill s : skills) {
            UserSkill us = new UserSkill(u, s, 3);
            u.getSkills().add(us);
        }
        return u;
    }

    private static User userWithProficiency(Long id, String name, Skill skill, int proficiency) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.getSkills().add(new UserSkill(u, skill, proficiency));
        return u;
    }

    private Project project(User lead, Skill... required) {
        Project p = new Project();
        p.setLead(lead);
        p.setRequiredSkills(Set.of(required));
        return p;
    }

    @Test
    void twoOfThreeRequirementsIs67Percent() {   // the example on Slide 6
        Project p = project(user(99L, "Lead"), java, mysql, jdbc);
        assertEquals(67, svc.score(p, user(1L, "A", java, mysql)).getScore());
    }

    @Test
    void allRequirementsIs100Percent() {
        Project p = project(user(99L, "Lead"), java, mysql);
        assertEquals(100, svc.score(p, user(1L, "A", java, mysql)).getScore());
    }

    @Test
    void noOverlapIsZero() {
        Project p = project(user(99L, "Lead"), java, mysql);
        assertEquals(0, svc.score(p, user(1L, "A", jdbc)).getScore());
    }

    @Test
    void projectWithNoRequiredSkillsIsZero() {
        Project p = project(user(99L, "Lead"));
        assertEquals(0, svc.score(p, user(1L, "A", java)).getScore());
    }

    @Test
    void candidatesAreRankedBestFirstAndLeadIsExcluded() {
        User lead = user(99L, "Lead", java, mysql, jdbc);
        User aarav = user(1L, "Aarav", java, mysql, jdbc);
        User neha = user(2L, "Neha", java, mysql);
        User rohan = user(3L, "Rohan", jdbc);
        when(repo.findAll()).thenReturn(List.of(rohan, lead, neha, aarav));

        List<MatchingService.Candidate> ranked = svc.rankCandidates(project(lead, java, mysql, jdbc));

        assertEquals(3, ranked.size());
        assertEquals("Aarav", ranked.get(0).getUser().getName());
        assertEquals("Neha", ranked.get(1).getUser().getName());
        assertEquals("Rohan", ranked.get(2).getUser().getName());
    }

    @Test
    void proficiencyWeightingGivesHigherScoreForExpert() {
        Project p = project(user(99L, "Lead"), java);
        User beginner = userWithProficiency(1L, "Beginner", java, 1);
        User expert = userWithProficiency(2L, "Expert", java, 5);

        MatchingService.Candidate cBeginner = svc.scoreWeighted(p, beginner);
        MatchingService.Candidate cExpert = svc.scoreWeighted(p, expert);

        assertTrue(cExpert.getScore() > cBeginner.getScore(),
                "Expert proficiency should yield higher weighted score than beginner");
        assertEquals(100, cExpert.getScore());
        assertEquals(60, cBeginner.getScore());
    }

    @Test
    void candidateTracksMissingSkillsAndExplanation() {
        Project p = project(user(99L, "Lead"), java, mysql, jdbc);
        User candidate = user(1L, "Alex", java, mysql);

        MatchingService.Candidate scored = svc.score(p, candidate);
        assertEquals(2, scored.getMatched().size());
        assertEquals(1, scored.getMissing().size());
        assertTrue(scored.getMissing().contains("JDBC"));
        assertTrue(scored.getExplanation().contains("2 skills matched"));
    }
}
