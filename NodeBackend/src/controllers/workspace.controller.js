const prisma = require('../config/db');
const { uuidToBuffer, bufferToUuid, generateUuidBuffer } = require('../config/uuid');

exports.getUserWorkspaces = async (req, res) => {
    try {
        const user = await prisma.users.findUnique({ where: { email: req.user.email } });
        
        const workspaces = await prisma.workspaces.findMany({
            where: { owner_id: user.id },
            orderBy: { created_at: 'asc' }
        });

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

exports.createWorkspace = async (req, res) => {
    try {
        const { name } = req.body;
        const user = await prisma.users.findUnique({ where: { email: req.user.email } });

        const workspace = await prisma.workspaces.create({
            data: {
                id: generateUuidBuffer(),
                name,
                owner_id: user.id,
                created_at: new Date(),
                updated_at: new Date()
            }
        });

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

exports.renameWorkspace = async (req, res) => {
    try {
        const { name } = req.body;
        const idBuffer = uuidToBuffer(req.params.id);

        const workspace = await prisma.workspaces.update({
            where: { id: idBuffer },
            data: { name, updated_at: new Date() }
        });

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

exports.deleteWorkspace = async (req, res) => {
    try {
        const idBuffer = uuidToBuffer(req.params.id);

        // Delete cascade logic if DB doesn't handle it
        await prisma.note_tags.deleteMany({
            where: {
                notes: { workspace_id: idBuffer }
            }
        });
        
        await prisma.revision_schedules.deleteMany({
            where: {
                notes: { workspace_id: idBuffer }
            }
        });
        
        await prisma.notes.deleteMany({ where: { workspace_id: idBuffer } });
        await prisma.tags.deleteMany({ where: { workspace_id: idBuffer } });
        await prisma.folders.deleteMany({ where: { workspace_id: idBuffer } });
        
        await prisma.workspaces.delete({ where: { id: idBuffer } });

        res.status(204).send();
    } catch (err) {
        console.error("Workspace Delete Error:", err);
        res.status(500).json({ error: "Failed to delete workspace" });
    }
};
