// Import Prisma database client
const prisma = require('../config/db');
// Import UUID utilities for converting between strings and buffers
const { uuidToBuffer, bufferToUuid, generateUuidBuffer } = require('../config/uuid');
// Import activity service to log user actions
const activityService = require('../services/activity.service');

// Utility function to map raw database note objects into structured DTOs for the frontend
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

// Fetch all notes for a given workspace
exports.getWorkspaceNotes = async (req, res) => {
    try {
        const workspaceIdBuffer = uuidToBuffer(req.params.workspaceId);

        // Auto-cleanup: Find all notes in this workspace to check for empty/dangling notes
        const allNotes = await prisma.notes.findMany({
            where: { workspace_id: workspaceIdBuffer },
            include: { note_tags: true }
        });
        
        // Iterate through notes and delete those that have no content and no tags attached
        for (const n of allNotes) {
            const hasNoContent = !n.content || n.content.trim() === '' || n.content === '<br>' || n.content === '<p><br></p>';
            if (hasNoContent && n.note_tags.length === 0) {
                // Delete associated revision schedules first to prevent foreign key errors
                await prisma.revision_schedules.deleteMany({ where: { note_id: n.id } });
                // Delete the empty note
                await prisma.notes.delete({ where: { id: n.id } });
            }
        }

        // Fetch the cleaned list of notes ordered by recently updated
        const notes = await prisma.notes.findMany({
            where: { workspace_id: workspaceIdBuffer },
            orderBy: { updated_at: 'desc' },
            include: {
                note_tags: {
                    include: { tags: true }
                },
                users: true // Include owner details
            }
        });
        
        // Map and return the notes
        res.json(notes.map(mapNoteToDto));
    } catch (err) {
        console.error("Note Fetch Error:", err);
        res.status(500).json({ error: "Failed to fetch notes" });
    }
};

// Fetch a single specific note by its ID
exports.getNote = async (req, res) => {
    try {
        const idBuffer = uuidToBuffer(req.params.id);
        
        // Query the database for the note and include related tags and user info
        const note = await prisma.notes.findUnique({
            where: { id: idBuffer },
            include: {
                note_tags: { include: { tags: true } },
                users: true
            }
        });
        
        // If the note doesn't exist, return a 404 Not Found error
        if (!note) return res.status(404).json({ error: "Note not found" });
        
        // Return the mapped note DTO
        res.json(mapNoteToDto(note));
    } catch (err) {
        console.error("Note Fetch Error:", err);
        res.status(500).json({ error: "Failed to fetch note" });
    }
};

// Fetch all notes associated with a specific tag
exports.getNotesByTag = async (req, res) => {
    try {
        const tagIdBuffer = uuidToBuffer(req.params.tagId);
        
        // Query the database for notes that have a note_tags entry matching the tag ID
        const notes = await prisma.notes.findMany({
            where: {
                note_tags: { some: { tag_id: tagIdBuffer } }
            },
            include: {
                note_tags: { include: { tags: true } },
                users: true
            }
        });
        
        // Map and return the found notes
        res.json(notes.map(mapNoteToDto));
    } catch (err) {
        res.status(500).json({ error: "Failed to fetch notes by tag" });
    }
};

// Create a new note
exports.createNote = async (req, res) => {
    try {
        const { title, content, tags } = req.body;
        // Extract workspace and owner IDs, handling different payload formats
        const workspaceId = req.body.workspaceId || req.body.workspace?.id;
        const ownerId = req.body.ownerId || req.body.owner?.id || req.user.id;

        const workspaceIdBuffer = uuidToBuffer(workspaceId);
        const userIdBuffer = uuidToBuffer(ownerId);
        const noteIdBuffer = generateUuidBuffer();

        // Create the base note record in the database
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

        // If tags were provided during creation, associate them with the note
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

        // Log this creation activity for the user
        await activityService.logActivity(userIdBuffer, 'created', 'NOTE', noteIdBuffer, title || 'Untitled Note');

        // Fetch the newly fully created note including tags to return to the frontend
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

// Update an existing note's title, content, or tags
exports.updateNote = async (req, res) => {
    try {
        const idBuffer = uuidToBuffer(req.params.id);
        const userIdBuffer = uuidToBuffer(req.user.id);
        const { title, content, tags } = req.body;

        // Update the main note record
        await prisma.notes.update({
            where: { id: idBuffer },
            data: { title, content, updated_at: new Date() }
        });

        // If tags were provided, sync them by deleting old ones and inserting the new ones
        if (tags) {
            await prisma.note_tags.deleteMany({ where: { note_id: idBuffer } });
            for (const tagId of tags) {
                await prisma.note_tags.create({
                    data: { note_id: idBuffer, tag_id: uuidToBuffer(tagId) }
                });
            }
        }

        // Log this edit activity for the user
        await activityService.logActivity(userIdBuffer, 'edited', 'NOTE', idBuffer, title || 'note');

        // Fetch and return the updated note data
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

// Toggle the favorite status of a note
exports.toggleFavorite = async (req, res) => {
    try {
        const idBuffer = uuidToBuffer(req.params.id);
        
        // Determine the new favorite value based on body payload or by toggling current state
        let newFavValue;
        if (req.body && req.body.isFavorite !== undefined) {
            newFavValue = req.body.isFavorite;
        } else {
            const existing = await prisma.notes.findUnique({ where: { id: idBuffer } });
            newFavValue = !existing.is_favorite;
        }

        // Update the favorite flag in the database
        const note = await prisma.notes.update({
            where: { id: idBuffer },
            data: { is_favorite: newFavValue, updated_at: new Date() },
            include: { note_tags: { include: { tags: true } }, users: true }
        });

        // Return the updated note DTO
        res.json(mapNoteToDto(note));
    } catch (err) {
        res.status(500).json({ error: "Failed to update favorite status" });
    }
};

// Add a single tag to a note
exports.addTag = async (req, res) => {
    try {
        const noteIdBuffer = uuidToBuffer(req.params.noteId);
        const tagIdBuffer = uuidToBuffer(req.params.tagId);

        // Check if the tag is already associated with the note to prevent duplicates
        const existing = await prisma.note_tags.findUnique({
            where: {
                note_id_tag_id: {
                    note_id: noteIdBuffer,
                    tag_id: tagIdBuffer
                }
            }
        });

        // If it doesn't exist, create the association
        if (!existing) {
            await prisma.note_tags.create({
                data: { note_id: noteIdBuffer, tag_id: tagIdBuffer }
            });
        }

        // Fetch and return the updated note
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

// Remove a single tag from a note
exports.removeTag = async (req, res) => {
    try {
        const noteIdBuffer = uuidToBuffer(req.params.noteId);
        const tagIdBuffer = uuidToBuffer(req.params.tagId);

        // Delete the association record between the note and the tag
        await prisma.note_tags.delete({
            where: {
                note_id_tag_id: {
                    note_id: noteIdBuffer,
                    tag_id: tagIdBuffer
                }
            }
        });

        // Respond with 204 No Content upon success
        res.status(204).send();
    } catch (err) {
        res.status(500).json({ error: "Failed to remove tag from note" });
    }
};

// Delete a note and all its associated data
exports.deleteNote = async (req, res) => {
    try {
        const idBuffer = uuidToBuffer(req.params.id);
        const userIdBuffer = uuidToBuffer(req.user.id);

        // Fetch the note details before deletion so we can log its name
        const note = await prisma.notes.findUnique({ where: { id: idBuffer } });

        // Delete related records to prevent foreign key errors
        await prisma.note_tags.deleteMany({ where: { note_id: idBuffer } });
        await prisma.revision_schedules.deleteMany({ where: { note_id: idBuffer } });
        
        // Finally, delete the actual note record
        await prisma.notes.delete({ where: { id: idBuffer } });

        // If the note existed, log the deletion activity for the user
        if (note) {
            await activityService.logActivity(userIdBuffer, 'deleted', 'NOTE', idBuffer, note.title);
        }

        // Respond with 204 No Content upon success
        res.status(204).send();
    } catch (err) {
        console.error("Note Delete Error:", err);
        res.status(500).json({ error: "Failed to delete note" });
    }
};
