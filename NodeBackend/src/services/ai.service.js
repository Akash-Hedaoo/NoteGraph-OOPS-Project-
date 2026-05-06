// Import axios for making HTTP requests
const axios = require('axios');
// Import Prisma database client
const prisma = require('../config/db');
// Import UUID utilities
const { uuidToBuffer, bufferToUuid } = require('../config/uuid');

// Load API keys from environment variables
const GEMINI_API_KEY = process.env.GEMINI_API_KEY;
const MISTRAL_API_KEY = process.env.MISTRAL_API_KEY;

// Define preferred Gemini models in order of fallback priority
const geminiModels = [
    "gemini-1.5-flash",
    "gemini-2.5-flash",
    "gemini-1.5-pro",
    "gemini-1.0-pro"
];

// Define Mistral models for secondary fallback if all Gemini models fail
const mistralModels = [
    "mistral-small-latest",
    "mistral-large-latest",
    "open-mistral-nemo"
];

/**
 * Attempts to generate a response using Gemini models sequentially.
 * If all Gemini models fail, it falls back to Mistral models.
 * @param {string} systemPrompt - The system instructions for the AI
 * @param {string} userPrompt - The specific prompt from the user
 * @param {Array} history - Previous chat history context
 * @returns {string} The AI generated text response
 */
async function generateAiResponse(systemPrompt, userPrompt, history = []) {
    let lastError = null;

    // 1. Try Gemini API First
    for (const model of geminiModels) {
        try {
            // Construct the endpoint URL for the specific Gemini model
            const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${GEMINI_API_KEY}`;
            
            const contents = [];
            
            // Add system prompt if it exists, formatting it as a user message that the AI silently acknowledges
            if (systemPrompt) {
                contents.push({
                    role: "user",
                    parts: [{ text: systemPrompt + "\n(Acknowledge this context silently. Do not reply to it, just use it for the subsequent conversation.)" }]
                });
                contents.push({
                    role: "model",
                    parts: [{ text: "Understood. I will use this context to assist you. How can I help?" }]
                });
            }

            // Append chat history to the context
            for (const msg of history) {
                contents.push({
                    role: msg.role === 'model' ? 'model' : 'user',
                    parts: [{ text: msg.text }]
                });
            }

            // Add the current user prompt to the end of the conversation
            if (userPrompt) {
                contents.push({
                    role: "user",
                    parts: [{ text: userPrompt }]
                });
            }

            // Make the POST request to the Gemini API
            const response = await axios.post(url, { contents }, {
                headers: { 'Content-Type': 'application/json' },
                validateStatus: false // Prevent Axios from throwing on 4xx/5xx to allow fallback handling
            });

            // Check if the response was successful and contains text
            if (response.status === 200 && response.data.candidates?.[0]?.content?.parts?.[0]?.text) {
                console.log(`Successfully generated response using Gemini model: ${model}`);
                return response.data.candidates[0].content.parts[0].text;
            }

            // If rate limited or service unavailable, continue to the next model
            if (response.status === 503 || response.status === 429) {
                lastError = new Error(`Gemini ${model} failed with status ${response.status}`);
                continue;
            }
            
            // If any other error occurred, log and continue to next model
            if (response.status >= 400) {
                 lastError = new Error(`Gemini ${model} failed with status ${response.status}: ${JSON.stringify(response.data)}`);
                 continue;
            }

        } catch (err) {
            // Catch network errors and continue to next model
            lastError = err;
            console.error(`Gemini model ${model} failed:`, err.message);
        }
    }

    // If we reach this point, all Gemini models failed
    console.warn("All Gemini models failed. Switching to Mistral Fallback...");

    // 2. Fallback to Mistral API
    for (const model of mistralModels) {
        try {
            // Construct the endpoint URL for Mistral chat completions
            const url = 'https://api.mistral.ai/v1/chat/completions';
            const messages = [];

            // Add system prompt if it exists
            if (systemPrompt) {
                messages.push({ role: 'system', content: systemPrompt });
            }

            // Append chat history
            for (const msg of history) {
                messages.push({
                    role: msg.role === 'model' ? 'assistant' : 'user',
                    content: msg.text
                });
            }

            // Add the current user prompt
            if (userPrompt) {
                messages.push({ role: 'user', content: userPrompt });
            }

            // Make the POST request to the Mistral API
            const response = await axios.post(url, {
                model: model,
                messages: messages
            }, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${MISTRAL_API_KEY}`
                },
                validateStatus: false // Prevent Axios from throwing on error
            });

            // Check if the response was successful and contains text
            if (response.status === 200 && response.data.choices?.[0]?.message?.content) {
                console.log(`Successfully generated response using Mistral model: ${model}`);
                return response.data.choices[0].message.content;
            }

            // If rate limited, continue to the next Mistral model
            if (response.status === 503 || response.status === 429) {
                lastError = new Error(`Mistral ${model} failed with status ${response.status}`);
                continue;
            }

            // If any other error occurred, log and continue
            if (response.status >= 400) {
                 lastError = new Error(`Mistral ${model} failed with status ${response.status}: ${JSON.stringify(response.data)}`);
                 continue;
            }

        } catch (err) {
            // Catch network errors and continue
            lastError = err;
            console.error(`Mistral model ${model} failed:`, err.message);
        }
    }

    // If we reach this point, ALL models (Gemini and Mistral) failed
    throw new Error("All AI models (Gemini & Mistral) are currently unreachable. " + (lastError ? lastError.message : ""));
}

// Export the service functions
module.exports = {
    generateAiResponse
};
