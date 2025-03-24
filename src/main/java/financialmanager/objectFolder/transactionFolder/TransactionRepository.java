package financialmanager.objectFolder.transactionFolder;

import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByBankAccount(BankAccount bankAccount);

    List<Transaction> findByBankAccountAndContractEmpty(BankAccount bankAccount);

    List<Transaction> findByIdInAndBankAccount(List<Long> ids, BankAccount bankAccount);

    List<Transaction> findByCounterParty(CounterParty counterParty);

    List<Transaction> findByCounterPartyIn(List<CounterParty> counterParties);

    List<Transaction> findByOriginalCounterParty(String originalCounterParty);

    List<Transaction> findByContract(Contract contract);

    List<Transaction> findByContractIn(List<Contract> contracts);
}
