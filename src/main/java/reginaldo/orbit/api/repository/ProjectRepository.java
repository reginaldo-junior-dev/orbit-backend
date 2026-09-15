package reginaldo.orbit.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import reginaldo.orbit.api.entity.Project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByUserId(UUID userId);

    Optional<Project> findByIdAndUserId(UUID id, UUID userId);

    void deleteByUserId(UUID userId);

    @Query("SELECT p.status, COUNT(p) FROM Project p WHERE p.user.id = :userId GROUP BY p.status")
    List<Object[]> countProjectsByStatus(@Param("userId") UUID userId);
}
