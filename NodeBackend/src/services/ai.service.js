const axios = require('axios');
const prisma = require('../config/db');
const { uuidToBuffer, bufferToUuid } = require('../config/uuid');

const GEMINI_API_KEY = process.env.GEMINI_API_KEY;
const MISTRAL_API_KEY = process.env.MISTRAL_API_KEY;

const geminiModels = [
    "gemini-1.5-flash",
    "gemini-2.5-flash",
    "gemini-1.5-pro",
    "gemini-1.0-pro"
];

const mistralModels = [
    "mistral-small-latest",
    "mistral-large-latest",
    "open-mistral-nemo"
];

/**
 * Attempts to generate a response using Gemini models sequentially.
 * If all fail, falls back to Mistral models.
 */
async function generateAiResponse(systemPrompt, userPrompt, history = []) {
    let lastError = null;

    // 1. Try Gemini API
    for (const model of geminiModels) {
        try {
            const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${GEMINI_API_KEY}`;
            
            const contents = [];
            
            // Add system prompt if exists
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

            // Add history
            for (const msg of history) {
                contents.push({
                    role: msg.role === 'model' ? 'model' : 'user',
                    parts: [{ text: msg.text }]
                });
            }

            // Add current user prompt
            if (userPrompt) {
                contents.push({
                    role: "user",
                    parts: [{ text: userPrompt }]
                });
            }

            const response = await axios.post(url, { contents }, {
                headers: { 'Content-Type': 'application/json' },
                validateStatus: false
            });

            if (response.status === 200 && response.data.candidates?.[0]?.content?.parts?.[0]?.text) {
                console.log(`Successfully generated response using Gemini model: ${model}`);
                return response.data.candidates[0].content.parts[0].text;
            }

            if (response.status === 503 || response.status === 429) {
                // Rate limit or unavailable, try next Gemini model
                lastError = new Error(`Gemini ${model} failed with status ${response.status}`);
                continue;
            }
            
            if (response.status >= 400) {
                 lastError = new Error(`Gemini ${model} failed with status ${response.status}: ${JSON.stringify(response.data)}`);
                 continue;
            }

        } catch (err) {
            lastError = err;
            console.error(`Gemini model ${model} failed:`, err.message);
        }
    }

    console.warn("All Gemini models failed. Switching to Mistral Fallback...");

    // 2. Fallback to Mistral API
    for (const model of mistralModels) {
        try {
            const url = 'https://api.mistral.ai/v1/chat/completions';
            const messages = [];

            if (systemPrompt) {
                messages.push({ role: 'system', content: systemPrompt });
            }

            for (const msg of history) {
                messages.push({
                    role: msg.role === 'model' ? 'assistant' : 'user',
                    content: msg.text
                });
            }

            if (userPrompt) {
                messages.push({ role: 'user', content: userPrompt });
            }

            const response = await axios.post(url, {
                model: model,
                messages: messages
            }, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${MISTRAL_API_KEY}`
                },
                validateStatus: false
            });

            if (response.status === 200 && response.data.choices?.[0]?.message?.content) {
                console.log(`Successfully generated response using Mistral model: ${model}`);
                return response.data.choices[0].message.content;
            }

            if (response.status === 503 || response.status === 429) {
                lastError = new Error(`Mistral ${model} failed with status ${response.status}`);
                continue;
            }

            if (response.status >= 400) {
                 lastError = new Error(`Mistral ${model} failed with status ${response.status}: ${JSON.stringify(response.data)}`);
                 continue;
            }

        } catch (err) {
            lastError = err;
            console.error(`Mistral model ${model} failed:`, err.message);
        }
    }

    throw new Error("All AI models (Gemini & Mistral) are currently unreachable. " + (lastError ? lastError.message : ""));
}

module.exports = {
    generateAiResponse
};
