package at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.repository;

import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    @Query("select feedback from Feedback feedback where feedback.organizerEmailLowercase = :email")
    List<Feedback> getAllByOrganizer(@Param("email") String email);
}

