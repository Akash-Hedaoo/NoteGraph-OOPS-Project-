// Import Express framework to create route handlers
const express = require('express');
const router = express.Router();

// Import the activity controller which contains the business logic for activity logs
const activityController = require('../controllers/activity.controller');

// Import authentication middleware to protect routes
const authenticateToken = require('../middleware/auth');

// Route to get activity logs for a specific user.
// Requires valid authentication token.
router.get('/user/:userId', authenticateToken, activityController.getUserActivity);

// Export the router to be used in the main application
module.exports = router;
