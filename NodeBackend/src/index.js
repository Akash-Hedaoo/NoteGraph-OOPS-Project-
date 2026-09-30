// Load environment variables from .env file
require('dotenv').config();

// Import necessary Express and CORS modules
const express = require('express');
const cors = require('cors');

// Initialize the Express application
const app = express();

// Define the port for the server to listen on, defaulting to 8080
const PORT = process.env.PORT || 8080;

// Apply CORS middleware to allow cross-origin requests
app.use(cors());

// Parse incoming JSON requests with a payload limit of 50mb
app.use(express.json({ limit: '50mb' }));

// ---------------------------------------------------------
// Route Definitions
// ---------------------------------------------------------
// Setup API endpoints for various resources
app.use('/api/auth', require('./routes/auth.routes'));
app.use('/api/workspaces', require('./routes/workspace.routes'));
app.use('/api/tags', require('./routes/tag.routes'));
app.use('/api/notes', require('./routes/note.routes'));
app.use('/api/activity', require('./routes/activity.routes'));
app.use('/api/graph', require('./routes/graph.routes'));
app.use('/api/revisions', require('./routes/revision.routes'));
app.use('/api/ai', require('./routes/ai.routes'));

// Basic health check endpoint to verify server status
app.get('/', (req, res) => {
    res.json({ message: 'NoteGraph Node.js Backend is running' });
});

// ---------------------------------------------------------
// Background Jobs
// ---------------------------------------------------------
// Initialize the background scheduler for revisions
const { initScheduler } = require('./jobs/revisionScheduler');
initScheduler();

// ---------------------------------------------------------
// Error Handling
// ---------------------------------------------------------
// Global error handling middleware
app.use((err, req, res, next) => {
    console.error(err.stack); // Log the error stack trace
    res.status(500).json({ error: err.message || 'Internal Server Error' }); // Send a 500 response
});

// Start the server and listen on the configured port
app.listen(PORT, () => {
    console.log(`============================================`);
    console.log(`  NoteGraph NodeBackend (Express)`);
    console.log(`============================================`);
    console.log(`Server is running on http://localhost:${PORT}`);
});
