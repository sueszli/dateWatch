package at.ac.tuwien.sepm.groupphase.backend.config.profile.datagen;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.AccountDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.AdminAccountDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.OrganizerAccountDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.ParticipantAccountDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.OrganizerAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.ParticipantAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.service.AccountService;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.AddUpdateEventDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.EventGroupDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.dto.EventGroupsDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.entity.*;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.persistence.repository.EventRepository;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.service.EventExecutionService;
import at.ac.tuwien.sepm.groupphase.backend.domain.event.service.EventService;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.service.EventArrivalService;
import at.ac.tuwien.sepm.groupphase.backend.domain.participation.service.EventRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;


@Slf4j
@Profile("datagen")
@Component
@RequiredArgsConstructor
public class DataGenerator {

    private final DataSource dataSource;

    private final AccountService accountService;
    private final EventArrivalService eventArrivalService;
    private final EventRegistrationService eventRegistrationService;
    private final EventExecutionService eventExecutionService;
    private final EventRepository eventRepository;
    private final EventService eventService;


    @PostConstruct
    private void generateTestData() throws SQLException {
        log.info("Generating data…");
        try (var connection = dataSource.getConnection()) {

            //region ADMINS
            createAdmin("patrick.admin@datewatch.at");
            log.info("Generated 1 admin");
            //endregion

            //region ORGANIZERS
            var testOrganizer = createOrganizer("o", "o", "o@o"); // für Tests
            log.info("Generated default organizer o@o");

            var canceledEventOrganizer = createOrganizer("Ruth", "Organizer", "ruth.organizer@datewatch.at", "Datewatch");
            var liveEventOrganizer = createOrganizer("Tobias", "Organizer", "tobias.organizer@datewatch.at", "Datewatch");
            log.info("Generated 2 organizers");
            //endregion

            //region PARTICIPANTS
            var defaultParticipant = createParticipant("p"); // für Tests
            log.info("Generated default participant p@p");

            var toBeDeletedParticipant = createParticipant("Jan", "jabaryyahya@gmail.com", "+43 012 3456789"); // email: p1@p1
            var delayedMaleParticipantWithoutMatch = createParticipant("Dmytro", "domork3@gmail.com", "+43 676 5921204");
            var rejectedParticipant = createParticipant("Patrick", "patrick989@gmx.at", "+43 000 0000000");
            var femaleMatchlessParticipantWaitingOutFirstRound = createParticipant("Ruth", "ruth.slavicky@gmail.com", "+43 012 9876543");
            var oneMatchMaleParticipant = createParticipant("Jan", "jan.participant@datewatch.at", "+43 012 3456789"); // email: p1@p1
            var secondOneMatchMaleParticipant = createParticipant("Richard", "e11908080@student.tuwien.ac.at", "+43 012 9876543");
            var noMatchesFemaleParticipant = createParticipant("Patrina", "patrina.participant@datewatch.at", "+43 987 6543210");
            var multipleMatchesFemaleParticipant = createParticipant("Tobina", "tobi.dv@gmail.com", "+43 258 7413690");
            log.info("Generated 7 participants");
            //endregion

            //region EVENTS
            var addUpdateEventDto = new AddUpdateEventDto();
            addUpdateEventDto.setTitle("Freihaus Dating Party");
            addUpdateEventDto.setDescription("Generic description");
            addUpdateEventDto.setDurationInMinutes(70);
            addUpdateEventDto.setMaxParticipants(6);
            addUpdateEventDto.setRoundDurationInSeconds(30);
            addUpdateEventDto.setDescription("Fun dating party at Freihaus.");
            addUpdateEventDto.setStartDateAndTime(LocalDateTime.now().minusMinutes(5));
            addUpdateEventDto.setCity("Vienna");
            addUpdateEventDto.setStreet("Wiedner Hauptstraße 8-10");
            addUpdateEventDto.setPostcode("1040");
            addUpdateEventDto.setPublic(true);

            var groups = new EventGroupsDto();
            groups.setFirstGroup(new EventGroupDto("Boys", "All the males", null));
            groups.setSecondGroup(new EventGroupDto("Gal's", "All the females", null));
            addUpdateEventDto.setGroups(groups);

            var eventDto = eventService.createEvent(addUpdateEventDto, liveEventOrganizer.getEmailLowercase());
            var liveEvent = eventService.getEventByAccessToken(eventDto.getAccessToken());
            registerParticipants(liveEvent,
                List.of(delayedMaleParticipantWithoutMatch, oneMatchMaleParticipant, secondOneMatchMaleParticipant, rejectedParticipant),
                List.of(femaleMatchlessParticipantWaitingOutFirstRound, noMatchesFemaleParticipant, multipleMatchesFemaleParticipant));
            log.info("Generated live event");

            var oldEvent1 = createEvent(liveEventOrganizer, LocalDateTime.now().minusDays(10).withMinute(30), "Saturday night at Wednesday", 210, "Goldene Stiege", "Mödling", "2340", true, "AAAAA");
            var oldEvent2 = createEvent(liveEventOrganizer, LocalDateTime.now().plusDays(3).withMinute(0), "Break the Guards", 194, "Am Fasangarten 2", "Vienna", "1120", false, "BBBBB");
            var oldEvent3 = createEvent(canceledEventOrganizer, LocalDateTime.now().minusDays(30).withMinute(45), "Spooky-Wookie-Vienna-Bar", 120, "Mollardgasse 15", "Vienna", "1060", true, "CCCCC");
            var oldEvent4 = createEvent(canceledEventOrganizer, LocalDateTime.now().minusDays(4).withMinute(0), "Dating in Vienna", 240, "Stephansplatz 3", "Vienna", "1010", false, "DDDDD");

            //Dima
            var futureEvent1 = createEvent(canceledEventOrganizer, LocalDateTime.now().plusDays(15).withMinute(15), "Dating am Ring", 90, "am Ring 1", "Vienna", "1010", true, "EEEEE");
            var futureEvent2 = createEvent(liveEventOrganizer, LocalDateTime.now().plusDays(25).withMinute(45), "It's a sweet love, baby", 45, "Stumpergasse 13", "Vienna", "1060", true, "FFFFF");
            var futureEvent3 = createEvent(liveEventOrganizer, LocalDateTime.now().plusDays(35).withMinute(0), "Date for the motherland", 150, "Drechslergasse 42", "Vienna", "1140", true, "GGGGG");

            // Tobias
            var futureEvent4 = createEvent(canceledEventOrganizer, LocalDateTime.now().plusDays(4).withMinute(15), "Wednesday night at Saturday", 500, "Operngasse 7", "Vienna", "1010", true, "HHHHH");
            var futureEvent5 = createEvent(liveEventOrganizer, LocalDateTime.now().plusDays(14).withMinute(30), "Super cool event", 147, "Am Gartenfasan 2", "Cool City", "1120", true, "IIIII");
            var futureEvent6 = createEvent(liveEventOrganizer, LocalDateTime.now().plusDays(8).withMinute(50), "Halloween Speed-Dating", 30, "Gruselgasse 1", "Vienna", "0000", true, "JJJJJ");

            // Jan
            var futureEvent7 = createEvent(canceledEventOrganizer, LocalDateTime.now().plusDays(3).withMinute(0), "Sugar mommys lookin' for young fellas", 120, "MoneyLaundryStreet 1", "San Francisco", "1111", true, "KKKKK");
            var futureEvent8 = createEvent(liveEventOrganizer, LocalDateTime.now().plusDays(5).withMinute(15), "Gay Dating Fun", 69, "GrindrStreet 2", "FunkyTown", "1111", true, "LLLLL");
            var futureEvent9 = createEvent(liveEventOrganizer, LocalDateTime.now().plusDays(30).withMinute(30), "Rookie dating introduction", 120, "RookyStreet 15", "Vienna", "1060", true, "MMMMM");

            // Ruth
            var futureEvent10 = createEvent(canceledEventOrganizer, LocalDateTime.now().plusDays(4).withMinute(30), "Party im EI7", 230, "Rathausplatz", "Gmunden", "3340", true, "NNNNN");
            var futureEvent11 = createEvent(liveEventOrganizer, LocalDateTime.now().plusDays(14).withMinute(45), "Fun dating for engineers", 190, "Am Fasangarten 2", "Vienna", "1120", true, "OOOOO");
            var futureEvent12 = createEvent(liveEventOrganizer, LocalDateTime.now().plusDays(10).withMinute(0), "Junior Dev 4 Hire", 120, "Mollardgasse 15", "Vienna", "1060", true, "PPPPP");

            // Richard
            var futureEvent13 = createEvent(canceledEventOrganizer, LocalDateTime.now().plusDays(12).withMinute(30), "Musicians Symphony", 120, "Karlsplatz 1", "Vienna", "1010", true, "RRRRR");
            var futureEvent14 = createEvent(liveEventOrganizer, LocalDateTime.now().plusDays(43).withMinute(30), "Meet the Beer Buddies", 189, "Kellergasse 9", "Eibesbrunn", "2304", true, "QQQQQ");
            var futureEvent15 = createEvent(liveEventOrganizer, LocalDateTime.now().plusDays(2).withMinute(45), "Sinni's Chance", 296, "Bahnstrasse 4", "Leopoldsdorf im Marchfelde", "2285", true, "SSSSS");
            //endregion
        }
        log.info("Finished generating data without error.");
    }

    private void letParticipantsArrive(Event event, List<ParticipantAccount> participants) {
        for (var participant : participants) {
            eventArrivalService.participantArrivedAtEvent(
                event.getAccessToken(), event.getEntranceToken(), participant.getEmailLowercase());
        }
    }

    private void letParticipantsLeave(Event event, List<ParticipantAccount> participants) {
        for (var participant : participants) {
            eventExecutionService.closeEventParticipant(event.getAccessToken(), participant.getEmailLowercase());
        }
    }

    private void registerParticipants(Event event,
                                      List<ParticipantAccount> firstGroupParticipants,
                                      List<ParticipantAccount> secondGroupParticipants) {
        for (var participant : firstGroupParticipants) {
            eventRegistrationService.registerForEvent(
                event.getAccessToken(), event.getGroups().getFirstGroup().getTitle(), participant.getEmailLowercase());
        }
        for (var participant : secondGroupParticipants) {
            eventRegistrationService.registerForEvent(
                event.getAccessToken(), event.getGroups().getSecondGroup().getTitle(), participant.getEmailLowercase());
        }
    }

    private OrganizerAccount createOrganizer(String firstName, String lastName, String email) {
        return createOrganizer(firstName, lastName, email, null);
    }

    private OrganizerAccount createOrganizer(String firstName, String lastName, String email, String organizationName) {
        var organizerAccountDto = new OrganizerAccountDto();
        organizerAccountDto.setContactPersonFirstName(firstName);
        organizerAccountDto.setContactPersonLastName(lastName);
        organizerAccountDto.setOrganizationName(organizationName);
        createAccount(organizerAccountDto, email);
        return accountService.findAccountByEmail(email, OrganizerAccount.class);
    }

    private void createAdmin(String email) {
        var accountDto = new AdminAccountDto();
        accountDto.setName("admin");
        createAccount(accountDto, email);
    }

    private ParticipantAccount createParticipant(String nickname) {
        return createParticipant(nickname, null, null);
    }

    private ParticipantAccount createParticipant(String nickname, String email) {
        return createParticipant(nickname, email, null);
    }

    private ParticipantAccount createParticipant(String nickname, String email, String phoneNumber) {
        if (email == null) {
            var nicknameLowerCase = nickname.toLowerCase();
            email = nicknameLowerCase + "@" + nicknameLowerCase;
        }
        var participantAccountDto = new ParticipantAccountDto();
        participantAccountDto.setNickname(nickname);
        participantAccountDto.setPhone(phoneNumber);
        createAccount(participantAccountDto, email);
        return accountService.findAccountByEmail(email, ParticipantAccount.class);
    }

    private void createAccount(AccountDto accountDto, String email) {
        accountDto.setEmail(email);
        accountDto.setPassword("123");
        accountService.registerAccount(accountDto, null, Locale.ROOT);
        var token = accountService.findAccountByEmail(email).getVerificationToken().getToken();
        accountService.confirmRegistration(token);
    }

    private EventGroup createGroup(String title, String description) {
        var group = new EventGroup();
        group.setTitle(title);
        group.setDescription(description);
        return group;
    }

    private Event createEvent(OrganizerAccount organizer, LocalDateTime startTime, String title, int durationInMinutes, String street,
                              String city, String postcode, boolean publicEvent, String accesstoken) {
        var event = new Event();
        event.setOrganizer(organizer);
        event.setTitle(title);
        event.setDescription("Generic description");
        event.setDurationInMinutes(durationInMinutes);
        event.setMaxParticipants(10);
        event.setRoundDurationInSeconds(durationInMinutes / 2);
        event.setStartDateAndTime(startTime);
        event.setCity(city);
        event.setStreet(street);
        event.setPostcode(postcode);
        event.setAccessToken(accesstoken);
        event.setEntranceToken(accesstoken);
        event.setPublic(publicEvent);

        var groups = new EventGroups();
        groups.setFirstGroup(createGroup("Male", "All the males"));
        groups.setSecondGroup(createGroup("Female", "All the females"));
        event.setGroups(groups);

        return eventRepository.save(event);
    }

}
