package com.notegraph.api.repository;

import com.notegraph.api.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    List<Tag> findByWorkspaceId(UUID workspaceId);
    boolean existsByWorkspaceIdAndNameIgnoreCase(UUID workspaceId, String name);
}
