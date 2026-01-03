# Habitate

**Habitate** is a mobile-first social super-app combining social networking, fitness tracking, habit/task management, and journaling. **Privacy-first** design is a core principle.

> *Not louder. Not faster. Just better.*

## Product Vision
Habitate rejects addictive social media patterns. It is designed to be calm, minimal, and intentional.

### Core Features
1. **Social**: Private habitats, ephemeral stories, real connections
2. **Fitness**: Health Connect integration, workout tracking, challenges
3. **Habits & Tasks**: Daily tracking, streaks, smart reminders
4. **Wellbeing**: Mood tracking, focus sessions, journaling

## Android Application
This repository contains the native Android implementation of Habitate.

### Tech Stack
- **Kotlin** & **Jetpack Compose** (Material3)
- **Room Database** (Offline-first with sync queue)
- **Hilt** Dependency Injection
- **Coroutines & Flow** for reactive streams
- **Health Connect API** for fitness data
- **Timber** for production-safe logging
- **Firebase** (Auth, Firestore, Cloud Messaging)

### Getting Started
1. Clone the repository
2. Open in Android Studio (Hedgehog or newer)
3. Sync Gradle dependencies
4. Build and run on device/emulator (minSdk 29, targetSdk 36)

### Build Commands
```bash
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease        # Release build
./gradlew test                   # Unit tests
```

## Status
**v1.3.0** - Production Ready ✅

### Recent Updates
- ✅ All NPE risks fixed with safe null handling
- ✅ Migrated to new design system (Habitate* components)
- ✅ Removed deprecated icons (RTL support)
- ✅ Production logging with Timber
- ✅ Privacy dashboard with data export/delete
- ✅ Challenge system implemented
- ✅ Focus mode features
- ✅ AI insights engine

See [CHANGELOG.md](CHANGELOG.md) for detailed version history.

## Architecture
```
app/src/main/java/com/ninety5/habitate/
├── core/           # DI modules, utilities, analytics
├── data/           # Repositories, DAOs, entities, DTOs
├── domain/         # Use cases, mappers, business logic
├── service/        # Background services (FCM, Sync)
├── ui/             # Screens, ViewModels, components
├── worker/         # WorkManager jobs
└── util/           # Extensions, helpers
```

## License
Proprietary. All rights reserved.

