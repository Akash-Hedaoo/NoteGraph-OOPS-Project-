const prisma = require('../config/db');
const { uuidToBuffer, bufferToUuid } = require('../config/uuid');

exports.getGraphData = async (req, res) => {
    try {
        const workspaceIdBuffer = uuidToBuffer(req.params.workspaceId);

        const tags = await prisma.tags.findMany({
            where: { workspace_id: workspaceIdBuffer }
        });

        const graphData = [];

        for (const tag of tags) {
            const notes = await prisma.notes.findMany({
                where: {
                    note_tags: { some: { tag_id: tag.id } }
                }
            });

            if (notes.length > 0) {
                graphData.push({
                    tagId: bufferToUuid(tag.id),
                    tagTitle: tag.name,
                    tagColor: tag.color,
                    noteCount: notes.length,
                    leaves: notes.map(n => ({
                        id: bufferToUuid(n.id),
                        title: n.title,
                        tagColor: tag.color
                    }))
                });
            }
        }

        res.json({
            rootTitle: "Global Workspace Data",
            isRoot: true,
            rootTagColor: "blue",
            branches: graphData
        });
    } catch (err) {
        console.error("Graph Fetch Error:", err);
        res.status(500).json({ error: "Failed to fetch graph data" });
    }
};
