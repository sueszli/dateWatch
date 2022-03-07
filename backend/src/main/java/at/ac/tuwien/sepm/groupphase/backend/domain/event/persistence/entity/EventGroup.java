package at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;


@Getter
@Setter
@Entity
public class EventGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(length = Event.MAX_LENGTH_GROUP_TITLE)
    private String title;

    @Column(length = Event.MAX_LENGTH_GROUP_DESCRIPTION)
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventGroup that = (EventGroup) o;
        return getTitle().equals(that.getTitle()) && getDescription().equals(that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getDescription());
    }
}
