const prisma = require('../config/db');
const { uuidToBuffer, generateUuidBuffer } = require('../config/uuid');

exports.logActivity = async (userIdBuffer, action, targetType, targetIdBuffer, targetName) => {
    try {
        await prisma.activity_logs.create({
            data: {
                id: generateUuidBuffer(),
                user_id: userIdBuffer,
                action: action,
                target_type: targetType,
                target_id: targetIdBuffer,
                target_name: targetName,
                created_at: new Date()
            }
        });
    } catch (err) {
        console.error("Failed to log activity:", err);
    }
};
