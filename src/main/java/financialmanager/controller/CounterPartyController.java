package financialmanager.controller;

import financialmanager.Utils.Result.Err;
import financialmanager.Utils.Result.Result;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import financialmanager.objectFolder.counterPartyFolder.CounterPartyDisplay;
import financialmanager.objectFolder.counterPartyFolder.CounterPartyProcessingService;
import financialmanager.objectFolder.counterPartyFolder.CounterPartyService;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/counterParty/data")
public class CounterPartyController {

    private final CounterPartyProcessingService counterPartyProcessingService;
    private final UsersService usersService;
    private final CounterPartyService counterPartyService;
    private final ResponseService responseService;

    @GetMapping("")
    public ResponseEntity<?> getCounterPartyDisplays() {
        Result<Users, ResponseEntity<Response>> currentUserResponse = usersService.getCurrentUser();

        if (currentUserResponse.isErr()) {
            return currentUserResponse.getError();
        }

        Users currentUser = currentUserResponse.getValue();

        List<CounterPartyDisplay> counterPartyDisplays = counterPartyProcessingService.createCounterPartyDisplays(currentUser);

        return ResponseEntity.ok(counterPartyDisplays);
    }

    private Result<CounterParty, ResponseEntity<Response>> getCounterParty(@PathVariable Long counterPartyId) {
        Result<Users, ResponseEntity<Response>> currentUserResponse = usersService.getCurrentUser();

        if (currentUserResponse.isErr()) {
            return new Err<>(currentUserResponse.getError());
        }

        Users currentUser = currentUserResponse.getValue();

        return counterPartyService.findByIdAndUsers(counterPartyId, currentUser);
    }

    @PostMapping("/{counterPartyId}/change/name/{newValue}")
    public ResponseEntity<?> nameOfCounterPartyChanged(@PathVariable("counterPartyId") Long counterPartyId,
                                                       @PathVariable("newValue") String newValue) {
        Result<CounterParty, ResponseEntity<Response>> counterPartyResult = getCounterParty(counterPartyId);

        if (counterPartyResult.isErr()) {
            return counterPartyResult.getError();
        }

        CounterParty counterParty = counterPartyResult.getValue();
        counterParty.setName(newValue);

        counterPartyService.save(counterParty);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{counterPartyId}/change/description/{newValue}")
    public ResponseEntity<?> descriptionOfCounterPartyChanged(@PathVariable("counterPartyId") Long counterPartyId,
                                                       @PathVariable("newValue") String newValue) {
        Result<CounterParty, ResponseEntity<Response>> counterPartyResult = getCounterParty(counterPartyId);

        if (counterPartyResult.isErr()) {
            return counterPartyResult.getError();
        }

        CounterParty counterParty = counterPartyResult.getValue();
        counterParty.setDescription(newValue);

        counterPartyService.save(counterParty);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{counterPartyId}/removeSearchString")
    public ResponseEntity<?> removeSearchStringFromCounterParty(
            @PathVariable("counterPartyId") Long counterPartyId,
            @RequestParam(value = "searchString") String searchString) {
        Result<CounterParty, ResponseEntity<Response>> counterPartyResult = getCounterParty(counterPartyId);

        if (counterPartyResult.isErr()) {
            return counterPartyResult.getError();
        }

        CounterParty counterParty = counterPartyResult.getValue();
        List<String> searchStrings = counterParty.getCounterPartySearchStrings();

        // Ensure search string removal is allowed
        if (searchStrings.size() == 1) {
            return responseService.createResponse(
                    HttpStatus.BAD_REQUEST, "searchStringCanNotBeRemovedFromCounterParty", AlertType.ERROR);
        }

        // Attempt to remove the search string
        if (!searchStrings.remove(searchString)) {
            return responseService.createResponseWithPlaceHolders(
                    HttpStatus.NOT_FOUND, "searchStringNotFoundInCounterParty", AlertType.ERROR, List.of(searchString));
        }

        CounterParty splitCounterParty = counterPartyProcessingService.changeCounterPartyOfTransactions(counterParty.getUsers(), searchString);

        if (splitCounterParty == null) {
            // Return success response without new counterParty
            return responseService.createResponse(
                    HttpStatus.OK, "removedSearchStringFromCounterParty", AlertType.SUCCESS);
        }

        // Return success response with new counterParty
        return responseService.createResponseWithData(
                HttpStatus.OK, "removedSearchStringFromCounterParty", AlertType.SUCCESS, splitCounterParty);
    }
}
