package at.ac.tuwien.sepm.groupphase.backend.domain.token.persistence.repository;

import at.ac.tuwien.sepm.groupphase.backend.domain.token.persistence.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    void deleteByToken(final String token);
}
