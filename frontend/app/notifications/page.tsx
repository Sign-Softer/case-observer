'use client';

import DashboardLayout from '../components/layout/DashboardLayout';
import NotificationList from '../components/notifications/NotificationList';

export default function NotificationsPage() {
  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Notifications</h1>
          <p className="text-gray-600 mt-2">
            Stay updated on changes to your monitored cases
          </p>
        </div>

        <NotificationList />
      </div>
    </DashboardLayout>
  );
}

