package com.notegraph.api.repository;

import com.notegraph.api.domain.RevisionSchedule;
import com.notegraph.api.domain.RevisionSchedule.RevisionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RevisionRepository extends JpaRepository<RevisionSchedule, UUID> {

    List<RevisionSchedule> findByUserIdAndStatus(UUID userId, RevisionStatus status);

    @Query("SELECT r FROM RevisionSchedule r WHERE r.note.workspace.id = :wsId AND r.user.id = :userId ORDER BY r.scheduledAt ASC")
    List<RevisionSchedule> findByWorkspaceAndUser(@Param("wsId") UUID wsId, @Param("userId") UUID userId);

    List<RevisionSchedule> findByStatusAndScheduledAtBefore(RevisionStatus status, LocalDateTime before);
}
