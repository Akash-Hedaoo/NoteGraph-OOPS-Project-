// Import Express framework to create route handlers
const express = require('express');
const router = express.Router();

// Import the tag controller containing logic for managing tags
const tagController = require('../controllers/tag.controller');

// Import authentication middleware to protect routes
const authenticateToken = require('../middleware/auth');

// Route to get all tags belonging to a specific workspace
router.get('/workspace/:workspaceId', authenticateToken, tagController.getWorkspaceTags);

// Route to create a new tag
router.post('/', authenticateToken, tagController.createTag);

// Route to update an existing tag by its ID
router.put('/:id', authenticateToken, tagController.updateTag);

// Route to delete a specific tag by its ID
router.delete('/:id', authenticateToken, tagController.deleteTag);

// Export the router to be used in the main application
module.exports = router;
