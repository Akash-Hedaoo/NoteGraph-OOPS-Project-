const prisma = require('./src/config/db');

async function forceCleanup() {
    try {
        const notes = await prisma.notes.findMany({ include: { note_tags: true } });
        let count = 0;
        
        for (const n of notes) {
            // Check if the note has absolutely NO tags
            if (n.note_tags.length === 0) {
                // Delete associated revision schedules
                await prisma.revision_schedules.deleteMany({ where: { note_id: n.id } });
                // Delete the note itself
                await prisma.notes.delete({ where: { id: n.id } });
                count++;
            }
        }
        console.log(`Forcefully deleted ${count} untagged notes.`);
    } catch (e) {
        console.error("Cleanup error:", e);
    } finally {
        await prisma.$disconnect();
    }
}

forceCleanup();
