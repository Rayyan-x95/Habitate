package com.ninety5.habitate.ui.screens.social

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.ninety5.habitate.R
import com.ninety5.habitate.data.local.entity.UserEntity
import com.ninety5.habitate.data.repository.UserRepository
import com.ninety5.habitate.data.repository.AuthRepository
import com.ninety5.habitate.ui.components.designsystem.HabitateErrorState
import com.ninety5.habitate.ui.components.designsystem.HabitateEmptyState
import com.ninety5.habitate.ui.components.designsystem.HabitateSkeletonList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Screen displaying list of followers for a user.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowersListScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: FollowersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(userId) {
        viewModel.loadFollowers(userId)
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Followers", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                HabitateSkeletonList(
                    itemCount = 5,
                    modifier = Modifier.padding(padding)
                )
            }
            uiState.error != null -> {
                HabitateErrorState(
                    title = "Something went wrong",
                    description = uiState.error ?: "Unknown error",
                    onRetry = { viewModel.loadFollowers(userId) },
                    modifier = Modifier.padding(padding)
                )
            }
            uiState.followers.isEmpty() -> {
                HabitateEmptyState(
                    title = "No followers yet",
                    description = "Share your content to get followers",
                    icon = Icons.Rounded.PersonAdd,
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.followers) { user ->
                        UserListItem(
                            user = user,
                            isFollowing = uiState.followingIds.contains(user.id),
                            isCurrentUser = uiState.currentUserId == user.id,
                            onUserClick = { onUserClick(user.id) },
                            onFollowClick = { viewModel.toggleFollow(user.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Screen displaying list of users being followed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowingListScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: FollowingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(userId) {
        viewModel.loadFollowing(userId)
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Following", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                HabitateSkeletonList(
                    itemCount = 5,
                    modifier = Modifier.padding(padding)
                )
            }
            uiState.error != null -> {
                HabitateErrorState(
                    title = "Something went wrong",
                    description = uiState.error ?: "Unknown error",
                    onRetry = { viewModel.loadFollowing(userId) },
                    modifier = Modifier.padding(padding)
                )
            }
            uiState.following.isEmpty() -> {
                HabitateEmptyState(
                    title = "Not following anyone yet",
                    description = "Find people to follow and build your network",
                    icon = Icons.Rounded.PersonAdd,
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.following) { user ->
                        UserListItem(
                            user = user,
                            isFollowing = true,
                            isCurrentUser = uiState.currentUserId == user.id,
                            onUserClick = { onUserClick(user.id) },
                            onFollowClick = { viewModel.toggleFollow(user.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserListItem(
    user: UserEntity,
    isFollowing: Boolean,
    isCurrentUser: Boolean,
    onUserClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUserClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.avatarUrl,
            contentDescription = "Avatar of ${user.displayName}",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_profile),
            error = painterResource(R.drawable.ic_profile)
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        
        if (!isCurrentUser) {
            Button(
                onClick = onFollowClick,
                colors = if (isFollowing) {
                    ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                },
                border = if (isFollowing) BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) else null
            ) {
                Text(if (isFollowing) "Unfollow" else "Follow")
            }
        }
    }
}

// ViewModels
@HiltViewModel
class FollowersViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FollowListUiState())
    val uiState: StateFlow<FollowListUiState> = _uiState.asStateFlow()
    
    fun loadFollowers(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val currentUserId = authRepository.getCurrentUserId()
                userRepository.getFollowers(userId).collect { followers ->
                    _uiState.update { it.copy(
                        followers = followers,
                        currentUserId = currentUserId,
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
    
    fun toggleFollow(targetUserId: String) {
        viewModelScope.launch {
            val currentUserId = authRepository.getCurrentUserId() ?: return@launch
            val isFollowing = _uiState.value.followingIds.contains(targetUserId)
            
            if (isFollowing) {
                userRepository.unfollowUser(currentUserId, targetUserId)
            } else {
                userRepository.followUser(currentUserId, targetUserId)
            }
        }
    }
}

@HiltViewModel
class FollowingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FollowListUiState())
    val uiState: StateFlow<FollowListUiState> = _uiState.asStateFlow()
    
    fun loadFollowing(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val currentUserId = authRepository.getCurrentUserId()
                userRepository.getFollowing(userId).collect { following ->
                    _uiState.update { it.copy(
                        following = following,
                        currentUserId = currentUserId,
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
    
    fun toggleFollow(targetUserId: String) {
        viewModelScope.launch {
            val currentUserId = authRepository.getCurrentUserId() ?: return@launch
            userRepository.unfollowUser(currentUserId, targetUserId)
        }
    }
}

data class FollowListUiState(
    val followers: List<UserEntity> = emptyList(),
    val following: List<UserEntity> = emptyList(),
    val followingIds: Set<String> = emptySet(),
    val currentUserId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
