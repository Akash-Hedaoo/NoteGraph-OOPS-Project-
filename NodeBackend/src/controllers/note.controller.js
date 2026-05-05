const prisma = require('../config/db');
const { uuidToBuffer, bufferToUuid, generateUuidBuffer } = require('../config/uuid');
const activityService = require('../services/activity.service');

const mapNoteToDto = (n) => ({
    id: bufferToUuid(n.id),
    title: n.title,
    content: n.content,
    favorite: n.is_favorite ? true : false,
    status: n.status,
    createdAt: n.created_at,
    updatedAt: n.updated_at,
    owner: n.users ? {
        id: bufferToUuid(n.users.id),
        name: n.users.name,
        email: n.users.email
    } : undefined,
    tags: n.note_tags ? n.note_tags.map(nt => ({
        id: bufferToUuid(nt.tags.id),
        name: nt.tags.name,
        color: nt.tags.color
    })) : []
});

exports.getWorkspaceNotes = async (req, res) => {
    try {
        const workspaceIdBuffer = uuidToBuffer(req.params.workspaceId);

        // Auto-cleanup: Delete untagged notes with no body
        const allNotes = await prisma.notes.findMany({
            where: { workspace_id: workspaceIdBuffer },
            include: { note_tags: true }
        });
        
        for (const n of allNotes) {
            const hasNoContent = !n.content || n.content.trim() === '' || n.content === '<br>' || n.content === '<p><br></p>';
            if (hasNoContent && n.note_tags.length === 0) {
                await prisma.revision_schedules.deleteMany({ where: { note_id: n.id } });
                await prisma.notes.delete({ where: { id: n.id } });
            }
        }

        const notes = await prisma.notes.findMany({
            where: { workspace_id: workspaceIdBuffer },
            orderBy: { updated_at: 'desc' },
            include: {
                note_tags: {
                    include: { tags: true }
                },
                users: true
            }
        });
        res.json(notes.map(mapNoteToDto));
    } catch (err) {
        console.error("Note Fetch Error:", err);
        res.status(500).json({ error: "Failed to fetch notes" });
    }
};

exports.getNote = async (req, res) => {
    try {
        const idBuffer = uuidToBuffer(req.params.id);
        const note = await prisma.notes.findUnique({
            where: { id: idBuffer },
            include: {
                note_tags: { include: { tags: true } },
                users: true
            }
        });
        
        if (!note) return res.status(404).json({ error: "Note not found" });
        res.json(mapNoteToDto(note));
    } catch (err) {
        console.error("Note Fetch Error:", err);
        res.status(500).json({ error: "Failed to fetch note" });
    }
};

exports.getNotesByTag = async (req, res) => {
    try {
        const tagIdBuffer = uuidToBuffer(req.params.tagId);
        const notes = await prisma.notes.findMany({
            where: {
                note_tags: { some: { tag_id: tagIdBuffer } }
            },
            include: {
                note_tags: { include: { tags: true } },
                users: true
            }
        });
        res.json(notes.map(mapNoteToDto));
    } catch (err) {
        res.status(500).json({ error: "Failed to fetch notes by tag" });
    }
};

exports.createNote = async (req, res) => {
    try {
        const { title, content, tags } = req.body;
        // Handle both formats: { workspaceId } or { workspace: { id } }
        const workspaceId = req.body.workspaceId || req.body.workspace?.id;
        const ownerId = req.body.ownerId || req.body.owner?.id || req.user.id;

        const workspaceIdBuffer = uuidToBuffer(workspaceId);
        const userIdBuffer = uuidToBuffer(ownerId);
        const noteIdBuffer = generateUuidBuffer();

        const note = await prisma.notes.create({
            data: {
                id: noteIdBuffer,
                title: title || 'Untitled Note',
                content: content || '',
                is_favorite: false,
                status: 'DRAFT',
                workspace_id: workspaceIdBuffer,
                owner_id: userIdBuffer,
                created_at: new Date(),
                updated_at: new Date()
            }
        });

        if (tags && tags.length > 0) {
            for (const tagId of tags) {
                await prisma.note_tags.create({
                    data: {
                        note_id: noteIdBuffer,
                        tag_id: uuidToBuffer(tagId)
                    }
                });
            }
        }

        await activityService.logActivity(userIdBuffer, 'created', 'NOTE', noteIdBuffer, title || 'Untitled Note');

        const savedNote = await prisma.notes.findUnique({
            where: { id: noteIdBuffer },
            include: { note_tags: { include: { tags: true } }, users: true }
        });

        res.status(201).json(mapNoteToDto(savedNote));
    } catch (err) {
        console.error("Note Create Error:", err);
        res.status(500).json({ error: "Failed to create note" });
    }
};

exports.updateNote = async (req, res) => {
    try {
        const idBuffer = uuidToBuffer(req.params.id);
        const userIdBuffer = uuidToBuffer(req.user.id);
        const { title, content, tags } = req.body;

        await prisma.notes.update({
            where: { id: idBuffer },
            data: { title, content, updated_at: new Date() }
        });

        if (tags) {
            // Re-sync tags
            await prisma.note_tags.deleteMany({ where: { note_id: idBuffer } });
            for (const tagId of tags) {
                await prisma.note_tags.create({
                    data: { note_id: idBuffer, tag_id: uuidToBuffer(tagId) }
                });
            }
        }

        await activityService.logActivity(userIdBuffer, 'edited', 'NOTE', idBuffer, title || 'note');

        const updatedNote = await prisma.notes.findUnique({
            where: { id: idBuffer },
            include: { note_tags: { include: { tags: true } }, users: true }
        });

        res.json(mapNoteToDto(updatedNote));
    } catch (err) {
        console.error("Note Update Error:", err);
        res.status(500).json({ error: "Failed to update note" });
    }
};

exports.toggleFavorite = async (req, res) => {
    try {
        const idBuffer = uuidToBuffer(req.params.id);
        
        // If isFavorite is provided in body, use it; otherwise toggle
        let newFavValue;
        if (req.body && req.body.isFavorite !== undefined) {
            newFavValue = req.body.isFavorite;
        } else {
            const existing = await prisma.notes.findUnique({ where: { id: idBuffer } });
            newFavValue = !existing.is_favorite;
        }

        const note = await prisma.notes.update({
            where: { id: idBuffer },
            data: { is_favorite: newFavValue, updated_at: new Date() },
            include: { note_tags: { include: { tags: true } }, users: true }
        });

        res.json(mapNoteToDto(note));
    } catch (err) {
        res.status(500).json({ error: "Failed to update favorite status" });
    }
};

exports.addTag = async (req, res) => {
    try {
        const noteIdBuffer = uuidToBuffer(req.params.noteId);
        const tagIdBuffer = uuidToBuffer(req.params.tagId);

        // Check if tag already exists on note
        const existing = await prisma.note_tags.findUnique({
            where: {
                note_id_tag_id: {
                    note_id: noteIdBuffer,
                    tag_id: tagIdBuffer
                }
            }
        });

        if (!existing) {
            await prisma.note_tags.create({
                data: { note_id: noteIdBuffer, tag_id: tagIdBuffer }
            });
        }

        const note = await prisma.notes.findUnique({
            where: { id: noteIdBuffer },
            include: { note_tags: { include: { tags: true } }, users: true }
        });

        res.json(mapNoteToDto(note));
    } catch (err) {
        console.error("Add Tag Error:", err);
        res.status(500).json({ error: "Failed to add tag to note" });
    }
};

exports.removeTag = async (req, res) => {
    try {
        const noteIdBuffer = uuidToBuffer(req.params.noteId);
        const tagIdBuffer = uuidToBuffer(req.params.tagId);

        await prisma.note_tags.delete({
            where: {
                note_id_tag_id: {
                    note_id: noteIdBuffer,
                    tag_id: tagIdBuffer
                }
            }
        });

        res.status(204).send();
    } catch (err) {
        res.status(500).json({ error: "Failed to remove tag from note" });
    }
};

exports.deleteNote = async (req, res) => {
    try {
        const idBuffer = uuidToBuffer(req.params.id);
        const userIdBuffer = uuidToBuffer(req.user.id);

        const note = await prisma.notes.findUnique({ where: { id: idBuffer } });

        await prisma.note_tags.deleteMany({ where: { note_id: idBuffer } });
        await prisma.revision_schedules.deleteMany({ where: { note_id: idBuffer } });
        await prisma.notes.delete({ where: { id: idBuffer } });

        if (note) {
            await activityService.logActivity(userIdBuffer, 'deleted', 'NOTE', idBuffer, note.title);
        }

        res.status(204).send();
    } catch (err) {
        console.error("Note Delete Error:", err);
        res.status(500).json({ error: "Failed to delete note" });
    }
};
