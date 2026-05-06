// Import uuid utilities for parsing, stringifying, and generating UUID v4
const { parse, stringify, v4: uuidv4 } = require('uuid');

// Convert a Node Buffer back into a standard UUID string format
function bufferToUuid(buffer) {
    // Return null if no buffer is provided
    if (!buffer) return null;
    
    // Convert the buffer to a UUID string (handles standard Java UUIDs stored as bytes)
    return stringify(buffer);
}

// Convert a standard UUID string into a Node Buffer
function uuidToBuffer(uuidString) {
    // Return null if no UUID string is provided
    if (!uuidString) return null;
    
    // Parse the UUID string and convert it into a Buffer
    return Buffer.from(parse(uuidString));
}

// Generate a new UUID v4 and return it directly as a Node Buffer
function generateUuidBuffer() {
    return Buffer.from(parse(uuidv4()));
}

// Export the UUID conversion utilities
module.exports = { bufferToUuid, uuidToBuffer, generateUuidBuffer };
