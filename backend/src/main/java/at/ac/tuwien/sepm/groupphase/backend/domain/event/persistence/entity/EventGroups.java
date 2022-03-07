package at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.util.Objects;


@Getter
@Setter
@Entity
public class EventGroups {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "first_group_id")
    @Cascade(CascadeType.PERSIST)
    private EventGroup firstGroup;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "second_group_id")
    @Cascade(CascadeType.PERSIST)
    private EventGroup secondGroup;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventGroups that = (EventGroups) o;
        return Objects.equals(getFirstGroup(), that.getFirstGroup()) && Objects.equals(getSecondGroup(), that.getSecondGroup());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirstGroup(), getSecondGroup());
    }
}
