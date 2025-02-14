package financialmanager.objectFolder.bankAccountFolder;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import financialmanager.Utils.JsonStringListConverter;
import financialmanager.objectFolder.bankAccountFolder.savingsBankAccountFolder.SavingsBankAccount;
import financialmanager.objectFolder.usersFolder.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SavingsBankAccount.class, name = "saving"),
        @JsonSubTypes.Type(value = BankAccount.class, name = "checking"),
})
@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users users;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column
    private String description;

    @Setter
    @Column(nullable = false)
    private String currencySymbol;

    @Setter
    @Convert(converter = JsonStringListConverter.class)
    private List<String> amountSearchStrings;

    @Setter
    @Convert(converter = JsonStringListConverter.class)
    private List<String> dateSearchStrings;

    @Setter
    @Convert(converter = JsonStringListConverter.class)
    private List<String> counterPartySearchStrings;

    @Setter
    @Convert(converter = JsonStringListConverter.class)
    private List<String> amountInBankAfterSearchStrings;
}