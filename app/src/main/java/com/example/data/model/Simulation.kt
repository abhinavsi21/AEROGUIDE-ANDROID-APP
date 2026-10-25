package com.example.data.model

enum class SimulationStage(
    val stageName: String,
    val timeLabel: String,
    val status: FlightStatus,
    val gate: String,
    val distanceMeters: Int,
    val walkingMinutes: Int,
    val description: String,
    val recommendation: String,
    val alertMessage: String?
) {
    SCHEDULED(
        stageName = "1. Scheduled",
        timeLabel = "12:00",
        status = FlightStatus.SCHEDULED,
        gate = "24",
        distanceMeters = 650,
        walkingMinutes = 8,
        description = "Flight scheduled. Check-in opens in 1 hour.",
        recommendation = "You have time. Relax in the terminal or visit shops.",
        alertMessage = null
    ),
    CHECK_IN_OPEN(
        stageName = "2. Check-in Open",
        timeLabel = "13:00",
        status = FlightStatus.CHECK_IN_OPEN,
        gate = "24",
        distanceMeters = 650,
        walkingMinutes = 8,
        description = "Check-in counter 14 is open. Baggage drop available.",
        recommendation = "Check-in is open. Head to Security when ready.",
        alertMessage = "Check-in is now open for AI 1234 at Counter 14."
    ),
    BOARDING(
        stageName = "3. Boarding",
        timeLabel = "14:25",
        status = FlightStatus.BOARDING,
        gate = "24",
        distanceMeters = 650,
        walkingMinutes = 8,
        description = "Boarding is in progress for Group B. Gate 24 closes at 14:45.",
        recommendation = "Boarding is in progress. We recommend leaving for Gate 24 now.",
        alertMessage = "Boarding has started for AI 1234 at Gate 24."
    ),
    GATE_CHANGE(
        stageName = "4. Gate Change",
        timeLabel = "14:32",
        status = FlightStatus.GATE_CHANGE,
        gate = "31",
        distanceMeters = 850,
        walkingMinutes = 11,
        description = "Operational Gate Change! Gate relocated from 24 to 31.",
        recommendation = "Urgent: Gate changed to Gate 31 (850m · 11 min). Leave immediately.",
        alertMessage = "Your gate has changed from Gate 24 to Gate 31. Please proceed immediately."
    ),
    FINAL_CALL(
        stageName = "5. Final Call",
        timeLabel = "14:40",
        status = FlightStatus.FINAL_CALL,
        gate = "31",
        distanceMeters = 850,
        walkingMinutes = 11,
        description = "Final boarding call. Gate 31 doors close in 5 minutes.",
        recommendation = "Final call! Proceed immediately to Gate 31. Gate closure imminent.",
        alertMessage = "FINAL CALL for AI 1234. Proceed to Gate 31 immediately."
    ),
    DEPARTED(
        stageName = "6. Departed",
        timeLabel = "15:10",
        status = FlightStatus.DEPARTED,
        gate = "31",
        distanceMeters = 0,
        walkingMinutes = 0,
        description = "Flight AI 1234 has departed for Goa (GOI). Safe travels!",
        recommendation = "Flight departed on schedule. Your AeroGuide journey is complete.",
        alertMessage = "AI 1234 has departed on schedule. Thank you for flying Air India."
    )
}
