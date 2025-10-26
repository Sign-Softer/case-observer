package ro.signsofter.caseobserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "monitoring")
public class MonitoringProperties {
    private boolean enabled = true;
    private long scheduledCheckIntervalMs = 300000; // 5 minutes
    private int defaultNotificationIntervalMinutes = 60;
    private int maxConcurrentChecks = 10;
}
