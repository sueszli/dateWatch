package at.ac.tuwien.sepm.groupphase.backend.domain.event.event;

import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * Triggered when an event {@link Event} changes and handled by
 * {@link at.ac.tuwien.sepm.groupphase.backend.domain.event.event.listener.EventChangedListener}.
 */
@Getter
@RequiredArgsConstructor
public class EventChangedEvent {
    private final Event event;
    private final Event oldDiffEvent;
    private final Event newDiffEvent;
}
