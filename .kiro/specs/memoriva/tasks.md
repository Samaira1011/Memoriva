# Implementation Plan: Memoriva Android App

## Overview

This implementation plan breaks down the Memoriva Android app into actionable coding tasks organized by feature and component. The app is built in Java using Android SDK, SQLite for local storage, Firebase Authentication, Google Maps SDK, Retrofit for networking, and Material Design components. Tasks are ordered by dependency: setup → database → models → networking → authentication → UI → features → polish → testing.

## Tasks

- [x] 1. Project Setup & Configuration
  - Add Firebase dependencies (firebase-auth, firebase-bom) to build.gradle.kts
  - Add Retrofit and Gson dependencies for HTTP networking
  - Add Google Play Services dependencies (maps, location)
  - Add Material Design and RecyclerView dependencies
  - Add AndroidX dependencies (appcompat, constraintlayout, cardview)
  - Configure AndroidManifest.xml with required permissions (CAMERA, READ_MEDIA_IMAGES, ACCESS_FINE_LOCATION, INTERNET, ACCESS_NETWORK_STATE)
  - Create FileProvider configuration in res/xml/file_paths.xml for camera intent
  - Set up app theme colors and styles in res/values/colors.xml and res/values/themes.xml
  - Create app icon and branding assets
  - _Requirements: 1, 2, 7, 21, 22_

- [x] 2. Database Layer - Schema & Helper
  - Create MemorivaDbHelper class extending SQLiteOpenHelper
  - Implement database schema with 8 tables: memories, trips, places, reviews, dream_destinations, friends, time_capsules, users
  - Add primary keys and foreign key relationships for all tables
  - Create database indexes on frequently queried columns (user_id, trip_id, date, location)
  - Implement onCreate() callback to create all tables on first install
  - Implement onUpgrade() callback for schema migrations
  - _Requirements: 9_

- [x] 3. Database Layer - DAO Classes
  - Create MemoryDao class with CRUD operations (insert, query, update, delete)
  - Create TripDao class with CRUD operations
  - Create PlaceDao class with CRUD operations
  - Create ReviewDao class with CRUD operations
  - Create DreamDestinationDao class with CRUD operations
  - Create FriendDao class with CRUD operations
  - Create TimeCapsuleDao class with CRUD operations
  - Create UserDao class with CRUD operations
  - Implement parameterized queries for all DAO operations to prevent SQL injection
  - _Requirements: 9_

- [x] 4. Data Models - POJOs
  - Create User.java POJO with fields: userId, email, fullName, username, bio, avatar, createdAt
  - Create Memory.java POJO with fields: memoryId, userId, title, date, location, notes, photoPath, createdAt, updatedAt
  - Create Trip.java POJO with fields: tripId, userId, name, startDate, endDate, destination, description, coverPhotoPath, createdAt
  - Create Place.java POJO with fields: placeId, name, latitude, longitude, address, city, country
  - Create Review.java POJO with fields: reviewId, userId, placeId, rating, text, createdAt
  - Create DreamDestination.java POJO with fields: dreamId, userId, placeName, status (PLANNED/WISHLIST), expectedDate, notes, budget, coverImagePath
  - Create Friend.java POJO with fields: friendshipId, userId, friendId, status (PENDING/ACCEPTED), createdAt
  - Create TimeCapsule.java POJO with fields: capsuleId, memoryId, openDate, message, isOpened
  - _Requirements: 8, 13, 14, 17_

- [x] 5. Networking - Retrofit Setup
  - Create MemorivaApiService interface with methods for API endpoints
  - Create Retrofit singleton client with base URL and Gson converter factory
  - Implement API methods: signUp, signIn, searchUsers, geocodeLocation, reverseGeocode, sendFriendRequest, getFeed, getMemories, getTrips
  - Create request/response model classes for API calls
  - Implement error handling for network failures and HTTP errors
  - _Requirements: 22_

- [x] 6. Authentication - Firebase Setup
  - Create AuthManager helper class for Firebase Authentication operations
  - Implement Firebase Authentication initialization in Application class
  - Implement email/password sign-up with validation (email format, password length ≥ 8)
  - Implement email/password sign-in with error handling
  - Implement Google Sign-In configuration and integration
  - Implement session persistence using SharedPreferences
  - Implement sign-out functionality with session clearing
  - _Requirements: 2, 3_

- [x] 7. Authentication - Splash Activity
  - Create SplashActivity with animated ProgressBar (3-second duration)
  - Display app name "Memoriva" and tagline "Your Memories. Your Map. Your Story."
  - Implement navigation logic: if session exists → MapActivity, else → SignInActivity
  - Use explicit Intent for navigation and finish() to prevent back navigation
  - _Requirements: 1_

- [x] 8. Authentication - Sign Up Activity
  - Create SignUpActivity with form fields: Full Name, Email, Password, Confirm Password
  - Implement form validation: required fields, email format, password length ≥ 8, password match
  - Implement "Create Account" button with Firebase Authentication integration
  - Display validation errors on respective fields using Snackbar
  - Handle duplicate email error from Firebase
  - Navigate to MapActivity on successful sign-up
  - _Requirements: 2_

- [x] 9. Authentication - Sign In Activity
  - Create SignInActivity with form fields: Email, Password
  - Implement "Sign In" button with Firebase Authentication
  - Implement Google Sign-In button with OAuth flow
  - Implement "Forgot Password?" link navigation to ForgotPasswordActivity
  - Display error messages for incorrect credentials and network errors
  - Navigate to MapActivity on successful sign-in
  - _Requirements: 2_

- [x] 10. Authentication - Forgot Password Activity
  - Create ForgotPasswordActivity with email input field
  - Implement form validation for email field
  - Implement "Reset Password" button with Firebase password reset email
  - Display confirmation Toast on successful email send
  - Display error message for unregistered email
  - _Requirements: 3_

- [x] 11. Bottom Navigation & Main Activities Setup
  - Create BottomNavigationView in a base layout used by main activities
  - Implement 4 navigation tabs: Home (Map), Explore (Calendar), Future (Dream Board), Profile
  - Create MapActivity as home screen with Google Maps integration
  - Create CalendarActivity with monthly calendar view
  - Create ProfileActivity with user stats
  - Create FriendsDiscoverActivity with user search
  - Implement navigation between activities via explicit Intents
  - Implement back stack management: back button on non-Home tab navigates to Home
  - _Requirements: 4_

- [x] 12. Map Activity - Google Maps Integration
  - Create MapActivity extending AppCompatActivity with Google Maps fragment
  - Implement GoogleMap initialization and setup
  - Implement location permission handling (ACCESS_FINE_LOCATION)
  - Create map pins for all memories with associated locations
  - Implement map pin click listener to show memory preview card
  - Implement preview card click to navigate to MemoryDetailActivity
  - Implement location search field with Geocoding API integration
  - Display search results as suggestions within 2 seconds
  - Implement offline error handling for location search
  - Add FAB "Add Trip" button navigating to TripCreateEditActivity
  - _Requirements: 5, 11_

- [x] 13. Calendar Activity - Monthly Calendar View
  - Create CalendarActivity with monthly calendar grid layout
  - Implement calendar navigation (swipe left/right for previous/next month)
  - Load memory thumbnails from SQLite for each date
  - Display thumbnail in date cell or placeholder ProgressBar while loading
  - Implement date cell click to navigate to MemoryListActivity for that date
  - Implement weekly view toggle with 7-day horizontal strip
  - Pass selected date to MemoryListActivity via Intent extra
  - _Requirements: 10_

- [x] 14. Memory Management - Create/Edit Activity
  - Create MemoryCreateEditActivity with form fields: title, date, location, photos, notes
  - Implement form validation: title required (max 100 chars), at least 1 photo required, notes max 1000 chars
  - Implement photo import from Gallery via MediaStore API
  - Implement camera capture via Camera Intent with FileProvider
  - Display photo thumbnails in preview area
  - Implement location search and assignment
  - Implement save button with SQLite insert/update via MemoryDao
  - Display validation errors using Snackbar
  - Navigate back to Calendar or Memory list on successful save
  - Pass Memory ID via Intent extra for edit mode
  - _Requirements: 6, 7, 8_

- [x] 15. Memory Management - Photo Import from Gallery
  - Create PhotoImporter helper class using MediaStore API
  - Implement gallery photo scanning grouped by EXIF date
  - Implement photo selection and Memory candidate creation
  - Extract EXIF GPS coordinates if available
  - Implement manual location assignment for photos without GPS data
  - Implement photo deduplication logic
  - Display import completion Toast with count
  - _Requirements: 6_

- [x] 16. Memory Management - Camera Capture
  - Implement Camera Intent launch (ACTION_IMAGE_CAPTURE) in MemoryCreateEditActivity
  - Implement FileProvider configuration for camera file URI
  - Handle Camera Intent result (RESULT_OK, RESULT_CANCELED)
  - Save captured photo to app's external files directory
  - Display photo thumbnail preview on successful capture
  - Request CAMERA permission before launching Camera Intent
  - Display AlertDialog explaining permission need if denied
  - _Requirements: 7_

- [ ] 17. Memory Management - Detail & List Activities
  - Create MemoryDetailActivity displaying: title, date, location, photos, notes, reviews
  - Create MemoryListActivity for date-based view with RecyclerView
  - Create MemoryAdapter for RecyclerView with ViewHolder pattern
  - Implement memory CRUD operations via MemoryDao
  - Implement delete confirmation via AlertDialog
  - Implement edit button navigation to MemoryCreateEditActivity
  - Implement share buttons for email and SMS
  - Pass Memory ID via Intent extra between activities
  - _Requirements: 8, 24_

- [x] 18. Trip Management - Create/Edit Activity
  - Create TripCreateEditActivity with form fields: name, start date, end date, destination, description, cover photo
  - Implement form validation: name required (max 100 chars), end date ≥ start date
  - Implement date picker for start and end dates
  - Implement cover photo selection from Memory photos
  - Implement save button with SQLite insert/update via TripDao
  - Display validation errors using Snackbar
  - Navigate back to MyTripsActivity on successful save
  - Pass Trip ID via Intent extra for edit mode
  - _Requirements: 14_

- [x] 19. Trip Management - My Trips Activity
  - Create MyTripsActivity with RecyclerView grid layout
  - Create TripAdapter for RecyclerView with ViewHolder pattern
  - Load trips from SQLite via TripDao
  - Display trip cards with: cover photo, destination, description, date tag
  - Implement trip card click to navigate to TripDetailActivity
  - Add "+ New Trip" card at end of grid navigating to TripCreateEditActivity
  - Implement trip CRUD operations via TripDao
  - Pass Trip ID via Intent extra to detail activity
  - _Requirements: 14_

- [x] 20. Trip Management - Trip Timeline Activity
  - Create TripTimelineActivity displaying trip header with stats (Days, Places, Photos count)
  - Load memories for trip from SQLite grouped by date
  - Display chronological list of memories in RecyclerView sorted by date ascending
  - Show location name, place, and city for each memory
  - Implement memory click to navigate to MemoryDetailActivity
  - Receive Trip ID via Intent extra from launching activity
  - _Requirements: 15_

- [ ] 21. Trip Management - Trip Detail & Sharing
  - Create TripDetailActivity with hero cover photo, trip name, date range, notes
  - Display Gallery section with photo thumbnails and "View all" link
  - Display tagged places list
  - Implement "Share Memory" button with implicit Intent (ACTION_SEND)
  - Implement "Add to Story" button with implicit Intent for social sharing
  - Implement "View all" link to full-screen photo gallery
  - Pass Trip ID via Intent extra from MyTripsActivity
  - _Requirements: 16_

- [ ] 22. Dream Board - Activity & Layout
  - Create DreamBoardActivity with RecyclerView grid/masonry layout
  - Create DreamDestinationAdapter for RecyclerView with ViewHolder pattern
  - Load dream destinations from SQLite via DreamDestinationDao
  - Display destination cards with: place name, cover image, expected date, status badge (PLANNED/WISHLIST)
  - Implement destination card click to navigate to DreamDestinationDetailActivity
  - Add "+ Add Dream Trip" FAB navigating to DreamDestinationCreateEditActivity
  - Implement filter chips (All, PLANNED, WISHLIST, by year) with filtering logic
  - Pass Dream Destination ID via Intent extra to detail activity
  - _Requirements: 13_

- [x] 23. Dream Board - Create/Edit Activity
  - Create DreamDestinationCreateEditActivity with form fields: place name, status, expected date, notes, budget, cover image
  - Implement form validation: place name required, no duplicate destinations
  - Implement date picker for expected date
  - Implement cover image selection from gallery
  - Implement save button with SQLite insert/update via DreamDestinationDao
  - Display duplicate destination warning via Snackbar
  - Navigate back to DreamBoardActivity on successful save
  - Pass Dream Destination ID via Intent extra for edit mode
  - _Requirements: 13_

- [ ] 24. Dream Board - Detail Activity & Visited Conversion
  - Create DreamDestinationDetailActivity displaying: place name, cover image, expected date, notes, budget, checklist
  - Implement checklist EditText for preparation items (max 500 chars)
  - Implement "Mark as Visited" button with AlertDialog confirmation
  - On visited confirmation, prompt user to create new Trip linked to destination
  - Implement edit button navigation to DreamDestinationCreateEditActivity
  - Implement delete button with confirmation AlertDialog
  - _Requirements: 13_

- [x] 25. Location Services - Geocoding Integration
  - Create LocationService helper class using Android Geocoder
  - Implement forward geocoding: place name → coordinates
  - Implement reverse geocoding: coordinates → place name
  - Implement location search with Geocoding API returning suggestions within 2 seconds
  - Implement offline error handling with manual coordinate entry fallback
  - Implement location permission handling (ACCESS_FINE_LOCATION)
  - Store resolved place names and coordinates in SQLite as part of Memory records
  - _Requirements: 5, 11_

- [ ] 26. Reviews & Places - Place Detail Activity
  - Create PlaceDetailActivity displaying place name, address, average rating
  - Load reviews from SQLite via ReviewDao for the place
  - Display reviews in RecyclerView sorted by most recent first
  - Show average star rating calculated from all reviews
  - Implement review card click to navigate to ReviewCreateEditActivity
  - Implement "Add Review" FAB navigating to ReviewCreateEditActivity
  - Receive Place ID via Intent extra from launching activity
  - _Requirements: 12_

- [ ] 27. Reviews & Places - Review Create/Edit Activity
  - Create ReviewCreateEditActivity with form fields: star rating (1-5), text (max 500 chars)
  - Implement form validation: rating 1-5, text max 500 chars
  - Implement star rating picker (visual 1-5 star selector)
  - Implement save button with SQLite insert/update via ReviewDao
  - Display validation errors using Toast
  - Navigate back to PlaceDetailActivity on successful save
  - Pass Review ID via Intent extra for edit mode
  - _Requirements: 12_

- [ ] 28. Reviews & Places - Review Adapter & CRUD
  - Create ReviewAdapter for RecyclerView with ViewHolder pattern
  - Display review: author name, star rating, text, date
  - Implement review edit button navigation to ReviewCreateEditActivity
  - Implement review delete button with AlertDialog confirmation
  - Implement average rating calculation from all reviews in PlaceDetailActivity
  - Implement place CRUD operations via PlaceDao
  - _Requirements: 12_

- [ ] 29. Time Capsule - Activity & Locking Logic
  - Create TimeCapsuleActivity accessible from MemoryDetailActivity
  - Display Memory location, "Lock this Memory" Switch, open date picker, "Dear Future Self" message EditText (max 500 chars)
  - Implement "Seal Capsule" button with validation: future date required
  - Save TimeCapsule record to SQLite via TimeCapsuleDao with Memory ID, open date, message
  - Display confirmation Toast on successful seal
  - Display validation errors using Snackbar for missing/invalid date
  - Prevent viewing locked memory content until open date reached
  - _Requirements: 17_

- [ ] 30. Time Capsule - Notification & Opening
  - Implement time capsule notification trigger when current date ≥ open date
  - Implement TimeCapsuleActivity to display "Dear Future Self" message when opened
  - Display locked memory's full content after opening
  - Update TimeCapsule record in SQLite marking as isOpened = true
  - Implement time capsule CRUD operations via TimeCapsuleDao
  - _Requirements: 17, 23_

- [ ] 31. Travel Stats - Stats Calculation & Display
  - Create TravelStatsActivity displaying profile header and "All-Time Journey" stats section
  - Implement stats calculation from SQLite: Cities Visited count, Countries count, Most Visited Place, Longest Trip duration, Total Memories count
  - Display "Your Year in Travel" highlight card for current calendar year
  - Implement tap-to-filter functionality: clicking stat navigates to filtered Trips/Memories list
  - Implement stats auto-update within 5 seconds when new Memories/Trips added
  - _Requirements: 18_

- [x] 32. Travel Stats - Year in Travel Sharing
  - Implement "Share My Travel Year" button with implicit Intent (ACTION_SEND)
  - Share Year_In_Travel summary text and stats to compatible apps
  - Format shareable content with travel highlights and statistics
  - _Requirements: 18_

- [x] 33. Profile Activity - User Stats & Quick Access
  - Create ProfileActivity displaying: user avatar, username (@handle), bio, stats row (Trips, Friends, Memories count)
  - Load user data from SharedPreferences and SQLite
  - Display quick-access cards: Add Friends, My Trips, Future Trips, Trip Timeline
  - Implement card clicks to navigate to corresponding Activities via explicit Intents
  - Implement profile edit button to update avatar, username, bio
  - Save profile changes to Firebase Authentication and SQLite
  - Implement sign-out button with session clearing
  - _Requirements: 19_

- [ ] 34. Friends Discovery - Search & Suggested Users
  - Create FriendsDiscoverActivity with username search field and "Suggested for You" RecyclerView
  - Implement search field with Retrofit API query returning results within 3 seconds
  - Display suggested users: avatar, @handle, display name, Add/Added button
  - Create UserAdapter for RecyclerView with ViewHolder pattern
  - Implement Add button to send friend request via Retrofit API
  - Change button label to "Added" on successful request
  - _Requirements: 20_

- [ ] 35. Friends Management - Friend Request & Acceptance
  - Implement friend request sending via Retrofit API
  - Implement friend request acceptance/decline logic
  - Add accepted friends to Friends list in SQLite via FriendDao
  - Remove pending requests on decline
  - Display Toast status updates for request actions
  - Implement friend removal from Friends list in SQLite
  - Stop showing removed friend's memories in Feed
  - _Requirements: 20_

- [x] 36. Sensor Integration - Shake Detection
  - Create ShakeDetector class implementing SensorEventListener
  - Register accelerometer sensor listener on Memory browse screen
  - Implement shake detection: acceleration > 15 m/s² on any axis for ≥ 2 consecutive events
  - Navigate to random Memory detail Activity on shake detection
  - Unregister sensor listener on Activity pause to conserve battery
  - Re-register sensor listener on Activity resume
  - Handle devices without accelerometer sensor gracefully
  - _Requirements: 21_

- [x] 37. Menus - Options Menu
  - Create options menu for MemoryListActivity with Sort, Filter, Settings actions
  - Create options menu for MyTripsActivity with Sort, Filter, Settings actions
  - Implement menu item click handlers with corresponding actions
  - Display Toast/Snackbar confirming menu actions
  - _Requirements: 25_

- [ ] 38. Menus - Context & Popup Menus
  - Implement context menu for Memory cards (long-press) with Edit, Delete, Share options
  - Implement popup menu for Dream Board destination cards with Edit, Delete, Mark as Visited options
  - Implement menu item click handlers with corresponding actions
  - Display Toast/Snackbar confirming menu actions
  - _Requirements: 25_

- [ ] 39. Sharing & Communication - Email & SMS
  - Implement email sharing via implicit Intent (ACTION_SENDTO) with mailto URI
  - Implement SMS sharing via implicit Intent (ACTION_SENDTO) with smsto URI
  - Pre-populate email body with Memory/Trip title and summary
  - Pre-populate SMS body with Memory/Trip summary
  - Display Toast if no email client app installed
  - Display Toast if no SMS app installed
  - _Requirements: 24_

- [x] 40. Notifications - On This Day Setup
  - Create NotificationManager helper class
  - Implement "On This Day" notification trigger using AlarmManager
  - Schedule daily notification at user-configured time (default: 09:00)
  - Query SQLite for memories matching current calendar date from previous years
  - Deliver notification only if matching memories exist
  - Respect user's "On This Day" notification setting
  - _Requirements: 23_

- [ ] 41. Notifications - On This Day Activity & Handling
  - Create OnThisDayActivity displaying all memories from same calendar date across all years
  - Implement notification tap to open OnThisDayActivity
  - Load memories from SQLite grouped by year
  - Display memories in chronological order
  - _Requirements: 23_

- [ ] 42. Notifications - Time Capsule & Friend Activity
  - Implement time capsule ready notification when open date reached
  - Implement friend activity notification when friend publishes new shared memory
  - Deliver friend activity notification within 60 seconds
  - Implement notification tap handlers to navigate to corresponding activities
  - _Requirements: 23_

- [ ] 43. UI/UX Polish - Layout Files & Material Design
  - Create all XML layout files using ConstraintLayout
  - Implement Material Design components: Cards, Buttons, FABs, Dialogs, TextInputLayout
  - Create custom themes and styles in res/values/themes.xml
  - Implement loading states with ProgressBar in list activities
  - Implement error states with Snackbar/Toast messages
  - Implement empty states for lists (no memories, no trips, no friends)
  - _Requirements: 1, 2, 4, 5, 8, 10, 12, 13, 14, 15, 16, 18, 19, 20_

- [ ] 44. Offline Support - Caching & Sync Queue
  - Implement offline data caching in SQLite for all memories, trips, places, reviews
  - Create sync queue table in SQLite for tracking offline changes
  - Implement offline change queuing: create, update, delete operations
  - Implement conflict resolution logic for sync conflicts
  - Display offline indicator in Bottom_Nav area when no network
  - _Requirements: 26_

- [~] 45. Offline Support - Automatic Sync
  - Implement network connectivity monitoring using ConnectivityManager
  - Implement automatic sync trigger when connectivity restored
  - Sync queued changes to backend via Retrofit_Client within 30 seconds
  - Implement sync conflict detection and resolution UI (AlertDialog with both versions)
  - Allow user to choose which version to keep on conflict
  - Update SQLite with synced data
  - _Requirements: 26_

- [ ] 46. Checkpoint - Core Features Complete
  - Verify all activities launch without crashes
  - Verify database CRUD operations work correctly
  - Verify authentication flow (sign-up, sign-in, sign-out)
  - Verify memory creation and display on calendar
  - Verify trip creation and timeline display
  - Verify dream board functionality
  - Verify location services and map display
  - Ensure all tests pass, ask the user if questions arise.

- [ ]* 47. Testing - Unit Tests
  - Write unit tests for User, Memory, Trip, Place, Review, DreamDestination, Friend, TimeCapsule POJOs
  - Write unit tests for MemoryDao, TripDao, PlaceDao, ReviewDao, DreamDestinationDao, FriendDao, TimeCapsuleDao, UserDao
  - Write unit tests for LocationService geocoding methods
  - Write unit tests for NotificationManager scheduling logic
  - Write unit tests for ShakeDetector acceleration calculation
  - _Requirements: 9, 11, 12, 21, 23_

- [ ]* 48. Testing - Integration Tests
  - Write integration tests for authentication flow (sign-up, sign-in, session persistence)
  - Write integration tests for memory CRUD operations with database
  - Write integration tests for trip CRUD operations with database
  - Write integration tests for offline sync queue and conflict resolution
  - Write integration tests for Retrofit API calls with mock server
  - _Requirements: 2, 9, 22, 26_

- [ ]* 49. Testing - UI Tests
  - Write UI tests for SplashActivity navigation
  - Write UI tests for SignUpActivity form validation
  - Write UI tests for SignInActivity authentication
  - Write UI tests for MemoryCreateEditActivity photo import
  - Write UI tests for CalendarActivity date navigation
  - Write UI tests for MapActivity pin interaction
  - Write UI tests for BottomNavigationView tab switching
  - _Requirements: 1, 2, 4, 5, 8, 10_

- [ ] 50. Final Checkpoint - All Features Verified
  - Verify all 26 requirements implemented and functional
  - Verify all activities and fragments load without crashes
  - Verify all database operations complete successfully
  - Verify all API calls execute correctly
  - Verify offline mode works with sync on reconnect
  - Verify notifications trigger at correct times
  - Verify sensor shake detection works
  - Verify all menus display and function correctly
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints (46, 50) ensure incremental validation
- Database setup (tasks 2-3) must complete before any feature tasks
- Authentication (tasks 6-10) must complete before main activities
- Bottom navigation (task 11) provides foundation for all main screens
- Offline support (tasks 44-45) should be implemented after core features work
- Testing tasks (47-49) can be done in parallel with feature implementation
