package at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.repository;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.Account;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.AdminAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminAccountRepository extends JpaRepository<AdminAccount, Long> {

    Optional <Account> getFirstByEmailLowercase(final String emailLowercase);

}
