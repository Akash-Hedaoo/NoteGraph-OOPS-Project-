const prisma = require('../config/db');
const { uuidToBuffer, bufferToUuid } = require('../config/uuid');

exports.getUserActivity = async (req, res) => {
    try {
        const userIdBuffer = uuidToBuffer(req.params.userId);

        const logs = await prisma.activity_logs.findMany({
            where: { user_id: userIdBuffer },
            orderBy: { created_at: 'desc' },
            take: 50,
            include: { users: true }
        });

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
        console.error("Activity Fetch Error:", err);
        res.status(500).json({ error: "Failed to fetch activity logs" });
    }
};
