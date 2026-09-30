// Import Prisma client for database access
const prisma = require('../config/db');

// Import utilities for handling UUIDs
const { uuidToBuffer, generateUuidBuffer } = require('../config/uuid');

// Function to log an activity related to user actions in the database
exports.logActivity = async (userIdBuffer, action, targetType, targetIdBuffer, targetName) => {
    try {
        // Create a new activity log entry using the provided data
        await prisma.activity_logs.create({
            data: {
                id: generateUuidBuffer(), // Generate a unique UUID buffer for the activity ID
                user_id: userIdBuffer, // Buffer representing the user who performed the action
                action: action, // Action performed (e.g., CREATE, UPDATE, DELETE)
                target_type: targetType, // The type of entity targeted (e.g., NOTE, WORKSPACE)
                target_id: targetIdBuffer, // Buffer representing the ID of the targeted entity
                target_name: targetName, // Human-readable name of the target entity
                created_at: new Date() // Timestamp of when the activity occurred
            }
        });
    } catch (err) {
        // Log an error if the activity failed to be recorded
        console.error("Failed to log activity:", err);
    }
};
