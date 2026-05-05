const express = require('express');
const router = express.Router();
const workspaceController = require('../controllers/workspace.controller');
const authenticateToken = require('../middleware/auth');

router.get('/', authenticateToken, workspaceController.getUserWorkspaces);
router.post('/', authenticateToken, workspaceController.createWorkspace);
router.put('/:id', authenticateToken, workspaceController.renameWorkspace);
router.delete('/:id', authenticateToken, workspaceController.deleteWorkspace);

module.exports = router;
