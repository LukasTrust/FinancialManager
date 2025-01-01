package financialmanager.accountFolder.savingsFolder;

import financialmanager.accountFolder.Account;
import financialmanager.accountFolder.JsonStringListConverter;
import financialmanager.userFolder.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
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

    public SavingsAccount(User user, Double interestRate) {
        super(user);
        this.interestRate = interestRate;
    }

    public SavingsAccount(User user, List<String> amountSearchStrings, List<String> dateSearchStrings,
                          List<String> counterPartySearchStrings, List<String> amountInBankAfterSearchStrings,
                          Double interestRate, List<String> interestRateSearchStrings) {
        super(user, amountSearchStrings, dateSearchStrings, counterPartySearchStrings, amountInBankAfterSearchStrings);
        this.interestRate = interestRate;
        this.interestRateSearchStrings = interestRateSearchStrings;
    }
}