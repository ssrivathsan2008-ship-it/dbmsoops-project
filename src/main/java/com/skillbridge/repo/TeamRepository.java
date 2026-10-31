package com.skillbridge.repo;

import com.skillbridge.model.Team;
import com.skillbridge.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByMembersContaining(User user);
}
