package at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.repository;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.ParticipantAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    /**
     * Returns the event identified by its access token if it exists.
     *
     * @param accessToken The invite token of the event that is to be found.
     * @return The event whose token matches the provided one if it exists.
     */
    Optional<Event> findByAccessToken(final String accessToken);

    @Query("select event from Event event where event.accessToken = :token and event.organizer.emailLowercase = :email")
    Optional<Event> findEventFromOrganizer(@Param("email") String email, @Param("token") String accessToken);

    @Query("select event from Event event join event.participations participations " +
        "where event.accessToken = :token and participations.participant.emailLowercase = :email")
    Optional<Event> findEventWithParticipant(@Param("email") String email, @Param("token") String accessToken);

    @Query("select event from Event event where event.organizer.emailLowercase = :email and event.status > 25")
    Optional<Event> findOngoingEventFromOrganizer(@Param("email") String email);

    @Query("select event from Event event join event.participations participation where participation.status >= 30 and " +
        "participation.arrivedAtEvent = true and participation.participant.emailLowercase = :email")
    Optional<Event> findOngoingEventWithArrivedParticipant(@Param("email") String email);

    @Query("select event from Event event where event.organizer.emailLowercase = :email order by event.startDateAndTime")
    List<Event> getAllByOrganizer(@Param("email") String email);

    /**
     * Returns all future (UTC) events by organizer.
     * It is possible to use jpql's CURRENT_TIMESTAMP, but it uses UTC time
     * and not current user's time zone. Because of that time is parsed two times (providing UTC-1).
     * In order to filter by UTC,
     * a second @param current_date with default "LocalDateTime.now()" is required.
     *
     * @param email        of organizer.
     * @param current_date is the current time in UTC.
     * @return all future events by organizer's email.
     */
    @Query("select event from Event event where " +
        "event.startDateAndTime > :currentTime and event.organizer.emailLowercase = :email order by event.startDateAndTime")
    List<Event> getAllFutureEventsByOrganizer(@Param("email") String email, @Param(value = "currentTime") LocalDateTime current_date);

    @Query("select event from Event event join event.participations participation where participation.participant.emailLowercase = :email")
    List<Event> getAllByParticipant(@Param(value = "email") String email);

    boolean existsByAccessToken(final String accessToken);

    boolean existsByEntranceToken(final String entranceToken);

    /* ATTENTION
     * check for status >= 30 is done to avoid having to load all participations or having to check for many statuses.
     * See EventStatus.java.
     * ATTENTION
     */
    @Query("select count(participation) from Event event join event.participations participation where event.id = :id " +
        "and participation.status >= 30")
    long countArrivals(@Param(value = "id") long eventId);

    @Query("select count(participation) from Event event join event.participations participation where event.id = :id " +
        "and participation.status >= 30 and participation.group.title = :title")
    long countGroupArrivals(@Param(value = "id") long eventId, @Param(value = "title") String groupTitle);

    @Query("select participation.participant from Event event join event.participations participation " +
        "where event.id = :id and participation.status >= 30")
    List<ParticipantAccount> getArrivedParticipants(@Param(value = "id") long eventId);

    @Query("select participation.participant.id from Event event join event.participations participation " +
        "where event.id = :id and participation.status >= 30")
    List<Long> getIdsOfArrivedParticipants(@Param(value = "id") long eventId);

    /* ATTENTION
     * check for status >= 30 is done to avoid having to load all participations or having to check for many statuses
     * See EventStatus.java.
     * ATTENTION
     */
}
