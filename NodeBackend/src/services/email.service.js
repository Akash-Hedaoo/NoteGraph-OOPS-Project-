// Import the nodemailer library for sending emails
const nodemailer = require('nodemailer');

// Configure the email transporter using Gmail's SMTP settings
const transporter = nodemailer.createTransport({
    host: 'smtp.gmail.com',
    port: 587,
    secure: false, // Set to true for port 465, false for 587 or other ports
    auth: {
        user: process.env.SMTP_EMAIL, // Authenticate using SMTP email from environment variables
        pass: process.env.SMTP_PASSWORD // Authenticate using SMTP password from environment variables
    }
});

// Function to send a revision reminder email to the user
exports.sendRevisionReminder = async (toEmail, noteTitle, reason) => {
    try {
        // Send the email using the configured transporter
        const info = await transporter.sendMail({
            from: `"NoteGraph Assistant" <${process.env.SMTP_EMAIL}>`, // Sender display name and address
            to: toEmail, // Recipient email address
            subject: `Time to review: ${noteTitle}`, // Subject line for the email
            html: `
                <div style="font-family: sans-serif; padding: 20px; color: #333;">
                    <h2>Spaced Repetition Reminder 🧠</h2>
                    <p>It's time to review your note: <strong>${noteTitle}</strong></p>
                    <div style="background: #f3f4f6; padding: 15px; border-left: 4px solid #6366f1; margin: 15px 0;">
                        <p style="margin: 0;"><strong>AI Scheduling Reason:</strong></p>
                        <p style="margin: 5px 0 0 0;">${reason}</p>
                    </div>
                    <p>Reviewing this now will help cement it in your long-term memory!</p>
                    <a href="http://localhost:5173" style="display: inline-block; background: #6366f1; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Open NoteGraph</a>
                </div>
            `
        });
        
        // Log success message upon sending the email
        console.log(`Reminder email sent to ${toEmail} for note '${noteTitle}'`);
        return true;
    } catch (err) {
        // Log error and return false if the email could not be sent
        console.error("Failed to send email reminder:", err);
        return false;
    }
};
