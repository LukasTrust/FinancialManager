package financialmanager.objectFolder.contractFolder;

import financialmanager.Utils.JsonStringListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
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

    @Column(nullable = false)
    private Double amount;

    @Setter
    @Column(nullable = false)
    private LocalDate lastPaymentDate;

    @Setter
    @Column(nullable = false)
    private LocalDate lastUpdatedAt;

    @Setter
    @Convert(converter = JsonStringListConverter.class)
    private List<String> contractSearchStrings;

    public Contract(String name, LocalDate startDate, LocalDate lastPaymentDate, Integer monthsBetweenPayments, Double amount) {
        this.name = name;
        this.startDate = startDate;
        this.lastUpdatedAt = startDate;
        this.lastPaymentDate = lastPaymentDate;
        this.monthsBetweenPayments = monthsBetweenPayments;
        this.amount = amount;
        this.contractSearchStrings = new ArrayList<>();
        this.contractSearchStrings.add(name);
    }
}
