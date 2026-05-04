# NoteGraph: Java Swing Frontend Architecture

This document outlines the architectural division of the NoteGraph Java Swing desktop client. The frontend has been designed to strictly separate concerns, ensuring that UI rendering, API communication, and state management remain distinct and maintainable. 

The codebase is logically structured into five primary domains.

---

### Part 1: Core Architecture & Application Shell
**Purpose:** Handles the application lifecycle, look-and-feel initialization, and the primary window layout structure that acts as the container for all other views.

*   **`JavaUI/src/com/notegraph/ui/NoteGraphApp.java`**
    *   **Role:** The main entry point `public static void main()`. Initializes the JVM, sets up the FlatLaf modern styling theme, and manages the high-level authentication state transition (swapping between the Login view and the Main App view).
*   **`JavaUI/src/com/notegraph/ui/layout/MainFrame.java`**
    *   **Role:** The core structural template of the authenticated app. It implements a `CardLayout` to seamlessly swap out the central content area (Dashboard, Editor, Graph, etc.) without destroying the surrounding navigation elements.

---

### Part 2: API & Data Access Layer
**Purpose:** Serves as the bridge between the Java UI and the Spring Boot REST backend. It manages network requests, JSON serialization/deserialization, and authentication tokens.

*   **`JavaUI/src/com/notegraph/ui/api/ApiClient.java`**
    *   **Role:** A Singleton class utilizing Java's built-in `HttpClient`. It manages all external communication, including injecting the JWT `Authorization` header into requests, handling network timeouts, and forwarding generic HTTP statuses to the UI.
*   **`JavaUI/src/com/notegraph/ui/api/ApiModels.java`**
    *   **Role:** Contains all Data Transfer Objects (DTOs) and Plain Old Java Objects (POJOs). These define the exact schema of JSON payloads (e.g., `Note`, `GraphBranch`, `AuthResponse`), allowing Google Gson to map them cleanly into strongly-typed Java objects.

---

### Part 3: Application Layout & Navigation Flow
**Purpose:** Defines the persistent navigation elements that surround the primary content areas, allowing users to traverse the workspace hierarchy and different modules.

*   **`JavaUI/src/com/notegraph/ui/layout/SidebarPanel.java`**
    *   **Role:** The primary navigation column. It maintains the state of the active page, fetches and displays the interactive Workspace selector, and provides visual feedback for navigation choices using custom-painted hover and active states.
*   **`JavaUI/src/com/notegraph/ui/layout/TopBarPanel.java`**
    *   **Role:** The persistent search and profile header. Currently acts as a visual placeholder and functional shell for global search operations.

---

### Part 4: Core Functional Views (The Business Logic)
**Purpose:** The heavy-lifting components where the actual application features live. These represent the specific pages the user interacts with.

*   **`JavaUI/src/com/notegraph/ui/pages/DashboardPanel.java`**
    *   **Role:** Aggregates data to present a high-level overview. Connects multiple data streams (recent notes, activity logs, workspace statistics) into a single cohesive UI.
*   **`JavaUI/src/com/notegraph/ui/pages/EditorPanel.java`**
    *   **Role:** A complex rich-text implementation built over `JTextPane`. Includes a `SwingWorker`-based auto-save mechanism that quietly synchronizes changes with the backend without freezing the UI thread.
*   **`JavaUI/src/com/notegraph/ui/pages/NoteHierarchyPanel.java`**
    *   **Role:** The most visually complex component. It features a custom `paintComponent` implementation to render a dynamic, interactive organizational chart (`Graphics2D` lines, nodes, bounds checking for clicks), plus a secondary traditional `JTree` view mode.
*   **`JavaUI/src/com/notegraph/ui/pages/TagsPanel.java`** & **`FavoritesPanel.java`**
    *   **Role:** Grid-based management interfaces for entity lists. They implement localized filtering logic natively in the UI to minimize unnecessary API calls during simple text searches.
*   **`JavaUI/src/com/notegraph/ui/auth/LoginPanel.java`**
    *   **Role:** Handles user authentication and registration workflows, verifying credentials before authorizing transition to the `MainFrame`.

---

### Part 5: Reusable UI Kit & Design System
**Purpose:** Eliminates code duplication and enforces visual consistency. Rather than styling every button individually, the app relies on standardized custom components.

*   **`JavaUI/src/com/notegraph/ui/components/ColorScheme.java`**
    *   **Role:** The central registry for all visual constants (Colors, Fonts, Paddings). By storing these here, a developer can change the entire application's theme in one file.
*   **`JavaUI/src/com/notegraph/ui/components/RoundedPanel.java`**
    *   **Role:** A custom `JPanel` extension that overrides the default rectangular painting to produce anti-aliased, rounded-corner cards (a critical element of modern UI design).
*   **`JavaUI/src/com/notegraph/ui/components/PlaceholderTextField.java`** & **`ConfirmDialog.java`**
    *   **Role:** Standardized, reusable inputs and dialog modals that wrap baseline Swing components with specific event listeners and visual styling.
