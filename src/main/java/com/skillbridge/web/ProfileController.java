package com.skillbridge.web;

import com.skillbridge.model.Skill;
import com.skillbridge.model.User;
import com.skillbridge.model.UserSkill;
import com.skillbridge.repo.UserRepository;
import com.skillbridge.service.SkillService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class ProfileController {

    private final UserRepository users;
    private final SkillService skillService;

    public ProfileController(UserRepository users, SkillService skillService) {
        this.users = users;
        this.skillService = skillService;
    }

    private User me(Principal p) {
        return users.findByEmail(p.getName()).orElseThrow();
    }

    @GetMapping("/profile")
    String profile(Principal p, Model m) {
        m.addAttribute("user", me(p));
        return "profile";
    }

    @PostMapping("/profile")
    @Transactional
    String updateProfile(Principal p,
                         @RequestParam String name,
                         @RequestParam(required = false) String department,
                         @RequestParam(required = false) String preferredRole,
                         @RequestParam(required = false) String bio,
                         @RequestParam(required = false) String portfolioUrl) {
        User u = me(p);
        u.setName(name != null ? name.trim() : u.getName());
        u.setDepartment(department != null ? department.trim() : "");
        u.setPreferredRole(preferredRole != null ? preferredRole.trim() : "");
        u.setBio(bio != null ? bio.trim() : "");
        u.setPortfolioUrl(portfolioUrl != null ? portfolioUrl.trim() : "");
        users.save(u);
        return "redirect:/profile?updated=true";
    }

    @PostMapping("/profile/skills")
    @Transactional
    String addSkill(Principal p,
                    @RequestParam String skill,
                    @RequestParam(required = false, defaultValue = "Technical") String category,
                    @RequestParam int proficiency) {
        User u = me(p);
        Skill s = skillService.getOrCreate(skill, category);
        UserSkill us = u.getSkills().stream()
                .filter(x -> x.getSkill().getId().equals(s.getId()))
                .findFirst()
                .orElse(null);
        if (us == null) {
            us = new UserSkill();
            us.setUser(u);
            us.setSkill(s);
            u.getSkills().add(us);
        }
        us.setProficiency(Math.max(1, Math.min(5, proficiency)));
        users.save(u);
        return "redirect:/profile";
    }

    @PostMapping("/profile/skills/{id}/delete")
    @Transactional
    String removeSkill(Principal p, @PathVariable Long id) {
        User u = me(p);
        u.getSkills().removeIf(x -> x.getId().equals(id));
        users.save(u);
        return "redirect:/profile";
    }
}
