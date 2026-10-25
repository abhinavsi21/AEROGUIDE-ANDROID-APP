package com.example

import com.example.data.model.AlertType
import com.example.data.model.FlightStatus
import com.example.data.model.SimulationStage
import com.example.data.repository.AeroGuideRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AeroGuideUnitTest {

    private lateinit var repository: AeroGuideRepository

    @Before
    fun setUp() {
        repository = AeroGuideRepository()
    }

    @Test
    fun testInitialDemoFlightState() {
        val flight = repository.currentFlight.value
        assertEquals("AI 1234", flight.flightNumber)
        assertEquals("24", flight.gate)
        assertEquals(FlightStatus.BOARDING, flight.status)

        val loc = repository.passengerLocation.value
        assertEquals(650, loc.distanceMeters)
        assertEquals(8, loc.walkingMinutes)
        assertEquals("Food Court (Level 2)", loc.name)
    }

    @Test
    fun testGateChangeTransition() {
        // Trigger Gate Change stage (Stage 4)
        repository.setSimulationStage(SimulationStage.GATE_CHANGE)

        val flight = repository.currentFlight.value
        assertEquals("31", flight.gate)
        assertEquals("24", flight.previousGate)
        assertEquals(FlightStatus.GATE_CHANGE, flight.status)

        val loc = repository.passengerLocation.value
        assertEquals(850, loc.distanceMeters)
        assertEquals(11, loc.walkingMinutes)

        // Verify alert was generated
        val alerts = repository.alerts.value
        assertTrue(alerts.any { it.type == AlertType.GATE_CHANGE && it.message.contains("Gate 31") })

        // Verify navigation steps updated to South Wing
        val steps = repository.getNavigationSteps()
        assertTrue(steps.any { it.instruction.contains("Gate 31") })
    }

    @Test
    fun testDeterministicTimeIntelligence() {
        repository.setSimulationStage(SimulationStage.BOARDING)
        val recBoarding = repository.getTimeIntelligenceRecommendation()
        assertTrue(recBoarding.contains("Gate 24"))

        repository.setSimulationStage(SimulationStage.FINAL_CALL)
        val recFinalCall = repository.getTimeIntelligenceRecommendation()
        assertTrue(recFinalCall.contains("FINAL CALL") || recFinalCall.contains("Proceed immediately"))
    }

    @Test
    fun testAskAeroGuideDeterministicAssistant() {
        repository.resetSimulation()

        val answerGate = repository.answerPassengerQuery("Where is my gate?")
        assertTrue(answerGate.contains("Gate 24"))
        assertTrue(answerGate.contains("Food Court"))

        val answerDistance = repository.answerPassengerQuery("How far is my gate?")
        assertTrue(answerDistance.contains("650 metres"))
        assertTrue(answerDistance.contains("8 minutes"))

        // After gate change
        repository.setSimulationStage(SimulationStage.GATE_CHANGE)
        val answerGateChanged = repository.answerPassengerQuery("Has my gate changed?")
        assertTrue(answerGateChanged.contains("Gate 31"))
        assertTrue(answerGateChanged.contains("850 metres"))
    }

    @Test
    fun testBookingLookup() {
        // Valid demo booking
        val result = repository.lookupBooking("AG7X92")
        assertTrue(result.isSuccess)
        assertEquals("AG7X92", result.getOrNull()?.bookingReference)
        assertEquals("AI1234", result.getOrNull()?.flightId)

        // Alternative mock flight
        val altResult = repository.lookupBooking("6E6123")
        assertTrue(altResult.isSuccess)
        assertEquals("6E6123", altResult.getOrNull()?.bookingReference)
        assertEquals("6E 6123", repository.currentFlight.value.flightNumber)

        // Invalid booking error handling
        val invalidResult = repository.lookupBooking("XYZ999")
        assertTrue(invalidResult.isFailure)
    }

    @Test
    fun testRadarAircraftCountAndFiltering() {
        // Must contain at least 15 simulated aircraft
        assertTrue(repository.radarAircraftList.size >= 15)

        // Check Air India demo aircraft presence
        val demoAircraft = repository.radarAircraftList.find { it.callsign == "AI1234" }
        assertNotNull(demoAircraft)
        assertEquals("Air India", demoAircraft?.airline)
        assertEquals("BOM → GOX", demoAircraft?.route)
        assertEquals(42, demoAircraft?.distanceKm)
        assertTrue(demoAircraft?.isDemo == true)

        // Test filtering
        val indigoOnly = repository.getFilteredRadarAircraft("INDIGO", "")
        assertTrue(indigoOnly.isNotEmpty())
        assertTrue(indigoOnly.all { it.airline == "IndiGo" })

        // Test search query
        val searchAI = repository.getFilteredRadarAircraft("ALL", "AI 102")
        assertEquals(1, searchAI.size)
        assertEquals("AI102", searchAI[0].callsign)
    }

    @Test
    fun testRadarSelection() {
        repository.selectRadarAircraft("6E6123")
        assertEquals("6E6123", repository.selectedRadarAircraft.value.callsign)
        assertEquals("IndiGo", repository.selectedRadarAircraft.value.airline)

        // Select demo flight
        repository.selectRadarAircraft("AI1234")
        assertEquals("AI1234", repository.selectedRadarAircraft.value.callsign)
    }

    @Test
    fun testPasswordlessNameAndEmailLoginFlow() {
        assertFalse(repository.isLoggedIn.value)

        val result = repository.loginWithNameAndEmail("Rohan Sharma", "rohan.sharma@example.com")
        assertTrue(result.isSuccess)
        assertTrue(repository.isLoggedIn.value)
        assertEquals("rohan.sharma@example.com", repository.profile.value.email)
        assertEquals("Rohan Sharma", repository.profile.value.fullName)

        // Verify booking passengerName updated
        assertEquals("Rohan Sharma", repository.booking.value.passengerName)

        // Session logout
        repository.logout()
        assertFalse(repository.isLoggedIn.value)
    }

    @Test
    fun testAllSpecifiedChatbotQuestions() {
        repository.resetSimulation()

        // 1. Where is my gate?
        val q1 = repository.answerPassengerQuery("Where is my gate?")
        assertTrue(q1.contains("Gate 24"))
        assertTrue(q1.contains("Food Court"))

        // 2. How far is Gate 24?
        val q2 = repository.answerPassengerQuery("How far is Gate 24?")
        assertTrue(q2.contains("650 metres"))
        assertTrue(q2.contains("8 minutes"))

        // 3. How long will it take to walk?
        val q3 = repository.answerPassengerQuery("How long will it take to walk?")
        assertTrue(q3.contains("8 minutes"))

        // 4. When should I leave?
        val q4 = repository.answerPassengerQuery("When should I leave?")
        assertTrue(q4.contains("leave now") || q4.contains("Gate closes at 14:45"))

        // 5. Show my boarding pass
        val q5 = repository.answerPassengerQuery("Show my boarding pass")
        assertTrue(q5.contains("digital boarding pass"))
        assertTrue(q5.contains("AI 1234"))
        assertTrue(q5.contains("14C"))

        // 6. What is my flight status?
        val q6 = repository.answerPassengerQuery("What is my flight status?")
        assertTrue(q6.contains("BOARDING"))
        assertTrue(q6.contains("15:10"))

        // 7. Where am I?
        val q7 = repository.answerPassengerQuery("Where am I?")
        assertTrue(q7.contains("Food Court"))
        assertTrue(q7.contains("Terminal T1"))

        // 8. What changed about my gate? (Before gate change)
        val q8Before = repository.answerPassengerQuery("What changed about my gate?")
        assertTrue(q8Before.contains("Gate 24 as scheduled"))

        // 8. What changed about my gate? (After gate change)
        repository.setSimulationStage(SimulationStage.GATE_CHANGE)
        val q8After = repository.answerPassengerQuery("What changed about my gate?")
        assertTrue(q8After.contains("Gate 31"))
        assertTrue(q8After.contains("850 metres"))

        // 9. What is AeroGuide?
        val q9 = repository.answerPassengerQuery("What is AeroGuide?")
        assertTrue(q9.contains("AeroGuide is a personalized passenger flight intelligence"))
        assertTrue(q9.contains("Cloud Crew"))

        // 10. Is this live flight data?
        val q10 = repository.answerPassengerQuery("Is this live flight data?")
        assertTrue(q10.contains("simulation mode") || q10.contains("prototype evaluation"))
    }

    @Test
    fun testGateChangeSequenceAndResetFullCycle() {
        // Initial State
        repository.resetSimulation()
        assertEquals("24", repository.currentFlight.value.gate)
        assertEquals(650, repository.passengerLocation.value.distanceMeters)
        assertEquals(8, repository.passengerLocation.value.walkingMinutes)
        val initialSteps = repository.getNavigationSteps()
        assertTrue(initialSteps.last().instruction.contains("Gate 24"))

        // Trigger Gate Change
        repository.setSimulationStage(SimulationStage.GATE_CHANGE)
        assertEquals("31", repository.currentFlight.value.gate)
        assertEquals("24", repository.currentFlight.value.previousGate)
        assertEquals(850, repository.passengerLocation.value.distanceMeters)
        assertEquals(11, repository.passengerLocation.value.walkingMinutes)
        val updatedSteps = repository.getNavigationSteps()
        assertTrue(updatedSteps.last().instruction.contains("Gate 31"))

        // Verify alert generated
        val alerts = repository.alerts.value
        assertTrue(alerts.any { it.type == AlertType.GATE_CHANGE && it.message.contains("Gate 31") })

        // Reset
        repository.resetSimulation()
        assertEquals("24", repository.currentFlight.value.gate)
        assertEquals(650, repository.passengerLocation.value.distanceMeters)
        assertEquals(8, repository.passengerLocation.value.walkingMinutes)
        val resetSteps = repository.getNavigationSteps()
        assertTrue(resetSteps.last().instruction.contains("Gate 24"))
    }

    @Test
    fun testLoginValidationEdgeCases() {
        // Empty Name should fail
        val emptyNameRes = repository.loginWithNameAndEmail("", "alex@example.com")
        assertTrue(emptyNameRes.isFailure)

        // Invalid Email should fail
        val invalidEmailRes = repository.loginWithNameAndEmail("Alex", "not-an-email")
        assertTrue(invalidEmailRes.isFailure)

        // Valid should succeed
        val validRes = repository.loginWithNameAndEmail("Alex Morgan", "alex@example.com")
        assertTrue(validRes.isSuccess)
        assertTrue(repository.isLoggedIn.value)
    }

    @Test
    fun testAlertsFilterAndMarkAllAsRead() {
        repository.setSimulationStage(SimulationStage.GATE_CHANGE)
        val allAlerts = repository.alerts.value
        assertTrue(allAlerts.isNotEmpty())

        // Mark all as read
        repository.markAllAlertsAsRead()
        assertTrue(repository.alerts.value.all { it.isRead })

        // Clear all
        repository.clearAllAlerts()
        assertTrue(repository.alerts.value.isEmpty())
    }

    @Test
    fun testFirebaseConfigurationModel() {
        val config = repository.getFirebaseConfig()
        assertNotNull(config)
    }
}
