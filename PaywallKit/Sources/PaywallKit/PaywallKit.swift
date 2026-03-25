@_exported import Paywall

public typealias UserDimensions = VisitorData
public typealias UserDimensionRepository = VisitorDataRepository
public typealias DeviceDimension = DeviceData

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
