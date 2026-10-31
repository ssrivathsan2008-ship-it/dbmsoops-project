package com.skillbridge.service;

import com.skillbridge.model.Skill;
import com.skillbridge.repo.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SkillServiceTest {

    private SkillRepository skillRepo;
    private SkillService skillService;

    @BeforeEach
    void setUp() {
        skillRepo = mock(SkillRepository.class);
        skillService = new SkillService(skillRepo);
    }

    @Test
    void parseSplitsCsvAndTrimsWhitespace() {
        when(skillRepo.findByNameIgnoreCase(any())).thenReturn(Optional.empty());
        when(skillRepo.save(any(Skill.class))).thenAnswer(i -> i.getArgument(0));

        Set<Skill> skills = skillService.parse(" Java , MySQL ,  JDBC ");

        assertEquals(3, skills.size());
        assertTrue(skills.stream().anyMatch(s -> s.getName().equals("Java")));
        assertTrue(skills.stream().anyMatch(s -> s.getName().equals("MySQL")));
        assertTrue(skills.stream().anyMatch(s -> s.getName().equals("JDBC")));
    }

    @Test
    void parseHandlesEmptyAndNullGracefully() {
        assertTrue(skillService.parse(null).isEmpty());
        assertTrue(skillService.parse("   ").isEmpty());
        assertTrue(skillService.parse(",,,").isEmpty());
    }

    @Test
    void getOrCreateReusesExistingSkillCaseInsensitively() {
        Skill existing = new Skill("Python");
        existing.setId(42L);
        when(skillRepo.findByNameIgnoreCase("python")).thenReturn(Optional.of(existing));

        Skill result = skillService.getOrCreate("python");

        assertEquals(42L, result.getId());
        assertEquals("Python", result.getName());
        verify(skillRepo, never()).save(any());
    }
}

