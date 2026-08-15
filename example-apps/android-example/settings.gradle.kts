pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        
        // GitHub Packages Maven repository for Sophi Paywall Kit
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Mather-Sophi/io-sophi-paywall-native")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

rootProject.name = "NewsPublisher"
include(":app")
