package at.ac.tuwien.sepm.groupphase.backend.domain.account.persistence.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;


@Getter
@Setter
@Entity
@Table(name = "account_admin")
@DiscriminatorValue("admin")
public class AdminAccount extends Account {

    public static final int MAX_LENGTH_NAME = 100;

    @Column(nullable = false, length = MAX_LENGTH_NAME)
    private String name;
}
