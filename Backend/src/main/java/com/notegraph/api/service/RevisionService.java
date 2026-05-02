package com.notegraph.api.service;

import com.notegraph.api.domain.Note;
import com.notegraph.api.domain.RevisionSchedule;
import com.notegraph.api.domain.RevisionSchedule.RevisionStatus;
import com.notegraph.api.domain.User;
import com.notegraph.api.repository.NoteRepository;
import com.notegraph.api.repository.RevisionRepository;
import com.notegraph.api.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RevisionService {

    private final RevisionRepository revisionRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public List<RevisionSchedule> getWorkspaceRevisions(UUID workspaceId, UUID userId) {
        return revisionRepository.findByWorkspaceAndUser(workspaceId, userId);
    }

    @Transactional
    public RevisionSchedule scheduleRevision(RevisionRequest request) {
        Note note = noteRepository.findById(UUID.fromString(request.getNoteId()))
                .orElseThrow(() -> new IllegalArgumentException("Note not found: " + request.getNoteId()));
        User user = userRepository.findById(UUID.fromString(request.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));

        RevisionSchedule revision = RevisionSchedule.builder()
                .note(note)
                .user(user)
                .scheduledAt(request.getScheduledAt())
                .complexityScore(request.getComplexityScore())
                .aiReason(request.getAiReason())
                .status(RevisionStatus.PENDING)
                .build();

        return revisionRepository.save(revision);
    }

    @Transactional
    public RevisionSchedule updateStatus(UUID revisionId, String newStatus) {
        RevisionSchedule revision = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new IllegalArgumentException("Revision not found: " + revisionId));
        revision.setStatus(RevisionStatus.valueOf(newStatus.toUpperCase()));
        return revisionRepository.save(revision);
    }

    @Transactional
    public void deleteRevision(UUID revisionId) {
        revisionRepository.deleteById(revisionId);
    }

    @Data
    public static class RevisionRequest {
        private String noteId;
        private String userId;
        private LocalDateTime scheduledAt;
        private Integer complexityScore;
        private String aiReason;
    }
}
