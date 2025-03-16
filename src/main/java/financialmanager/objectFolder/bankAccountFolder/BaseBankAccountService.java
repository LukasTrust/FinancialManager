package financialmanager.objectFolder.bankAccountFolder;

import financialmanager.objectFolder.usersFolder.Users;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BaseBankAccountService {

    private final BankAccountRepository bankAccountRepository;

    public BankAccount save(BankAccount bankAccount) {
        return bankAccountRepository.save(bankAccount);
    }

    public List<BankAccount> findAllByUsers(Users users) {
        return bankAccountRepository.findAllByUsers(users);
    }

    public BankAccount findByIdAndUsers(Long bankAccountId, Users users) {
        return bankAccountRepository.findByIdAndUsers(bankAccountId, users).orElse(null);
    }
}
