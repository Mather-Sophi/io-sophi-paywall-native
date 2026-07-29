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
            url: "https://github.com/Mather-Sophi/io-sophi-paywall-native/releases/download/v1.2.2/Paywall.xcframework.zip",
            checksum: "aeccceb4ad315f353e0bb7625b4e16e9d6f86e3d5c70cca0770f8c090df01c40"
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