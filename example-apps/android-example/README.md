# Sophi Paywall Android Example

This is a complete Android example app demonstrating integration with the Sophi Paywall library using domain-driven design principles.

## Overview

This example shows how to:
- Import the Sophi Paywall library from GitHub Packages
- Implement user and device dimension repositories, backed by a **real rolling-window
  engagement tracker** (not a static mock) — see [Rolling-Window Tracking](#rolling-window-tracking)
- Capture page/session referrer URLs in user dimensions
- Toggle a visitor between anonymous and registered with a Sign In/Sign Out control,
  and see how it changes paywall decisions for the same content
- Make paywall decisions, including passing `contentProperties`
- Display paywall UI and a full decision-debug panel (trace/context/inputs/score)
- Track page views and wall impressions back into the repository

## Project Structure

```
app/src/main/java/news/publisher/
├── NewsApplication.kt                    # Application entry point
├── domain/                               # Domain layer (business logic)
│   ├── model/
│   │   ├── Article.kt                   # Article domain model
│   │   └── User.kt                      # User domain model
│   └── paywall/
│       ├── PaywallDecisionService.kt    # Paywall decision interface
│       └── WallDecision.kt              # Decision result model (trace/context/inputs/score)
├── infrastructure/                       # Infrastructure layer (external integrations)
│   ├── paywall/
│   │   ├── types.kt                     # Dimension type definitions with extensive docs
│   │   ├── UserDimensionRepository.kt   # Rolling-window (day-bucketed) metrics implementation
│   │   ├── DeviceDimensionRepository.kt # Device dimensions implementation
│   │   └── SophiPaywallAdapter.kt       # Sophi library adapter
│   └── storage/
│       └── UserMetricsStore.kt          # Local storage for metrics
└── ui/                                   # UI layer (presentation)
    ├── MainActivity.kt                  # Switches between Home and Article screens
    ├── home/
    │   ├── HomeScreen.kt                # Article list + Sign In/Sign Out control
    │   └── HomeViewModel.kt             # Sample article catalog + visitor state
    ├── article/
    │   ├── ArticleScreen.kt             # Article display with paywall + decision debug panel
    │   └── ArticleViewModel.kt          # Article business logic
    └── theme/
        └── Theme.kt                     # Compose theme
```

## Setup

### 1. Configure GitHub Packages Authentication

Create or edit `~/.gradle/gradle.properties` and add your GitHub credentials:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN
```

**Creating a GitHub Personal Access Token:**
1. Go to GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token with `read:packages` scope
3. Copy the token and use it as `gpr.key`

### 2. Update Host ID

In `ArticleScreen.kt`, replace `"your-host-id"` with your actual Sophi host ID:

```kotlin
val paywallService = SophiPaywallAdapter(
    context = context,
    hostId = "your-actual-host-id"  // ← Update this
)
```

### 3. Build and Run

```bash
./gradlew assembleDebug
```

Or open the project in Android Studio and run it.

## Key Features

### Rolling-Window Tracking

`UserDimensionRepositoryImpl` stores one `DayBucket` per calendar day (today plus up to
28 days of history) in DataStore, and rolls the history forward automatically whenever a
day boundary is crossed. `today*`/`sevenDay*`/`twentyEightDay*` dimensions are computed by
summing disjoint windows of that history — the same day-bucketing approach Sophi's web SDK
uses — instead of being static/flat values. Call `trackPageView()` and `trackWallView()`
from your real content-view events to feed it.

### Login/Logout Demo

The Home screen's Sign In/Sign Out button flips `visitorType` between `anonymous` and
`registered` via `SophiPaywallAdapter.setVisitorType()`. Open the same article signed out,
then signed in, to see how `visitorType` changes the decision.

### Referrer Handling

The `UserDimensionRepository.kt` captures raw referrer URLs (`pageReferrer`, `sessionReferrer`).
The paywall SDK derives medium/source/channel internally from those URLs.

### Dimension Types Documentation

The `types.kt` file provides comprehensive documentation for all dimension types:

- **UserDimensions**: 27 fields tracking engagement over 1-day, 7-day, and 28-day periods
- **DeviceDimensions**: Hour of day, lowercase OS, and viewer/build identifier
- **Enum Types**: VisitorType

Each field includes:
- Clear description
- Practical examples
- Usage notes

### Domain-Driven Design

The architecture follows DDD principles:

1. **Domain Layer**: Pure business logic, no dependencies on external libraries
2. **Infrastructure Layer**: Sophi library integration via adapter pattern
3. **UI Layer**: Compose-based presentation

This separation makes the code:
- **Testable**: Domain logic can be tested without Sophi library
- **Maintainable**: Changes to Sophi library only affect adapter
- **Clear**: Each layer has a single responsibility

## Integration Guide

### Step 1: Initialize PaywallDecider

```kotlin
val paywallService = SophiPaywallAdapter(
    context = context,
    hostId = "your-host-id"
)
```

### Step 2: Get Paywall Decision

```kotlin
val decision = paywallService.decide(
    contentId = "article-123",
    assignedGroup = null  // or "variant"/"control" for A/B testing
)
```

### Step 3: Handle Decision

```kotlin
when (decision.wallType) {
    WallType.PAYWALL -> showPaywall()
    WallType.REGWALL -> showRegistrationWall()
    WallType.NONE -> showFullContent()
}
```

### Step 4: Track Analytics

```kotlin
analyticsSDK.trackPageView(
    contentId = article.id,
    properties = mapOf(
        "sophi_trace" to decision.trace,
        "sophi_context" to decision.context,
        "sophi_inputs" to decision.inputs,
        "sophi_experiment_group" to decision.experimentGroup
    )
)
```

## Dependencies

- **Sophi Paywall Kit**: `io.sophi.android:paywall-kit:0.0.4-alpha.1`
- **Jetpack Compose**: Latest stable
- **DataStore**: For local metrics storage
- **Coroutines**: For async operations

## Production Checklist

Before deploying to production:

- [ ] Replace `"your-host-id"` with actual Sophi host ID
- [ ] Wire `trackPageView()`/`trackWallView()` into your real content-view and analytics events
- [ ] Add proper error handling and retry logic
- [ ] Implement subscription/registration flows
- [ ] Add proper logging for debugging
- [ ] Validate `pageReferrer`/`sessionReferrer` capture from deep links
- [ ] Configure ProGuard rules for release builds
- [ ] Set up proper dependency injection (Hilt/Koin)

## License

This example code is provided for demonstration purposes.
