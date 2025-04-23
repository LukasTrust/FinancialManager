package financialmanager.controller;

import financialmanager.objectFolder.categoryFolder.Category;
import financialmanager.objectFolder.responseFolder.Response;
import financialmanager.objectFolder.resultFolder.Result;
import financialmanager.objectFolder.resultFolder.ResultService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/categories/data")
public class CategoryController {

    private final ResultService resultService;

    @GetMapping("")
    public ResponseEntity<?> getCategories() {
        Result<List<Category>, ResponseEntity<Response>> categoriesResult = resultService.findCategoriesByUsers();

        if (categoriesResult.isErr()) {
            return categoriesResult.getError();
        }

        return ResponseEntity.ok(categoriesResult.getValue());
    }
}
