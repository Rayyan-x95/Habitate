# Habitate — AI Coding Instructions

## Project Overview
Habitate is a mobile-first social super-app combining social networking, fitness tracking, habit/task management, journaling, focus sessions, challenges, and wellbeing studies. **Privacy-first** design is a core principle.

**Package:** `com.ninety5.habitate`
**Target:** Android (Kotlin + Jetpack Compose), minSdk 29, targetSdk 36
**Version:** 1.0.0-public-beta

## Architecture & Structure

```
app/src/main/java/com/ninety5/habitate/
├── HabitateApplication.kt   # Application class
├── MainActivity.kt          # Single-activity entry point + NavHost
├── core/
│   ├── analytics/           # Analytics abstractions & tracking
│   ├── audio/               # Audio playback utilities
│   ├── di/                  # Hilt modules (Network, Database, Firebase, etc.)
│   ├── export/              # Data export functionality
│   ├── focus/               # Focus session core logic
│   ├── glyph/               # Nothing Glyph integration
│   ├── insights/            # AI-powered insights engine
│   └── utils/               # Core utilities
├── data/                    # Data layer
│   ├── health/              # Health Connect integration
│   ├── local/               # Room DB, DAOs, Entities, Relations, Views
│   ├── remote/              # Retrofit APIs, DTOs
│   └── repository/          # Repository implementations
├── domain/                  # Use cases, business logic, domain models
├── service/                 # Android Services (Pomodoro, Focus, etc.)
├── ui/                      # UI Layer
│   ├── common/              # Shared UI logic
│   ├── components/          # Reusable composables
│   ├── navigation/          # Routes, NavHost (Screen.kt, HabitateNavHost.kt)
│   ├── screens/             # Feature screens (28+ feature modules)
│   ├── theme/               # Material3 theming
│   └── viewmodel/           # Shared ViewModels
├── util/                    # Extensions, helpers
└── worker/                  # WorkManager workers (Sync, Upload, Archival, Cleanup)
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

### OkHttp
- Use OkHttp 4.x/5.x extension functions instead of deprecated static methods.
  - `payload.toRequestBody("application/json".toMediaTypeOrNull())` instead of `RequestBody.create(...)`

---

## Compose Navigation

### Route Constants
Defined in `ui/navigation/Screen.kt`.

```kotlin
sealed class Screen(val route: String) {
    // Main Bottom Navigation
    object Feed : Screen("feed")
    object Focus : Screen("focus")
    object Create : Screen("create")
    object Habitats : Screen("habitats")
    object Profile : Screen("profile")
    
    // Core Features
    object Studies : Screen("studies")
    object Wellbeing : Screen("wellbeing")
    object ChatList : Screen("chat_list")
    object Journal : Screen("journal")
    object Planner : Screen("planner")
    object HabitList : Screen("habits")
    object TaskList : Screen("tasks")
    object WorkoutList : Screen("workouts")
    object Insights : Screen("insights")
    object Archive : Screen("archive")
    object DailyCheckIn : Screen("daily_checkin")
    object HealthConnect : Screen("health_connect")
    object PublicApi : Screen("public_api")
    
    // Auth Flow
    object Welcome : Screen("welcome")
    object Login : Screen("auth/login")
    object Register : Screen("auth/register")
    object ForgotPassword : Screen("auth/forgot_password")
    object Onboarding : Screen("auth/onboarding")
    object VerifyEmail : Screen("auth/verify_email")
    
    // Create Flow
    object CreatePost : Screen("create/post")
    object CreateTask : Screen("create/task")
    object CreateWorkout : Screen("create/workout")
    object CreateHabitat : Screen("create/habitat")
    
    // Settings
    object Settings : Screen("settings")
    object EditProfile : Screen("settings/profile")
    object PrivacySettings : Screen("settings/privacy")
    object NotificationSettings : Screen("settings/notifications")

    // Dynamic Routes with Arguments
    object Chat : Screen("chat/{roomId}")
    object PostDetail : Screen("post/{postId}")
    object UserProfile : Screen("user/{userId}")
    object HabitatDetail : Screen("habitat/{habitatId}")
    object TaskDetail : Screen("task/{taskId}")
    object WorkoutDetail : Screen("workout/{workoutId}")
    object HabitDetail : Screen("habit/{habitId}")
    object HabitEdit : Screen("habit/edit/{habitId}")
    object HabitCreate : Screen("habit/create")
    object StoryViewer : Screen("story/{userId}")
    object ChallengeDetail : Screen("challenge/{challengeId}")
}
```

### NavHost Setup
Root navigation is handled in `HabitateNavHost.kt` using `NavHost`.
Deep links are supported for posts, habitats, user profiles, and stories.

---

## Dependency Injection (Hilt)

Modules are located in `core/di/`.

- `NetworkModule`: OkHttp, Retrofit, API clients
- `DatabaseModule`: Room Database, DAOs
- `RepositoryModule`: Binds Repository interfaces to implementations
- `ServiceModule`: Service bindings
- `FeatureModule`: Feature-specific dependencies
- `FirebaseModule`: Firebase Auth, Firestore, Crashlytics
- `AnalyticsModule`: Analytics tracking
- `PublicApiModule`: Public API bindings

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
- `UserEntity`, `PostEntity`, `CommentEntity`, `LikeEntity`
- `TaskEntity`, `TaskCategoryEntity`, `TaskCompletionEntity`
- `WorkoutEntity`
- `HabitatEntity`, `HabitatMembershipEntity`
- `HabitEntity`, `HabitRelations`
- `JournalEntryEntity`, `DailySummaryEntity`
- `StoryEntity`, `StoryViewEntity`, `StoryMuteEntity`
- `ChatEntity`, `MessageEntity`, `MessageReactionEntity`
- `ChallengeEntity`, `ChallengeProgressEntity`
- `FocusSessionEntity`, `InsightEntity`
- `FollowingEntity`, `NotificationEntity`
- `SyncOperationEntity`, `RemoteKeysEntity` (for offline sync & pagination)

### DAO Pattern
All DAOs are defined in `data/local/dao/`. Use `Flow` return types for observable queries.

Key DAOs include:
- `UserDao`, `PostDao`, `CommentDao`, `LikeDao`
- `TaskDao`, `HabitDao`, `WorkoutDao`
- `HabitatDao`, `ChatDao`, `MessageDao`
- `StoryDao`, `ChallengeDao`, `FocusDao`
- `JournalDao`, `InsightDao`, `NotificationDao`
- `SyncQueueDao`, `TimelineDao`

---

## Offline & Sync

- **Sync Queue:** `SyncQueueDao` and `SyncWorker` handle offline-first operations.
- **Workers:** Located in `worker/` package:
  - `SyncWorker`: Main synchronization logic
  - `SyncScheduler`: Schedules periodic sync
  - `UploadWorker`: Handles media uploads
  - `UserSyncWorker`: User data synchronization
  - `ArchivalWorker`: Data archival operations
  - `StoryCleanupWorker`: Cleans up expired stories
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
- `android.permission.health.READ_STEPS`
- `android.permission.POST_NOTIFICATIONS`

---

## Coding Standards & Best Practices

### Error Handling Requirements

**ViewModel Error Handling:**
```kotlin
// REQUIRED pattern for all ViewModels
data class FeatureUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val data: T? = null
)

// Always provide clearError() function
fun clearError() {
    _uiState.update { it.copy(error = null) }
}

// Use AppResult<T> for repository calls
viewModelScope.launch {
    _uiState.update { it.copy(isLoading = true, error = null) }
    repository.getData()
        .onSuccess { data ->
            _uiState.update { it.copy(isLoading = false, data = data) }
        }
        .onError { e ->
            Timber.e(e, "Failed to load data")
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
}
```

**One-Off Events (Navigation, Snackbars):**
```kotlin
// Use Channel for one-off events, NOT boolean flags
private val _events = Channel<UiEvent>(Channel.BUFFERED)
val events = _events.receiveAsFlow()

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
}
```

### Repository Pattern

**All repositories MUST follow offline-first:**
```kotlin
// REQUIRED: Return AppResult<T> for error handling
suspend fun getData(): AppResult<T> {
    return try {
        // 1. Return cached data immediately
        val cached = localDao.getData()
        if (cached != null) return AppResult.Success(cached)
        
        // 2. Fetch from network
        val remote = apiService.getData()
        
        // 3. Cache to local DB
        localDao.insert(remote.toEntity())
        
        AppResult.Success(remote.toDomain())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Timber.e(e, "Failed to get data")
        AppResult.Error(AppError.from(e))
    }
}

// For mutations, queue to SyncQueue
suspend fun updateData(data: T): AppResult<Unit> {
    return try {
        // 1. Update local DB immediately (optimistic)
        localDao.update(data.toEntity())
        
        // 2. Queue for sync
        val payload = moshi.adapter(DataDto::class.java).toJson(data.toDto())
        syncQueueDao.insert(SyncOperationEntity(
            entityType = "entity_name",
            entityId = data.id,
            operation = "UPDATE",
            payload = payload,
            status = SyncStatus.PENDING,
            createdAt = Instant.now(),
            lastAttemptAt = null
        ))
        
        // 3. Attempt immediate sync (optional but recommended)
        try {
            val requestBody = payload.toRequestBody("application/json".toMediaTypeOrNull())
            apiService.update("entity_name", data.id, requestBody)
            localDao.updateSyncState(data.id, SyncState.SYNCED)
            syncQueueDao.deleteByEntity("entity_name", data.id, "UPDATE")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Immediate sync failed, will retry later")
        }
        
        AppResult.Success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AppResult.Error(AppError.from(e))
    }
}
```

### Worker/Service Standards

**Cancellation Handling (REQUIRED for loops):**
```kotlin
// Always check for cancellation in loops
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

for (item in items) {
    coroutineContext.ensureActive() // Throws CancellationException if cancelled
    processItem(item)
}
```

**Service Coroutine Scope:**
```kotlin
// CORRECT: Use SupervisorJob + cancel in onDestroy
private val serviceJob = SupervisorJob()
private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

override fun onDestroy() {
    super.onDestroy()
    serviceScope.cancel() // Clean up all coroutines
}
```

### Compose Best Practices

**Stateless Composables:**
```kotlin
// Composables should be stateless - hoist state to ViewModel
@Composable
fun FeatureScreen(
    uiState: FeatureUiState,
    onAction: (FeatureAction) -> Unit,
    modifier: Modifier = Modifier
)
```

**Always use remember for expensive computations:**
```kotlin
val formattedDate = remember(date) { 
    dateFormatter.format(date) 
}
```

**LazyColumn/LazyRow keys:**
```kotlin
// ALWAYS provide stable keys
LazyColumn {
    items(
        items = items,
        key = { it.id } // Stable identifier
    ) { item ->
        ItemRow(item)
    }
}
```

**Side Effects:**
```kotlin
// Collect one-off events
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            is UiEvent.Navigate -> navController.navigate(event.route)
        }
    }
}
```

### Testing Requirements

**ViewModel Tests:**
- Test all state transitions
- Test error handling paths
- Use Turbine for Flow testing
- Use MockK or Mockito for mocking

```kotlin
@Test
fun `loading state is shown while fetching data`() = runTest {
    viewModel.uiState.test {
        viewModel.loadData()
        
        val loading = awaitItem()
        assertTrue(loading.isLoading)
        
        val success = awaitItem()
        assertFalse(success.isLoading)
        assertNotNull(success.data)
    }
}
```

---

## Security Guidelines

**API Security:**
- Never hardcode API keys in source code
- Use BuildConfig for secrets from local.properties
- Implement certificate pinning for production

**Data Protection:**
- Encrypt sensitive data with EncryptedSharedPreferences
- Never log sensitive user data
- Clear sensitive data on logout

**Permissions:**
- Request permissions at point of use, not at startup
- Check `POST_NOTIFICATIONS` on Android 13+ before showing notifications
- Handle permission denial gracefully

---

## Design System

**Theme:** Material 3
**Location:** `ui/theme/`
**Colors:** Defined in `Color.kt`.
**Typography:** Defined in `Type.kt`.

Make sure to use `MaterialTheme.colorScheme` and `MaterialTheme.typography` in all composables.
