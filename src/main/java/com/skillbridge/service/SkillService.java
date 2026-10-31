package com.skillbridge.service;

import com.skillbridge.model.Skill;
import com.skillbridge.repo.SkillRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SkillService {

    private final SkillRepository skills;

    public SkillService(SkillRepository skills) {
        this.skills = skills;
    }

    public Skill getOrCreate(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill name cannot be empty");
        }
        String n = name.trim();
        return skills.findByNameIgnoreCase(n).orElseGet(() -> skills.save(new Skill(n)));
    }

    public Skill getOrCreate(String name, String category) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill name cannot be empty");
        }
        String n = name.trim();
        return skills.findByNameIgnoreCase(n).orElseGet(() -> skills.save(new Skill(n, category)));
    }

    /** "Java, MySQL, JDBC" -> Set of Skill entities */
    public Set<Skill> parse(String csv) {
        if (csv == null || csv.isBlank()) {
            return new HashSet<>();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::getOrCreate)
                .collect(Collectors.toSet());
    }

    public List<Skill> findAll() {
        return skills.findAll();
    }
}
