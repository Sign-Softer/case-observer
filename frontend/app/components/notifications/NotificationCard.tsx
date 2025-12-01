import Link from 'next/link';
import type { Notification } from '../../../lib/api/notifications';

interface NotificationCardProps {
  notification: Notification;
  onMarkAsRead?: (id: number) => void;
}

export default function NotificationCard({ notification, onMarkAsRead }: NotificationCardProps) {
  const formatDate = (dateString: string) => {
    try {
      const date = new Date(dateString);
      const now = new Date();
      const diffMs = now.getTime() - date.getTime();
      const diffMins = Math.floor(diffMs / 60000);
      const diffHours = Math.floor(diffMs / 3600000);
      const diffDays = Math.floor(diffMs / 86400000);

      if (diffMins < 1) return 'Just now';
      if (diffMins < 60) return `${diffMins} minute${diffMins > 1 ? 's' : ''} ago`;
      if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
      if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;

      return new Intl.DateTimeFormat('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      }).format(date);
    } catch {
      return dateString;
    }
  };

  return (
    <Link href={`/cases/${notification.caseId}`}>
      <div
        className={`p-4 rounded-lg border transition-all hover:shadow-md cursor-pointer ${
          notification.read
            ? 'bg-white border-gray-200'
            : 'bg-blue-50 border-blue-300 border-l-4'
        }`}
      >
        <div className="flex items-start justify-between gap-3">
          <div className="flex-1 min-w-0">
            <div className="flex items-start gap-2 mb-2">
              {!notification.read && (
                <div className="w-2 h-2 bg-blue-600 rounded-full mt-2 flex-shrink-0" />
              )}
              <div className="flex-1">
                <p className="text-sm font-medium text-gray-900">{notification.message}</p>
                <p className="text-xs text-gray-500 mt-1">
                  Case: {notification.caseNumber}
                </p>
              </div>
            </div>
            <p className="text-xs text-gray-500">{formatDate(notification.sentAt)}</p>
          </div>
          {!notification.read && onMarkAsRead && (
            <button
              onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
                onMarkAsRead(notification.id);
              }}
              className="text-xs text-blue-600 hover:text-blue-700 font-medium flex-shrink-0"
            >
              Mark as read
            </button>
          )}
        </div>
      </div>
    </Link>
  );
}

