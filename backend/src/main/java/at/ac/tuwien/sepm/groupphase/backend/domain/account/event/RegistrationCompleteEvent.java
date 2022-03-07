package at.ac.tuwien.sepm.groupphase.backend.domain.account.event;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.Account;

import java.util.Locale;


public class RegistrationCompleteEvent extends AccountActionEvent {
    public RegistrationCompleteEvent(Account account, String baseUrl, Locale locale) {
        super(account, baseUrl, locale);
    }
}
