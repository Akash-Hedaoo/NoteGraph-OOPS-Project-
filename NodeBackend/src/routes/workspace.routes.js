// Import Express framework to create route handlers
const express = require('express');
const router = express.Router();

// Import the workspace controller containing logic for managing workspaces
const workspaceController = require('../controllers/workspace.controller');

// Import authentication middleware to protect routes
const authenticateToken = require('../middleware/auth');

// Route to get all workspaces belonging to the authenticated user
router.get('/', authenticateToken, workspaceController.getUserWorkspaces);

// Route to create a new workspace
router.post('/', authenticateToken, workspaceController.createWorkspace);

// Route to rename or update an existing workspace by its ID
router.put('/:id', authenticateToken, workspaceController.renameWorkspace);

// Route to delete a specific workspace by its ID
router.delete('/:id', authenticateToken, workspaceController.deleteWorkspace);

// Export the router to be used in the main application
module.exports = router;
