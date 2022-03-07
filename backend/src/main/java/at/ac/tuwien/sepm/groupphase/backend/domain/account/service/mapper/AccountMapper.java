package at.ac.tuwien.sepm.groupphase.backend.domain.account.service.mapper;

import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.AccountDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.AdminAccountDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.OrganizerAccountDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.dto.ParticipantAccountDto;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.exception.NoMapperForOperationException;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.Account;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.AdminAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.OrganizerAccount;
import at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity.ParticipantAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;


@Mapper
public interface AccountMapper {

    //region Domain to DTO
    @Mapping(target = "email", source = "emailLowercase")
    OrganizerAccountDto toDto(OrganizerAccount organizerAccount);

    @Mapping(target = "email", source = "emailLowercase")
    @Mapping(target = "phone", source = "phone")
    ParticipantAccountDto toDto(ParticipantAccount participantAccount);

    @Mapping(target = "email", source = "emailLowercase")
    AdminAccountDto toDto(AdminAccount adminAccount);

    default AccountDto toDto(Account account) {
        if (account instanceof OrganizerAccount) {
            return toDto((OrganizerAccount) account);
        } else if (account instanceof ParticipantAccount) {
            return toDto((ParticipantAccount) account);
        } else if (account instanceof AdminAccount) {
            return toDto((AdminAccount) account);
        }

        throw new NoMapperForOperationException(
            "There is no mapper for " + account.getClass().getName()
                + ". Add a mapping to AccountMapper.map(Account account)."
        );
    }

    List<AccountDto> toDtos(List<Account> accounts);
    //endregion

    //region DTO to Domain
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "password", source = "hashedPassword")
    @Mapping(target = "emailLowercase", source = "organizerAccountDto.email")
    OrganizerAccount toEntity(OrganizerAccountDto organizerAccountDto, String hashedPassword);

    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "password", source = "hashedPassword")
    @Mapping(target = "emailLowercase", source = "participantAccountDto.email")
    @Mapping(target = "phone", source = "participantAccountDto.phone")
    ParticipantAccount toEntity(ParticipantAccountDto participantAccountDto, String hashedPassword);

    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "password", source = "hashedPassword")
    @Mapping(target = "emailLowercase", source = "adminAccountDto.email")
    @Mapping(target = "name", source = "adminAccountDto.name")
    AdminAccount toEntity(AdminAccountDto adminAccountDto, String hashedPassword);

    default Account toEntity(AccountDto accountDto, String hashedPassword) {
        if (accountDto instanceof OrganizerAccountDto) {
            return toEntity((OrganizerAccountDto) accountDto, hashedPassword);
        } else if (accountDto instanceof ParticipantAccountDto) {
            return toEntity((ParticipantAccountDto) accountDto, hashedPassword);
        } else if (accountDto instanceof AdminAccountDto) {
            return toEntity((AdminAccountDto) accountDto, hashedPassword);
        }

        throw new NoMapperForOperationException(
            "There is no mapper for " + accountDto.getClass().getName() + "."
                + " Add a mapping to AccountMapper.map(AccountDto accountDto, String hashedPassword).");
    }
    //endregion

    //region Domain-Update from DTO
    void updateEntity(@MappingTarget OrganizerAccount organizerAccount, OrganizerAccountDto organizerAccountDto);

    void updateEntity(@MappingTarget ParticipantAccount participantAccount, ParticipantAccountDto participantAccountDto);

    default void updateEntity(Account account, AccountDto accountDto) {
        if (account instanceof OrganizerAccount && accountDto instanceof OrganizerAccountDto) {
            updateEntity((OrganizerAccount) account, (OrganizerAccountDto) accountDto);
            return;
        }
        if (account instanceof ParticipantAccount && accountDto instanceof ParticipantAccountDto) {
            updateEntity((ParticipantAccount) account, (ParticipantAccountDto) accountDto);
            return;
        }

        throw new NoMapperForOperationException(
            "There is no mapper for " + account.getClass().getName() + " and " + accountDto.getClass().getName() + "."
                + " Add a mapping to AccountMapper.update(Account account, AccountDto accountDto)."
        );
    }
    //endregion
}

