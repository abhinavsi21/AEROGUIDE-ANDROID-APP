# AeroGuide — Personalized Passenger Flight Intelligence & Airport Navigation System

**Team:** Cloud Crew  
**Competition:** AIEM IDEAS  
**Tagline:** *Your Flight. Your Gate. Your Way.*  
**Primary Hub:** Manohar International Airport (GOX), Mopa, Goa, India.

> **Important Operational Disclaimer:** This prototype operates with a high-fidelity simulated AODB (Airport Operational Database), FIDS, flight radar, and indoor positioning environment. All radar and passenger wayfinding coordinates are simulated for demonstration purposes.

---

## 1. Product Vision & Positioning

Traditional airport Flight Information Display Systems (FIDS) tell passengers **what is happening** (flight number, departure time, status, and gate number). However, passengers must still manually decipher:
- Where is my gate located in the terminal?
- How far is it and how long will it take to walk there?
- When should I leave my current position?
- Has my gate changed?
- Is there sufficient time before gate closure?

**Core Positioning:**
> *"A FIDS tells you what is happening. AeroGuide tells you what to do next."*

```
Flight + Passenger + Location + Time + Operational Events
                        ↓
            Personalized Intelligence
                        ↓
               Actionable Guidance
```

---

## 2. Key Upgraded Features

1. **Passwordless Name-and-Email Login:**
   - Low-friction onboarding: passenger full name + email address. No password required.
   - Individual user records saved in Firebase Cloud Firestore (`users/{uid}` and `loginEvents/{eventId}`).
   - Instant 1-tap demo setup for Air India flight AI 1234.
   - Dynamic passenger greeting across all dashboard and concierge screens.

2. **Dedicated Airspace Flight Radar:**
   - Real-time 360° radar scope centered around Manohar International Airport (GOX / Mopa: 15.767° N, 73.868° E).
   - 16 distinct simulated aircraft with live telemetry (altitude, speed, heading, distance, route).
   - Airline filters (Air India, IndiGo, Vistara, Inbound, Outbound) and live flight search.
   - Track demo flight AI 1234 approaching Goa at 32,000 ft.

3. **Prominent Terminal Navigation Map:**
   - 2D terminal concourse rendering for Mopa Terminal 1 Post-Security Level 2.
   - Gates 20, 21, 22, 24, 25, 26, 31 with interactive nodes and waypoints.
   - Food Court starting point, Security Checkpoints, Duty Free Plaza, Maharaja Lounge.
   - Pulsing `YOU ARE HERE` marker with animated directional route polyline.

4. **Gate Change Transition Engine:**
   - Full reactive transition when operational changes occur: **Gate 24 → Gate 31**.
   - Immediate recalculation: Distance updates from `650m` to `850m`, walking ETA from `8 min` to `11 min`.
   - Visual transition banner with animated route re-pathing and high-priority alert generation.

5. **Complete Dark / Light Theme System:**
   - Global toggle accessible via header and profile settings.
   - Immediate theme switching across all views, radar, maps, and dialogs.
   - Theme preference saved to storage and Firestore profile.

6. **State-Aware Airport Concierge:**
   - Context-aware intelligence engine providing instant answers regarding gate number, distance, walking ETA, and flight status.
   - Reacts immediately to simulation updates and gate relocations.

7. **Interactive AIEM Flight Simulator:**
   - Six interactive lifecycle stages:
     1. `Scheduled` (12:00)
     2. `Check-in Open` (13:00)
     3. `Boarding` (14:25, Gate 24, 650m, 8 min)
     4. `Gate Change` (14:32, Gate 31, 850m, 11 min)
     5. `Final Call` (14:40, 5 min to gate closure)
     6. `Departed` (15:10, Airborne)

---

## 3. Technology Architecture

- **Android App:** Kotlin, Jetpack Compose, Material Design 3, StateFlow, Coroutines
- **Backend & Cloud Persistence:** Firebase Cloud Firestore & Firebase Authentication
- **Package ID:** `com.aistudio.aeroguide.cxzqwb`
