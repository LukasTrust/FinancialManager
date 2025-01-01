package financialmanager.transactionFolder;

import financialmanager.accountFolder.Account;
import financialmanager.categoryFolder.Category;
import financialmanager.contractFolder.Contract;
import financialmanager.counterPartyFolder.CounterParty;
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
    private Integer id;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Double amountInBankBefore;

    @Column(nullable = false)
    private Double amountInBankAfter;

    @ManyToOne
    @JoinColumn(name = "accountId", nullable = false)
    private Account account;

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

    public Transaction(LocalDateTime date, Double amount, Double amountInBankBefore, Double amountInBankAfter, Account account) {
        this.date = date;
        this.amount = amount;
        this.amountInBankBefore = amountInBankBefore;
        this.amountInBankAfter = amountInBankAfter;
        this.account = account;
    }

    public Transaction(LocalDateTime date, Double amount, Double amountInBankBefore, Double amountInBankAfter,
                       Account account, Contract contract, CounterParty counterParty, Category category) {
        this.date = date;
        this.amount = amount;
        this.amountInBankBefore = amountInBankBefore;
        this.amountInBankAfter = amountInBankAfter;
        this.account = account;
        this.contract = contract;
        this.counterParty = counterParty;
        this.category = category;
    }
}
