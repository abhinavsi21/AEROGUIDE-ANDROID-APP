package com.example.data.repository

import com.example.data.firebase.FirebaseConfig
import com.example.data.firebase.FirebaseSession
import com.example.data.firebase.FirebaseUser
import com.example.data.model.*
import com.example.data.storage.AeroStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class AeroGuideRepository(private val storage: AeroStorage? = null) {

    // Initial simulation stage restored from storage or default to BOARDING
    private val initialStage = storage?.getLastSimulationStage() ?: SimulationStage.BOARDING
    private val _currentStage = MutableStateFlow(initialStage)
    val currentStage: StateFlow<SimulationStage> = _currentStage.asStateFlow()

    // Mock Flights from AeroGuide Specification (GOX Mopa Hub)
    val availableMockFlights = listOf(
        Flight(
            id = "AI1234",
            flightNumber = "AI 1234",
            airline = "Air India",
            airlineLogoName = "AI",
            originCity = "Mumbai (CSMI T1)",
            originCode = "BOM",
            destinationCity = "Goa (Manohar Int'l Airport, Mopa)",
            destinationCode = "GOX",
            terminal = "T1",
            gate = "24",
            status = FlightStatus.BOARDING,
            departureTime = "15:10",
            boardingTime = "14:25",
            gateCloseTime = "14:45",
            delayMinutes = 0,
            aircraft = "Airbus A321neo",
            seat = "14C",
            boardingGroup = "B",
            baggageCarousel = "Belt 02 (Mopa)"
        ),
        Flight(
            id = "6E6123",
            flightNumber = "6E 6123",
            airline = "IndiGo",
            airlineLogoName = "6E",
            originCity = "Delhi",
            originCode = "DEL",
            destinationCity = "Goa (Mopa)",
            destinationCode = "GOX",
            terminal = "T1",
            gate = "22",
            status = FlightStatus.CHECK_IN_OPEN,
            departureTime = "15:35",
            boardingTime = "14:50",
            gateCloseTime = "15:15",
            delayMinutes = 0,
            aircraft = "Airbus A320neo",
            seat = "18F",
            boardingGroup = "C",
            baggageCarousel = "Belt 03 (Mopa)"
        ),
        Flight(
            id = "UK881",
            flightNumber = "UK 881",
            airline = "Vistara",
            airlineLogoName = "UK",
            originCity = "Bengaluru",
            originCode = "BLR",
            destinationCity = "Goa (Mopa)",
            destinationCode = "GOX",
            terminal = "T1",
            gate = "25",
            status = FlightStatus.SCHEDULED,
            departureTime = "16:00",
            boardingTime = "15:15",
            gateCloseTime = "15:40",
            delayMinutes = 15,
            aircraft = "Airbus A320",
            seat = "04K",
            boardingGroup = "A",
            baggageCarousel = "Belt 01 (Mopa)"
        ),
        Flight(
            id = "QP1102",
            flightNumber = "QP 1102",
            airline = "Akasa Air",
            airlineLogoName = "QP",
            originCity = "Ahmedabad",
            originCode = "AMD",
            destinationCity = "Goa (Mopa)",
            destinationCode = "GOX",
            terminal = "T1",
            gate = "20",
            status = FlightStatus.SCHEDULED,
            departureTime = "16:20",
            boardingTime = "15:35",
            gateCloseTime = "16:00",
            delayMinutes = 0,
            aircraft = "Boeing 738 MAX",
            seat = "10B",
            boardingGroup = "B",
            baggageCarousel = "Belt 02 (Mopa)"
        )
    )

    // FLIGHT RADAR: 16 Simulated Aircraft centered around Manohar International Airport GOX / Mopa (15.767° N, 73.868° E)
    val radarAircraftList = listOf(
        RadarAircraft("AI1234", "AI 1234", "Air India", "A321neo", 15.820, 73.740, 0.46f, 0.38f, 32000, 450, 135, "BOM → GOX", "Approaching GOX", 42, "16:25", isDemo = true),
        RadarAircraft("6E6123", "6E 6123", "IndiGo", "A320neo", 15.950, 73.680, 0.35f, 0.22f, 18000, 380, 150, "DEL → GOX", "Descending", 65, "16:40"),
        RadarAircraft("UK881", "UK 881", "Vistara", "A320", 15.610, 73.950, 0.62f, 0.65f, 12000, 340, 325, "BLR → GOX", "Inbound", 38, "16:32"),
        RadarAircraft("QP1102", "QP 1102", "Akasa Air", "B738 MAX", 16.120, 73.550, 0.24f, 0.12f, 24000, 420, 160, "AMD → GOX", "En Route", 110, "17:05"),
        RadarAircraft("AI102", "AI 102", "Air India", "B788 Dreamliner", 15.890, 73.320, 0.18f, 0.32f, 38000, 490, 295, "GOX → LHR", "Climbing", 180, "22:15"),
        RadarAircraft("6E521", "6E 521", "IndiGo", "A321neo", 15.710, 74.080, 0.74f, 0.52f, 16000, 360, 260, "HYD → GOX", "Approaching", 52, "16:48"),
        RadarAircraft("SG304", "SG 304", "SpiceJet", "B737-800", 16.020, 74.310, 0.82f, 0.28f, 28000, 440, 245, "CCU → GOX", "En Route", 140, "17:22"),
        RadarAircraft("IX782", "IX 782", "Air India Express", "B737-800", 15.650, 73.120, 0.12f, 0.56f, 34000, 470, 105, "DXB → GOX", "Inbound", 210, "17:45"),
        RadarAircraft("G8340", "G8 340", "Go First", "A320neo", 15.860, 73.610, 0.32f, 0.42f, 20000, 390, 145, "BOM → GOX", "En Route", 75, "16:55"),
        RadarAircraft("6E892", "6E 892", "IndiGo", "ATR 72-600", 15.790, 73.790, 0.52f, 0.45f, 8000, 260, 110, "PNQ → GOX", "Final Approach", 28, "16:18"),
        RadarAircraft("AI651", "AI 651", "Air India", "A320neo", 15.480, 74.020, 0.68f, 0.78f, 22000, 410, 335, "MAA → GOX", "En Route", 95, "17:10"),
        RadarAircraft("QP1341", "QP 1341", "Akasa Air", "B738", 15.880, 73.810, 0.54f, 0.30f, 15000, 350, 175, "DEL → GOX", "Descending", 48, "16:42"),
        RadarAircraft("UK725", "UK 725", "Vistara", "A321neo", 15.810, 73.820, 0.48f, 0.44f, 10000, 310, 140, "BOM → GOX", "Intercepting", 32, "16:22"),
        RadarAircraft("6E204", "6E 204", "IndiGo", "A320neo", 15.750, 73.910, 0.56f, 0.53f, 4000, 240, 45, "GOX → BOM", "Departed", 14, "16:10"),
        RadarAircraft("AI884", "AI 884", "Air India", "A321", 15.730, 73.940, 0.60f, 0.58f, 6000, 280, 30, "GOX → DEL", "Initial Climb", 20, "16:08"),
        RadarAircraft("I5912", "I5 912", "AIX Connect", "A320", 15.690, 73.980, 0.64f, 0.62f, 14000, 330, 315, "BLR → GOX", "Inbound", 44, "16:38")
    )

    private val _selectedRadarAircraft = MutableStateFlow(radarAircraftList[0])
    val selectedRadarAircraft: StateFlow<RadarAircraft> = _selectedRadarAircraft.asStateFlow()

    fun selectRadarAircraft(callsign: String) {
        val found = radarAircraftList.find { it.callsign.equals(callsign, ignoreCase = true) }
        if (found != null) {
            _selectedRadarAircraft.value = found
        }
    }

    fun getFilteredRadarAircraft(filter: String, query: String): List<RadarAircraft> {
        return radarAircraftList.filter { a ->
            when (filter) {
                "AIR_INDIA" -> a.airline.contains("Air India")
                "INDIGO" -> a.airline.contains("IndiGo")
                "VISTARA" -> a.airline.contains("Vistara")
                "INBOUND" -> a.route.endsWith("GOX")
                "OUTBOUND" -> a.route.startsWith("GOX")
                else -> true
            }
        }.filter { a ->
            if (query.isBlank()) true else {
                val q = query.trim().lowercase(Locale.ROOT)
                a.code.lowercase(Locale.ROOT).contains(q) ||
                a.airline.lowercase(Locale.ROOT).contains(q) ||
                a.route.lowercase(Locale.ROOT).contains(q)
            }
        }
    }

    private val _currentFlight = MutableStateFlow(availableMockFlights[0])
    val currentFlight: StateFlow<Flight> = _currentFlight.asStateFlow()

    private val _profile = MutableStateFlow(storage?.getProfile() ?: PassengerProfile())
    val profile: StateFlow<PassengerProfile> = _profile.asStateFlow()

    private val _booking = MutableStateFlow(
        Booking(
            bookingReference = storage?.getCurrentBookingReference() ?: "AG7X92",
            flightId = availableMockFlights[0].id,
            passengerName = _profile.value.fullName,
            seat = availableMockFlights[0].seat,
            ticketClass = "Economy Extra",
            gate = availableMockFlights[0].gate,
            terminal = availableMockFlights[0].terminal
        )
    )
    val booking: StateFlow<Booking> = _booking.asStateFlow()

    private val _passengerLocation = MutableStateFlow(
        PassengerLocation(
            name = "Food Court (Level 2)",
            terminal = "T1",
            level = "Level 2 (Central Atrium)",
            distanceMeters = 650,
            walkingMinutes = 8,
            xRatio = 0.55f,
            yRatio = 0.38f
        )
    )
    val passengerLocation: StateFlow<PassengerLocation> = _passengerLocation.asStateFlow()

    private val _alerts = MutableStateFlow(
        listOf(
            FlightAlert(
                id = "alt_1",
                flightNumber = "AI 1234",
                type = AlertType.BOARDING,
                title = "Boarding Started",
                message = "Flight AI 1234 to Goa (Mopa GOX) is now boarding at Gate 24. Boarding Group B may proceed.",
                timestamp = "14:25",
                isRead = false,
                isUrgent = false
            ),
            FlightAlert(
                id = "alt_2",
                flightNumber = "AI 1234",
                type = AlertType.INFORMATION,
                title = "DigiYatra Express Flow",
                message = "Average security wait time at Mopa Terminal 1 E-Gates is currently 2 minutes.",
                timestamp = "14:15",
                isRead = true,
                isUrgent = false
            )
        )
    )
    val alerts: StateFlow<List<FlightAlert>> = _alerts.asStateFlow()

    private val _showGateChangeBanner = MutableStateFlow(false)
    val showGateChangeBanner: StateFlow<Boolean> = _showGateChangeBanner.asStateFlow()

    private val _isNavigating = MutableStateFlow(false)
    val isNavigating: StateFlow<Boolean> = _isNavigating.asStateFlow()

    private val _currentNavStepIndex = MutableStateFlow(0)
    val currentNavStepIndex: StateFlow<Int> = _currentNavStepIndex.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(storage?.isLoggedIn() ?: false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // GOX Mopa Terminal 1 Facilities
    val terminalFacilities = listOf(
        AirportFacility("f_food", "Food Court (Level 2)", FacilityType.FOOD, "FOOD", 0.55f, 0.38f, "L2", "Central Food Court & Dining"),
        AirportFacility("f_sec", "Security & DigiYatra", FacilityType.SECURITY, "SEC", 0.35f, 0.32f, "L2", "Main Passenger Screening"),
        AirportFacility("f_duty", "Goa Duty Free Plaza", FacilityType.DUTY_FREE, "DUTY", 0.48f, 0.45f, "L2", "Duty Free Shopping & Souvenirs"),
        AirportFacility("f_rest1", "Restrooms Central", FacilityType.RESTROOM, "WC", 0.40f, 0.40f, "L2", "Accessible Restrooms & Nursery"),
        AirportFacility("f_lounge", "Mopa Maharaja Lounge", FacilityType.LOUNGE, "LNG", 0.62f, 0.45f, "L2", "VIP & Frequent Flyer Lounge"),
        AirportFacility("g_20", "Gate 20", FacilityType.GATE, "G20", 0.68f, 0.22f, "L2", "Regional Departures"),
        AirportFacility("g_21", "Gate 21", FacilityType.GATE, "G21", 0.74f, 0.22f, "L2", "Domestic Departures"),
        AirportFacility("g_22", "Gate 22", FacilityType.GATE, "G22", 0.80f, 0.22f, "L2", "Domestic Departures"),
        AirportFacility("g_24", "Gate 24", FacilityType.GATE, "G24", 0.86f, 0.22f, "L2", "Domestic Departures - AI 1234 Initial"),
        AirportFacility("g_25", "Gate 25", FacilityType.GATE, "G25", 0.70f, 0.65f, "L2", "Domestic Departures"),
        AirportFacility("g_26", "Gate 26", FacilityType.GATE, "G26", 0.78f, 0.65f, "L2", "Domestic Departures"),
        AirportFacility("g_31", "Gate 31", FacilityType.GATE, "G31", 0.86f, 0.65f, "L2", "Concourse South End - AI 1234 Relocated")
    )

    init {
        if (initialStage != SimulationStage.BOARDING) {
            setSimulationStage(initialStage)
        }
    }

    fun isDarkTheme(): Boolean = storage?.isDarkTheme() ?: true

    fun setDarkTheme(isDark: Boolean) {
        storage?.setDarkTheme(isDark)
    }

    fun setSimulationStage(stage: SimulationStage) {
        _currentStage.value = stage
        storage?.saveSimulationStage(stage)

        val prevFlight = _currentFlight.value
        val isGateChange = stage == SimulationStage.GATE_CHANGE || stage == SimulationStage.FINAL_CALL || stage == SimulationStage.DEPARTED
        val currentGate = if (isGateChange) "31" else "24"
        val prevGate = if (isGateChange) "24" else null

        _currentFlight.value = prevFlight.copy(
            gate = currentGate,
            previousGate = prevGate,
            status = stage.status,
            lastUpdated = "Just now (${stage.timeLabel})"
        )

        _passengerLocation.value = _passengerLocation.value.copy(
            distanceMeters = stage.distanceMeters,
            walkingMinutes = stage.walkingMinutes
        )

        _booking.value = _booking.value.copy(
            gate = currentGate
        )

        if (stage == SimulationStage.GATE_CHANGE) {
            _showGateChangeBanner.value = true
        }

        stage.alertMessage?.let { msg ->
            val alertType = when (stage) {
                SimulationStage.CHECK_IN_OPEN -> AlertType.INFORMATION
                SimulationStage.BOARDING -> AlertType.BOARDING
                SimulationStage.GATE_CHANGE -> AlertType.GATE_CHANGE
                SimulationStage.FINAL_CALL -> AlertType.FINAL_CALL
                SimulationStage.DEPARTED -> AlertType.DEPARTED
                else -> AlertType.INFORMATION
            }
            val title = when (stage) {
                SimulationStage.CHECK_IN_OPEN -> "Check-in Open"
                SimulationStage.BOARDING -> "Boarding Started"
                SimulationStage.GATE_CHANGE -> "Gate Changed to Gate 31"
                SimulationStage.FINAL_CALL -> "Urgent: Final Call"
                SimulationStage.DEPARTED -> "Flight Departed"
                else -> "Flight Update"
            }
            addAlert(
                FlightAlert(
                    id = "alt_${System.currentTimeMillis()}",
                    flightNumber = _currentFlight.value.flightNumber,
                    type = alertType,
                    title = title,
                    message = msg,
                    timestamp = stage.timeLabel,
                    isRead = false,
                    isUrgent = stage == SimulationStage.FINAL_CALL || stage == SimulationStage.GATE_CHANGE
                )
            )
        }
    }

    fun nextSimulationStage() {
        val stages = SimulationStage.values()
        val nextIndex = (_currentStage.value.ordinal + 1) % stages.size
        setSimulationStage(stages[nextIndex])
    }

    fun resetSimulation() {
        setSimulationStage(SimulationStage.BOARDING)
        _showGateChangeBanner.value = false
        _isNavigating.value = false
        _currentNavStepIndex.value = 0
    }

    fun dismissGateChangeBanner() {
        _showGateChangeBanner.value = false
    }

    fun addAlert(alert: FlightAlert) {
        _alerts.value = listOf(alert) + _alerts.value
    }

    fun markAlertAsRead(alertId: String) {
        _alerts.value = _alerts.value.map {
            if (it.id == alertId) it.copy(isRead = true) else it
        }
    }

    fun markAllAlertsAsRead() {
        _alerts.value = _alerts.value.map { it.copy(isRead = true) }
    }

    fun startNavigation() {
        _isNavigating.value = true
        _currentNavStepIndex.value = 0
    }

    fun stopNavigation() {
        _isNavigating.value = false
    }

    fun nextNavigationStep() {
        val maxSteps = getNavigationSteps().size
        if (_currentNavStepIndex.value < maxSteps - 1) {
            _currentNavStepIndex.value += 1
        } else {
            stopNavigation()
        }
    }

    fun nextNavStep() = nextNavigationStep()

    fun prevNavStep() {
        if (_currentNavStepIndex.value > 0) {
            _currentNavStepIndex.value -= 1
        }
    }

    fun clearAllAlerts() {
        _alerts.value = emptyList()
    }

    val airportFacilities: List<AirportFacility> get() = terminalFacilities

    fun getNavigationSteps(): List<NavigationStep> {
        val isGate31 = _currentFlight.value.gate == "31"
        return if (!isGate31) {
            listOf(
                NavigationStep(0, "Depart Food Court (Level 2)", "Walk through central dining court toward concourse entrance", 120, 90),
                NavigationStep(1, "Pass Central Atrium", "Follow overhead signage toward Gates 20–26", 200, 150),
                NavigationStep(2, "Walk along Concourse North", "Continue past Gates 21 and 22", 230, 160),
                NavigationStep(3, "Arrive at Gate 24", "Gate 24 boarding podium on your right", 100, 80)
            )
        } else {
            listOf(
                NavigationStep(0, "Depart Food Court (Level 2)", "Turn toward Concourse South passage", 150, 110),
                NavigationStep(1, "Pass Central Plaza", "Follow updated electronic signs toward Concourse South", 250, 190),
                NavigationStep(2, "Traverse South Walkway", "Walk along moving sidewalk past Gate 30", 300, 220),
                NavigationStep(3, "Arrive at Gate 31 (Relocated)", "Gate 31 boarding gate podium straight ahead", 150, 140)
            )
        }
    }

    fun getTimeIntelligenceRecommendation(): String {
        val flight = _currentFlight.value
        val loc = _passengerLocation.value
        val stage = _currentStage.value

        return when (stage) {
            SimulationStage.CHECK_IN_OPEN -> "Check-in is open at Counter 14. Proceed to security when ready."
            SimulationStage.BOARDING -> "Gate ${flight.gate} is ${loc.distanceMeters}m away (${loc.walkingMinutes} min walk). Leave now for relaxed boarding."
            SimulationStage.GATE_CHANGE -> "URGENT: Gate changed to Gate 31 (${loc.distanceMeters}m · ${loc.walkingMinutes} min). Proceed immediately."
            SimulationStage.FINAL_CALL -> "FINAL CALL! Gate 31 closing imminent. Proceed immediately to gate."
            SimulationStage.DEPARTED -> "Flight departed on schedule. Thank you for using AeroGuide."
            else -> "Gate ${flight.gate} is ${loc.distanceMeters}m away. Keep track of boarding calls."
        }
    }

    fun answerPassengerQuery(query: String): String {
        val q = query.trim().lowercase(Locale.ROOT)
        val flight = _currentFlight.value
        val loc = _passengerLocation.value
        val stage = _currentStage.value

        return when {
            q.contains("where am i") || q.contains("my location") || q.contains("current location") -> {
                "You are currently at ${loc.name} (${loc.level}) in Terminal ${loc.terminal}, approximately ${loc.distanceMeters} metres (${loc.walkingMinutes} min walk) from your departure Gate ${flight.gate}."
            }
            q.contains("where") || q.contains("which gate") || q.contains("find gate") -> {
                "Your flight ${flight.flightNumber} departs from Gate ${flight.gate} in Terminal ${flight.terminal}. You are currently at ${loc.name}, approximately ${loc.distanceMeters} metres away."
            }
            q.contains("how far") || q.contains("distance") || q.contains("how long") || q.contains("walking") -> {
                "Gate ${flight.gate} is ${loc.distanceMeters} metres from your current position (${loc.name}). Estimated walking time is about ${loc.walkingMinutes} minutes."
            }
            q.contains("when should i leave") || q.contains("leave") || q.contains("time to go") -> {
                if (stage == SimulationStage.FINAL_CALL || stage == SimulationStage.GATE_CHANGE) {
                    "Proceed immediately! Gate ${flight.gate} is ${loc.distanceMeters}m away (${loc.walkingMinutes} min walk) and closes at ${flight.gateCloseTime}."
                } else {
                    "Your gate is ${loc.distanceMeters} metres away and takes about ${loc.walkingMinutes} minutes to reach. Your gate closes at ${flight.gateCloseTime}, so you should leave now to ensure relaxed boarding."
                }
            }
            q.contains("boarding pass") || q.contains("ticket") || q.contains("barcode") -> {
                "Your digital boarding pass is available for Flight ${flight.flightNumber}, Seat ${flight.seat} (Group ${flight.boardingGroup}) departing Gate ${flight.gate}. You can open and export it anytime from the Boarding Pass screen."
            }
            q.contains("change") || q.contains("gate change") -> {
                if (flight.gate == "31") {
                    "Yes, your gate has relocated from Gate 24 to Gate 31 in Concourse South. The updated walking distance is 850 metres (11 minutes walk). Please follow overhead signs to Concourse South."
                } else {
                    "Your gate is currently Gate 24 as scheduled. No gate change has occurred."
                }
            }
            q.contains("aeroguide") || q.contains("what is this") || q.contains("who are you") -> {
                "AeroGuide is a personalized passenger flight intelligence and airport navigation system developed by Cloud Crew for Manohar International Airport (GOX / Mopa), providing real-time wayfinding, gate tracking, and operational alerts."
            }
            q.contains("live") || q.contains("real-time") || q.contains("real time") -> {
                "AeroGuide is operating in demo simulation mode connected to modeled AODB and FIDS flight operational feeds for Manohar International Airport (GOX). Air traffic and indoor wayfinding are simulated for prototype evaluation."
            }
            q.contains("radar") -> {
                "Flight Radar is actively monitoring 16 aircraft centered around Manohar International Airport (GOX / Mopa). Flight ${flight.flightNumber} is currently at 32,000 ft approaching Goa."
            }
            q.contains("boarding") || q.contains("start") -> {
                "Boarding for ${flight.flightNumber} started at ${flight.boardingTime}. Boarding group ${flight.boardingGroup} is currently being called at Gate ${flight.gate}."
            }
            q.contains("status") || q.contains("delay") || q.contains("schedule") -> {
                "Flight ${flight.flightNumber} (${flight.originCity} to ${flight.destinationCity}) status is ${flight.status.label}. Departure is scheduled for ${flight.departureTime} from Gate ${flight.gate} (closes at ${flight.gateCloseTime})."
            }
            q.contains("seat") || q.contains("baggage") || q.contains("belt") -> {
                "Your seat on ${flight.flightNumber} is ${flight.seat} (Group ${flight.boardingGroup}). Checked baggage will arrive at ${flight.baggageCarousel} upon landing."
            }
            else -> {
                "Flight ${flight.flightNumber} to ${flight.destinationCity} departs at ${flight.departureTime} from Gate ${flight.gate}. You are at ${loc.name} (${loc.distanceMeters}m · ${loc.walkingMinutes} min walk). Gate closes at ${flight.gateCloseTime}."
            }
        }
    }

    fun lookupBooking(reference: String): Result<Booking> {
        val cleanRef = reference.trim().uppercase()
        if (cleanRef.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter a booking reference."))
        }

        val matchedFlight = when (cleanRef) {
            "AG7X92" -> availableMockFlights[0]
            "6E6123" -> availableMockFlights[1]
            "UK881" -> availableMockFlights[2]
            "QP1102" -> availableMockFlights[3]
            else -> null
        }

        return if (matchedFlight != null) {
            _currentFlight.value = matchedFlight
            val newBooking = Booking(
                bookingReference = cleanRef,
                flightId = matchedFlight.id,
                passengerName = _profile.value.fullName,
                seat = matchedFlight.seat,
                ticketClass = "Economy Extra",
                gate = matchedFlight.gate,
                terminal = matchedFlight.terminal
            )
            _booking.value = newBooking
            storage?.saveCurrentBookingReference(cleanRef)
            resetSimulation()
            Result.success(newBooking)
        } else {
            Result.failure(
                IllegalArgumentException("We couldn't find booking $cleanRef. Try demo booking AG7X92.")
            )
        }
    }

    // -------------------------------------------------------------
    // FIREBASE & NAME-AND-EMAIL AUTHENTICATION ARCHITECTURE
    // -------------------------------------------------------------

    fun getFirebaseConfig(): FirebaseConfig {
        return storage?.getFirebaseConfig() ?: FirebaseConfig()
    }

    fun saveFirebaseConfig(apiKey: String, projectId: String, appId: String = "") {
        storage?.saveFirebaseConfig(apiKey, projectId, appId)
    }

    /**
     * High-frictionless Name-and-Email Login Flow (No password required)
     * Sets up demo profile and session in storage / Firebase.
     */
    fun loginWithNameAndEmail(fullName: String, email: String): Result<PassengerProfile> {
        val cleanName = fullName.trim()
        val cleanEmail = email.trim().lowercase()

        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your full name."))
        }
        if (cleanEmail.isBlank() || !cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }

        val uid = "usr_" + cleanEmail.replace("@", "_").replace(".", "_")
        val updatedProfile = _profile.value.copy(
            id = uid,
            email = cleanEmail,
            fullName = cleanName
        )
        _profile.value = updatedProfile
        storage?.saveProfile(updatedProfile)
        storage?.setLoggedIn(true, cleanEmail, cleanName, uid)
        _isLoggedIn.value = true
        _booking.value = _booking.value.copy(passengerName = cleanName)

        // Save demo session
        val demoSession = FirebaseSession(
            accessToken = "demo_token_${System.currentTimeMillis()}",
            refreshToken = "demo_refresh",
            user = FirebaseUser(uid = uid, email = cleanEmail, fullName = cleanName)
        )
        storage?.saveFirebaseSession(demoSession)

        return Result.success(updatedProfile)
    }

    fun loadDemoBooking() {
        if (!_isLoggedIn.value) {
            loginWithNameAndEmail("Passenger", "passenger@aeroguide.com")
        } else {
            lookupBooking("AG7X92")
        }
    }

    fun login(email: String, fullName: String) {
        loginWithNameAndEmail(fullName, email)
    }

    fun logout() {
        storage?.clearSession()
        _isLoggedIn.value = false
    }

    fun updateProfile(fullName: String, phone: String) {
        val updated = _profile.value.copy(
            fullName = fullName,
            phone = phone
        )
        _profile.value = updated
        _booking.value = _booking.value.copy(passengerName = fullName)
        storage?.saveProfile(updated)
    }
}
