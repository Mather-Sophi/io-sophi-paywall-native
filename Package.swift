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
            url: "https://github.com/Mather-Sophi/io-sophi-paywall-native/releases/download/v1.2.1/Paywall.xcframework.zip",
            checksum: "66cd6dc2dcf4ce9b7e9b23d7ced586e695199dc9de2e970b2072b35e5ff37a7b"
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