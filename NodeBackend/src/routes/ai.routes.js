const express = require('express');
const router = express.Router();
const aiController = require('../controllers/ai.controller');
const authenticateToken = require('../middleware/auth');

router.post('/chat/:workspaceId', authenticateToken, aiController.chat);
router.post('/tags/suggest/:workspaceId', authenticateToken, aiController.suggestTags);
router.post('/format', authenticateToken, aiController.formatContent);
router.post('/revision-plan/:workspaceId', authenticateToken, aiController.generateRevisionPlan);

module.exports = router;
