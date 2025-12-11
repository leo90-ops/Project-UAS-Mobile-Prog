package com.example.eventmanagement.data.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("status")
    val status: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
) {
    fun isSuccess(): Boolean {
        return status in 200..299
    }

    fun isError(): Boolean {
        return status >= 400
    }
}