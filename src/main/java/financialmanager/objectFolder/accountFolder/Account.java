package financialmanager.objectFolder.accountFolder;

import financialmanager.objectFolder.usersFolder.Users;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "Account")
@Data
@NoArgsConstructor
public abstract class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private Users users;

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

    public Account(Users users) {
        this.users = users;
    }

    public Account(Users users, List<String> amountSearchStrings, List<String> dateSearchStrings,
                   List<String> counterPartySearchStrings, List<String> amountInBankAfterSearchStrings) {
        this.users = users;
        this.amountSearchStrings = amountSearchStrings;
        this.dateSearchStrings = dateSearchStrings;
        this.counterPartySearchStrings = counterPartySearchStrings;
        this.amountInBankAfterSearchStrings = amountInBankAfterSearchStrings;
    }
}