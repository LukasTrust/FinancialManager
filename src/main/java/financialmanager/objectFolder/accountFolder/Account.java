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
    @Column(nullable = false)
    private String name;

    @Setter
    @Column
    private String description;

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

    public Account(Users users, String name) {
        this.users = users;
        this.name = name;
    }

    public Account(Users users, String name, String description, List<String> amountSearchStrings,
                   List<String> dateSearchStrings, List<String> counterPartySearchStrings,
                   List<String> amountInBankAfterSearchStrings) {
        this.users = users;
        this.name = name;
        this.description = description;
        this.amountSearchStrings = amountSearchStrings;
        this.dateSearchStrings = dateSearchStrings;
        this.counterPartySearchStrings = counterPartySearchStrings;
        this.amountInBankAfterSearchStrings = amountInBankAfterSearchStrings;
    }
}