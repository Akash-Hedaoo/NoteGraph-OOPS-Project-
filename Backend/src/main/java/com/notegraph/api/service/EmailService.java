package com.notegraph.api.service;

import com.notegraph.api.domain.RevisionSchedule;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    /**
     * Send a revision reminder email for a scheduled revision.
     * Returns true if sent, false if SMTP is not configured.
     */
    public boolean sendRevisionReminder(RevisionSchedule revision) throws MessagingException {
        // Skip if SMTP is not configured
        if (fromEmail == null || fromEmail.isBlank()) {
            System.out.println("[EmailService] SMTP not configured (SMTP_EMAIL is empty). Skipping email for: " 
                + revision.getNote().getTitle());
            return false;
        }

        String userEmail = revision.getUser().getEmail();
        String noteTitle = revision.getNote().getTitle();
        String reason = revision.getAiReason() != null ? revision.getAiReason() : "Time for a scheduled review!";
        int complexity = revision.getComplexityScore() != null ? revision.getComplexityScore() : 5;

        String complexityLabel;
        String complexityColor;
        if (complexity <= 3) { complexityLabel = "Low"; complexityColor = "#10B981"; }
        else if (complexity <= 6) { complexityLabel = "Medium"; complexityColor = "#F59E0B"; }
        else { complexityLabel = "High"; complexityColor = "#EF4444"; }

        String scheduledTime = revision.getScheduledAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"));

        String htmlContent = """
            <div style="font-family: 'Inter', 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f9fafb; padding: 32px;">
                <div style="background: white; border-radius: 12px; padding: 32px; border: 1px solid #e5e7eb; box-shadow: 0 1px 3px rgba(0,0,0,0.05);">
                    <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 24px;">
                        <span style="font-size: 24px;">📋</span>
                        <h1 style="margin: 0; font-size: 22px; color: #111827;">NoteGraph Revision Reminder</h1>
                    </div>
                    
                    <p style="color: #6B7280; font-size: 15px; margin-bottom: 24px;">
                        It's time to revise your note to strengthen your memory retention!
                    </p>
                    
                    <div style="background: #EFF6FF; border-radius: 8px; padding: 20px; margin-bottom: 20px; border-left: 4px solid #0A66F0;">
                        <h2 style="margin: 0 0 8px 0; font-size: 18px; color: #111827;">%s</h2>
                        <div style="display: flex; gap: 12px; align-items: center; margin-top: 12px;">
                            <span style="background: %s; color: white; padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 600;">
                                Complexity: %s (%d/10)
                            </span>
                        </div>
                    </div>
                    
                    <div style="background: #f9fafb; border-radius: 8px; padding: 16px; margin-bottom: 20px;">
                        <p style="margin: 0; color: #374151; font-size: 14px;">
                            <strong>Why now?</strong> %s
                        </p>
                    </div>
                    
                    <p style="color: #9CA3AF; font-size: 13px; margin-top: 24px;">
                        Scheduled for: %s
                    </p>
                    
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;">
                    <p style="color: #9CA3AF; font-size: 12px; margin: 0; text-align: center;">
                        This is an automated reminder from NoteGraph • Powered by AI-driven spaced repetition
                    </p>
                </div>
            </div>
            """.formatted(noteTitle, complexityColor, complexityLabel, complexity, reason, scheduledTime);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(userEmail);
        helper.setSubject("📋 Revision Reminder: " + noteTitle);
        helper.setText(htmlContent, true);
        mailSender.send(message);
        return true;
    }
}
