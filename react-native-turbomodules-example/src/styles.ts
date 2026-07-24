import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  section: {
    padding: 20,
    backgroundColor: 'white',
    marginTop: 10,
    marginHorizontal: 10,
    borderRadius: 8,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 14,
    color: '#666',
  },
  sectionTitle: {
    color: '#333',
    fontSize: 18,
    fontWeight: '600',
    marginBottom: 12,
  },
  summaryBox: {
    backgroundColor: '#f0f0f0',
    padding: 16,
    borderRadius: 8,
    borderLeftWidth: 4,
    borderLeftColor: '#007AFF',
  },
  summaryTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#333',
    marginBottom: 12,
  },
  summaryText: {
    fontSize: 14,
    fontWeight: '600',
    marginBottom: 4,
    color: '#333',
  },
  testResultCard: {
    backgroundColor: 'white',
    borderRadius: 8,
    marginBottom: 12,
    borderLeftWidth: 4,
    overflow: 'hidden',
  },
  testResultHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 12,
  },
  testResultTitle: {
    fontSize: 13,
    fontWeight: '600',
    color: '#333',
    flex: 1,
    marginRight: 8,
  },
  testResultDuration: {
    fontSize: 12,
    fontWeight: '500',
    color: '#666',
  },
  testResultDetails: {
    backgroundColor: '#f9f9f9',
    padding: 12,
    borderTopWidth: 1,
    borderTopColor: '#e0e0e0',
  },
  testResultDetail: {
    fontSize: 11,
    color: '#555',
    fontFamily: 'Courier',
    marginBottom: 6,
  },
  testResultError: {
    fontSize: 11,
    color: '#ef4444',
    fontFamily: 'Courier',
    marginBottom: 6,
    fontWeight: '600',
  },
});
