const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const prisma = require('../config/db');
const { generateUuidBuffer, bufferToUuid } = require('../config/uuid');

exports.register = async (req, res) => {
    try {
        const { email, password, name } = req.body;
        
        const existingUser = await prisma.users.findUnique({ where: { email } });
        if (existingUser) {
            return res.status(400).json({ error: 'Email already registered' });
        }

        const passwordHash = await bcrypt.hash(password, 10);
        const userIdBuffer = generateUuidBuffer();
        
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

        await prisma.workspaces.create({
            data: {
                id: generateUuidBuffer(),
                name: "My Workspace",
                owner_id: userIdBuffer,
                created_at: new Date(),
                updated_at: new Date()
            }
        });

        res.status(201).json({ message: "User registered successfully" });
    } catch (err) {
        console.error("Register Error:", err);
        res.status(500).json({ error: "Failed to register user" });
    }
};

exports.login = async (req, res) => {
    try {
        const { email, password } = req.body;

        const user = await prisma.users.findUnique({ where: { email } });
        if (!user) {
            return res.status(401).json({ error: 'Invalid email or password' });
        }

        const isValid = await bcrypt.compare(password, user.password_hash);
        if (!isValid) {
            return res.status(401).json({ error: 'Invalid email or password' });
        }

        const userIdString = bufferToUuid(user.id);

        const token = jwt.sign(
            { id: userIdString, email: user.email }, 
            process.env.JWT_SECRET, 
            { expiresIn: '24h' }
        );

        res.json({
            token,
            user: {
                id: userIdString,
                email: user.email,
                name: user.name
            }
        });
    } catch (err) {
        console.error("Login Error:", err);
        res.status(500).json({ error: "Failed to log in" });
    }
};
