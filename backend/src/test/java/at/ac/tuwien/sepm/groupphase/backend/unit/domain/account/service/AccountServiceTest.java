package at.ac.tuwien.sepm.groupphase.backend.unit.domain.account.service;


import at.ac.tuwien.sepm.groupphase.backend.common.exception.AlreadyExistsException;
import at.ac.tuwien.sepm.groupphase.backend.common.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.common.exception.NotVerifiedException;
import at.ac.tuwien.sepm.groupphase.backend.config.data.NewData;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.AccountDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.service.AccountService;
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

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class AccountServiceTest {

    @Autowired
    PlatformTransactionManager txm;

    TransactionStatus txstatus;

    @Autowired
    AccountService accountService;

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
    @DisplayName("create Organizer")
    public void createOrganizer() {
        AccountDto accountDto = NewData.createOrganizerAccountDto();
        AccountDto accountDto1 = accountService.registerAccount(accountDto, null, Locale.ROOT);

        assertEquals(accountDto1.getEmail(), accountDto.getEmail());
        assertFalse(accountDto1.isVerified());
    }

    @Test
    @DisplayName("create Participant")
    public void createParticipant() {
        AccountDto accountDto = NewData.createParticipantAccountDto();
        AccountDto accountDto1 = accountService.registerAccount(accountDto, null, Locale.ROOT);

        assertEquals(accountDto1.getEmail(), accountDto.getEmail());
        assertFalse(accountDto1.isVerified());
    }

    @Test
    @DisplayName("create Participant with no new mail")
    public void createParticipantWithNoNewMail() {
        AccountDto accountDto = NewData.createParticipantAccountDto();
        accountService.registerAccount(accountDto, null, Locale.ROOT);

        assertThrows(AlreadyExistsException.class, () -> accountService.registerAccount(accountDto, null, Locale.ROOT));
    }

    @Test
    @DisplayName("find an Account which does not exist")
    public void findAccountDoesNotExist() {
        assertThrows(NotFoundException.class, () -> accountService.findAccountByEmail("abc@def"));
    }

}
