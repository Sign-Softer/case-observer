package ro.signsofter.caseobserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CaseObserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaseObserverApplication.class, args);
    }

}
