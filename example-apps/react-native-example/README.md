# Sophi Paywall — React Native Example

This example app demonstrates integration with the Sophi Paywall SDK for React Native. It runs a suite of paywall decision scenarios to validate the SDK integration.

`@mather-sophi/paywall` is a single package that detects your app's architecture (New Architecture/TurboModules or Legacy Bridge) at runtime — this example applies unchanged regardless of which one your app uses.

## Prerequisites

- Node.js >= 20
- React Native CLI environment set up ([React Native Environment Setup](https://reactnative.dev/docs/set-up-your-environment))
- GitHub Personal Access Token with `read:packages` scope
- For iOS: Xcode 15+, CocoaPods
- For Android: Android Studio, JDK 17+

## Setup

### 1. Configure GitHub Packages Authentication

Create or edit `~/.npmrc`:

```ini
//npm.pkg.github.com/:_authToken=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN
@mather-sophi:registry=https://npm.pkg.github.com/
```

For Android native dependencies, add to `~/.gradle/gradle.properties`:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN
```

### 2. Install Dependencies

```bash
npm install
```

### 3. iOS Setup

```bash
cd ios && pod install && cd ..
```

The `ios/` directory is a complete React Native host app. It builds the
published Turbo Module package through CocoaPods, including the package's
vendored `Paywall.xcframework`.

### 4. Run the App

```bash
# iOS
npm run ios

# Build the native iOS target without launching a simulator
npm run build:ios

# Android
npm run android
```

## What This Example Does

The app initializes the `PaywallDeciderRepository` with mock implementations of `UserDimensionRepository` and `DeviceDimensionRepository`, then runs a test suite that:

1. Fetches a `PaywallDecider` from `test.sophi.codes`
2. Iterates through scenarios varying user metrics, referrer sources, and visitor types
3. Validates that decisions contain expected context codes, input codes, and trace formats
4. Displays pass/fail results with timing and decision details

## Key Files

| File | Purpose |
|------|---------|
| [`src/App.tsx`](src/App.tsx) | Main app — test runner UI |
| [`src/repositories/MockRepositories.ts`](src/repositories/MockRepositories.ts) | Example implementations of `UserDimensionRepository` and `DeviceDimensionRepository` |
| [`src/tests/testScenarios.ts`](src/tests/testScenarios.ts) | Test scenarios with expected outcomes |
| [`src/tests/codes.ts`](src/tests/codes.ts) | Context/input code mappings used for validation |
| [`src/styles.ts`](src/styles.ts) | React Native styles |

## Adapting for Your App

Replace `MockUserDimensionRepository` and `MockDeviceDimensionRepository` with your own implementations that return real visitor engagement data from your app's local storage. See the [full integration guide](https://resources.sophi.io/docs/paywall-guide-for-native-apps) for details on computing each metric.

## Reference: Realistic Rolling-Window Repository

[`src/repositories/RollingUserDimensionRepository.ts`](src/repositories/RollingUserDimensionRepository.ts) is a second `UserDimensionRepository` implementation, separate from `MockRepositories.ts`. The two serve different purposes:

| | `MockRepositories.ts` | `RollingUserDimensionRepository.ts` |
|---|---|---|
| Values | Flat, hand-picked per scenario | Computed from real tracked activity |
| Used by | The automated test suite (`App.tsx`) — must stay deterministic | Nothing in this example — it's a reference for your app |
| Storage | None (in-memory object) | Pluggable `KeyValueStore` (swap in `@react-native-async-storage/async-storage`, MMKV, etc.) |

It tracks one day-bucket of engagement per calendar day (today + up to 28 days of history), rolls the history forward automatically as days pass, and sums disjoint windows of it into the `today*`/`sevenDay*`/`twentyEightDay*` dimensions — the same day-bucketing algorithm Sophi's web SDK uses. Call `trackPageView()`/`trackWallView()` from your real content-view events, and `refresh()` before each `getAll()` (e.g. on app foreground) since `getAll()` itself is synchronous per the SDK's interface.
