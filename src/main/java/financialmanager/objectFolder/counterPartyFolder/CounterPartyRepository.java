package financialmanager.objectFolder.counterPartyFolder;

import financialmanager.objectFolder.usersFolder.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CounterPartyRepository extends JpaRepository<CounterParty, Long> {

    List<CounterParty> findByUsers(Users users);

    Optional<CounterParty> findByIdAndUsers(Long counterPartyId, Users users);

    List<CounterParty> findByIdInAndUsers(List<Long> counterPartyIds, Users users);
}
