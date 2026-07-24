# Sophi Paywall Android Example

This is a complete Android example app demonstrating integration with the Sophi Paywall library using domain-driven design principles.

## Overview

This example shows how to:
- Import the Sophi Paywall library from GitHub Packages
- Implement user and device dimension repositories
- Detect referrer sources (social media, search engines, campaigns)
- Make paywall decisions
- Display paywall UI based on decisions
- Track analytics data

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
│       └── WallDecision.kt              # Decision result model
├── infrastructure/                       # Infrastructure layer (external integrations)
│   ├── paywall/
│   │   ├── types.kt                     # Dimension type definitions with extensive docs
│   │   ├── UserDimensionRepository.kt   # User metrics implementation
│   │   ├── DeviceDimensionRepository.kt # Device dimensions implementation
│   │   └── SophiPaywallAdapter.kt       # Sophi library adapter
│   └── storage/
│       └── UserMetricsStore.kt          # Local storage for metrics
└── ui/                                   # UI layer (presentation)
    ├── MainActivity.kt
    ├── article/
    │   ├── ArticleScreen.kt             # Article display with paywall
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

### Comprehensive Referrer Detection

The `UserDimensionRepository.kt` includes extensive examples for detecting referrer sources:

**Social Media:**
- Facebook, Instagram, X/Twitter, LinkedIn, Reddit

**Search Engines:**
- Google (Search, News, Discover)
- Bing, Yahoo, DuckDuckGo

**Campaigns:**
- UTM parameter detection
- Email newsletter tracking

**Examples:**
```kotlin
// User clicks Facebook link → (SOCIAL, FACEBOOK, null)
// User searches on Google → (SEARCH, GOOGLE, SEARCH)
// User browses Google News → (SEARCH, GOOGLE, NEWS)
// User swipes Google Discover → (SEARCH, GOOGLE, DISCOVER)
// User clicks email link → (CAMPAIGN, NEWSLETTER, null)
```

### Dimension Types Documentation

The `types.kt` file provides comprehensive documentation for all dimension types:

- **UserDimensions**: 27 fields tracking engagement over 1-day, 7-day, and 28-day periods
- **DeviceDimensions**: Device type, OS, browser, app context
- **Enum Types**: VisitorType, ReferrerMedium, ReferrerSource, ReferrerChannel

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
- [ ] Implement proper user metrics tracking (integrate with your analytics)
- [ ] Add proper error handling and retry logic
- [ ] Implement subscription/registration flows
- [ ] Add proper logging for debugging
- [ ] Test referrer detection with real traffic sources
- [ ] Configure ProGuard rules for release builds
- [ ] Set up proper dependency injection (Hilt/Koin)

## License

This example code is provided for demonstration purposes.
