package com.skillbridge.repo;

import com.skillbridge.model.Project;
import com.skillbridge.model.ProjectMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectMilestoneRepository extends JpaRepository<ProjectMilestone, Long> {
    List<ProjectMilestone> findByProjectOrderByDueDateAsc(Project project);
}

