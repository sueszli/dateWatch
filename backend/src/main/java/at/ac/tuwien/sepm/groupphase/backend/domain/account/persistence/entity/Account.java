package at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity;

import at.ac.tuwien.sepm.groupphase.backend.domain.token.persistence.entity.VerificationToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;


@Getter
@Setter
@RequiredArgsConstructor
@ToString(exclude = "password")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "account_type")
public class Account {

    public static final int MAX_LENGTH_EMAIL = 320;


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "account_type", insertable = false, updatable = false)
    protected String accountType;

    @Column(name = "email_lowercase", unique = true, length = MAX_LENGTH_EMAIL)
    private String emailLowercase;

    /**
     * The hashed password of an account. May never be plain text!
     */
    private String password;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "verification_token_id")
    private VerificationToken verificationToken;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "forgot_password_token_id")
    private VerificationToken forgotPasswordToken;

    /**
     * Whether the account-owner already verified his email.
     */
    private boolean verified;

    @Column(nullable = false)
    private boolean isBanned = false;

    private String banReason;

    @Column(nullable = false)
    private int loginFailCount = 0;
}
