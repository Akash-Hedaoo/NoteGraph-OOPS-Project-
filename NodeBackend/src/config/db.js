// Import PrismaClient from the Prisma package
const { PrismaClient } = require('@prisma/client');

// Initialize a new Prisma client instance for database operations
const prisma = new PrismaClient();

// Export the Prisma instance for use across the application
module.exports = prisma;
