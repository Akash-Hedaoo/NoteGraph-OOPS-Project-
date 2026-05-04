# Part 3: Application Layout & Navigation Flow

**Files Discussed:**
- `JavaUI/src/com/notegraph/ui/layout/SidebarPanel.java`
- `JavaUI/src/com/notegraph/ui/layout/TopBarPanel.java`

Professor, the third division covers the spatial navigation controls. Let me explain the procedural logic governing how the user moves through the app.

In **`SidebarPanel.java`**, the layout logic sets a `BoxLayout` locked to the Y-axis. This simply takes every element I add and stacks them vertically top-to-bottom. However, standard Java `JButton`s look bulky and out of place in modern apps. Instead, my logic implements custom navigation items using generic `JPanel`s. 

I attached a custom `MouseAdapter` to each panel. The logic here monitors for events. `mouseEntered()` triggers my code to execute `.setBackground()` to a slight gray hover color and calls `.repaint()` to instantly flush the color buffer to the screen. `mouseExited()` returns it to the default transparent state. When `mouseClicked()` fires, the logic records the clicked page ID into an `activePage` variable, highlights that specific panel permanently in blue, and triggers the `onNavigate.accept()` callback we discussed in Part 1 to swap visually to that page.

A major technical challenge in the sidebar was loading the Workspace dropdown. If I simply called the `ApiClient` to get workspaces inside the UI construction code, the logic would block the Event Dispatch Thread (EDT) while waiting for the server, completely freezing the app for several seconds. To solve this, my logic instantiates a `SwingWorker`. Inside its `doInBackground()` method, the thread leaves the UI environment, crosses over the network, grabs the JSON array of workspaces, parses them, and returns them. The logic then moves into `done()`, which safely merges back onto the UI thread and updates the `workspaceLabel.setText()` without ever causing a stutter.

In **`TopBarPanel.java`**, the structural logic leverages a `BorderLayout` to shove the search bar all the way to `BorderLayout.WEST` and the avatar to `BorderLayout.EAST`. The logic here relies heavily on `BorderFactory.createCompoundBorder()`. In CSS, spacing elements is easy with margins and padding. In Java Swing, to replicate a bottom divider line with 16 pixels of empty padding above and below it, my logic compounds a `MatteBorder(0,0,1,0)` object with an `EmptyBorder(16, 24, 16, 24)` object. This mathematical boundary encapsulates the components inside, forcing the exact pixel-perfect spacing required by our aesthetic design.
