package financialmanager.objectFolder.contractFolder.contractHistoryFolder;

import financialmanager.objectFolder.contractFolder.Contract;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "ContractHistory")
public record ContractHistory(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Long id,

        @OneToOne
        @JoinColumn(name = "contractId", nullable = false)
        Contract contract,

        @Column(nullable = false)
        Double previousAmount,

        @Column(nullable = false)
        Double newAmount,

        @Column(nullable = false)
        LocalDate changedAt
) { }
