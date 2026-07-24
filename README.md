# Sophi Paywall SDK

[![NPM](https://img.shields.io/badge/npm-v1.1.0-blue)]()
[![Maven](https://img.shields.io/badge/maven-v1.1.0-blue)]()


This is an SDK for Sophi Paywall API integration for mobile apps. The SDK is available for both Android and iOS platforms, and it supports both the new TurboModules architecture and the legacy architecture for apps developed on React Native applications.

>[!Note]
> Use `test.sophi.io` as your host for integration and testing.
> Before go-live, contact Sophi to provision and configure your production host, then replace `test.sophi.io` in your initialization code.

Find the complete project's [documentation here](https://resources.sophi.io/docs/paywall-guide-for-native-apps)


## Table of Contents

<a id="table-of-contents"></a>

- [Prerequisites (PAT for npm and Maven)](#prerequisites-pat-for-npm-and-maven)
- [Installation](#installation)
  - [React Native SDK](#installation-react-native-sdk)
  - [Android SDK](#installation-android-sdk)
  - [iOS SDK](#installation-ios-sdk)
- [Usage](#usage)
  - [React Native SDK](#usage-react-native-sdk)
  - [Android SDK](#usage-android-sdk)
  - [iOS SDK](#usage-ios-sdk)
- [Troubleshooting](#troubleshooting)

## Prerequisites

To access Sophi packages from GitHub Packages, configure a GitHub Personal Access Token (PAT):

- npm registry access
- Maven registry access

Reference: [Create a personal access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-personal-access-token-classic)

For npm packages, add your token to `~/.npmrc`:

```ini
//npm.pkg.github.com/:_authToken=GITHUB_PERSONAL_ACCESS_TOKEN
@Mather-Sophi:registry=https://npm.pkg.github.com/
```

If GitHub Packages is not your default registry, login explicitly:

```sh
npm login --scope=@Mather-Sophi --auth-type=legacy --registry=https://npm.pkg.github.com
```

For Gradle, add credentials to `~/.gradle/gradle.properties`:

```ini
gpr.user=GITHUB_USERNAME
gpr.key=GITHUB_PERSONAL_ACCESS_TOKEN
```

[Back to Table of Contents](#table-of-contents)

## Installation

<a id="installation-react-native-sdk"></a>

### React Native SDK

The React Native SDK is available in two packages:

- TurboModules package (recommended for New Architecture projects)
- Legacy Architecture package (for existing/older RN integration)

Install TurboModules package:

```sh
npm install @mather-sophi/sophi-react-native-paywall-kit-turbo-modules@1.1.0 --save
```

Install Legacy package:

```sh
npm install @mather-sophi/sophi-react-native-paywall-kit@1.1.0 --save
```

Then install iOS pods in your React Native app:

```bash
cd ios && pod install
```

<a id="installation-android-sdk"></a>

### Android SDK

Add GitHub Packages repository to your project-level `build.gradle` repositories:

```gradle
repositories {
  maven {
    name = "GitHubPackages"
    url = uri("https://maven.pkg.github.com/Mather-Sophi/io-sophi-paywall-native")
    credentials {
      username = project.findProperty("gpr.user") as String?: System.getenv("GITHUB_ACTOR")
      password = project.findProperty("gpr.key") as String?: System.getenv("GITHUB_TOKEN")
    }
  }
}
```

Add dependency in your app/library module:

```gradle
dependencies {
  implementation("io.sophi.android:paywall-kit:1.1.0")
}
```

<a id="installation-ios-sdk"></a>

### iOS SDK

Add the Sophi Paywall package in Xcode:

1. Open your Xcode project.
2. Go to `File` -> `Add Package Dependencies...`
3. Enter the GitHub URL as the package URL: `https://github.com/Mather-Sophi/io-sophi-paywall-native`
4. Select version: `v1.1.0`
5. Add the package product to your target.

## Usage

<a id="usage-react-native-sdk"></a>

### React Native SDK (TurboModules and Legacy)

Both React Native packages use the same repository and decider flow. Only the import package name changes.

Use one of these imports:

```ts
import { PaywallDeciderRepository, type UserDimensions, type DeviceDimensions } from '@mather-sophi/sophi-react-native-paywall-kit-turbo-modules';
```

```ts
import { PaywallDeciderRepository, type UserDimensions, type DeviceDimensions } from '@mather-sophi/sophi-react-native-paywall-kit';
```

Then use the same integration flow:

```ts
const userDimensionsRepository = {
  getAll(): UserDimensions {
    return {
      todayPageViews: 1,
      todayPageViewsByArticle: 1,
      todayPageViewsByArticleWithPaywall: 0,
      todayPageViewsByArticleWithRegwall: 0,
      todayTopLevelSections: 1,
      todayTopLevelSectionsByArticle: 1,
      sevenDayPageViews: 3,
      sevenDayPageViewsByArticle: 3,
      sevenDayPageViewsByArticleWithPaywall: 0,
      sevenDayPageViewsByArticleWithRegwall: 0,
      sevenDayTopLevelSections: 1,
      sevenDayTopLevelSectionsByArticle: 1,
      sevenDayVisitCount: 2,
      twentyEightDayPageViews: 8,
      twentyEightDayPageViewsByArticle: 8,
      twentyEightDayPageViewsByArticleWithPaywall: 0,
      twentyEightDayPageViewsByArticleWithRegwall: 0,
      twentyEightDayTopLevelSections: 3,
      twentyEightDayTopLevelSectionsByArticle: 3,
      twentyEightDayVisitCount: 4,
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
      viewer: 'app-ios-1.0.0',
    };
  },
};

const repository = PaywallDeciderRepository.createNew(
  userDimensionsRepository,
  deviceDimensionsRepository
);

const decider = await repository.getOneByHost('test.sophi.io', 1500);
const decision = await decider.decide('content-123');

```

<a id="usage-android-sdk"></a>

### Android SDK

```kotlin
val visitorDataRepository: UserDimensionRepository = NativeUserDimensionRepository()
val deviceDataRepository: DeviceDimensionRepository = NativeDeviceDimensionRepository()

val deciders: PaywallDeciderRepository = PaywallDeciderRepository.createNew(visitorDataRepository, deviceDataRepository)
val thisDecider: PaywallDecider = deciders.getOneByHost(host = "www.sophi.io")

val decision = thisDecider.decide(contentId = content.id)

```

<a id="usage-ios-sdk"></a>

### iOS SDK

```swift
import PaywallKit

let userDimensions = NativeUserDimensionRepository()
let deviceDimensions = NativeDeviceDimensionRepository()

let deciders = PaywallDeciderRepository.createNew(
  userRepository: userDimensions,
  deviceRepository: deviceDimensions
)

let decider = try await deciders.getOneByHost(
  host: "test.sophi.io",
  apiTimeoutInMilliSeconds: nil
)

let decision = try await decider.decide(
  contentId: content.id,
  contentProperties: nil,
  userProperties: nil
)
```

[Back to Table of Contents](#table-of-contents)

## Troubleshooting

- npm install fails with 401/403:
  - Confirm `//npm.pkg.github.com/:_authToken=...` is in `~/.npmrc`.
  - Confirm you used the `@Mather-Sophi` scope registry.
  - Confirm PAT has package read permissions.
- Gradle cannot resolve Maven artifact:
  - Confirm `gpr.user` and `gpr.key` are in `~/.gradle/gradle.properties`.
  - Confirm the GitHub Packages repository block exists in project repositories.
  - Confirm dependency is `io.sophi.android:paywall-kit:1.1.0`.
- React Native iOS build fails after package install:
  - Run `cd ios && pod install`.
  - Clean and rebuild your iOS project.
- Decisions fail during integration:
  - Verify host is `test.sophi.io` for integration/testing.
  - Contact Sophi to provision and configure non-test hosts.

[Back to Table of Contents](#table-of-contents)
