# AeroGuide Web Application

A full-featured, responsive airport companion and flight tracking web application mirroring the AeroGuide Android experience with live Supabase integration, interactive terminal navigation, digital boarding passes, and airport services.

## User Review & Critical Decisions

> [!IMPORTANT]
> The web application will be created in a dedicated `web/` workspace within the project. This preserves the existing Android application and Gradle build configurations while providing a standalone modern web app that can be launched, hosted, and shared.

- **Confirmed Decision 1**: Standalone modern web app with identical Supabase flight features and live flight schedules.
- **Recommended Tech Stack**: Modern React 18 with Vite, Tailwind CSS, Lucide icons, and the Supabase JavaScript Client.
- **Recommended Architecture**: Dual-platform structure where the `web/` directory houses the complete web app, while the Android build pipeline remains intact and functional.

---

## 1. Overview & Core Concept

- **What It Does**: AeroGuide Web delivers the airport companion experience directly in any web browser. Passengers can look up live departures and arrivals, view gate assignments and real-time status updates, interact with a visual terminal map for gate pathfinding, view their digital boarding pass with scannable QR code, monitor security checkpoint wait times, browse terminal amenities, and manage their profile.
- **Target Audience / Persona**: Airline passengers navigating airports, travelers checking flight updates from laptops or mobile browsers, and airport visitors seeking gate navigation.
- **Key Value**: Instant, zero-install access to AeroGuide's smart airport intelligence from any desktop, tablet, or smartphone browser, with direct cross-linking to the Android APK for mobile users.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Welcome & Guest/Demo Access**:
   - Clean, professional entry screen allowing users to jump in with an instant demo session (derived from custom email or guest mode) or enter their booking reference.
2. **Flight Radar & Schedule Board**:
   - Real-time departures and arrivals board with instant search, airline filters, gate badges, terminal numbers, and status indicators (On Time, Boarding, Delayed, Landed).
   - Detail drawer showing aircraft model, baggage carousel, weather at destination, and countdown to departure.
3. **Interactive Terminal & Gate Navigator**:
   - Visual terminal map featuring Concourse A, B, and C with interactive gate pins, security checkpoints, dining, and baggage claim.
   - Route pathfinding from entrance/security to the assigned departure gate with estimated walking time.
4. **Digital Boarding Pass & QR Scanner**:
   - High-fidelity boarding pass widget displaying seat number, boarding group, gate, barcode/QR code, flight timing, and baggage allowance.
5. **Airport Services & Amenities Explorer**:
   - Filterable directory of airport restaurants, cafes, duty-free shops, lounges, and accessibility services categorized by terminal and concourse.
6. **Mobile APK Download Banner / Hub**:
   - Header/footer affordance allowing web visitors to directly download the Android `AeroGuide.apk`.

### Visual Identity & Theme

- **Aesthetic Direction**: Ultra-modern aviation dashboard combining deep navy blues, crisp slate neutrals, and electric cyan accents. Glassmorphism cards with subtle borders and refined elevation.
- **Color Tokens**:
  - Primary: Deep Aero Navy (`#0A192F`, `#0F2744`)
  - Accent / Flight Cyan: `#00A3FF`, `#38BDF8`
  - Status Indicators: Emerald Green (`#10B981` On Time), Amber (`#F59E0B` Boarding/Delayed), Crimson (`#EF4444` Cancelled)
  - Surface & Card: `#132238` with `rgba(255, 255, 255, 0.08)` borders
  - Text: High contrast snow white (`#F8FAFC`) and muted slate (`#94A3B8`)
- **Typography**: Space Grotesk / Inter pairing for modern tech-forward legibility, monospace numerals for flight codes, seats, and countdown timers.
- **Responsive Layout**: Fluid desktop grid with supporting panels; switches gracefully to an app-like bottom navigation layout on mobile viewports.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Project Structure & Coexistence**:
  - *Chosen Approach*: Create the web application in a self-contained `web/` directory with a standard Vite + React setup.
  - *Why*: Ensures the Android Gradle build (`app/build.gradle.kts`) and existing APK generation remain 100% stable and error-free, while providing a modern web build with fast HMR and clean production assets.
  - *Alternatives Considered*: Replacing the root with web files would break the existing Android app and APK compilation.
- **Decision 2: Supabase Integration & Offline Demo Fallback**:
  - *Chosen Approach*: Use `@supabase/supabase-js` configured to talk to the AeroGuide Supabase project, with an automatic fallback to local offline flight state if network or credentials are unavailable.
  - *Why*: Guarantees the website is always immediately interactive and reliable, even in environments without live API credentials.

---

## 4. Technical Architecture & Data Strategy

### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────┐
│                   AeroGuide Web App                    │
│                 (React 18 + Tailwind)                  │
└───────────────────────────┬────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ Flight Board │    │ Terminal Map │    │ Boarding Pass│
│  & Schedule  │    │ & Navigation │    │  & QR Code   │
└───────┬──────┘    └───────┬──────┘    └───────┬──────┘
        │                   │                   │
        └───────────────────┼───────────────────┘
                            │
                            ▼
        ┌───────────────────────────────────────┐
        │        Flight & Passenger Store       │
        │           (React Context / Hook)      │
        └───────────────────┬───────────────────┘
                            │
               ┌────────────┴────────────┐
               ▼                         ▼
      ┌──────────────────┐      ┌──────────────────┐
      │  Supabase Client │      │ Mock Data Engine │
      │  (Live API/Sync) │      │ (Instant Demo)   │
      └──────────────────┘      └──────────────────┘
```

### Data Model & State

- **`Flight`**: `id`, `flightNumber`, `airline`, `origin`, `destination`, `departureTime`, `arrivalTime`, `terminal`, `gate`, `status`, `delayMinutes`, `aircraftType`.
- **`PassengerProfile`**: `id`, `fullName`, `email`, `frequentFlyerNumber`, `activeBookingId`.
- **`Booking`**: `bookingReference`, `passengerId`, `flightId`, `seatNumber`, `seatClass`, `boardingGroup`, `gate`, `barcodeValue`.
- **`Amenity`**: `id`, `name`, `category`, `terminal`, `concourse`, `openingHours`, `rating`.

### Interactive Component Mapping

- **`FlightList`**: Real-time search by destination, flight number, or airline; tab toggles for Departures/Arrivals; live badges.
- **`TerminalMap`**: Interactive SVG airport canvas rendering gates, pathways, and animated route line to the user's flight gate.
- **`BoardingPassModal`**: High-resolution digital ticket with flip animation, seat details, and QR barcode.
- **`AmenityDirectory`**: Filter chips for Dining, Shopping, Lounges, and Services with location pins.
- **`DownloadBanner`**: Direct link to download `AeroGuide.apk`.
