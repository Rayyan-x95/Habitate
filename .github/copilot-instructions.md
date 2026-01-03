# Habitate — AI Coding Instructions

## Project Overview
Habitate is a mobile-first social super-app combining social networking, fitness tracking, habit/task management, and journaling. **Privacy-first** design is a core principle—health data sharing is always opt-in.

**Target:** Android (Kotlin + Jetpack Compose), minSdk 29, targetSdk 36

## Architecture & Structure

```
app/src/main/java/com/example/habitate/
├── MainActivity.kt          # Single-activity entry point + NavHost
├── ui/theme/                 # Material3 theming (Color.kt, Theme.kt, Type.kt)
├── ui/screens/               # Feature screens
├── ui/components/            # Reusable composables
├── ui/navigation/            # Routes, NavHost, deep links
├── data/                     # Repositories, data sources, DTOs
├── domain/                   # Use cases, business logic, domain models
├── di/                       # Hilt modules (NetworkModule, DatabaseModule)
└── util/                     # Extensions, helpers
```

**Pattern:** Clean Architecture with MVVM. Use `ViewModel` + `StateFlow` for UI state. Keep composables stateless; hoist state to ViewModels.

---

## API & Backend Patterns

### When to Use REST vs GraphQL
- **REST** → auth, file uploads, notifications, simple CRUD (cache-friendly, CDN-ready)
- **GraphQL** → feed queries, complex nested data (user + posts + habitats in one request)
- **Hybrid approach recommended:** REST for `/auth`, `/uploads`; GraphQL for `/graphql` feed queries

### Global API Conventions
```
Base path: /api/v1/...  |  GraphQL: /graphql + X-API-Version header
Auth: Bearer JWT tokens (refresh via /auth/refresh)
Pagination: Cursor-based (Relay-style) for feeds; offset for simple lists
Timestamps: ISO 8601 UTC (2025-12-12T16:29:00Z)
```

### Error Format (consistent across all endpoints)
```json
{
  "error": {
    "code": "TASK_NOT_FOUND",
    "message": "Task with id 123 not found",
    "details": { "taskId": "123" }
  }
}
```

### REST Example: Create Post
```http
POST /api/v1/posts
Authorization: Bearer <token>
Content-Type: application/json

{
  "content_text": "Morning run! 🏃‍♂️",
  "media_refs": ["s3://bucket/media/123.jpg"],
  "visibility": "public",
  "workout_summary_id": "w_123"
}
```

### GraphQL Example: Feed Query
```graphql
query Feed($cursor: String, $limit: Int = 20) {
  feed(cursor: $cursor, limit: $limit) {
    edges {
      node {
        id
        contentText
        author { id displayName avatarUrl }
        metrics { likes comments }
      }
    }
    pageInfo { endCursor hasNextPage }
  }
}
```

### Server Architecture Layers
```
Controller/Handler → Service (business logic) → Repository (DB) → External Adapters (S3, FCM, Health)
```
- Use **Domain Models + DTOs** (never leak ORM entities to API)
- Background jobs for media transcoding, notifications (Redis + queue)

---

## Compose Navigation

### Typed Route Constants
```kotlin
// ui/navigation/Screen.kt
sealed class Screen(val route: String) {
    object Feed : Screen("feed")
    object Habitats : Screen("habitats")
    object Create : Screen("create")
    object Activity : Screen("activity")
    object Profile : Screen("profile/{userId}") {
        fun createRoute(userId: String) = "profile/$userId"
    }
    object Post : Screen("post/{postId}") {
        fun createRoute(postId: String) = "post/$postId"
    }
}
```

### NavHost Setup
```kotlin
@Composable
fun RootNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController, startDestination = Screen.Feed.route) {
        composable(Screen.Feed.route) { FeedScreen(navController) }
        composable(Screen.Habitats.route) { HabitatsScreen(navController) }
        composable(
            Screen.Profile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            ProfileScreen(userId = backStackEntry.arguments?.getString("userId")!!)
        }
    }
}
```

### Navigation Best Practices
- Use `popUpTo` + `launchSingleTop` to avoid duplicate screens
- Handle deep links for posts, profiles, habitats
- Single-activity with nested NavGraphs for feature modules

---

## Dependency Injection (Hilt)

### Module Example
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .build()

    @Provides @Singleton
    fun provideApiService(client: OkHttpClient): ApiService =
        Retrofit.Builder()
            .baseUrl("https://api.habitate.app")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ApiService::class.java)
}
```

### ViewModel Injection
```kotlin
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository
) : ViewModel() { ... }

// In Composable:
val viewModel: FeedViewModel = hiltViewModel()
```

### DI Patterns
- Prefer `interface + implementation` for testability
- Use `@Named` qualifiers for multiple implementations
- Scopes: `@Singleton`, `@ActivityRetainedScoped`, `@ViewModelScoped`

---

## State Management

### ViewModel Pattern (StateFlow + Channel)
```kotlin
data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FeedViewModel @Inject constructor(private val repo: FeedRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    // One-off events (navigation, toasts)
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repo.getFeed()
                .onSuccess { posts -> _uiState.update { it.copy(posts = posts, isLoading = false) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    sealed class UiEvent {
        data class NavigateToPost(val id: String) : UiEvent()
        data class ShowToast(val message: String) : UiEvent()
    }
}
```

### Composable Consumption
```kotlin
@Composable
fun FeedScreen(viewModel: FeedViewModel = hiltViewModel(), navController: NavController) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FeedViewModel.UiEvent.NavigateToPost -> navController.navigate(Screen.Post.createRoute(event.id))
                is FeedViewModel.UiEvent.ShowToast -> { /* show snackbar */ }
            }
        }
    }

    // UI rendering...
}
```

### When to Use What
| State Type | Use Case |
|------------|----------|
| `StateFlow` | Screen state in ViewModel (survives config changes) |
| `mutableStateOf` | Local UI state (TextField, dropdown expanded) |
| `Channel/SharedFlow` | One-off events (navigation, toasts) |
| `derivedStateOf` | Computed values to reduce recomposition |

---

## Design System & UI Conventions

### Brand Colors (update in `Color.kt`)
```kotlin
val Charcoal = Color(0xFF1E1E24)      // Primary background
val SoftIndigo = Color(0xFF6366F1)    // Primary accent
val SageGreen = Color(0xFF84CC16)     // Success/fitness
val MutedLilac = Color(0xFFA78BFA)    // Secondary accent
val OffWhite = Color(0xFFFAFAFA)      // Light surfaces
```

### Component Standards
- **Cards:** Rounded corners (16.dp), soft shadows, glass-morphism effects
- **Touch targets:** Minimum 44.dp for accessibility (WCAG AA)
- **Bottom nav:** Feed / Habitats / Create / Activity / Profile
- **FAB:** Use for primary create actions
- **Motion:** Subtle micro-interactions, 60fps animations

### Compose Patterns
```kotlin
// Stateless composable with state hoisting
@Composable
fun PostCard(
    post: Post,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
)

// Always add preview annotations
@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PostCardPreview() { ... }
```

---

## Build & Development

### Commands
```bash
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease        # Release build
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests
```

### Version Catalog (`gradle/libs.versions.toml`)
```toml
[versions]
hilt = "2.48"
navigation = "2.7.0"

[libraries]
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
```

---

## Privacy & Permissions

**Critical:** Health data requires explicit opt-in. Never auto-enable.

```xml
<uses-permission android:name="android.permission.health.READ_STEPS" />
<uses-permission android:name="android.permission.health.READ_HEART_RATE" />
```

Request permissions at point of use with clear rationale dialogs.

---

## Testing Strategy

| Layer | Tool | Location |
|-------|------|----------|
| ViewModel | JUnit + Turbine (Flow testing) | `app/src/test/` |
| Repository | MockWebServer + Room in-memory | `app/src/test/` |
| UI | Compose Test + Robot pattern | `app/src/androidTest/` |

```kotlin
@Test
fun loadFeed_withNetworkError_showsErrorState() = runTest {
    // Arrange, Act, Assert with Turbine
}
```

---

## Offline & Sync

- **Tasks/Habits:** Room DB as source of truth + sync queue when online
- **Conflict resolution:** Last-write-wins or merge strategies
- **Media uploads:** Queue failed uploads, retry with exponential backoff
- **Feed:** Cache in Room, refresh on pull-to-refresh

---

## Authentication & Token Management

### OAuth + JWT Flow
```
1. Client → OAuth provider (Google/Apple via AppAuth)
2. Backend validates → issues access_token (15min) + refresh_token (30d)
3. Client stores tokens in EncryptedSharedPreferences
4. API requests: Authorization: Bearer <access_token>
5. On 401 or near-expiry → POST /auth/refresh with refresh_token
6. Logout → POST /auth/revoke + purge local storage
```

### Secure Token Storage
```kotlin
// di/SecurityModule.kt
@Module @InstallIn(SingletonComponent::class)
object SecurityModule {
    @Provides @Singleton
    fun provideEncryptedPrefs(@ApplicationContext context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context, "auth_prefs", masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
```

### AuthRepository Pattern
```kotlin
interface AuthRepository {
    suspend fun getAccessToken(): String?          // Returns valid token or refreshes
    suspend fun refreshAccessToken(): Result<String>
    suspend fun logout()
    val isAuthenticated: StateFlow<Boolean>
}

class AuthRepositoryImpl @Inject constructor(
    private val securePrefs: SharedPreferences,
    private val authApi: AuthApi
) : AuthRepository {
    private val refreshMutex = Mutex()  // Single-flight refresh

    override suspend fun getAccessToken(): String? {
        val token = securePrefs.getString("access_token", null) ?: return null
        val expiry = securePrefs.getLong("token_expiry", 0)
        
        return if (System.currentTimeMillis() < expiry - 60_000) token
        else refreshAccessToken().getOrNull()
    }

    override suspend fun refreshAccessToken(): Result<String> = refreshMutex.withLock {
        // Re-check after acquiring lock (another call may have refreshed)
        val currentExpiry = securePrefs.getLong("token_expiry", 0)
        if (System.currentTimeMillis() < currentExpiry - 60_000) {
            return Result.success(securePrefs.getString("access_token", "")!!)
        }
        // Actual refresh call...
    }
}
```

### OkHttp Authenticator (Auto-Refresh on 401)
```kotlin
class TokenAuthenticator @Inject constructor(
    private val authRepositoryProvider: Provider<AuthRepository>
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("No-Auth") == "true") return null
        if (responseCount(response) >= 2) return null  // Prevent loops

        val authRepository = authRepositoryProvider.get()
        val newToken = runBlocking { authRepository.refreshAccessToken().getOrNull() }
            ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) { count++; prior = prior.priorResponse }
        return count
    }
}
```

### Backend Token Security
- Store refresh tokens **hashed** (bcrypt/argon2) with metadata: `user_id`, `device_id`, `expires_at`, `revoked`
- **Rotate refresh tokens** on each use → detect reuse as compromise signal
- Invalidate all tokens on password change

---

## Room Database

### Entity Examples
```kotlin
// data/local/entity/TaskEntity.kt
@Entity(tableName = "tasks", indices = [Index("dueAt"), Index("status")])
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val dueAt: Instant?,
    val recurrenceRule: String?,      // iCal RRULE format
    val status: TaskStatus,           // OPEN, DONE, ARCHIVED
    val syncState: SyncState,         // PENDING, SYNCED, FAILED
    val updatedAt: Instant
)

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val source: WorkoutSource,        // MANUAL, HEALTH_CONNECT
    val externalId: String?,          // Health Connect ID for dedup
    val type: String,
    val startTs: Instant,
    val endTs: Instant,
    val distanceMeters: Double?,
    val calories: Double?,
    val syncState: SyncState,
    val updatedAt: Instant
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val contentText: String?,
    val mediaUris: List<String>,      // TypeConverter
    val visibility: Visibility,
    val workoutId: String?,           // Optional linked workout
    val syncState: SyncState,
    val createdAt: Instant
)
```

### TypeConverters
```kotlin
@TypeConverters(Converters::class)
@Database(
    entities = [
        TaskEntity::class, 
        WorkoutEntity::class, 
        PostEntity::class,
        ChallengeEntity::class
    ], 
    version = 8
)
abstract class HabitateDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun postDao(): PostDao
    abstract fun challengeDao(): ChallengeDao
}

class Converters {
    @TypeConverter fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()
    @TypeConverter fun toInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter fun fromStringList(list: List<String>): String = list.joinToString("||")
    @TypeConverter fun toStringList(data: String): List<String> = 
        if (data.isEmpty()) emptyList() else data.split("||")

    @TypeConverter fun fromSyncState(state: SyncState): String = state.name
    @TypeConverter fun toSyncState(value: String): SyncState = SyncState.valueOf(value)
}
```

### DAO Patterns
```kotlin
@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE status != 'ARCHIVED' ORDER BY dueAt NULLS LAST")
    fun getActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE syncState = 'PENDING'")
    suspend fun getPendingSyncTasks(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity)

    @Query("UPDATE tasks SET status = :status, syncState = 'PENDING', updatedAt = :now WHERE id = :id")
    suspend fun updateStatus(id: String, status: TaskStatus, now: Long = System.currentTimeMillis())

    @Transaction
    suspend fun upsertAndMarkSynced(task: TaskEntity) {
        upsert(task.copy(syncState = SyncState.SYNCED))
    }
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE externalId = :externalId LIMIT 1")
    suspend fun findByExternalId(externalId: String): WorkoutEntity?

    @Query("SELECT * FROM workouts ORDER BY startTs DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)  // Skip if exists
    suspend fun insertIfNotExists(workout: WorkoutEntity): Long
}
```

### Migrations
```kotlin
// Always write explicit migrations
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN recurrenceRule TEXT")
        db.execSQL("ALTER TABLE workouts ADD COLUMN externalId TEXT")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_workouts_externalId ON workouts(externalId)")
    }
}

// Complex migration: rename + restructure
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Create new table
        db.execSQL("""CREATE TABLE tasks_new (
            id TEXT PRIMARY KEY NOT NULL,
            title TEXT NOT NULL,
            ...
        )""")
        // 2. Copy data
        db.execSQL("INSERT INTO tasks_new SELECT ... FROM tasks")
        // 3. Drop old, rename new
        db.execSQL("DROP TABLE tasks")
        db.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
    }
}

// Test migrations in CI
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        HabitateDatabase::class.java
    )

    @Test fun migrate1To2() {
        helper.createDatabase(TEST_DB, 1).apply { close() }
        helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
    }
}
```

### Database Module
```kotlin
@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HabitateDatabase =
        Room.databaseBuilder(context, HabitateDatabase::class.java, "habitate.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides fun provideTaskDao(db: HabitateDatabase) = db.taskDao()
    @Provides fun provideWorkoutDao(db: HabitateDatabase) = db.workoutDao()
}
```

---

## Health Connect Integration

### Adapter Pattern
```kotlin
// data/health/HealthConnectAdapter.kt
interface HealthDataSource {
    suspend fun isAvailable(): Boolean
    suspend fun hasPermissions(): Boolean
    suspend fun requestPermissions(activity: Activity)
    suspend fun readWorkouts(since: Instant): List<HealthWorkout>
    fun observeDailySteps(): Flow<Int>
}

class HealthConnectAdapter @Inject constructor(
    @ApplicationContext private val context: Context
) : HealthDataSource {
    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    private val permissions = setOf(
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class)
    )

    override suspend fun isAvailable(): Boolean =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    override suspend fun hasPermissions(): Boolean =
        client.permissionController.getGrantedPermissions().containsAll(permissions)

    override suspend fun readWorkouts(since: Instant): List<HealthWorkout> {
        val request = ReadRecordsRequest(
            recordType = ExerciseSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.after(since)
        )
        return client.readRecords(request).records.map { session ->
            HealthWorkout(
                externalId = session.metadata.id,
                type = session.exerciseType.toWorkoutType(),
                startTs = session.startTime,
                endTs = session.endTime,
                calories = readCaloriesForSession(session),
                distanceMeters = readDistanceForSession(session)
            )
        }
    }
}
```

### Import & Deduplication
```kotlin
class WorkoutImporter @Inject constructor(
    private val healthDataSource: HealthDataSource,
    private val workoutDao: WorkoutDao,
    private val workoutRepository: WorkoutRepository
) {
    suspend fun importRecent(): ImportResult {
        val lastImport = workoutDao.getLastImportTimestamp() ?: Instant.EPOCH
        val healthWorkouts = healthDataSource.readWorkouts(since = lastImport)

        var imported = 0
        var skipped = 0

        healthWorkouts.forEach { hw ->
            // Deduplicate by externalId
            val existing = workoutDao.findByExternalId(hw.externalId)
            if (existing != null) {
                skipped++
                return@forEach
            }

            val entity = WorkoutEntity(
                id = UUID.randomUUID().toString(),
                source = WorkoutSource.HEALTH_CONNECT,
                externalId = hw.externalId,
                type = hw.type,
                startTs = hw.startTs,
                endTs = hw.endTs,
                distanceMeters = hw.distanceMeters,
                calories = hw.calories,
                syncState = SyncState.PENDING,
                updatedAt = Instant.now()
            )
            workoutDao.upsert(entity)
            imported++
        }

        // Trigger background sync to server
        workoutRepository.syncPendingWorkouts()

        return ImportResult(imported = imported, skipped = skipped)
    }
}
```

### Permission Flow UI
```kotlin
@Composable
fun HealthConnectPermissionScreen(
    onPermissionGranted: () -> Unit,
    viewModel: HealthConnectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.padding(24.dp)) {
        Text("Connect Health Data", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text(
            "Habitate can import your workouts and step counts. " +
            "Your health data is never shared without your permission.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))

        when (uiState) {
            is HealthUiState.NotAvailable -> {
                Text("Health Connect is not available on this device.")
                Button(onClick = { /* Open Play Store */ }) {
                    Text("Install Health Connect")
                }
            }
            is HealthUiState.NeedsPermission -> {
                Button(onClick = { viewModel.requestPermissions() }) {
                    Text("Grant Health Permissions")
                }
            }
            is HealthUiState.Ready -> {
                LaunchedEffect(Unit) { onPermissionGranted() }
            }
        }
    }
}
```

---

## Offline & Sync Queue

### Sync Queue Entity
```kotlin
@Entity(tableName = "sync_queue")
data class SyncOperation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,       // "task", "workout", "post"
    val entityId: String,
    val operation: String,        // "CREATE", "UPDATE", "DELETE"
    val payload: String,          // JSON payload
    val status: SyncStatus,       // PENDING, IN_PROGRESS, FAILED, COMPLETED
    val retryCount: Int = 0,
    val createdAt: Instant,
    val lastAttemptAt: Instant?
)

enum class SyncStatus { PENDING, IN_PROGRESS, FAILED, COMPLETED }
```

### Sync Worker
```kotlin
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncQueueDao: SyncQueueDao,
    private val apiService: ApiService
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pending = syncQueueDao.getPendingOperations()

        pending.forEach { op ->
            syncQueueDao.updateStatus(op.id, SyncStatus.IN_PROGRESS)
            try {
                when (op.operation) {
                    "CREATE" -> apiService.create(op.entityType, op.payload)
                    "UPDATE" -> apiService.update(op.entityType, op.entityId, op.payload)
                    "DELETE" -> apiService.delete(op.entityType, op.entityId)
                }
                syncQueueDao.updateStatus(op.id, SyncStatus.COMPLETED)
            } catch (e: Exception) {
                val newRetry = op.retryCount + 1
                if (newRetry >= MAX_RETRIES) {
                    syncQueueDao.updateStatus(op.id, SyncStatus.FAILED)
                } else {
                    syncQueueDao.updateRetry(op.id, newRetry, SyncStatus.PENDING)
                }
            }
        }
        return Result.success()
    }
}
```

---

## Push Notifications (FCM)

### Topic Subscriptions
- **User-level:** `user_{id}` for direct notifications
- **Global:** `announcements` for app-wide updates
- **Habitat-specific:** `habitat_{id}` for group notifications
- **Interest tags:** `running_challenges`, `meditation_reminders`

### Payload Structure
```json
{
  "to": "<token_or_topic>",
  "notification": { "title": "New comment", "body": "Alex replied to your post" },
  "data": {
    "type": "comment",
    "postId": "p_123",
    "deep_link": "habitate://post/p_123",
    "timestamp": "2025-12-12T16:29:00Z",
    "silent": "false"
  },
  "android": { "priority": "high" }
}
```

### Deep Link Handling
```kotlin
// Handle notification tap in MainActivity
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleDeepLink(intent?.data ?: intent?.getStringExtra("deep_link")?.toUri())
}

private fun handleDeepLink(uri: Uri?) {
    uri ?: return
    // Store pending deep link if auth required
    if (!authRepository.isAuthenticated.value) {
        pendingDeepLink = uri
        return
    }
    navController.handleDeepLink(Intent().setData(uri))
}
```

### Best Practices
- Use `silent: true` for background data sync (limit frequency for battery)
- Server fan-out for topics; direct token for 1:1 notifications
- Respect user preferences and DND settings

---

## Media Uploads (Signed URLs)

### Upload Flow
```
1. POST /api/v1/uploads/request { filename, mimeType, size }
2. Server returns: { uploadUrl, fileId, expiresAt }
3. Client PUT to uploadUrl (S3 pre-signed)
4. POST /api/v1/uploads/complete { fileId, etag }
5. Server validates + triggers thumbnail generation
```

### Progress Tracking
```kotlin
class ProgressRequestBody(
    private val delegate: RequestBody,
    private val onProgress: (Float) -> Unit
) : RequestBody() {
    override fun writeTo(sink: BufferedSink) {
        val countingSink = object : ForwardingSink(sink) {
            var bytesWritten = 0L
            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                bytesWritten += byteCount
                onProgress(bytesWritten.toFloat() / contentLength())
            }
        }
        delegate.writeTo(countingSink.buffer())
    }
}

// In ViewModel
fun uploadMedia(uri: Uri) {
    viewModelScope.launch {
        mediaRepository.upload(uri)
            .collect { progress ->
                _uiState.update { it.copy(uploadProgress = progress) }
            }
    }
}
```

### Multipart for Large Files
- Use S3 Multipart Upload for files > 100MB
- Server issues `uploadId` + signed URLs per part
- Client uploads parts in parallel, completes with part ETags

---

## GraphQL Client (Apollo Kotlin)

### Setup
```kotlin
// di/ApolloModule.kt
@Module @InstallIn(SingletonComponent::class)
object ApolloModule {
    @Provides @Singleton
    fun provideApolloClient(authRepository: AuthRepository): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl("https://api.habitate.app/graphql")
            .addHttpHeader("Authorization", "Bearer ${authRepository.getAccessTokenSync()}")
            .normalizedCache(
                MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024),
                cacheKeyGenerator = TypePolicyCacheKeyGenerator
            )
            .build()
    }
}
```

### Optimistic Updates
```kotlin
suspend fun likePost(postId: String) {
    val optimisticData = LikePostMutation.Data(
        likePost = LikePostMutation.LikePost(
            id = postId,
            metrics = LikePostMutation.Metrics(likes = currentLikes + 1)
        )
    )
    
    apolloClient.mutation(LikePostMutation(postId))
        .optimisticUpdates(optimisticData)
        .execute()
}
```

### Error Handling
```kotlin
sealed class GraphQLResult<T> {
    data class Success<T>(val data: T) : GraphQLResult<T>()
    data class GraphQLError<T>(val errors: List<Error>) : GraphQLResult<T>()
    data class NetworkError<T>(val exception: Exception) : GraphQLResult<T>()
}
```

---

## WebSocket / Realtime

### Message Types
```kotlin
sealed class WsMessage {
    data class NewPost(val post: Post) : WsMessage()
    data class Typing(val chatId: String, val userId: String, val isTyping: Boolean) : WsMessage()
    data class Presence(val userId: String, val status: PresenceStatus) : WsMessage()
    data class ChallengeUpdate(val challengeId: String, val leaderboard: List<Entry>) : WsMessage()
}
```

### Connection Pattern
```kotlin
class RealtimeClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val authRepository: AuthRepository
) {
    private val _messages = MutableSharedFlow<WsMessage>()
    val messages: SharedFlow<WsMessage> = _messages.asSharedFlow()

    fun connect() {
        val request = Request.Builder()
            .url("wss://realtime.habitate.app/ws?token=${authRepository.getAccessTokenSync()}")
            .build()
        
        okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val message = parseMessage(text)
                _messages.tryEmit(message)
            }
        })
    }
}
```

---

## Habitats (Groups)

### Roles & Permissions
| Role | Manage Members | Moderate Posts | Edit Settings | Delete Habitat |
|------|---------------|----------------|---------------|----------------|
| Owner | ✓ | ✓ | ✓ | ✓ |
| Admin | ✓ | ✓ | ✓ | ✗ |
| Moderator | ✗ | ✓ | ✗ | ✗ |
| Member | ✗ | ✗ | ✗ | ✗ |

### Moderation Actions
```kotlin
sealed class ModerationAction {
    data class RemovePost(val postId: String, val reason: String) : ModerationAction()
    data class MuteUser(val userId: String, val duration: Duration) : ModerationAction()
    data class BanUser(val userId: String, val reason: String) : ModerationAction()
    data class PinPost(val postId: String) : ModerationAction()
}

// All actions logged to audit trail
@Entity(tableName = "moderation_logs")
data class ModerationLog(
    @PrimaryKey val id: String,
    val habitatId: String,
    val moderatorId: String,
    val action: String,
    val targetId: String,
    val reason: String?,
    val createdAt: Instant
)
```

### Privacy Levels
- **Public:** Anyone can join/view, listed in search
- **Private:** Join by invite/approval, visible in search
- **Secret:** Invite-only, not listed, link-only discovery

---

## Stories (Ephemeral Content)

### 24h Expiry
```kotlin
// Room query excludes expired
@Query("SELECT * FROM stories WHERE createdAt > :cutoff ORDER BY createdAt DESC")
fun getActiveStories(cutoff: Long = System.currentTimeMillis() - 24.hours.inWholeMilliseconds): Flow<List<StoryEntity>>

// Server-side: TTL index on MongoDB or scheduled cleanup job
```

### View Tracking
```kotlin
// Debounced view recording to reduce writes
class StoryViewTracker @Inject constructor(private val api: StoryApi) {
    private val pendingViews = MutableStateFlow<Set<String>>(emptySet())

    init {
        pendingViews
            .debounce(2000)
            .filter { it.isNotEmpty() }
            .onEach { views ->
                api.recordViews(views.toList())
                pendingViews.value = emptySet()
            }
            .launchIn(scope)
    }

    fun recordView(storyId: String) {
        pendingViews.update { it + storyId }
    }
}
```

---

## Pomodoro / Focus Mode

### Foreground Service
```kotlin
class PomodoroService : Service() {
    private val binder = PomodoroServiceBinder()
    private var timerJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer(intent.getIntExtra(EXTRA_DURATION, 25 * 60))
            ACTION_PAUSE -> pauseTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_STICKY
    }

    private fun startTimer(durationSeconds: Int) {
        startForeground(NOTIFICATION_ID, createNotification())
        timerJob = serviceScope.launch {
            // Timer logic with Room persistence for crash recovery
        }
    }
}
```

### Focus Mode Notification Suppression
```kotlin
// Preferences
data class FocusModeSettings(
    val suppressFeed: Boolean = true,
    val suppressPush: Boolean = true,
    val allowUrgent: Boolean = true,  // Allow @mentions
    val autoReply: String? = "🍅 Currently in focus mode"
)

// Server respects focusMode flag and sends silent pushes only
```

---

## Challenges & Leaderboards

### Progress Tracking
```kotlin
@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val type: ChallengeType,      // DISTANCE, STEPS, CONSISTENCY
    val goal: Double,
    val unit: String,             // "km", "steps", "days"
    val startDate: Instant,
    val endDate: Instant,
    val habitatId: String?,       // Optional: linked to a habitat
    val syncState: SyncState,
    val updatedAt: Instant
)

@Entity(tableName = "challenge_progress")
data class ChallengeProgress(
    @PrimaryKey val odice: String,  // odiceallenge_user composite
    val challengeId: String,
    val userId: String,
    val metric: Double,            // Normalized score (distance in km, count, etc.)
    val rawData: String,           // JSON of original workout/task data
    val updatedAt: Instant
)

// Leaderboard computed server-side, cached in Redis
data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val avatarUrl: String?,
    val score: Double,
    val trend: Trend  // UP, DOWN, SAME
)
```

### Anti-Cheat
- Validate GPS routes for distance-based challenges
- Rate-limit manual entries
- Flag statistical anomalies for review

---

## Analytics Events

### Naming Convention
`verb_object` format: `post_created`, `task_completed`, `workout_imported`, `challenge_joined`

### Required Properties
```kotlin
data class AnalyticsEvent(
    val name: String,
    val userId: String,
    val deviceId: String,
    val sessionId: String,
    val timestamp: Instant,
    val appVersion: String,
    val locale: String,
    val properties: Map<String, Any>  // Event-specific: postId, taskId, etc.
)
```

### Key Events
| Event | Properties | Trigger |
|-------|------------|---------|
| `sign_up` | `method`, `persona` | Registration complete |
| `post_created` | `postId`, `hasMedia`, `habitatId?` | Post published |
| `workout_imported` | `source`, `type`, `duration` | Health Connect import |
| `challenge_joined` | `challengeId`, `habitatId` | User joins challenge |
| `focus_session_completed` | `duration`, `interruptions` | Pomodoro finished |

---

## Feature Flags

### Remote Config Pattern
```kotlin
interface FeatureFlags {
    val pomodoroV2Enabled: Boolean
    val aiSummariesEnabled: Boolean
    val maxUploadSizeMb: Int
    suspend fun refresh()
}

@Singleton
class FirebaseFeatureFlags @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) : FeatureFlags {
    override val pomodoroV2Enabled: Boolean
        get() = remoteConfig.getBoolean("feature_pomodoro_v2")

    override suspend fun refresh() {
        remoteConfig.fetchAndActivate().await()
    }
}
```

### A/B Testing
```kotlin
// Track experiment exposure
analytics.track("experiment_exposed", mapOf(
    "experiment" to "onboarding_flow",
    "variant" to featureFlags.onboardingVariant
))
```

---

## CI/CD (GitHub Actions)

### Build & Test Workflow
```yaml
# .github/workflows/android.yml
name: Android CI
on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      
      - name: Run tests
        run: ./gradlew test
      
      - name: Build debug APK
        run: ./gradlew assembleDebug
      
      - name: Run screenshot tests
        run: ./gradlew verifyPaparazziDebug

  deploy-staging:
    needs: build
    if: github.ref == 'refs/heads/develop'
    runs-on: ubuntu-latest
    steps:
      - name: Build release
        run: ./gradlew assembleRelease
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
      
      - name: Upload to Play Store (internal)
        uses: r0adkll/upload-google-play@v1
        with:
          track: internal
          packageName: com.example.habitate
```

---

## Security

### Certificate Pinning
```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("api.habitate.app", "sha256/AAAA...=")  // Primary
    .add("api.habitate.app", "sha256/BBBB...=")  // Backup
    .build()

val okHttpClient = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

### ProGuard Rules
```proguard
# Keep Moshi adapters
-keep class * extends com.squareup.moshi.JsonAdapter
-keepclassmembers class * { @com.squareup.moshi.* <methods>; }

# Keep Room entities
-keep class com.example.habitate.data.local.entity.** { *; }

# Keep Retrofit interfaces
-keep,allowobfuscation interface * { @retrofit2.http.* <methods>; }
```

### Biometric Auth
```kotlin
fun showBiometricPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Authenticate")
        .setSubtitle("Verify your identity to view health data")
        .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        .build()

    BiometricPrompt(activity, executor, object : AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: AuthenticationResult) {
            onSuccess()
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            onError(errString.toString())
        }
    }).authenticate(promptInfo)
}
```

---

## Testing

### Screenshot Testing (Paparazzi)
```kotlin
class PostCardScreenshotTest {
    @get:Rule val paparazzi = Paparazzi(
        theme = "android:Theme.Material3.DayNight"
    )

    @Test fun postCard_default() {
        paparazzi.snapshot {
            PostCard(post = samplePost, onLikeClick = {})
        }
    }

    @Test fun postCard_darkMode() {
        paparazzi.snapshot {
            HabitateTheme(darkTheme = true) {
                PostCard(post = samplePost, onLikeClick = {})
            }
        }
    }
}
```

### Performance (Baseline Profiles)
```kotlin
// benchmark/src/main/java/BaselineProfileGenerator.kt
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule val rule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() = rule.collect(
        packageName = "com.example.habitate"
    ) {
        startActivityAndWait()
        device.findObject(By.text("Feed")).click()
        device.waitForIdle()
    }
}
```

### Accessibility
```kotlin
@Composable
fun PostCard(post: Post, onLikeClick: () -> Unit) {
    Card(
        modifier = Modifier.semantics {
            contentDescription = "Post by ${post.author.displayName}: ${post.contentText}"
        }
    ) {
        // ...
        IconButton(
            onClick = onLikeClick,
            modifier = Modifier.semantics {
                contentDescription = if (post.isLiked) "Unlike post" else "Like post"
                role = Role.Button
            }
        ) {
            Icon(if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder)
        }
    }
}
```

---

## Key Integrations

- **Health Connect API** for fitness data (opt-in, privacy-first)
- **Firebase/Supabase** for auth + realtime
- **FCM** for push notifications
- **S3/CloudFront** for media storage
- **Apollo Kotlin** for GraphQL
- **Mixpanel/Amplitude** for analytics

---
*Last updated: December 2025*
