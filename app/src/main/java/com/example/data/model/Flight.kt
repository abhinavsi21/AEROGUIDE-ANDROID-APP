package com.example.data.model

enum class FlightStatus(val label: String) {
    SCHEDULED("Scheduled"),
    CHECK_IN_OPEN("Check-in Open"),
    SECURITY("Security"),
    BOARDING("BOARDING"),
    GATE_CHANGE("GATE CHANGED"),
    FINAL_CALL("FINAL CALL"),
    DEPARTED("DEPARTED")
}

data class Flight(
    val id: String,
    val flightNumber: String,
    val airline: String,
    val airlineLogoName: String,
    val originCity: String,
    val originCode: String,
    val destinationCity: String,
    val destinationCode: String,
    val terminal: String,
    val gate: String,
    val previousGate: String? = null,
    val status: FlightStatus,
    val departureTime: String,
    val boardingTime: String,
    val gateCloseTime: String,
    val delayMinutes: Int = 0,
    val aircraft: String = "Airbus A321neo",
    val seat: String = "14C",
    val boardingGroup: String = "B",
    val baggageCarousel: String = "Belt 02 (Mopa)",
    val lastUpdated: String = "Just now"
)

data class FlightTimelineEvent(
    val step: Int,
    val title: String,
    val subtitle: String,
    val time: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean
)

data class RadarAircraft(
    val callsign: String,
    val code: String,
    val airline: String,
    val model: String,
    val lat: Double,
    val lon: Double,
    val xRatio: Float, // 0.0f..1.0f on radar scope
    val yRatio: Float, // 0.0f..1.0f on radar scope
    val altitudeFt: Int,
    val speedKnots: Int,
    val headingDeg: Int,
    val route: String,
    val status: String,
    val distanceKm: Int,
    val eta: String,
    val isDemo: Boolean = false
)
