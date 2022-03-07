package at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.repository;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.ParticipantAccount;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ParticipantAccountRepository extends JpaRepository<ParticipantAccount, Long> {

}

