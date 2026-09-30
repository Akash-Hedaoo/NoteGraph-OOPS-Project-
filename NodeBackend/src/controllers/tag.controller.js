// Import Prisma database client
const prisma = require('../config/db');
// Import UUID utilities for converting between strings and buffers
const { uuidToBuffer, bufferToUuid, generateUuidBuffer } = require('../config/uuid');

// Fetch all tags belonging to a specific workspace
exports.getWorkspaceTags = async (req, res) => {
    try {
        const workspaceIdBuffer = uuidToBuffer(req.params.workspaceId);
        
        // Find all tags associated with the given workspace ID
        const tags = await prisma.tags.findMany({
            where: { workspace_id: workspaceIdBuffer }
        });

        // Map the results to a JSON-friendly format
        res.json(tags.map(t => ({
            id: bufferToUuid(t.id),
            name: t.name,
            color: t.color
        })));
    } catch (err) {
        console.error("Tag Fetch Error:", err);
        res.status(500).json({ error: "Failed to fetch tags" });
    }
};

// Create a new tag within a specific workspace
exports.createTag = async (req, res) => {
    try {
        const { name, color } = req.body;
        // Handle payload formats from different frontend sources
        const workspaceId = req.body.workspaceId || req.body.workspace?.id;
        const workspaceIdBuffer = uuidToBuffer(workspaceId);

        // Insert the new tag into the database
        const tag = await prisma.tags.create({
            data: {
                id: generateUuidBuffer(),
                name,
                color,
                workspace_id: workspaceIdBuffer
            }
        });

        // Return the created tag
        res.status(201).json({
            id: bufferToUuid(tag.id),
            name: tag.name,
            color: tag.color
        });
    } catch (err) {
        console.error("Tag Create Error:", err);
        res.status(500).json({ error: "Failed to create tag" });
    }
};

// Update an existing tag's name or color
exports.updateTag = async (req, res) => {
    try {
        const { name, color } = req.body;
        const idBuffer = uuidToBuffer(req.params.id);

        // Update the tag record in the database
        const tag = await prisma.tags.update({
            where: { id: idBuffer },
            data: { name, color }
        });

        // Return the updated tag
        res.json({
            id: bufferToUuid(tag.id),
            name: tag.name,
            color: tag.color
        });
    } catch (err) {
        console.error("Tag Update Error:", err);
        res.status(500).json({ error: "Failed to update tag" });
    }
};

// Delete a tag and remove its associations from all notes
exports.deleteTag = async (req, res) => {
    try {
        const idBuffer = uuidToBuffer(req.params.id);

        // First, remove the tag from any notes it is attached to (prevent foreign key constraint errors)
        await prisma.note_tags.deleteMany({
            where: { tag_id: idBuffer }
        });
        
        // Then delete the tag itself
        await prisma.tags.delete({
            where: { id: idBuffer }
        });

        // Respond with a 204 No Content status on success
        res.status(204).send();
    } catch (err) {
        console.error("Tag Delete Error:", err);
        res.status(500).json({ error: "Failed to delete tag" });
    }
};
