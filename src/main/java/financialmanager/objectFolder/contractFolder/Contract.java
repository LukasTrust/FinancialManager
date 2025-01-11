package financialmanager.objectFolder.contractFolder;

import financialmanager.Utils.JsonStringListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Entity
@Table(name = "Contract")
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

    public Contract(String name, LocalDate startDate, Integer monthsBetweenPayments) {
        this.name = name;
        this.startDate = startDate;
        this.monthsBetweenPayments = monthsBetweenPayments;
        this.lastUpdatedAt = startDate;
    }

    public Contract(String name, String description, LocalDate startDate, LocalDate endDate,
                    Integer monthsBetweenPayments, LocalDate lastUpdatedAt, List<String> contractSearchStrings) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.monthsBetweenPayments = monthsBetweenPayments;
        this.lastUpdatedAt = lastUpdatedAt;
        this.contractSearchStrings = contractSearchStrings;
    }
}
