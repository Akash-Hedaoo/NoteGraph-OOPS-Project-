const prisma = require('../config/db');
const { uuidToBuffer, bufferToUuid, generateUuidBuffer } = require('../config/uuid');

exports.getWorkspaceTags = async (req, res) => {
    try {
        const workspaceIdBuffer = uuidToBuffer(req.params.workspaceId);
        const tags = await prisma.tags.findMany({
            where: { workspace_id: workspaceIdBuffer }
        });

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

exports.createTag = async (req, res) => {
    try {
        const { name, color } = req.body;
        const workspaceId = req.body.workspaceId || req.body.workspace?.id;
        const workspaceIdBuffer = uuidToBuffer(workspaceId);

        const tag = await prisma.tags.create({
            data: {
                id: generateUuidBuffer(),
                name,
                color,
                workspace_id: workspaceIdBuffer
            }
        });

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

exports.updateTag = async (req, res) => {
    try {
        const { name, color } = req.body;
        const idBuffer = uuidToBuffer(req.params.id);

        const tag = await prisma.tags.update({
            where: { id: idBuffer },
            data: { name, color }
        });

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

exports.deleteTag = async (req, res) => {
    try {
        const idBuffer = uuidToBuffer(req.params.id);

        await prisma.note_tags.deleteMany({
            where: { tag_id: idBuffer }
        });
        
        await prisma.tags.delete({
            where: { id: idBuffer }
        });

        res.status(204).send();
    } catch (err) {
        console.error("Tag Delete Error:", err);
        res.status(500).json({ error: "Failed to delete tag" });
    }
};
