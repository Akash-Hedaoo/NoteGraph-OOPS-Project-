const express = require('express');
const router = express.Router();
const noteController = require('../controllers/note.controller');
const authenticateToken = require('../middleware/auth');

router.get('/workspace/:workspaceId', authenticateToken, noteController.getWorkspaceNotes);
router.get('/:id', authenticateToken, noteController.getNote);
router.get('/tag/:tagId', authenticateToken, noteController.getNotesByTag);
router.post('/', authenticateToken, noteController.createNote);
router.put('/:id', authenticateToken, noteController.updateNote);
router.put('/:id/favorite', authenticateToken, noteController.toggleFavorite);
router.post('/:noteId/tags/:tagId', authenticateToken, noteController.addTag);
router.delete('/:noteId/tags/:tagId', authenticateToken, noteController.removeTag);
router.delete('/:id', authenticateToken, noteController.deleteNote);

module.exports = router;
