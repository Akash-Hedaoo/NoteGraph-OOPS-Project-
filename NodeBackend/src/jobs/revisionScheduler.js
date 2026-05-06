// Import scheduling library to run background jobs
const cron = require('node-cron');
// Import database client
const prisma = require('../config/db');
// Import email service for sending notifications
const emailService = require('../services/email.service');

// Initialize the background scheduler
const initScheduler = () => {
    // Schedule a task to run every minute using cron syntax
    cron.schedule('* * * * *', async () => {
        try {
            // Get the current date and time
            const now = new Date();
            
            // Query the database for revision schedules that are due
            const dueRevisions = await prisma.revision_schedules.findMany({
                where: {
                    status: 'PENDING', // Only fetch pending revisions
                    scheduled_at: { lte: now } // Only fetch revisions scheduled for the current time or earlier
                },
                include: {
                    users: true, // Include associated user data to get the email address
                    notes: true  // Include associated note data to get the note title
                }
            });

            // Log the number of due revisions found
            if (dueRevisions.length > 0) {
                console.log(`[Scheduler] Found ${dueRevisions.length} due revisions.`);
            }

            // Iterate through each due revision to process it
            for (const revision of dueRevisions) {
                const userEmail = revision.users.email;
                const noteTitle = revision.notes.title;
                const reason = revision.ai_reason;

                // Attempt to send a reminder email to the user
                const success = await emailService.sendRevisionReminder(userEmail, noteTitle, reason);

                // If the email was sent successfully, update the revision status to 'SENT'
                if (success) {
                    await prisma.revision_schedules.update({
                        where: { id: revision.id },
                        data: { status: 'SENT' }
                    });
                }
            }
        } catch (err) {
            // Log any errors encountered during the scheduling process
            console.error("[Scheduler] Error processing revisions:", err);
        }
    });

    // Log that the background scheduler has successfully started
    console.log("Revision background scheduler started.");
};

// Export the scheduler initialization function
module.exports = { initScheduler };
