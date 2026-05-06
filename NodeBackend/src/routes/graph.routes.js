// Import Express framework to create route handlers
const express = require('express');
const router = express.Router();

// Import the graph controller to handle knowledge graph data retrieval
const graphController = require('../controllers/graph.controller');

// Import authentication middleware to protect routes
const authenticateToken = require('../middleware/auth');

// Route to fetch graph node and link data for a specific workspace
// Requires a valid authentication token
router.get('/:workspaceId', authenticateToken, graphController.getGraphData);

// Export the router to be used in the main application
module.exports = router;
