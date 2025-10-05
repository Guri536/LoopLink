### `ui/MainScreen.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/ui/MainScreen.kt`

**Description:**

This file is responsible for rendering the primary user interface that users see after logging in. Its key responsibility is to provide a responsive layout that adapts to different screen sizes, specifically distinguishing between a wide-screen (desktop) and a narrow-screen (mobile) experience. It acts as a central hub, orchestrating the display of the chat list (sidebar) and the chat content area. It leverages the `ViewModel` to manage the list of chat rooms and uses the `Voyager` navigation library to handle all navigation logic, including the dual-pane layout on desktop and the screen-stacking navigation on mobile.

**Data Classes & Objects:**

- **`TopTab(id: String, label: String)`**: A simple data class, currently unused, likely intended for a tab-based UI structure.

- **`MainScreen : Screen`**: This is the main entry point for this UI, defined as a `Screen` from the Voyager library. It represents a destination in the navigation graph.

- **`EmptyChatTab : Tab`**: A Voyager `Tab` object that represents the default state in the desktop layout when no chat is selected. It simply displays the `EmptyChatPlaceholder` composable.

- **`ChatTab(val room: RoomItem) : Tab`**: A Voyager `Tab` used in the desktop layout. Each instance represents a specific chat room. When this tab is active, its `Content` (the `ChatAppWithScaffold`) is displayed in the main content area.

- **`ChatTabScreen(val room: RoomItem) : Screen`**: A Voyager `Screen` used for mobile navigation. When a user taps a chat room, the app navigates to this screen, pushing it onto the navigation stack and displaying the chat UI full-screen.

**Composable Functions:**

- **`MainScreen.Content()`**

  The primary composable function that builds the screen's UI. Its logic is centered around adapting the layout based on the platform.

  - **Functionality:**
    - **ViewModel Integration:** It obtains an instance of `ChatViewModel` to access and manage the list of chat rooms. It also pre-populates the list with a "Self" chat room and defines a lambda (`addRoom`) for adding new rooms.
    - **Responsive Layout:** It calls `getPlatformType()` to determine if it's running on a `DESKTOP` or `ANDROID` platform.
    - **Desktop Layout (isWideScreen = true):**
        - It sets up a `Row` to create a two-pane layout.
        - It initializes a `TabNavigator` (from Voyager) which manages the content of the main (right) pane. The `EmptyChatTab` is set as the default.
        - It places the `InitiateSideBar` in the left pane, occupying 15% of the width.
        - It places the `CurrentTab()` composable in the right pane, which dynamically displays the content of the currently selected Voyager `Tab`.
        - It uses `CompositionLocalProvider` to make the `tabNavigator` available to child composables, allowing the sidebar to change the active tab.
    - **Mobile Layout (isWideScreen = false):**
        - It displays only the `InitiateSideBar`, which is configured to take up the full screen width.
        - Navigation to individual chat screens is handled by pushing new `Screen`s onto the stack, not by switching tabs.

- **`InitiateSideBar(isWideScreen: Boolean, rooms: List<RoomItem>, onIconClick: () -> Unit)`**

  This composable acts as a bridge, connecting the top-level navigation logic with the `Sidebar` UI component.

  - **Parameters:**
    - `isWideScreen: Boolean`: A flag indicating if the wide-screen layout should be used.
    - `rooms: List<RoomItem>`: The list of chat rooms to be displayed.
    - `onIconClick: () -> Unit`: A callback function to handle clicks on the "Add Chat" icon.

  - **Functionality:**
    - **Navigation Logic:** It defines the `onRoomClick` lambda. This is the core of the adaptive navigation. On a wide screen, it changes the active tab in the `TabNavigator` (`tabNavigator?.current = ChatTab(room)`). On a narrow screen, it pushes a new `ChatTabScreen` onto the navigator's stack (`navigator?.push(ChatTabScreen(room))`).
    - **Component Inflation:** It calls the main `Sidebar` composable, passing down the prepared data (rooms) and navigation callbacks (`onRoomClick`, `onSettingsClick`, etc.).

- **`Sidebar(modifier: Modifier, rooms: List<RoomItem>, onRoomClick: (RoomItem) -> Unit, onProfileClick: () -> Unit, onSettingsClick: () -> Unit, onIconClick: () -> Unit)`**

  This is the main presentational composable for the sidebar UI.

  - **Parameters:**
    - `modifier: Modifier`: The modifier to be applied to the sidebar's root `Column`.
    - `rooms: List<RoomItem>`: The list of chat rooms to display.
    - `onRoomClick: (RoomItem) -> Unit`: Callback invoked when a chat room is clicked.
    - `onProfileClick: () -> Unit`: Callback for when the "Profile" button is clicked.
    - `onSettingsClick: () -> Unit`: Callback for when the "Settings" button is clicked.
    - `onIconClick: () -> Unit`: Callback for the "Add Chat" FAB item.

  - **Functionality:**
    - **Layout:** It uses a `Scaffold` to easily place a `MultiFloatingActionButton` and a `BottomBar`. The main content area contains the chat list.
    - **Chat List:** It displays a `LazyColumn` of chat rooms using the `SidebarRoomItem` composable for each item.
    - **Actions:** It integrates the `MultiFloatingActionButton` for adding new chats and groups, and the `BottomBar` for accessing Profile and Settings.

- **`SidebarRoomItem(room: RoomItem, onClick: () -> Unit)`**

  A simple composable that renders a single row in the chat list.

  - **Parameters:**
    - `room: RoomItem`: The data for the chat room to render.
    - `onClick: () -> Unit`: The callback to invoke when the item is clicked.

  - **Functionality:**
    - Displays the room's name and an icon derived from its label.
    - Shows the number of unread messages if it's greater than zero.
    - Has a `clickable` modifier to trigger the `onClick` navigation.

- **`BottomBar(onProfileClick: () -> Unit, onSettingsClick: () -> Unit)`**

  Renders the "Profile" and "Settings" buttons at the bottom of the sidebar.

  - **Parameters:**
    - `onProfileClick: () -> Unit`: Callback for the "Profile" button.
    - `onSettingsClick: () -> Unit`: Callback for the "Settings" button.

- **`EmptyChatPlaceholder()`**

  A simple UI shown in the main content area on desktop before any chat has been selected, prompting the user to select a chat.
