package financialmanager.objectFolder.categoryFolder;

import financialmanager.objectFolder.counterPartyFolder.BaseCounterPartyService;
import financialmanager.objectFolder.counterPartyFolder.CounterParty;
import financialmanager.objectFolder.responseFolder.AlertType;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.responseFolder.ResponseService;
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

    public ResponseEntity<Response> deleteCategories(List<Long> categoryIds) {
        Result<List<Category>, ResponseEntity<Response>> categoryResult = resultService.findCategoriesByIdInAndUsers(categoryIds);

        if (categoryResult.isErr())
            return categoryResult.getError();

        List<Category> categories = categoryResult.getValue();

        baseCategoryService.deleteAll(categories);

        return responseService.createResponse(HttpStatus.OK, "categoriesDeleted", AlertType.SUCCESS);
    }

    public ResponseEntity<Response> updateContractField(Long categoryId,
                                                        Map<String, Object> requestBody,
                                                        BiConsumer<Category, Object> fieldUpdater) {
        Object newValue = requestBody.get("newValue");

        Result<Category, ResponseEntity<Response>> categoryResult = resultService.findCategoryByIdAndUsers(categoryId);

        if (categoryResult.isErr())
            return categoryResult.getError();

        Category category = categoryResult.getValue();
        fieldUpdater.accept(category, newValue);

        baseCategoryService.save(category);

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Response> updateCounterPartiesField(Map<String, Long> requestBody,
                                                                boolean isAddOperation) {
        Long counterPartyId = requestBody.get("counterPartyId");
        Long categoryId = requestBody.get("categoryId");

        Result<Category, ResponseEntity<Response>> categoryResult = resultService.findCategoryByIdAndUsers(categoryId);

        if (categoryResult.isErr())
            return categoryResult.getError();

        Result<CounterParty, ResponseEntity<Response>> counterPartyResult = resultService.findCounterPartyById(counterPartyId);

        if (counterPartyResult.isErr())
            return counterPartyResult.getError();

        Category category = categoryResult.getValue();
        CounterParty counterParty = counterPartyResult.getValue();

        List<String> placeHolders = List.of(counterParty.getName(), category.getName());

        if (isAddOperation) {
            if (category.getCounterParties().contains(counterParty))
                return responseService.createResponseWithPlaceHolders(HttpStatus.BAD_REQUEST, "counterPartyAlreadyInCategory", AlertType.WARNING, placeHolders);

            category.getCounterParties().add(counterParty);
            baseCategoryService.save(category);
            return responseService.createResponseWithPlaceHolders(HttpStatus.OK, "counterPartyAddedToCategory", AlertType.SUCCESS, placeHolders);
        }

        if (!category.getCounterParties().contains(counterParty))
            return responseService.createResponseWithPlaceHolders(HttpStatus.BAD_REQUEST, "counterPartyNotInCategory", AlertType.WARNING, placeHolders);

        category.getCounterParties().remove(counterParty);
        baseCategoryService.save(category);
        return responseService.createResponseWithPlaceHolders(HttpStatus.OK, "counterPartyRemovedFromCategory", AlertType.SUCCESS, placeHolders);
    }
}
