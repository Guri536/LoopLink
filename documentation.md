# LoopLink Project Documentation

This document provides a detailed overview of the LoopLink project, including its architecture, components, and implementation details.

**Last Updated:** 2024-07-27
<!-- TOC -->
* [LoopLink Project Documentation](#looplink-project-documentation)
  * [`commonMain`](#commonmain)
    * [`App.kt`](#appkt)
    * [`Greeting.kt`](#greetingkt)
    * [`Platform.kt`](#platformkt)
    * [`ui/theme/Theme.kt`](#uithemethemekt)
    * [`ui/MainScreen.kt`](#uimainscreenkt)
    * [`ui/ChatScreen.kt`](#uichatscreenkt)
    * [`theme/colors.kt`](#themecolorskt)
    * [`network/KtorClientFactory.kt`](#networkktorclientfactorykt)
    * [`network/KtorServerFactory.kt`](#networkktorserverfactorykt)
    * [`network/discovery/ServiceInfo.kt`](#networkdiscoveryserviceinfokt)
    * [`network/discovery/LANServiceDiscovery.kt`](#networkdiscoverylanservicediscoverykt)
    * [`errors/errors.kt`](#errorserrorskt)
    * [`secrets/Secrets.kt`](#secretssecretskt)
    * [`viewmodel/PeerDiscoveryViewModel.kt`](#viewmodelpeerdiscoveryviewmodelkt)
    * [`viewmodel/ChatViewModel.kt`](#viewmodelchatviewmodelkt)
    * [`webDriver/cuimsAPI.kt`](#webdrivercuimsapikt)
    * [`components/ProfileCard.kt`](#componentsprofilecardkt)
    * [`components/textField.kt`](#componentstextfieldkt)
    * [`components/customTextField.kt`](#componentscustomtextfieldkt)
    * [`components/GetProfileImage.kt`](#componentsgetprofileimagekt)
    * [`components/LocalCompositions.kt`](#componentslocalcompositionskt)
    * [`components/loginAPIComposable.kt`](#componentsloginapicomposablekt)
    * [`components/chat/Data.kt`](#componentschatdatakt)
    * [`components/chat/Store.kt`](#componentschatstorekt)
    * [`components/chat/Colors.kt`](#componentschatcolorskt)
    * [`components/chat/ChatApp.kt`](#componentschatchatappkt)
    * [`components/chat/Reducer.kt`](#componentschatreducerkt)
    * [`components/chat/Messages.kt`](#componentschatmessageskt)
    * [`components/chat/ChatMessage.kt`](#componentschatchatmessagekt)
    * [`components/chat/SendMessage.kt`](#componentschatsendmessagekt)
    * [`components/chat/EmojiFactory.kt`](#componentschatemojifactorykt)
    * [`components/chat/EmojiBackground.kt`](#componentschatemojibackgroundkt)
    * [`components/chat/currentTime.common.kt`](#componentschatcurrenttimecommonkt)
    * [`components/fabButtons/FabButtonSub.kt`](#componentsfabbuttonsfabbuttonsubkt)
    * [`components/fabButtons/FabButtonState.kt`](#componentsfabbuttonsfabbuttonstatekt)
    * [`components/fabButtons/FabButtonMain.kt`](#componentsfabbuttonsfabbuttonmainkt)
    * [`components/fabButtons/FabButtonItem.kt`](#componentsfabbuttonsfabbuttonitemkt)
    * [`components/fabButtons/ExpandedFabButton.kt`](#componentsfabbuttonsexpandedfabbuttonkt)
    * [`operations/Logout.kt`](#operationslogoutkt)
    * [`operations/InsertUserDataFromProfile.kt`](#operationsinsertuserdatafromprofilekt)
  * [`androidMain`](#androidmain)
    * [`MainActivity.kt`](#mainactivitykt)
    * [`Platform.android.kt`](#platformandroidkt)
    * [`network/AndroidKtorServer.kt`](#networkandroidktorserverkt)
    * [`network/KtorClientFactory.android.kt`](#networkktorclientfactoryandroidkt)
    * [`network/KtorServerFactory.android.kt`](#networkktorserverfactoryandroidkt)
    * [`network/discovery/LANServiceDiscovery.android.kt`](#networkdiscoverylanservicediscoveryandroidkt)
    * [`secrets/Secrets.android.kt`](#secretssecretsandroidkt)
    * [`webDriver/cuimsAPI.android.kt`](#webdrivercuimsapiandroidkt)
    * [`components/GetProfileImage.android.kt`](#componentsgetprofileimageandroidkt)
    * [`components/chat/Messages.android.kt`](#componentschatmessagesandroidkt)
    * [`components/chat/EmojiBackground.android.kt`](#componentschatemojibackgroundandroidkt)
  * [`jvmMain`](#jvmmain)
    * [`main.kt`](#mainkt)
    * [`Platform.jvm.kt`](#platformjvmkt)
    * [`network/KtorClientFactory.jvm.kt`](#networkktorclientfactoryjvmkt)
    * [`network/KtorServerFactory.jvm.kt`](#networkktorserverfactoryjvmkt)
    * [`network/JvmKtorServerRunner.kt`](#networkjvmktorserverrunnerkt)
    * [`network/discovery/LANServiceDiscovery.jvm.kt`](#networkdiscoverylanservicediscoveryjvmkt)
    * [`secrets/Secrets.jvm.kt`](#secretssecretsjvmkt)
    * [`webDriver/cuimsAPI.jvm.kt`](#webdrivercuimsapijvmkt)
    * [`components/GetProfileImage.jvm.kt`](#componentsgetprofileimagejvmkt)
    * [`components/chat/Messages.jvm.kt`](#componentschatmessagesjvmkt)
    * [`components/chat/EmojiFactory.jvm.kt`](#componentschatemojifactoryjvmkt)
    * [`components/chat/EmojiBackground_jvm.kt`](#componentschatemojibackground_jvmkt)
    * [`ui/theme/Type.jvm.kt`](#uithemetypejvmkt)
  * [`LLData.sq`](#lldatasq)
  * [`build.gradle.kts`](#buildgradlekts)
  * [`libs.versions.toml`](#libsversionstoml)
<!-- TOC -->

## `commonMain`

### `App.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/App.kt`

**Description:**

This file serves as the main entry point for the application's user interface. It is responsible for setting up the overall theme, providing essential dependencies to the rest of the app via `CompositionLocalProvider`, and determining the initial screen to display. If the local database is empty, it directs the user to the login screen; otherwise, it displays the main application screen.

**Functions:**

- **`App(database: DatabaseMng, cuimsAPI: cuimsAPI, peerDiscoveryViewModel: PeerDiscoveryViewModel)`**

  This is the main composable function that builds the root of the UI.

  - **Parameters:**
    - `database: DatabaseMng`: An instance of the database manager for local data storage.
    - `cuimsAPI: cuimsAPI`: An instance for interacting with the CUIMS API.
    - `peerDiscoveryViewModel: PeerDiscoveryViewModel`: The view model responsible for handling peer discovery on the network.

  - **Functionality:**
    - Initializes the `Ktor` client for network operations.
    - Wraps the UI in `CompositionLocalProvider` to make `database`, `cuimsAPI`, and `peerDiscoveryViewModel` available throughout the component tree.
    - Applies the custom `AppTheme`.
    - Conditionally displays the `LoginFields` screen if no user data is found in the database (`database.getSize() == 0`), otherwise, it shows the `MainScreen`.

### `Greeting.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/Greeting.kt`

**Description:**

A simple class used for demonstration or testing purposes. It provides a greeting message that includes the name of the current platform.

**Classes:**

- **`Greeting`**
  - **Properties:**
    - `platform: Platform`: An instance of the `Platform` interface, which provides the platform-specific name.
  - **Functions:**
    - `greet(): String`: Returns a greeting string that incorporates the platform's name. For example, "Hello lets see how this works, Android!".

### `Platform.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/Platform.kt`

**Description:**

This file defines the multiplatform abstractions for LoopLink. It includes expected functions and classes that require platform-specific implementations (in `androidMain` and `jvmMain`). It also contains the `DatabaseMng` class, which handles all interactions with the local SQLite database.

**Enums:**

- **`PlatformType`**: An enum to distinguish between `ANDROID` and `DESKTOP` platforms.

**Interfaces:**

- **`Platform`**: An interface that requires a `name` property to identify the platform.

**Expected Declarations:**

- **`getPlatformType(): PlatformType`**: Expected to return the current `PlatformType`.
- **`getPlatform(): Platform`**: Expected to return an implementation of the `Platform` interface.
- **`DriverFactory`**: A class with a constructor and a `createDriver()` method that returns a `SqlDriver` for the database.

**Classes:**

- **`DatabaseMng(driver: SqlDriver)`**: Manages database operations.
  - **Functions:**
    - `insertIntoDatabase(name: String, uid: String)`: Inserts the user's name and UID into the database.
    - `insertUserData(...)`: Inserts comprehensive user data, including profile details and an optional profile picture.
    - `getProfileImage(): ByteArray`: Retrieves the user's profile picture from the database.
    - `getAllFromDatabase(): List<List<String>>`: Fetches all user records and returns them as a list of string lists.
    - `getUserData(): userInfo`: Retrieves and returns user data as a `userInfo` object.
    - `deleteUser()`: Deletes the user's data from the database.
    - `getSize(): Int`: Returns the number of user records in the database.

### `ui/theme/Theme.kt`

**Path:** `composeApp/src/commonMain/kotlin/ui/theme/Theme.kt`

**Description:**

This file defines the application's main theme, `AppTheme`, utilizing Jetpack Compose's MaterialTheme. It configures the typography and color scheme for the entire application, ensuring a consistent visual style.

**Composable Functions:**

- **`AppTheme(content: @Composable () -> Unit)`**

  This composable function sets up the `MaterialTheme` for the application.

  - **Parameters:**
    - `content: @Composable () -> Unit`: The composable content to which the theme will be applied.

  - **Functionality:**
    - Defines `AppTypography` by overriding default MaterialTheme typography with `RobotFont`.
    - Applies `AppTypography` and `Colors.DarkColorScheme` to the `MaterialTheme`.

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

### `ui/ChatScreen.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/ui/ChatScreen.kt`

**Description:**

This file defines the `ChatScreen`, which is responsible for displaying the chat interface for a specific service discovered on the network. It manages the WebSocket session for real-time communication and handles incoming messages.

**Classes:**

- **`ChatScreen(private val serviceInfo: ServiceInfo, private val session: DefaultClientWebSocketSession) : Screen`**: A screen composable that displays the chat UI.
  - **Constructor Parameters:**
    - `serviceInfo: ServiceInfo`: Information about the discovered service to connect to.
    - `session: DefaultClientWebSocketSession`: The active WebSocket session for communication.
  - **Composable `Content()`:**
    - Manages the lifecycle of the WebSocket connection using `DisposableEffect`.
    - Listens for incoming text frames from the WebSocket, creates a `Message` object, and sends it to the central `store`.
    - Closes the WebSocket session when the composable is disposed.
    - Renders the main chat UI using `ChatAppWithScaffold`.


### `theme/colors.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/theme/colors.kt`

**Description:**

This file defines the color palette for the application. It contains the `Colors` object, which specifies the colors used in both the light and dark themes.

**Objects:**

- **`Colors`**: A data object that holds all the color definitions and color schemes.
  - **Properties:**
    - `DarkGrayPrimary`, `LightGrayButton`, `Charcoal`, `BrandBlue`, etc.: `Color` properties defining the specific colors used in the app.
    - `LightColorScheme`: A `lightColorScheme` for the app's light theme.
    - `DarkColorScheme`: A `darkColorScheme` for the app's dark theme, which is used in the `AppTheme`.


### `network/KtorClientFactory.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/network/KtorClientFactory.kt`

**Description:**

This file provides a factory function for creating and configuring a Ktor `HttpClient`. This client is used for making network requests throughout the application.

**Functions:**

- **`createKtorClient(): HttpClient`**: Creates and configures the `HttpClient`.

  - **Configuration:**
    - **Engine:** It uses a platform-specific HTTP client engine provided by the `httpClientEngine()` expect function. This allows for different underlying HTTP clients on Android and JVM.
    - **Content Negotiation:** It installs the `ContentNegotiation` plugin with `Json` for automatic JSON serialization and deserialization.
    - **WebSockets:** It installs the `WebSockets` plugin.
    - **Logging:** It includes a `Logging` plugin for debugging network requests.

**Expected Declarations:**

- **`httpClientEngine(): io.ktor.client.engine.HttpClientEngine`**: This expect function must be implemented in the platform-specific source sets (`androidMain` and `jvmMain`) to provide the appropriate Ktor client engine.

### `network/KtorServerFactory.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/network/KtorServerFactory.kt`

**Description:**

This file configures the Ktor server for the LoopLink application. It sets up JSON content negotiation, WebSockets, and defines the server's routing.

**Functions:**

- **`Application.configureLoopLinkServer()`**: An extension function for the Ktor `Application` class that applies the server configuration.

  - **Configuration:**
    - **Content Negotiation:** Configures the server to use JSON for serialization.
    - **WebSockets:** Installs and configures the `WebSockets` plugin.
    - **Routing:** Defines the following routes:
      - `GET "/"`: A simple test endpoint that responds with "Hello there!".
      - `GET "/android"`: A test endpoint that responds with "Hello from Android!".
      - `webSocket "/looplink/sync"`: A WebSocket endpoint for real-time communication. It listens for text frames and sends a response back to the client.

**Expected Declarations:**

- **`createKtorServerFactory(): ApplicationEngineFactory<ApplicationEngine, *>`**: This expect function must be implemented in the platform-specific source sets to provide the appropriate Ktor server engine (e.g., Netty for JVM, CIO for Android).

### `network/discovery/ServiceInfo.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/network/discovery/ServiceInfo.kt`

**Description:**

This file contains a data class that represents a discovered network service. It holds all the necessary information to connect to a service found on the local network.

**Data Classes:**

- **`ServiceInfo`**
  - **Properties:**
    - `instanceName: String`: The name of the service instance.
    - `serviceName: String`: The name of the service itself.
    - `hostAddress: String`: The IP address of the host providing the service.
    - `port: Int`: The port number on which the service is running.
    - `attributes: Map<String, String>`: A map of key-value pairs containing additional service metadata.

### `network/discovery/LANServiceDiscovery.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/network/discovery/LANServiceDiscovery.kt`

**Description:**

This file defines the `LANServiceDiscovery` class, which is responsible for discovering and registering services on the local area network (LAN). This is an `expect` class, meaning that the actual implementation is provided in the platform-specific source sets (`androidMain` and `jvmMain`).

**Expected Classes:**

- **`LANServiceDiscovery`**: A class that provides the core functionality for network service discovery.
  - **Functions:**
    - `discoverServices(serviceType: String): Flow<List<ServiceInfo>>`: Starts the discovery of services of a given type on the network and returns a `Flow` that emits a list of `ServiceInfo` objects whenever the list of discovered services changes.
    - `registerService(...)`: Registers a new service on the network so that other devices can discover it.
    - `unregistedService()`: Unregisters a previously registered service.
    - `stopDiscovery()`: Stops the service discovery process.
    - `stopDiscovery(serviceType: String?)`: Stops the discovery for a specific service type.

### `errors/errors.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/errors/errors.kt`

**Description:**

This file contains a data object that centralizes error messages used throughout the application.

**Data Objects:**

- **`errorsLL`**: A data object containing common error messages.
  - **Properties:**
    - `timeout_error`: Message for timeout errors.
    - `captcha_error`: Message for captcha-related errors.
    - `internet_error`: Message for internet connection issues.
    - `unknownError`: A generic error message.

### `secrets/Secrets.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/secrets/Secrets.kt`

**Description:**

This file defines an `expect` object for managing API keys and other secrets. The actual values are provided in the platform-specific `actual` declarations to avoid hardcoding secrets in the common code.

**Expected Objects:**

- **`APIKeys`**
  - **Properties:**
    - `ocrKey: String`: The API key for the OCR service.

### `viewmodel/PeerDiscoveryViewModel.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/viewmodel/PeerDiscoveryViewModel.kt`

**Description:**

This view model manages the process of discovering and connecting to peers on the local network. It uses the `LANServiceDiscovery` class to find other devices and exposes the list of discovered services as a `StateFlow`. It also handles the WebSocket connection lifecycle.

**Classes:**

- **`PeerDiscoveryViewModel`**: The view model for peer discovery.

**Sealed Classes:**

- **`ConnectionStatus`**: Represents the current state of the WebSocket connection.
  - `Idle`: The connection is not active.
  - `Connecting`: A connection attempt is in progress.
  - `Connected(session: DefaultClientWebSocketSession)`: The connection is established.
  - `Error(message: String)`: An error occurred during connection.

**Properties:**

- `discoveredServices: StateFlow<List<ServiceInfo>>`: A flow that emits the list of discovered services.
- `isDiscovering: StateFlow<Boolean>`: A flow that emits `true` if the app is currently discovering services.
- `connectionStatus: StateFlow<ConnectionStatus>`: A flow that emits the current WebSocket connection status.

**Functions:**

- `startDiscovery()`: Begins the process of discovering services on the network.
- `stopDiscovery()`: Stops the discovery process.
- `clear()`: Stops discovery and cancels the view model's scope.
- `connectToService(service: ServiceInfo, navigator: Navigator)`: Attempts to connect to a discovered service via WebSocket and navigates to the `ChatScreen` on success.

### `viewmodel/ChatViewModel.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/viewmodel/ChatViewModel.kt`

**Description:**

This file defines the `ChatViewModel`, which is responsible for managing the state of the chat rooms in the application. It uses a `MutableStateFlow` to hold the list of rooms and provides a function to add new rooms.

**Data Classes:**

- **`RoomItem(id: Int, label: String, unread: Int = 0)`**: Represents a single chat room.
  - `id: Int`: A unique identifier for the room.
  - `label: String`: The display name of the room.
  - `unread: Int`: The number of unread messages in the room.

**Classes:**

- **`ChatViewModel: ViewModel`**: Manages the list of chat rooms.
  - **Properties:**
    - `rooms`: A `StateFlow` that emits the current list of `RoomItem`s.
  - **Functions:**
    - `addRoom(roomItem: RoomItem)`: Adds a new room to the list if it doesn't already exist.

### `webDriver/cuimsAPI.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/webDriver/cuimsAPI.kt`

**Description:**

This file defines the `cuimsAPI` class, which is responsible for interacting with the CUIMS web portal. It uses a web driver (like Selenium) to automate the login process, handle captchas, and scrape student data. This is an `expect` class, requiring platform-specific implementations for web driver management and UI display.

**Data Classes:**

- **`successLog`**: A simple data class to represent the success or failure of an operation, with an optional message.
- **`studentInfo`**: A data class to hold the scraped student information.

**Expected Declarations:**

- **`ByteArray.toImageBitmap(): ImageBitmap`**: An extension function to convert a byte array to a platform-specific `ImageBitmap`.
- **`ImageBitmap.toBase64(): String`**: An extension function to convert an `ImageBitmap` to a Base64 string.
- **`cuimsAPI`**: The main class for interacting with the CUIMS portal.
  - **Properties:**
    - `uid: String?`, `pass: String?`: The user's credentials.
    - `student: studentInfo?`: The scraped student data.
    - `BASEURL: String`: The base URL of the CUIMS portal.
    - `endPoints: Map<String, String>`: A map of API endpoints.
  - **Functions:**
    - `initDriver()`: Initializes the web driver.
    - `login(uid: String, pass: String): successLog`: Performs the login process.
    - `getCaptcha(): Pair<successLog, ImageBitmap?>`: Retrieves the captcha image.
    - `fillCaptcha(captcha: String): successLog`: Fills in the captcha.
    - `endSession()`: Ends the web driver session.
    - `processCaptcha(imgBase64: String): String`: Processes the captcha image (e.g., using an OCR service).
    - `autoFillCaptcha(): successLog`: Automates the captcha filling process.
    - `getWebView(): Any`: Returns the web view instance.
    - `loadStudentData(): Pair<successLog, studentInfo?>`: Scrapes the student data from the portal.
    - `destroySession()`: Destroys the web driver session.
- **`getWebViewer(webView: cuimsAPI, modifier: Modifier)`**: A composable function to display the web view for manual captcha entry.

### `components/ProfileCard.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/ProfileCard.kt`

**Description:**

This file contains the UI components for displaying a user's profile information. It includes a `UserProfileCard` that adapts to different screen sizes and a `SettingsPage` that displays this card.

**Data Objects:**

- **`userInfo`**: A data object that holds the current user's information. It includes a `reset()` function to clear the data.

**Functions:**

- **`loadUserInfo(database: DatabaseMng)`**: A function that loads the user's data from the database into the `userInfo` object.

**Composable Functions:**

- **`UserProfileCard(modifier: Modifier = Modifier)`**: A composable that displays the user's profile in a `Card`. It switches between `TallScreenLayout` and `WideScreenLayout` based on the available width.
- **`TallScreenLayout()`**: A composable for displaying the profile in a vertical layout on narrow screens.
- **`WideScreenLayout()`**: A composable for displaying the profile in a horizontal layout on wider screens.
- **`ShowUserData()`**: A composable that displays the individual fields of the user's data (name, UID, etc.).
- **`SideButtons()`**: A composable that contains the `FindDevicesButton` and `LogoutButton`.
- **`LogoutButton(modifier: Modifier = Modifier)`**: A button that logs the user out, clears the database, and navigates to the `LoginFields` screen.
- **`FindDevicesButton(modifier: Modifier = Modifier)`**: A button that navigates to the `AvailableServicesScreen` to start peer discovery.

**Classes:**

- **`SettingsPage : Screen`**: A `Screen` that displays the `UserProfileCard` within a `Scaffold`, providing a settings page with a top app bar.

### `components/textField.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/textField.kt`

**Description:**

This file contains a custom `textField` composable that provides a simple, reusable text input field with a placeholder.

**Composable Functions:**

- **`textField(onvalueChange: (String) -> Unit, radius: Int, paddingH: Int, paddingV: Int, placeholder: String)`**

  This is a composable function that creates a custom text field.

  - **Parameters:**
    - `onvalueChange: (String) -> Unit`: A callback function that is invoked when the text in the field changes.
    - `radius: Int`: The corner radius of the text field.
    - `paddingH: Int`: The horizontal padding of the text field.
    - `paddingV: Int`: The vertical padding of the text field.
    - `placeholder: String`: The placeholder text to be displayed when the text field is empty.

### `components/customTextField.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/customTextField.kt`

**Description:**

This file provides a highly customizable `CustomOutlinedTextField` composable, which is a modified version of the Material Design `OutlinedTextField`. It allows for fine-tuned control over the text field's appearance and behavior.

**Composable Functions:**

- **`CustomOutlinedTextField(...)`**

  This composable offers a wide range of parameters to customize the text field, including:

  - `value`, `onValueChange`: The text field's state.
  - `label`, `placeholder`, `leadingIcon`, `trailingIcon`, etc.: Composable slots for customizing the text field's layout.
  - `isError`, `enabled`, `readOnly`: The text field's state.
  - `shape`, `colors`: The text field's appearance.
  - `keyboardOptions`, `keyboardActions`: The keyboard configuration.

### `components/GetProfileImage.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/GetProfileImage.kt`

**Description:**

This file defines an `expect` composable function for displaying an image from a byte array. This function must be implemented in the platform-specific source sets (`androidMain` and `jvmMain`) to handle the conversion of the byte array to a platform-specific image format.

**Expected Composable Functions:**

- **`GetProfileImage(bytes: ByteArray?, modifier: Modifier = Modifier)`**: Displays an image from a byte array.

### `components/LocalCompositions.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/LocalCompositions.kt`

**Description:**

This file defines several `CompositionLocal` providers that make key dependencies available to the entire Compose UI tree. This is a powerful pattern for dependency injection in Compose, as it avoids the need to pass dependencies down through every composable.

**Composition Locals:**

- **`LocalDatabase`**: Provides an instance of `DatabaseMng` for database access.
- **`LocalCuimsApi`**: Provides an instance of `cuimsAPI` for interacting with the CUIMS portal.
- **`LocalPeerDiscoveryViewModel`**: Provides an instance of `PeerDiscoveryViewModel` for managing peer discovery.
- **`LocalTabNavigator`**: Provides an instance of `TabNavigator` for managing tab-based navigation.

### `components/loginAPIComposable.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/loginAPIComposable.kt`

**Description:**

This file contains the UI for the login screen. It includes text fields for UID and password, a captcha image, and a submit button. It uses the `cuimsAPI` to handle the login process, including fetching the captcha, submitting the login credentials, and handling any errors that may occur.

**Classes:**

- **`LoginFields : Screen`**: The main screen for the login UI.

**Composable Functions:**

- **`Content()`**: The main composable function for the login screen, which includes:
  - Text fields for UID, password, and captcha.
  - An `Image` to display the captcha.
  - A `Button` to submit the login credentials.
  - Error handling and display of error messages.
- **`TextFieldFooterErrorMsg(text: String = "Error")`**: A composable function to display an error message below a text field.

### `components/chat/Data.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/chat/Data.kt`

**Description:**

This file defines the data models for the chat functionality, including `User`, `Message`, and `MessageList`. It also includes a `ColorProvider` to assign unique colors to users.

**Data Classes:**

- **`User`**: Represents a user in the chat.
  - **Properties:**
    - `name: String`: The user's name.
    - `color: Color`: The color associated with the user, provided by `ColorProvider`.
    - `picture: ByteArray?`: An optional byte array for the user's profile picture.
- **`Message`**: Represents a single chat message.
  - **Properties:**
    - `user: User`: The user who sent the message.
    - `text: String`: The content of the message.
    - `seconds: Long`: The time the message was sent, in epoch seconds.
    - `id: Long`: a unique id for the message.
- **`MessageList`**: Represents a list of chat messages.

**Objects:**

- **`ColorProvider`**: An object that provides a unique color for each user from a predefined list of colors.
  - **Functions:**
    - `getColor(): Color`: Returns a unique color for a user.

### `components/chat/Store.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/chat/Store.kt`

**Description:**

This file defines the `Store` interface, which is a key part of the chat's state management. It uses a Redux-like architecture with a central store that receives actions and updates the state through a reducer. The `createStore` function creates an instance of the store within a coroutine scope.

**Interfaces:**

- **`Store`**
  - **Functions:**
    - `send(action: Action)`: Sends an action to the store.
  - **Properties:**
    - `stateFlow: StateFlow<State>`: A `StateFlow` that emits the current state of the chat.
    - `state: State`: The current state of the chat.

**Functions:**

- **`createStore(): Store`**: A factory function that creates a new `Store` instance.

### `components/chat/Colors.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/chat/Colors.kt`

**Description:**

This file defines a set of colors and gradients for the chat UI.

**Objects:**

- **`ChatColors`**: An object containing color and gradient definitions for the chat UI.
  - **Properties:**
    - `GRADIENT_3`, `GRADIENT_2`: Lists of colors for creating gradients.
    - `PRIMARY`, `SURFACE`, `BACKGROUND`: Individual color values.
    - `TOP_GRADIENT`: A gradient for the top of the chat screen.
    - `MY_MESSAGE`, `OTHERS_MESSAGE`: Colors for the message bubbles.
    - `TIME_TEXT`: The color for the message timestamp.

### `components/chat/ChatApp.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/chat/ChatApp.kt`

**Description:**

This file is central to the application's chat feature. It defines the composable functions responsible for rendering the entire chat screen. It follows a clean architectural pattern by separating the UI into a container composable (`ChatAppWithScaffold`) that handles layout, navigation, and user interactions like keyboard shortcuts, and a content composable (`ChatApp`) that focuses on displaying the chat messages and the input field. The file also initializes and exposes a global `store` for state management, based on a Redux-like pattern, and defines the `myUser` object to represent the current user.

**Global Initializations:**

- **`myUser: User`**: Defines a static `User` object named "Me". This instance is used to identify messages sent by the current user, which allows the UI to render them differently (e.g., align them to the right side of the screen).
- **`store: Store`**: Creates a global, application-wide instance of the `Store`. This is the single source of truth for the chat state. It's created within a `CoroutineScope` with a `SupervisorJob` to ensure its lifecycle is independent of any single component. All actions, such as sending a message, are dispatched to this store, which then updates the state that the UI observes.

**Composable Functions:**

- **`ChatAppWithScaffold(displayTextField: Boolean, room: RoomItem)`**

  This function builds a complete, opinionated chat screen. It uses `Scaffold` to provide a standard Material Design layout structure, which includes a top app bar and a main content area.

  - **Parameters:**
    - `displayTextField: Boolean`: A flag to control the visibility of the message input field.
    - `room: RoomItem`: The data object for the current chat room, containing its ID and label.

  - **Functionality:**
    - **Scaffolding:** Provides a `TopAppBar` and a content area for the `ChatApp` composable.
    - **Navigation:** The `TopAppBar` includes a back arrow (`IconButton`) that uses the `LocalTabNavigator` to navigate back to the `EmptyChatTab`, effectively closing the current chat view in a wide-screen layout.
    - **Focus & Keyboard Management:**
      - It sets up a sophisticated focus management system using `FocusRequester` and `onKeyEvent`.
      - It listens for the `Escape` key. When pressed, it navigates back, providing a desktop-like user experience.
      - It also uses `pointerInput` to clear focus from any text field and request focus for the main scaffold area when the user clicks on the background, which is crucial for handling keyboard shortcuts reliably.
    - **Appearance:** The `TopAppBar` is styled with a semi-transparent background to create a modern look.

  - **Initial State:** Uses a `LaunchedEffect` to request focus for the scaffold as soon as it enters the composition, ensuring that keyboard events are captured immediately.

- **`ChatApp(modifier: Modifier, displayTextField: Boolean, room: RoomItem)`**

  This is the core content of the chat screen, responsible for displaying the message history and the input area. It's designed to be a more reusable component that could be placed in different layouts.

  - **Parameters:**
    - `modifier: Modifier`: Standard composable modifier.
    - `displayTextField: Boolean`: Controls whether the `SendMessage` input field is shown.
    - `room: RoomItem`: The current chat room's data.

  - **Functionality:**
    - **State Observation:** It subscribes to the `store.stateFlow` and collects the latest state as a Compose `State` object. This ensures that the UI automatically recomposes whenever the chat state (e.g., a new message arrives) changes.
    - **UI Composition:**
      - It uses a `Column` and `Box` with a weight modifier to structure the screen, ensuring the message list takes up most of the space and the input field is anchored to the bottom.
      - It renders the `Messages` composable, passing it the list of messages for the current `room.id` from the collected state.
      - It conditionally renders the `SendMessage` composable at the bottom.
    - **Action Dispatching:** The `SendMessage` composable provides a callback with the message text. This text is used to create a `Message` object and dispatch a `SendMessage` action to the `store`, which triggers the state update cycle.


### `components/chat/Reducer.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/chat/Reducer.kt`

**Description:**

This file defines the `chatReducer` function, which is the core of the chat's state management. It takes the current state and an action, and returns a new state. It also defines the `Action` sealed interface and the `State` data class.

**Sealed Interfaces:**

- **`Action`**: Represents the possible actions that can be dispatched to the store.
  - `SendMessage(roomId: Int, message: Message)`: An action to send a message to a specific room.
  - `LoadRoom(roomId: Int)`: An action to load a room's data (currently not implemented).

**Data Classes:**

- **`State`**: Represents the state of the chat, which is a map of room IDs to their message lists.

**Functions:**

- **`chatReducer(state: State, action: Action): State`**: The reducer function that handles state updates based on the dispatched action.

### `components/chat/Messages.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/chat/Messages.kt`

**Description:**

This file defines the `Messages` composable, which is responsible for displaying the list of messages in the chat. It also includes a `UserPic` composable for displaying a user's profile picture.

**Expected Composable Functions:**

- **`Messages(modifier: Modifier, messages: List<Message>)`**: An `expect` composable function that displays a list of messages. This function must be implemented in the platform-specific source sets (`androidMain` and `jvmMain`).

**Composable Functions:**

- **`UserPic(user: User)`**: A composable that displays a user's profile picture in a circular shape. If the user has no picture, it displays a colored rectangle instead.

### `components/chat/ChatMessage.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/chat/ChatMessage.kt`

**Description:**

This file defines the `ChatMessage` composable, which is responsible for displaying a single chat message. It shows the user's name, profile picture, the message text, and a timestamp. It also includes a `TriangleEdgeShape` to create a speech bubble effect.

**Composable Functions:**

- **`Triangle(risingToTheRight: Boolean, background: Color)`**: A composable that displays a triangular shape, used to create the speech bubble effect.
- **`ChatMessage(isMyMessage: Boolean, message: Message, sameUser: Boolean)`**: The main composable for displaying a single chat message.
  - **Parameters:**
    - `isMyMessage: Boolean`: A boolean to indicate if the message is from the current user.
    - `message: Message`: The message to be displayed.
    - `sameUser: Boolean`: A boolean to indicate if the message is from the same user as the previous one.

**Classes:**

- **`TriangleEdgeShape(risingToTheRight: Boolean) : Shape`**: A custom `Shape` that creates a triangular path for the speech bubble effect.

### `components/chat/SendMessage.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/chat/SendMessage.kt`

**Description:**

This file defines the `SendMessage` composable, which is the input field for the chat. It includes a text field, an emoji picker, and a send button. It also handles the logic for sending a message and showing/hiding the emoji panel.

**Composable Functions:**

- **`SendMessage(modifier: Modifier, sendMessage: (String) -> Unit)`**: The main composable for the message input field.
  - **Parameters:**
    - `sendMessage: (String) -> Unit`: A callback function to be invoked when the user sends a message.
- **`EmojiPanel(onEmojiSelected: (String) -> Unit, modifier: Modifier)`**: A composable that displays a grid of emojis. When an emoji is selected, the `onEmojiSelected` callback is invoked.

### `components/chat/EmojiFactory.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/chat/EmojiFactory.kt`

**Description:**

This file defines an `expect` function for opening an emoji panel. This function must be implemented in the platform-specific source sets (`androidMain` and `jvmMain`) to provide the appropriate Emoji picker for each platform.

**Expected Functions:**

- **`openEmojiPanel(x: Int, y: Int)`**: Opens a platform-specific emoji panel at the specified coordinates.

### `components/chat/EmojiBackground.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/chat/EmojiBackground.kt`

**Description:**

This file contains a complex composable that displays a background of animated emojis. It uses a custom `EmojiBgState` class to manage the state of the emojis, including their positions, animations, and the emoji set to be displayed.

**Composable Functions:**

- **`EmojiFont()`**: A composable that loads the custom emoji font.
- **`EmojiBg(...)`**: The main composable for the emoji background.
  - **Parameters:**
    - `emojiBgState: EmojiBgState`: The state of the emoji background.
    - `emojiSize: Dp`: The size of the emojis.
    - `emojiColor: Color`: The color of the emojis.
    - `gap: Dp`: The gap between the emojis.

**Classes:**

- **`EmojiBgState`**: A state class that manages the emoji background. It includes functions for updating the container size, item diameter, and text size. It also handles animations for the emojis.

**Expected Functions:**

- **`getPlatformTextStyle(): PlatformTextStyle`**: An `expect` function to get the platform-specific text style for emojis.

### `components/chat/currentTime.common.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/chat/currentTime.common.kt`

**Description:**

This file contains a utility function to format a timestamp into a human-readable string (HH:mm).

**Functions:**

- **`timeToString(seconds: Long): String`**: Formats a timestamp (in seconds) into a string.

### `components/fabButtons/FabButtonSub.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/fabButtons/FabButtonSub.kt`

**Description:**

This file defines the styling options for the sub-buttons within a multi-action Floating Action Button (FAB). It provides an interface and a composable factory function to create customized sub-button appearances.

**Interfaces:**

- **`FabButtonSub`**: Defines the required styling properties for a FAB sub-item.
  - `iconTint: Color`: The color of the icon within the sub-button.
  - `backgroundTint: Color`: The background color of the sub-button.

**Composable Functions:**

- **`FabButtonSub(backgroundTint: Color, iconTint: Color): FabButtonSub`**: A factory function that constructs a `FabButtonSub` instance. It allows for the customization of the background and icon tints, using default colors from the application's `MaterialTheme` if not explicitly provided.

### `components/fabButtons/FabButtonState.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/fabButtons/FabButtonState.kt`

**Description:**

This file defines the state management for the multi-action Floating Action Button (FAB). It provides a sealed class to represent the FAB's collapsed and expanded states, along with a composable function to remember and manage this state.

**Sealed Classes:**

- **`FabButtonState`**: Represents the two possible states of the FAB.
  - **Objects:**
    - `Collapsed`: The state where the FAB shows only the main button.
    - `Expand`: The state where the FAB also shows its sub-action buttons.
  - **Functions:**
    - `isExpanded()`: Returns `true` if the state is `Expand`.
    - `toggleValue()`: Switches the state between `Collapsed` and `Expand`.

**Composable Functions:**

- **`rememberMultiFabState()`**: A composable function that creates and remembers an instance of `MutableState<FabButtonState>`, initialized to `FabButtonState.Collapsed`. This allows the FAB's state to be preserved across recompositions.

### `components/fabButtons/FabButtonMain.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/fabButtons/FabButtonMain.kt`

**Description:**

This file defines the properties and creation of the main Floating Action Button (FAB) in a multi-action FAB component. It specifies the icon for the button and its rotation behavior when the FAB is expanded.

**Interfaces:**

- **`FabButtonMain`**: Defines the properties for the main FAB.
  - `iconRes: ImageVector`: The icon to be displayed on the main FAB.
  - `iconRotate: Float?`: The angle in degrees to rotate the icon when the FAB is in its expanded state. A null value means no rotation.

**Functions:**

- **`FabButtonMain(iconRes: ImageVector, iconRotate: Float): FabButtonMain`**: A factory function that creates an instance of `FabButtonMain`. It allows customization of the icon and rotation angle, providing default values for a common "add" icon that rotates 45 degrees.

### `components/fabButtons/FabButtonItem.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/fabButtons/FabButtonItem.kt`

**Description:**

This file defines the data model for an individual item within a multi-action Floating Action Button (FAB) component. Each item represents a specific action that the user can take.

**Data Classes:**

- **`FabButtonItem(iconRes: ImageVector, label: String, onClick: () -> Unit)`**: Represents a single action item in the FAB.
  - `iconRes: ImageVector`: The icon to be displayed for this action item.
  - `label: String`: The text label that describes the action.
  - `onClick: () -> Unit`: The lambda function that is invoked when the user clicks on this action item.

### `components/fabButtons/ExpandedFabButton.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/components/fabButtons/ExpandedFabButton.kt`

**Description:**

This file contains the core implementation of the multi-action Floating Action Button (FAB). It provides composable functions to build a main FAB that can be expanded to reveal a list of smaller, secondary action buttons. The expansion is animated, and the main FAB icon can be configured to rotate when the state changes.

**Composable Functions:**

- **`MultiFloatingActionButton(...)`**

  This is the main composable for the multi-action FAB.

  - **Parameters:**
    - `items: List<FabButtonItem>`: A list of `FabButtonItem` objects, each representing a secondary action.
    - `fabState: MutableState<FabButtonState>`: The state of the FAB (collapsed or expanded).
    - `fabIcon: FabButtonMain`: The configuration for the main FAB's icon and rotation.
    - `fabOption: FabButtonSub`: The styling options for the sub-action buttons.
    - `stateChanged: (fabState: FabButtonState) -> Unit`: A callback invoked when the FAB's state changes.

  - **Functionality:**
    - Displays a primary `FloatingActionButton`.
    - Animates the rotation of the main FAB's icon based on the `fabState`.
    - When `fabState` is `Expand`, it uses `AnimatedVisibility` to show a `LazyColumn` of `MiniFabItem`s with a smooth animation.
    - Toggles the `fabState` when the main FAB is clicked.

- **`MiniFabItem(item: FabButtonItem, fabOption: FabButtonSub)`**

  This composable renders a single secondary action item.

  - **Parameters:**
    - `item: FabButtonItem`: The data for the item, including its icon, label, and `onClick` action.
    - `fabOption: FabButtonSub`: The styling for the sub-button.

  - **Functionality:**
    - Displays a `Row` containing a text label and a small `FloatingActionButton`.
    - The `onClick` lambda from the `item` is attached to the button.


### `operations/Logout.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/operations/Logout.kt`

**Description:**

This file defines the `logout` function responsible for clearing user session data.

**Functions:**

- **`logout(database: DatabaseMng)`**
  - **Parameters:**
    - `database: DatabaseMng`: An instance of the database manager for local data storage.
  - **Functionality:**
    - Deletes all user data from the local database using `database.deleteUser()`.
    - Resets the `userInfo` singleton to clear any in-memory user data.

### `operations/InsertUserDataFromProfile.kt`

**Path:** `composeApp/src/commonMain/kotlin/org/asv/looplink/operations/InsertUserDataFromProfile.kt`

**Description:**

This file contains a utility function to insert scraped student profile data into the local database and update the current user's in-memory information.

**Functions:**

- **`insertUserDataFromProfile(databaseMng: DatabaseMng, it: studentInfo, myUser: User)`**
  - **Parameters:**
    - `databaseMng: DatabaseMng`: An instance of the database manager for local data storage.
    - `it: studentInfo`: A `studentInfo` object containing the scraped data from the CUIMS portal.
    - `myUser: User`: The `User` object representing the current user, whose details will be updated.
  - **Functionality:**
    - Inserts the student's full details (name, UID, section, program, contact, cGPA, email, profile picture bytes) into the database.
    - Updates the `myUser` object's `name` and `picture` properties with the corresponding values from the `studentInfo` object.

## `androidMain`

### `MainActivity.kt`

**Path:** `composeApp/src/androidMain/kotlin/org/asv/looplink/MainActivity.kt`

**Description:**

This is the main entry point for the Android application. It creates and initializes the `MainViewModel`, which in turn initializes the database, the CUIMS API, the LAN service discovery, the peer discovery view model, and the Ktor server. The `MainActivity` then sets the content to the `App` composable, passing the necessary dependencies.

**Classes:**

- **`MainViewModel(applicationContext: Context) : ViewModel`**: A `ViewModel` that holds instances of the application's key components, including the server, database, and view models. It also manages the lifecycle of these components.
- **`MainActivity : ComponentActivity`**: The main activity of the application. It creates the `MainViewModel` and sets the content to the `App` composable.

### `Platform.android.kt`

**Path:** `composeApp/src/androidMain/kotlin/org/asv/looplink/Platform.android.kt`

**Description:**

This file provides the Android-specific implementations for the `expect` declarations in `commonMain/Platform.kt`. It defines the `AndroidPlatform` class, which provides the platform name, and the `DriverFactory` class, which creates an `AndroidSqliteDriver` for the database. It also returns the correct `PlatformType`.

**Classes:**

- **`AndroidPlatform : Platform`**: The Android-specific implementation of the `Platform` interface.
- **`DriverFactory`**: The Android-specific implementation of the `DriverFactory` class.

**Functions:**

- **`getPlatform()`**: Returns an instance of the `AndroidPlatform` class.
- **`getPlatformType()`**: Returns `PlatformType.ANDROID`.

### `network/AndroidKtorServer.kt`

**Path:** `composeApp/src/androidMain/kotlin/org/asv/looplink/network/AndroidKtorServer.kt`

**Description:**

This file defines a class that manages the Ktor server on Android. It handles starting and stopping the server, as well as registering and unregistering the service with `LANServiceDiscovery`.

**Classes:**

- **`AndroidKtorServer(context: Context)`**: A class that manages the Ktor server on Android.
  - **Functions:**
    - `start(port: Int, instanceName: String?)`: Starts the Ktor server and registers the service.
    - `stop()`: Stops the Ktor server.
    - `isRunning(): Boolean`: Returns `true` if the server is running.
    - `getCurrentPort(): Int`: Returns the current port of the server.
    - `close()`: Stops the server and closes the service discovery.

### `network/KtorClientFactory.android.kt`

**Path:** `composeApp/src/androidMain/kotlin/org/asv/looplink/network/KtorClientFactory.android.kt`

**Description:**

This file provides the Android-specific implementation for the `httpClientEngine` function, which returns an instance of the `CIO` engine.

**Functions:**

- **`httpClientEngine(): HttpClientEngine`**: Returns an instance of the `CIO` HTTP client engine.
### `network/KtorServerFactory.android.kt`

**Path:** `composeApp/src/androidMain/kotlin/org/asv/looplink/network/KtorServerFactory.android.kt`

**Description:**

This file provides the Android-specific implementation for the `createKtorServerFactory` function, which returns an instance of the `CIO` engine.

**Functions:**

- **`createKtorServerFactory(): ApplicationEngineFactory<ApplicationEngine, *>`**: Returns an instance of the `CIO` server engine.

### `network/discovery/LANServiceDiscovery.android.kt`

**Path:** `composeApp/src/androidMain/kotlin/org/asv/looplink/network/discovery/LANServiceDiscovery.android.kt`

**Description:**

This file provides the Android-specific implementation for the `LANServiceDiscovery` class. It uses Android's `NsdManager` to discover and register services on the local network.

**Classes:**

- **`LANServiceDiscovery`**: The Android-specific implementation of the `LANServiceDiscovery` class.
  - **Functions:**
    - `discoverServices(serviceType: String): Flow<List<ServiceInfo>>`: Discovers services using `NsdManager` and returns a `Flow` of `ServiceInfo` objects.
    - `registerService(...)`: Registers a service using `NsdManager`.
    - `unregistedService()`: Unregisters a service.
    - `stopDiscovery()`: Stops the discovery process.
    - `close()`: Stops discovery and unregisters the service.

### `secrets/Secrets.android.kt`

**Path:** `composeApp/src/androidMain/kotlin/org/asv/looplink/secrets/Secrets.android.kt`

**Description:**

This file provides the Android-specific implementation for the `APIKeys` object. It retrieves the OCR API key from the `BuildConfig` file, which is generated at compile time. This is a secure way to store API keys without hardcoding them in the source code.

**Objects:**

- **`APIKeys`**: The Android-specific implementation of the `APIKeys` object.
  - **Properties:**
    - `ocrKey: String`: The OCR API key, retrieved from `BuildConfig.ocrSpaceAPIKEY`.

### `webDriver/cuimsAPI.android.kt`

**Path:** `composeApp/src/androidMain/kotlin/org/asv/looplink/webDriver/cuimsAPI.android.kt`

**Description:**

This file contains the Android-specific implementation of the `cuimsAPI` class, which uses an Android `WebView` to interact with the CUIMS website. It uses JavaScript injection to automate the login process, capture captcha images, and scrape student data.

**Classes:**

- **`cuimsAPI`**: The Android-specific implementation of the `cuimsAPI` class.
  - **Functions:**
    - `initDriver()`: Initializes the `WebView`.
    - `login(uid: String, pass: String)`: Automates the login process.
    - `getCaptcha()`: Captures the captcha image from the `WebView`.
    - `fillCaptcha(captcha: String)`: Fills the captcha and submits the login form.
    - `processCaptcha(imgBase64: String)`: Uses an OCR API to solve the captcha.
    - `loadStudentData()`: Scrapes student data from the profile and results pages.
    - `getWebViewer(webView: cuimsAPI, modifier: Modifier)`: A composable function to display the `WebView` in the UI.

**Functions:**

- **`ByteArray.toImageBitmap()`**: Converts a `ByteArray` to a Compose `ImageBitmap`.
- **`ImageBitmap.toBase64()`**: Converts an `ImageBitmap` to a Base64 string.


### `components/GetProfileImage.android.kt`

**Path:** `composeApp/src/androidMain/kotlin/org/asv/looplink/components/GetProfileImage.android.kt`

**Description:**

This file provides the Android-specific implementation for the `GetProfileImage` composable function. It decodes a `ByteArray` into a `Bitmap` and displays it using the `Image` composable.

**Composable Functions:**

- **`GetProfileImage(bytes: ByteArray?, modifier: Modifier)`**: The Android-specific implementation of the `GetProfileImage` composable.

### `components/chat/Messages.android.kt`

**Path:** `composeApp/src/androidMain/kotlin/org/asv/looplink/components/chat/Messages.android.kt`

**Description:**

This file provides the Android-specific implementation for the `Messages` composable. It uses a `LazyColumn` to display the list of messages and automatically scrolls to the bottom when a new message is added.

**Composable Functions:**

- **`Messages(modifier: Modifier, messages: List<Message>)`**: The Android-specific implementation of the `Messages` composable.

### `components/chat/EmojiBackground.android.kt`

**Path:** `composeApp/src/androidMain/kotlin/org/asv/looplink/components/chat/EmojiBackground.android.kt`

**Description:**

This file provides the Android-specific implementation for the `getPlatformTextStyle` and `openEmojiPanel` functions. `getPlatformTextStyle` is configured to disable the default emoji support, and `openEmojiPanel` is a stub that currently does nothing.

**Functions:**

- **`getPlatformTextStyle()`**: Returns a `PlatformTextStyle` with emoji support disabled.
- **`openEmojiPanel(x: Int, y: Int)`**: An empty function, intended to open a platform-specific emoji panel.
## `jvmMain`

### `main.kt`

**Path:** `composeApp/src/jvmMain/kotlin/org/asv/looplink/main.kt`

**Description:**

This is the main entry point for the JVM application. It sets up the main window, initializes the database, the CUIMS API, the LAN service discovery, the peer discovery view model, and the Ktor server. It also handles the application's lifecycle, including a graceful shutdown process.

**Functions:**

- **`main()`**: The main function that launches the application.

### `Platform.jvm.kt`

**Path:** `composeApp/src/jvmMain/kotlin/org/asv/looplink/Platform.jvm.kt`

**Description:**

This file provides the JVM-specific implementations for the `expect` declarations in `commonMain/Platform.kt`. It defines the `JVMPlatform` class, which provides the platform name, and the `DriverFactory` class, which creates a `JdbcSqliteDriver` for the database. It also returns the correct `PlatformType`.

**Classes:**

- **`JVMPlatform : Platform`**: The JVM-specific implementation of the `Platform` interface.
- **`DriverFactory`**: The JVM-specific implementation of the `DriverFactory` class.

**Functions:**

- **`getPlatform()`**: Returns an instance of the `JVMPlatform` class.
- **`getPlatformType()`: Returns `PlatformType.DESKTOP`.**
### `network/KtorClientFactory.jvm.kt`

**Path:** `composeApp/src/jvmMain/kotlin/org/asv/looplink/network/KtorClientFactory.jvm.kt`

**Description:**

This file provides the JVM-specific implementation for the `httpClientEngine` function, which returns an instance of the `Apache` engine.

**Functions:**

- **`httpClientEngine(): HttpClientEngine`**: Returns an instance of the `Apache` HTTP client engine.

### `network/KtorServerFactory.jvm.kt`

**Path:** `composeApp/src/jvmMain/kotlin/org/asv/looplink/network/KtorServerFactory.jvm.kt`

**Description:**

This file provides the JVM-specific implementation for the `createKtorServerFactory` function, which returns an instance of the `Netty` engine.

**Functions:**

- **`createKtorServerFactory(): ApplicationEngineFactory<ApplicationEngine, *>`**: Returns an instance of the `Netty` server engine.

### `network/JvmKtorServerRunner.kt`

**Path:** `composeApp/src/jvmMain/kotlin/org/asv/looplink/network/JvmKtorServerRunner.kt`

**Description:**

This file defines the `jvmKtorServerRunner` object, which is responsible for managing the lifecycle of the Ktor server on JVM platforms. It handles starting, stopping, and registering the server as a service on the local network using `LANServiceDiscovery`.

**Objects:**

- **`jvmKtorServerRunner`**
  - **Properties:**
    - `serverEngine: EmbeddedServer<ApplicationEngine, *>?`: The Ktor `EmbeddedServer` instance.
    - `serverJob: Job?`: The coroutine job managing the server's lifecycle.
    - `serverScope: CoroutineScope`: The coroutine scope for server operations.
    - `currentPort: Int`: The port the server is currently running on.
    - `isRunning: Boolean`: Indicates if the server is currently running.
    - `serviceDiscovery: LANServiceDiscovery`: An instance of `LANServiceDiscovery` for network service management.
    - `SERVICE_TYPE: String`: The service type for network discovery (`_looplink._tcp`).
    - `serviceInstanceName: String`: The instance name for the registered service.
  - **Functions:**
    - `start(port: Int = 0, instanceName: String = serviceInstanceName): Int`: Starts the Ktor server, registers the service on the network, and returns the port it's running on.
      - **Parameters:**
        - `port: Int`: The desired port for the server. If 0, a random available port is used.
        - `instanceName: String`: The instance name for the service registration.
    - `stop()`: Stops the Ktor server and unregisters the service.
    - `isRunning(): Boolean`: Returns `true` if the server is active.
    - `getCurrentPort(): Int`: Returns the port the server is running on, or 0 if not active.
    - `closeDiscovery()`: Closes the service discovery mechanism.

### `network/discovery/LANServiceDiscovery.jvm.kt`

**Path:** `composeApp/src/jvmMain/kotlin/org/asv/looplink/network/discovery/LANServiceDiscovery.jvm.kt`

**Description:**

This file provides the JVM-specific implementation for the `LANServiceDiscovery` class. It utilizes Java's MulticastSocket for service discovery and registration on the local network.

**Classes:**

- **`LANServiceDiscovery`**: The JVM-specific implementation of the `LANServiceDiscovery` class.
  - **Functions:**
    - `discoverServices(serviceType: String): Flow<List<ServiceInfo>>`: Discovers services using MulticastSocket and returns a `Flow` of `ServiceInfo` objects.
    - `registerService(...)`: Registers a service using MulticastSocket.
    - `unregistedService()`: Unregisters a service.
    - `stopDiscovery()`: Stops the discovery process.
    - `close()`: Stops discovery and unregisters the service.

### `secrets/Secrets.jvm.kt`

**Path:** `composeApp/src/jvmMain/kotlin/org/asv/looplink/secrets/Secrets.jvm.kt`

**Description:**

This file provides the JVM-specific implementation for the `APIKeys` object. It retrieves the OCR API key from environment variables, which is a secure way to manage secrets in production environments.

**Objects:**

- **`APIKeys`**: The JVM-specific implementation of the `APIKeys` object.
  - **Properties:**
    - `ocrKey: String`: The OCR API key, retrieved from the `OCR_SPACE_API_KEY` environment variable.

### `webDriver/cuimsAPI.jvm.kt`

**Path:** `composeApp/src/jvmMain/kotlin/org/asv/looplink/webDriver/cuimsAPI.jvm.kt`

**Description:**

This file contains the JVM-specific implementation of the `cuimsAPI` class, which uses Selenium WebDriver (specifically, ChromeDriver) to interact with the CUIMS website. It automates the login process, handles captcha images, and scrapes student data.

**Classes:**

- **`cuimsAPI`**: The JVM-specific implementation of the `cuimsAPI` class.
  - **Functions:**
    - `initDriver()`: Initializes the Selenium ChromeDriver.
    - `login(uid: String, pass: String)`: Automates the login process using Selenium.
    - `getCaptcha()`: Captures the captcha image from the web page.
    - `fillCaptcha(captcha: String)`: Fills the captcha and submits the login form.
    - `processCaptcha(imgBase64: String)`: Uses an OCR API to solve the captcha.
    - `loadStudentData()`: Scrapes student data from the profile and results pages.
    - `getWebView(): Any`: Returns the WebDriver instance.
    - `destroySession()`: Quits the WebDriver session.

**Functions:**

- **`ByteArray.toImageBitmap()`**: Converts a `ByteArray` to a Compose `ImageBitmap`.
- **`ImageBitmap.toBase64()`**: Converts an `ImageBitmap` to a Base64 string.

### `components/GetProfileImage.jvm.kt`

**Path:** `composeApp/src/jvmMain/java/org/asv/looplink/components/GetProfileImage.jvm.kt`

**Description:**

This file provides the JVM-specific implementation for the `GetProfileImage` composable function. It decodes a `ByteArray` into a `Bitmap` and displays it using the `Image` composable.

**Composable Functions:**

- **`GetProfileImage(bytes: ByteArray?, modifier: Modifier)`**: The JVM-specific implementation of the `GetProfileImage` composable.

### `components/chat/Messages.jvm.kt`

**Path:** `composeApp/src/jvmMain/kotlin/org/asv/looplink/components/chat/Messages.jvm.kt`

**Description:**

This file provides the JVM-specific implementation for the `Messages` composable. It uses a `LazyColumn` to display the list of messages and automatically scrolls to the bottom when a new message is added.

**Composable Functions:**

- **`Messages(modifier: Modifier, messages: List<Message>)`**: The JVM-specific implementation of the `Messages` composable.

### `components/chat/EmojiFactory.jvm.kt`

**Path:** `composeApp/src/jvmMain/kotlin/org/asv/looplink/components/chat/EmojiFactory.jvm.kt`

**Description:**

This file provides the JVM-specific implementation for the `openEmojiPanel` function, which is an `expect` declaration from `commonMain/components/chat/EmojiFactory.kt`. On Windows, it uses Java's `Robot` class to simulate the `Windows + .` key combination, which typically opens the emoji panel. For other operating systems, it logs a message indicating that the functionality is only supported on Windows.

**Functions:**

- **`openEmojiPanel(x: Int, y: Int)`**:
  - **Parameters:**
    - `x: Int`: The X-coordinate for positioning the emoji panel (currently unused).
    - `y: Int`: The Y-coordinate for positioning the emoji panel (currently unused).
  - **Functionality:**
    - Checks the operating system.
    - If Windows, simulates the `Windows + .` key press to open the system emoji panel.
    - For other OS, prints a message indicating lack of support.

### `components/chat/EmojiBackground_jvm.kt`

**Path:** `composeApp/src/jvmMain/java/org/asv/looplink/components/chat/emojiBackground_jvm.kt`

**Description:**

This file provides the JVM-specific implementation for the `getPlatformTextStyle` function, which is an `expect` declaration from `commonMain/components/chat/EmojiBackground.kt`. It configures the text style to disable default emoji support, ensuring custom emoji rendering can be applied.

**Functions:**

- **`getPlatformTextStyle(): PlatformTextStyle`**:
  - **Functionality:** Returns a `PlatformTextStyle` with `spanStyle` and `paragraphStyle` set to `null`, effectively disabling platform-specific emoji rendering.

### `ui/theme/Type.jvm.kt`

**Path:** `composeApp/src/jvmMain/kotlin/ui/theme/Type.jvm.kt`

**Description:**

This file provides the JVM-specific implementation for the `resource` function, which is used for loading fonts in Compose Multiplatform. It wraps the platform-agnostic font path, weight, and style into a JVM-specific `androidx.compose.ui.text.platform.Font` object.

**Functions:**

- **`resource(path: String, weight: FontWeight, style: FontStyle): Font`**:
  - **Parameters:**
    - `path: String`: The path to the font resource.
    - `weight: FontWeight`: The font weight (e.g., `FontWeight.Normal`, `FontWeight.Bold`).
    - `style: FontStyle`: The font style (e.g., `FontStyle.Normal`, `FontStyle.Italic`).
  - **Functionality:** Creates and returns a `androidx.compose.ui.text.platform.Font` instance using the provided parameters.

## `LLData.sq`

**Path:** `composeApp/src/commonMain/sqldelight/com/db/LLData.sq`

**Description:**

This file defines the SQLDelight schema for the local SQLite database used in the LoopLink project. It specifies the `LoopLinkUser` table and the SQL queries for common database operations such as inserting, retrieving, and deleting user data.

**Table Schema:**

- **`LoopLinkUser`**: Stores user-related information.
  - `name TEXT NOT NULL`: The user's name.
  - `uid TEXT NOT NULL`: The user's unique identifier.
  - `section TEXT`: The user's academic section (nullable).
  - `program TEXT`: The user's academic program (nullable).
  - `contact TEXT`: The user's contact information (nullable).
  - `cGPA TEXT`: The user's current cGPA (nullable).
  - `email TEXT`: The user's email address (nullable).
  - `pfpImage BLOB`: The user's profile picture as a binary large object (nullable).

**Indexes:**

- `USERNAME ON LoopLinkUser(name)`: An index on the `name` column for faster lookups.

**Queries:**

- **`insert`**: Inserts a new user with `name` and `uid`.
  - `INSERT INTO LoopLinkUser (name, uid) VALUES (?, ?);`
- **`insertAll`**: Inserts comprehensive user data including all nullable fields.
  - `INSERT INTO LoopLinkUser (name, uid, section, program, contact, cGPA, email, pfpImage) VALUES (?, ?, ?, ? , ?, ?, ?, ?);`
- **`getPFP`**: Retrieves the profile picture (`pfpImage`) of the first user found.
  - `SELECT pfpImage FROM LoopLinkUser LIMIT 1;`
- **`selectAll`**: Retrieves all columns for all users from the `LoopLinkUser` table.
  - `SELECT LoopLinkUser.* FROM LoopLinkUser;`
- **`delete`**: Deletes all records from the `LoopLinkUser` table.
  - `DELETE FROM LoopLinkUser;`
- **`getSize`**: Returns the total number of records in the `LoopLinkUser` table.
  - `SELECT COUNT(*) FROM LoopLinkUser;`

## `build.gradle.kts`

**Path:** `composeApp/build.gradle.kts`

**Description:**

This Gradle Kotlin script configures the `composeApp` module of the LoopLink multiplatform project. It applies various plugins, defines platform-specific and common dependencies, and configures Android and Compose Desktop build settings, including SQLDelight database generation and application packaging.

**Plugins:**

- `kotlinMultiplatform`: Enables Kotlin Multiplatform capabilities.
- `androidApplication`: Configures the module as an Android application.
- `composeMultiplatform`, `composeCompiler`, `composeHotReload`: Essential plugins for Compose Multiplatform development, including hot reload functionality.
- `kotlinxSerialization`: Enables Kotlinx Serialization for efficient data serialization.
- `sqldelight`: Integrates SQLDelight for SQLite database management.

**Kotlin Configuration:**

- **`androidTarget`**: Sets the JVM target to 11 for Android builds.
- **`jvm()`**: Configures the JVM target for desktop builds.
- **`sourceSets`**: Defines dependencies for different source sets:
  - **`androidMain`**: Android-specific dependencies including Compose UI, AndroidX Activity, SQLDelight Android driver, Ktor Android client and CIO server, and JmDNS for service discovery.
  - **`commonMain`**: Common dependencies shared across all platforms, such as Compose UI, Material3, Kotlinx Coroutines, Kotlinx Datetime, Koin for DI, SQLDelight runtime and coroutines extensions, Ktor client and server core, WebSockets, Content Negotiation, Serialization, Logging, Selenium for web driving, and Jsoup for HTML parsing, and Voyager for navigation.
  - **`jvmMain`**: JVM-specific dependencies including Compose Desktop, Kotlinx Coroutines Swing, SQLDelight JVM driver, Ktor Netty and CIO server, Ktor client CIO, and JmDNS.

**SQLDelight Configuration:**

- **`databases`**: Configures the `LLData` database.
  - `packageName = "com.db"`: Specifies the package name for generated database code.
  - `generateAsync = false`: Disables asynchronous code generation.
  - `version = 1.0`: Sets the database schema version.

**Android Configuration:**

- `namespace`: Sets the application's namespace.
- `compileSdk`: Specifies the Android compile SDK version (36).
- `buildFeatures.buildConfig = true`: Enables BuildConfig field generation.
- `defaultConfig`:
  - `applicationId`: Sets the application ID.
  - `minSdk`, `targetSdk`, `versionCode`, `versionName`: Standard Android application properties.
  - **`ocrSpaceAPIKEY`**: Reads the `ocrSpaceAPIKEY` from `local.properties` and exposes it as a `BuildConfig` field for secure API key management.
- `packaging.resources.excludes`: Excludes specific files from resource packaging.
- `buildTypes.release`: Disables minification for release builds.
- `compileOptions`: Sets Java source and target compatibility to Java 11.

**Dependencies:**

- `core-ktx`: Kotlin extensions for AndroidX libraries.
- `compose.uiTooling`: Compose UI tooling for debug builds.

**Compose Resources:**

- `publicResClass = true`, `generateResClass = auto`: Configures resource generation for Compose Multiplatform.

**Compose Desktop Application:**

- `mainClass = "org.asv.looplink.MainKt"`: Specifies the main class for the desktop application.
- `nativeDistributions`: Configures native desktop package generation.
  - `targetFormats`: Defines target formats (Dmg, Msi, Deb).
  - `packageName`, `packageVersion`: Sets package name and version.
  - `windows`:
    - `iconFile`: Specifies the application icon for Windows.
    - `includeAllModules = true`: Ensures all modules are included in the distribution.

**Repositories:**

- `google()`, `mavenCentral()`: Standard Maven repositories for dependency resolution.

## `libs.versions.toml`

**Path:** `D:/Work/College/Projects/Log Chat/LoopLink/LoopLink/gradle/libs.versions.toml`

**Description:**

This TOML file manages the versions and declarations of dependencies and plugins used across the LoopLink project, leveraging Gradle's Version Catalogs feature. This centralizes dependency management, making it easier to maintain and update versions consistently.

**Sections:**

- **`[versions]`**: Defines various version numbers for libraries and tools.
  - `agp`: Android Gradle Plugin version.
  - `android-compileSdk`, `android-minSdk`, `android-targetSdk`: Android SDK versions.
  - `androidx-activity`, `androidx-appcompat`, `androidx-core`, `androidx-espresso`, `androidx-lifecycle`, `androidx-testExt`: AndroidX library versions.
  - `composeHotReload`, `composeMultiplatform`, `composeCompiler`: Compose Multiplatform related versions.
  - `junit`, `kotlin`, `kotlinx-coroutines`, `coreKtx`, `coroutinesVersion`, `dateTimeVersion`: Core Kotlin and testing library versions.
  - `koin`: Koin dependency injection framework version.
  - `ktor`: Ktor framework version.
  - `logbackClassic`: Logback logging library version.
  - `sqlDelight`: SQLDelight database library version.
  - `lifecycleViewmodelCompose`, `material3`: Additional AndroidX and Material Design library versions.
  - `jmdns`: JmDNS for network service discovery.
  - `selenium`: Selenium WebDriver version.
  - `jsoup`: Jsoup for HTML parsing.
  - `voyager`: Voyager navigation library version.
  - `uiTextAndroid`: Android-specific Compose UI text library version.

- **`[libraries]`**: Declares the actual library dependencies with their modules and versions, referencing the versions defined in the `[versions]` section.
  - Includes various AndroidX libraries, Kotlin Coroutines, Kotlinx Datetime, Koin, Ktor client and server components (core, websockets, content negotiation, serialization, logging, platform-specific engines like Android, CIO, Netty), SQLDelight drivers (Android, JVM), JmDNS, Selenium, Jsoup, and Voyager navigation components.

- **`[plugins]`**: Declares Gradle plugins used in the project, referencing their versions.
  - `androidApplication`, `androidLibrary`: Android Gradle plugins.
  - `composeHotReload`, `composeMultiplatform`, `composeCompiler`: Compose Multiplatform plugins.
  - `kotlinMultiplatform`, `kotlinAndroid`: Kotlin Gradle plugins.
  - `kotlinxSerialization`: Kotlinx Serialization plugin.
  - `sqldelight`: SQLDelight plugin.
  - `com.google.gms.google-services`: Google Services plugin (commented out in the provided `build.gradle.kts` but still defined here).