const cron = require('node-cron');
const prisma = require('../config/db');
const emailService = require('../services/email.service');

// Run every minute
const initScheduler = () => {
    cron.schedule('* * * * *', async () => {
        try {
            const now = new Date();
            
            const dueRevisions = await prisma.revision_schedules.findMany({
                where: {
                    status: 'PENDING',
                    scheduled_at: { lte: now }
                },
                include: {
                    users: true,
                    notes: true
                }
            });

            if (dueRevisions.length > 0) {
                console.log(`[Scheduler] Found ${dueRevisions.length} due revisions.`);
            }

            for (const revision of dueRevisions) {
                const userEmail = revision.users.email;
                const noteTitle = revision.notes.title;
                const reason = revision.ai_reason;

                const success = await emailService.sendRevisionReminder(userEmail, noteTitle, reason);

                if (success) {
                    await prisma.revision_schedules.update({
                        where: { id: revision.id },
                        data: { status: 'SENT' }
                    });
                }
            }
        } catch (err) {
            console.error("[Scheduler] Error processing revisions:", err);
        }
    });

    console.log("Revision background scheduler started.");
};

module.exports = { initScheduler };
