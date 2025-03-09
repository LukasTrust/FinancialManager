package financialmanager.objectFolder.counterPartyFolder;

import financialmanager.objectFolder.usersFolder.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CounterPartyRepository extends JpaRepository<CounterParty, Long> {

    List<CounterParty> findByUsers(Users users);

    Optional<CounterParty> findByIdAndUsers(Long counterPartyId, Users users);

    List<CounterParty> findByIdInAndUsers(List<Long> counterPartyIds, Users users);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM counter_party c WHERE c.counter_party_search_strings @> to_jsonb(:searchString::text))", nativeQuery = true)
    boolean existsByCounterPartySearchStringsContaining(@Param("searchString") String searchString);
}
