package financialmanager.objectFolder.categoryFolder;

import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import financialmanager.objectFolder.usersFolder.Users;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Entity
@Data
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    @Column
    private Double maxSpendingPerMonth;

    @ManyToMany
    @JoinTable(
            name = "category_counterparty",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "counterparty_id")
    )
    private List<CounterParty> counterParties;
}
