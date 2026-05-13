# 💡 Grameen-Light

**Citizen-led streetlight audit app for safer, brighter villages.**

Grameen-Light is a production-grade Android application that empowers rural citizens and Panchayat officials to audit, report, and manage streetlight infrastructure. Built with Jetpack Compose, it features offline-first architecture, AI-powered complaint analysis, and a premium glassmorphism UI with day/night audit modes.

---

## ✨ Features at a Glance

| Feature | Description |
|---|---|
| 🗺️ Interactive Pole Map | Google Maps integration with color-coded streetlight markers |
| 📝 One-Tap Reporting | Submit streetlight complaints in seconds from a bottom sheet |
| 🤖 AI-Powered Analysis | Auto-generated tags, repair suggestions & anomaly detection |
| 🔧 Repair Tracker | Timeline view tracking complaints from Submitted → Assigned → Fixed |
| 📊 Energy Dashboard | Live kWh savings estimates from daytime burning reports |
| 🏛️ Admin Dashboard | Panchayat officials can search, filter, assign & resolve complaints |
| 🌙 Dark Audit Mode | Night-friendly UI with animated star field for evening audits |
| 📴 Offline-First | Full functionality without internet; syncs when connectivity returns |
| 🔐 Secure Auth | Google Sign-In via Credential Manager + anonymous & offline fallback |

---

## 📱 App Screens

### 1. Login Screen
- **Google Sign-In** using Android Credential Manager (modern replacement for legacy sign-in)
- **Anonymous Sign-In** for quick demos via Firebase Anonymous Auth
- **Offline Mode** — generates a local session so the app is usable without any network
- Premium glassmorphism card UI with animated error states

### 2. Map Screen (Pole Audit)
- **Google Maps View** with live markers for every registered streetlight pole
  - 🟢 Green = Working
  - 🔴 Red = Fused
  - 🟡 Yellow = Burning During Daytime
- **List View** — toggle between map and scrollable pole directory
- **Cluster markers** at low zoom levels for villages with many poles
- **Fallback canvas map** when Google Maps API key is not configured
- **Quick Report Sheet** — tap any pole to open a bottom sheet, select the observed status (Working / Fused / Burning Daytime), and submit a report instantly
- **Complaint ID Card** — animated card displays the generated complaint ID and AI tags after submission

### 3. Repairs Screen (Complaint Tracker)
- **Summary cards** showing active vs. fixed complaint counts
- **Timeline stepper** visualizing the complaint lifecycle: `Submitted → Assigned → Fixed`
- Each complaint card displays:
  - Unique complaint ID
  - Associated pole ID
  - Reported status & timestamp
  - Current status with color-coded chip
  - AI-generated tags
  - Last updated timestamp
- Tap any complaint to view its **full detail screen**

### 4. Complaint Detail Screen
- Full-page view of a single complaint
- **Repair Timeline** with animated progress bar
- **Details section** — submission date, last update, reporter ID
- **AI Insights section** — AI-generated tags displayed as chips + AI summary text

### 5. Energy Savings Dashboard
- **Circular Gauge** — animated progress ring showing monthly kWh savings vs. a 40 kWh goal
- **Animated Counter** — smooth number animation for total kWh saved
- **Stat Cards** — daytime alerts, active repairs, fixed count, total poles
- **Weekly Trend Bar Chart** — 7-day complaint trend visualization
- **Anomaly Alerts** — AI-detected patterns (e.g., "High fuse rate — possible supply line issue")
- **Monthly AI Report** — natural-language summary of energy impact from community reports

### 6. Admin Dashboard (Panchayat Panel)
- **Dashboard Stats** — total complaints, resolved count, resolution rate percentage
- **Search Bar** — search by pole ID or complaint ID
- **Filter Chips** — filter by status: All / Submitted / Assigned / Fixed
- **Complaint Management Cards** with:
  - Timeline stepper
  - AI tags and AI summary
  - **Assign** and **Mark Fixed** action buttons
- **Confirmation Bottom Sheet** — confirm before status updates

---

## 🏗️ Architecture

The app follows **Clean Architecture** with MVVM and is organized into four layers:

```
com.example.glight/
├── di/                  # Hilt dependency injection modules
├── domain/              # Business logic layer
│   ├── model/           #   Domain models (Pole, Complaint)
│   ├── repository/      #   Repository interfaces
│   ├── usecase/         #   Use cases (GenerateComplaintTagsUseCase)
│   └── ai/              #   AI provider interface
├── data/                # Data layer
│   ├── local/           #   Room database, DAOs, entities
│   ├── remote/          #   Firebase Realtime Database data source
│   ├── repository/      #   Repository implementations
│   ├── auth/            #   Firebase Auth + offline auth
│   ├── sync/            #   Offline sync queue manager
│   ├── mapper/          #   Entity ↔ Domain model mappers
│   └── ai/              #   OpenRouter AI provider implementation
└── ui/                  # Presentation layer
    ├── screens/         #   Screen composables + ViewModels
    │   ├── login/       #     Login screen
    │   ├── map/         #     Map screen (audit view)
    │   ├── repairs/     #     Repairs tracker + detail screen
    │   ├── savings/     #     Energy dashboard
    │   └── admin/       #     Panchayat admin panel
    ├── components/      #   Reusable UI components
    ├── navigation/      #   NavHost + bottom navigation
    └── theme/           #   Colors, typography, design tokens
```

---

## 🔧 Tech Stack

| Layer | Technology |
|---|---|
| **UI** | Jetpack Compose + Material 3 |
| **Navigation** | Jetpack Navigation Compose |
| **DI** | Hilt (Dagger) |
| **Local DB** | Room (SQLite) |
| **Remote DB** | Firebase Realtime Database |
| **Auth** | Firebase Auth + Android Credential Manager |
| **Maps** | Google Maps SDK + Maps Compose |
| **AI** | OpenRouter API (GPT/free models) with deterministic fallback |
| **Async** | Kotlin Coroutines + Flow |
| **Architecture** | MVVM + Clean Architecture |

---

## 🤖 AI Capabilities

The app integrates AI via the **OpenRouter API** (configurable model) with intelligent fallbacks so the app remains fully functional without an API key:

| Capability | Description |
|---|---|
| **Tag Generation** | Auto-generates 1–3 relevant tags per complaint (e.g., "Daytime Waste", "Switching Error") |
| **Repair Suggestions** | Suggests likely cause and repair action in under 25 words |
| **Complaint Categorization** | Classifies complaints as "Energy Waste", "Safety Hazard", or "Routine Audit" |
| **Anomaly Detection** | Identifies patterns like recurring fuse failures or daytime burning |
| **Monthly Energy Summary** | Generates natural-language reports on community energy savings |

> **Fallback Mode**: When no API key is configured, the app uses rule-based heuristics to generate tags and suggestions, ensuring the demo always works.

---

## 📴 Offline-First Architecture

Grameen-Light is designed for rural areas with unreliable connectivity:

1. **Room Database** caches all poles and complaints locally
2. **SyncQueue** — a local queue (Room entity) that stores pending operations when offline
3. **SyncManager** — processes the queue with:
   - Exponential backoff retry (up to 5 attempts)
   - Automatic sync trigger on new operations
   - Status tracking: `PENDING → COMPLETED / FAILED`
4. **Remote availability check** — gracefully degrades when Firebase is unreachable
5. **Offline authentication** — generates a local session so users can audit without internet

---

## 🎨 Design System

- **Glassmorphism cards** with semi-transparent backgrounds, subtle borders, and soft shadows
- **Day mode** — clean white background with soft blue/yellow ambient glows
- **Night audit mode** — deep dark background with animated star field and green accent borders
- **Animated micro-interactions**:
  - Pulsing status indicators for working poles
  - Ripple effect for fused poles
  - Shimmer sweep for daytime-burning poles
  - Bouncy scale animations on bottom nav selection
  - Typewriter-style complaint ID reveal
  - Smooth gauge progress animation
- **Premium bottom navigation** with floating pill design and theme toggle

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 24+ (target SDK 35)
- A Firebase project with Realtime Database enabled

### Setup Steps

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   cd glight
   ```

2. **Firebase Configuration**
   - Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
   - Enable **Authentication** → Google provider and/or Anonymous provider
   - Enable **Realtime Database**
   - Download `google-services.json` and place it in `app/`
   - Deploy the security rules:
     ```bash
     firebase deploy --only database
     ```
     or manually paste `database.rules.json` into the Firebase Console.

3. **Add keys to `local.properties`**
   ```properties
   # Required for Google Maps
   MAPS_API_KEY=your_google_maps_api_key

   # Required for Google Sign-In
   GOOGLE_WEB_CLIENT_ID=your_oauth_web_client_id

   # Optional — custom Firebase RTDB URL
   FIREBASE_DATABASE_URL=https://your-project.firebaseio.com

   # Optional — enables live AI responses
   OPENROUTER_API_KEY=your_openrouter_api_key
   OPENROUTER_MODEL=openrouter/free
   ```

4. **SHA fingerprints** (for Google Sign-In)
   - Add your debug & release SHA-1 / SHA-256 fingerprints in Firebase Console → Project Settings → Your Apps
   - Re-download `google-services.json` after adding fingerprints

5. **Build & Run**
   ```bash
   ./gradlew installDebug
   ```

> **Quick Demo**: The app seeds demo pole data if Firebase has no poles, so the map works immediately. Use "Continue Offline" on the login screen to skip auth setup entirely.

---

## 📂 Key Files

| File | Purpose |
|---|---|
| `AppModule.kt` | Hilt DI module — wires all dependencies |
| `AppNavigation.kt` | Navigation graph + premium bottom bar |
| `MapScreen.kt` | Pole map with Google Maps + list view + report sheet |
| `RepairsScreen.kt` | Complaint tracker with timeline stepper |
| `ComplaintDetailScreen.kt` | Full complaint detail with AI insights |
| `SavingsScreen.kt` | Energy dashboard with gauge, chart, anomalies |
| `AdminScreen.kt` | Panchayat admin panel with search/filter/actions |
| `SyncManager.kt` | Offline sync queue with retry logic |
| `OpenRouterAiProvider.kt` | AI integration with OpenRouter + fallback |
| `AuthRepositoryImpl.kt` | Google, anonymous & offline authentication |
| `FirebaseRemoteDataSource.kt` | Firebase RTDB read/write operations |
| `PremiumComponents.kt` | Reusable glassmorphism UI components |
| `database.rules.json` | Firebase Realtime Database security rules |

---

## 📄 License

This project is developed as part of an internship project.
