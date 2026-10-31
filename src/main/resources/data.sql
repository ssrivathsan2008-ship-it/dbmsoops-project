-- ============================================================================
-- SkillBridge Seed Data (MySQL 8)
-- Demo Accounts and Sample Campus Projects (Matching Presentation Slide 4)
-- ============================================================================

USE skillbridge;

-- Seed Skills
INSERT IGNORE INTO skills (id, name, category) VALUES
(1, 'Java', 'Technical'),
(2, 'DBMS', 'Technical'),
(3, 'SQL', 'Technical'),
(4, 'Spring', 'Technical'),
(5, 'JDBC', 'Technical'),
(6, 'MySQL', 'Technical'),
(7, 'APIs', 'Technical'),
(8, 'Python', 'Technical'),
(9, 'Machine Learning', 'Technical'),
(10, 'Figma', 'Design'),
(11, 'UI/UX', 'Design'),
(12, 'Communication', 'Soft Skill'),
(13, 'Teamwork', 'Soft Skill');

-- Note: Passwords below are BCrypt hashes of 'password'
-- $2a$10$w8T0s5L56e.v86t1iHq3je1jRrq1rFkUa.jGz0e7v7a.G8fM1YV8W

