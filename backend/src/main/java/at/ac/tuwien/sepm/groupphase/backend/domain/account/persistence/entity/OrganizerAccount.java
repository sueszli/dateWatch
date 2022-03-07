package at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity;

import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.builder.ToStringExclude;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Data
@Entity
@Table(name = "account_organizer")
@DiscriminatorValue("organizer")
@EqualsAndHashCode(callSuper = true)
public class OrganizerAccount extends Account {

    public static final int MAX_LENGTH_ORGANIZATION_NAME = 150;
    public static final int MAX_LENGTH_CONTACT_PERSON_FIRST_NAME = 100;
    public static final int MAX_LENGTH_CONTACT_PERSON_LAST_NAME = 100;


    @Column(length = MAX_LENGTH_ORGANIZATION_NAME)
    private String organizationName;

    @Column(nullable = false, length = MAX_LENGTH_CONTACT_PERSON_FIRST_NAME)
    private String contactPersonFirstName;

    @Column(nullable = false, length = MAX_LENGTH_CONTACT_PERSON_LAST_NAME)
    private String contactPersonLastName;

    @OneToMany(mappedBy = "organizer")
    @JsonIgnore
    @ToString.Exclude
    private List<Event> organizedEvents = new ArrayList<>();

    @Column(nullable = false)
    private boolean isDeactivated = false;

    public String getFullName() {
        return contactPersonFirstName + " " + contactPersonLastName;
    }
}
