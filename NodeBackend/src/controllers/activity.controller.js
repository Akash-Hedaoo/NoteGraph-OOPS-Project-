// Import Prisma database client
const prisma = require('../config/db');
// Import UUID utilities for converting between strings and buffers
const { uuidToBuffer, bufferToUuid } = require('../config/uuid');

// Fetch the most recent activity logs for a specific user
exports.getUserActivity = async (req, res) => {
    try {
        // Convert the user ID from string to a database-compatible buffer
        const userIdBuffer = uuidToBuffer(req.params.userId);

        // Query the database for the 50 most recent activity logs for this user
        const logs = await prisma.activity_logs.findMany({
            where: { user_id: userIdBuffer },
            orderBy: { created_at: 'desc' },
            take: 50,
            include: { users: true } // Include user details in the result
        });

        // Format the retrieved logs into a JSON-friendly structure
        res.json(logs.map(log => ({
            id: bufferToUuid(log.id),
            user: {
                id: bufferToUuid(log.users.id),
                name: log.users.name,
                email: log.users.email
            },
            action: log.action,
            targetType: log.target_type,
            targetId: bufferToUuid(log.target_id),
            targetName: log.target_name,
            createdAt: log.created_at
        })));
    } catch (err) {
        // Log the error and return a 500 status code if fetching fails
        console.error("Activity Fetch Error:", err);
        res.status(500).json({ error: "Failed to fetch activity logs" });
    }
};
