package financialmanager.controller;

import financialmanager.objectFolder.categoryFolder.Category;
import financialmanager.objectFolder.categoryFolder.CategoryBody;
import financialmanager.objectFolder.categoryFolder.CategoryService;
import financialmanager.objectFolder.contractFolder.Contract;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.resultFolder.Result;
import financialmanager.objectFolder.resultFolder.ResultService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
@RequestMapping("/categories/data")
public class CategoryController {

    private final ResultService resultService;
    private final CategoryService categoryService;

    @GetMapping("")
    public ResponseEntity<?> getCategories() {
        Result<List<Category>, ResponseEntity<Response>> categoriesResult = resultService.findCategoriesByUsers();

        if (categoriesResult.isErr()) {
            return categoriesResult.getError();
        }

        return ResponseEntity.ok(categoriesResult.getValue());
    }

    @PostMapping("/addCategory")
    public ResponseEntity<?> addCategory(@RequestBody CategoryBody categoryBody) {
        return categoryService.createCategory(categoryBody);
    }

    @PostMapping("/{categoryId}/change/name")
    public ResponseEntity<Response> updateCategoryName(@PathVariable Long categoryId,
                                                       @RequestBody Map<String, Object> requestBody) {
        return categoryService.updateContractField(categoryId, requestBody,
                (category, value) -> category.setName((String) value));
    }

    @PostMapping("/{categoryId}/change/description")
    public ResponseEntity<Response> updateCategoryDescription(@PathVariable Long categoryId,
                                                              @RequestBody Map<String, Object> requestBody) {
        return categoryService.updateContractField(categoryId, requestBody,
                (category, value) -> category.setDescription((String) value));
    }

    @PostMapping("/{categoryId}/change/maxSpendingPerMonth")
    public ResponseEntity<Response> updateCategoryMaxSpendingPerMonth(@PathVariable Long categoryId,
                                                                      @RequestBody Map<String, Object> requestBody) {
        return categoryService.updateContractField(categoryId, requestBody,
                (category, value) -> category.setMaxSpendingPerMonth(Double.valueOf(value.toString())));
    }
}
