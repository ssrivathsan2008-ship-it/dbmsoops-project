package com.skillbridge.web;

import com.skillbridge.model.*;
import com.skillbridge.repo.*;
import com.skillbridge.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

@Controller
public class ProjectController {

    private final ProjectRepository projects;
    private final UserRepository users;
    private final JoinRequestRepository requests;
    private final TeamRepository teams;
    private final ProjectMilestoneRepository milestones;
    private final ProjectService projectService;
    private final MatchingService matching;

    public ProjectController(ProjectRepository projects, UserRepository users,
                             JoinRequestRepository requests, TeamRepository teams,
                             ProjectMilestoneRepository milestones,
                             ProjectService projectService, MatchingService matching) {
        this.projects = projects;
        this.users = users;
        this.requests = requests;
        this.teams = teams;
        this.milestones = milestones;
        this.projectService = projectService;
        this.matching = matching;
    }

    private User me(Principal p) {
        return users.findByEmail(p.getName()).orElseThrow();
    }

    @GetMapping("/projects")
    String list(@RequestParam(required = false) String q,
                @RequestParam(required = false) String role,
                @RequestParam(required = false, defaultValue = "match") String sort,
                Principal p, Model m) {
        User u = me(p);
        List<Project> list = projects.filterProjects(q, role);

        Map<Long, MatchingService.Candidate> candidateScores = new HashMap<>();
        for (Project pr : list) {
            candidateScores.put(pr.getId(), matching.score(pr, u));
        }

        if ("match".equalsIgnoreCase(sort)) {
            list = new ArrayList<>(list);
            list.sort((a, b) -> Integer.compare(
                candidateScores.get(b.getId()).getScore(),
                candidateScores.get(a.getId()).getScore()
            ));
        }

        m.addAttribute("projects", list);
        m.addAttribute("scores", candidateScores);
        m.addAttribute("q", q != null ? q : "");
        m.addAttribute("selectedRole", role != null ? role : "");
        m.addAttribute("sort", sort);
        return "projects";
    }

    @GetMapping("/projects/new")
    String form() {
        return "project-form";
    }

    @PostMapping("/projects")
    String create(Principal p,
                  @RequestParam String title,
                  @RequestParam(required = false) String description,
                  @RequestParam(required = false) String roleNeeded,
                  @RequestParam(required = false) String roleType,
                  @RequestParam(required = false) String projectType,
                  @RequestParam String skills) {
        Project pr = projectService.create(me(p), title, description, roleNeeded, roleType, projectType, skills);
        return "redirect:/projects/" + pr.getId();
    }

    @GetMapping("/projects/{id}")
    String detail(@PathVariable Long id, Principal p, Model m) {
        User u = me(p);
        Project pr = projects.findById(id).orElseThrow();
        boolean lead = pr.getLead().getId().equals(u.getId());

        MatchingService.Candidate myCandidate = matching.score(pr, u);
        Optional<JoinRequest> userRequest = requests.findByProjectAndUser(pr, u);

        m.addAttribute("project", pr);
        m.addAttribute("isLead", lead);
        m.addAttribute("myScore", myCandidate);
        m.addAttribute("applied", userRequest.isPresent());
        m.addAttribute("myRequest", userRequest.orElse(null));
        m.addAttribute("isMember", pr.getTeam() != null &&
                pr.getTeam().getMembers().stream().anyMatch(x -> x.getId().equals(u.getId())));
        m.addAttribute("milestones", milestones.findByProjectOrderByDueDateAsc(pr));

        if (lead) {
            m.addAttribute("candidates", matching.rankCandidates(pr));
            m.addAttribute("requests", requests.findByProject(pr));
        }
        return "project-detail";
    }

    @PostMapping("/projects/{id}/apply")
    String apply(@PathVariable Long id,
                 @RequestParam(required = false) String message,
                 Principal p) {
        User u = me(p);
        Project pr = projects.findById(id).orElseThrow();
        MatchingService.Candidate score = matching.score(pr, u);
        projectService.apply(id, u, message, score.getScore());
        return "redirect:/projects/" + id + "?applied=true";
    }

    @PostMapping("/requests/{id}/{action}")
    String decide(@PathVariable Long id, @PathVariable String action, Principal p) {
        Long projectId = projectService.decide(id, me(p), "approve".equalsIgnoreCase(action));
        return "redirect:/projects/" + projectId;
    }

    @PostMapping("/projects/{id}/milestones")
    String addMilestone(@PathVariable Long id,
                        @RequestParam String title,
                        @RequestParam(required = false) String dueDateStr,
                        Principal p) {
        User u = me(p);
        Project pr = projects.findById(id).orElseThrow();
        if (!pr.getLead().getId().equals(u.getId())) {
            return "redirect:/projects/" + id;
        }
        LocalDate date = (dueDateStr != null && !dueDateStr.isBlank()) ? LocalDate.parse(dueDateStr) : null;
        ProjectMilestone m = new ProjectMilestone(pr, title, date);
        milestones.save(m);
        return "redirect:/projects/" + id;
    }

    @PostMapping("/milestones/{id}/toggle")
    String toggleMilestone(@PathVariable Long id, Principal p) {
        ProjectMilestone pm = milestones.findById(id).orElseThrow();
        User u = me(p);
        if (pm.getProject().getLead().getId().equals(u.getId())) {
            pm.setCompleted(!pm.isCompleted());
            milestones.save(pm);
        }
        return "redirect:/projects/" + pm.getProject().getId();
    }

    @GetMapping("/dashboard")
    String dashboard(Principal p, Model m) {
        User u = me(p);
        List<Project> ledProjects = projects.findByLead(u);
        List<Team> myTeams = teams.findByMembersContaining(u);
        List<JoinRequest> myRequests = requests.findByUser(u);

        // Pending requests for projects the user leads
        Map<Long, Long> pendingCounts = new HashMap<>();
        for (Project pr : ledProjects) {
            long count = requests.countByProjectAndStatus(pr, JoinRequest.Status.PENDING);
            pendingCounts.put(pr.getId(), count);
        }

        // Recommendations: top projects not led by user and not yet joined
        List<Project> allProjects = projects.findAll();
        List<Map<String, Object>> recommended = new ArrayList<>();
        for (Project pr : allProjects) {
            boolean isLead = pr.getLead().getId().equals(u.getId());
            boolean isMember = pr.getTeam() != null &&
                    pr.getTeam().getMembers().stream().anyMatch(mem -> mem.getId().equals(u.getId()));
            if (!isLead && !isMember) {
                MatchingService.Candidate cand = matching.score(pr, u);
                if (cand.getScore() > 0) {
                    Map<String, Object> rec = new HashMap<>();
                    rec.put("project", pr);
                    rec.put("score", cand.getScore());
                    rec.put("matched", cand.getMatched());
                    recommended.add(rec);
                }
            }
        }
        recommended.sort((a, b) -> Integer.compare((int) b.get("score"), (int) a.get("score")));

        m.addAttribute("user", u);
        m.addAttribute("led", ledProjects);
        m.addAttribute("pendingCounts", pendingCounts);
        m.addAttribute("teams", myTeams);
        m.addAttribute("myRequests", myRequests);
        m.addAttribute("recommended", recommended.stream().limit(4).toList());
        return "dashboard";
    }
}
