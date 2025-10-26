package ro.signsofter.caseobserver.service;

import ro.signsofter.caseobserver.entity.CourtCase;
import ro.signsofter.caseobserver.entity.Notification;
import ro.signsofter.caseobserver.entity.NotificationSettings;
import ro.signsofter.caseobserver.entity.User;
import ro.signsofter.caseobserver.service.CaseChangeDetectorService.CaseChanges;

import java.util.List;

public interface NotificationService {
    
    void sendCaseChangeNotification(User user, CourtCase courtCase, CaseChanges changes, NotificationSettings settings);
    
    void sendEmailNotification(User user, CourtCase courtCase, String subject, String message);
    
    void sendSmsNotification(User user, CourtCase courtCase, String message);
    
    List<Notification> getNotificationsForUser(Long userId);
    
    List<Notification> getNotificationsForCase(Long caseId);
    
    void markNotificationAsRead(Long notificationId);
    
    String generateNotificationMessage(CourtCase courtCase, CaseChanges changes);
    
    String generateNotificationSubject(CourtCase courtCase, CaseChanges changes);
}
