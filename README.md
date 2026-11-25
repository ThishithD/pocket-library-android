# Pocket Library

Pocket Library is a native Android book-management app built with Kotlin and Jetpack Compose. It searches Open Library, lets users maintain a personal library, capture custom cover photos, and synchronize library records through Firebase Firestore.

## Features

- Search books through the Open Library Search API.
- View book titles, authors, publication years, and cover images.
- Save or remove books from a personal library.
- Add books manually when they are not available through Open Library.
- Search saved books by title or author.
- Delete books from the local library.
- Take and save custom cover photos.
- Display search results in a list or two-column grid, depending on device orientation.
- Store library data locally with Room for fast, offline access.
- Synchronize saved records to Firebase Firestore through anonymous authentication.
- Responsive Material 3 interface built with Jetpack Compose.

## Technology Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose and Material 3
- **Architecture:** MVVM with Android ViewModels and Kotlin Flows
- **Navigation:** Jetpack Navigation Compose
- **Local database:** Room
- **Remote book data:** Open Library REST API
- **Networking:** Retrofit, Moshi, and Kotlin coroutines
- **Image loading:** Coil
- **Cloud storage:** Firebase Authentication and Cloud Firestore
- **Build system:** Gradle Kotlin DSL

## Screens

### Search

The Search screen uses a text field to query Open Library. Results show the cover, title, author, and first publication year. Users can save a result to My Library or take a photo to use as a custom cover.

### My Library

The My Library screen lists locally stored books in alphabetical order. Users can filter the list, manually add a book, replace a cover with a camera photo, or delete a book.

## Requirements

- Android Studio (current stable release recommended)
- Android SDK Platform 34
- Android SDK Build Tools
- JDK 17 recommended for the Gradle build
- Android device or emulator running Android 7.0 (API 24) or newer
- Internet connection for Open Library and Firebase
- Camera hardware or an emulator that supports the camera preview API

## Getting Started

### 1. Clone the project

```bash
git clone <repository-url>
cd <project-directory>
```

### 2. Open in Android Studio

Open the project directory in Android Studio. Allow Gradle synchronization to finish and accept any requested SDK components.

### 3. Configure Firebase

The app expects a Firebase Android app for the package name:

```text
com.example.pocketlibrary
```

A configured `app/google-services.json` file is included. To use another Firebase project:

1. Create or select a Firebase project in the Firebase Console.
2. Add an Android app with package name `com.example.pocketlibrary`.
3. Download the generated `google-services.json`.
4. Replace `app/google-services.json`.
5. Enable **Anonymous** sign-in under Firebase Authentication > Sign-in method.
6. Create a Cloud Firestore database and configure security rules appropriate for your app.

The app stores Firestore records at:

```text
users/{anonymous-user-id}/books
```

### 4. Run the app

In Android Studio:

1. Select an API 24 or newer device or emulator.
2. Press **Run** or use `Shift+F10`.
3. Search for a book and add it to My Library.

From a command line on Windows:

```bat
gradlew.bat assembleDebug
gradlew.bat installDebug
```

The debug APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Permissions

The app declares the following permissions:

| Permission | Purpose |
|---|---|
| `INTERNET` | Search Open Library and synchronize with Firebase |
| `READ_CONTACTS` | Declared in the manifest; not used by the current app screens |

Custom cover capture uses the Android camera preview API. The app requests access through the system camera launcher rather than a camera runtime permission.

## Data Storage

### Room

Room stores saved books in the `pocket_library_database` database. The `BookEntity` model contains:

- Open Library API key or a generated manual key
- Title
- Author
- Publication year
- Local image path or remote cover URL

### Internal Storage

Camera photographs are compressed as JPEG files and stored in the app's internal files directory. The generated file path is saved with the corresponding Room record.

### Firestore

When a book is saved or a cover is updated, the app writes the book to the current anonymous user's Firestore collection. On My Library initialization, the app signs in anonymously and imports Firestore records into Room.

## Project Structure

```text
app/src/main/
├── AndroidManifest.xml
├── java/com/example/pocketlibrary/
│   ├── MainActivity.kt              # Compose app and bottom navigation
│   ├── SearchScreen.kt              # Open Library search UI
│   ├── SearchViewModel.kt           # Search and saved-book state
│   ├── MyLibraryScreen.kt           # Local library and manual entry UI
│   ├── MyLibraryViewModel.kt        # Library filtering and synchronization
│   ├── FirebaseRepository.kt        # Firebase Auth and Firestore access
│   ├── AppDatabase.kt               # Room database
│   ├── BookDao.kt                   # Room queries
│   ├── BookEntity.kt                # Local persistence model
│   ├── Book.kt                      # Open Library API models
│   ├── OpenLibraryApiService.kt     # Retrofit endpoint
│   ├── Network.kt                   # Retrofit configuration
│   └── ui/theme/                    # Compose theme
└── res/                             # Resources and app icons
```

## Testing

Run JVM unit tests:

```bat
gradlew.bat test
```

Run the instrumented test suite on a connected device or emulator:

```bat
gradlew.bat connectedAndroidTest
```

Android Studio also provides test actions through the gutter and **Run** menu.

## Building

Build a debug APK:

```bat
gradlew.bat assembleDebug
```

Build a release APK:

```bat
gradlew.bat assembleRelease
```

The release configuration currently uses the default Android debug signing setup and does not enable code shrinking.

## Troubleshooting

### Gradle or SDK errors

- Confirm Android SDK Platform 34 is installed.
- Open **File > Sync Project with Gradle Files**.
- Verify `local.properties` contains a valid Windows SDK path, for example:

  ```properties
  sdk.dir=C:\\Users\\<user>\\AppData\\Local\\Android\\Sdk
  ```

### Firebase errors

- Confirm `google-services.json` belongs to a Firebase project that contains an Android app with package `com.example.pocketlibrary`.
- Enable Anonymous Authentication in Firebase.
- Create the Firestore database.
- Add development security rules and test rules before deploying to users.

### No search results or network error

- Verify that the device or emulator has internet access.
- Confirm access to `https://openlibrary.org`.
- Try a broader search term.

### Camera cover does not appear

- Use a device or emulator with a working camera preview.
- Test on a physical device if the emulator camera is unavailable.
- After taking a photo, reopen the library screen to confirm the updated cover.

## API Reference

The app retrieves book documents from Open Library:

```text
GET https://openlibrary.org/search.json
```

Requested fields include the key, title, author names, first publication year, and cover ID. Cover images use:

```text
https://covers.openlibrary.org/b/id/{coverId}-M.jpg
```

## Known Limitations

- Firestore synchronization is one-way on local deletions: deleting a book in Room does not currently delete the matching Firestore document.
- Anonymous Firebase identity can change for a new install or environment, so cloud records may not be available across unrelated installations.
- Custom cover file paths are local device paths and are not uploaded to Firebase Storage.
- The included README and tests are starter-level; no automated feature test suite is included yet.

## License

No license has been specified for this project. All rights remain with the project author unless a license is added.
