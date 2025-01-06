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

    public Transaction(LocalDateTime date, Double amount, Double amountInBankBefore, Double amountInBankAfter, BankAccount bankAccount) {
        this.date = date;
        this.amount = amount;
        this.amountInBankBefore = amountInBankBefore;
        this.amountInBankAfter = amountInBankAfter;
        this.bankAccount = bankAccount;
    }

    public Transaction(LocalDateTime date, Double amount, Double amountInBankBefore, Double amountInBankAfter,
                       BankAccount bankAccount, Contract contract, CounterParty counterParty, Category category) {
        this.date = date;
        this.amount = amount;
        this.amountInBankBefore = amountInBankBefore;
        this.amountInBankAfter = amountInBankAfter;
        this.bankAccount = bankAccount;
        this.contract = contract;
        this.counterParty = counterParty;
        this.category = category;
    }
}
