package com.skillbridge.repo;

import com.skillbridge.model.*;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    List<JoinRequest> findByProject(Project project);
    List<JoinRequest> findByUser(User user);
    boolean existsByProjectAndUser(Project project, User user);
    Optional<JoinRequest> findByProjectAndUser(Project project, User user);
    long countByProjectAndStatus(Project project, JoinRequest.Status status);
}
