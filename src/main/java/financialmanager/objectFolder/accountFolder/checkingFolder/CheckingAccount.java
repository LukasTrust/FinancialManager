package financialmanager.objectFolder.accountFolder.checkingFolder;

import financialmanager.objectFolder.accountFolder.Account;
import financialmanager.objectFolder.usersFolder.Users;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "CheckingAccount")
@Data
@NoArgsConstructor
public class CheckingAccount extends Account {

    public CheckingAccount(Users users) {
        super(users);
    }

    public CheckingAccount(Users users, List<String> amountSearchStrings, List<String> dateSearchStrings,
                           List<String> counterPartySearchStrings, List<String> amountInBankAfterSearchStrings) {
        super(users, amountSearchStrings, dateSearchStrings, counterPartySearchStrings, amountInBankAfterSearchStrings);
    }
}
