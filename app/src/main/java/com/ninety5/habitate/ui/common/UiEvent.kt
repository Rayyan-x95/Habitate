package com.ninety5.habitate.ui.common

import com.ninety5.habitate.core.result.AppError

/**
 * Base UI event sealed interface for one-off events.
 *
 * ViewModels should define feature-specific sealed classes that extend this.
 * Use [kotlinx.coroutines.channels.Channel] to emit one-off events.
 *
 * ## Example
 * ```kotlin
 * sealed class FeedEvent : UiEvent {
 *     data class NavigateToPost(val postId: String) : FeedEvent()
 *     data class ShowSnackbar(val message: String) : FeedEvent()
 * }
 * ```
 */
interface UiEvent

/**
 * Common one-off events shared across all features.
 */
sealed class CommonEvent : UiEvent {
    data class ShowSnackbar(val message: String) : CommonEvent()
    data class NavigateTo(val route: String) : CommonEvent()
    data object NavigateBack : CommonEvent()
    data class ShowError(val error: AppError) : CommonEvent()
}
