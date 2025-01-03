package financialmanager.objectFolder.categoryFolder;

import financialmanager.objectFolder.accountFolder.JsonStringListConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Entity
@Table(name = "Category")
@Data
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column
    private String description;

    @Setter
    @Column
    private Double maxSpendingPerMonth;

    @Setter
    @Convert(converter = JsonStringListConverter.class)
    private List<String> counterPartySearchStrings;

    public Category(String name) {
        this.name = name;
    }

    public Category(String name, String description, Double maxSpendingPerMonth, List<String> counterPartySearchStrings) {
        this.name = name;
        this.description = description;
        this.maxSpendingPerMonth = maxSpendingPerMonth;
        this.counterPartySearchStrings = counterPartySearchStrings;
    }
}
