package financialmanager.objectFolder.bankAccountFolder.savingsAccountFolder;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.Utils.JsonStringListConverter;
import financialmanager.objectFolder.usersFolder.Users;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
public class SavingsBankAccount extends BankAccount {

    @Column(nullable = false)
    private Double interestRate;

    @Convert(converter = JsonStringListConverter.class)
    private List<String> interestRateSearchStrings;

    public SavingsBankAccount(Users users, String name, Double interestRate) {
        super(users, name);
        this.interestRate = interestRate;
    }

    public SavingsBankAccount(Users users, String name, String description, List<String> amountSearchStrings, List<String> dateSearchStrings,
                              List<String> counterPartySearchStrings, List<String> amountInBankAfterSearchStrings,
                              Double interestRate, List<String> interestRateSearchStrings) {
        super(users, name, description, amountSearchStrings, dateSearchStrings, counterPartySearchStrings, amountInBankAfterSearchStrings);
        this.interestRate = interestRate;
        this.interestRateSearchStrings = interestRateSearchStrings;
    }
}