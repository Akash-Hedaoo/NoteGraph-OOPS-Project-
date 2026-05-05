const express = require('express');
const router = express.Router();
const tagController = require('../controllers/tag.controller');
const authenticateToken = require('../middleware/auth');

router.get('/workspace/:workspaceId', authenticateToken, tagController.getWorkspaceTags);
router.post('/', authenticateToken, tagController.createTag);
router.put('/:id', authenticateToken, tagController.updateTag);
router.delete('/:id', authenticateToken, tagController.deleteTag);

module.exports = router;
