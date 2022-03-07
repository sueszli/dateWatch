package at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.repository;

import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.PairingRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface PairingRoundRepository extends JpaRepository<PairingRound, Long> {

    @Query("select p from PairingRound p where p.event.id = :eventId and p.startedAt is null")
    Optional<PairingRound> findUpcomingPairingRound(@Param("eventId") Long eventId);
}
