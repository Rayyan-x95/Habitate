package com.ninety5.habitate.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class representing all navigation destinations in Habitate.
 * Using sealed class ensures type-safe navigation and exhaustive when statements.
 */
sealed class Screen(val route: String) {
    // Main bottom navigation screens
    object Feed : Screen("feed")
    object Habitats : Screen("habitats")
    object Create : Screen("create")
    object Activity : Screen("activity")
    object Profile : Screen("profile")
    object Studies : Screen("studies")
    object Focus : Screen("focus")
    object Wellbeing : Screen("wellbeing")
    object ChatList : Screen("chat_list")
    object Welcome : Screen("welcome")
    object PublicApi : Screen("public_api")

    // Detail screens with arguments
    object Chat : Screen("chat/{roomId}") {
        fun createRoute(roomId: String) = "chat/$roomId"
    }

    object PostDetail : Screen("post/{postId}") {
        fun createRoute(postId: String) = "post/$postId"
    }
    
    object UserProfile : Screen("user/{userId}") {
        fun createRoute(userId: String) = "user/$userId"
    }
    
    object HabitatDetail : Screen("habitat/{habitatId}") {
        fun createRoute(habitatId: String) = "habitat/$habitatId"
    }
    
    object TaskDetail : Screen("task/{taskId}") {
        fun createRoute(taskId: String) = "task/$taskId"
    }
    
    object WorkoutDetail : Screen("workout/{workoutId}") {
        fun createRoute(workoutId: String) = "workout/$workoutId"
    }


    // Habit screens
    object HabitList : Screen("habits")
    object HabitDetail : Screen("habit/{habitId}") {
        fun createRoute(habitId: String) = "habit/$habitId"
    }
    object HabitCreate : Screen("habit/create")
    object HabitEdit : Screen("habit/edit/{habitId}") {
        fun createRoute(habitId: String) = "habit/edit/$habitId"
    }

    // Auth
    object Login : Screen("auth/login")
    object Register : Screen("auth/register")
    object ForgotPassword : Screen("auth/forgot_password")
    object Onboarding : Screen("auth/onboarding")
    object VerifyEmail : Screen("auth/verify_email")

    // Features
    object HealthConnect : Screen("health_connect")
    object Journal : Screen("journal")
    object StoryViewer : Screen("story/{userId}") {
        fun createRoute(userId: String) = "story/$userId"
    }

    object Planner : Screen("planner")

    // Create flow screens
    object CreatePost : Screen("create/post")
    object CreateTask : Screen("create/task")
    object CreateWorkout : Screen("create/workout")
    object CreateHabitat : Screen("create/habitat")

    // List screens
    object TaskList : Screen("tasks")
    object WorkoutList : Screen("workouts")

    object ChallengeDetail : Screen("challenge/{challengeId}") {
        fun createRoute(challengeId: String) = "challenge/$challengeId"
    }

    object Archive : Screen("archive")

    // Settings
    object Settings : Screen("settings")
    object EditProfile : Screen("settings/profile")
    object PrivacySettings : Screen("settings/privacy")
    object NotificationSettings : Screen("settings/notifications")
    object HealthConnectSettings : Screen("settings/health")
}

/**
 * Bottom navigation items configuration
 */
data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean = false,
    val badgeCount: Int? = null
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Feed,
        label = "Social",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        screen = Screen.Focus,
        label = "Focus",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange
    ),
    BottomNavItem(
        screen = Screen.Create,
        label = "Create",
        selectedIcon = Icons.Filled.Add,
        unselectedIcon = Icons.Outlined.Add
    ),
    BottomNavItem(
        screen = Screen.Habitats,
        label = "Habitats",
        selectedIcon = Icons.Filled.Groups,
        unselectedIcon = Icons.Outlined.Groups
    ),
    BottomNavItem(
        screen = Screen.Profile,
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)
