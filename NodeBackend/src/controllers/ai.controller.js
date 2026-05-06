// Import the AI service which handles communication with external AI providers
const aiService = require('../services/ai.service');
// Import Prisma database client
const prisma = require('../config/db');
// Import UUID utilities for converting between strings and buffers
const { uuidToBuffer, bufferToUuid } = require('../config/uuid');

// Handle chat requests between the user and the AI assistant
exports.chat = async (req, res) => {
    try {
        const workspaceIdBuffer = uuidToBuffer(req.params.workspaceId);
        const { message, currentNoteId, currentNoteTitle, currentNoteContent, currentTags, history } = req.body;

        // Fetch related notes from the knowledge graph based on shared tags
        const relatedNotes = [];
        if (currentTags && currentTags.length > 0) {
            for (const tagId of currentTags) {
                try {
                    // Find all notes that share this tag
                    const notes = await prisma.notes.findMany({
                        where: {
                            note_tags: {
                                some: { tag_id: uuidToBuffer(tagId) }
                            }
                        }
                    });z
                    
                    // Add them to relatedNotes, making sure we don't include the current note or duplicates
                    notes.forEach(n => {
                        const idStr = bufferToUuid(n.id);
                        if (idStr !== currentNoteId && !relatedNotes.find(rn => rn.id === idStr)) {
                            relatedNotes.push({ id: idStr, title: n.title, content: n.content });
                        }
                    });
                } catch (e) { /* skip invalid tag IDs */ }
            }
        }

        // Build the system prompt using the current note context
        let systemPrompt = `You are an AI assistant in a knowledge graph note-taking app. You help the user write notes and answer questions based on their connected knowledge.\n\n--- CURRENT NOTE CONTEXT ---\nTitle: ${currentNoteTitle || 'Untitled'}\nContent:\n${currentNoteContent || ''}\n\n`;

        // Append related notes context to the system prompt if any exist
        if (relatedNotes.length > 0) {
            systemPrompt += "--- RELATED NOTES (KNOWLEDGE GRAPH) ---\n";
            relatedNotes.forEach(n => {
                // Strip HTML tags for cleaner AI processing
                systemPrompt += `Title: ${n.title}\nContent:\n${(n.content || '').replace(/<[^>]*>?/gm, ' ')}\n\n`;
            });
            systemPrompt += "----------------------------\n";
        }

        // Clean and format the chat history for the AI service, handling both frontend formats
        const cleanHistory = [];
        if (history && Array.isArray(history)) {
            for (const msg of history) {
                const text = msg.content || msg.text || '';
                if (text.trim()) {
                    cleanHistory.push({
                        // Normalize roles to either 'model' or 'user'
                        role: msg.role === 'ai' || msg.role === 'model' || msg.role === 'assistant' ? 'model' : 'user',
                        text: text
                    });
                }
            }
        }

        // Add the current user message to the end of the history
        if (message && message.trim()) {
            cleanHistory.push({ role: 'user', text: message });
        }

        // Generate the AI response using the service
        const responseText = await aiService.generateAiResponse(systemPrompt, null, cleanHistory);
        
        // Return JSON with the 'answer' field as expected by the frontend
        res.json({ answer: responseText });
    } catch (err) {
        console.error("AI Chat Error:", err);
        res.status(500).json({ error: err.message });
    }
};

// Generate automated tag suggestions based on note content
exports.suggestTags = async (req, res) => {
    try {
        const workspaceIdBuffer = uuidToBuffer(req.params.workspaceId);
        const { content } = req.body;

        // Fetch existing tags in the workspace to encourage reuse
        const existingTags = await prisma.tags.findMany({
            where: { workspace_id: workspaceIdBuffer }
        });
        const tagNames = existingTags.map(t => t.name).join(', ');

        // Create the prompt asking the AI for a strict JSON array of tags
        const prompt = `You are a tagging assistant. Based on the following note content, suggest 3-5 tags.\nHere are the existing tags in the workspace: ${tagNames || 'None.'}\nPrefer reusing existing tags if they fit perfectly. Otherwise, suggest new relevant ones.\nFormat your response as a strict JSON array of strings, e.g. ["Development", "Architecture"]. Do not include markdown formatting.\n\nNote Content:\n${content}`;

        // Get the AI response
        const responseText = await aiService.generateAiResponse(null, prompt);

        // Attempt to parse the AI response as a JSON array
        const trimmed = responseText.trim();
        let jsonStr = trimmed;
        // Sometimes the AI might wrap the JSON in markdown code blocks or add text, attempt to extract just the array
        if (!trimmed.startsWith("[")) {
            const start = trimmed.indexOf('[');
            const end = trimmed.lastIndexOf(']');
            if (start >= 0 && end > start) {
                jsonStr = trimmed.substring(start, end + 1);
            }
        }

        // Parse and clean up the tags
        const tags = JSON.parse(jsonStr).map(t => t.trim().replace(/[^a-zA-Z0-9- ]/g, ''));
        res.json(tags);
    } catch (err) {
        console.error("AI Tag Suggestion Error:", err);
        res.status(500).json({ error: err.message });
    }
};

// Ask the AI to clean up and format messy note content
exports.formatContent = async (req, res) => {
    try {
        const { content } = req.body;
        // Prompt the AI to format the content as standard plain text
        const prompt = `You are a text formatting assistant. Please format the following messy note content into a clean, well-structured, easy to read plain text document.\nFix spelling and grammatical errors, add proper spacing, newlines, and bullet points if necessary.\nIMPORTANT: Return ONLY the formatted text. DO NOT use Markdown asterisks or hash tags (like **bold** or # Heading), just use standard plain text formatting with newlines and bullet characters like '•'.\n\nNote Content:\n${content}`;

        // Generate the formatted response
        const responseText = await aiService.generateAiResponse(null, prompt);
        
        // Return JSON with the 'formatted' field as expected by the frontend
        res.json({ formatted: responseText });
    } catch (err) {
        console.error("AI Format Content Error:", err);
        res.status(500).json({ error: err.message });
    }
};

// Generate an automated spaced-repetition revision schedule for all notes in a workspace
exports.generateRevisionPlan = async (req, res) => {
    try {
        const workspaceIdBuffer = uuidToBuffer(req.params.workspaceId);
        
        // Fetch all notes in the workspace, ordered by most recently updated
        const notes = await prisma.notes.findMany({
            where: { workspace_id: workspaceIdBuffer },
            orderBy: { updated_at: 'desc' }
        });

        // If there are no notes, return an empty array
        if (notes.length === 0) {
            return res.json([]);
        }

        // Construct the prompt instructing the AI on the rules for spaced-repetition
        let prompt = `You are an intelligent revision planning assistant. Analyze the following notes and create a spaced-repetition revision plan.\n\nIMPORTANT RULES:\n1. Assess the COMPLEXITY of each note on a scale of 1-10 based on technical depth, concepts involved, and difficulty.\n2. Apply the EBBINGHAUS FORGETTING CURVE: humans forget ~56% within 1 hour, ~66% within 1 day, ~75% within 6 days.\n3. Consider the TIME ELAPSED since note creation — older unreviewed notes are MORE URGENT.\n4. Higher complexity notes need MORE FREQUENT revision intervals.\n5. Suggest 2-3 revision dates per note based on optimal spaced-repetition intervals.\n6. Rate urgency as HIGH (overdue), MEDIUM (due soon), or LOW (well within schedule).\n\nCurrent date/time: ${new Date().toISOString()}\n\n--- NOTES ---\n`;

        // Append note metadata and short content previews to the prompt
        notes.forEach(note => {
            prompt += `Note ID: ${bufferToUuid(note.id)}\n`;
            prompt += `Title: ${note.title}\n`;
            
            // Strip HTML and truncate content to save tokens
            let textContent = (note.content || '').replace(/<[^>]*>?/gm, ' ');
            if (textContent.length > 500) textContent = textContent.substring(0, 500);
            
            prompt += `Content Preview: ${textContent}\n`;
            prompt += `Created At: ${note.created_at ? note.created_at.toISOString() : 'unknown'}\n`;
            prompt += `Last Updated: ${note.updated_at ? note.updated_at.toISOString() : 'unknown'}\n`;
            
            // Calculate days since creation for context
            const daysSince = note.created_at ? Math.floor((new Date() - note.created_at) / (1000 * 60 * 60 * 24)) : 0;
            prompt += `Days Since Creation: ${daysSince}\n---\n`;
        });

        // Instruct the AI to return a specific JSON array format
        prompt += `\nReturn a JSON array with this exact structure for EACH note:\n[{"noteId":"uuid", "title":"string", "complexity":number(1-10), "urgency":"HIGH|MEDIUM|LOW", "suggestedDates":["ISO-8601 datetime strings"], "reason":"explanation of why this revision schedule is optimal"}]\n\nDo not include any markdown formatting. Return ONLY the JSON array.`;

        // Generate the plan using the AI service
        const responseText = await aiService.generateAiResponse(null, prompt);

        // Attempt to parse the returned JSON array
        let trimmed = responseText.trim();
        // Handle cases where the AI wrapped the response in markdown blocks or text
        if (!trimmed.startsWith("[")) {
            const start = trimmed.indexOf('[');
            const end = trimmed.lastIndexOf(']');
            if (start >= 0 && end > start) {
                trimmed = trimmed.substring(start, end + 1);
            }
        }

        // Parse and return the generated plan
        const plan = JSON.parse(trimmed);
        res.json(plan);
    } catch (err) {
        console.error("AI Revision Plan Error:", err);
        res.status(500).json({ error: err.message });
    }
};
