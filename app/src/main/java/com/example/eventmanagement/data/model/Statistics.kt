package com.example.eventmanagement.data.model

import com.google.gson.annotations.SerializedName

/**
 * Statistics Data ModeStatisticsl
 * Sesuai dengan Class Diagram: Statistics
 * Digunakan di HomeScreen untuk menampilkan statistik event
 */
data class Statistics(
    @SerializedName("total")
    val total: Int = 0,

    @SerializedName("upcoming")
    val upcoming: Int = 0,

    @SerializedName("ongoing")
    val ongoing: Int = 0,

    @SerializedName("completed")
    val completed: Int = 0,

    @SerializedName("cancelled")
    val cancelled: Int = 0
)