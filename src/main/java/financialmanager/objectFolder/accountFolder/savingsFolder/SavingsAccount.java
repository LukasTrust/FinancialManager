package financialmanager.objectFolder.accountFolder.savingsFolder;

import financialmanager.objectFolder.accountFolder.Account;
import financialmanager.objectFolder.accountFolder.JsonStringListConverter;
import financialmanager.objectFolder.usersFolder.Users;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "SavingsAccount")
@Data
@NoArgsConstructor
public class SavingsAccount extends Account {

    @Column(nullable = false)
    private Double interestRate;

    @Convert(converter = JsonStringListConverter.class)
    private List<String> interestRateSearchStrings;

    public SavingsAccount(Users users, Double interestRate) {
        super(users);
        this.interestRate = interestRate;
    }

    public SavingsAccount(Users users, List<String> amountSearchStrings, List<String> dateSearchStrings,
                          List<String> counterPartySearchStrings, List<String> amountInBankAfterSearchStrings,
                          Double interestRate, List<String> interestRateSearchStrings) {
        super(users, amountSearchStrings, dateSearchStrings, counterPartySearchStrings, amountInBankAfterSearchStrings);
        this.interestRate = interestRate;
        this.interestRateSearchStrings = interestRateSearchStrings;
    }
}