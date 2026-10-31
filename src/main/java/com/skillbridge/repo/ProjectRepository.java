package com.skillbridge.repo;

import com.skillbridge.model.Project;
import com.skillbridge.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByLead(User lead);

    List<Project> findAllByOrderByCreatedAtDesc();

    @Query("select distinct p from Project p join p.requiredSkills s " +
           "where lower(s.name) like lower(concat('%', :skill, '%'))")
    List<Project> searchBySkill(@Param("skill") String skill);

    @Query("select distinct p from Project p left join p.requiredSkills s " +
           "where (:q is null or :q = '' " +
           "   or lower(p.title) like lower(concat('%', :q, '%')) " +
           "   or lower(p.description) like lower(concat('%', :q, '%')) " +
           "   or lower(p.roleNeeded) like lower(concat('%', :q, '%')) " +
           "   or lower(s.name) like lower(concat('%', :q, '%'))) " +
           "  and (:role is null or :role = '' or p.roleType = :role)")
    List<Project> filterProjects(@Param("q") String q, @Param("role") String role);
}
