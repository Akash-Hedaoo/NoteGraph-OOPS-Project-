const express = require('express');
const router = express.Router();
const revisionController = require('../controllers/revision.controller');
const authenticateToken = require('../middleware/auth');

router.get('/workspace/:wsId/user/:userId', authenticateToken, revisionController.getRevisions);
router.post('/', authenticateToken, revisionController.saveRevisions);
router.put('/:id/status', authenticateToken, revisionController.updateStatus);
router.delete('/:id', authenticateToken, revisionController.deleteRevision);

module.exports = router;
