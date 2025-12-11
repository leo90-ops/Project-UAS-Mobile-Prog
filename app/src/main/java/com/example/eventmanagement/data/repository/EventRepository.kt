package com.example.eventmanagement.data.repository

import com.example.eventmanagement.data.model.Event
import com.example.eventmanagement.data.model.Statistics
import com.example.eventmanagement.data.network.ApiService
import com.example.eventmanagement.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Event Repository
 * Sesuai dengan Class Diagram: EventRepository
 * Layer untuk mengakses data dari API
 * Digunakan oleh EventViewModel
 */
class EventRepository {

    private val apiService: ApiService = RetrofitClient.apiService

    /**
     * Get All Events
     * Use Case: View All Events
     */
    suspend fun getAllEvents(): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllEvents()
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.isSuccess() && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(kotlin.Exception(apiResponse.message))
                }
            } else {
                Result.failure(kotlin.Exception("Failed to fetch events: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get Event by ID
     * Use Case: Get Event by ID
     * Activity Diagram: Get Event by ID
     */
    suspend fun getEventById(id: String): Result<Event> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getEventById(id)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.isSuccess() && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(kotlin.Exception(apiResponse.message))
                }
            } else {
                Result.failure(kotlin.Exception("Event not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get Events by Date
     * Use Case: Get Events by Date
     * Activity Diagram: Get Events by Date
     */
    suspend fun getEventsByDate(date: String): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getEventsByDate(date)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.isSuccess()) {
                    Result.success(apiResponse.data ?: emptyList())
                } else {
                    Result.failure(kotlin.Exception(apiResponse.message))
                }
            } else {
                Result.failure(kotlin.Exception("Failed to fetch events by date"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get Events by Date Range
     */
    suspend fun getEventsByDateRange(dateFrom: String, dateTo: String): Result<List<Event>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getEventsByDateRange(dateFrom, dateTo)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.isSuccess()) {
                        Result.success(apiResponse.data ?: emptyList())
                    } else {
                        Result.failure(kotlin.Exception(apiResponse.message))
                    }
                } else {
                    Result.failure(kotlin.Exception("Failed to fetch events by date range"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Get Events by Status
     */
    suspend fun getEventsByStatus(status: String): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getEventsByStatus(status)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.isSuccess()) {
                    Result.success(apiResponse.data ?: emptyList())
                } else {
                    Result.failure(kotlin.Exception(apiResponse.message))
                }
            } else {
                Result.failure(kotlin.Exception("Failed to fetch events by status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get Statistics
     * Use Case: View Statistics
     */
    suspend fun getStatistics(): Result<Statistics> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getStatistics()
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.isSuccess() && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(kotlin.Exception(apiResponse.message))
                }
            } else {
                Result.failure(kotlin.Exception("Failed to fetch statistics"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create Event
     * Use Case: Create Event
     * Activity Diagram: Create Event
     */
    suspend fun createEvent(event: Event): Result<Event> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createEvent(event)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.isSuccess() && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(kotlin.Exception(apiResponse.message))
                }
            } else {
                Result.failure(kotlin.Exception("Failed to create event"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update Event
     * Use Case: Update Event
     */
    suspend fun updateEvent(id: String, event: Event): Result<Event> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateEvent(id, event)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.isSuccess() && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(kotlin.Exception(apiResponse.message))
                }
            } else {
                Result.failure(kotlin.Exception("Failed to update event"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete Event
     * Use Case: Delete Event
     */
    suspend fun deleteEvent(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteEvent(id)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.isSuccess()) {
                    Result.success(Unit)
                } else {
                    Result.failure(kotlin.Exception(apiResponse.message))
                }
            } else {
                Result.failure(kotlin.Exception("Failed to delete event"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}