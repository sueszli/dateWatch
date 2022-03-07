package at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.repository;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.Account;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.OrganizerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Returns the first account identified by their (lowercase) email address if exists.
     *
     * @param emailLowercase The lower case email address of the account that is to be found.
     * @return The account whose email matches the provided one iff it exists.
     */
    Optional<Account> findFirstByEmailLowercase(final String emailLowercase);

    @Query("select acc from Account acc where acc.verificationToken.token = :token")
    Optional<Account> findByVerificationToken(@Param("token") final String token);

    @Query("select acc from Account acc where acc.forgotPasswordToken.token = :token")
    Optional<Account> findByForgotPasswordToken(@Param("token") final String token);

    /**
     * @param emailLowercase The email in question.
     * @return True if an account with the specified email already exists, false otherwise.
     */
    boolean existsByEmailLowercase(final String emailLowercase);

    @Query("select a,o from Account a join OrganizerAccount o on a.id=o.id where a.emailLowercase = :email")
    Optional<Account> getOrganizerProfile(@Param("email") final String email);

    @Query("select account from Account account where not (account.accountType = 'admin')")
    List<Account> findNonAdminAccounts();
}
