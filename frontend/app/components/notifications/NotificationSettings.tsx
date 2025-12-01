'use client';

import { useEffect, useState } from 'react';
import { notificationsApi, type NotificationSettings } from '../../../lib/api/notifications';
import Button from '../common/Button';

interface NotificationSettingsProps {
  caseId: number;
}

export default function NotificationSettingsComponent({ caseId }: NotificationSettingsProps) {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [settings, setSettings] = useState<NotificationSettings>({
    notificationIntervalMinutes: 60,
    emailEnabled: true,
    smsEnabled: false,
    notifyOnHearingChanges: true,
    notifyOnStatusChanges: true,
    notifyOnPartyChanges: true,
    notifyOnProceduralStageChanges: true,
  });

  useEffect(() => {
    loadSettings();
  }, [caseId]);

  const loadSettings = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await notificationsApi.getCaseSettings(caseId);
      setSettings(data);
    } catch (err: any) {
      // If settings don't exist yet, use defaults
      if (err.status === 404) {
        setError(null);
      } else {
        setError(err.message || 'Failed to load notification settings');
        console.error('Error loading settings:', err);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    setSaving(true);
    setError(null);
    try {
      await notificationsApi.updateCaseSettings(caseId, settings);
      alert('Notification settings saved successfully');
    } catch (err: any) {
      setError(err.message || 'Failed to save notification settings');
      console.error('Error saving settings:', err);
    } finally {
      setSaving(false);
    }
  };

  const handleChange = (field: keyof NotificationSettings, value: boolean | number) => {
    setSettings((prev) => ({ ...prev, [field]: value }));
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-8">
        <div className="text-center">
          <svg
            className="animate-spin h-6 w-6 text-blue-600 mx-auto mb-2"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle
              className="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              strokeWidth="4"
            />
            <path
              className="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            />
          </svg>
          <p className="text-sm text-gray-600">Loading settings...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-xl font-semibold text-gray-900 mb-4">Notification Settings</h2>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-3 mb-4">
          <p className="text-sm text-red-800">{error}</p>
        </div>
      )}

      <div className="space-y-6">
        {/* Check Interval */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Check Interval (minutes)
          </label>
          <input
            type="number"
            min="1"
            value={settings.notificationIntervalMinutes}
            onChange={(e) => handleChange('notificationIntervalMinutes', parseInt(e.target.value) || 60)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          />
          <p className="text-xs text-gray-500 mt-1">
            How often to check for updates (minimum: 1 minute)
          </p>
        </div>

        {/* Notification Channels */}
        <div>
          <h3 className="text-sm font-semibold text-gray-900 mb-3">Notification Channels</h3>
          <div className="space-y-3">
            <label className="flex items-center gap-3 cursor-pointer">
              <input
                type="checkbox"
                checked={settings.emailEnabled}
                onChange={(e) => handleChange('emailEnabled', e.target.checked)}
                className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
              />
              <div>
                <span className="text-sm font-medium text-gray-700">Email Notifications</span>
                <p className="text-xs text-gray-500">Receive updates via email</p>
              </div>
            </label>

            <label className="flex items-center gap-3 cursor-pointer">
              <input
                type="checkbox"
                checked={settings.smsEnabled}
                onChange={(e) => handleChange('smsEnabled', e.target.checked)}
                className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
              />
              <div>
                <span className="text-sm font-medium text-gray-700">SMS Notifications</span>
                <p className="text-xs text-gray-500">Receive updates via SMS</p>
              </div>
            </label>
          </div>
        </div>

        {/* What to Notify About */}
        <div>
          <h3 className="text-sm font-semibold text-gray-900 mb-3">Notify Me About</h3>
          <div className="space-y-3">
            <label className="flex items-center gap-3 cursor-pointer">
              <input
                type="checkbox"
                checked={settings.notifyOnHearingChanges}
                onChange={(e) => handleChange('notifyOnHearingChanges', e.target.checked)}
                className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
              />
              <span className="text-sm text-gray-700">Hearing Changes</span>
            </label>

            <label className="flex items-center gap-3 cursor-pointer">
              <input
                type="checkbox"
                checked={settings.notifyOnStatusChanges}
                onChange={(e) => handleChange('notifyOnStatusChanges', e.target.checked)}
                className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
              />
              <span className="text-sm text-gray-700">Status Changes</span>
            </label>

            <label className="flex items-center gap-3 cursor-pointer">
              <input
                type="checkbox"
                checked={settings.notifyOnPartyChanges}
                onChange={(e) => handleChange('notifyOnPartyChanges', e.target.checked)}
                className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
              />
              <span className="text-sm text-gray-700">Party Changes</span>
            </label>

            <label className="flex items-center gap-3 cursor-pointer">
              <input
                type="checkbox"
                checked={settings.notifyOnProceduralStageChanges}
                onChange={(e) => handleChange('notifyOnProceduralStageChanges', e.target.checked)}
                className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
              />
              <span className="text-sm text-gray-700">Procedural Stage Changes</span>
            </label>
          </div>
        </div>

        {/* Save Button */}
        <div className="pt-4 border-t border-gray-200">
          <Button
            onClick={handleSave}
            variant="primary"
            size="md"
            loading={saving}
            disabled={saving}
          >
            Save Settings
          </Button>
        </div>
      </div>
    </div>
  );
}

