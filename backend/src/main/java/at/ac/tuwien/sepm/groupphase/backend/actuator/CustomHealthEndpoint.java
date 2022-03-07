package at.ac.tuwien.sepm.groupphase.backend.actuator;

import javax.annotation.security.PermitAll;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/** Provides endpoints for kubernetes health checks. */
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class CustomHealthEndpoint {

    private final ApplicationContext applicationContext;
    private boolean isUp = true;


    @PermitAll
    @GetMapping
    public ResponseEntity<?> getHealth() {
        return isUp
            ? ResponseEntity.ok("OK")
            : ResponseEntity.internalServerError().build();
    }

    /**
     * Before the shutdown of a pod this url will be called. Afterwards the health probes fail. Therefore, the pod
     * is removed from the healthy pods which are exposed. This way a zero downtime upgrade is possible.
     */
    @PermitAll
    @GetMapping("/prepareShutdown")
    public void prepareShutdown() {
        AvailabilityChangeEvent.publish(applicationContext, LivenessState.BROKEN);
        isUp = false;
    }
}
