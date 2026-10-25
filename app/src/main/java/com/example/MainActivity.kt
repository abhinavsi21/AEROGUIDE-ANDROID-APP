package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.AeroGuideRepository
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class ActiveScreen {
    MAIN_TABS,
    FLIGHT_DETAILS,
    ASK_ASSISTANT,
    JOURNEY_TIMELINE,
    BOARDING_PASS
}

class MainActivity : ComponentActivity() {

    private lateinit var repository: AeroGuideRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val storage = com.example.data.storage.AeroStorage(applicationContext)
        repository = AeroGuideRepository(storage)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(storage.isDarkTheme()) }

            AeroGuideTheme(darkTheme = isDarkTheme) {
                AeroGuideApp(
                    repository = repository,
                    storage = storage,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = {
                        val newMode = !isDarkTheme
                        isDarkTheme = newMode
                        storage.setDarkTheme(newMode)
                    }
                )
            }
        }
    }
}

@Composable
fun AeroGuideApp(
    repository: AeroGuideRepository,
    storage: com.example.data.storage.AeroStorage? = null,
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {}
) {
    val isLoggedIn by repository.isLoggedIn.collectAsStateWithLifecycle()
    val currentFlight by repository.currentFlight.collectAsStateWithLifecycle()
    val currentStage by repository.currentStage.collectAsStateWithLifecycle()
    val profile by repository.profile.collectAsStateWithLifecycle()
    val booking by repository.booking.collectAsStateWithLifecycle()
    val passengerLocation by repository.passengerLocation.collectAsStateWithLifecycle()
    val alerts by repository.alerts.collectAsStateWithLifecycle()
    val showGateChangeBanner by repository.showGateChangeBanner.collectAsStateWithLifecycle()
    val isNavigating by repository.isNavigating.collectAsStateWithLifecycle()
    val currentNavStepIndex by repository.currentNavStepIndex.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(AeroNavTab.HOME) }
    var activeScreen by remember { mutableStateOf(ActiveScreen.MAIN_TABS) }
    var showSimulationSheet by remember { mutableStateOf(false) }

    val unreadAlertCount = remember(alerts) { alerts.count { !it.isRead } }
    val recommendation = remember(currentStage, currentFlight, passengerLocation) {
        repository.getTimeIntelligenceRecommendation()
    }
    val navSteps = remember(currentFlight.gate) {
        repository.getNavigationSteps()
    }

    // Hardware Back Button handling
    BackHandler(enabled = activeScreen != ActiveScreen.MAIN_TABS || showSimulationSheet) {
        if (showSimulationSheet) {
            showSimulationSheet = false
        } else if (activeScreen != ActiveScreen.MAIN_TABS) {
            activeScreen = ActiveScreen.MAIN_TABS
        }
    }

    if (!isLoggedIn) {
        WelcomeAuthScreen(
            repository = repository,
            storage = storage
        )
        return
    }

    // Simulation Bottom Sheet Modal
    if (showSimulationSheet) {
        SimulationSheet(
            currentStage = currentStage,
            onSelectStage = { stage ->
                repository.setSimulationStage(stage)
                showSimulationSheet = false
            },
            onNextStage = {
                repository.nextSimulationStage()
            },
            onReset = {
                repository.resetSimulation()
                showSimulationSheet = false
            },
            onDismiss = {
                showSimulationSheet = false
            }
        )
    }

    when (activeScreen) {
        ActiveScreen.ASK_ASSISTANT -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(modifier = Modifier.fillMaxSize().widthIn(max = 680.dp)) {
                    AskAeroGuideScreen(
                        passengerName = profile.fullName,
                        onAnswerQuery = { query -> repository.answerPassengerQuery(query) },
                        onBack = { activeScreen = ActiveScreen.MAIN_TABS }
                    )
                }
            }
        }
        ActiveScreen.JOURNEY_TIMELINE -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(modifier = Modifier.fillMaxSize().widthIn(max = 680.dp)) {
                    JourneyTimelineScreen(
                        flight = currentFlight,
                        currentStage = currentStage,
                        onBack = { activeScreen = ActiveScreen.MAIN_TABS }
                    )
                }
            }
        }
        ActiveScreen.BOARDING_PASS -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(modifier = Modifier.fillMaxSize().widthIn(max = 680.dp)) {
                    BoardingPassScreen(
                        profile = profile,
                        flight = currentFlight,
                        onBack = { activeScreen = ActiveScreen.MAIN_TABS },
                        onNavigateToGate = {
                            activeScreen = ActiveScreen.MAIN_TABS
                            currentTab = AeroNavTab.NAVIGATE
                            repository.startNavigation()
                        }
                    )
                }
            }
        }
        ActiveScreen.FLIGHT_DETAILS -> {
            Scaffold(
                topBar = {
                    AeroTopBar(
                        flightNumber = currentFlight.flightNumber,
                        stage = currentStage,
                        unreadAlertCount = unreadAlertCount,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onOpenSimulation = { showSimulationSheet = true },
                        onOpenAlerts = { currentTab = AeroNavTab.ALERTS; activeScreen = ActiveScreen.MAIN_TABS },
                        onOpenAssistant = { activeScreen = ActiveScreen.ASK_ASSISTANT }
                    )
                },
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(modifier = Modifier.fillMaxSize().widthIn(max = 680.dp)) {
                        FlightDetailsScreen(
                            flight = currentFlight,
                            onBack = { activeScreen = ActiveScreen.MAIN_TABS },
                            onRefresh = { repository.setSimulationStage(currentStage) },
                            onOpenBoardingPass = { activeScreen = ActiveScreen.BOARDING_PASS }
                        )
                    }
                }
            }
        }
        ActiveScreen.MAIN_TABS -> {
            Scaffold(
                topBar = {
                    AeroTopBar(
                        flightNumber = currentFlight.flightNumber,
                        stage = currentStage,
                        unreadAlertCount = unreadAlertCount,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onOpenSimulation = { showSimulationSheet = true },
                        onOpenAlerts = { currentTab = AeroNavTab.ALERTS },
                        onOpenAssistant = { activeScreen = ActiveScreen.ASK_ASSISTANT }
                    )
                },
                bottomBar = {
                    AeroBottomNav(
                        currentTab = currentTab,
                        unreadAlertCount = unreadAlertCount,
                        onTabSelected = { tab -> currentTab = tab }
                    )
                },
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 680.dp)
                    ) {
                        when (currentTab) {
                            AeroNavTab.HOME -> {
                                HomeScreen(
                                    profile = profile,
                                    flight = currentFlight,
                                    passengerLocation = passengerLocation,
                                    currentStage = currentStage,
                                    recommendation = recommendation,
                                    showGateChangeBanner = showGateChangeBanner,
                                    onNavigateClick = {
                                        currentTab = AeroNavTab.NAVIGATE
                                        repository.startNavigation()
                                    },
                                    onFlightDetailsClick = { activeScreen = ActiveScreen.FLIGHT_DETAILS },
                                    onOpenMapClick = { currentTab = AeroNavTab.NAVIGATE },
                                    onOpenSimulationClick = { showSimulationSheet = true },
                                    onOpenAssistantClick = { activeScreen = ActiveScreen.ASK_ASSISTANT },
                                    onDismissGateChangeBanner = { repository.dismissGateChangeBanner() },
                                    onLoadDemoBooking = { repository.loadDemoBooking() },
                                    onOpenBoardingPass = { activeScreen = ActiveScreen.BOARDING_PASS },
                                    onOpenJourney = { activeScreen = ActiveScreen.JOURNEY_TIMELINE },
                                    onViewAllAlerts = { currentTab = AeroNavTab.ALERTS }
                                )
                            }
                            AeroNavTab.RADAR -> {
                                FlightRadarScreen(
                                    repository = repository,
                                    onBack = { currentTab = AeroNavTab.HOME }
                                )
                            }
                            AeroNavTab.NAVIGATE -> {
                                NavigationScreen(
                                    currentGate = currentFlight.gate,
                                    passengerLocation = passengerLocation,
                                    facilities = repository.airportFacilities,
                                    steps = navSteps,
                                    currentStepIndex = currentNavStepIndex,
                                    isNavigating = isNavigating,
                                    onStartNavigation = { repository.startNavigation() },
                                    onStopNavigation = { repository.stopNavigation() },
                                    onNextStep = { repository.nextNavStep() },
                                    onPrevStep = { repository.prevNavStep() },
                                    onOpenBoardingPass = { activeScreen = ActiveScreen.BOARDING_PASS }
                                )
                            }
                            AeroNavTab.ALERTS -> {
                                AlertsScreen(
                                    alerts = alerts,
                                    onMarkAsRead = { alertId -> repository.markAlertAsRead(alertId) },
                                    onMarkAllAsRead = { repository.markAllAlertsAsRead() },
                                    onClearAll = { repository.clearAllAlerts() },
                                    onNavigateClick = {
                                        currentTab = AeroNavTab.NAVIGATE
                                        repository.startNavigation()
                                    },
                                    onBoardingPassClick = { activeScreen = ActiveScreen.BOARDING_PASS }
                                )
                            }
                            AeroNavTab.PROFILE -> {
                                ProfileScreen(
                                    profile = profile,
                                    booking = booking,
                                    flight = currentFlight,
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = onToggleTheme,
                                    onResetDemo = { repository.resetSimulation() },
                                    onOpenSimulation = { showSimulationSheet = true },
                                    onOpenJourney = { activeScreen = ActiveScreen.JOURNEY_TIMELINE },
                                    onOpenBoardingPass = { activeScreen = ActiveScreen.BOARDING_PASS },
                                    onLogout = { repository.logout() },
                                    onUpdateProfile = { name, phone -> repository.updateProfile(name, phone) },
                                    onLookupBooking = { ref -> repository.lookupBooking(ref) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
