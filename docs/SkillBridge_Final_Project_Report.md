# SkillBridge — Campus Team Formation & Skill Matching Platform
## Final Project Report
### OOPs and DBMS Project

---

> **Project Title:** SkillBridge — Campus Team Formation and Skill Matching Platform  
> **Technology Stack:** Java 21+, Spring Boot 3.3.5, Spring Data JPA / Hibernate ORM, MySQL 8, Spring Security 6, Thymeleaf, Bootstrap 5.3  
> **Repository:** [github.com/ssrivathsan2008-ship-it/dbmsoops-project](https://github.com/ssrivathsan2008-ship-it/dbmsoops-project)

---

## Preliminary Pages

---

### Bonafide Certificate

This is to certify that the project titled **"SkillBridge — Campus Team Formation and Skill Matching Platform"** is a bonafide record of work done by the student in partial fulfilment of the requirements for the Mini Project submission in the subject of **Object-Oriented Programming (OOPs) and Database Management Systems (DBMS)**.

The project demonstrates the practical implementation of:
- Core Java OOP principles (Encapsulation, Inheritance, Polymorphism, Abstraction)
- Relational database design with normalization up to Third Normal Form (3NF)
- JDBC-based database connectivity via Spring Data JPA / Hibernate ORM
- Full CRUD operations across seven normalized relational tables
- Automated test suites (20 JUnit 5 unit and MockMvc integration tests)

---

### Student Declaration

I hereby declare that the project work titled **"SkillBridge — Campus Team Formation and Skill Matching Platform"** submitted herewith is an original piece of work carried out by me. The project has not been submitted elsewhere for any degree or award. All external libraries, frameworks, and references have been duly acknowledged.

---

### Acknowledgement

I would like to express sincere gratitude to the faculty of the OOPs and DBMS department for providing the structured framework and guidance necessary to build a real-world full-stack Java application with a normalized relational database backend. The project has been an invaluable learning experience in applying theoretical OOP and database concepts to a practical, working system.

---

### Abstract

**SkillBridge** is a web-based campus platform that solves the problem of fragmented skill discovery and unbalanced team formation in student project ecosystems. Students across departments (Computer Science, Data Science, Design, etc.) struggle to find collaborators whose technical and soft skills complement the gaps in their project teams.

The system implements a **transparent, deterministic, rule-based skill matching engine** where the match score is computed as:

$$\text{score} = \left\lfloor 100 \times \frac{|\text{Matched Required Skills}|}{|\text{Total Required Skills}|} \right\rfloor$$

A proficiency-weighted variant factors in each candidate's self-reported 1–5 skill rating to produce a finer-grained ranking.

The application is built as a Spring Boot 3.3.5 MVC application using Java 21 with standard Java OOP (no code-generation libraries), Spring Data JPA with Hibernate ORM backed by MySQL 8, and Spring Security 6 with BCrypt password hashing. The frontend is rendered server-side via Thymeleaf with a minimalist design system (Plus Jakarta Sans typography, warm terracotta accent palette). 20 automated tests covering service unit tests and MockMvc integration tests guarantee correctness of the matching algorithm, transactional team insertion, and access-control rules.

---

## Chapter 1: Introduction

### 1.1 Problem Statement

Campus hackathons, research projects, and course mini-projects fail to launch or deliver weak results due to one recurring problem: **teams are formed through social proximity rather than complementary skills**. A project lead looking for a backend developer has no structured way to discover which of the 500 students in the institution has Java at Expert level (5/5) and MySQL at Advanced (4/5). Conversely, a skilled student with niche knowledge of Machine Learning has no visibility into which ongoing research projects need that skill.

Specifically:
1. Skills are locked inside course transcripts and personal networks — invisible to potential collaborators.
2. Project leads post requirements in informal channels (WhatsApp, notice boards) with no way to rank applicants transparently.
3. Team formation has no referential integrity — the same student can be "approved" into multiple conflicting projects with no system oversight.

### 1.2 Proposed Solution

**SkillBridge** provides a structured platform with three core flows:

1. **Profile:** A student registers and lists their technical and soft skills with 1–5 proficiency levels.
2. **Post:** A project lead posts an open role with required skills (e.g., "Backend Developer — requires Java, DBMS, SQL").
3. **Match & Apply:** Every student sees all open roles ranked by their personal match score. They apply with a message. The lead reviews applicants ranked by match score and approves or rejects with a single click. Approval atomically inserts the student into the team roster in one database transaction.

### 1.3 Objectives

- Implement a normalized relational schema (MySQL 8) covering Users, Skills, Projects, Teams, and Join Requests.
- Apply core Java OOP concepts: Encapsulation via constrained getters/setters; Inheritance of Spring's Repository hierarchy; Polymorphism via dual-mode scoring methods; Abstraction via service-layer separation from controllers and persistence.
- Demonstrate JDBC/ORM-based database connectivity with full CRUD operations.
- Ensure transactional integrity: approve-and-insert-into-team happens atomically in a single `@Transactional` boundary.
- Implement and verify a deterministic matching algorithm with 20 automated tests.

### 1.4 Scope

**In scope:**
- Student registration, authentication (BCrypt + Spring Security form login), and profile management
- Skill directory with category tagging (Technical, Soft Skill, Tool, Design)
- Project role postings with required skill sets (comma-separated, auto-created in `skills` table)
- Transparent rule-based matching with basic overlap score and proficiency-weighted score
- Join request lifecycle (PENDING → APPROVED / REJECTED) with atomic team insertion
- Dashboard with personal stats, led projects, team memberships, and application tracking
- Project milestone tracking (create, mark complete)
- Responsive, accessible UI (Bootstrap 5.3, WCAG AA contrast)

**Out of scope:** Real-time notifications, file upload (résumés), and external OAuth login.

---

## Chapter 2: Requirements and Feasibility

### 2.1 Functional Requirements

| # | Requirement |
|---|---|
| FR-01 | A student shall be able to register with name, email, password, department, preferred role, and bio. |
| FR-02 | A student shall log in and be authenticated via BCrypt-hashed passwords. |
| FR-03 | A student shall add, update, or remove skills on their profile with a 1–5 proficiency rating. |
| FR-04 | Any authenticated student shall view all open project roles. |
| FR-05 | The system shall compute and display a skill match percentage for every project-user pair. |
| FR-06 | A student shall apply to any project they do not lead, with an optional message. |
| FR-07 | A project lead shall see all pending join requests ranked by match score. |
| FR-08 | A project lead shall approve or reject an application. Approval atomically adds the student to the team. |
| FR-09 | A student shall post a new project role specifying required skills. |
| FR-10 | A project lead shall add and manage project milestones. |
| FR-11 | The system shall enforce that a student cannot apply to their own project. |
| FR-12 | The system shall enforce that only the project lead can approve or reject applications. |

### 2.2 Non-Functional Requirements

| Category | Requirement |
|---|---|
| **Security** | Passwords BCrypt-hashed; CSRF protection on all state-mutating forms; all non-public routes require authentication. |
| **Integrity** | Unique constraint on `(project_id, user_id)` in `join_requests` prevents duplicate applications. `ON DELETE CASCADE` on all child tables. `@Transactional` ensures approve + team-insert are atomic. |
| **Testability** | 20 JUnit 5 tests covering service logic and MockMvc integration; tests run against H2 in-memory using `dev` profile. |
| **Maintainability** | Standard Java 21 POJO models (no Lombok), clear package separation (model / repo / service / web / config), single `skillbridge.css` design system. |
| **Usability** | Responsive layout below 768px; all tap targets ≥ 44px; semantic HTML with ARIA labels and WCAG AA-compliant contrast ratios. |

### 2.3 Feasibility Analysis

**Technical Feasibility:** The chosen stack (Spring Boot 3.3.5, JPA/Hibernate, MySQL 8, Thymeleaf) is mature, well-documented, and runs on commodity hardware. Java 21 LTS provides modern language features (switch expressions, records, stream APIs) used throughout the codebase.

**Economic Feasibility:** All libraries and frameworks are open-source (MIT/Apache 2.0). Development environment requires only JDK 21+ and MySQL 8 (or H2 for local development with zero configuration).

**Operational Feasibility:** The packaged `skillbridge-1.0.0.jar` is a self-contained Spring Boot executable. Deployment requires only `java -jar skillbridge-1.0.0.jar`. The H2 dev profile (`--spring.profiles.active=dev`) allows zero-configuration demo runs.

---

## Chapter 3: System Analysis and Design

### 3.1 System Architecture

The application follows a classic **three-layer MVC architecture**:

```
┌─────────────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                             │
│  Thymeleaf Templates + Bootstrap 5.3 + skillbridge.css          │
│  (index, dashboard, projects, project-detail, profile …)        │
└────────────────────────┬────────────────────────────────────────┘
                         │ HTTP Request / Response
┌────────────────────────▼────────────────────────────────────────┐
│                   APPLICATION LAYER (Spring MVC)                 │
│  AuthController  ·  ProjectController  ·  ProfileController      │
│  │                                                               │
│  ├─ ProjectService   (create, apply, decide — @Transactional)   │
│  ├─ MatchingService  (score, scoreWeighted, rankCandidates)      │
│  └─ SkillService     (getOrCreate, parse CSV, findAll)           │
└────────────────────────┬────────────────────────────────────────┘
                         │ Spring Data JPA (JPQL + Derived Queries)
┌────────────────────────▼────────────────────────────────────────┐
│                   PERSISTENCE LAYER                              │
│  UserRepository · ProjectRepository · SkillRepository            │
│  TeamRepository · JoinRequestRepository                          │
│  ProjectMilestoneRepository                                      │
└────────────────────────┬────────────────────────────────────────┘
                         │ Hibernate ORM / JDBC / HikariCP
┌────────────────────────▼────────────────────────────────────────┐
│                   DATABASE LAYER  (MySQL 8 / H2)                 │
│  users · skills · user_skills · projects · project_skills        │
│  teams · team_members · join_requests · project_milestones       │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 OOP Concept Mapping

| OOP Concept | Implementation in SkillBridge |
|---|---|
| **Encapsulation** | All entity fields are `private` with explicit getter/setter pairs. `UserSkill.setProficiency()` enforces the 1–5 range with `Math.max(1, Math.min(5, proficiency))`. `User.getInitials()` encapsulates name-parsing logic ("Priya Patel" → "PP") internally. |
| **Abstraction** | `ProjectService`, `MatchingService`, `SkillService` expose clean APIs to controllers and hide all JPA/transaction details. `JpaRepository<T,ID>` abstracts raw JDBC away from all repository interfaces. |
| **Inheritance** | All six repositories extend `JpaRepository<Entity, Long>`, inheriting 17 CRUD methods. `Project.Status` and `JoinRequest.Status` are Java `enum` types — a form of type hierarchy. |
| **Polymorphism** | `MatchingService.score()` and `MatchingService.scoreWeighted()` both return a `Candidate` object but compute the primary score differently. `ProjectRepository.filterProjects(q, role)` accepts `null` for either parameter — polymorphic data-access. Spring resolves the concrete `UserDetailsService` at runtime via DI. |
| **Composition** | `Project` contains a `Team` (one-to-one), a `Set<Skill>` (many-to-many), and a `lead` reference (User). `User` has a `List<UserSkill>` — meaningful only in the context of its parent user. |
| **Static Nested Class** | `MatchingService.Candidate` is a static nested class that groups the output of a scoring operation — score, weightedScore, matched/missing skill lists, averageProficiency — as an immutable value object. |

### 3.3 Module Description

| Package | Class | Responsibility |
|---|---|---|
| `config` | `SecurityConfig` | Spring Security 6 filter chain, BCrypt bean, `UserDetailsService` |
| `config` | `DataInitializer` | Seeds 5 users, 14 skills, 4 projects, 1 join request on first boot |
| `model` | `User` | `@Entity` → `users` table; `getInitials()` avatar helper |
| `model` | `Skill` | `@Entity` → `skills` table; case-insensitive `equals()`/`hashCode()` |
| `model` | `UserSkill` | `@Entity` → `user_skills` junction; proficiency 1–5 with constraint in setter |
| `model` | `Project` | `@Entity` → `projects`; `Status` enum (OPEN/IN\_PROGRESS/COMPLETED) |
| `model` | `Team` | `@Entity` → `teams`; Many-to-Many members set |
| `model` | `JoinRequest` | `@Entity` → `join_requests`; `Status` enum (PENDING/APPROVED/REJECTED) |
| `model` | `ProjectMilestone` | `@Entity` → `project_milestones`; `completed` boolean toggle |
| `repo` | `ProjectRepository` | `filterProjects(q, role)` JPQL; `findByLead(User)` derived |
| `service` | `SkillService` | `getOrCreate(name)` — case-insensitive dedup; `parse(csv)` CSV → `Set<Skill>` |
| `service` | `ProjectService` | `@Transactional` create / apply / decide; `AccessDeniedException` guards |
| `service` | `MatchingService` | Pure algorithmic class; `score()`, `scoreWeighted()`, `rankCandidates()` |
| `web` | `AuthController` | `/`, `/login`, `/register`, `/demo-login` |
| `web` | `ProfileController` | `/profile` (GET/POST), `/profile/skills`, `/profile/skill/{id}/delete` |
| `web` | `ProjectController` | `/projects`, `/projects/new`, `/projects/{id}`, apply, decide, milestone |

### 3.4 Data Flow Diagrams

**User Registration:**
```
Browser POST /register
    → AuthController.register()
    → UserRepository.existsByEmail()          [SELECT]
    → new User() + BCryptPasswordEncoder.encode(password)
    → UserRepository.save(user)               [INSERT into users]
    → redirect /login?registered=true
```

**Application + Approval (Atomic Transaction):**
```
Browser POST /projects/{id}/apply
    → ProjectService.apply(projectId, user, message, score)
        @Transactional  ─────────────────────────────────┐
        → ProjectRepository.findById(id)     [SELECT]    │
        → JoinRequestRepo.existsByProjectAndUser [SELECT] │
        → new JoinRequest(PENDING)                        │
        → JoinRequestRepository.save()       [INSERT]     │
        @Transactional commit ────────────────────────────┘

Browser POST /requests/{id}/decide?approve=true
    → ProjectService.decide(requestId, actor, approve=true)
        @Transactional  ─────────────────────────────────┐
        → JoinRequestRepo.findById()          [SELECT]   │
        → Access check: actor.id == lead.id              │
        → request.setStatus(APPROVED)                    │
        → team.getMembers().add(user)                    │
        → TeamRepository.save(team)     [INSERT team_members] │
        → JoinRequestRepo.save(request) [UPDATE join_requests]│
        @Transactional commit (both writes or neither) ───┘
```

---

## Chapter 4: Database Design

### 4.1 Entity-Relationship Overview

```
users ──────< user_skills >──── skills
  │ (lead_id FK)                    │
  ▼                            project_skills
projects ──────────────────────────>┘
  │ (1:1, CASCADE)
  └──── teams
          │ >── team_members >── users

projects ──< join_requests
projects ──< project_milestones
```

Nine tables total: 5 entity tables + 3 junction tables + 1 milestone table.

### 4.2 Normalization Analysis (up to 3NF)

**1NF:** Every table has a primary key. All attribute values are atomic. No repeating groups — many-to-many relationships are resolved via junction tables (`user_skills`, `project_skills`, `team_members`).

**2NF:** Every non-key attribute depends on the full composite key. In `user_skills`, `proficiency` depends on both `user_id` AND `skill_id` — not a partial dependency.

**3NF:** No transitive dependencies. `users.department` depends only on `users.id`. `projects.role_type` depends only on `projects.id`, not on any other non-key column.

### 4.3 Table Definitions

#### `users`

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Surrogate primary key |
| `name` | VARCHAR(255) | NOT NULL | Full display name |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | Login identifier |
| `password` | VARCHAR(255) | NOT NULL | BCrypt hash (60 chars) |
| `department` | VARCHAR(100) | | Campus department |
| `preferred_role` | VARCHAR(100) | | e.g., "Backend Developer" |
| `bio` | VARCHAR(1000) | | Personal description |
| `portfolio_url` | VARCHAR(255) | | External link |
| `created_at` | TIMESTAMP | DEFAULT NOW() | Account creation time |

**Index:** `idx_user_email (email)` — O(log n) login lookup.

#### `skills`

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Surrogate PK |
| `name` | VARCHAR(100) | NOT NULL, UNIQUE | Canonical skill name |
| `category` | VARCHAR(50) | DEFAULT 'Technical' | Technical / Soft Skill / Tool / Design |

#### `user_skills` (Many-to-Many: Users ↔ Skills)

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | |
| `user_id` | BIGINT | FK → users(id) CASCADE | |
| `skill_id` | BIGINT | FK → skills(id) CASCADE | |
| `proficiency` | INT | NOT NULL, CHECK (1–5) | Novice=1 … Expert=5 |

**Unique:** `(user_id, skill_id)` — one proficiency rating per skill per user.

#### `projects`

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | |
| `title` | VARCHAR(255) | NOT NULL | Project title |
| `description` | TEXT | | Detailed description |
| `role_needed` | VARCHAR(100) | | e.g., "Backend Developer" |
| `role_type` | VARCHAR(50) | DEFAULT 'Developer' | Developer / Designer / Researcher / Tester |
| `project_type` | VARCHAR(50) | DEFAULT 'Mini Project' | Mini Project / Hackathon / Research |
| `status` | VARCHAR(30) | DEFAULT 'OPEN' | OPEN / IN\_PROGRESS / COMPLETED |
| `lead_id` | BIGINT | FK → users(id) RESTRICT | Project creator |
| `created_at` | TIMESTAMP | DEFAULT NOW() | Posting time |

**Note:** `ON DELETE RESTRICT` on `lead_id` prevents accidental deletion of a user who leads active projects.

#### `project_skills` (Many-to-Many: Projects ↔ Skills)

| Column | Type | Constraints |
|---|---|---|
| `project_id` | BIGINT | PK (composite), FK → projects CASCADE |
| `skill_id` | BIGINT | PK (composite), FK → skills CASCADE |

#### `teams` (One-to-One: Projects ↔ Teams)

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | |
| `team_name` | VARCHAR(255) | NOT NULL | Auto: "{title} Team" |
| `project_id` | BIGINT | FK → projects CASCADE, UNIQUE | One team per project |

The `UNIQUE` constraint on `project_id` enforces the 1:1 relationship.

#### `team_members` (Many-to-Many: Teams ↔ Users)

| Column | Type | Constraints |
|---|---|---|
| `team_id` | BIGINT | PK (composite), FK → teams CASCADE |
| `user_id` | BIGINT | PK (composite), FK → users CASCADE |

#### `join_requests`

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | |
| `project_id` | BIGINT | FK → projects CASCADE | |
| `user_id` | BIGINT | FK → users CASCADE | |
| `status` | VARCHAR(30) | NOT NULL DEFAULT 'PENDING' | PENDING / APPROVED / REJECTED |
| `message` | VARCHAR(1000) | | Cover note |
| `match_score` | INT | | Score at time of application |
| `applied_at` | TIMESTAMP | DEFAULT NOW() | |

**Unique:** `(project_id, user_id)` — one application per student per project.

#### `project_milestones`

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | |
| `project_id` | BIGINT | FK → projects CASCADE | |
| `title` | VARCHAR(255) | NOT NULL | Milestone name |
| `description` | VARCHAR(1000) | | Details |
| `due_date` | DATE | | Target date |
| `completed` | BOOLEAN | NOT NULL DEFAULT FALSE | Toggle status |

### 4.4 CRUD Operations Summary

| Operation | Table(s) Affected | Method |
|---|---|---|
| **Create** user | `users` | `UserRepository.save(user)` → `INSERT` |
| **Read** user by email | `users` | `findByEmail(email)` → `SELECT WHERE email = ?` |
| **Create** project + team | `projects`, `teams`, `team_members` | `ProjectService.create()` `@Transactional` → `INSERT` both atomically |
| **Read** projects filtered | `projects`, `project_skills`, `skills` | `ProjectRepository.filterProjects(q, role)` → JPQL JOIN query |
| **Create** join request | `join_requests` | `ProjectService.apply()` `@Transactional` → `INSERT` |
| **Update** request + **Insert** team member | `join_requests`, `team_members` | `ProjectService.decide()` `@Transactional` → `UPDATE` + `INSERT` atomically |
| **Delete** user skill | `user_skills` | `UserSkillRepository.delete()` → `DELETE WHERE id = ?` |
| **Update** project status | `projects` | `setStatus()` + `save()` → `UPDATE` |
| **Create** milestone | `project_milestones` | `milestoneRepository.save()` → `INSERT` |
| **Update** milestone (complete) | `project_milestones` | `setCompleted(true)` + `save()` → `UPDATE` |

---

## Chapter 5: Implementation

### 5.1 Project Structure

```
skillbridge/
├── src/main/java/com/skillbridge/
│   ├── SkillBridgeApplication.java      # @SpringBootApplication entry point
│   ├── config/
│   │   ├── SecurityConfig.java          # Spring Security 6 — BCrypt, form login, URL rules
│   │   └── DataInitializer.java         # Seed 5 users, 14 skills, 4 projects on first boot
│   ├── model/
│   │   ├── User.java                    # @Entity: users
│   │   ├── Skill.java                   # @Entity: skills (equals/hashCode by name)
│   │   ├── UserSkill.java               # @Entity: user_skills (junction + proficiency)
│   │   ├── Project.java                 # @Entity: projects + Status enum
│   │   ├── Team.java                    # @Entity: teams
│   │   ├── JoinRequest.java             # @Entity: join_requests + Status enum
│   │   └── ProjectMilestone.java        # @Entity: project_milestones
│   ├── repo/
│   │   ├── UserRepository.java          # existsByEmail, findByEmail
│   │   ├── SkillRepository.java         # findByNameIgnoreCase
│   │   ├── ProjectRepository.java       # filterProjects JPQL, searchBySkill JPQL
│   │   ├── TeamRepository.java
│   │   ├── JoinRequestRepository.java   # existsByProjectAndUser
│   │   └── ProjectMilestoneRepository.java
│   ├── service/
│   │   ├── SkillService.java            # CSV parser, case-insensitive getOrCreate
│   │   ├── ProjectService.java          # @Transactional create / apply / decide
│   │   └── MatchingService.java         # score, scoreWeighted, rankCandidates, Candidate nested class
│   └── web/
│       ├── AuthController.java          # /, /login, /register, /demo-login
│       ├── ProfileController.java       # /profile, /profile/skills
│       └── ProjectController.java       # /projects, /projects/{id}, apply, decide, milestone
├── src/main/resources/
│   ├── application.properties           # MySQL 8 production config
│   ├── application-dev.properties       # H2 in-memory dev config
│   ├── schema-mysql.sql                 # Full DDL (see Appendix A)
│   ├── static/css/skillbridge.css       # Master design system (CSS variables, components)
│   └── templates/                       # Thymeleaf server-side templates
│       ├── fragments.html               # Shared layout fragment (navbar, fonts, footer)
│       ├── index.html                   # Landing page with hero + formula card
│       ├── login.html / register.html
│       ├── dashboard.html               # Stat row + 3-column cards
│       ├── projects.html                # Project list with match meter + skill chips
│       ├── project-detail.html          # Lead view: requests + candidates ranked
│       ├── project-form.html
│       └── profile.html
├── src/test/java/com/skillbridge/
│   ├── service/MatchingServiceTest.java     # 7 unit tests
│   ├── service/ProjectServiceTest.java      # 6 unit tests
│   ├── service/SkillServiceTest.java        # 3 unit tests
│   └── web/ProjectControllerIntegrationTest.java  # 4 MockMvc tests
├── docs/dbms_project_report.md
└── pom.xml
```

### 5.2 Key OOP Implementations

#### 5.2.1 Encapsulation — `UserSkill.java`

The `UserSkill` entity encapsulates its proficiency field with a constrained setter that enforces the business rule (1–5 scale) regardless of the caller:

```java
@Column(nullable = false)
private int proficiency = 3; // 1=Beginner … 5=Expert

// Constrained setter — enforces range at the object boundary
public void setProficiency(int proficiency) {
    this.proficiency = Math.max(1, Math.min(5, proficiency));
}

// Derived label — logic hidden from callers
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
```

#### 5.2.2 Encapsulation — `User.java` (derived method)

```java
// Encapsulates name-parsing — callers only call getInitials()
public String getInitials() {
    if (name == null || name.isBlank()) return "U";
    String[] parts = name.trim().split("\\s+");
    if (parts.length == 1)
        return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
    return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
}
// "Priya Patel" → "PP"   |   "Aarav" → "AA"
```

#### 5.2.3 Abstraction — Service Layer

The controller has zero knowledge of JPA, transactions, or data consistency:

```java
// ProjectController — delegates entirely to service
@PostMapping("/projects/{id}/apply")
String apply(@PathVariable Long id, Principal p,
             @RequestParam(required = false) String message, Model m) {
    User u = me(p);
    Project project = projects.findById(id).orElseThrow();
    MatchingService.Candidate c = matching.score(project, u);
    projectService.apply(id, u, message, c.getScore()); // ← hides all JPA/tx complexity
    return "redirect:/projects/" + id + "?applied=true";
}
```

#### 5.2.4 Polymorphism — `MatchingService` Dual Scoring

```java
// Both methods return Candidate but compute the primary score differently
public Candidate score(Project project, User user) {
    // Basic overlap:  score = |matched| / |required| * 100
    int basicScore = Math.round(100f * matched.size() / requiredIds.size());
    ...
    return new Candidate(user, basicScore, weightedScore, matched, missing, avgProf);
}

public Candidate scoreWeighted(Project project, User user) {
    // Promotes weightedScore to the primary score
    Candidate base = score(project, user);
    return new Candidate(
        base.getUser(), base.getWeightedScore(), base.getWeightedScore(),
        base.getMatched(), base.getMissing(), base.getAverageProficiency());
}
```

**Proficiency weight table:**

| Proficiency Level | Weight |
|---|---|
| 1 — Beginner | 0.60 |
| 2 — Elementary | 0.75 |
| 3 — Intermediate | 0.85 |
| 4 — Advanced | 0.95 |
| 5 — Expert | 1.00 |

#### 5.2.5 Inheritance — JPA Repository Hierarchy

```java
// All 6 repos inherit 17 CRUD methods from JpaRepository at zero implementation cost
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);        // derived → SELECT COUNT(*) WHERE email=?
    Optional<User> findByEmail(String email);   // derived → SELECT * WHERE email=?
}

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByLead(User lead);        // derived → SELECT * WHERE lead_id=?

    @Query("select distinct p from Project p left join p.requiredSkills s " +
           "where (:q is null or :q = '' " +
           "   or lower(p.title) like lower(concat('%', :q, '%')) " +
           "   or lower(s.name) like lower(concat('%', :q, '%'))) " +
           "  and (:role is null or :role = '' or p.roleType = :role)")
    List<Project> filterProjects(@Param("q") String q, @Param("role") String role);
}
```

#### 5.2.6 Transactional Integrity — `ProjectService.decide()`

```java
@Transactional  // ← both writes commit together or neither
public Long decide(Long requestId, User actor, boolean approve) {
    JoinRequest r = requests.findById(requestId).orElseThrow();
    Project p = r.getProject();

    // Polymorphic access-control check
    if (!p.getLead().getId().equals(actor.getId())) {
        throw new AccessDeniedException("Only the project lead can do this");
    }
    if (r.getStatus() != JoinRequest.Status.PENDING) return p.getId();

    if (approve) {
        r.setStatus(JoinRequest.Status.APPROVED);
        Team t = p.getTeam();
        if (t != null) {
            t.getMembers().add(r.getUser());
            teams.save(t);      // INSERT into team_members  ─┐ atomic
        }                                                       │
    } else {                                                    │
        r.setStatus(JoinRequest.Status.REJECTED);              │
    }                                                           │
    requests.save(r);           // UPDATE join_requests       ─┘
    return p.getId();
}
```

#### 5.2.7 Static Nested Class — `MatchingService.Candidate`

```java
public static class Candidate {
    private final User user;
    private final int score;              // basic overlap %
    private final int weightedScore;      // proficiency-adjusted %
    private final List<String> matched;   // skill names that matched
    private final List<String> missing;   // required skills not in profile
    private final double averageProficiency;

    public String getExplanation() {
        if (matched.isEmpty()) return "No matching required skills yet";
        return matched.size() + " skill" + (matched.size() > 1 ? "s" : "")
             + " matched (" + String.join(", ", matched) + ")";
    }
    // All fields final → immutable after construction
}
```

### 5.3 Database Connectivity

The application connects to MySQL 8 via **HikariCP** connection pool (bundled with Spring Boot). Hibernate ORM translates all `@Entity` classes to DDL automatically (`ddl-auto=update`). All queries use parameterized JPQL — no raw `Statement` or `PreparedStatement` objects.

**Production config (`application.properties`):**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/skillbridge?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=CHANGE_ME
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=never
```

**Development / Test config (`application-dev.properties`):**
```properties
spring.datasource.url=jdbc:h2:mem:skillbridge;MODE=MySQL
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
spring.sql.init.mode=never
```

### 5.4 Matching Algorithm

```
Given:
  Project P with required skills R = {r₁, r₂, …, rₙ}
  Candidate User U with skills S = {s₁(p₁), s₂(p₂), …} where pᵢ ∈ {1..5}

Step 1:  M = R ∩ S     (matched skills)
Step 2:  D = R \ S     (missing skills)

Basic Score:
  basicScore = round(100 × |M| / |R|)

Weighted Score:
  w(p) = { 1→0.60, 2→0.75, 3→0.85, 4→0.95, 5→1.00 }
  weightedScore = min(100, round(100 × Σ w(pᵢ) / |R|))
                                        i ∈ M

Result: Candidate(user, basicScore, weightedScore, matched=M, missing=D)
```

**Verified example (from test `twoOfThreeRequirementsIs67Percent`):**
- Required: Java, MySQL, JDBC → |R| = 3
- Candidate has: Java (prof=5), MySQL (prof=4) → |M| = 2
- `basicScore` = round(100 × 2/3) = **67%**
- `weightedScore` = round(100 × (1.00 + 0.95) / 3) = round(65.0) = **65%**
- Missing: `["JDBC"]`

### 5.5 Security Implementation

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(a -> a
            .requestMatchers("/", "/login", "/register", "/demo-login",
                             "/error", "/css/**", "/js/**").permitAll()
            .anyRequest().authenticated())
        .formLogin(f -> f
            .loginPage("/login")
            .defaultSuccessUrl("/dashboard", true).permitAll())
        .logout(l -> l.logoutSuccessUrl("/").permitAll())
        .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/demo-login"));
    return http.build();
}

@Bean
public UserDetailsService userDetailsService(UserRepository repo) {
    return email -> repo.findByEmail(email)
        .map(u -> User.withUsername(u.getEmail())
                      .password(u.getPassword())
                      .roles("STUDENT").build())
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
}
```

---

## Chapter 6: Verification and Test Plan

### 6.1 Test Summary

```
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| Test Class | Count | Scope |
|---|---|---|
| `MatchingServiceTest` | 7 | Algorithm unit tests (pure Java) |
| `ProjectServiceTest` | 6 | Business logic unit tests (Mockito) |
| `SkillServiceTest` | 3 | CSV parsing unit tests (Mockito) |
| `ProjectControllerIntegrationTest` | 4 | Full-stack MockMvc + H2 tests |

### 6.2 Test Case Details

#### `MatchingServiceTest`

| Test Method | What It Verifies |
|---|---|
| `twoOfThreeRequirementsIs67Percent` | Exact Slide-6 example: Java+MySQL matching 2/3 = 67% |
| `allRequirementsIs100Percent` | Full skill set match = 100% |
| `noOverlapIsZero` | No overlapping skills = 0% |
| `projectWithNoRequiredSkillsIsZero` | Empty required skill set = 0% (edge case) |
| `candidatesAreRankedBestFirstAndLeadIsExcluded` | Ranking is descending; lead is excluded from results |
| `proficiencyWeightingGivesHigherScoreForExpert` | Expert (5/5) scores 100%; Beginner (1/5) scores 60% |
| `candidateTracksMissingSkillsAndExplanation` | `missing` list populated; `getExplanation()` returns readable string |

#### `ProjectServiceTest`

| Test Method | Scenario Verified |
|---|---|
| `createProjectInitializesTeamWithLead` | Team created automatically; lead is initial team member |
| `applyPreventsLeadFromApplyingToOwnProject` | `IllegalArgumentException` thrown; `save()` never called |
| `applyCreatesPendingRequestWhenValid` | Status=PENDING, message and score stored correctly |
| `decideApproveAddsMemberToTeamInSameTransaction` | Status→APPROVED; student added to team; `teamRepo.save()` called |
| `decideRejectDoesNotAddMemberToTeam` | Status→REJECTED; student NOT added; `teamRepo.save()` NOT called |
| `decideThrowsAccessDeniedIfActorIsNotLead` | `AccessDeniedException` thrown for unauthorized actor |

#### `ProjectControllerIntegrationTest`

| Test Method | HTTP Route | Assertion |
|---|---|---|
| `publicHomeIsAccessible` | `GET /` | 200 OK; body contains "SkillBridge" and hero headline |
| `publicLoginIsAccessible` | `GET /login` | 200 OK; body contains "Sign in to your SkillBridge" |
| `unauthenticatedUserRedirectedFromProjects` | `GET /projects` | 3xx → /login |
| `authenticatedUserCanAccessDashboardAndProjects` | `GET /dashboard`, `GET /projects` with `@WithMockUser` | 200 OK; correct user and project names in response body |

### 6.3 Manual Testing Checklist

| Flow | Steps | Expected Result |
|---|---|---|
| **Register** | POST /register with valid data | Account created; redirect to /login?registered=true |
| **Login** | `priya@skillbridge.edu` / `password` | Authenticated; redirect to /dashboard |
| **Add skill** | Profile → "Spring Boot" at proficiency 4 | Skill chip appears; `user_skills` row inserted |
| **Post project** | New Project → "Data Pipeline", skills "Python, SQL" | Project in /projects list |
| **Browse + match** | View /projects as Aarav | Projects sorted by match score; "SkillBridge Platform" shows 100% |
| **Apply** | Click Apply on a project; submit message | JoinRequest inserted; redirect with `?applied=true` |
| **Approve** | Login as Priya; click Approve on Rohan's request | Status → APPROVED; Rohan appears in team roster |
| **Reject** | Click Decline on another applicant | Status → REJECTED; team roster unchanged |
| **Milestone** | Add "ER Diagram" due 30 Nov | Row in `project_milestones`; checklist appears on detail page |
| **Logout** | Click Sign out | Session invalidated; redirect to / |

---

## Chapter 7: Security, Limitations, and Maintenance

### 7.1 Security Measures

| Threat | Mitigation |
|---|---|
| Credential theft | BCrypt hashing (cost factor 10 ≈ 100ms per hash) — offline brute-force infeasible |
| CSRF attacks | Spring Security default CSRF tokens injected into every form via Thymeleaf |
| Unauthorized access | All non-public routes protected by Spring Security filter chain |
| Privilege escalation | `ProjectService.decide()` throws `AccessDeniedException` if actor ≠ lead |
| Duplicate applications | `UNIQUE (project_id, user_id)` DB constraint + idempotent `existsByProjectAndUser()` check |
| SQL Injection | All queries use JPQL with named parameters — no raw string concatenation |
| XSS | Thymeleaf `th:text` auto-escapes all model output as HTML entities |

### 7.2 Limitations

1. **Password reset:** No email-based reset flow implemented.
2. **One application per project:** A student cannot re-apply or bid for multiple roles.
3. **No real-time notifications:** Status changes are visible only on next login.
4. **No profile photos:** Avatar shows initials only; no file upload.
5. **In-memory H2 (dev):** All data is lost on restart without a MySQL instance.
6. **Skill canonicalization:** "JavaScript" and "JS" are stored as separate skills.
7. **Single-institution scope:** No department-level or institution-level isolation.

### 7.3 Maintenance Guidelines

- **Adding a new entity:** Create `@Entity` → `JpaRepository` → service methods → controller routes → Thymeleaf template.
- **Schema changes:** Update `schema-mysql.sql`. Hibernate `ddl-auto=update` handles additive changes automatically.
- **CSS changes:** Edit `src/main/resources/static/css/skillbridge.css` — the single design system shared by all pages via `fragments.html`.
- **Running tests:** `mvn test` — all 20 existing tests must remain green before committing.
- **Production secrets:** Set `spring.datasource.password` via environment variable — never commit credentials to version control.

---

## Chapter 8: Conclusion and Future Work

### 8.1 Conclusion

SkillBridge successfully demonstrates the integration of Java OOP principles with a normalized relational database in a production-quality full-stack web application:

1. **OOP in practice:** Seven `@Entity` model classes showcase all four OOP pillars — encapsulation (constrained setters, internal business methods), abstraction (three-tier service layer), inheritance (Spring Data JPA repository hierarchy), and polymorphism (dual scoring methods, dynamic JPQL query parameters). The `MatchingService.Candidate` static nested class demonstrates clean value-object design.

2. **Normalized DBMS design:** Nine tables with proper primary keys, foreign keys (`ON DELETE CASCADE` / `RESTRICT`), unique constraints, and check constraints. The schema satisfies 3NF with junction tables resolving all many-to-many relationships.

3. **Full CRUD with JDBC/ORM:** Spring Data JPA / Hibernate provides transparent JDBC connectivity to MySQL 8. All four CRUD operations are demonstrated across every entity. Two JPQL `@Query` methods provide flexible search capabilities beyond what derived query methods can express.

4. **Transactional integrity:** The approve-and-join-team flow is wrapped in a single `@Transactional` boundary, ensuring the database is never inconsistent between `join_requests` and `team_members`.

5. **Verified correctness:** 20 automated tests across 4 test classes — including the Slide-6 example (67% match), proficiency weighting (Expert=100%, Beginner=60%), access-control enforcement, and full HTTP routing — give high confidence in correctness.

### 8.2 Future Work

| Enhancement | Description |
|---|---|
| Email notifications | Spring Mail alerts on application status change |
| Skill endorsement | Team members vouch for each other's proficiency post-project |
| OAuth 2.0 login | "Sign in with Google" for institutional accounts |
| REST API layer | JSON endpoints + JWT for a mobile app (React Native / Flutter) |
| Advanced matching | Factor department, availability, past project history into ranking |
| Portfolio integration | Parse GitHub activity to surface verifiable skill signals |
| Institutional analytics | Admin skill-demand heatmap and team success rate dashboard |

---

## References

1. **Spring Boot Reference Documentation** — docs.spring.io/spring-boot/docs/3.3.5
2. **Spring Data JPA Reference** — docs.spring.io/spring-data/jpa
3. **Spring Security Reference** — docs.spring.io/spring-security/reference
4. **Hibernate ORM Documentation 6.5** — hibernate.org/orm/documentation/6.5
5. **MySQL 8.0 Reference Manual** — dev.mysql.com/doc/refman/8.0/en
6. **Thymeleaf Documentation** — thymeleaf.org/documentation.html
7. **JUnit 5 User Guide** — junit.org/junit5/docs/current/user-guide
8. **Mockito Documentation** — site.mockito.org
9. Provos, N. & Mazières, D. *A Future-Adaptable Password Scheme*. USENIX ATC, 1999.
10. Ramakrishnan, R. & Gehrke, J. *Database Management Systems*, 3rd Ed. McGraw-Hill, 2002.
11. **Bootstrap 5.3 Documentation** — getbootstrap.com/docs/5.3
12. **WCAG 2.1 Accessibility Guidelines** — w3.org/TR/WCAG21

---

## Appendix A: SQL Schema

```sql
-- ============================================================================
-- SkillBridge Database Schema (MySQL 8)
-- Normalized Relational Model for Student Team Formation & Skill Matching
-- ============================================================================

CREATE DATABASE IF NOT EXISTS skillbridge
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE skillbridge;

-- 1. Users
CREATE TABLE IF NOT EXISTS users (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    email          VARCHAR(255) NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    department     VARCHAR(100),
    preferred_role VARCHAR(100),
    bio            VARCHAR(1000),
    portfolio_url  VARCHAR(255),
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Skills
CREATE TABLE IF NOT EXISTS skills (
    id       BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50)  DEFAULT 'Technical',
    INDEX idx_skill_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. User_Skills (junction: Users ↔ Skills with proficiency)
CREATE TABLE IF NOT EXISTS user_skills (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    skill_id    BIGINT NOT NULL,
    proficiency INT    NOT NULL DEFAULT 3,
    CONSTRAINT fk_user_skills_user  FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE CASCADE,
    CONSTRAINT fk_user_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_skill        UNIQUE (user_id, skill_id),
    CONSTRAINT chk_proficiency      CHECK  (proficiency BETWEEN 1 AND 5),
    INDEX idx_user_skills_user  (user_id),
    INDEX idx_user_skills_skill (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Projects
CREATE TABLE IF NOT EXISTS projects (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    role_needed  VARCHAR(100),
    role_type    VARCHAR(50)  DEFAULT 'Developer',
    project_type VARCHAR(50)  DEFAULT 'Mini Project',
    status       VARCHAR(30)  DEFAULT 'OPEN',
    lead_id      BIGINT       NOT NULL,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_projects_lead FOREIGN KEY (lead_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_project_lead   (lead_id),
    INDEX idx_project_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Project_Skills (junction: Projects ↔ Skills)
CREATE TABLE IF NOT EXISTS project_skills (
    project_id BIGINT NOT NULL,
    skill_id   BIGINT NOT NULL,
    PRIMARY KEY (project_id, skill_id),
    CONSTRAINT fk_proj_skills_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_proj_skills_skill   FOREIGN KEY (skill_id)   REFERENCES skills(id)   ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Teams (1:1 with Project)
CREATE TABLE IF NOT EXISTS teams (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    team_name  VARCHAR(255) NOT NULL,
    project_id BIGINT       NOT NULL UNIQUE,
    CONSTRAINT fk_teams_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Team_Members (junction: Teams ↔ Users)
CREATE TABLE IF NOT EXISTS team_members (
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (team_id, user_id),
    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Join_Requests
CREATE TABLE IF NOT EXISTS join_requests (
    id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    project_id  BIGINT        NOT NULL,
    user_id     BIGINT        NOT NULL,
    status      VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    message     VARCHAR(1000),
    match_score INT,
    applied_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_join_requests_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_join_requests_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    CONSTRAINT uq_project_user_request  UNIQUE (project_id, user_id),
    INDEX idx_requests_project (project_id),
    INDEX idx_requests_user    (user_id),
    INDEX idx_requests_status  (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Project_Milestones
CREATE TABLE IF NOT EXISTS project_milestones (
    id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    project_id  BIGINT        NOT NULL,
    title       VARCHAR(255)  NOT NULL,
    description VARCHAR(1000),
    due_date    DATE,
    completed   BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_milestones_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    INDEX idx_milestones_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## Appendix B: Output Screenshots

All screenshots captured from `http://localhost:8080` (Spring Boot running locally with H2 dev profile).

### B.1 Landing Page

**Desktop (1280×900):** Hero with headline, matching formula card, problem/solution cards, and public navbar with Sign in + Get started.

![Landing Page — Desktop (1280×900)](file:///C:/Users/srivathsan%20s/.gemini/antigravity/brain/073466c6-43c7-4a71-9e38-0908d318cb97/screenshots/landing-desktop.png)

**Mobile (390×844):** Hero and formula card stack vertically. Single-column responsive layout.

![Landing Page — Mobile (390×844)](file:///C:/Users/srivathsan%20s/.gemini/antigravity/brain/073466c6-43c7-4a71-9e38-0908d318cb97/screenshots/landing-mobile.png)

---

### B.2 Dashboard

**Desktop (1280×900):** Plain greeting header; compact 4-stat row (Projects led = 2, Teams joined = 2, Applications = 0, Skills listed = 3); three-column card layout: Projects you lead (with pending request badge), Your teams (Active dot), Your applications (empty state).

![Dashboard — Desktop (1280×900)](file:///C:/Users/srivathsan%20s/.gemini/antigravity/brain/073466c6-43c7-4a71-9e38-0908d318cb97/screenshots/dashboard-desktop.png)

**Mobile (390×844):** Stats collapse to 2×2 grid. Three columns stack vertically.

![Dashboard — Mobile (390×844)](file:///C:/Users/srivathsan%20s/.gemini/antigravity/brain/073466c6-43c7-4a71-9e38-0908d318cb97/screenshots/dashboard-mobile.png)

---

### B.3 Projects List

**Desktop (1280×900):** Role type + project type badges; slim match score progress bar with percentage; clickable required skill chips; correct pluralization ("1 team member"); search/filter/sort bar.

![Projects List — Desktop (1280×900)](file:///C:/Users/srivathsan%20s/.gemini/antigravity/brain/073466c6-43c7-4a71-9e38-0908d318cb97/screenshots/projects-desktop.png)

**Mobile (390×844):** Cards go full-width. Filter controls stack. Match meter and skill chips remain visible.

![Projects List — Mobile (390×844)](file:///C:/Users/srivathsan%20s/.gemini/antigravity/brain/073466c6-43c7-4a71-9e38-0908d318cb97/screenshots/projects-mobile.png)

---

### B.4 Project Detail (Lead View)

**Desktop (1280×900):** Project header (OPEN badge, type, posted date); lead avatar card; role description; required skill chips (Java, SQL, DBMS); team roster with Lead badge; project milestones checklist; right panel — **Join Requests** (Rohan Verma, Pending, 76% match, cover message, Approve/Decline buttons) and **Matching Candidates** ranked: Aarav 100%, Neha 100%, Rohan 67% — each with their matched skill chips.

![Project Detail — Desktop (1280×900)](file:///C:/Users/srivathsan%20s/.gemini/antigravity/brain/073466c6-43c7-4a71-9e38-0908d318cb97/screenshots/project-detail-desktop.png)

**Mobile (390×844):** All sections stack vertically. Approve/Decline buttons remain full-width tap targets.

![Project Detail — Mobile (390×844)](file:///C:/Users/srivathsan%20s/.gemini/antigravity/brain/073466c6-43c7-4a71-9e38-0908d318cb97/screenshots/project-detail-mobile.png)

---

*End of SkillBridge Final Project Report*
