package at.ac.tuwien.sepm.groupphase.backend.domain.event.event;

import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.EventStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * Triggered when an event's {@link Event#getStatus() status} changes and handled by
 * {@link at.ac.tuwien.sepm.groupphase.backend.domain.event.event.listener.EventStatusChangedListener}.
 */
@Getter
@RequiredArgsConstructor
public class EventStatusChangedEvent {
    private final EventStatus oldEventStatus;
    private final Event event;
}
