// Import Express framework to create route handlers
const express = require('express');
const router = express.Router();

// Import the authentication controller containing logic for user registration and login
const authController = require('../controllers/auth.controller');

// Route for registering a new user account
router.post('/register', authController.register);

// Route for authenticating an existing user and obtaining a JWT
router.post('/login', authController.login);

// Export the router to be used in the main application
module.exports = router;
