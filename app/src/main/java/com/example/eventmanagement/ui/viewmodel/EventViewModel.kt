package com.example.eventmanagement.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventmanagement.data.model.Event
import com.example.eventmanagement.data.model.Statistics
import com.example.eventmanagement.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.onFailure
import kotlin.onSuccess

/**
 * Event ViewModel
 * Sesuai dengan Class Diagram: EventViewModel
 * Mengelola state dan business logic untuk semua screen
 * Menggunakan StateFlow untuk reactive UI updates
 */
class EventViewModel : ViewModel() {

    private val repository = EventRepository()

    // State untuk list events
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    // State untuk single event (detail)
    private val _selectedEvent = MutableStateFlow<Event?>(null)
    val selectedEvent: StateFlow<Event?> = _selectedEvent.asStateFlow()

    // NEW: State untuk event yang sedang di-edit (untuk pre-fill form di CreateEventScreen)
    private val _editEvent = MutableStateFlow<Event?>(null)
    val editEvent: StateFlow<Event?> = _editEvent.asStateFlow()

    // State untuk statistics
    private val _statistics = MutableStateFlow<Statistics?>(null)
    val statistics: StateFlow<Statistics?> = _statistics.asStateFlow()

    // State untuk loading indicator
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State untuk error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // State untuk success message
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    /**
     * Get All Events
     * Use Case: View All Events
     * Dipanggil dari: HomeScreen
     */
    fun getAllEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getAllEvents()
                .onSuccess { eventList ->
                    _events.value = eventList
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load events"
                    _events.value = emptyList()
                }

            _isLoading.value = false
        }
    }

    /**
     * Get Event by ID
     * Use Case: Get Event by ID
     * Activity Diagram: Get Event by ID
     * Dipanggil dari: EventDetailScreen
     */
    fun getEventById(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getEventById(id)
                .onSuccess { event ->
                    _selectedEvent.value = event
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Event not found"
                    _selectedEvent.value = null
                }

            _isLoading.value = false
        }
    }

    /**
     * Get Events by Date
     * Use Case: Get Events by Date
     * Activity Diagram: Get Events by Date
     * Dipanggil dari: EventsByDateScreen
     */
    fun getEventsByDate(date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getEventsByDate(date)
                .onSuccess { eventList ->
                    _events.value = eventList
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load events for this date"
                    _events.value = emptyList()
                }

            _isLoading.value = false
        }
    }

    /**
     * Get Events by Date Range
     */
    fun getEventsByDateRange(dateFrom: String, dateTo: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getEventsByDateRange(dateFrom, dateTo)
                .onSuccess { eventList ->
                    _events.value = eventList
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load events"
                    _events.value = emptyList()
                }

            _isLoading.value = false
        }
    }

    /**
     * Get Events by Status
     */
    fun getEventsByStatus(status: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getEventsByStatus(status)
                .onSuccess { eventList ->
                    _events.value = eventList
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to filter events"
                    _events.value = emptyList()
                }

            _isLoading.value = false
        }
    }

    /**
     * Get Statistics
     * Use Case: View Statistics
     * Dipanggil dari: HomeScreen
     */
    fun getStatistics() {
        viewModelScope.launch {
            repository.getStatistics()
                .onSuccess { stats ->
                    _statistics.value = stats
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load statistics"
                }
        }
    }

    /**
     * Create Event
     * Use Case: Create Event
     * Activity Diagram: Create Event
     * Dipanggil dari: CreateEventScreen
     */
    fun createEvent(event: Event, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            repository.createEvent(event)
                .onSuccess { createdEvent ->
                    _successMessage.value = "Event berhasil dibuat!"
                    _isLoading.value = false
                    // Refresh event list
                    getAllEvents()
                    getStatistics()
                    onSuccess()
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to create event"
                    _isLoading.value = false
                }
        }
    }

    /**
     * Update Event
     * Use Case: Update Event
     * Dipanggil dari: CreateEventScreen (mode edit)
     */
    fun updateEvent(event: Event, onSuccess: () -> Unit = {}) {
        val eventId = event.id ?: return  // Pastikan ID ada untuk update
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            repository.updateEvent(eventId, event)
                .onSuccess { updatedEvent ->
                    _successMessage.value = "Event berhasil diupdate!"
                    _selectedEvent.value = updatedEvent
                    clearEditEvent()  // Clear state edit setelah sukses
                    _isLoading.value = false
                    // Refresh event list
                    getAllEvents()
                    getStatistics()
                    onSuccess()
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to update event"
                    _isLoading.value = false
                }
        }
    }

    /**
     * Delete Event
     * Use Case: Delete Event
     * Dipanggil dari: EventDetailScreen
     */
    fun deleteEvent(id: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            repository.deleteEvent(id)
                .onSuccess {
                    _successMessage.value = "Event berhasil dihapus!"
                    _isLoading.value = false
                    // Refresh event list
                    getAllEvents()
                    getStatistics()
                    onSuccess()
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to delete event"
                    _isLoading.value = false
                }
        }
    }

    // NEW: Fungsi untuk set event yang akan di-edit
    fun setSelectedEventForEdit(event: Event) {
        _editEvent.value = event
    }

    // NEW: Fungsi untuk clear edit event (misal setelah save/update)
    fun clearEditEvent() {
        _editEvent.value = null
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
     * Clear selected event
     */
    fun clearSelectedEvent() {
        _selectedEvent.value = null
    }
}