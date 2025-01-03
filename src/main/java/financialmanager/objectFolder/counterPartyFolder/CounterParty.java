package financialmanager.objectFolder.counterPartyFolder;

import financialmanager.objectFolder.accountFolder.JsonStringListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Entity
@Table(name = "CounterParty")
@Data
@NoArgsConstructor
public class CounterParty {

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
    @Convert(converter = JsonStringListConverter.class)
    private List<String> counterPartySearchStrings;

    public CounterParty(String name) {
        this.name = name;
    }

    public CounterParty(String name, String description, List<String> counterPartySearchStrings) {
        this.name = name;
        this.description = description;
        this.counterPartySearchStrings = counterPartySearchStrings;
    }
}
