package com.skillbridge.config;

import com.skillbridge.model.*;
import com.skillbridge.repo.*;
import com.skillbridge.service.ProjectService;
import com.skillbridge.service.SkillService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository users,
                                     SkillRepository skills,
                                     ProjectRepository projects,
                                     SkillService skillService,
                                     ProjectService projectService,
                                     PasswordEncoder encoder) {
        return args -> {
            if (users.count() > 0) {
                return; // already seeded
            }

            // Create Skills
            Skill java = skillService.getOrCreate("Java", "Technical");
            Skill dbms = skillService.getOrCreate("DBMS", "Technical");
            Skill sql = skillService.getOrCreate("SQL", "Technical");
            Skill spring = skillService.getOrCreate("Spring", "Technical");
            Skill jdbc = skillService.getOrCreate("JDBC", "Technical");
            Skill mysql = skillService.getOrCreate("MySQL", "Technical");
            Skill apis = skillService.getOrCreate("APIs", "Technical");
            Skill figma = skillService.getOrCreate("Figma", "Design");
            Skill uiux = skillService.getOrCreate("UI/UX", "Design");
            Skill python = skillService.getOrCreate("Python", "Technical");
            Skill ml = skillService.getOrCreate("Machine Learning", "Technical");
            Skill git = skillService.getOrCreate("Git", "Tool");
            Skill comm = skillService.getOrCreate("Communication", "Soft Skill");
            Skill teamw = skillService.getOrCreate("Teamwork", "Soft Skill");

            // Create Demo Users
            // 1. Lead: Priya Patel
            User priya = new User("Priya Patel", "priya@skillbridge.edu", encoder.encode("password"), "Computer Science");
            priya.setPreferredRole("Project Lead & Fullstack");
            priya.setBio("Passionate about building collaborative student platforms and scalable backend architectures.");
            priya.getSkills().add(new UserSkill(priya, java, 5));
            priya.getSkills().add(new UserSkill(priya, spring, 4));
            priya.getSkills().add(new UserSkill(priya, teamw, 5));
            users.save(priya);

            // 2. Aarav Sharma (92% match on Java, DBMS, SQL)
            User aarav = new User("Aarav Sharma", "aarav@skillbridge.edu", encoder.encode("password"), "Information Technology");
            aarav.setPreferredRole("Backend Developer");
            aarav.setBio("Specialized in Java Spring Boot, microservices, and database optimization.");
            aarav.getSkills().add(new UserSkill(aarav, java, 5));
            aarav.getSkills().add(new UserSkill(aarav, spring, 4));
            aarav.getSkills().add(new UserSkill(aarav, sql, 5));
            aarav.getSkills().add(new UserSkill(aarav, dbms, 4));
            aarav.getSkills().add(new UserSkill(aarav, git, 4));
            users.save(aarav);

            // 3. Neha Gupta (87% match on Java, DBMS, APIs)
            User neha = new User("Neha Gupta", "neha@skillbridge.edu", encoder.encode("password"), "Computer Science");
            neha.setPreferredRole("Software Engineer");
            neha.setBio("Focused on REST APIs, relational databases, and clean code principles.");
            neha.getSkills().add(new UserSkill(neha, java, 4));
            neha.getSkills().add(new UserSkill(neha, dbms, 4));
            neha.getSkills().add(new UserSkill(neha, sql, 4));
            neha.getSkills().add(new UserSkill(neha, apis, 4));
            neha.getSkills().add(new UserSkill(neha, comm, 5));
            users.save(neha);

            // 4. Rohan Verma (76% match on JDBC, MySQL)
            User rohan = new User("Rohan Verma", "rohan@skillbridge.edu", encoder.encode("password"), "Data Science");
            rohan.setPreferredRole("Database Developer");
            rohan.setBio("Loves database indexing, query optimization, and JDBC data layer connectivity.");
            rohan.getSkills().add(new UserSkill(rohan, jdbc, 4));
            rohan.getSkills().add(new UserSkill(rohan, mysql, 4));
            rohan.getSkills().add(new UserSkill(rohan, sql, 3));
            rohan.getSkills().add(new UserSkill(rohan, java, 3));
            users.save(rohan);

            // 5. Ananya Iyer (Designer)
            User ananya = new User("Ananya Iyer", "ananya@skillbridge.edu", encoder.encode("password"), "Design & Media");
            ananya.setPreferredRole("UI/UX Designer");
            ananya.setBio("Crafting intuitive user journeys, interactive wireframes, and design systems.");
            ananya.getSkills().add(new UserSkill(ananya, figma, 5));
            ananya.getSkills().add(new UserSkill(ananya, uiux, 5));
            ananya.getSkills().add(new UserSkill(ananya, comm, 4));
            users.save(ananya);

            // Create Projects
            // Project 1 (From Slide 4): Backend Developer for SkillBridge
            Project p1 = projectService.create(
                priya,
                "SkillBridge Platform",
                "A campus team formation and skill matching platform for student projects, hackathons and research. We need a backend developer to build robust REST APIs, handle transaction management, and implement the matching algorithm.",
                "Backend Developer",
                "Developer",
                "Mini Project",
                "Java, DBMS, SQL"
            );

            // Project 2: Campus RideShare App
            Project p2 = projectService.create(
                aarav,
                "Smart Campus RideShare",
                "Carpooling and campus transit optimization system for students and faculty. Looking for someone with database and API integration skills.",
                "Database & API Specialist",
                "Developer",
                "Hackathon",
                "MySQL, APIs, SQL"
            );

            // Project 3: AI Health Diagnoser
            Project p3 = projectService.create(
                neha,
                "Campus Health Bot",
                "Predictive assistant for student healthcare triage and clinic appointment slot scheduling.",
                "ML Research Assistant",
                "Researcher",
                "Research",
                "Python, Machine Learning"
            );

            // Project 4: EcoCampus UI/UX Redesign
            Project p4 = projectService.create(
                priya,
                "EcoCampus Portal",
                "Redesigning the sustainability dashboard and waste tracker for our campus community.",
                "Lead UI Designer",
                "Designer",
                "Student Project",
                "Figma, UI/UX"
            );

            // Pre-create a sample pending join request from Rohan to Project 1
            projectService.apply(p1.getId(), rohan, "I have experience with JDBC and SQL data pipelines and would love to collaborate on the backend!", 76);
        };
    }
}

