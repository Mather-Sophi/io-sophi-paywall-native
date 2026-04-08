# Sophi Paywall SDK

[![NPM](https://img.shields.io/badge/npm-0.0.2-blue)]()
[![Maven](https://img.shields.io/badge/maven-0.0.1-blue)]()

SDK for Paywall API integration.

# Installation
**Prerequisite**: To use these packages you might require having a GitHub Personal Access Token (PAT). See GitHub documentation on [Creating a personal access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-personal-access-token-classic).

For NPM packages, you can add your PAT under `~/.npmrc`.

```ini
//npm.pkg.github.com/:_authToken=GITHUB_PERSONAL_ACCESS_TOKEN
```

or login if GitHub Packages is not your default package registry
```sh
npm login --scope=@Mather-Sophi --auth-type=legacy --registry=https://npm.pkg.github.com
```

For Gradle, you can add your PAT under `gradle.properties`.

```ini
# ~/.gradle/gradle.properties
gpr.user=GITHUB_USERNAME
gpr.key=GITHUB_PERSONAL_ACCESS_TOKEN
```



### React Native SDK Installation

In the same directory as your package.json file, create or edit an .npmrc file to include the line below specifying the GitHub Packages URL and the namespace.

```
@Mather-Sophi:registry=https://npm.pkg.github.com/
```

The SDK is built using TurboModules which supports both new and old architecture. To install the Sophi Paywall React Native SDK, you can use either `npm` or `yarn`. Run one of the install command using the `--save` flag.

```sh
npm install @mather-sophi/sophi-react-native-paywall-kit --save
```

Under `ios` directory, install the CocoaPods dependencies by running:
```bash
cd ios && pod install
```

Update the `build.gradle` file in your Android project to include the GitHub Packages repository. Add the following lines to the `repositories` section:

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


### Android SDK Installation

Ensure your `build.gradle` includes the required repositories to install android dependencies.

Update `build.gradle`
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

Then add the dependency to your `build.gradle` file.

```gradle
dependencies {
    implementation("io.sophi.android:paywall-kit:0.0.1")
}
```