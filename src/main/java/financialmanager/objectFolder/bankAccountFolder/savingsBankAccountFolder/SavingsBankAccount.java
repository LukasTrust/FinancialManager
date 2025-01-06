package financialmanager.objectFolder.bankAccountFolder.savingsBankAccountFolder;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.Utils.JsonStringListConverter;
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
}