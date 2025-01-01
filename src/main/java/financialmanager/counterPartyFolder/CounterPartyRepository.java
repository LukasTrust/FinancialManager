package financialmanager.counterPartyFolder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CounterPartyRepository extends JpaRepository<CounterParty, Integer> {
}
