import Testing

@testable import PaywallKit

final class TestUserDimensionRepository: UserDimensionRepository {
    private let dimension = UserDimensions(
        todayPageViews: 3,
        todayPageViewsByArticle: 2,
        todayPageViewsByArticleWithPaywall: 1,
        todayPageViewsByArticleWithRegwall: 0,
        todayTopLevelSections: 2,
        todayTopLevelSectionsByArticle: 1,
        sevenDayPageViews: 7,
        sevenDayPageViewsByArticle: 5,
        sevenDayPageViewsByArticleWithPaywall: 2,
        sevenDayPageViewsByArticleWithRegwall: 1,
        sevenDayTopLevelSections: 4,
        sevenDayTopLevelSectionsByArticle: 3,
        sevenDayVisitCount: 5,
        twentyEightDayPageViews: 15,
        twentyEightDayPageViewsByArticle: 10,
        twentyEightDayPageViewsByArticleWithPaywall: 5,
        twentyEightDayPageViewsByArticleWithRegwall: 2,
        twentyEightDayTopLevelSections: 8,
        twentyEightDayTopLevelSectionsByArticle: 6,
        twentyEightDayVisitCount: 12,
        daysSinceLastVisit: 2,
        visitorType: .registered,
        timezone: "America/New_York",
        pageReferrer: "https://test.sophi.io/article",
        sessionReferrer: "https://test.sophi.io/home"
    )

    func getAll() -> UserDimensions {
        dimension
    }
}

final class TestDeviceDimensionRepository: IDeviceDimensionRepository {
    private let dimension = DeviceDimension(
        hourOfDay: 14,
        os: .ios,
        type: .native,
        viewer: "paywall-kit-tests"
    )

    func getAll() -> DeviceDimension {
        dimension
    }
}

@Suite("PaywallKit Repository and Decision Tests")
struct PaywallKitTests {
    @Test("Initialize UserDimensionRepository and DeviceDimensionRepository")
    func initializesDimensionRepositories() {
        let userRepository = TestUserDimensionRepository()
        let deviceRepository = DeviceDimensionRepository(
            viewer: "paywall-kit-tests"
        )

        let userDimension = userRepository.getAll()
        let deviceDimension = deviceRepository.getAll()

        #expect(userDimension.todayPageViews == 3)
        #expect(userDimension.todayPageViewsByArticle == 2)
        #expect(userDimension.todayPageViewsByArticleWithPaywall == 1)
        #expect(userDimension.todayPageViewsByArticleWithRegwall == 0)
        #expect(userDimension.todayTopLevelSections == 2)
        #expect(userDimension.todayTopLevelSectionsByArticle == 1)
        #expect(userDimension.sevenDayPageViews == 7)
        #expect(userDimension.sevenDayPageViewsByArticle == 5)
        #expect(userDimension.sevenDayPageViewsByArticleWithPaywall == 2)
        #expect(userDimension.sevenDayPageViewsByArticleWithRegwall == 1)
        #expect(userDimension.sevenDayTopLevelSections == 4)
        #expect(userDimension.sevenDayTopLevelSectionsByArticle == 3)
        #expect(userDimension.sevenDayVisitCount == 5)
        #expect(userDimension.twentyEightDayPageViews == 15)
        #expect(userDimension.twentyEightDayPageViewsByArticle == 10)
        #expect(userDimension.twentyEightDayPageViewsByArticleWithPaywall == 5)
        #expect(userDimension.twentyEightDayPageViewsByArticleWithRegwall == 2)
        #expect(userDimension.twentyEightDayTopLevelSections == 8)
        #expect(userDimension.twentyEightDayTopLevelSectionsByArticle == 6)
        #expect(userDimension.twentyEightDayVisitCount == 12)
        #expect(userDimension.daysSinceLastVisit == 2)
        #expect(userDimension.visitorType == .registered)
        #expect(userDimension.timezone == "America/New_York")
        #expect(userDimension.pageReferrer == "https://test.sophi.io/article")
        #expect(userDimension.sessionReferrer == "https://www.sophi.io")

        #expect(deviceDimension.hourOfDay >= 0)
        #expect(deviceDimension.hourOfDay <= 23)
        #expect(deviceDimension.os == .ios)
        #expect(deviceDimension.type == .native)
        #expect(deviceDimension.viewer == "paywall-kit-tests")
    }

    @Test("Initialize PaywallDeciderRepository with repositories")
    func initializesPaywallDeciderRepository() async throws {
        let userRepository = TestUserDimensionRepository()
        let deviceRepository = DeviceDimensionRepository(
            viewer: "paywall-kit-tests"
        )

        let repository = PaywallDeciderRepository.createNew(
            userRepository: userRepository,
            deviceRepository: deviceRepository
        )

        let decider = try await repository.getOneByHost(
            host: "test.sophi.io",
            apiTimeoutInMilliSeconds: nil
        )
        #expect(decider.host == "test.sophi.io")
    }

    @Test("Call decide and validate WallDecision fields")
    func validatesWallDecisionObject() async throws {
        let userRepository = TestUserDimensionRepository()
        let deviceRepository = TestDeviceDimensionRepository()
        let repository = PaywallDeciderRepository.createNew(
            userRepository: userRepository,
            deviceRepository: deviceRepository
        )

        let decider = try await repository.getOneByHost(
            host: "test.sophi.io",
            apiTimeoutInMilliSeconds: nil
        )

        let decision = try await decider.decide(
            contentId: "article-123",
            contentProperties: ["section": "sports"],
            userProperties: ["countryCode": "US"]
        )

        #expect(decider.host == "test.sophi.io")

        // WallDecision invariants
        #expect(!decision.trace.isEmpty)
        #expect(
            decision.context
                == "DA02AA03AB02AC01AD02AE01AF00BA07BB05BC02BD04BE03BF05BG01CA15CB10CC05CD08CE06CF12CG02"
        )
        #expect(
            decision.inputs
                == "FA:01|DE:02|EA:ios|EB:06|EC:paywall-kit-tests|DD:14|DB:America/New_York|GC:internal"
        )

        // Outcome codes are constrained by the SDK enums
        #expect(decision.outcome.wallVisibilityCode >= 0)
        #expect(decision.outcome.wallVisibilityCode <= 2)
        if let wallTypeCode = decision.outcome.wallTypeCode {
            #expect(wallTypeCode == 1 || wallTypeCode == 2)
        }

        // Optional fields are nil with default fallback config because propertyCodes are not mapped.
        #expect(decision.userProperties == nil)
        #expect(decision.contentProperties == nil)

        // Score, if present, must be within 0...100
        if let score = decision.paywallScore?.intValue {
            #expect(score >= 0)
            #expect(score <= 100)
        }

        // Swift extension on WallDecision should always provide a date
        #expect(decision.createdAt.timeIntervalSince1970 > 0)
    }

    @Test(
        "Call decide using DeviceDimensionRepository and validate WallDecision"
    )
    func validatesWallDecisionWithBuiltInDeviceRepository() async throws {
        let userRepository = TestUserDimensionRepository()
        let deviceRepository = DeviceDimensionRepository(
            viewer: "paywall-kit-tests"
        )
        let repository = PaywallDeciderRepository.createNew(
            userRepository: userRepository,
            deviceRepository: deviceRepository
        )

        let decider = try await repository.getOneByHost(
            host: "www.sophi.io",
            apiTimeoutInMilliSeconds: nil
        )

        let decision = try await decider.decide(
            contentId: "article-using-built-in-device-repo",
            contentProperties: ["section": "news"],
            userProperties: ["countryCode": "CA"]
        )

        #expect(decider.host == "www.sophi.io")
        #expect(!decision.trace.isEmpty)
        #expect(
            decision.context
                == "DA02AA03AB02AC01AD02AE01AF00BA07BB05BC02BD04BE03BF05BG01CA15CB10CC05CD08CE06CF12CG02"
        )
        #expect(decision.inputs?.contains("FA:01") == true)
        #expect(decision.inputs?.contains("DE:02") == true)
        #expect(decision.inputs?.contains("EB:06") == true)
        #expect(decision.inputs?.contains("EC:paywall-kit-tests") == true)
        #expect(decision.inputs?.contains("DB:America/New_York") == true)
        #expect(decision.inputs?.contains("GC:internal") == true)

        #expect(decision.outcome.wallVisibilityCode >= 0)
        #expect(decision.outcome.wallVisibilityCode <= 2)

        if let wallTypeCode = decision.outcome.wallTypeCode {
            #expect(wallTypeCode == 1 || wallTypeCode == 2)
        }

        if let score = decision.paywallScore?.intValue {
            #expect(score >= 0)
            #expect(score <= 100)
        }

        // Optional fields are nil with default fallback config because propertyCodes are not mapped.
        #expect(decision.userProperties == nil)
        #expect(decision.contentProperties == nil)

        #expect(decision.createdAt.timeIntervalSince1970 > 0)
    }
}
