// Import Express framework to create route handlers
const express = require('express');
const router = express.Router();

// Import the AI controller for handling AI-related requests
const aiController = require('../controllers/ai.controller');

// Import authentication middleware to protect routes
const authenticateToken = require('../middleware/auth');

// Route to chat with the AI assistant within a specific workspace context
router.post('/chat/:workspaceId', authenticateToken, aiController.chat);

// Route to get AI-suggested tags for a workspace
router.post('/tags/suggest/:workspaceId', authenticateToken, aiController.suggestTags);

// Route to format content using AI
router.post('/format', authenticateToken, aiController.formatContent);

// Route to generate a revision plan for a workspace using AI
router.post('/revision-plan/:workspaceId', authenticateToken, aiController.generateRevisionPlan);

// Export the router to be used in the main application
module.exports = router;
