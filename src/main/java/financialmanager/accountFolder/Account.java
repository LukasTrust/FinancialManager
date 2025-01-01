package financialmanager.accountFolder;

import financialmanager.userFolder.User;
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
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

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

    public Account(User user) {
        this.user = user;
    }

    public Account(User user, List<String> amountSearchStrings, List<String> dateSearchStrings,
                   List<String> counterPartySearchStrings, List<String> amountInBankAfterSearchStrings) {
        this.user = user;
        this.amountSearchStrings = amountSearchStrings;
        this.dateSearchStrings = dateSearchStrings;
        this.counterPartySearchStrings = counterPartySearchStrings;
        this.amountInBankAfterSearchStrings = amountInBankAfterSearchStrings;
    }
}