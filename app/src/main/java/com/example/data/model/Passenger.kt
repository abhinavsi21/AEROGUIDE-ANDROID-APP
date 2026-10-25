package com.example.data.model

data class PassengerProfile(
    val id: String = "usr_guest",
    val fullName: String = "Passenger",
    val email: String = "",
    val phone: String = "+1 555-0199",
    val frequentFlyerNumber: String = "AG-PLATINUM-88902",
    val walkingPaceMinutesPerKm: Double = 12.0 // ~5 km/h
)

data class Booking(
    val bookingReference: String = "AG7X92",
    val flightId: String = "AI1234",
    val passengerName: String = "Passenger",
    val seat: String = "14C",
    val ticketClass: String = "Economy Extra",
    val gate: String = "24",
    val terminal: String = "T1"
)

data class PassengerLocation(
    val name: String = "Food Court",
    val terminal: String = "T1",
    val level: String = "Level 2 (Post-Security)",
    val distanceMeters: Int = 650,
    val walkingMinutes: Int = 8,
    val xRatio: Float = 0.35f,
    val yRatio: Float = 0.52f
) {
    val currentZone: String
        get() = "$name, Level 2"
}
