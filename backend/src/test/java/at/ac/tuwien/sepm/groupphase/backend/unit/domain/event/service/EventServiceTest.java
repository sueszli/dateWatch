package at.ac.tuwien.sepm.groupphase.backend.unit.domain.event.service;


import at.ac.tuwien.sepm.groupphase.backend.common.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.common.exception.NotVerifiedException;
import at.ac.tuwien.sepm.groupphase.backend.config.data.NewData;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.AccountDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.service.AccountService;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.*;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.Event;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.service.EventService;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.service.EventRegistrationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles({"test", "datagen"})
public class EventServiceTest {

    @Autowired
    PlatformTransactionManager txm;

    TransactionStatus txstatus;

    @Autowired
    EventService eventService;

    @Autowired
    EventRegistrationService eventRegistrationService;

    @Autowired
    AccountService accountService;

    private static final String usedOrganizer = "tobias.organizer@datewatch.at";

    @BeforeEach
    public void setupDBTransaction(){
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txstatus = txm.getTransaction(def);
        assumeTrue(txstatus.isNewTransaction());
        txstatus.setRollbackOnly();
    }

    @AfterEach
    public void tearDownDBData(){
        txm.rollback(txstatus);
    }

    @Test
    @DisplayName("Finding event by non-existing access-token should throw NotFoundException")
    public void findingEventByAccessToken_nonExisting_shouldThrowNotFoundException(){
        assertThrows(NotFoundException.class, () -> eventService.getEventByAccessToken("ABCDEFG"));
    }

    @Test
    @DisplayName("Finding event by existing access-token as organizer")
    public void findingEventByAccessToken_as_Organizer(){
        EventDetailsDto eventDetailsDto = eventService.getEventDtoByAccessToken("AAAAA", usedOrganizer);
        assertEquals("Saturday night at Wednesday", eventDetailsDto.getTitle());
        OrganizerEventDetailsDto organizerEventDetailsDto = (OrganizerEventDetailsDto) eventDetailsDto;
        assertEquals("AAAAA", organizerEventDetailsDto.getEntranceToken());
    }

    @Test
    @DisplayName("Finding event by existing access-token as participant")
    public void findingEventByAccessToken_as_Participant(){
        EventDetailsDto eventDetailsDto = eventService.getEventDtoByAccessToken("AAAAA", "p@p");
        assertEquals("Saturday night at Wednesday", eventDetailsDto.getTitle());
        ParticipantEventDetailsDto participantEventDetailsDto = (ParticipantEventDetailsDto) eventDetailsDto;
        assertEquals(usedOrganizer, participantEventDetailsDto.getOrganizer().getEmail());
    }

    @Test
    @DisplayName("create Event")
    public void createEvent() {
        AddUpdateEventDto addUpdateEventDto = NewData.createAddUpdateEventDto(true);
        OrganizerEventDetailsDto organizerEventDetailsDto = eventService.createEvent(addUpdateEventDto, usedOrganizer);

        assertThat(organizerEventDetailsDto.getAccessToken()).isNotNull();
        assertThat(organizerEventDetailsDto.getEntranceToken()).isNotNull();
        assertEquals("A", organizerEventDetailsDto.getTitle());
        assertEquals("B", organizerEventDetailsDto.getDescription());
        assertFalse(organizerEventDetailsDto.isHasRegistrationClosed());
        assumeTrue(organizerEventDetailsDto.isPublic());

        ParticipantEventDetailsDto participantEventDetailsDto = (ParticipantEventDetailsDto) eventService.getEventDtoByAccessToken(organizerEventDetailsDto.getAccessToken(), "p@p");

        assertEquals(usedOrganizer, participantEventDetailsDto.getOrganizer().getEmail());
    }

    @Test
    @DisplayName("create Event throws NotVerifiedException")
    public void createEventNotVerified() {
        AccountDto accountDto = NewData.createOrganizerAccountDto();
        accountService.registerAccount(accountDto, null, Locale.ROOT);

        AddUpdateEventDto addUpdateEventDto = NewData.createAddUpdateEventDto(true);

        assertThrows(NotVerifiedException.class, () -> eventService.createEvent(addUpdateEventDto, accountDto.getEmail()));

    }

    @Test
    @DisplayName("Get All Events as Organizer")
    public void getAllEventsAsOrganizer() {
        List<EventDetailsDto> eventsDto = eventService.getAllEvents(usedOrganizer);
        assertFalse(eventsDto.isEmpty());
        Event event;
        for (EventDetailsDto e : eventsDto) {
            event = eventService.getEventByAccessToken(e.getAccessToken());
            assertEquals(event.getOrganizer().getEmailLowercase(), usedOrganizer);
        }
    }

    @Test
    @DisplayName("Update Event")
    public void updateEvent() {
        AddUpdateEventDto addUpdateEventDtoOld = NewData.createAddUpdateEventDto(true);
        OrganizerEventDetailsDto organizerEventDetailsDtoOld = eventService.createEvent(addUpdateEventDtoOld, usedOrganizer);

        AddUpdateEventDto addUpdateEventDtoNew = NewData.createAddUpdateEventDto(true);
        addUpdateEventDtoNew.setTitle("New Title");
        addUpdateEventDtoNew.setPostcode("1234");

        OrganizerEventDetailsDto organizerEventDetailsDtoNew = eventService.updateEvent(organizerEventDetailsDtoOld.getAccessToken(), addUpdateEventDtoNew, usedOrganizer);

        assertNotEquals(organizerEventDetailsDtoOld.getTitle(), organizerEventDetailsDtoNew.getTitle());
        assertNotEquals(organizerEventDetailsDtoOld.getPostcode(), organizerEventDetailsDtoNew.getPostcode());
        assertEquals(organizerEventDetailsDtoOld.getDescription(), organizerEventDetailsDtoNew.getDescription());
    }

    @Test
    @DisplayName("Give and Get Feedback")
    public void giveAndGetFeedback() {
        AddUpdateEventDto addUpdateEventDto = NewData.createAddUpdateEventDto(true);
        OrganizerEventDetailsDto organizerEventDetailsDto = eventService.createEvent(addUpdateEventDto, usedOrganizer);

        eventRegistrationService.registerForEvent(organizerEventDetailsDto.getAccessToken(), organizerEventDetailsDto.getGroups().getFirstGroup().getTitle(), "p@p");
        eventRegistrationService.registerForEvent(organizerEventDetailsDto.getAccessToken(), organizerEventDetailsDto.getGroups().getFirstGroup().getTitle(), "patrina.participant@datewatch.at");
        eventRegistrationService.registerForEvent(organizerEventDetailsDto.getAccessToken(), organizerEventDetailsDto.getGroups().getFirstGroup().getTitle(), "jan.participant@datewatch.at");

        var feedbackDto1 = new FeedbackDto();
        feedbackDto1.setEventAccessToken(organizerEventDetailsDto.getAccessToken());
        feedbackDto1.setEventTitle(organizerEventDetailsDto.getTitle());
        feedbackDto1.setMessage("Greate Event. " + "p@p" + " :-)");
        eventService.giveFeedback(feedbackDto1, "p@p");

        var feedbackDto2 = new FeedbackDto();
        feedbackDto2.setEventAccessToken(organizerEventDetailsDto.getAccessToken());
        feedbackDto2.setEventTitle(organizerEventDetailsDto.getTitle());
        feedbackDto2.setMessage("Greate Event. " + "patrina.participant@datewatch.at" + " :-)");
        eventService.giveFeedback(feedbackDto2, "patrina.participant@datewatch.at");

        var feedbackDto3 = new FeedbackDto();
        feedbackDto3.setEventAccessToken(organizerEventDetailsDto.getAccessToken());
        feedbackDto3.setEventTitle(organizerEventDetailsDto.getTitle());
        feedbackDto3.setMessage("Greate Event. " + "jan.participant@datewatch.at" + " :-)");
        eventService.giveFeedback(feedbackDto3, "jan.participant@datewatch.at");


        List<FeedbackDto> feedBackDtos = eventService.getAllFeedback(usedOrganizer);

        List<FeedbackDto> feedBackDtoFormThisEvents = feedBackDtos.stream().filter(f -> f.getEventAccessToken().equals(organizerEventDetailsDto.getAccessToken())).collect(toList());

        assertEquals(feedBackDtoFormThisEvents.size(), 3);

    }

}
