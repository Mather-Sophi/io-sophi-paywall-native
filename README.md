# Sophi Paywall SDK — Native Integration

[![NPM](https://img.shields.io/badge/npm-v2.1.0-blue)]()
[![Maven](https://img.shields.io/badge/maven-v2.1.0-blue)]()
[![Swift Package](https://img.shields.io/badge/swift-v2.1.0-orange)]()

The Sophi Paywall SDK enables on-device paywall decisions in your native mobile app. It evaluates visitor engagement, device context, and content signals locally—delivering low-latency wall decisions without round-trip API calls for every page view.

This repository contains:

- **SDK packages** for Android (Kotlin), iOS (Swift), and React Native (TypeScript)
- **Example apps** demonstrating complete integration for each platform
- **Type definitions** and interface contracts you must implement

> [!IMPORTANT]
> **Full integration guide:** [resources.sophi.io/docs/paywall-guide-for-native-apps](https://resources.sophi.io/docs/paywall-guide-for-native-apps)
>
> This README covers installation and quick-start usage. The full guide covers architecture, visitor-level analytics porting, decision tracing, and production deployment.

> [!NOTE]
> Use `test.sophi.io` as your host for integration and testing.
> Before go-live, contact Sophi to provision and configure your production host, then replace `test.sophi.io` in your initialization code.

> [!WARNING]
> **v2.1.0 is a breaking change for React Native only.** The separate `@sophi/paywall` and `@mather-sophi/sophi-react-native-paywall-kit` packages are merged into a single package, `@mather-sophi/paywall`, that supports both architectures. Android and iOS package names and APIs are unchanged. See [Upgrading from v1.x](#upgrading-from-v1x).

---

## Table of Contents

- [How It Works](#how-it-works)
- [Prerequisites](#prerequisites)
- [Platform Guides](#platform-guides)
  - [Android (Kotlin)](#android-kotlin)
  - [iOS (Swift)](#ios-swift)
  - [React Native](#react-native)
- [Upgrading from v1.x](#upgrading-from-v1x)
- [Example Apps](#example-apps)
- [What You Need to Implement](#what-you-need-to-implement)
- [Troubleshooting](#troubleshooting)

---

## How It Works

The SDK has a simple 3-step flow on every platform:

1. **Initialize** — Create a `PaywallDeciderRepository` with your implementations of `UserDimensionRepository` and `DeviceDimensionRepository`.
2. **Get a Decider** — Call `getOneByHost(host)` to fetch the on-device model configuration for your site. This should happen at app startup and when the app returns to foreground after inactivity.
3. **Decide** — Call `decider.decide(contentId)` each time a user views content. The SDK returns a `WallDecision` with the action to take (show paywall, regwall, or allow access).

```
┌─────────────┐     ┌──────────────────────┐     ┌────────────────┐
│ Your App    │────▶│ PaywallDeciderRepo   │────▶│ PaywallDecider │
│             │     │  .createNew(user,dev) │     │  .decide(id)   │
└─────────────┘     └──────────────────────┘     └────────────────┘
       │                                                  │
       │  Implements:                                     ▼
       │  • UserDimensionRepository          ┌────────────────────┐
       │  • DeviceDimensionRepository        │   WallDecision     │
       └─────────────────────────────────────│  (paywall/regwall/ │
                                             │   allow/etc.)      │
                                             └────────────────────┘
```

---

## Prerequisites

SDK packages are distributed through two mechanisms depending on your platform:

| Platform | Distribution | Authentication |
|----------|-------------|----------------|
| React Native (npm) | [GitHub Packages](https://docs.github.com/en/packages) | Personal Access Token (PAT) |
| Android (Gradle/Maven) | [GitHub Packages](https://docs.github.com/en/packages) | Personal Access Token (PAT) |
| iOS (Swift) | Swift Package Manager (this repository) | GitHub account with org access |

### Why is a Personal Access Token required?

GitHub does not allow anonymous access to packages hosted in GitHub Packages — even for packages published from public repositories. This is a [GitHub platform restriction](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-npm-registry#authenticating-to-github-packages), not a Sophi-specific requirement.

To authenticate, you need a **GitHub Personal Access Token (classic)** with the `read:packages` scope.

**How to create one:**

1. Go to [GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)](https://github.com/settings/tokens)
2. Click **Generate new token (classic)**
3. Give it a descriptive name (e.g. "Sophi SDK read access")
4. Select the `read:packages` scope
5. Click **Generate token** and copy the value — you won't see it again

Reference: [GitHub docs — Creating a personal access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-personal-access-token-classic)

### For npm (React Native)

Create or edit `~/.npmrc`:

```ini
//npm.pkg.github.com/:_authToken=GITHUB_PERSONAL_ACCESS_TOKEN
@mather-sophi:registry=https://npm.pkg.github.com/
```

Or login explicitly:

```sh
npm login --scope=@Mather-Sophi --auth-type=legacy --registry=https://npm.pkg.github.com
```

### For Gradle (Android)

Add to `~/.gradle/gradle.properties`:

```properties
gpr.user=GITHUB_USERNAME
gpr.key=GITHUB_PERSONAL_ACCESS_TOKEN
```

### For Swift Package Manager (iOS)

The iOS SDK is distributed as a Swift Package directly from this repository — it does **not** use GitHub Packages. Xcode resolves the package via your GitHub account.

1. Ensure your GitHub account has been granted access to the `Mather-Sophi` organization
2. In Xcode: **Settings → Accounts** — add or verify your GitHub account
3. Xcode will authenticate automatically when resolving the package dependency

> [!NOTE]
> If your GitHub account does not have access to this repository, contact your Sophi technical team to be granted access.

---

## Platform Guides

### Android (Kotlin)

#### Installation

1. Add the GitHub Packages repository to your project-level `build.gradle` (or `settings.gradle.kts`):

```kotlin
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/Mather-Sophi/io-sophi-paywall-native")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```

2. Add the dependency to your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.sophi.android:paywall-kit:2.1.0")
}
```

#### Quick Start

```kotlin
import io.sophi.android.paywallkit.*

// 1. Implement the required interfaces (see "What You Need to Implement" section)
val userDimRepo: UserDimensionRepository = YourUserDimensionRepository()
val deviceDimRepo: DeviceDimensionRepository = YourDeviceDimensionRepository()

// 2. Create the repository (once, at app startup)
val deciders = PaywallDeciderRepository.createNew(userDimRepo, deviceDimRepo)

// 3. Get a decider for your host (at startup + on foreground after inactivity)
val decider = deciders.getOneByHost(host = "test.sophi.io", apiTimeoutInMilliSeconds = 1500)

// 4. Get a decision for each piece of content
val decision = decider.decide(
    contentId = article.id,
    contentProperties = null,
    userProperties = null
)

// 5. Use the decision
when (decision.wallType) {
    // Show paywall, regwall, or allow access
}
```

See the [Android example app](example-apps/android-example/) for a complete implementation with referrer detection, rolling-window engagement tracking, a login/logout demo, and Compose UI.

---

### iOS (Swift)

#### Installation

1. In Xcode: **File → Add Package Dependencies...**
2. Enter URL: `https://github.com/Mather-Sophi/io-sophi-paywall-native`
3. Select version `2.1.0` (or "Up to Next Major")
4. Add the `PaywallKit` product to your target

#### Quick Start

```swift
import PaywallKit

// 1. Implement the required protocols (see "What You Need to Implement" section)
let userDimensions = YourUserDimensionRepository()
let deviceDimensions = YourDeviceDimensionRepository()

// 2. Create the repository (once, at app startup)
let deciders = PaywallDeciderRepository.createNew(
    userRepository: userDimensions,
    deviceRepository: deviceDimensions
)

// 3. Get a decider for your host (at startup + on foreground after inactivity)
let decider = try await deciders.getOneByHost(
    host: "test.sophi.io",
    apiTimeoutInMilliSeconds: 1500
)

// 4. Get a decision for each piece of content
let decision = try await decider.decide(
    contentId: article.id,
    contentProperties: nil,
    userProperties: nil
)

// 5. Use the decision
switch decision.wallType {
    // Show paywall, regwall, or allow access
}
```

---

### React Native

The React Native SDK is a single package that supports both the **New Architecture** (TurboModules) and the **Legacy** (Bridge) architecture — it detects which one your app uses at runtime, so there's nothing to configure.

| Package | Install Command |
|---------|-----------------|
| `@mather-sophi/paywall` | `npm install @mather-sophi/paywall@2.1.0 --save` |

> [!NOTE]
> Upgrading from v1.x, where TurboModules and Legacy were separate packages? See [Upgrading from v1.x](#upgrading-from-v1x).

#### Installation

1. Install the package (see table above)

2. **iOS** — install CocoaPods:
   ```bash
   cd ios && pod install
   ```

3. **Android** — add the GitHub Packages maven repository to your app's `android/build.gradle`:
   ```groovy
   repositories {
       maven {
           name = "GitHubPackages"
           url = uri("https://maven.pkg.github.com/Mather-Sophi/io-sophi-paywall-native")
           credentials {
               username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
               password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
           }
       }
   }
   ```

#### Quick Start

```typescript
import { PaywallDeciderRepository, type UserDimensions, type DeviceDimensions } from '@mather-sophi/paywall';
```

```typescript
// 1. Implement the required interfaces
const userDimensionsRepository = {
  getAll(): UserDimensions {
    return {
      todayPageViews: 5,
      todayPageViewsByArticle: 4,
      todayPageViewsByArticleWithPaywall: 0,
      todayPageViewsByArticleWithRegwall: 0,
      todayTopLevelSections: 2,
      todayTopLevelSectionsByArticle: 2,
      sevenDayPageViews: 20,
      sevenDayPageViewsByArticle: 18,
      sevenDayPageViewsByArticleWithPaywall: 1,
      sevenDayPageViewsByArticleWithRegwall: 0,
      sevenDayTopLevelSections: 4,
      sevenDayTopLevelSectionsByArticle: 3,
      sevenDayVisitCount: 5,
      twentyEightDayPageViews: 60,
      twentyEightDayPageViewsByArticle: 55,
      twentyEightDayPageViewsByArticleWithPaywall: 3,
      twentyEightDayPageViewsByArticleWithRegwall: 0,
      twentyEightDayTopLevelSections: 6,
      twentyEightDayTopLevelSectionsByArticle: 5,
      twentyEightDayVisitCount: 12,
      daysSinceLastVisit: 1,
      visitorType: 'registered',
      timezone: 'America/New_York',
      pageReferrer: null,
      sessionReferrer: null,
    };
  },
};

const deviceDimensionsRepository = {
  getAll(): DeviceDimensions {
    return {
      hourOfDay: new Date().getHours(),
      os: 'ios',
      viewer: 'app-ios-1.0.0',  // Format: app-{platform}-{version}
    };
  },
};

// 2. Create the repository
const repository = PaywallDeciderRepository.createNew(
  userDimensionsRepository,
  deviceDimensionsRepository
);

// 3. Get a decider for your host
const decider = await repository.getOneByHost('test.sophi.io', 1500);

// 4. Get a decision (with optional properties)
const decision = await decider.decide(
  'content-123',
  { category: 'sports' },          // contentProperties (optional)
  { subscriptionTier: 'premium' }   // userProperties (optional)
);
// Or call with just contentId: await decider.decide('content-123');
```

See the [React Native example app](example-apps/react-native-example/) for a complete React Native CLI implementation with an automated scenario-validation test suite, built on `@mather-sophi/paywall`.

---

## Upgrading from v1.x

**v2.1.0 only changes React Native package names.** Android and iOS are unaffected beyond the version bump — no API or code changes.

| Platform | v1.x | v2.1.0 |
|----------|------|--------|
| Android (Gradle) | `io.sophi.android:paywall-kit:1.2.2` | `io.sophi.android:paywall-kit:2.1.0` — version bump only |
| iOS (SPM) | Package version `1.2.2` | Package version `2.1.0` — version bump only |
| React Native, New Architecture | `@sophi/paywall@1.2.2` | `@mather-sophi/paywall@2.1.0` |
| React Native, Legacy Architecture | `@mather-sophi/sophi-react-native-paywall-kit@1.2.2` | `@mather-sophi/paywall@2.1.0` |

### React Native migration steps

1. Remove whichever old package you had installed:
   ```bash
   npm uninstall @sophi/paywall
   # or
   npm uninstall @mather-sophi/sophi-react-native-paywall-kit
   ```
2. Install the new package:
   ```bash
   npm install @mather-sophi/paywall@2.1.0 --save
   ```
3. Update every import to the new module specifier, `@mather-sophi/paywall`. The exported API (`PaywallDeciderRepository`, `UserDimensions`, `DeviceDimensions`, `WallDecision`, etc.) is unchanged.
4. **iOS** — reinstall CocoaPods after swapping the package: `cd ios && pod install`.
5. **Android** — no changes needed; the native module registers itself for both architectures.
6. Drop any logic that chose a package based on `newArchEnabled` — `@mather-sophi/paywall` detects the architecture at runtime.

> [!NOTE]
> `@sophi/paywall` and `@mather-sophi/sophi-react-native-paywall-kit` are deprecated as of v2.1.0 (see the npm deprecation notice on each) and will not receive further updates. Use `@mather-sophi/paywall` for all new and existing integrations.

---

## Example Apps

This repository includes working example apps for each platform:

| Platform | Location | Description |
|----------|----------|-------------|
| Android | [`example-apps/android-example/`](example-apps/android-example/) | Complete Kotlin + Compose app with domain-driven architecture, referrer detection, rolling-window engagement tracking, and a login/logout demo |
| React Native | [`example-apps/react-native-example/`](example-apps/react-native-example/) | React Native CLI app on `@mather-sophi/paywall` with an automated scenario-validation test suite — works unchanged on New Architecture or Legacy Bridge apps |

Each example shows:
- How to implement `UserDimensionRepository` and `DeviceDimensionRepository`
- How to detect referrer sources (social, search, campaign, direct)
- How to call the decider and act on decisions
- Proper initialization and lifecycle management

---

## What You Need to Implement

The SDK provides the decision engine. **You** must implement two interfaces that supply visitor and device context:

### `UserDimensionRepository`

Returns engagement metrics about the current visitor. You are responsible for tracking and persisting these metrics in your app's local storage.

| Field | Type | Description |
|-------|------|-------------|
| `todayPageViews` | number | Total pages viewed today |
| `todayPageViewsByArticle` | number | Article pages viewed today |
| `todayPageViewsByArticleWithPaywall` | number | Paywalled articles encountered today |
| `todayPageViewsByArticleWithRegwall` | number | Regwalled articles encountered today |
| `todayTopLevelSections` | number | Unique top-level sections visited today |
| `todayTopLevelSectionsByArticle` | number | Sections visited via articles today |
| `sevenDayPageViews` | number | Total pages viewed in last 7 days |
| `sevenDayPageViewsByArticle` | number | Article pages in last 7 days |
| `sevenDayPageViewsByArticleWithPaywall` | number | Paywalled articles in last 7 days |
| `sevenDayPageViewsByArticleWithRegwall` | number | Regwalled articles in last 7 days |
| `sevenDayTopLevelSections` | number | Unique sections in last 7 days |
| `sevenDayTopLevelSectionsByArticle` | number | Sections via articles in last 7 days |
| `sevenDayVisitCount` | number | Visit count in last 7 days |
| `twentyEightDayPageViews` | number | Total pages in last 28 days |
| `twentyEightDayPageViewsByArticle` | number | Article pages in last 28 days |
| `twentyEightDayPageViewsByArticleWithPaywall` | number | Paywalled articles in last 28 days |
| `twentyEightDayPageViewsByArticleWithRegwall` | number | Regwalled articles in last 28 days |
| `twentyEightDayTopLevelSections` | number | Unique sections in last 28 days |
| `twentyEightDayTopLevelSectionsByArticle` | number | Sections via articles in last 28 days |
| `twentyEightDayVisitCount` | number | Visit count in last 28 days |
| `daysSinceLastVisit` | number | Days since last visit |
| `visitorType` | string | `'anonymous'` or `'registered'` |
| `timezone` | string | IANA timezone (e.g. `'America/New_York'`) |
| `pageReferrer` | string \| null | Referrer URL for the current page |
| `sessionReferrer` | string \| null | Referrer URL captured at session start |

### `DeviceDimensionRepository`

Returns device/app context. Minimal implementation:

| Field | Type | Description |
|-------|------|-------------|
| `hourOfDay` | number | Current hour in 24h format (`0-23`) |
| `os` | string | Lowercase OS identifier: `'ios'` or `'android'` |
| `viewer` | string | App identifier in format `app-{platform}-{version}` (e.g. `app-ios-2.1.0`) |

> [!NOTE]
> For detailed guidance on implementing these interfaces—including reference source code from the web SDK—see the [full integration guide](https://resources.sophi.io/docs/paywall-guide-for-native-apps).

---

## Troubleshooting

### npm install fails with 401/403

- Verify `//npm.pkg.github.com/:_authToken=...` is in `~/.npmrc`
- Verify the scope is `@mather-sophi` (case-insensitive)
- Verify your PAT has `read:packages` permission
- Verify your GitHub account has access to the `Mather-Sophi` organization

### Gradle cannot resolve Maven artifact

- Verify `gpr.user` and `gpr.key` in `~/.gradle/gradle.properties`
- Verify the GitHub Packages repository URL is `https://maven.pkg.github.com/Mather-Sophi/io-sophi-paywall-native`
- Verify the dependency is `io.sophi.android:paywall-kit:2.1.0`

### iOS Swift Package resolution fails

- Verify your GitHub account in Xcode has access to `Mather-Sophi` org
- Try **File → Packages → Reset Package Caches** in Xcode

### React Native iOS build fails

- Run `cd ios && pod install` after installing the npm package
- Clean build folder: **Product → Clean Build Folder** in Xcode

### Decisions return unexpected results during testing

- Verify host is `test.sophi.io` for integration/testing
- Ensure `UserDimensionRepository.getAll()` returns current, non-stale data
- Recreate `PaywallDecider` after app returns from background (model parameters refresh)

### Production deployment

- Contact Sophi to provision your production host before go-live
- Replace `test.sophi.io` with your production host in initialization code

---

## Resources

- [Full Integration Guide](https://resources.sophi.io/docs/paywall-guide-for-native-apps) — Architecture, analytics porting, decision tracing
- [GitHub Packages](https://github.com/orgs/Mather-Sophi/packages?repo_name=io-sophi-paywall-native) — All published SDK artifacts
- [GitHub PAT Documentation](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens) — Token setup help

---

## License

See [LICENSE](LICENSE) for details.
