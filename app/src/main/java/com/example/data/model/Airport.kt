package com.example.data.model

enum class FacilityType {
    GATE,
    FOOD,
    SECURITY,
    RESTROOM,
    DUTY_FREE,
    LOUNGE,
    INFO_DESK
}

data class AirportFacility(
    val id: String,
    val name: String,
    val type: FacilityType,
    val code: String,
    val x: Float, // 0.0f .. 1.0f on map canvas
    val y: Float, // 0.0f .. 1.0f on map canvas
    val floor: String = "L2",
    val description: String = ""
)

data class NavigationStep(
    val stepIndex: Int,
    val instruction: String,
    val detail: String,
    val distanceMeters: Int,
    val etaSeconds: Int,
    val isCompleted: Boolean = false
)
