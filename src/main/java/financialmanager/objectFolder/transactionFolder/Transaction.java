package financialmanager.objectFolder.transactionFolder;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Objects;

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

    @Column(nullable = false)
    private String originalCounterParty;

    @Setter
    @Column(nullable = false)
    private boolean isHidden;

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

    public Transaction(BankAccount bankAccount, String counterParty, LocalDate date, Double amount, Double amountInBankAfter,
                       Double amountInBankBefore) {
        this.bankAccount = bankAccount;
        this.originalCounterParty = counterParty;
        this.date = date;
        this.amount = amount;
        this.amountInBankAfter = amountInBankAfter;
        this.amountInBankBefore = amountInBankBefore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(date, that.date) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(originalCounterParty, that.originalCounterParty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, amount, originalCounterParty);
    }
}
