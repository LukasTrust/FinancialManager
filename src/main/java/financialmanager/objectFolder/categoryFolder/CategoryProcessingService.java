package financialmanager.objectFolder.categoryFolder;

import financialmanager.objectFolder.transactionFolder.Transaction;
import financialmanager.objectFolder.usersFolder.Users;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class CategoryProcessingService {

    private final CategoryService categoryService;

    public void addTransactionsToCategories(Users user, List<Transaction> transactions) {
        List<Category> categories = categoryService.findAllByUsers(user);

        // Create map of counterparty search strings to categories for fast lookup
        Map<String, Category> searchStringToCategoryMap = new HashMap<>();
        for (Category category : categories) {
            for (String searchString : category.getCounterPartySearchStrings()) {
                searchStringToCategoryMap.put(searchString, category);
            }
        }

        // Loop through the transactions and assign the appropriate category
        for (Transaction transaction : transactions) {
            Category matchedCategory = searchStringToCategoryMap.get(transaction.getOriginalCounterParty());
            if (matchedCategory != null) {
                transaction.setCategory(matchedCategory);
            }
        }
    }
}
