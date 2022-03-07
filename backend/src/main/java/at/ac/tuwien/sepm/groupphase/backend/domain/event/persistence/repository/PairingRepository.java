package at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.repository;

import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Pairing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface PairingRepository extends JpaRepository<Pairing, Long> {

    @Query("select count(pairing) from Pairing pairing join pairing.pairingRound round where round.event.id = :eventId")
    int countAllByEventId(@Param("eventId") Long eventId);

    @Query("select count(pairing) from Pairing pairing join pairing.pairingRound round where " +
        "pairing.initiatorApprovedMatch = true and pairing.pairedPersonApprovedMatch = true " +
        "and pairing.confirmed = true and round.event.id = :eventId")
    int countAllMatchesByEventId(@Param("eventId") Long eventID);

    @Query("select pairing from Pairing pairing join pairing.pairingRound round where round.event.id = :eventId")
    List<Pairing> getAllByEventId(@Param("eventId") Long eventId);

    @Query("select pairing from Pairing pairing where " +
        "((pairing.initiator.emailLowercase = :userEmail and pairing.pairedPerson.emailLowercase = :otherUserEmail) " +
        "or (pairing.pairedPerson.emailLowercase = :userEmail and pairing.initiator.emailLowercase = :otherUserEmail))")
    List<Pairing> findUserPairings(@Param("userEmail") String userEmail, @Param("otherUserEmail") String otherPersonsEmail);

    @Query("select pairing from Pairing pairing where pairing.pairingRound.id = :pairingRoundId" +
        " and ((pairing.initiator.emailLowercase = :userEmail and pairing.pairedPerson.pairingTokenForCurrentEvent = :pairingToken)" +
        " or (pairing.pairedPerson.emailLowercase = :userEmail and pairing.initiator.pairingTokenForCurrentEvent = :pairingToken))")
    Optional<Pairing> findUserPairing(@Param("pairingRoundId") Long pairingRoundId,
                                      @Param("userEmail") String userEmail,
                                      @Param("pairingToken") String otherPersonsPairingToken);

    // Joins and case statements can not be used and using JpaSpecificationExecutor<Pairing> would be an overkill
    // see: https://openjpa.apache.org/builds/2.4.2/apache-openjpa/docs/jpa_langref.html#jpa_langref_case_expressions

    @Query("select p from Pairing p where p.pairingRound.id = :roundId " +
        "and (p.pairedPerson.id = :userId or p.initiator.id = :userId)")
    Optional<Pairing> findByRoundAndParticipant(@Param("roundId") Long roundId, @Param("userId") Long userId);

    @Query("select pairing from Pairing pairing where pairing.initiatorApprovedMatch = true " +
        "and pairing.pairedPersonApprovedMatch = true and pairing.confirmed = true and " +
        "(pairing.initiator.id = :userId or pairing.pairedPerson.id = :userId)")
    List<Pairing> getAllMatchPairingsForParticipant(@Param("userId") Long participantId);
}
