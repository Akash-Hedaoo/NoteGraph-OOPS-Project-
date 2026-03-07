package com.notegraph.backend.repository;

import com.notegraph.backend.model.Note;
import com.notegraph.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    Optional<Note> findByTitle(String title);

    List<Note> findByUserAndIsTrashedFalse(User user);

    List<Note> findByUserAndIsTrashedTrue(User user);

    List<Note> findByUserAndIsFavoriteTrueAndIsTrashedFalse(User user);

    List<Note> findByUserAndFolderIdAndIsTrashedFalse(User user, Long folderId);
}
