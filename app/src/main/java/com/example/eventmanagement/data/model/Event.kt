package com.example.eventmanagement.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlin.collections.find
import kotlin.text.isNotBlank

/**
 * Event Data Model
 * Sesuai dengan Class Diagram: Event
 * Digunakan di semua screen untuk representasi data event
 */
@Parcelize
data class Event(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("date")
    val date: String = "",

    @SerializedName("time")
    val time: String = "",

    @SerializedName("location")
    val location: String = "",

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("capacity")
    val capacity: Int? = null,

    @SerializedName("status")
    val status: String = "upcoming",

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
) : Parcelable {

    /**
     * Fungsi helper untuk mendapatkan label status dalam Bahasa Indonesia
     */
    fun getStatusLabel(): String {
        return when (status) {
            "upcoming" -> "Akan Datang"
            "ongoing" -> "Berlangsung"
            "completed" -> "Selesai"
            "cancelled" -> "Dibatalkan"
            else -> status
        }
    }

    /**
     * Fungsi helper untuk validasi data event
     */
    fun isValid(): Boolean {
        return title.isNotBlank() &&
                date.isNotBlank() &&
                time.isNotBlank() &&
                location.isNotBlank() &&
                status.isNotBlank()
    }
}

/**
 * Enum class untuk status event
 */
enum class EventStatus(val value: String, val label: String) {
    UPCOMING("upcoming", "Akan Datang"),
    ONGOING("ongoing", "Sedang Berlangsung"),
    COMPLETED("completed", "Selesai"),
    CANCELLED("cancelled", "Dibatalkan");

    companion object {
        fun fromValue(value: String): EventStatus {
            return values().find { it.value == value } ?: UPCOMING
        }
    }
}