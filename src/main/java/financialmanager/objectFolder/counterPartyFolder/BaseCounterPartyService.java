package financialmanager.objectFolder.counterPartyFolder;

import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.usersFolder.Users;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BaseCounterPartyService {

    private final CounterPartyRepository counterPartyRepository;

    public List<CounterParty> findByUsers(Users user) {
        return counterPartyRepository.findByUsers(user);
    }

    public List<CounterParty> findByIdInAndUsers(List<Long> counterPartyIds, Users user) {
        return counterPartyRepository.findByIdInAndUsers(counterPartyIds, user);
    }

    public CounterParty findByIdAndUsers(Long counterPartyId, Users user) {
        return counterPartyRepository.findByIdAndUsers(counterPartyId, user).orElse(null);
    }

    public boolean existsByCounterPartySearchStringsContaining(String searchString) {
        return !counterPartyRepository.findByCounterPartySearchStringsContaining(searchString).isEmpty();
    }

    public void saveAll(List<CounterParty> counterParties) {
        counterPartyRepository.saveAll(counterParties);
    }

    public void save(CounterParty counterParty) {
        counterPartyRepository.save(counterParty);
    }

    public void deleteAll(List<CounterParty> counterParties) {
        counterPartyRepository.deleteAll(counterParties);
    }

    public void setHidden(boolean hide, List<CounterParty> counterParties) {
        counterParties.forEach(counterParty -> counterParty.setHidden(hide));
        saveAll(counterParties);
    }
}
