// Import Prisma database client
const prisma = require('../config/db');
// Import UUID utilities for converting between strings and buffers
const { uuidToBuffer, bufferToUuid, generateUuidBuffer } = require('../config/uuid');

// Fetch all workspaces owned by the authenticated user
exports.getUserWorkspaces = async (req, res) => {
    try {
        // Find the user by their email from the JWT token
        const user = await prisma.users.findUnique({ where: { email: req.user.email } });
        
        // Find all workspaces owned by this user, ordered by creation date
        const workspaces = await prisma.workspaces.findMany({
            where: { owner_id: user.id },
            orderBy: { created_at: 'asc' }
        });

        // Format and return the workspaces
        res.json(workspaces.map(w => ({
            id: bufferToUuid(w.id),
            name: w.name,
            createdAt: w.created_at,
            updatedAt: w.updated_at
        })));
    } catch (err) {
        console.error("Workspace Fetch Error:", err);
        res.status(500).json({ error: "Failed to fetch workspaces" });
    }
};

// Create a new workspace for the authenticated user
exports.createWorkspace = async (req, res) => {
    try {
        const { name } = req.body;
        // Identify the user based on their JWT token
        const user = await prisma.users.findUnique({ where: { email: req.user.email } });

        // Insert the new workspace into the database
        const workspace = await prisma.workspaces.create({
            data: {
                id: generateUuidBuffer(),
                name,
                owner_id: user.id,
                created_at: new Date(),
                updated_at: new Date()
            }
        });

        // Return the created workspace details
        res.status(201).json({
            id: bufferToUuid(workspace.id),
            name: workspace.name,
            createdAt: workspace.created_at,
            updatedAt: workspace.updated_at
        });
    } catch (err) {
        console.error("Workspace Create Error:", err);
        res.status(500).json({ error: "Failed to create workspace" });
    }
};

// Rename an existing workspace
exports.renameWorkspace = async (req, res) => {
    try {
        const { name } = req.body;
        const idBuffer = uuidToBuffer(req.params.id);

        // Update the workspace name and its updated_at timestamp
        const workspace = await prisma.workspaces.update({
            where: { id: idBuffer },
            data: { name, updated_at: new Date() }
        });

        // Return the updated workspace
        res.json({
            id: bufferToUuid(workspace.id),
            name: workspace.name,
            createdAt: workspace.created_at,
            updatedAt: workspace.updated_at
        });
    } catch (err) {
        console.error("Workspace Rename Error:", err);
        res.status(500).json({ error: "Failed to rename workspace" });
    }
};

// Delete a workspace and cascade delete all its associated data
exports.deleteWorkspace = async (req, res) => {
    try {
        const idBuffer = uuidToBuffer(req.params.id);

        // Delete cascade logic if the database doesn't handle it natively
        // 1. Delete note-tag associations in this workspace
        await prisma.note_tags.deleteMany({
            where: {
                notes: { workspace_id: idBuffer }
            }
        });
        
        // 2. Delete revision schedules for notes in this workspace
        await prisma.revision_schedules.deleteMany({
            where: {
                notes: { workspace_id: idBuffer }
            }
        });
        
        // 3. Delete notes, tags, and folders belonging to this workspace
        await prisma.notes.deleteMany({ where: { workspace_id: idBuffer } });
        await prisma.tags.deleteMany({ where: { workspace_id: idBuffer } });
        await prisma.folders.deleteMany({ where: { workspace_id: idBuffer } });
        
        // 4. Finally, delete the workspace itself
        await prisma.workspaces.delete({ where: { id: idBuffer } });

        // Respond with a 204 No Content status on success
        res.status(204).send();
    } catch (err) {
        console.error("Workspace Delete Error:", err);
        res.status(500).json({ error: "Failed to delete workspace" });
    }
};
