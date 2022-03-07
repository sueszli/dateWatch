package at.ac.tuwien.sepm.groupphase.backend.domain.account.event;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.Account;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Locale;


@Getter
@RequiredArgsConstructor
public abstract class AccountActionEvent {
    private final Account account;
    private final String baseUrl;
    private final Locale locale;
}
