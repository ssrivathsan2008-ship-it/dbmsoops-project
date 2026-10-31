package com.skillbridge.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 50)
    private String category = "Technical"; // Technical, Soft Skill, Tool, Framework

    public Skill() {}

    public Skill(String name) {
        this.name = name;
        this.category = "Technical";
    }

    public Skill(String name, String category) {
        this.name = name;
        this.category = (category != null && !category.isBlank()) ? category : "Technical";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Skill skill = (Skill) o;
        return Objects.equals(name != null ? name.toLowerCase() : null,
                              skill.name != null ? skill.name.toLowerCase() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name != null ? name.toLowerCase() : "");
    }
}
