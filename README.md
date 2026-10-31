# SkillBridge

> **Find your people. Build what matters.**  
> *A team formation and skill matching platform for student projects, hackathons and research.*  
> **Java + DBMS Mini Project**

SkillBridge bridges the gap between students with diverse technical skills and student project leads forming balanced teams across campus departments.

---

## Architecture & Tech Stack (Slide 7)

| Layer | Technologies |
|---|---|
| **Presentation Layer** | HTML5, CSS3, JavaScript, Bootstrap 5.3, Thymeleaf Templates |
| **Application Layer** | Java 21+, Spring Boot 3.3.5, Spring Security (Form Login, BCrypt) |
| **Persistence Layer** | Spring Data JPA / Hibernate 6 ORM, Transaction Management |
| **Database Layer** | MySQL 8 (Normalized Relational Schema) / H2 in-memory for testing |

---

## Key Features (Slides 3, 4, 5, 10)

1. **Student Profiles & Proficiency Levels**:
   - Technical and soft skills with category tagging (Technical, Soft Skill, Tool, Design).
   - Verifiable 1–5 proficiency levels (Novice to Expert).
   - Bio, campus department, preferred role, and portfolio link.

2. **Project Role Postings (Slide 4 Step 2)**:
   - Project title, required role (e.g. *Backend Developer*), role type (*Developer*, *Designer*, *Researcher*, *Tester*).
   - Project category (*Mini Project*, *Hackathon*, *Research*, *Capstone*).
   - Comma-separated required tech stack (e.g. *Java, DBMS, SQL*).

3. **Transparent Rule-Based Matching Engine (Slide 4 Step 3 & Slide 6)**:
   - $\text{Basic Overlap Score} = \text{round}\left(100 \times \frac{\text{Matched Required Skills}}{\text{Total Required Skills}}\right)$.
   - *Example from Slide 6:* Java & MySQL matching 2 out of 3 requirements = **67%**.
   - Optional proficiency weighting factors in candidate skill levels (1–5 scale) for realistic rankings (e.g., Aarav 92%, Neha 87%, Rohan 76%).
   - Transparent fit explanation highlighting both matched and missing skill requirements.

4. **1-Click Application & Atomic Team Roster Insertion (Slide 4 Step 4 & Slide 8)**:
   - Students submit join requests with an optional motivation note.
   - Project leads review pending requests and candidate fit scores.
   - **Transaction Guarantee:** Approving a join request (`Status = APPROVED`) and inserting the user into `team_members` executes in **one atomic `@Transactional` method** to maintain referential consistency.

5. **Milestone Tracking (Slide 10 Future Scope)**:
   - Teams can define deliverables, track deadlines, and toggle completion.

---

## Database Design & Relational Schema (Slide 8)

Normalized Relational Model with Primary Keys, Foreign Keys, Unique Constraints, and Check Constraints:

- `users (id PK, name, email UNIQUE, password, department, preferred_role, bio, portfolio_url, created_at)`
- `skills (id PK, name UNIQUE, category)`
- `user_skills (id PK, user_id FK, skill_id FK, proficiency CHECK 1..5, UNIQUE(user_id, skill_id))`
- `projects (id PK, title, description, role_needed, role_type, project_type, status, lead_id FK, created_at)`
- `project_skills (project_id PK/FK, skill_id PK/FK)`
- `teams (id PK, team_name, project_id UNIQUE FK)`
- `team_members (team_id PK/FK, user_id PK/FK)`
- `join_requests (id PK, project_id FK, user_id FK, status, message, match_score, applied_at, UNIQUE(project_id, user_id))`
- `project_milestones (id PK, project_id FK, title, description, due_date, completed, project_id FK)`

The SQL DDL schema file is located at `src/main/resources/schema-mysql.sql`.

---

## Quick Start & Running the Project

### Option A: Zero-Setup Dev Mode (Instant In-Memory H2 Database)
No need to configure MySQL credentials or start any database:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
Then open your browser to: **http://localhost:8080**  
*(H2 Console is available at `http://localhost:8080/h2-console` with JDBC URL `jdbc:h2:mem:skillbridge`)*

### Option B: Production MySQL 8 Mode
1. Ensure MySQL 8 is running.
2. Edit `src/main/resources/application.properties` and set your MySQL password:
   ```properties
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
   *The `skillbridge` database schema and tables will be created automatically on initial boot.*

---

## Demo Accounts (Slide 4)

All pre-seeded demo accounts share the password: `password`

| Name | Email | Role | Notes |
|---|---|---|---|
| **Priya Patel** | `priya@skillbridge.edu` | Project Lead | Leads "SkillBridge Platform" (Java, DBMS, SQL) |
| **Aarav Sharma** | `aarav@skillbridge.edu` | Candidate (92% Match) | Skills: Java (5/5), Spring (4/5), SQL (5/5), DBMS (4/5) |
| **Neha Gupta** | `neha@skillbridge.edu` | Candidate (87% Match) | Skills: Java (4/5), DBMS (4/5), SQL (4/5), APIs (4/5) |
| **Rohan Verma** | `rohan@skillbridge.edu` | Candidate (76% Match) | Skills: JDBC (4/5), MySQL (4/5), Java (3/5), SQL (3/5) |
| **Ananya Iyer** | `ananya@skillbridge.edu` | Candidate (Designer) | Skills: Figma (5/5), UI/UX (5/5), Communication (4/5) |

---

## Running Tests

Execute all 20 JUnit 5 unit and MockMvc integration tests:
```bash
mvn test
```
- `MatchingServiceTest`: Rule-based matching score, 67% test, ranking, proficiency weighting.
- `ProjectServiceTest`: Team initialization, lead-only access controls, atomic transaction validation.
- `SkillServiceTest`: Case-insensitive deduplication, CSV sanitization.
- `ProjectControllerIntegrationTest`: Full Spring Security, MockMvc routing, and authentication flow.
