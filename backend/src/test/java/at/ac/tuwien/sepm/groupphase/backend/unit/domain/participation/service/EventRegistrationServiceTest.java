package at.ac.tuwien.sepm.groupphase.backend.unit.domain.participation.service;


import at.ac.tuwien.sepm.groupphase.backend.config.data.NewData;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.service.AccountService;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.*;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.service.EventService;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.persistance.entity.Participation;
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
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles({"test", "datagen"})
public class EventRegistrationServiceTest {

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
    public void setupDBTransaction() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txstatus = txm.getTransaction(def);
        assumeTrue(txstatus.isNewTransaction());
        txstatus.setRollbackOnly();
    }

    @AfterEach
    public void tearDownDBData() {
        txm.rollback(txstatus);
    }

    @Test
    @DisplayName("register for event")
    public void registerForEvent() {
        AddUpdateEventDto addUpdateEventDto = NewData.createAddUpdateEventDto(true);
        OrganizerEventDetailsDto organizerEventDetailsDto = eventService.createEvent(addUpdateEventDto, usedOrganizer);

        eventRegistrationService.registerForEvent(organizerEventDetailsDto.getAccessToken(), organizerEventDetailsDto.getGroups().getFirstGroup().getTitle(), "p@p");

        Set<Participation> set = eventService.getEventByAccessToken(organizerEventDetailsDto.getAccessToken()).getParticipations();

        boolean containsEvent = false;

        for(Participation p:set){
            if(p.getParticipant().getEmailLowercase().equals("p@p")){
                containsEvent=true;
                break;
            }
        }

        assertTrue(containsEvent);
    }

}
