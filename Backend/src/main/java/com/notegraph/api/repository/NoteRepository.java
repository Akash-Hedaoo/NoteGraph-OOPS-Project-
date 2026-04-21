package com.notegraph.api.repository;

import com.notegraph.api.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    List<Note> findByWorkspaceIdOrderByUpdatedAtDesc(UUID workspaceId);
    List<Note> findByWorkspaceIdAndFavoriteTrue(UUID workspaceId);
    
    @Query("SELECT n FROM Note n JOIN n.tags t WHERE t.id = :tagId")
    List<Note> findByTagId(@Param("tagId") UUID tagId);
}
