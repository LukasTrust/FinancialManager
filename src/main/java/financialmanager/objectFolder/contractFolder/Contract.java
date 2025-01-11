package financialmanager.objectFolder.contractFolder;

import financialmanager.Utils.JsonStringListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Entity
@Data
@NoArgsConstructor
public class Contract {

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
    @Column(nullable = false)
    private LocalDate startDate;

    @Setter
    private LocalDate endDate;

    @Setter
    @Column(nullable = false)
    private Integer monthsBetweenPayments;

    @Setter
    @Column(nullable = false)
    private LocalDate lastUpdatedAt;

    @Setter
    @Convert(converter = JsonStringListConverter.class)
    private List<String> contractSearchStrings;
}
