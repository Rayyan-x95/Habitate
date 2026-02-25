<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp" width="120" alt="Habitate Logo"/>
</p>

<h1 align="center">Habitate</h1>

<p align="center">
  <strong>A calm, privacy-first social super-app for habits, fitness, and wellbeing</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" alt="Platform"/>
  <img src="https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?logo=jetpackcompose&logoColor=white" alt="Compose"/>
  <img src="https://img.shields.io/badge/Min%20SDK-29-brightgreen" alt="Min SDK"/>
  <img src="https://img.shields.io/badge/Target%20SDK-36-blue" alt="Target SDK"/>
  <img src="https://img.shields.io/badge/AGP-9.0.1-green" alt="AGP"/>
  <img src="https://img.shields.io/badge/Version-1.0.0--public--beta-orange" alt="Version"/>
</p>

<p align="center">
  <em>"Not louder. Not faster. Just better."</em>
</p>

---

## 🌿 About Habitate

Habitate is a **mobile-first social super-app** that combines social networking, fitness tracking, habit management, task planning, journaling, focus sessions, and wellbeing studies—all in one cohesive experience.

Unlike traditional social apps that compete for your attention with endless feeds and dopamine-driven mechanics, Habitate is designed to be **calm, minimal, and intentional**. We believe technology should enhance your life without consuming it.

### 🎯 Design Philosophy

> *"Quietly powerful, thoughtfully designed, effortless to use"*

- **Calm Over Loud** — Muted colors, subtle animations, minimal shadows
- **Function Over Flash** — Every element serves a purpose
- **Breathable Space** — Generous whitespace and padding
- **Privacy First** — Your data belongs to you, always

---

## ✨ Core Features

### 🏠 Social — Habitats
Create and join private communities (called "Habitats") around shared interests. Features include:
- Ephemeral stories that disappear after 24 hours
- Real-time chat with reactions and replies
- Post sharing with comments and likes
- Follow/unfollow without algorithmic manipulation

### 💪 Fitness & Health
Comprehensive fitness tracking powered by Health Connect:
- Step counting and activity monitoring
- Workout logging (strength, cardio, flexibility)
- Fitness challenges with friends
- Health data visualization and insights

### ✅ Habits & Tasks
Build lasting habits and stay organized:
- Daily habit tracking with streak support
- Task management with categories and priorities
- Smart reminders and notifications
- Archive system for completed items

### 🧘 Wellbeing
Tools for mental and emotional health:
- **Focus Sessions** — Pomodoro-style deep work timers
- **Journaling** — Daily reflections with mood tracking
- **Daily Check-ins** — Quick emotional pulse checks
- **AI Insights** — Personalized observations and patterns
- **Wellbeing Studies** — Participate in research studies

### 📊 Insights & Analytics
Understand your patterns:
- AI-powered insights engine
- Weekly and monthly summaries
- Progress visualization
- Data export (JSON format)

---

## 🏗️ Architecture

Habitate follows **Clean Architecture** principles with the **MVVM** pattern, ensuring testability, maintainability, and clear separation of concerns.

```
app/src/main/java/com/ninety5/habitate/
│
├── 📱 HabitateApplication.kt          # Application class with Hilt
├── 🚀 MainActivity.kt                 # Single-activity entry point
│
├── 🔧 core/                            # Core infrastructure
│   ├── analytics/                      # Analytics abstractions
│   ├── audio/                          # HabitateAudioManager (unified audio)
│   ├── di/                             # Hilt DI modules
│   ├── export/                         # Data export functionality
│   ├── focus/                          # Focus session logic
│   ├── glyph/                          # Nothing Phone Glyph integration
│   ├── insights/                       # AI insights engine
│   ├── result/                         # AppResult<T> & AppError types
│   └── utils/                          # Core utilities
│
├── 📦 data/                            # Data layer
│   ├── health/                         # Health Connect integration
│   ├── local/                          # Room DB (v27, 29 entities, exported schema)
│   │   ├── dao/                        # 25 DAOs with Flow return types
│   │   ├── entity/                     # Room entities
│   │   ├── relations/                  # Room relations & views
│   │   └── HabitateDatabase.kt         # Database definition
│   ├── remote/                         # Retrofit APIs, DTOs (Moshi)
│   └── repository/                     # Repository implementations
│
├── 🎯 domain/                          # Domain layer (framework-free)
│   ├── ai/                             # AI domain abstractions
│   ├── mapper/                         # Entity ↔ Domain mappers
│   ├── model/                          # 22 domain models (Post, Habit, Task, etc.)
│   ├── repository/                     # 20 repository interfaces (AppResult-based)
│   └── usecase/                        # UseCase<P,R>, NoParamUseCase, FlowUseCase
│
├── ⚙️ service/                         # Android Services
│   ├── PomodoroService.kt              # Focus/Pomodoro foreground service
│   ├── WorkoutTrackingService.kt       # Workout tracking service
│   └── MyFirebaseMessagingService.kt   # Firebase Cloud Messaging
│
├── 🎨 ui/                              # Presentation layer
│   ├── common/                         # UiEvent, shared UI logic
│   ├── components/                     # Reusable Compose components
│   ├── navigation/                     # NavHost & Screen routes (40+ routes)
│   ├── screens/                        # 24+ feature screens (28+ total including auth settings)
│   ├── theme/                          # Material3 theming (brand-aligned tokens)
│   └── viewmodel/                      # Shared ViewModels (AppViewModel)
│
├── 🔨 util/                            # Utilities
│   ├── FeatureFlags.kt                 # Feature flag interface & implementation
│   ├── FormatUtils.kt                  # Formatting extensions
│   └── audio/                          # Audio utilities (delegates to core)
│
└── 👷 worker/                          # Background work
    ├── SyncWorker.kt                   # Offline sync worker
    ├── SyncScheduler.kt                # Periodic sync scheduling
    ├── UploadWorker.kt                 # Media upload worker
    ├── UserSyncWorker.kt               # User data sync
    ├── ArchivalWorker.kt               # Data archival
    └── StoryCleanupWorker.kt           # Expired story cleanup
```

### Core Patterns

#### Result Wrapper (`core/result/`)
All repository operations return `AppResult<T>` instead of raw exceptions:
```kotlin
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val error: AppError) : AppResult<Nothing>()
    data object Loading : AppResult<Nothing>()
}
```

`AppError` provides typed error categories: `Network`, `Timeout`, `Unauthorized`, `NotFound`, `Validation`, `Database`, etc.

#### Use Case Pattern (`domain/usecase/`)
```kotlin
abstract class UseCase<in P, out R> {
    suspend operator fun invoke(params: P): AppResult<R> = execute(params)
    protected abstract suspend fun execute(params: P): AppResult<R>
}
```

#### Navigation Pattern
Screens receive **lambda callbacks** instead of `NavController` references, keeping composables stateless:
```kotlin
@Composable
fun FeatureScreen(
    uiState: FeatureUiState,
    onNavigateBack: () -> Unit,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
)
```

### Key Architectural Decisions

| Decision | Rationale |
|----------|-----------|
| **Offline-First** | All data cached in Room DB, synced via SyncWorker when online |
| **Single Activity** | Jetpack Compose Navigation with 40+ routes and deep link support |
| **StateFlow for UI** | Lifecycle-aware, efficient state management with Channel for one-off events |
| **Repository Pattern** | 20 domain interfaces with AppResult return types; implementations in data layer |
| **WorkManager** | Reliable background processing with constraints (6 specialized workers) |
| **Lambda Navigation** | Screens receive callbacks, not NavController — enables preview & testing |
| **Typed Errors** | AppError hierarchy replaces raw exceptions for structured error handling |
| **Detekt + Lint** | Static analysis enforced with custom rulesets and lint baseline |

---

## 🛠️ Tech Stack

### Core
| Technology | Version | Purpose |
|------------|---------|---------|
| **Kotlin** | 2.2.10 | Primary language |
| **Jetpack Compose** | BOM 2025.01.01 | Declarative UI |
| **Material3** | Latest | Design system |
| **Coroutines** | 1.10.1 | Async programming |
| **Flow** | - | Reactive streams |

### Android Jetpack
| Library | Version | Purpose |
|---------|---------|---------|
| **Room** | 2.7.1 | Local database (29 entities, exported schema) |
| **Hilt** | 2.56.2 | Dependency injection |
| **Navigation** | 2.9.0 | Screen navigation (40+ routes) |
| **WorkManager** | 2.10.0 | Background tasks (6 workers) |
| **DataStore** | 1.1.4 | Preferences storage |
| **Lifecycle** | 2.9.0 | Lifecycle-aware components |
| **Paging 3** | 3.3.5 | Pagination |
| **Health Connect** | 1.1.0-alpha10 | Fitness data |

### Networking & Data
| Library | Version | Purpose |
|---------|---------|---------|
| **Retrofit** | 2.11.0 | REST API client |
| **OkHttp** | 4.12.0 | HTTP client |
| **Moshi** | 1.15.2 | JSON serialization |
| **Coil** | 2.7.0 | Image loading |

### Firebase
| Service | Purpose |
|---------|---------|
| **Auth** | User authentication |
| **Firestore** | Cloud database |
| **Cloud Messaging** | Push notifications |
| **Crashlytics** | Crash reporting |
| **Performance** | Performance monitoring |
| **Remote Config** | Feature flags |

### Development
| Tool | Purpose |
|------|---------|
| **Timber** | Logging |
| **KSP** | Annotation processing |
| **Detekt** | Static code analysis (custom ruleset) |
| **Android Lint** | Code quality with baseline tracking |
| **Gradle** | Build system (Gradle 9.2.1, AGP 9.0.1) |
| **ProGuard/R8** | Code shrinking |

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Ladybug (2024.2.1) or newer
- **JDK 17** or higher
- **Android SDK** with API 36 installed
- **Git** for version control

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Rayyan-x95/Habitate.git
   cd Habitate
   ```

2. **Configure local.properties**
   
   Create or edit `local.properties` in the project root:
   ```properties
   sdk.dir=/path/to/Android/Sdk
   
   # API Keys (required for full functionality)
   OPENAI_API_KEY=your_openai_api_key
   GOOGLE_WEB_CLIENT_ID=your_google_client_id
   
   # Optional integrations
   NOTHING_GLYPH_KEY=your_glyph_key
   
   # Release signing (for release builds)
   RELEASE_STORE_FILE=keystore/release.jks
   RELEASE_STORE_PASSWORD=your_store_password
   RELEASE_KEY_ALIAS=your_key_alias
   RELEASE_KEY_PASSWORD=your_key_password
   ```

   > ⚠️ **Security Note**
   > 
   > - **Never commit `local.properties` to version control** — it contains sensitive API keys and passwords.
   > - Ensure `local.properties` is listed in your `.gitignore` (it is by default in this project).
   > - For CI/CD pipelines, use **environment variables** or a **secret management service** (e.g., GitHub Secrets, Azure Key Vault, AWS Secrets Manager) instead of storing secrets in files.

3. **Sync and build**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run on device/emulator**
   - Connect an Android device (API 29+) or start an emulator
   - Click "Run" in Android Studio or use:
   ```bash
   ./gradlew installDebug
   ```

### Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing config)
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Clean build
./gradlew clean

# Generate KSP code (Room, Hilt, Moshi)
./gradlew kspDebugKotlin

# Check for dependency updates
./gradlew dependencyUpdates
```

---

## 📱 Screens & Navigation

Habitate features **28+ screens** organized into logical flows:

### Main Navigation (Bottom Bar)
| Screen | Route | Description |
|--------|-------|-------------|
| Feed | `feed` | Social feed with posts and stories |
| Focus | `focus` | Focus session timer and history |
| Create | `create` | Quick create hub |
| Habitats | `habitats` | Community discovery and management |
| Profile | `profile` | User profile and settings |

### Feature Screens
| Category | Screens |
|----------|---------|
| **Social** | Chat, Post Detail, User Profile, Story Viewer |
| **Fitness** | Workouts, Workout Detail, Challenges, Health Connect |
| **Productivity** | Habits, Tasks, Planner, Archive |
| **Wellbeing** | Journal, Daily Check-in, Insights, Studies |
| **Settings** | Edit Profile, Privacy, Notifications, Public API |

### Auth Flow
| Screen | Route |
|--------|-------|
| Welcome | `welcome` |
| Login | `auth/login` |
| Register | `auth/register` |
| Forgot Password | `auth/forgot_password` |
| Verify Email | `auth/verify_email` |
| Onboarding | `auth/onboarding` |

---

## 🎨 Design System

Habitate uses a custom design system built on Material3 with a calm, nature-inspired palette:

### Color Palette

| Color | Light | Dark | Usage |
|-------|-------|------|-------|
| **Primary** | `#2D5A47` | `#6B9E8C` | Primary actions, focus states |
| **Accent** | `#B8956A` | `#CFA06A` | Highlights, special actions |
| **Success** | `#5A8A72` | `#7AAF94` | Completed states |
| **Warning** | `#C4956B` | `#D4A574` | Caution states |
| **Error** | `#B56B6B` | `#CF8A8A` | Error states (calm, not alarming) |
| **Background** | `#FAFAF8` | `#0F1412` | App background |

### Typography
- **Display** — Poppins Bold for headlines
- **Body** — Inter for readable content
- **Mono** — JetBrains Mono for code/data

See [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md) for the complete design specification.

---

## 🔐 Privacy & Security

Privacy is not a feature—it's a **core principle**:

- ✅ **On-device first** — Data is stored locally before syncing
- ✅ **Encrypted storage** — Sensitive data uses EncryptedSharedPreferences
- ✅ **Minimal permissions** — Only request what's absolutely needed
- ✅ **Data export** — Export all your data in JSON format
- ✅ **Data deletion** — Delete your account and all associated data
- ✅ **No tracking** — No third-party analytics or advertising SDKs
- ✅ **Health data protection** — Health Connect data requires explicit opt-in

### Required Permissions
| Permission | Purpose |
|------------|---------|
| `INTERNET` | API communication |
| `POST_NOTIFICATIONS` | Push notifications (Android 13+) |
| `health.READ_*` | Health Connect integration (opt-in) |
| `FOREGROUND_SERVICE` | Focus sessions, workout tracking |

---

## 🧪 Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# Test coverage report
./gradlew jacocoTestReport
```

### Testing Stack
- **JUnit 5** — Unit testing
- **MockK** — Mocking framework
- **Turbine** — Flow testing
- **Compose Testing** — UI tests
- **Espresso** — Instrumented tests

---

## 📦 Project Status

| Milestone | Status |
|-----------|--------|
| Core Architecture | ✅ Complete |
| Social Features | ✅ Complete |
| Fitness Tracking | ✅ Complete |
| Habit Management | ✅ Complete |
| Focus Sessions | ✅ Complete |
| AI Insights | ✅ Complete |
| Privacy Dashboard | ✅ Complete |
| Public Beta | 🟡 In Progress |
| Production Release | 🔜 Coming Soon |

---

## 🤝 Contributing

Habitate is currently in private development. If you're interested in contributing, please reach out to the team.

---

## 📄 License

**Proprietary** — All rights reserved.

Copyright © 2024-2026 Ninety5. Unauthorized copying, modification, distribution, or use of this software is strictly prohibited.

---

<p align="center">
  <strong>Built with 💚 by Ninety5</strong>
</p>

<p align="center">
  <em>Habitate — Where habits become habitats for growth</em>
</p>

