import { useEffect, useMemo, useRef, useState } from 'react';
import {
  ActivityIndicator,
  Button,
  ScrollView,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import {
  PaywallDeciderRepository,
  type WallDecision,
} from '@mather-sophi/sophi-react-native-paywall-kit-turbo-modules';
import {
  MockDeviceDimensionRepository,
  MockUserDimensionRepository,
} from './repositories/MockRepositories';
import { styles } from './styles';
import {
  generateTestScenarios,
  runScenarioValidations,
} from './tests/testScenarios';

type TestResult = {
  testName: string;
  passed: boolean;
  duration: number;
  error?: string;
  decision?: WallDecision;
};

export default function App() {
  const [testResults, setTestResults] = useState<TestResult[]>([]);
  const [isRunning, setIsRunning] = useState(false);
  const [expandedResults, setExpandedResults] = useState<
    Record<number, boolean>
  >({});

  const userRepoRef = useRef<MockUserDimensionRepository | null>(null);
  const deviceRepoRef = useRef<MockDeviceDimensionRepository | null>(null);
  const deciderRepoRef = useRef<PaywallDeciderRepository | null>(null);

  const scenarios = useMemo(() => generateTestScenarios(), []);

  useEffect(() => {
    userRepoRef.current = new MockUserDimensionRepository();
    deviceRepoRef.current = new MockDeviceDimensionRepository();

    try {
      deciderRepoRef.current = PaywallDeciderRepository.createNew(
        userRepoRef.current,
        deviceRepoRef.current
      );
    } catch (error) {
      setTestResults([
        {
          testName: 'Initialization',
          passed: false,
          duration: 0,
          error: `Failed to initialize PaywallDeciderRepository: ${String(error)}`,
        },
      ]);
    }
  }, []);

  const runTests = async () => {
    if (
      !deciderRepoRef.current ||
      !userRepoRef.current ||
      !deviceRepoRef.current
    ) {
      setTestResults([
        {
          testName: 'Initialization',
          passed: false,
          duration: 0,
          error: 'PaywallDeciderRepository not initialized',
        },
      ]);
      return;
    }

    setIsRunning(true);
    setExpandedResults({});
    setTestResults([]);

    const results: TestResult[] = [];

    let decider;
    try {
      decider = await deciderRepoRef.current.getOneByHost(
        'test.sophi.codes',
        1500
      );
    } catch (error) {
      setTestResults([
        {
          testName: 'Decider Initialization',
          passed: false,
          duration: 0,
          error: `Failed to get decider: ${String(error)}`,
        },
      ]);
      setIsRunning(false);
      return;
    }

    for (let index = 0; index < scenarios.length; index += 1) {
      const scenario = scenarios[index]!;
      const startTime = Date.now();

      try {
        userRepoRef.current.reset();
        userRepoRef.current.update(scenario.userDimension);

        deviceRepoRef.current.reset();
        deviceRepoRef.current.update(scenario.deviceDimension);

        const userProperties =
          scenario.contentId === 'test-content-properties'
            ? { subscriptionTier: 'premium' }
            : undefined;
        const contentProperties =
          scenario.contentId === 'test-content-properties'
            ? { category: 'sports' }
            : undefined;

        const decision = await decider.decide(
          scenario.contentId as string | undefined,
          contentProperties,
          userProperties
        );

        const validationErrors = runScenarioValidations(
          scenario,
          userRepoRef.current.getAll(),
          deviceRepoRef.current.getAll(),
          decision ?? null
        );

        results.push({
          testName: scenario.name,
          passed:
            !!decision && !!decision.outcome && validationErrors.length === 0,
          duration: Date.now() - startTime,
          decision,
          error:
            validationErrors.length > 0
              ? validationErrors.join(' | ')
              : undefined,
        });
      } catch (error) {
        results.push({
          testName: scenario.name,
          passed: false,
          duration: Date.now() - startTime,
          error: String(error),
        });
      }

      setTestResults([...results]);
    }

    setIsRunning(false);
  };

  const passedCount = testResults.filter((result) => result.passed).length;
  const totalTests = testResults.length;

  return (
    <ScrollView style={styles.container}>
      <View style={styles.section}>
        <Text style={styles.title}>Paywall Decision Test Suite</Text>
        <Text style={styles.subtitle}>
          TurboModules SDK — scenario runner for New Architecture
        </Text>
      </View>

      <View style={styles.section}>
        <Button
          title={isRunning ? 'Running Tests...' : 'Run All Tests'}
          onPress={runTests}
          disabled={isRunning}
        />
        {isRunning && (
          <View style={{ marginTop: 16, alignItems: 'center' }}>
            <ActivityIndicator size="large" color="#0066cc" />
            <Text style={{ marginTop: 8 }}>
              Running test {Math.min(testResults.length + 1, scenarios.length)}{' '}
              of {scenarios.length}...
            </Text>
          </View>
        )}
      </View>

      {totalTests > 0 && (
        <>
          <View style={styles.section}>
            <View style={styles.summaryBox}>
              <Text style={styles.summaryTitle}>Test Summary</Text>
              <Text style={[styles.summaryText, { color: '#22c55e' }]}>
                Passed: {passedCount}
              </Text>
              <Text style={[styles.summaryText, { color: '#ef4444' }]}>
                Failed: {totalTests - passedCount}
              </Text>
              <Text style={styles.summaryText}>Total: {totalTests}</Text>
              <Text style={styles.summaryText}>
                Success Rate: {((passedCount / totalTests) * 100).toFixed(1)}%
              </Text>
            </View>
          </View>

          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Test Results</Text>
            {testResults.map((result, index) => (
              <TouchableOpacity
                key={result.testName}
                style={[
                  styles.testResultCard,
                  result.passed
                    ? { borderLeftColor: '#22c55e' }
                    : { borderLeftColor: '#ef4444' },
                ]}
                onPress={() =>
                  setExpandedResults((previous) => ({
                    ...previous,
                    [index]: !previous[index],
                  }))
                }
              >
                <View style={styles.testResultHeader}>
                  <Text style={styles.testResultTitle}>
                    {result.passed ? 'PASS' : 'FAIL'} {result.testName}
                  </Text>
                  <Text style={styles.testResultDuration}>
                    {result.duration}ms
                  </Text>
                </View>
                {expandedResults[index] && (
                  <View style={styles.testResultDetails}>
                    {result.error && (
                      <Text style={styles.testResultError}>
                        Error: {result.error}
                      </Text>
                    )}
                    {result.decision && (
                      <>
                        <Text style={styles.testResultDetail}>
                          Wall Type:{' '}
                          {result.decision.outcome?.wallType || 'N/A'}
                        </Text>
                        <Text style={styles.testResultDetail}>
                          Visibility:{' '}
                          {result.decision.outcome?.wallVisibility || 'N/A'}
                        </Text>
                        <Text style={styles.testResultDetail}>
                          Trace: {result.decision.trace || 'N/A'}
                        </Text>
                        <Text style={styles.testResultDetail}>
                          Context: {result.decision.context || 'N/A'}
                        </Text>
                        <Text style={styles.testResultDetail}>
                          Inputs: {result.decision.inputs || 'N/A'}
                        </Text>
                        <Text style={styles.testResultDetail}>
                          Paywall Score:{' '}
                          {String(result.decision.paywallScore ?? 'N/A')}
                        </Text>
                        <Text style={styles.testResultDetail}>
                          User Properties:{' '}
                          {JSON.stringify(result.decision.userProperties)}
                        </Text>
                        <Text style={styles.testResultDetail}>
                          Content Properties:{' '}
                          {JSON.stringify(result.decision.contentProperties)}
                        </Text>
                      </>
                    )}
                  </View>
                )}
              </TouchableOpacity>
            ))}
          </View>
        </>
      )}
    </ScrollView>
  );
}
