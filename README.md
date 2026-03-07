# NoteGraph (SmartNotes)

## Project Title & Overview
A full-stack, Single Page Application (SPA) note-taking tool featuring a physics-based interactive graph view, built with Java Swing and Spring Boot.

## Tech Stack
- **Backend:** Java, Spring Boot, Spring Data JPA (Hibernate), Spring Security, MySQL.
- **Frontend:** Java Swing, FlatLaf (Dynamic Dark/Light Themes), iText (PDF Export generation).
- **Build Tool:** Maven (handles all dependencies automatically).

## Key Features
- Multi-tenant user isolation
- Soft-deletion (Trash bin)
- Force-directed physics Graph View
- Auto-login via Java Preferences
- Obsidian-style context menu for note management

## Local Setup & Installation

### Step 1: Database Setup
Open MySQL Workbench and run the `database-setup.sql` script to create the `notegraph_db` database.

### Step 2: Backend Configuration
Navigate to `backend/src/main/resources/application.properties` and update the `spring.datasource.username` and `spring.datasource.password` to match your local MySQL credentials.

### Step 3: Run Backend
Run the Spring Boot application first so Hibernate can auto-generate all the database tables.

### Step 4: Run Frontend
Run `Main.java` in the frontend workspace. Maven will automatically download all required UI libraries.

## Next Steps for Development (Phase 2)
- Building the UI for Note Version History
- Implementing Markdown parsing (Reading/Source modes)
- Wiring up the Folders/Favorites sidebars
