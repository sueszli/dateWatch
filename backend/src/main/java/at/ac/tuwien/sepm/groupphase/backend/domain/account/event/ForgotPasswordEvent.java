package at.ac.tuwien.sepm.groupphase.backend.domain.account.event;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.Account;

import java.util.Locale;


public class ForgotPasswordEvent extends AccountActionEvent {
    public ForgotPasswordEvent(Account account, String baseUrl, Locale locale) {
        super(account, baseUrl, locale);
    }
}
