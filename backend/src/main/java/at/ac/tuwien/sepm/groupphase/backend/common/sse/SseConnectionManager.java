package at.ac.tuwien.sepm.groupphase.backend.common.sse;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


/**
 * The manager for all sse connections.
 * Works on a per-user basis by their id.
 */
@Slf4j
public class SseConnectionManager {

    /**
     * The current emitter which are in use, where the key is the user id.
     * Closed, errored and timed-out emitters should be removed regularly.
     */
    private final Map<Long, SseEmitter> emitters = new HashMap<>();

    /**
     * The timout of the emitters measured in milliseconds.
     * Note that the timeout denotes the delta time between sent events and not a TCP timeout.
     */
    @Value("${sse.timeout}")
    private Long emitterTimeout = 1L;


    /**
     * Create a new emitter for the user with the given id.
     * If the user already has an emitter, the old one will be closed and a new will be created.
     *
     * @param userId the id of the user to register
     * @return the created emitter
     */
    public SseEmitter startUserConnection(Long userId) {
        log.debug("start user connection for user with id {}", userId);
        if (emitters.containsKey(userId)) {
            log.debug("user with id {} already has a connection, doing nothing...", userId);
            return emitters.get(userId);
        }

        var emitter = new SseEmitter(this.emitterTimeout);
        emitter.onTimeout(() -> {
            log.debug("emitter timeout for user with id {}", userId);
            closeUserConnection(userId);
        });
        emitter.onError(error -> {
            log.debug("emitter for user with id {} resulted in an error: {}", userId, error.getMessage());
            closeUserConnection(userId);
        });
        emitter.onCompletion(() -> emitters.remove(userId));
        emitters.put(userId, emitter);
        return emitter;
    }

    /**
     * A broadcast version of {@link #sendMessageToUser(Long, SseDto)}.
     *
     * @param userIds the user ids where the message to send
     * @param sseDto  the data to send to the users
     * @return 'true' if all transmissions where successful and 'false' otherwise
     */
    public boolean sendMessageToUsers(List<Long> userIds, SseDto sseDto) {
        return userIds.stream()
            .allMatch(userId -> sendMessageToUser(userId, sseDto));
    }

    /**
     * A version of {@link #sendMessageToUsers(java.util.List, SseDto)} where every user receives an individual update.
     *
     * @param users           The {@link Account users} who are to receive an update if they have a connection.
     * @param updateGenerator A callback to generate individual {@link SseDto updates} for each user.
     * @return 'true' if all transmissions where successful and 'false' otherwise
     */
    public <T extends Account> boolean sendMessagesToUsers(List<T> users, Function<T, SseDto> updateGenerator) {
        return users.stream()
            .allMatch(user -> sendMessageToUser(user.getId(), updateGenerator.apply(user)));
    }

    /**
     * Send a dto to a user. If the emitter results into an exception, it will be closed and remove.
     *
     * @param userId the id of the user which should receive the message
     * @param sseDto the data to send to the user
     * @return 'true' if this was a success and 'false' otherwise
     */
    public boolean sendMessageToUser(Long userId, SseDto sseDto) {
        var emitter = emitters.get(userId);
        try {
            var eventBuilder = SseEmitter.event()
                .data(sseDto, MediaType.APPLICATION_JSON)
                .name(sseDto.getEventType());
            emitter.send(eventBuilder);
        } catch (Exception exception) {
            if (emitter != null) {
                emitter.completeWithError(exception);
            }
            emitters.remove(userId);
            return false;
        }
        return true;
    }

    /**
     * Close and remove the emitter for a user.
     *
     * @param userId the id user the user whose emitter should be closed
     */
    public void closeUserConnection(Long userId) {
        emitters.get(userId).complete();
        emitters.remove(userId);
    }

    /**
     * Schedule a heart beat to all clients every 10 seconds.
     * This is the recommended approach to prevent client reconnects which are not necessary.
     */
    @Scheduled(fixedRate = 10000)
    public void heartBeat() {
        log.debug("Send the heart beat to all clients");
        this.emitters.values().parallelStream().forEach(emitter -> {
            try {
                emitter.send("heartBeat");
            } catch (IOException ignored) {
            }
        });
    }
}
