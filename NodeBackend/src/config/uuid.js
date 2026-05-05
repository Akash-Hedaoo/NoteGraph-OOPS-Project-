const { parse, stringify, v4: uuidv4 } = require('uuid');

// Convert Node Buffer to UUID string
function bufferToUuid(buffer) {
    if (!buffer) return null;
    // In some cases Hibernate uses a specific byte order, but standard parse/stringify usually works
    // if standard java.util.UUID was stored as raw bytes.
    return stringify(buffer);
}

// Convert UUID string to Node Buffer
function uuidToBuffer(uuidString) {
    if (!uuidString) return null;
    return Buffer.from(parse(uuidString));
}

// Generate new Buffer UUID
function generateUuidBuffer() {
    return Buffer.from(parse(uuidv4()));
}

module.exports = { bufferToUuid, uuidToBuffer, generateUuidBuffer };
