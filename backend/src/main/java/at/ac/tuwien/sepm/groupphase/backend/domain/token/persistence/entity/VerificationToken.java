package at.ac.tuwien.sepm.groupphase.backend.domain.token.persistence.entity;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;
import java.util.UUID;


@Getter
@Setter
@Entity
@NoArgsConstructor
public class VerificationToken {

    private static final int EXPIRATION_TIME_MS = 1000 * 60 * 60 * 24;


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token = UUID.randomUUID().toString();

    @Column(nullable = false)
    private Date expiryDate = new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS);


    public boolean isExpired() {
        return expiryDate.before(new Date());
    }


    @Override
    public String toString() {
        return token;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof VerificationToken && token.equals(((VerificationToken)other).getToken());
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }
}
