package at.ac.tuwien.sepm.groupphase.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableAsync
@EnableScheduling
@SpringBootApplication
public class DatewatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatewatchApplication.class, args);
    }
}
