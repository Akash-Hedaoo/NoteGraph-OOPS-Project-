// Import Prisma database client
const prisma = require('../config/db');
// Import UUID utilities for converting between strings and buffers
const { uuidToBuffer, bufferToUuid } = require('../config/uuid');

// Fetch tags and their associated notes to generate a knowledge graph structure
exports.getGraphData = async (req, res) => {
    try {
        // Convert the workspace ID from string to a database-compatible buffer
        const workspaceIdBuffer = uuidToBuffer(req.params.workspaceId);

        // Fetch all tags that belong to the specified workspace
        const tags = await prisma.tags.findMany({
            where: { workspace_id: workspaceIdBuffer }
        });

        const graphData = [];

        // Iterate through each tag to find its associated notes
        for (const tag of tags) {
            // Fetch notes that have this specific tag assigned
            const notes = await prisma.notes.findMany({
                where: {
                    note_tags: { some: { tag_id: tag.id } }
                }
            });

            // If the tag has associated notes, add it to the graph data as a branch
            if (notes.length > 0) {
                graphData.push({
                    tagId: bufferToUuid(tag.id),
                    tagTitle: tag.name,
                    tagColor: tag.color,
                    noteCount: notes.length,
                    // Map notes to be the "leaves" of this tag branch
                    leaves: notes.map(n => ({
                        id: bufferToUuid(n.id),
                        title: n.title,
                        tagColor: tag.color
                    }))
                });
            }
        }

        // Return the formatted graph data with a root node for the UI to render
        res.json({
            rootTitle: "Global Workspace Data",
            isRoot: true,
            rootTagColor: "blue",
            branches: graphData
        });
    } catch (err) {
        // Log the error and return a 500 status code if fetching fails
        console.error("Graph Fetch Error:", err);
        res.status(500).json({ error: "Failed to fetch graph data" });
    }
};
