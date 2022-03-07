package at.ac.tuwien.sepm.groupphase.backend.domain.event.event.listener;

import at.ac.tuwien.sepm.groupphase.backend.common.sse.SseConnectionManager;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.sse.EventChangedDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.event.EventChangedEvent;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.event.handler.EventChangedHandler;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;


@Slf4j
@Component
@RequiredArgsConstructor
public class EventChangedListener {

    private final SseConnectionManager sseConnectionManager;
    private final EventChangedHandler eventChangedHandler;


    /**
     * Handles {@link EventChangedEvent}s which indicate that the {@link Event} has changed.
     *
     * @param eventChangedEvent The {@link EventChangedEvent} containing the event in question.
     */
    @Async
    @EventListener(EventChangedEvent.class)
    @TransactionalEventListener
    public void handleEventStatusChanged(final EventChangedEvent eventChangedEvent) {
        var event = eventChangedEvent.getEvent();
        handleEventChanged(eventChangedEvent);
        this.sseConnectionManager.sendMessageToUser(event.getOrganizer().getId(), EventChangedDto.fromEvent(event));
    }

    private void handleEventChanged(EventChangedEvent eventChangedEvent) {
        eventChangedHandler.handleEventChanged(eventChangedEvent);
    }
}
