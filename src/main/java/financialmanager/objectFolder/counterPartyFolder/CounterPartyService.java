package financialmanager.objectFolder.counterPartyFolder;

import financialmanager.Utils.Result.Err;
import financialmanager.Utils.Result.Ok;
import financialmanager.Utils.Result.Result;
import financialmanager.objectFolder.bankAccountFolder.BankAccount;
import financialmanager.objectFolder.bankAccountFolder.BankAccountService;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class CounterPartyService {

    private final CounterPartyRepository counterPartyRepository;
    private final ResponseService responseService;

    private static final Logger log = LoggerFactory.getLogger(CounterPartyService.class);

    public void saveAll(List<CounterParty> counterParties) {
        counterPartyRepository.saveAll(counterParties);
    }

    public void save(CounterParty counterParty) {
        counterPartyRepository.save(counterParty);
    }

    public List<CounterParty> findByUsers(Users user) {
        return counterPartyRepository.findByUsers(user);
    }

    public Result<CounterParty, ResponseEntity<Response>> findByIdAndUsers(Long counterPartyId, Users currentUser) {
        Optional<CounterParty> counterPartyOptional = counterPartyRepository.findByIdAndUsers(counterPartyId, currentUser);

        if (counterPartyOptional.isPresent())  {
            return new Ok<>(counterPartyOptional.get());
        }

        log.warn("User {} does not own the bank account {}", currentUser, counterPartyId);

        return new Err<>(responseService.createResponse(HttpStatus.NOT_FOUND, "counterPartyNotFound", AlertType.ERROR));
    }
}
