# Part 4: Core Functional Views (The Business Logic)

**Files Discussed:**
- `JavaUI/src/com/notegraph/ui/pages/DashboardPanel.java`
- `JavaUI/src/com/notegraph/ui/pages/EditorPanel.java`
- `JavaUI/src/com/notegraph/ui/pages/NoteHierarchyPanel.java`
- `JavaUI/src/com/notegraph/ui/pages/TagsPanel.java`
- `JavaUI/src/com/notegraph/ui/pages/FavoritesPanel.java`
- `JavaUI/src/com/notegraph/ui/auth/LoginPanel.java`

Professor, the logic defining the functional business views, like the editor and the graph, is where the bulk of the application's complexity resides. Let's look at the algorithms driving them.

In **`EditorPanel.java`**, I used a `JTextPane` bound to an `HTMLEditorKit` to construct the text-entry constraint. The logic required here is auto-saving. If my logic sent an HTTP PUT on every single keystroke, the backend would crash from the traffic, and the UI would freeze while typing. Instead, I bound a `DocumentListener` to the `JTextPane`. This listener fires on `insertUpdate` (typing) and `removeUpdate` (backspacing). 

My debounce logic leverages a `javax.swing.Timer`. I instantiated the timer at 1,500 milliseconds. Every single time the `DocumentListener` fires, my logic calls `saveTimer.restart()`. The timer resets back to zero instantly. *Only* when the user stops typing for a full 1.5 seconds does the timer actually trigger its `actionPerformed` event. Inside that event, the logic pulls the HTML string from the `JTextPane` and wraps it in a asynchronous `SwingWorker` thread, firing the `ApiClient.updateNote()` method silently without ever blocking the user from resuming typing.

In **`NoteHierarchyPanel.java`**, the logical objective is to illustrate connections. Instead of using standard buttons, my logic overrides `paintComponent(Graphics g)`. When the JVM asks this panel to draw itself, my logic takes over. I cast to `Graphics2D` and turn on `RenderingHints.KEY_ANTIALIASING` so diagonal math lines render smoothly. 

The algorithmic logic dynamically computes `branches.size()`, allocating pixel offsets horizontally based on $W = 200 + \text{GAP}$. It establishes the center screen coordinates $X$ and $Y$. Then, looping through each `GraphBranch` object returned from the API, the logic draws the root workspace, pulls a vertical stroke down via `g2.drawLine()`, drops horizontal strokes scaling perfectly with the loop counter across all tags, and then drops standard rounded rectangle nodes mapped to tag colors. For interactivity, my logic attached a `MouseListener`. On `.mouseClicked()`, the logic maps the $X, Y$ coordinate clicked on the canvas against the mathematical bounds arrays I plotted for each rendered node, accurately identifying which specific tag or note card the user clicked on to navigate them properly.

In **`DashboardPanel.java`**, **`TagsPanel.java`**, and **`FavoritesPanel.java`**, the logic uses `GridLayout` and `FlowLayout`. `GridLayout` is particularly powerful for metric cards. I tell the layout, "create a grid with 1 row and 4 columns," and the internal Swing logic automatically stretches the cards horizontally to perfectly partition the window regardless of how the user resizes the OS frame. Like the sidebar, the data logic exclusively utilizes `SwingWorker.doInBackground()` algorithms to query the REST endpoints over sockets, parsing JSON arrays, looping over the results to instantiate `JPanel` UI cards, and finally executing standard Swing `.removeAll()` and `.revalidate()` sequences strictly on the UI thread when complete.

Finally, in **`LoginPanel.java`**, the logic operates on a `GridBagLayout` schema. This is a coordinate plane definition. Every single email field, password field, and button is bound to an `GridBagConstraints(gbc)` object. I iteratively mutate `gbc.gridy` to slide items down rows, and I adjust `gbc.insets = new Insets(top, left, bottom, right)` to exactly pinpoint the spacing. The toggle algorithm is elegantly simple: a boolean flag `isLogin` is mutated upon clicking "Sign Up". The logic checks this flag—if false, it simply executes `nameRow.setVisible(true)` and modifies the `submitBtn.setText()` strings without ever tearing down the surrounding layout.
