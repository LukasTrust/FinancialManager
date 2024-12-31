package financialmanager.accountFolder;

import financialmanager.userFolder.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@MappedSuperclass
@Table(name = "Account")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Convert(converter = JsonStringListConverter.class)
    private List<String> amountSearchStrings;

    @Convert(converter = JsonStringListConverter.class)
    private List<String> dateSearchStrings;

    @Convert(converter = JsonStringListConverter.class)
    private List<String> counterPartySearchStrings;

    @Convert(converter = JsonStringListConverter.class)
    private List<String> amountInBankAfterSearchStrings;

    public Account(User user, List<String> amountSearchStrings, List<String> dateSearchStrings,
                   List<String> counterPartySearchStrings, List<String> amountInBankAfterSearchStrings) {
        this.user = user;
        this.amountSearchStrings = amountSearchStrings;
        this.dateSearchStrings = dateSearchStrings;
        this.counterPartySearchStrings = counterPartySearchStrings;
        this.amountInBankAfterSearchStrings = amountInBankAfterSearchStrings;
    }
}