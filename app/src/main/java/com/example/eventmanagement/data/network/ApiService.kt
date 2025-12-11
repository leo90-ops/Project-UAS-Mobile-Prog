package com.example.eventmanagement.data.network

import com.example.eventmanagement.data.model.ApiResponse
import com.example.eventmanagement.data.model.Event
import com.example.eventmanagement.data.model.Statistics
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service Interface
 * Sesuai dengan Class Diagram: ApiService
 * Definisi semua endpoint REST API
 */
interface ApiService {

    /**
     * GET All Events
     * Use Case: View All Events
     * Endpoint: GET /api.php
     */
    @GET("api.php")
    suspend fun getAllEvents(): Response<ApiResponse<List<Event>>>

    /**
     * GET Event by ID
     * Use Case: Get Event by ID
     * Activity Diagram: Get Event by ID
     * Endpoint: GET /api.php?id={id}
     */
    @GET("api.php")
    suspend fun getEventById(@Query("id") id: String): Response<ApiResponse<Event>>

    /**
     * GET Events by Date
     * Use Case: Get Events by Date
     * Activity Diagram: Get Events by Date
     * Endpoint: GET /api.php?date={date}
     */
    @GET("api.php")
    suspend fun getEventsByDate(@Query("date") date: String): Response<ApiResponse<List<Event>>>

    /**
     * GET Events by Date Range
     * Use Case: Get Events by Date (dengan range)
     * Endpoint: GET /api.php?date_from={from}&date_to={to}
     */
    @GET("api.php")
    suspend fun getEventsByDateRange(
        @Query("date_from") dateFrom: String,
        @Query("date_to") dateTo: String
    ): Response<ApiResponse<List<Event>>>

    /**
     * GET Events by Status
     * Use Case: Filter Events
     * Endpoint: GET /api.php?status={status}
     */
    @GET("api.php")
    suspend fun getEventsByStatus(@Query("status") status: String): Response<ApiResponse<List<Event>>>

    /**
     * GET Statistics
     * Use Case: View Statistics
     * Endpoint: GET /api.php?stats=1
     */
    @GET("api.php")
    suspend fun getStatistics(@Query("stats") stats: Int = 1): Response<ApiResponse<Statistics>>

    /**
     * POST Create Event
     * Use Case: Create Event
     * Activity Diagram: Create Event
     * Endpoint: POST /api.php
     */
    @POST("api.php")
    suspend fun createEvent(@Body event: Event): Response<ApiResponse<Event>>

    /**
     * PUT Update Event
     * Use Case: Update Event
     * Endpoint: PUT /api.php?id={id}
     */
    @PUT("api.php")
    suspend fun updateEvent(
        @Query("id") id: String,
        @Body event: Event
    ): Response<ApiResponse<Event>>

    /**
     * DELETE Event
     * Use Case: Delete Event
     * Endpoint: DELETE /api.php?id={id}
     */
    @DELETE("api.php")
    suspend fun deleteEvent(@Query("id") id: String): Response<ApiResponse<Unit>>
}