// Import bcrypt for password hashing
const bcrypt = require('bcryptjs');
// Import jsonwebtoken for creating auth tokens
const jwt = require('jsonwebtoken');
// Import Prisma database client
const prisma = require('../config/db');
// Import UUID utilities for converting between strings and buffers
const { generateUuidBuffer, bufferToUuid } = require('../config/uuid');

// Register a new user and create their default workspace
exports.register = async (req, res) => {
    try {
        const { email, password, name } = req.body;
        
        // Check if a user with this email already exists
        const existingUser = await prisma.users.findUnique({ where: { email } });
        if (existingUser) {
            return res.status(400).json({ error: 'Email already registered' });
        }

        // Hash the user's password for secure storage
        const passwordHash = await bcrypt.hash(password, 10);
        // Generate a new UUID buffer for the user ID
        const userIdBuffer = generateUuidBuffer();
        
        // Create the new user in the database
        const user = await prisma.users.create({
            data: {
                id: userIdBuffer,
                email,
                name,
                password_hash: passwordHash,
                created_at: new Date(),
                updated_at: new Date()
            }
        });

        // Create a default "My Workspace" for the newly registered user
        await prisma.workspaces.create({
            data: {
                id: generateUuidBuffer(),
                name: "My Workspace",
                owner_id: userIdBuffer,
                created_at: new Date(),
                updated_at: new Date()
            }
        });

        // Respond with success status
        res.status(201).json({ message: "User registered successfully" });
    } catch (err) {
        // Log the error and return a 500 status code if registration fails
        console.error("Register Error:", err);
        res.status(500).json({ error: "Failed to register user" });
    }
};

// Authenticate a user and return a JWT
exports.login = async (req, res) => {
    try {
        const { email, password } = req.body;

        // Find the user by their email
        const user = await prisma.users.findUnique({ where: { email } });
        if (!user) {
            return res.status(401).json({ error: 'Invalid email or password' });
        }

        // Verify that the provided password matches the stored hash
        const isValid = await bcrypt.compare(password, user.password_hash);
        if (!isValid) {
            return res.status(401).json({ error: 'Invalid email or password' });
        }

        // Convert the user ID buffer to a string for the JWT payload
        const userIdString = bufferToUuid(user.id);

        // Generate a JSON Web Token that expires in 24 hours
        const token = jwt.sign(
            { id: userIdString, email: user.email }, 
            process.env.JWT_SECRET, 
            { expiresIn: '24h' }
        );

        // Return the token and basic user information
        res.json({
            token,
            user: {
                id: userIdString,
                email: user.email,
                name: user.name
            }
        });
    } catch (err) {
        // Log the error and return a 500 status code if login fails
        console.error("Login Error:", err);
        res.status(500).json({ error: "Failed to log in" });
    }
};
