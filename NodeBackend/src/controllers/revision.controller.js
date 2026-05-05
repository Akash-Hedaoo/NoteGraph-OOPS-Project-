const prisma = require('../config/db');
const { uuidToBuffer, bufferToUuid, generateUuidBuffer } = require('../config/uuid');

const mapRevision = (r) => ({
    id: bufferToUuid(r.id),
    noteId: bufferToUuid(r.note_id),
    userId: bufferToUuid(r.user_id),
    scheduledAt: r.scheduled_at,
    status: r.status,
    complexityScore: r.complexity_score,
    aiReason: r.ai_reason,
    noteTitle: r.notes?.title,
    createdAt: r.created_at
});

exports.getRevisions = async (req, res) => {
    try {
        const wsIdBuffer = uuidToBuffer(req.params.wsId);
        const userIdBuffer = uuidToBuffer(req.params.userId);

        const schedules = await prisma.revision_schedules.findMany({
            where: {
                user_id: userIdBuffer,
                notes: { workspace_id: wsIdBuffer }
            },
            include: { notes: true },
            orderBy: { scheduled_at: 'asc' }
        });

        res.json(schedules.map(mapRevision));
    } catch (err) {
        console.error("Fetch Revisions Error:", err);
        res.status(500).json({ error: "Failed to fetch revisions" });
    }
};

exports.saveRevisions = async (req, res) => {
    try {
        const userIdBuffer = uuidToBuffer(req.user.id);
        const payload = req.body;
        
        // Handle both single object and array payloads
        const items = Array.isArray(payload) ? payload : [payload];
        const results = [];

        for (const item of items) {
            const noteIdBuffer = uuidToBuffer(item.noteId);
            
            // If the item has a single scheduledAt date (from frontend schedule button)
            if (item.scheduledAt) {
                const schedule = await prisma.revision_schedules.create({
                    data: {
                        id: generateUuidBuffer(),
                        note_id: noteIdBuffer,
                        user_id: userIdBuffer,
                        scheduled_at: new Date(item.scheduledAt),
                        status: 'PENDING',
                        complexity_score: item.complexityScore || item.complexity,
                        ai_reason: item.aiReason || item.reason,
                        created_at: new Date()
                    },
                    include: { notes: true }
                });
                results.push(mapRevision(schedule));
            } 
            // If the item has an array of suggestedDates
            else if (item.suggestedDates && Array.isArray(item.suggestedDates)) {
                for (const dateStr of item.suggestedDates) {
                    const schedule = await prisma.revision_schedules.create({
                        data: {
                            id: generateUuidBuffer(),
                            note_id: noteIdBuffer,
                            user_id: userIdBuffer,
                            scheduled_at: new Date(dateStr),
                            status: 'PENDING',
                            complexity_score: item.complexityScore || item.complexity,
                            ai_reason: item.aiReason || item.reason,
                            created_at: new Date()
                        },
                        include: { notes: true }
                    });
                    results.push(mapRevision(schedule));
                }
            }
        }

        // Return array if input was array, else single object
        res.status(201).json(Array.isArray(payload) ? results : results[0]);
    } catch (err) {
        console.error("Save Revisions Error:", err);
        res.status(500).json({ error: "Failed to save revisions" });
    }
};

exports.updateStatus = async (req, res) => {
    try {
        const idBuffer = uuidToBuffer(req.params.id);
        const { status } = req.body;

        const updated = await prisma.revision_schedules.update({
            where: { id: idBuffer },
            data: { status: status },
            include: { notes: true }
        });

        res.json(mapRevision(updated));
    } catch (err) {
        console.error("Update Revision Status Error:", err);
        res.status(500).json({ error: "Failed to update revision status" });
    }
};

exports.deleteRevision = async (req, res) => {
    try {
        const idBuffer = uuidToBuffer(req.params.id);
        await prisma.revision_schedules.delete({
            where: { id: idBuffer }
        });
        res.status(204).send();
    } catch (err) {
        console.error("Delete Revision Error:", err);
        res.status(500).json({ error: "Failed to delete revision" });
    }
};
