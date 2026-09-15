package reginaldo.orbit.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import reginaldo.orbit.api.entity.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByUserId(UUID userId);

    Optional<Task> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByProjectId(UUID projectId);

    void deleteByUserId(UUID userId);

    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.user.id = :userId GROUP BY t.status")
    List<Object[]> countTasksByStatus(@Param("userId") UUID userId);
}
