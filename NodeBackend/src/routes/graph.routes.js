const express = require('express');
const router = express.Router();
const graphController = require('../controllers/graph.controller');
const authenticateToken = require('../middleware/auth');

router.get('/:workspaceId', authenticateToken, graphController.getGraphData);

module.exports = router;
