const express = require('express');
const router = express.Router();
const activityController = require('../controllers/activity.controller');
const authenticateToken = require('../middleware/auth');

router.get('/user/:userId', authenticateToken, activityController.getUserActivity);

module.exports = router;
