package financialmanager.controller;

import financialmanager.Utils.Result.Err;
import financialmanager.Utils.Result.Result;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import financialmanager.objectFolder.counterPartyFolder.CounterPartyDisplay;
import financialmanager.objectFolder.counterPartyFolder.CounterPartyProcessingService;
import financialmanager.objectFolder.counterPartyFolder.CounterPartyService;
import financialmanager.objectFolder.keyFigureFolder.KeyFigure;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.usersFolder.Users;
import financialmanager.objectFolder.usersFolder.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@AllArgsConstructor
public class CounterPartyController {

    private final CounterPartyProcessingService counterPartyProcessingService;
    private final UsersService usersService;
    private final CounterPartyService counterPartyService;
    private final ResponseService responseService;

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

    @PostMapping("/counterParty/data/{counterPartyId}/removeSearchString")
    public ResponseEntity<?> removeSearchStringFromCounterParty(
            @PathVariable("counterPartyId") Long counterPartyId,
            @RequestParam(value = "searchString") String searchString) {
        Result<Users, ResponseEntity<Response>> currentUserResponse = usersService.getCurrentUser();

        if (currentUserResponse.isErr()) {
            return currentUserResponse.getError();
        }

        Users currentUser = currentUserResponse.getValue();

        Result<CounterParty, ResponseEntity<Response>> counterPartyResult = counterPartyService.findByIdAndUsers(counterPartyId, currentUser);

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

        CounterParty splitCounterParty = counterPartyProcessingService.changeCounterPartyOfTransactions(currentUser, searchString);

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
