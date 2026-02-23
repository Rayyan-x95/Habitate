package com.ninety5.habitate.ui.screens.publicapi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninety5.habitate.domain.model.Book
import com.ninety5.habitate.domain.model.Meal
import com.ninety5.habitate.domain.model.Quote
import com.ninety5.habitate.domain.model.Weather
import com.ninety5.habitate.domain.repository.PublicApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PublicApiUiState(
    val weather: Weather? = null,
    val quote: Quote? = null,
    val meal: Meal? = null,
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PublicApiViewModel @Inject constructor(
    private val repository: PublicApiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublicApiUiState())
    val uiState: StateFlow<PublicApiUiState> = _uiState.asStateFlow()

    fun loadWeather(lat: Double, long: Double) {
        launchDataLoad(
            call = { repository.getCurrentWeather(lat, long) },
            onSuccess = { weather -> _uiState.update { it.copy(weather = weather) } }
        )
    }

    fun loadRandomQuote() {
        launchDataLoad(
            call = { repository.getRandomQuote() },
            onSuccess = { quote -> _uiState.update { it.copy(quote = quote) } }
        )
    }

    fun loadRandomMeal() {
        launchDataLoad(
            call = { repository.getRandomMeal() },
            onSuccess = { meal -> _uiState.update { it.copy(meal = meal) } }
        )
    }

    fun searchBooks(query: String) {
        if (query.isBlank()) return
        launchDataLoad(
            call = { repository.searchBooks(query) },
            onSuccess = { books -> _uiState.update { it.copy(books = books) } }
        )
    }

    private var activeJobs = 0

    private fun <T> launchDataLoad(
        call: suspend () -> Result<T>,
        onSuccess: (T) -> Unit
    ) {
        viewModelScope.launch {
            activeJobs++
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                call()
                    .onSuccess(onSuccess)
                    .onFailure { e ->
                        _uiState.update { it.copy(error = e.message) }
                    }
            } finally {
                activeJobs--
                if (activeJobs == 0) {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }
}
