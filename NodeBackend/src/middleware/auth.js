// Import the jsonwebtoken library for handling JWTs
const jwt = require('jsonwebtoken');

// Middleware to authenticate incoming requests via JWT
const authenticateToken = (req, res, next) => {
    // Extract the Authorization header
    const authHeader = req.headers['authorization'];
    // Extract the token part assuming format "Bearer TOKEN"
    const token = authHeader && authHeader.split(' ')[1]; 

    // If no token is found, return a 401 Unauthorized response
    if (!token) {
        return res.status(401).json({ error: 'Access denied. No token provided.' });
    }

    try {
        // Verify the token using the secret key
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        
        // Attach the decoded user payload (id and email) to the request object
        req.user = decoded; 
        
        // Proceed to the next middleware or route handler
        next();
    } catch (err) {
        // If the token is invalid or expired, return a 403 Forbidden response
        return res.status(403).json({ error: 'Invalid or expired token.' });
    }
};

// Export the middleware for use in securing routes
module.exports = authenticateToken;
