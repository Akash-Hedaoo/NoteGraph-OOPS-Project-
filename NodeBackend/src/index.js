require('dotenv').config();
const express = require('express');
const cors = require('cors');

const app = express();
const PORT = process.env.PORT || 8080;

app.use(cors());
app.use(express.json({ limit: '50mb' }));

// Routes
app.use('/api/auth', require('./routes/auth.routes'));
app.use('/api/workspaces', require('./routes/workspace.routes'));
app.use('/api/tags', require('./routes/tag.routes'));
app.use('/api/notes', require('./routes/note.routes'));
app.use('/api/activity', require('./routes/activity.routes'));
app.use('/api/graph', require('./routes/graph.routes'));
app.use('/api/revisions', require('./routes/revision.routes'));
app.use('/api/ai', require('./routes/ai.routes'));

// Basic health check
app.get('/', (req, res) => {
    res.json({ message: 'NoteGraph Node.js Backend is running' });
});

// Initialize background jobs
const { initScheduler } = require('./jobs/revisionScheduler');
initScheduler();

// Error handling middleware
app.use((err, req, res, next) => {
    console.error(err.stack);
    res.status(500).json({ error: err.message || 'Internal Server Error' });
});

app.listen(PORT, () => {
    console.log(`============================================`);
    console.log(`  NoteGraph NodeBackend (Express)`);
    console.log(`============================================`);
    console.log(`Server is running on http://localhost:${PORT}`);
});
