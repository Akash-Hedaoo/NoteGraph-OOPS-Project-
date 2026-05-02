package com.notegraph.api.service;

import com.notegraph.api.domain.RevisionSchedule;
import com.notegraph.api.domain.RevisionSchedule.RevisionStatus;
import com.notegraph.api.repository.RevisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Background cron task that checks for pending revisions every minute
 * and sends email reminders when the scheduled time has arrived.
 */
@Component
@RequiredArgsConstructor
public class RevisionSchedulerTask {

    private final RevisionRepository revisionRepository;
    private final EmailService emailService;

    @Scheduled(fixedRate = 60000) // every 60 seconds
    public void checkAndSendReminders() {
        List<RevisionSchedule> dueRevisions = revisionRepository
                .findByStatusAndScheduledAtBefore(RevisionStatus.PENDING, LocalDateTime.now());

        for (RevisionSchedule revision : dueRevisions) {
            try {
                boolean emailSent = emailService.sendRevisionReminder(revision);
                revision.setStatus(RevisionStatus.SENT);
                revisionRepository.save(revision);
                if (emailSent) {
                    System.out.println("[RevisionScheduler] Sent reminder for note: " + revision.getNote().getTitle());
                } else {
                    System.out.println("[RevisionScheduler] Marked as SENT (no email — SMTP not configured): " + revision.getNote().getTitle());
                }
            } catch (Exception e) {
                System.err.println("[RevisionScheduler] Failed to send email for revision " + revision.getId() + ": " + e.getMessage());
                // Don't change status — will retry on next cycle
            }
        }
    }
}
