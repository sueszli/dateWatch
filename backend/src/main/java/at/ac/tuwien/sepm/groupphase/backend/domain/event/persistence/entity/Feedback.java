package at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;


@Getter
@Setter
@Entity
public class Feedback {
    public static final int MAX_LENGTH_MESSAGE = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private String eventAccessToken;

    private String eventTitle;

    private String organizerEmailLowercase;

    @Column(length = MAX_LENGTH_MESSAGE, nullable = false)
    private String message;

}
