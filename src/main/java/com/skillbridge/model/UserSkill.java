package com.skillbridge.model;

import jakarta.persistence.*;

/** Junction table User_Skills with proficiency column (1-5). */
@Entity
@Table(name = "user_skills", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "skill_id"}))
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(nullable = false)
    private int proficiency = 3; // 1 = Beginner, 2 = Elementary, 3 = Intermediate, 4 = Advanced, 5 = Expert

    public UserSkill() {}

    public UserSkill(User user, Skill skill, int proficiency) {
        this.user = user;
        this.skill = skill;
        this.proficiency = Math.max(1, Math.min(5, proficiency));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public int getProficiency() {
        return proficiency;
    }

    public void setProficiency(int proficiency) {
        this.proficiency = Math.max(1, Math.min(5, proficiency));
    }

    public String getProficiencyLabel() {
        return switch (proficiency) {
            case 1 -> "Beginner";
            case 2 -> "Elementary";
            case 3 -> "Intermediate";
            case 4 -> "Advanced";
            case 5 -> "Expert";
            default -> "Intermediate";
        };
    }
}
