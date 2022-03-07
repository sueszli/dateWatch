package at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.repository;

import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    @Query("select participation from Participation participation where participation.participant.id = :id and " +
        "participation.event.accessToken = :token")
    Optional<Participation> findParticipation(@Param("token") String eventAccessToken, @Param("id") Long participantId);

    long countAllByEventId(Long eventId);

    long countAllByEventIdAndGroupTitle(Long eventId, String groupTitle);

    boolean existsByEventIdAndParticipantId(Long eventId, Long participantId);

    @Query("select participation from Participation participation where participation.event.id = :id" +
        " and participation.participant.pairingTokenForCurrentEvent = :token")
    Optional<Participation> findParticipationByPairingToken(@Param(value = "id") long eventId,
                                                            @Param("token") String pairingToken);

    @Query("select participation from Participation participation where participation.event.id = :id" +
        " and participation.participant.emailLowercase = :email")
    Optional<Participation> findParticipationByEmail(@Param(value = "id") long eventId,
                                                     @Param("email") String participantEmail);

    @Modifying
    @Query("delete from Participation p where p.event.id = :event_id and p.participant.id = :participant_id")
    void deleteByEventIdAndParticipantId(@Param(value = "event_id") Long eventId, @Param(value = "participant_id") Long participantId);

}
