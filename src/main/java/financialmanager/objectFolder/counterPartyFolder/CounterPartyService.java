package financialmanager.objectFolder.counterPartyFolder;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CounterPartyService {

    private final CounterPartyRepository counterPartyRepository;

    public CounterParty save(CounterParty counterParty) {
        return counterPartyRepository.save(counterParty);
    }
}
