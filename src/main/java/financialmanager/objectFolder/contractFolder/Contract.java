package financialmanager.objectFolder.contractFolder;

import financialmanager.objectFolder.accountFolder.JsonStringListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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
    private LocalDateTime startDate;

    @Setter
    private LocalDateTime endDate;

    @Setter
    @Column(nullable = false)
    private Integer monthsBetweenPayments;

    @Setter
    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Setter
    @Convert(converter = JsonStringListConverter.class)
    private List<String> contractSearchStrings;

    public Contract(String name, LocalDateTime startDate, Integer monthsBetweenPayments) {
        this.name = name;
        this.startDate = startDate;
        this.monthsBetweenPayments = monthsBetweenPayments;
        this.lastUpdatedAt = startDate;
    }

    public Contract(String name, String description, LocalDateTime startDate, LocalDateTime endDate,
                    Integer monthsBetweenPayments, LocalDateTime lastUpdatedAt, List<String> contractSearchStrings) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.monthsBetweenPayments = monthsBetweenPayments;
        this.lastUpdatedAt = lastUpdatedAt;
        this.contractSearchStrings = contractSearchStrings;
    }
}
