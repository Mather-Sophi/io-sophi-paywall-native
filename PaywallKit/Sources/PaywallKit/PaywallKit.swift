@_exported import Paywall

public typealias UserDimensions = VisitorData
public typealias UserDimensionRepository = VisitorDataRepository
public typealias DeviceDimension = DeviceData
// A single experiment/test assignment for the current user -- see ExperimentAssignment in the
// Kotlin core. No Swift-side translation needed: the KMP-generated binding accepts it directly.
public typealias ExperimentAssignment = Paywall.ExperimentAssignment

extension PaywallDeciderRepository {
    public static func createNew(
        userRepository: UserDimensionRepository,
        deviceRepository: IDeviceDimensionRepository
    ) -> PaywallDeciderRepository {
        return companion.createNew(
            userRepository: userRepository,
            deviceRepository: deviceRepository
        )
    }
}

extension WallDecision {
    public var createdAt: NSDate {
        NSDate()
    }
}
