package financialmanager.controller;

import financialmanager.Utils.Result.Result;
import financialmanager.objectFolder.counterPartyFolder.CounterPartyDisplay;
import financialmanager.objectFolder.counterPartyFolder.CounterPartyProcessingService;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@AllArgsConstructor
public class CounterPartyController {

    private final CounterPartyProcessingService counterPartyProcessingService;
    private final UsersService usersService;

    @GetMapping("/counterParty/data")
    public ResponseEntity<?> getCounterPartyDisplays() {
        Result<Users, ResponseEntity<Response>> currentUserResponse = usersService.getCurrentUser();

        if (currentUserResponse.isErr()) {
            return currentUserResponse.getError();
        }

        Users currentUser = currentUserResponse.getValue();

        List<CounterPartyDisplay> counterPartyDisplays = counterPartyProcessingService.createCounterPartyDisplays(currentUser);

        return ResponseEntity.ok(counterPartyDisplays);
    }
}
