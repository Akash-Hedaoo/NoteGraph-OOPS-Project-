// Import Express framework to create route handlers
const express = require('express');
const router = express.Router();

// Import the note controller containing logic for note operations
const noteController = require('../controllers/note.controller');

// Import authentication middleware to protect routes
const authenticateToken = require('../middleware/auth');

// Route to get all notes belonging to a specific workspace
router.get('/workspace/:workspaceId', authenticateToken, noteController.getWorkspaceNotes);

// Route to get a specific note by its ID
router.get('/:id', authenticateToken, noteController.getNote);

// Route to get all notes associated with a specific tag
router.get('/tag/:tagId', authenticateToken, noteController.getNotesByTag);

// Route to create a new note
router.post('/', authenticateToken, noteController.createNote);

// Route to update an existing note by its ID
router.put('/:id', authenticateToken, noteController.updateNote);

// Route to toggle the favorite status of a note
router.put('/:id/favorite', authenticateToken, noteController.toggleFavorite);

// Route to add a specific tag to a specific note
router.post('/:noteId/tags/:tagId', authenticateToken, noteController.addTag);

// Route to remove a specific tag from a specific note
router.delete('/:noteId/tags/:tagId', authenticateToken, noteController.removeTag);

// Route to delete a specific note by its ID
router.delete('/:id', authenticateToken, noteController.deleteNote);

// Export the router to be used in the main application
module.exports = router;
