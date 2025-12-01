'use client';

import { useState } from 'react';
import { casesApi } from '../../../lib/api/cases';
import Button from '../common/Button';

interface MonitoringToggleProps {
  caseId: number;
  monitoringEnabled: boolean;
  onUpdate?: () => void;
}

export default function MonitoringToggle({ caseId, monitoringEnabled, onUpdate }: MonitoringToggleProps) {
  const [loading, setLoading] = useState(false);
  const [isEnabled, setIsEnabled] = useState(monitoringEnabled);

  const handleToggle = async () => {
    setLoading(true);
    try {
      if (isEnabled) {
        // Stop monitoring
        await casesApi.stopMonitoring(caseId);
        setIsEnabled(false);
      } else {
        // Start monitoring
        await casesApi.startMonitoring(caseId);
        setIsEnabled(true);
      }
      if (onUpdate) {
        onUpdate();
      }
    } catch (error: any) {
      console.error('Error toggling monitoring:', error);
      alert(error.message || 'Failed to update monitoring status');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-between">
      <div>
        <div className="flex items-center gap-2 mb-1">
          <h3 className="text-base font-semibold text-gray-900">Monitoring</h3>
          <span className={`px-2 py-1 text-xs font-semibold rounded-full ${
            isEnabled 
              ? 'bg-green-100 text-green-800' 
              : 'bg-gray-100 text-gray-600'
          }`}>
            {isEnabled ? 'ON' : 'OFF'}
          </span>
        </div>
        <p className="text-sm text-gray-600">
          {isEnabled
            ? 'Case is being monitored. You will receive notifications when changes occur.'
            : 'Monitoring is disabled. Enable to receive automatic updates.'}
        </p>
      </div>
      <Button
        onClick={handleToggle}
        variant={isEnabled ? 'danger' : 'primary'}
        size="sm"
        loading={loading}
        disabled={loading}
      >
        {isEnabled ? 'Stop Monitoring' : 'Start Monitoring'}
      </Button>
    </div>
  );
}

