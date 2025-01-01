package financialmanager.contractFolder.contractHistoryFolder;

import financialmanager.contractFolder.Contract;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ContractHistory")
public record ContractHistory(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Integer id,

        @OneToOne
        @JoinColumn(name = "contractId", nullable = false)
        Contract contract,

        @Column(nullable = false)
        Double previousAmount,

        @Column(nullable = false)
        Double newAmount,

        @Column(nullable = false)
        LocalDateTime changedAt
) { }
