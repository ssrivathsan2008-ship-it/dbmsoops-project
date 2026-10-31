package com.skillbridge.service;

import com.skillbridge.model.*;
import com.skillbridge.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProjectServiceTest {

    private ProjectRepository projectRepo;
    private TeamRepository teamRepo;
    private JoinRequestRepository requestRepo;
    private SkillRepository skillRepo;
    private SkillService skillService;
    private ProjectService projectService;

    private User lead;
    private User student;
    private Project project;
    private Team team;

    @BeforeEach
    void setUp() {
        projectRepo = mock(ProjectRepository.class);
        teamRepo = mock(TeamRepository.class);
        requestRepo = mock(JoinRequestRepository.class);
        skillRepo = mock(SkillRepository.class);

        // Real SkillService with mocked interface repo (avoids ByteBuddy class mocking on Java 27)
        skillService = new SkillService(skillRepo);
        projectService = new ProjectService(projectRepo, teamRepo, requestRepo, skillService);

        lead = new User();
        lead.setId(1L);
        lead.setName("Priya Lead");

        student = new User();
        student.setId(2L);
        student.setName("Aarav Student");

        project = new Project();
        project.setId(10L);
        project.setTitle("SkillBridge");
        project.setLead(lead);

        team = new Team();
        team.setId(20L);
        team.setTeamName("SkillBridge Team");
        team.setProject(project);
        team.setMembers(new HashSet<>());
        team.getMembers().add(lead);

        project.setTeam(team);
    }

    @Test
    void createProjectInitializesTeamWithLead() {
        when(skillRepo.findByNameIgnoreCase("Java")).thenReturn(Optional.of(new Skill("Java")));
        when(skillRepo.findByNameIgnoreCase("SQL")).thenReturn(Optional.of(new Skill("SQL")));
        when(projectRepo.save(any(Project.class))).thenAnswer(i -> {
            Project p = i.getArgument(0);
            p.setId(10L);
            return p;
        });

        Project created = projectService.create(lead, "SkillBridge", "Desc", "Developer", "Developer", "Mini Project", "Java, SQL");

        assertNotNull(created);
        assertEquals("SkillBridge", created.getTitle());
        assertEquals(lead, created.getLead());
        assertNotNull(created.getTeam());
        assertTrue(created.getTeam().getMembers().contains(lead), "Lead must be in team members upon creation");
        verify(projectRepo).save(created);
        verify(teamRepo).save(any(Team.class));
    }

    @Test
    void applyPreventsLeadFromApplyingToOwnProject() {
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));

        assertThrows(IllegalArgumentException.class, () -> projectService.apply(10L, lead));
        verify(requestRepo, never()).save(any());
    }

    @Test
    void applyCreatesPendingRequestWhenValid() {
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(requestRepo.existsByProjectAndUser(project, student)).thenReturn(false);
        when(requestRepo.save(any(JoinRequest.class))).thenAnswer(i -> i.getArgument(0));

        JoinRequest r = projectService.apply(10L, student, "I want to contribute", 85);

        assertNotNull(r);
        assertEquals(JoinRequest.Status.PENDING, r.getStatus());
        assertEquals("I want to contribute", r.getMessage());
        assertEquals(85, r.getMatchScore());
        verify(requestRepo).save(any(JoinRequest.class));
    }

    @Test
    void decideApproveAddsMemberToTeamInSameTransaction() {
        JoinRequest req = new JoinRequest(project, student);
        req.setId(100L);
        when(requestRepo.findById(100L)).thenReturn(Optional.of(req));

        Long projId = projectService.decide(100L, lead, true);

        assertEquals(10L, projId);
        assertEquals(JoinRequest.Status.APPROVED, req.getStatus());
        assertTrue(team.getMembers().contains(student), "Approved applicant must be added to team");
        verify(teamRepo).save(team);
        verify(requestRepo).save(req);
    }

    @Test
    void decideRejectDoesNotAddMemberToTeam() {
        JoinRequest req = new JoinRequest(project, student);
        req.setId(100L);
        when(requestRepo.findById(100L)).thenReturn(Optional.of(req));

        Long projId = projectService.decide(100L, lead, false);

        assertEquals(10L, projId);
        assertEquals(JoinRequest.Status.REJECTED, req.getStatus());
        assertFalse(team.getMembers().contains(student), "Rejected applicant must not be added to team");
        verify(requestRepo).save(req);
        verify(teamRepo, never()).save(any());
    }

    @Test
    void decideThrowsAccessDeniedIfActorIsNotLead() {
        JoinRequest req = new JoinRequest(project, student);
        req.setId(100L);
        when(requestRepo.findById(100L)).thenReturn(Optional.of(req));

        User unauthorized = new User();
        unauthorized.setId(999L);

        assertThrows(AccessDeniedException.class, () -> projectService.decide(100L, unauthorized, true));
    }
}
