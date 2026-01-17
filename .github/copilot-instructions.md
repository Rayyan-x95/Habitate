# Habitate — AI Coding Instructions

## Project Overview
Habitate is a mobile-first social super-app combining social networking, fitness tracking, habit/task management, journaling, and wellbeing studies. **Privacy-first** design is a core principle.

**Package:** `com.ninety5.habitate`
**Target:** Android (Kotlin + Jetpack Compose), minSdk 29, targetSdk 36
**Version:** 1.0.0-public-beta

## Architecture & Structure

```
app/src/main/java/com/ninety5/habitate/
├── HABITATEAPPLICATION.kt   # Application class
├── MainActivity.kt          # Single-activity entry point + NavHost
├── core/
│   ├── analytics/           # Analytics abstractions
│   ├── di/                  # Hilt modules (Network, Database, etc.)
│   └── service/             # Foreground services
├── data/                    # Data layer
│   ├── health/              # Health Connect integration
│   ├── local/               # Room DB, DAOs, Entities
│   ├── remote/              # Retrofit APIs, DTOs
│   └── repository/          # Repository implementations
├── domain/                  # Use cases, business logic, domain models
├── service/                 # Android Services (Pomodoro, etc.)
├── ui/                      # UI Layer
│   ├── common/              # Shared UI logic
│   ├── components/          # Reusable composables
│   ├── navigation/          # Routes, NavHost (Screen.kt)
│   ├── screens/             # Feature screens (Feed, Planner, Studies, etc.)
│   ├── theme/               # Material3 theming
│   └── viewmodel/           # Shared ViewModels
├── util/                    # Extensions, helpers
└── worker/                  # WorkManager workers (Sync, Upload)
```

**Pattern:** Clean Architecture with MVVM. Use `ViewModel` + `StateFlow` for UI state. Keep composables stateless; hoist state to ViewModels.

---

## API & Backend Patterns

### Global API Conventions
```
Base path: https://api.habitate.app/
Auth: Bearer JWT tokens in Authorization header
Items: Pagination mainly via cursor or offset
```

### JSON & Serialization
- Use `Moshi` for JSON parsing.
- Annotated DTOs in `data/remote/dto`.

---

## Compose Navigation

### Route Constants
Defined in `ui/navigation/Screen.kt`.

```kotlin
sealed class Screen(val route: String) {
    // Main Tabs
    object Feed : Screen("feed")
    object Habitats : Screen("habitats")
    object Create : Screen("create")
    object Activity : Screen("activity")
    object Profile : Screen("profile")
    
    // Features
    object Studies : Screen("studies")
    object Focus : Screen("focus")
    object Wellbeing : Screen("wellbeing")
    object Planner : Screen("planner")
    object Journal : Screen("journal")
    object PublicApi : Screen("public_api")

    // Dynamic Routes
    object PostDetail : Screen("post/{postId}") {
        fun createRoute(postId: String) = "post/$postId"
    }
    object HabitatDetail : Screen("habitat/{habitatId}") {
        fun createRoute(habitatId: String) = "habitat/$habitatId"
    }
}
```

### NavHost Setup
Root navigation is handled in `MainActivity.kt` using `NavHost`.
Deep links are supported for posts, habitats, and user profiles.

---

## Dependency Injection (Hilt)

Modules are located in `core/di/`.

- `NetworkModule`: OkHttp, Retrofit
- `DatabaseModule`: Room Database, DAOs
- `RepositoryModule`: Binds Repository interfaces to Implementations
- `ServiceModule`: Service bindings
- `FeatureModule`: Feature-specific dependencies

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideApiService(...): ApiService { ... }
}
```

---

## State Management

### ViewModel Pattern
Use `StateFlow` for UI State and `Channel/SharedFlow` for one-off events.

```kotlin
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repo: FeedRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState = _uiState.asStateFlow()
    
    // ...
}
```

---

## Database (Room)

**Database Class:** `HabitateDatabase` in `data/local/`

### Key Entities
- `UserEntity`, `PostEntity`
- `TaskEntity`, `TaskCategoryEntity`, `TaskCompletionEntity`
- `WorkoutEntity`
- `HabitatEntity`, `HabitatMembershipEntity`
- `HabitEntity`, `HabitLogEntity`, `HabitStreakEntity`
- `JournalEntryEntity`, `DailySummaryEntity`
- `StoryEntity`
- `ChatEntity`, `MessageEntity`
- `SyncOperationEntity` (for offline sync)

### DAO Pattern
All DAOs are defined in `data/local/dao/`. Use `Flow` return types for observable queries.

---

## Offline & Sync

- **Sync Queue:** `SyncSameDao` and `SyncWorker` handle offline-first operations.
- **Worker:** Located in `worker/` package.
- **Strategy:** Local changes are written to Room immediately and queued for sync.

---

## Integrations

- **Health Connect:** Located in `data/health/`. Handles reading steps, workouts, etc.
- **Nothing Glyph:** Integrated via `NOTHING_GLYPH_KEY` (see build config).
- **Firebase:** Usage for Crashlytics, Performance, and likely Auth/Push.
- **OpenAI:** Integrated for AI features (key in build config).

## Build & Development

### Config (`app/build.gradle.kts`)
- Namespace: `com.ninety5.habitate`
- Compile SDK: 36
- Min SDK: 29
- Java Version: 17
- Build Features: Compose, BuildConfig

### Important Secrets (local.properties)
- `OPENAI_API_KEY`
- `GOOGLE_WEB_CLIENT_ID`
- `NOTHING_GLYPH_KEY`
- `RELEASE_STORE_PASSWORD`, etc.

### Commands
```bash
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease        # Release build
./gradlew kspDebugKotlin         # Run KSP code gen
```

---

## Privacy & Permissions

**Privacy First:**
- Health data requires explicit opt-in.
- User data is protected.

**Permissions:**
- `android.permission.health.READ_Steps`
- `android.permission.POST_NOTIFICATIONS`

---

## Design System

**Theme:** Material 3
**Location:** `ui/theme/`
**Colors:** Defined in `Color.kt`.
**Typography:** Defined in `Type.kt`.

Make sure to use `MaterialTheme.colorScheme` and `MaterialTheme.typography` in all composables.
