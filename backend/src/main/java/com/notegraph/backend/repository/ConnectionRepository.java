package com.notegraph.backend.repository;

import com.notegraph.backend.model.Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    List<Connection> findBySourceNoteId(Long sourceNoteId);
}
