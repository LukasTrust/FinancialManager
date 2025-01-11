package financialmanager.objectFolder.bankAccountFolder;

import financialmanager.objectFolder.usersFolder.Users;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;

    public BankAccount save(BankAccount bankAccount) {
        return bankAccountRepository.save(bankAccount);
    }

    public List<BankAccount> findAllByUsers(Users users){
        return bankAccountRepository.findAllByUsers(users);
    }

    public boolean checkIfBankAccountBelongsToUser(Long bankAccountId, Users users){
        Optional<BankAccount> bankAccount = findByIdAndUsers(bankAccountId, users);

        return bankAccount.isPresent();
    }

    public Optional<BankAccount> findByIdAndUsers(Long id, Users users){
        return bankAccountRepository.findByIdAndUsers(id, users);
    }
}
