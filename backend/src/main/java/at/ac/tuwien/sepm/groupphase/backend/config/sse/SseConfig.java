package at.ac.tuwien.sepm.groupphase.backend.config.sse;

import at.ac.tuwien.sepm.groupphase.backend.common.sse.SseConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SseConfig {
    @Bean
    public SseConnectionManager transferService() {
        return new SseConnectionManager();
    }
}
