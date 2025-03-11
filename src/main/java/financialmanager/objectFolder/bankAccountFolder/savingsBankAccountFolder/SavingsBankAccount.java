package financialmanager.objectFolder.bankAccountFolder.savingsBankAccountFolder;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.Utils.JsonStringListConverter;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

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

    @Type(JsonBinaryType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "interest_rate_search_strings", columnDefinition = "jsonb")
    private List<String> interestRateSearchStrings;
}