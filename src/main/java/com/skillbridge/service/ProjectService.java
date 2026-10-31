package com.skillbridge.service;

import com.skillbridge.model.*;
import com.skillbridge.repo.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projects;
    private final TeamRepository teams;
    private final JoinRequestRepository requests;
    private final SkillService skillService;

    public ProjectService(ProjectRepository projects, TeamRepository teams,
                          JoinRequestRepository requests, SkillService skillService) {
        this.projects = projects;
        this.teams = teams;
        this.requests = requests;
        this.skillService = skillService;
    }

    @Transactional
    public Project create(User lead, String title, String description, String roleNeeded,
                          String roleType, String projectType, String skillsCsv) {
        Project p = new Project();
        p.setTitle(title);
        p.setDescription(description);
        p.setRoleNeeded(roleNeeded != null && !roleNeeded.isBlank() ? roleNeeded : "Team Member");
        p.setRoleType(roleType != null && !roleType.isBlank() ? roleType : "Developer");
        p.setProjectType(projectType != null && !projectType.isBlank() ? projectType : "Student Project");
        p.setLead(lead);
        p.setStatus(Project.Status.OPEN);
        p.setCreatedAt(LocalDateTime.now());
        p.setRequiredSkills(skillService.parse(skillsCsv));
        projects.save(p);

        Team t = new Team();
        t.setTeamName(title + " Team");
        t.setProject(p);
        t.getMembers().add(lead); // lead is automatically the first team member
        teams.save(t);
        p.setTeam(t);
        return p;
    }

    @Transactional
    public Project create(User lead, String title, String description, String roleNeeded, String skillsCsv) {
        return create(lead, title, description, roleNeeded, "Developer", "Student Project", skillsCsv);
    }

    @Transactional
    public JoinRequest apply(Long projectId, User user, String message, Integer score) {
        Project p = projects.findById(projectId).orElseThrow();
        if (p.getLead().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Lead cannot apply to their own project");
        }
        if (requests.existsByProjectAndUser(p, user)) {
            return requests.findByProjectAndUser(p, user).orElse(null);
        }
        JoinRequest r = new JoinRequest();
        r.setProject(p);
        r.setUser(user);
        r.setMessage(message != null ? message.trim() : "");
        r.setMatchScore(score != null ? score : 0);
        r.setStatus(JoinRequest.Status.PENDING);
        r.setAppliedAt(LocalDateTime.now());
        return requests.save(r);
    }

    @Transactional
    public void apply(Long projectId, User user) {
        apply(projectId, user, "", 0);
    }

    /**
     * Approval + team insert happen in ONE transaction so they never get out of sync.
     * Only the project lead can approve or reject.
     */
    @Transactional
    public Long decide(Long requestId, User actor, boolean approve) {
        JoinRequest r = requests.findById(requestId).orElseThrow();
        Project p = r.getProject();
        if (!p.getLead().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Only the project lead can do this");
        }
        if (r.getStatus() != JoinRequest.Status.PENDING) {
            return p.getId();
        }
        if (approve) {
            r.setStatus(JoinRequest.Status.APPROVED);
            Team t = p.getTeam();
            if (t != null) {
                t.getMembers().add(r.getUser());
                teams.save(t);
            }
        } else {
            r.setStatus(JoinRequest.Status.REJECTED);
        }
        requests.save(r);
        return p.getId();
    }

    @Transactional
    public void updateProjectStatus(Long projectId, User actor, Project.Status status) {
        Project p = projects.findById(projectId).orElseThrow();
        if (!p.getLead().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Only the project lead can update status");
        }
        p.setStatus(status);
        projects.save(p);
    }
}
