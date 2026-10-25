package com.example.data.model

enum class AlertType {
    BOARDING,
    GATE_CHANGE,
    FINAL_CALL,
    DELAY,
    INFORMATION,
    DEPARTED,
    SECURITY,
    CHECK_IN,
    WEATHER,
    FLIGHT_STATUS
}

data class FlightAlert(
    val id: String,
    val flightNumber: String = "AI 1234",
    val type: AlertType,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val isUrgent: Boolean = false
)
