package financialmanager.objectFolder.counterPartyFolder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CounterPartyRepository extends JpaRepository<CounterParty, Long> {
}
