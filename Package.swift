// swift-tools-version: 6.2
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "PaywallKit",
    products: [
        // Products define the executables and libraries a package produces, making them visible to other packages.
        .library(
            name: "PaywallKit",
            targets: ["PaywallKit"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "PaywallBinary",
            url: "https://github.com/Mather-Sophi/io-sophi-paywall-native/releases/download/v2.1.1/Paywall.xcframework.zip",
            checksum: "e92f35966354847fe74fc1c6dde9ee0488c663f0df78a948473c9ea0166f5028"
        ),
        .target(
            name: "PaywallKit",
            dependencies: ["PaywallBinary"],
            path: "PaywallKit/Sources/PaywallKit"
        ),
        .testTarget(
            name: "PaywallKitTests",
            dependencies: ["PaywallKit"],
            path: "PaywallKit/Tests/PaywallKitTests"
        ),
    ]
)