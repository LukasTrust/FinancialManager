package financialmanager.objectFolder.contractFolder;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
    @Column(nullable = false)
    private boolean isHidden;

    @ManyToOne
    @JoinColumn(name = "bank_account_Id", nullable = false)
    private BankAccount bankAccount;

    @Setter
    @ManyToOne
    @JoinColumn(name = "counter_party_id")
    private CounterParty counterParty;

    public Contract(LocalDate startDate, LocalDate lastPaymentDate, Integer monthsBetweenPayments, Double amount,
                    CounterParty counterParty, BankAccount bankAccount) {
        this.name = counterParty.getName();
        this.startDate = startDate;
        this.lastUpdatedAt = startDate;
        this.lastPaymentDate = lastPaymentDate;
        this.monthsBetweenPayments = monthsBetweenPayments;
        this.amount = amount;
        this.counterParty = counterParty;
        this.bankAccount = bankAccount;
    }
}
