package ro.signsofter.caseobserver.external;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "portal")
@Data
public class PortalProperties {
    private String baseUrl;
    private String host;
    private String soapAction;
    private int connectTimeoutMs;
    private int readTimeoutMs;
    private int retries;
}


