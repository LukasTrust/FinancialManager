package financialmanager.accountFolder.checkingFolder;

import financialmanager.accountFolder.Account;
import financialmanager.userFolder.User;
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

    public CheckingAccount(User user) {
        super(user);
    }

    public CheckingAccount(User user, List<String> amountSearchStrings, List<String> dateSearchStrings,
                           List<String> counterPartySearchStrings, List<String> amountInBankAfterSearchStrings) {
        super(user, amountSearchStrings, dateSearchStrings, counterPartySearchStrings, amountInBankAfterSearchStrings);
    }
}
