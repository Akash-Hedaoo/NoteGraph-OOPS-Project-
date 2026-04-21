# Part 1: Core Architecture & Application Shell

**Files Discussed:**
- `JavaUI/src/com/notegraph/ui/NoteGraphApp.java`
- `JavaUI/src/com/notegraph/ui/layout/MainFrame.java`

Professor, if we look at the foundation of the frontend, the core architecture is managed by two main files.

In **`NoteGraphApp.java`**, this is where the application execution begins. The very first piece of logic executed is setting up the visual theme. Standard Java Swing looks heavily outdated, so my logic attempts to load `FlatLightLaf` via `UIManager.setLookAndFeel()`. This completely replaces Swing's default drawing algorithms with a modern, flat UI. 

Next, the logic wraps the UI creation inside `SwingUtilities.invokeLater()`. This is absolutely crucial because Swing components are not thread-safe. If I instantiate the window on the main thread, the application could face deadlock conditions. Inside this safe thread, my logic creates the primary `JFrame` and sets its boundaries. 

The most important architectural logic here is the root `CardLayout`. Instead of opening a window for the Login screen, closing it, and then instantiating a brand new window for the Application, I instantiated a `CardLayout` panel. The logic creates a `showMainApp` method containing a runnable callback. I pass this callback into `LoginPanel`. When the user successfully authenticates, the login logic triggers this callback, telling `NoteGraphApp` to simply flip the "card" from the login view to the authenticated `MainFrame` view. This happens in constant time without destroying the window frame.

Moving to **`MainFrame.java`**, this is the authenticated application shell. The layout logic here relies on a `BorderLayout`. I place the `SidebarPanel` permanently on the West boundary and the `TopBarPanel` on the North layout boundary. This anchors them permanently to the screen. 

The Center region houses a secondary `CardLayout` panel. This is where the magic of our navigation happens. I instantiated the Dashboard, Editor, and Hierarchy panels and added them to this center card deck. I wrote a centralized `navigateTo(String pageName)` method. When the user clicks a button on the sidebar, the sidebar calls this method. The logic inside `navigateTo` does two things: first, it calls `cards.show()` to instantly swap the visible component in the center of the screen to match the user's choice. Second, and highly important, it executes a `refreshData()` method on that newly visible panel. This guarantees that whenever the user navigates to a new view, the logic forces that view to immediately fetch fresh data from the backend, keeping the UI state perfectly synchronized with the database.
