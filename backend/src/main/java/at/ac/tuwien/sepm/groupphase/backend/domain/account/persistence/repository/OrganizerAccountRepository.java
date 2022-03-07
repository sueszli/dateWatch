package at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.repository;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.Account;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.OrganizerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrganizerAccountRepository extends JpaRepository<OrganizerAccount, Long> {

    Optional <Account> getFirstByEmailLowercase(final String emailLowercase);

    @Query("select o from OrganizerAccount o where lower(o.contactPersonFirstName) like lower(concat('%', :str, '%')) or lower(o.contactPersonLastName) like lower(concat('%', :str, '%'))")
    List<OrganizerAccount> findAllByNameSubstring(@Param("str") final String substring);
}
