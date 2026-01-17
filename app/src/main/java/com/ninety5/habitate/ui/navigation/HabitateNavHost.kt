package com.ninety5.habitate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ninety5.habitate.ui.screens.activity.ActivityScreen
import com.ninety5.habitate.ui.screens.create.CreateScreen
import com.ninety5.habitate.ui.screens.feed.FeedScreen
import com.ninety5.habitate.ui.screens.habitats.HabitatsScreen
import com.ninety5.habitate.ui.screens.post.PostDetailScreen
import com.ninety5.habitate.ui.screens.profile.ProfileScreen
import com.ninety5.habitate.ui.screens.task.TaskListScreen
import com.ninety5.habitate.ui.screens.workout.WorkoutDetailScreen
import com.ninety5.habitate.ui.screens.auth.LoginScreen
import com.ninety5.habitate.ui.screens.auth.RegisterScreen
import com.ninety5.habitate.ui.screens.auth.ForgotPasswordScreen
import com.ninety5.habitate.ui.screens.auth.VerifyEmailScreen
import com.ninety5.habitate.ui.screens.onboarding.OnboardingScreen
import com.ninety5.habitate.ui.screens.create.CreatePostScreen
import com.ninety5.habitate.ui.screens.create.CreateTaskScreen
import com.ninety5.habitate.ui.screens.settings.HealthConnectPermissionScreen
import com.ninety5.habitate.ui.screens.story.StoryViewerScreen
import com.ninety5.habitate.ui.screens.focus.FocusScreen
import com.ninety5.habitate.ui.screens.settings.SettingsScreen
import com.ninety5.habitate.ui.screens.task.TaskDetailScreen
import com.ninety5.habitate.ui.screens.habitats.HabitatDetailScreen
import com.ninety5.habitate.ui.screens.create.CreateWorkoutScreen
import com.ninety5.habitate.ui.screens.create.CreateHabitatScreen
import com.ninety5.habitate.ui.screens.workout.WorkoutListScreen
import com.ninety5.habitate.ui.screens.profile.EditProfileScreen
import com.ninety5.habitate.ui.screens.chat.ChatListScreen
import com.ninety5.habitate.ui.screens.chat.ChatScreen
import com.ninety5.habitate.ui.screens.auth.WelcomeScreen
import com.ninety5.habitate.ui.screens.challenge.ChallengeDetailScreen
import com.ninety5.habitate.ui.screens.archive.ArchiveScreen

/**
 * Main navigation host for Habitate app.
 * Handles all navigation between screens with proper argument passing.
 */
@Composable
fun HabitateNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Feed.route
) {
    val context = LocalContext.current
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Main bottom navigation screens
        composable(Screen.Feed.route) {
            FeedScreen(
                onPostClick = { postId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId))
                },
                onUserClick = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
                },
                onCreatePostClick = {
                    navController.navigate(Screen.Create.route)
                },
                onNotificationClick = {
                    navController.navigate(Screen.Activity.route)
                },
                onChatClick = {
                    navController.navigate(Screen.ChatList.route)
                },
                onStoryClick = { userId ->
                    navController.navigate(Screen.StoryViewer.createRoute(userId))
                },
                onAddStoryClick = {
                    // Story creation redirects to post creation
                    navController.navigate(Screen.CreatePost.route) 
                }
            )
        }

        composable(Screen.Habitats.route) {
            HabitatsScreen(
                onHabitatClick = { habitatId ->
                    navController.navigate(Screen.HabitatDetail.createRoute(habitatId))
                },
                onCreateHabitat = {
                    navController.navigate(Screen.CreateHabitat.route)
                }
            )
        }

        composable(Screen.Focus.route) {
            FocusScreen(
                navController = navController
            )
        }

        composable(Screen.Planner.route) {
            com.ninety5.habitate.ui.screens.planner.PlannerScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.ChatList.route) {
            ChatListScreen(
                navController = navController,
                onChatClick = { roomId ->
                    navController.navigate(Screen.Chat.createRoute(roomId))
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: return@composable
            ChatScreen(
                navController = navController,
                roomId = roomId
            )
        }

        composable(
            route = Screen.StoryViewer.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            StoryViewerScreen(
                userId = userId,
                onClose = { navController.popBackStack() }
            )
        }


        composable(Screen.Create.route) {
            CreateScreen(
                onNavigateBack = { navController.popBackStack() },
                onCreatePost = { navController.navigate(Screen.CreatePost.route) },
                onCreateTask = { navController.navigate(Screen.CreateTask.route) },
                onCreateHabit = { navController.navigate(Screen.HabitCreate.route) },
                onCreateWorkout = { navController.navigate(Screen.CreateWorkout.route) },
                onCreateHabitat = { navController.navigate(Screen.CreateHabitat.route) },
                onPlannerClick = { navController.navigate(Screen.Planner.route) }
            )
        }

        composable(Screen.Activity.route) {
            ActivityScreen(
                onNotificationClick = { type, id ->
                    when (type) {
                        "post" -> navController.navigate(Screen.PostDetail.createRoute(id))
                        "user" -> navController.navigate(Screen.UserProfile.createRoute(id))
                        "habitat" -> navController.navigate(Screen.HabitatDetail.createRoute(id))
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                onPostClick = { postId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId))
                },
                onWorkoutClick = { workoutId ->
                    navController.navigate(Screen.WorkoutDetail.createRoute(workoutId))
                }

            )
        }

        // Detail screens with arguments
        composable(
            route = Screen.PostDetail.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
            PostDetailScreen(
                postId = postId,
                onBackClick = { navController.popBackStack() },
                onUserClick = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
                }
            )
        }

        composable(
            route = Screen.UserProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            ProfileScreen(
                userId = userId,
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                onPostClick = { postId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId))
                },
                onWorkoutClick = { workoutId ->
                    navController.navigate(Screen.WorkoutDetail.createRoute(workoutId))
                }
            )
        }

        composable(
            route = Screen.WorkoutDetail.route,
            arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getString("workoutId") ?: return@composable
            WorkoutDetailScreen(
                workoutId = workoutId,
                onBackClick = { navController.popBackStack() },
                onShareClick = { /* Share workout */ }
            )
        }

        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            TaskDetailScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.HabitatDetail.route,
            arguments = listOf(navArgument("habitatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val habitatId = backStackEntry.arguments?.getString("habitatId") ?: return@composable
            HabitatDetailScreen(
                habitatId = habitatId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPost = { postId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId))
                },
                onNavigateToUser = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
                },
                onNavigateToChallenge = { challengeId ->
                    navController.navigate(Screen.ChallengeDetail.createRoute(challengeId))
                }
            )
        }

        // Habit screens
        composable(Screen.HabitList.route) {
            com.ninety5.habitate.ui.screens.habit.HabitListScreen(
                navController = navController
            )
        }

        composable(
            route = Screen.HabitDetail.route,
            arguments = listOf(navArgument("habitId") { type = NavType.StringType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: return@composable
            com.ninety5.habitate.ui.screens.habit.HabitDetailScreen(
                habitId = habitId,
                navController = navController
            )
        }

        composable(Screen.HabitCreate.route) {
            com.ninety5.habitate.ui.screens.habit.HabitCreateScreen(
                navController = navController
            )
        }

        composable(
            route = Screen.HabitEdit.route,
            arguments = listOf(navArgument("habitId") { type = NavType.StringType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            com.ninety5.habitate.ui.screens.habit.HabitCreateScreen(
                habitId = habitId,
                navController = navController
            )
        }

        // Task list screen
        composable(Screen.TaskList.route) {
            TaskListScreen(
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                },
                onCreateTaskClick = {
                    navController.navigate(Screen.CreateTask.route)
                }
            )
        }

        // Create flow screens
        composable(Screen.CreatePost.route) {
            CreatePostScreen(
                onNavigateBack = { navController.popBackStack() },
                onPostCreated = { navController.popBackStack() }
            )
        }

        composable(Screen.CreateTask.route) {
            CreateTaskScreen(
                onNavigateBack = { navController.popBackStack() },
                onTaskCreated = { navController.popBackStack() }
            )
        }

        composable(Screen.CreateWorkout.route) {
            CreateWorkoutScreen(
                onNavigateBack = { navController.popBackStack() },
                onWorkoutCreated = { navController.popBackStack() }
            )
        }

        composable(Screen.CreateHabitat.route) {
            CreateHabitatScreen(
                onNavigateBack = { navController.popBackStack() },
                onHabitatCreated = { navController.popBackStack() }
            )
        }

        // Welcome
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // Auth
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = { 
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToVerifyEmail = {
                    navController.navigate(Screen.VerifyEmail.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.VerifyEmail.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.VerifyEmail.route) {
            VerifyEmailScreen(
                onEmailVerified = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.VerifyEmail.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Features
        // HealthConnect, StoryViewer, Pomodoro removed for Beta

        composable(Screen.WorkoutList.route) {
            WorkoutListScreen(
                onBackClick = { navController.popBackStack() },
                onWorkoutClick = { workoutId ->
                    navController.navigate(Screen.WorkoutDetail.createRoute(workoutId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHealthConnect = { navController.navigate(Screen.HealthConnect.route) },
                onNavigateToNotifications = { navController.navigate(Screen.NotificationSettings.route) },
                onNavigateToArchive = { navController.navigate(Screen.Archive.route) },
                onNavigateToPrivacyDashboard = { navController.navigate(Screen.PrivacySettings.route) },
                onNavigateToPublicApi = { navController.navigate(Screen.PublicApi.route) }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.HealthConnect.route) {
            HealthConnectPermissionScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ChallengeDetail.route,
            arguments = listOf(navArgument("challengeId") { type = NavType.StringType })
        ) { backStackEntry ->
            // challengeId is retrieved by ViewModel via SavedStateHandle
            ChallengeDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Archive.route) {
            ArchiveScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PublicApi.route) {
            com.ninety5.habitate.ui.screens.publicapi.PublicApiScreen()
        }

        // Privacy Dashboard
        composable(Screen.PrivacySettings.route) {
            com.ninety5.habitate.ui.screens.settings.PrivacyDashboardScreen(
                navController = navController
            )
        }

        // Notification Settings - simple placeholder for now
        composable(Screen.NotificationSettings.route) {
            com.ninety5.habitate.ui.screens.settings.NotificationSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Journal feature
        composable(Screen.Journal.route) {
            com.ninety5.habitate.ui.screens.journal.JournalScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Studies feature
        composable(Screen.Studies.route) {
            com.ninety5.habitate.ui.screens.studies.StudiesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFocus = { navController.navigate(Screen.Focus.route) }
            )
        }

        // Wellbeing feature
        composable(Screen.Wellbeing.route) {
            com.ninety5.habitate.ui.screens.wellbeing.WellbeingScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToJournal = { navController.navigate(Screen.Journal.route) }
            )
        }

        // Daily Check-in feature
        composable(Screen.DailyCheckIn.route) {
            com.ninety5.habitate.ui.screens.checkin.DailyCheckInScreen(
                onDismiss = { navController.popBackStack() }
            )
        }

        // Insights Dashboard feature
        composable(Screen.Insights.route) {
            com.ninety5.habitate.ui.screens.insights.InsightsDashboardScreen(
                navController = navController
            )
        }
    }
}

