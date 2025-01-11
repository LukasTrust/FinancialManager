package financialmanager.objectFolder.transactionFolder;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.categoryFolder.Category;
import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Entity
@Data
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Double amountInBankBefore;

    @Column(nullable = false)
    private Double amountInBankAfter;

    @ManyToOne
    @JoinColumn(name = "bank_account_Id", nullable = false)
    private BankAccount bankAccount;

    @Setter
    @ManyToOne
    @JoinColumn(name = "contract_Id")
    private Contract contract;

    @Setter
    @ManyToOne
    @JoinColumn(name = "counter_Party_Id")
    private CounterParty counterParty;

    @Setter
    @ManyToOne
    @JoinColumn(name = "category_Id")
    private Category category;

    public Transaction(BankAccount bankAccount, CounterParty counterParty, LocalDate date, Double amount, Double amountInBankAfter,
                       Double amountInBankBefore) {
        this.bankAccount = bankAccount;
        this.counterParty = counterParty;
        this.date = date;
        this.amount = amount;
        this.amountInBankAfter = amountInBankAfter;
        this.amountInBankBefore = amountInBankBefore;
    }
}
