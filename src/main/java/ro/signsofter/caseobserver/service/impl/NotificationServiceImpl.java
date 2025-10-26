package ro.signsofter.caseobserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.signsofter.caseobserver.entity.CourtCase;
import ro.signsofter.caseobserver.entity.Notification;
import ro.signsofter.caseobserver.entity.NotificationSettings;
import ro.signsofter.caseobserver.entity.User;
import ro.signsofter.caseobserver.repository.NotificationRepository;
import ro.signsofter.caseobserver.service.CaseChangeDetectorService.CaseChanges;
import ro.signsofter.caseobserver.service.NotificationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public void sendCaseChangeNotification(User user, CourtCase courtCase, CaseChanges changes, NotificationSettings settings) {
        if (!changes.hasAnyChanges()) {
            return; // No changes to notify about
        }

        String subject = generateNotificationSubject(courtCase, changes);
        String message = generateNotificationMessage(courtCase, changes);

        // Send email notification if enabled
        if (settings.getEmailEnabled()) {
            sendEmailNotification(user, courtCase, subject, message);
        }

        // Send SMS notification if enabled
        if (settings.getSmsEnabled()) {
            sendSmsNotification(user, courtCase, message);
        }

        // Save notification to database
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setCourtCase(courtCase);
        notification.setMessage(message);
        notification.setSentAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
    }

    @Override
    public void sendEmailNotification(User user, CourtCase courtCase, String subject, String message) {
        // TODO: Implement actual email sending using email service provider
        // For now, just log the email that would be sent
        System.out.println("=== EMAIL NOTIFICATION ===");
        System.out.println("To: " + user.getEmail());
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + message);
        System.out.println("========================");
        
        // In production, integrate with email service like SendGrid, AWS SES, etc.
        // Example:
        // emailService.sendEmail(user.getEmail(), subject, message);
    }

    @Override
    public void sendSmsNotification(User user, CourtCase courtCase, String message) {
        // TODO: Implement actual SMS sending using SMS service provider
        // For now, just log the SMS that would be sent
        System.out.println("=== SMS NOTIFICATION ===");
        System.out.println("To: " + user.getUsername()); // Assuming username is phone number
        System.out.println("Message: " + message);
        System.out.println("========================");
        
        // In production, integrate with SMS service like Twilio, AWS SNS, etc.
        // Example:
        // smsService.sendSms(user.getPhoneNumber(), message);
    }

    @Override
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId);
    }

    @Override
    public List<Notification> getNotificationsForCase(Long caseId) {
        return notificationRepository.findByCourtCaseIdOrderBySentAtDesc(caseId);
    }

    @Override
    public void markNotificationAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            // Add a readAt field to Notification entity if needed
            notificationRepository.save(notification);
        }
    }

    @Override
    public String generateNotificationMessage(CourtCase courtCase, CaseChanges changes) {
        StringBuilder message = new StringBuilder();
        
        message.append("Case Update Notification\n");
        message.append("========================\n\n");
        message.append("Case: ").append(courtCase.getCaseNumber()).append("\n");
        message.append("Court: ").append(courtCase.getCourtName()).append("\n");
        message.append("Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");
        
        message.append("Changes detected:\n");
        message.append("----------------\n");
        
        if (changes.isStatusChanged()) {
            message.append("• Status changed from '").append(changes.getOldStatus()).append("' to '").append(changes.getNewStatus()).append("'\n");
        }
        
        if (changes.isProceduralStageChanged()) {
            message.append("• Procedural stage changed from '").append(changes.getOldProceduralStage()).append("' to '").append(changes.getNewProceduralStage()).append("'\n");
        }
        
        if (changes.isCategoryChanged()) {
            message.append("• Category changed from '").append(changes.getOldCategory()).append("' to '").append(changes.getNewCategory()).append("'\n");
        }
        
        if (changes.isSubjectChanged()) {
            message.append("• Subject changed from '").append(changes.getOldSubject()).append("' to '").append(changes.getNewSubject()).append("'\n");
        }
        
        if (changes.isDepartmentChanged()) {
            message.append("• Department changed from '").append(changes.getOldDepartment()).append("' to '").append(changes.getNewDepartment()).append("'\n");
        }
        
        if (changes.isHearingsChanged()) {
            message.append("\nHearing Changes:\n");
            for (CaseChanges.HearingChange hearingChange : changes.getHearingChanges()) {
                message.append("• ").append(hearingChange.getType()).append(": ").append(hearingChange.getDescription()).append("\n");
            }
        }
        
        if (changes.isPartiesChanged()) {
            message.append("\nParty Changes:\n");
            for (CaseChanges.PartyChange partyChange : changes.getPartyChanges()) {
                message.append("• ").append(partyChange.getType()).append(": ").append(partyChange.getDescription()).append("\n");
            }
        }
        
        message.append("\nPlease log in to your Case Observer account to view full details.\n");
        message.append("This is an automated notification. Please do not reply to this email.");
        
        return message.toString();
    }

    @Override
    public String generateNotificationSubject(CourtCase courtCase, CaseChanges changes) {
        StringBuilder subject = new StringBuilder();
        subject.append("Case Update: ").append(courtCase.getCaseNumber());
        
        if (changes.isStatusChanged()) {
            subject.append(" - Status Changed");
        } else if (changes.isHearingsChanged()) {
            subject.append(" - Hearing Updates");
        } else if (changes.isPartiesChanged()) {
            subject.append(" - Party Changes");
        } else {
            subject.append(" - Case Updated");
        }
        
        return subject.toString();
    }
}
