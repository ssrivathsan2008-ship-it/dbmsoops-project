-- ============================================================================
-- SkillBridge Database Schema (MySQL 8)
-- Normalized Relational Model for Student Team Formation & Skill Matching
-- Refer to Slide 8: DATABASE DESIGN
-- ============================================================================

CREATE DATABASE IF NOT EXISTS skillbridge
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE skillbridge;

-- 1. Users Entity
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    department VARCHAR(100),
    preferred_role VARCHAR(100),
    bio VARCHAR(1000),
    portfolio_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Skills Entity
CREATE TABLE IF NOT EXISTS skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50) DEFAULT 'Technical',
    INDEX idx_skill_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. User_Skills Junction Table (Many-to-Many: Users <-> Skills with proficiency)
CREATE TABLE IF NOT EXISTS user_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    proficiency INT NOT NULL DEFAULT 3,
    CONSTRAINT fk_user_skills_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_skill UNIQUE (user_id, skill_id),
    CONSTRAINT chk_proficiency CHECK (proficiency BETWEEN 1 AND 5),
    INDEX idx_user_skills_user (user_id),
    INDEX idx_user_skills_skill (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Projects Entity
CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    role_needed VARCHAR(100),
    role_type VARCHAR(50) DEFAULT 'Developer',
    project_type VARCHAR(50) DEFAULT 'Mini Project',
    status VARCHAR(30) DEFAULT 'OPEN',
    lead_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_projects_lead FOREIGN KEY (lead_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_project_lead (lead_id),
    INDEX idx_project_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Project_Skills Junction Table (Many-to-Many: Projects <-> Skills)
CREATE TABLE IF NOT EXISTS project_skills (
    project_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    PRIMARY KEY (project_id, skill_id),
    CONSTRAINT fk_proj_skills_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_proj_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    INDEX idx_proj_skills_project (project_id),
    INDEX idx_proj_skills_skill (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Teams Entity (One-to-One with Project)
CREATE TABLE IF NOT EXISTS teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_name VARCHAR(255) NOT NULL,
    project_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_teams_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Team_Members Junction Table (Many-to-Many: Teams <-> Users)
CREATE TABLE IF NOT EXISTS team_members (
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (team_id, user_id),
    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_team_members_team (team_id),
    INDEX idx_team_members_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Join_Requests Entity
CREATE TABLE IF NOT EXISTS join_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    message VARCHAR(1000),
    match_score INT,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_join_requests_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_join_requests_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_project_user_request UNIQUE (project_id, user_id),
    INDEX idx_requests_project (project_id),
    INDEX idx_requests_user (user_id),
    INDEX idx_requests_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Project_Milestones Entity (Future Scope / Roadmap)
CREATE TABLE IF NOT EXISTS project_milestones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    due_date DATE,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_milestones_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    INDEX idx_milestones_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

