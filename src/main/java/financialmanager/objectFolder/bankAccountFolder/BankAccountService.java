package financialmanager.objectFolder.bankAccountFolder;

import financialmanager.objectFolder.bankAccountFolder.savingsBankAccountFolder.SavingsBankAccount;
import financialmanager.objectFolder.resultFolder.Result;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.resultFolder.ResultService;
import financialmanager.objectFolder.usersFolder.Users;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
@AllArgsConstructor
public class BankAccountService {

    private final BaseBankAccountService baseBankAccountService;
    private final ResponseService responseService;
    private final ResultService resultService;

    public ResponseEntity<Response> createBankAccount(BankAccount bankAccount) {
        Result<Users, ResponseEntity<Response>> currentUserResponse = resultService.getCurrentUser();

        if (currentUserResponse.isErr()) {
            return currentUserResponse.getError();
        }

        Users currentUser = currentUserResponse.getValue();

        // Set the associated currentUser
        bankAccount.setUsers(currentUser);

        try {
            BankAccount savedBankAccount = baseBankAccountService.save(bankAccount);

            return responseService.createResponseWithData(HttpStatus.CREATED, "bankAccountCreated",
                    AlertType.SUCCESS, savedBankAccount);
        } catch (Exception e) {
            return responseService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "failedCreateBankAccount", AlertType.ERROR);
        }
    }

    public ResponseEntity<Response> removeSearchString(Long bankAccountId, String listType, String searchString) {
        return modifySearchList(bankAccountId, listType, searchString, false);
    }

    public ResponseEntity<Response> addSearchString(Long bankAccountId, String listType, String searchString) {
        return modifySearchList(bankAccountId, listType, searchString, true);
    }

    private ResponseEntity<Response> modifySearchList(Long bankAccountId, String listType, String searchString, boolean isAddOperation) {
        Result<BankAccount, ResponseEntity<Response>> bankAccountResult = resultService.findBankAccountById(bankAccountId);

        if (bankAccountResult.isErr())
            return bankAccountResult.getError();

        BankAccount bankAccount = bankAccountResult.getValue();
        boolean modified = updateSearchList(bankAccount, listType, searchString, isAddOperation);

        if (!modified)
            return responseService.createResponse(HttpStatus.BAD_REQUEST, "invalidListType", AlertType.ERROR);

        baseBankAccountService.save(bankAccount);

        return responseService.createResponse(HttpStatus.OK, isAddOperation ? "searchStringAdded" : "searchStringRemoved", AlertType.SUCCESS);
    }

    private boolean updateSearchList(BankAccount bankAccount, String listType, String searchString, boolean isAddOperation) {
        // Initialize lists if null
        if (bankAccount.getAmountSearchStrings() == null) bankAccount.setAmountSearchStrings(new ArrayList<>());
        if (bankAccount.getDateSearchStrings() == null) bankAccount.setDateSearchStrings(new ArrayList<>());
        if (bankAccount.getAmountInBankAfterSearchStrings() == null)
            bankAccount.setAmountInBankAfterSearchStrings(new ArrayList<>());
        if (bankAccount.getCounterPartySearchStrings() == null)
            bankAccount.setCounterPartySearchStrings(new ArrayList<>());

        Map<String, Consumer<String>> searchStringModifiers = Map.of(
                "amountSearchStrings", str -> modifyList(bankAccount.getAmountSearchStrings(), str, isAddOperation),
                "dateSearchStrings", str -> modifyList(bankAccount.getDateSearchStrings(), str, isAddOperation),
                "amountInBankAfterSearchStrings", str -> modifyList(bankAccount.getAmountInBankAfterSearchStrings(), str, isAddOperation),
                "counterPartySearchStrings", str -> modifyList(bankAccount.getCounterPartySearchStrings(), str, isAddOperation)
        );

        if (searchStringModifiers.containsKey(listType)) {
            searchStringModifiers.get(listType).accept(searchString);
            return true;
        }

        if ("interestRateSearchStrings".equals(listType) && bankAccount instanceof SavingsBankAccount savingsBankAccount) {
            modifyList(savingsBankAccount.getInterestRateSearchStrings(), searchString, isAddOperation);
            return true;
        }

        return false;
    }

    private void modifyList(List<String> searchList, String searchString, boolean isAddOperation) {
        if (isAddOperation) {
            searchList.add(searchString);
        } else {
            searchList.remove(searchString);
        }
    }
}
