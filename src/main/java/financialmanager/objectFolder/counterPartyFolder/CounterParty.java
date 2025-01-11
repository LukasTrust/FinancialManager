package financialmanager.objectFolder.counterPartyFolder;

import financialmanager.Utils.JsonStringListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
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
        this.counterPartySearchStrings = new ArrayList<>();
        counterPartySearchStrings.add(name);
    }
}
