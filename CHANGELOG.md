# Changelog

All notable changes to Habitate are documented in this file.

## [1.3.0] - 2026-01-03

### 🔧 Code Quality & Safety

#### NPE Risk Fixes
- Replaced all `!!` operators with safe null handling across screens
- **EditProfileScreen**: `uiState.user!!` → `uiState.user ?: return@Box`
- **HealthConnectPermissionScreen**: `importStatus!!` → `importStatus?.let { ... }`
- **HabitDetailScreen**: `habitWithLogs!!` → `habitWithLogs?.let { ... }`
- **CreateTaskScreen**: `selectedDate!!` → `selectedDate?.let { ... }`
- **FollowListScreens**: Fixed error message null handling
- **CreateWorkoutScreen**: Fixed error state display
- **CreateHabitatScreen**: Fixed error state display
- **CreateChallengeScreen**: Fixed error state display
- **AuthRepository**: Fixed `response.body()!!` with safe local variable pattern

#### Logging Migration
- Replaced `android.util.Log` with `timber.log.Timber` for proper production logging
- Files updated: AuthRepository, GlyphMatrixController, GlyphInterfaceManager
- Timber logs are automatically stripped in release builds

#### Deprecated API Fixes
- Fixed `Icons.Default.ArrowBack` → `Icons.AutoMirrored.Filled.ArrowBack` (RTL support)
- Updated in Navigation.kt and TaskDetailScreen.kt

### 🗑️ Removed Files

#### Duplicate/Unused Screens
- `auth/OnboardingScreen.kt` - Duplicate of onboarding/OnboardingScreen.kt
- `auth/OnboardingViewModel.kt` - Companion to deleted screen

#### Replaced Components
- `ui/components/ErrorState.kt` - Replaced by designsystem/States.kt
- `ui/components/ErrorStates.kt` - Replaced by designsystem/States.kt
- `ui/components/StateComponents.kt` - Replaced by designsystem/States.kt

#### Obsolete Documentation
- `docs/BETA_RELEASE_STATUS.md`
- `docs/GLYPH_INTEGRATION.md`
- `docs/PRODUCTION_AUDIT_REPORT.md`
- `docs/SECURITY_OPERATIONS.md`
- `docs/TESTING_GUIDE.md`
- `docs/design/` folder (AUDIT_AND_REDESIGN.md, COMPONENT_LIBRARY.md, CORE_SCREENS.md, DESIGN_SYSTEM.md, README.md)
- `docs/v1.2_RELEASE_AUDIT.md`

### 🎨 Design System Migration

All screens now use the new Habitate design system components:
- `HabitateErrorState` - Consistent error UI with retry action
- `HabitateEmptyState` - Empty state with icon and description
- `HabitateLoadingScreen` - Full-screen loading indicator
- `HabitateSkeletonList` - Skeleton loading placeholders

Screens updated:
- PostDetailScreen
- FollowListScreens
- LikedByList
- HabitDetailScreen
- HabitListScreen

### ✨ New Features

#### Privacy Dashboard
- Added Export My Data functionality with confirmation dialog
- Added Delete All Data functionality with confirmation dialog
- Uses proper spacing tokens (Spacing.xxl)

#### HabitDetailViewModel
- Added `retry()` method for error recovery

### 📁 Project Structure

#### New Additions
- `core/analytics/` - Analytics tracking
- `core/audio/` - Audio utilities
- `core/export/` - Data export functionality
- `core/focus/` - Focus mode features
- `core/insights/` - AI insights
- `data/health/` - Health Connect integration
- `ui/components/designsystem/` - Design system components
- Various new screens: Challenge, Focus, Insights, Timeline, Archive

### 🔒 Security

- Improved token handling in AuthRepository
- Proper null safety across authentication flow
- Timber logging prevents log leaks in production

---

## [1.2.0] - Previous Release

See git history for earlier changes.
