const nodemailer = require('nodemailer');

const transporter = nodemailer.createTransport({
    host: 'smtp.gmail.com',
    port: 587,
    secure: false, // true for 465, false for other ports
    auth: {
        user: process.env.SMTP_EMAIL,
        pass: process.env.SMTP_PASSWORD
    }
});

exports.sendRevisionReminder = async (toEmail, noteTitle, reason) => {
    try {
        const info = await transporter.sendMail({
            from: `"NoteGraph Assistant" <${process.env.SMTP_EMAIL}>`,
            to: toEmail,
            subject: `Time to review: ${noteTitle}`,
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
        console.log(`Reminder email sent to ${toEmail} for note '${noteTitle}'`);
        return true;
    } catch (err) {
        console.error("Failed to send email reminder:", err);
        return false;
    }
};
