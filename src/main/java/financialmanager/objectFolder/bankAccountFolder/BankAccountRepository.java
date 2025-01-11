package financialmanager.objectFolder.bankAccountFolder;

import financialmanager.objectFolder.usersFolder.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findAllByUsers(Users users);

    Optional<BankAccount> findByIdAndUsers(Long id, Users users);
}