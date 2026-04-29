# Requirements Document

## Introduction

Memoriva is an Android memory calendar app that helps users relive past experiences and plan future adventures. It connects with the device gallery to organize photos into a calendar-based memory timeline. Users can tag locations, write reviews of visited places, plan dream trips on a visual board, track trip statistics, share memories with friends, and lock memories as time capsules to open on a future date. The app is implemented in Java using XML layouts, SQLite for local storage, Firebase Authentication, Google Maps SDK, Retrofit for networking, and on-device sensors.

---

## Glossary

- **App**: The Memoriva Android application (package `com.example.memoriva`)
- **User**: The authenticated person using the App
- **Memory**: A user-created entry combining photos, a date, a location, a title, and optional notes
- **Gallery**: The device's local photo storage accessible via Android's MediaStore API
- **Calendar_View**: A monthly/weekly calendar UI that displays Memories on their corresponding dates
- **Location**: A geographic coordinate pair (latitude, longitude) with an optional human-readable place name
- **Place**: A named real-world location (e.g., restaurant, landmark, city) associated with one or more Memories
- **Review**: A user-written text rating (1–5 stars plus optional text) attached to a visited Place
- **Dream_Board**: A visual planning board where the User pins future destinations with PLANNED or WISHLIST status badges and filter chips
- **Trip**: A named collection of Memories grouped by a date range and a primary destination, displayed as a card with cover photo in a grid layout
- **Trip_Stats**: Aggregated analytics derived from a User's Trips (e.g., countries visited, cities visited, total memories, longest trip)
- **Trip_Timeline**: A chronological list of Memory entries within a Trip, grouped by date with location name, place, and city
- **Friend**: Another registered User who has accepted a mutual connection request
- **Feed**: A chronological stream of Memories shared by Friends
- **Auth_Service**: The authentication component responsible for sign-in, sign-up, and session management using Firebase Authentication
- **Photo_Importer**: The component that reads photos from Gallery or captures via Camera and creates Memory candidates
- **Location_Service**: The component that resolves coordinates to place names and provides map rendering via Google Maps SDK and Geocoding API
- **Notification_Service**: The component that delivers on-device push notifications to the User
- **SQLite_DB**: The local SQLite database storing Memories, Trips, Places, Reviews, Friends, and Dream_Board entries with full CRUD operations
- **Splash_Screen**: The initial launch screen displaying the app name, tagline, and an animated loading bar
- **Time_Capsule**: A Memory that has been locked by the User with a future open date and a "Dear Future Self" message, sealed until the open date is reached
- **Camera**: The device camera accessed via Camera Intent for capturing photos within the App
- **Sensor_Manager**: The Android SensorManager component used to listen to hardware sensor events (accelerometer)
- **Retrofit_Client**: The Retrofit HTTP client used for all API calls and JSON parsing
- **Bottom_Nav**: The bottom navigation bar with 4 tabs present on main screens (Home, Map/Explore, Memories/Future, Profile)
- **Year_In_Travel**: A shareable summary card showing the User's travel highlights for the current calendar year

---

## Requirements

### Requirement 1: Splash Screen

**User Story:** As a user, I want to see a branded splash screen when I open the app, so that the app feels polished and loads gracefully.

#### Acceptance Criteria

1. WHEN the App process is started, THE Splash_Screen SHALL display the app name "Memoriva", the tagline "Your Memories. Your Map. Your Story.", and an animated horizontal ProgressBar.
2. WHEN the Splash_Screen loading bar animation completes, THE App SHALL navigate to the Sign In screen if no authenticated session exists, or to the home Map View if a session exists.
3. THE Splash_Screen SHALL use an explicit Intent to transition to the next Activity and SHALL finish itself so the back button does not return to it.
4. THE Splash_Screen SHALL complete its transition within 3 seconds of the App process starting.

---

### Requirement 2: User Authentication

**User Story:** As a new user, I want to sign up with email/password or sign in with Google, so that my memories are saved and synced securely.

#### Acceptance Criteria

1. WHEN the User opens the Sign Up screen, THE Auth_Service SHALL present a form with fields for Full Name, Email, Password, and Confirm Password, plus a "Create Account" button and social sign-in icons.
2. WHEN the User submits the Sign Up form with a valid Full Name, a valid email address, a password of at least 8 characters, and a matching Confirm Password, THE Auth_Service SHALL create a Firebase Authentication account and navigate the User to the home Map View within 3 seconds.
3. IF the User submits the Sign Up form with mismatched Password and Confirm Password values, THEN THE Auth_Service SHALL display a validation error on the Confirm Password field and prevent account creation.
4. IF the User submits the Sign Up form with an email address already registered, THEN THE Auth_Service SHALL display an error message stating the email is already in use.
5. WHEN the User opens the Sign In screen, THE Auth_Service SHALL present an Email field, a Password field, a "Sign In" button, Google Sign-In and Apple Sign-In icons, and a "Forgot Password?" link.
6. WHEN the User submits valid email and password credentials on the Sign In screen, THE Auth_Service SHALL authenticate via Firebase Authentication and navigate the User to the home Map View within 3 seconds.
7. WHEN the User taps the Google Sign-In icon, THE Auth_Service SHALL launch the Google Sign-In OAuth flow and, upon success, authenticate via Firebase Authentication and navigate the User to the home Map View.
8. IF Sign In fails due to incorrect credentials, THEN THE Auth_Service SHALL display a Snackbar with a descriptive error message and allow the User to retry.
9. IF Sign In fails due to a network error, THEN THE Auth_Service SHALL display an AlertDialog describing the failure and offering a retry option.
10. WHEN the User is already signed in and launches the App, THE Auth_Service SHALL restore the session and navigate directly to the home Map View without prompting sign-in again.
11. WHEN the User signs out, THE Auth_Service SHALL clear the local session data and return the User to the Sign In screen via an explicit Intent.

---

### Requirement 3: Forgot Password

**User Story:** As a user, I want to reset my password if I forget it, so that I can regain access to my account.

#### Acceptance Criteria

1. WHEN the User taps the "Forgot Password?" link on the Sign In screen, THE Auth_Service SHALL navigate to a Forgot Password screen via an explicit Intent.
2. WHEN the User enters a registered email address and submits the Forgot Password form, THE Auth_Service SHALL send a password reset email via Firebase Authentication and display a confirmation Toast.
3. IF the User submits the Forgot Password form with an email address not found in Firebase Authentication, THEN THE Auth_Service SHALL display an error message stating no account was found for that email.
4. IF the User submits the Forgot Password form with an empty email field, THEN THE Auth_Service SHALL display a validation error on the email field and prevent submission.

---

### Requirement 4: Bottom Navigation

**User Story:** As a user, I want a persistent bottom navigation bar, so that I can switch between the main sections of the app quickly.

#### Acceptance Criteria

1. THE App SHALL display a Bottom_Nav bar on all main screens containing 4 tabs: Home, Map (or Explore), Memories (or Future), and Profile.
2. WHEN the User taps a Bottom_Nav tab, THE App SHALL navigate to the corresponding Activity via an explicit Intent and highlight the selected tab.
3. WHEN the User is on a main screen and presses the Android back button, THE App SHALL navigate to the Home tab if the current tab is not Home, rather than exiting the App.
4. THE Bottom_Nav SHALL use Material Design BottomNavigationView with labeled icons for each tab.

---

### Requirement 5: Map View

**User Story:** As a user, I want to see my memories on a full-screen map, so that I can visualize where I've been.

#### Acceptance Criteria

1. THE App SHALL provide a Map View Activity displaying a full-screen Google Map with pins for every Memory that has an associated Location.
2. WHEN the User taps a map pin, THE App SHALL display a preview card showing the Memory title, date, and first photo thumbnail.
3. WHEN the User taps the preview card on the map, THE App SHALL navigate to the full Memory detail Activity via an explicit Intent, passing the Memory ID as an Intent extra.
4. THE Map View SHALL display a floating action button (FAB) labelled "Add Trip" that navigates to the Trip creation Activity when tapped.
5. WHEN the User searches for a place name in the Location search field on the Map View, THE Location_Service SHALL return matching place suggestions within 2 seconds using the Geocoding API.
6. IF the device has no network connection during a location search, THEN THE Location_Service SHALL display an offline error message and allow the User to enter coordinates manually.

---

### Requirement 6: Photo Import from Gallery

**User Story:** As a user, I want to import photos from my device gallery, so that I can turn existing photos into memories without re-uploading them manually.

#### Acceptance Criteria

1. WHEN the User grants READ_MEDIA_IMAGES permission, THE Photo_Importer SHALL scan the Gallery and present importable photos grouped by date using Android's MediaStore API.
2. WHEN the User selects one or more photos to import, THE Photo_Importer SHALL create a Memory candidate pre-filled with the photo's EXIF date and GPS coordinates (if available).
3. IF a photo has no embedded GPS data, THEN THE Photo_Importer SHALL allow the User to manually assign a Location before saving the Memory.
4. THE Photo_Importer SHALL deduplicate photos so that the same image is not imported more than once into the same Memory.
5. WHEN photo import completes, THE Photo_Importer SHALL display a Toast confirmation showing the count of successfully imported photos.

---

### Requirement 7: Camera Capture

**User Story:** As a user, I want to capture photos directly within the app, so that I can create memories from new photos without leaving the app.

#### Acceptance Criteria

1. WHEN the User initiates memory creation and selects the camera option, THE App SHALL launch a Camera Intent (ACTION_IMAGE_CAPTURE) to capture a photo.
2. WHEN the Camera Intent returns a result with RESULT_OK, THE Photo_Importer SHALL attach the captured photo to the Memory being created and display a thumbnail preview.
3. IF the Camera Intent returns RESULT_CANCELED, THEN THE App SHALL return the User to the memory creation form without displaying an error.
4. WHEN the User captures a photo via Camera Intent, THE App SHALL save the photo to a file URI in the App's external files directory and pass that URI to the Camera Intent via FileProvider.
5. THE App SHALL request CAMERA permission before launching the Camera Intent and, IF the permission is denied, THEN THE App SHALL display an AlertDialog explaining why the permission is needed.

---

### Requirement 8: Memory Creation and Editing

**User Story:** As a user, I want to create and edit memories with photos, dates, locations, and notes, so that I can capture the full context of an experience.

#### Acceptance Criteria

1. WHEN the User initiates memory creation, THE App SHALL present a form Activity with fields for title (required, max 100 characters), date (required), location (optional), photos (at least 1 required, sourced from Gallery or Camera), and notes (optional, max 1000 characters).
2. WHEN the User submits the memory creation form with all required fields filled, THE App SHALL save the Memory to SQLite_DB and display it on the Calendar_View on its corresponding date within 2 seconds.
3. IF the User submits the form with the title field empty, THEN THE App SHALL display a validation error on the title field using a Snackbar and prevent saving.
4. IF the User submits the form with no photos attached, THEN THE App SHALL display a validation error using a Snackbar and prevent saving.
5. WHEN the User edits an existing Memory and saves changes, THE App SHALL update the Memory in SQLite_DB and reflect the changes in the Calendar_View immediately.
6. WHEN the User deletes a Memory, THE App SHALL remove it from SQLite_DB and the Calendar_View after the User confirms the deletion in an AlertDialog.
7. THE App SHALL pass the Memory ID between Activities using Intent extras when navigating to the Memory edit or detail Activity.

---

### Requirement 9: SQLite Data Storage

**User Story:** As a developer, I want all core data stored in a local SQLite database, so that the app satisfies the course requirement for on-device relational storage with full CRUD operations.

#### Acceptance Criteria

1. THE SQLite_DB SHALL contain tables for: Memories, Trips, Places, Reviews, DreamDestinations, Friends, and TimeCapsules, each with a primary key and appropriate columns.
2. THE App SHALL perform Create, Read, Update, and Delete (CRUD) operations on each SQLite_DB table through a dedicated database helper class extending SQLiteOpenHelper.
3. WHEN the App is first installed, THE SQLite_DB SHALL be created with the correct schema via the onCreate callback of the SQLiteOpenHelper.
4. WHEN the App is upgraded to a new version with schema changes, THE SQLite_DB SHALL migrate the schema via the onUpgrade callback without data loss.
5. THE App SHALL use parameterized queries for all SQLite_DB operations to prevent SQL injection.

---

### Requirement 10: Calendar View of Memories

**User Story:** As a user, I want to see my memories organized on a calendar, so that I can easily browse and relive experiences by date.

#### Acceptance Criteria

1. THE App SHALL display a monthly Calendar_View as the home screen after sign-in, with each date cell showing a thumbnail of the first photo of any Memory on that date.
2. WHEN the User taps a date cell that contains one or more Memories, THE App SHALL open a detail list Activity showing all Memories for that date, passing the selected date as an Intent extra.
3. WHEN the User swipes left or right on the Calendar_View, THE App SHALL navigate to the next or previous month respectively.
4. WHILE the Calendar_View is loading Memory thumbnails, THE App SHALL display a ProgressBar placeholder in each date cell to prevent layout shift.
5. WHEN the User switches to weekly view, THE App SHALL display a 7-day strip with Memory thumbnails and allow horizontal scrolling through weeks.

---

### Requirement 11: Location Tagging

**User Story:** As a user, I want to tag locations on my memories, so that I can record where each experience happened.

#### Acceptance Criteria

1. WHEN the User assigns a Location to a Memory, THE Location_Service SHALL use the Android Geocoder to resolve the coordinates to a human-readable place name and display it on the Memory detail screen.
2. WHEN the User searches for a place name in the Location search field, THE Location_Service SHALL return matching place suggestions within 2 seconds using the Geocoding API over the device's network connection.
3. IF the device has no network connection during a location search, THEN THE Location_Service SHALL display an offline error message and allow the User to enter coordinates manually.
4. THE App SHALL store the resolved place name and coordinates in SQLite_DB as part of the Memory record.

---

### Requirement 12: Place Reviews

**User Story:** As a user, I want to write reviews for places I've visited, so that I can record my impressions and help friends decide where to go.

#### Acceptance Criteria

1. WHEN the User opens a Place detail Activity, THE App SHALL display all Reviews associated with that Place from SQLite_DB, sorted by most recent first, using a RecyclerView with a ViewHolder pattern.
2. WHEN the User submits a Review with a star rating between 1 and 5 and optional text (max 500 characters), THE App SHALL save the Review to SQLite_DB and display it at the top of the Place's review list.
3. IF the User submits a Review with a star rating outside the range 1–5, THEN THE App SHALL display a validation error using a Toast and prevent saving.
4. WHEN the User edits their own Review and saves, THE App SHALL update the Review in SQLite_DB and refresh the RecyclerView.
5. WHEN the User deletes their own Review, THE App SHALL remove it from SQLite_DB and the RecyclerView after confirmation via an AlertDialog.
6. THE App SHALL display the average star rating for each Place, calculated from all Reviews in SQLite_DB associated with that Place.

---

### Requirement 13: Dream Board for Future Trip Planning

**User Story:** As a user, I want a visual dream board where I can pin future destinations with status badges, so that I can plan and visualize trips I want to take.

#### Acceptance Criteria

1. THE App SHALL provide a Dream_Board Activity accessible from the Bottom_Nav, displaying destination cards in a grid or masonry layout using a RecyclerView.
2. WHEN the User adds a destination to the Dream_Board, THE App SHALL create a destination card showing the place name, an optional cover image, an optional expected date, and a status badge of either PLANNED or WISHLIST.
3. THE Dream_Board Activity SHALL display filter chips (e.g., "All Dreams", "Europe 2026", "Asia 2027") that filter the displayed destination cards by status or label when tapped.
4. WHEN the User taps a destination card on the Dream_Board, THE App SHALL open a detail Activity where the User can add notes (max 500 characters), a budget estimate, and a checklist of items to prepare.
5. WHEN the User marks a Dream_Board destination as visited, THE App SHALL prompt the User via an AlertDialog to create a new Trip linked to that destination.
6. THE Dream_Board Activity SHALL display a FAB labelled "+ Add Dream Trip" that opens the destination creation form when tapped.
7. IF the User attempts to add a duplicate destination (same place name already on the board), THEN THE App SHALL display a warning using a Snackbar and ask the User to confirm before adding.
8. THE App SHALL persist all Dream_Board entries in SQLite_DB with CRUD operations.

---

### Requirement 14: My Trips

**User Story:** As a user, I want to group memories into trips and view them in a grid, so that I can organize and revisit complete travel experiences.

#### Acceptance Criteria

1. THE App SHALL provide a My Trips Activity displaying Trip cards in a grid layout using a RecyclerView, each card showing a cover photo, destination name, short description, and date tag.
2. WHEN the User creates a Trip with a name (required, max 100 characters), start date, end date, and primary destination, THE App SHALL save the Trip to SQLite_DB and display it in the My Trips grid.
3. IF the User sets a Trip end date that is earlier than the start date, THEN THE App SHALL display a validation error using a Snackbar and prevent saving.
4. WHEN the User adds a Memory to a Trip, THE App SHALL display that Memory in the Trip's Trip_Timeline view grouped by date.
5. THE App SHALL allow the User to add a cover photo to a Trip, selected from the Memories within that Trip.
6. THE My Trips Activity SHALL display a "+ New Trip" card at the end of the grid that opens the Trip creation form when tapped.
7. THE App SHALL pass the Trip ID to the Trip detail Activity via an Intent extra when the User taps a Trip card.

---

### Requirement 15: Trip Timeline

**User Story:** As a user, I want to view a chronological timeline of memories within a trip, so that I can relive the full journey in order.

#### Acceptance Criteria

1. THE App SHALL provide a Trip_Timeline Activity that displays a trip header with stats (Days count, Places count, Photos count) and a chronological list of Memory entries grouped by date.
2. WHEN the User opens the Trip_Timeline Activity, THE App SHALL load Memory entries from SQLite_DB for the selected Trip and display them in a RecyclerView sorted by date ascending.
3. WHEN the User taps a Memory entry in the Trip_Timeline, THE App SHALL navigate to the Memory detail Activity via an explicit Intent, passing the Memory ID as an Intent extra.
4. THE Trip_Timeline Activity SHALL display each Memory entry with the location name, place, and city derived from the Memory's Location data.
5. THE Trip_Timeline Activity SHALL receive the Trip ID from the launching Activity via an Intent extra.

---

### Requirement 16: Trip Detail and Sharing

**User Story:** As a user, I want to view a trip's full detail and share it, so that I can show friends my travel experiences.

#### Acceptance Criteria

1. THE App SHALL provide a Trip detail Activity displaying a hero cover photo, trip name, date range, quote/notes text, a Gallery section with photo thumbnails and a "View all" link, a tagged places list, and action buttons "Share Memory" and "Add to Story".
2. WHEN the User taps "Share Memory", THE App SHALL launch an implicit Intent with ACTION_SEND to share the trip name, date range, and a selected photo to any compatible app (e.g., messaging, social media).
3. WHEN the User taps "Add to Story", THE App SHALL launch an implicit Intent to share the trip content to a compatible social story feature.
4. WHEN the User taps "View all" in the Gallery section, THE App SHALL navigate to a full-screen photo gallery Activity for that Trip.
5. THE App SHALL pass the Trip ID to the Trip detail Activity via an Intent extra.

---

### Requirement 17: Time Capsule

**User Story:** As a user, I want to lock a memory as a time capsule with a future open date and a message to my future self, so that I can rediscover it at a meaningful moment.

#### Acceptance Criteria

1. THE App SHALL provide a Time Capsule screen accessible from the Memory detail Activity, displaying the Memory's location, a "Lock this Memory" Switch, an open date picker, a "Dear Future Self" message EditText (max 500 characters), and a "Seal Capsule" button.
2. WHEN the User enables the "Lock this Memory" Switch and taps "Seal Capsule" with a future open date selected, THE App SHALL save the Time_Capsule record to SQLite_DB with the Memory ID, open date, and message, and display a confirmation Toast.
3. IF the User taps "Seal Capsule" without selecting an open date, THEN THE App SHALL display a validation error using a Snackbar and prevent sealing.
4. IF the User taps "Seal Capsule" with an open date that is not in the future, THEN THE App SHALL display a validation error using a Snackbar and prevent sealing.
5. WHILE a Time_Capsule's open date has not yet been reached, THE App SHALL display the Memory as locked and prevent the User from viewing its content.
6. WHEN the current date reaches or passes a Time_Capsule's open date, THE Notification_Service SHALL deliver an on-device notification informing the User that a Time Capsule is ready to open.
7. WHEN the User opens a Time_Capsule after its open date, THE App SHALL display the "Dear Future Self" message and the locked Memory's full content.

---

### Requirement 18: Travel Stats and Year in Travel

**User Story:** As a user, I want to see all-time travel statistics and a shareable Year in Travel card, so that I can celebrate and share my travel achievements.

#### Acceptance Criteria

1. THE App SHALL provide a Travel Stats Activity displaying a profile header and an "All-Time Journey" stats section showing: Cities Visited count, Countries count, Most Visited Place name, Longest Trip duration in days, and Total Memories count, all derived from SQLite_DB.
2. THE Travel Stats Activity SHALL display a "Your Year in Travel" highlight card summarizing the current calendar year's travel activity.
3. WHEN the User taps "Share My Travel Year", THE App SHALL launch an implicit Intent with ACTION_SEND to share the Year_In_Travel summary text and stats to any compatible app.
4. WHEN new Memories or Trips are added, THE App SHALL update Travel Stats values within 5 seconds without requiring a manual refresh.
5. WHEN the User taps a statistic entry in Travel Stats, THE App SHALL navigate to the corresponding filtered list of Trips or Memories via an explicit Intent.

---

### Requirement 19: Profile Screen

**User Story:** As a user, I want a profile screen showing my stats and quick-access cards, so that I can see my activity summary and navigate to key features.

#### Acceptance Criteria

1. THE App SHALL provide a Profile Activity displaying the User's avatar, username (@handle), bio, and a stats row showing Trips count, Friends count, and Memories count sourced from SQLite_DB.
2. THE Profile Activity SHALL display quick-access cards for: Add Friends, My Trips, Future Trips, and Trip Timeline, each navigating to the corresponding Activity via an explicit Intent when tapped.
3. WHEN the User updates their profile (avatar, username, or bio), THE App SHALL save the changes via Firebase Authentication and to SQLite_DB and reflect them on the Profile Activity immediately.
4. THE Profile Activity SHALL be accessible from the Bottom_Nav.

---

### Requirement 20: Friends Discovery and Suggested Users

**User Story:** As a user, I want to discover and add friends by searching usernames and seeing suggested users, so that I can build my social network within the app.

#### Acceptance Criteria

1. THE App SHALL provide a Friends/Discover Activity with a search field for finding users by username, and a "Suggested for You" RecyclerView list showing avatar, @handle, display name, and an Add/Added button for each suggested user.
2. WHEN the User types in the username search field, THE App SHALL query the backend via Retrofit_Client and display matching user results within 3 seconds.
3. WHEN the User taps the "Add" button next to a suggested user, THE App SHALL send a friend request and change the button label to "Added" using a ToggleButton or state change.
4. WHEN the User sends a friend request and the recipient accepts, THE App SHALL add both Users to each other's Friends list in SQLite_DB.
5. IF the recipient declines the friend request, THEN THE App SHALL remove the pending request and notify the sender with a Toast status update.
6. WHEN the User removes a Friend, THE App SHALL remove that Friend from the User's Friends list in SQLite_DB and stop showing that Friend's Memories in the User's Feed.

---

### Requirement 21: Sensor Integration — Shake to Shuffle

**User Story:** As a user, I want to shake my phone to shuffle through random memories, so that I can rediscover past experiences in a fun and spontaneous way.

#### Acceptance Criteria

1. THE App SHALL register an accelerometer sensor listener via Sensor_Manager and SensorEventListener on the Memory browse screen.
2. WHEN the Sensor_Manager detects a shake gesture (acceleration exceeding 15 m/s² on any axis for at least 2 consecutive sensor events), THE App SHALL navigate to a randomly selected Memory detail Activity.
3. WHEN the Memory browse Activity is paused or stopped, THE App SHALL unregister the SensorEventListener to conserve battery.
4. WHEN the Memory browse Activity is resumed, THE App SHALL re-register the SensorEventListener.
5. WHERE the device does not have an accelerometer sensor, THE App SHALL not register the SensorEventListener and SHALL not display the shake-to-shuffle feature hint.

---

### Requirement 22: Networking with Retrofit

**User Story:** As a developer, I want all API calls made through Retrofit, so that the app satisfies the course requirement for structured HTTP networking with JSON parsing.

#### Acceptance Criteria

1. THE Retrofit_Client SHALL be configured with a base URL and a Gson converter factory for JSON parsing.
2. WHEN the App makes an API call via Retrofit_Client, THE Retrofit_Client SHALL execute the call asynchronously on a background thread and deliver the result on the main thread via a Callback.
3. IF an API call via Retrofit_Client fails due to a network error, THEN THE App SHALL display a Snackbar with a descriptive error message.
4. IF an API call via Retrofit_Client returns an HTTP error response (4xx or 5xx), THEN THE App SHALL display a Toast with the error code and message.
5. THE Retrofit_Client SHALL be instantiated as a singleton to avoid redundant HTTP client creation.

---

### Requirement 23: On-Device Notifications

**User Story:** As a user, I want to receive on-device notifications for memories and time capsules, so that I am reminded of meaningful moments.

#### Acceptance Criteria

1. WHEN the current date matches the date of one or more Memories from a previous year, THE Notification_Service SHALL deliver an "On This Day" notification to the User once per day at a time configured by the User (default: 09:00 local time).
2. WHEN the User taps the "On This Day" notification, THE App SHALL open a dedicated Activity showing all Memories from that same calendar date across all past years.
3. WHERE the User has disabled "On This Day" notifications in App settings, THE Notification_Service SHALL not deliver those notifications.
4. IF there are no Memories matching the current date from any previous year, THEN THE Notification_Service SHALL not deliver an "On This Day" notification for that day.
5. WHEN a Time_Capsule's open date is reached, THE Notification_Service SHALL deliver a notification informing the User that the Time Capsule is ready to open.
6. WHEN a Friend publishes a new shared Memory, THE Notification_Service SHALL deliver an on-device notification to the User within 60 seconds.

---

### Requirement 24: SMS/Email Communication

**User Story:** As a user, I want to share memories via SMS or email directly from the app, so that I can send experiences to people who may not use Memoriva.

#### Acceptance Criteria

1. WHEN the User selects "Share via Email" on a Memory or Trip detail screen, THE App SHALL launch an implicit Intent with ACTION_SENDTO and a mailto URI pre-populated with the Memory or Trip title and a link or summary in the email body.
2. WHEN the User selects "Share via SMS" on a Memory or Trip detail screen, THE App SHALL launch an implicit Intent with ACTION_SENDTO and an smsto URI pre-populated with a summary of the Memory or Trip.
3. IF no email client app is installed on the device, THEN THE App SHALL display a Toast informing the User that no email app was found.
4. IF no SMS app is installed on the device, THEN THE App SHALL display a Toast informing the User that no SMS app was found.

---

### Requirement 25: Menus

**User Story:** As a developer, I want the app to use Android menu components, so that the app satisfies the course requirement for options, context, and popup menus.

#### Acceptance Criteria

1. THE App SHALL implement an Options Menu on the Memory list and Trip list Activities, providing actions such as Sort, Filter, and Settings.
2. WHEN the User long-presses a Memory card in a list, THE App SHALL display a Context Menu with options including Edit, Delete, and Share.
3. WHEN the User taps an overflow icon on a destination card in the Dream_Board, THE App SHALL display a Popup Menu with options including Edit, Delete, and Mark as Visited.
4. WHEN the User selects a menu item, THE App SHALL perform the corresponding action and display a Toast or Snackbar confirming the action.

---

### Requirement 26: Offline Support

**User Story:** As a user, I want to browse my memories without an internet connection, so that I can access my content while traveling or in areas with poor connectivity.

#### Acceptance Criteria

1. WHILE the device has no network connection, THE App SHALL display all locally stored Memories, Calendar_View, and Trip data from SQLite_DB without error.
2. WHILE the device has no network connection, THE App SHALL allow the User to create and edit Memories in SQLite_DB, queuing changes for sync when connectivity is restored.
3. WHEN network connectivity is restored after an offline period, THE App SHALL automatically sync queued changes to the backend via Retrofit_Client within 30 seconds.
4. WHILE the device has no network connection, THE App SHALL display an offline indicator in the Bottom_Nav area.
5. IF a sync conflict is detected (the same Memory was edited both locally and remotely), THEN THE App SHALL present the User with both versions in an AlertDialog and ask the User to choose which version to keep.
