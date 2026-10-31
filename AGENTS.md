# SkillBridge: Agent & Project Documentation

Team formation and skill-matching platform for student projects (Java + DBMS mini project).

## Stack
- Java 21+, Spring Boot 3.3.5, Spring Security (form login, BCrypt), Spring Data JPA/Hibernate
- MySQL 8 (schema DDL in `src/main/resources/schema-mysql.sql`, auto-created via `spring.jpa.hibernate.ddl-auto=update`)
- H2 In-Memory DB support via `-Dspring-boot.run.profiles=dev`
- Thymeleaf + Bootstrap 5.3 templates in `src/main/resources/templates`
- Standard Java POJOs (zero lombok annotation processor reliance for clean Java 21-27 compatibility)

## Layout
- `model/`: User, Skill, UserSkill, Project, Team, JoinRequest, ProjectMilestone
- `repo/`: UserRepository, SkillRepository, ProjectRepository, TeamRepository, JoinRequestRepository, ProjectMilestoneRepository
- `service/`: MatchingService, ProjectService, SkillService
- `web/`: AuthController, ProfileController, ProjectController
- `config/`: SecurityConfig, DataInitializer

## Commands
- Test: `mvn test` (runs all unit & MockMvc integration tests)
- Run: `mvn spring-boot:run` (MySQL 8) or `mvn spring-boot:run -Dspring-boot.run.profiles=dev` (in-memory H2)
- Package: `mvn clean package` -> produces `target/skillbridge-1.0.0.jar`

## Completed Roadmap Tasks
1. [x] Weight matching by `UserSkill.proficiency` (added in `MatchingService.scoreWeighted` and tested)
2. [x] More tests (`ProjectServiceTest`, `SkillServiceTest`, `ProjectControllerIntegrationTest` with MockMvc)
3. [x] Add `schema-mysql.sql` / ER diagram for the DBMS report
4. [x] Extended scope implemented: Portfolios in user profiles, Project milestones checklist, and team dashboards
