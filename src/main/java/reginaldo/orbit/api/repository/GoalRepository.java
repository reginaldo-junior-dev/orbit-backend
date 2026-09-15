package reginaldo.orbit.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import reginaldo.orbit.api.entity.Goal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<Goal, UUID> {
    List<Goal> findByUserId(UUID userId);

    Optional<Goal> findByIdAndUserId(UUID id, UUID userId);

    void deleteByUserId(UUID userId);

    @Query("SELECT g.status, COUNT(g) FROM Goal g WHERE g.user.id = :userId GROUP BY g.status")
    List<Object[]> countGoalsByStatus(@Param("userId") UUID userId);
}
