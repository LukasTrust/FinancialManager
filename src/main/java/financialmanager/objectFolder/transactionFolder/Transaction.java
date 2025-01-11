package financialmanager.objectFolder.transactionFolder;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.categoryFolder.Category;
import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "Transaction")
@Data
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Double amountInBankBefore;

    @Column(nullable = false)
    private Double amountInBankAfter;

    @ManyToOne
    @JoinColumn(name = "bankaccountId", nullable = false)
    private BankAccount bankAccount;

    @Setter
    @ManyToOne
    @JoinColumn(name = "contractId")
    private Contract contract;

    @Setter
    @ManyToOne
    @JoinColumn(name = "counterPartyId")
    private CounterParty counterParty;

    @Setter
    @ManyToOne
    @JoinColumn(name = "categoryId")
    private Category category;
}
