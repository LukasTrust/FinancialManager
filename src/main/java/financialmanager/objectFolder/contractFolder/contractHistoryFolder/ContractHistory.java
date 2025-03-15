package financialmanager.objectFolder.contractFolder.contractHistoryFolder;

import financialmanager.objectFolder.contractFolder.Contract;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "ContractHistory")
@Getter
@NoArgsConstructor
public class ContractHistory {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "contractId", nullable = false)
        private Contract contract;

        @Column(nullable = false)
        private Double previousAmount;

        @Column(nullable = false)
        private Double newAmount;

        @Column(nullable = false)
        private LocalDate changedAt;

        public ContractHistory(Contract contract, Double newAmount, LocalDate changedAt) {
                this.contract = contract;
                this.previousAmount = contract.getAmount();
                this.newAmount = newAmount;
                this.changedAt = changedAt;
        }
}