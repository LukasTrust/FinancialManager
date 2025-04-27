package financialmanager.objectFolder.categoryFolder;

import financialmanager.objectFolder.counterPartyFolder.BaseCounterPartyService;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
import financialmanager.objectFolder.resultFolder.Err;
import financialmanager.objectFolder.resultFolder.Ok;
import financialmanager.objectFolder.resultFolder.Result;
import financialmanager.objectFolder.resultFolder.ResultService;
import financialmanager.objectFolder.usersFolder.Users;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Service
@AllArgsConstructor
public class CategoryService {

    private final BaseCategoryService baseCategoryService;
    private final BaseCounterPartyService baseCounterPartyService;
    private final ResultService resultService;
    private final ResponseService responseService;

    public ResponseEntity<Response> createCategory(CategoryBody categoryBody) {
        Result<Users, ResponseEntity<Response>> currentUserResult = resultService.getCurrentUser();

        if (currentUserResult.isErr())
            return currentUserResult.getError();

        Users currentUser = currentUserResult.getValue();
        String name = categoryBody.getName();

        Category category = new Category(currentUser, name);

        List<Long> counterPartyIds = categoryBody.getCounterParties().stream().map(Long::parseLong).toList();
        List<CounterParty> counterParties = baseCounterPartyService.findByIdInAndUsers(counterPartyIds, currentUser);
        category.setCounterParties(counterParties);

        category.setDescription(categoryBody.getDescription());
        category.setMaxSpendingPerMonth(categoryBody.getMaxSpendingPerMonth());

        baseCategoryService.save(category);

        return responseService.createResponseWithData(HttpStatus.CREATED, "categoryCreated", AlertType.SUCCESS, category);
    }

    public ResponseEntity<Response> updateContractField(Long categoryId,
                                                        Map<String, Object> requestBody,
                                                        BiConsumer<Category, Object> fieldUpdater) {
        Object newValue = requestBody.get("newValue");

        Result<Category, ResponseEntity<Response>> categoryResult = resultService.findCategoryById(categoryId);

        if (categoryResult.isErr())
            return categoryResult.getError();

        Category category = categoryResult.getValue();
        fieldUpdater.accept(category, newValue);

        baseCategoryService.save(category);

        return ResponseEntity.ok().build();
    }
}
