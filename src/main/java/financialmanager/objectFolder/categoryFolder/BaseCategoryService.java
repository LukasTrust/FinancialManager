package financialmanager.objectFolder.categoryFolder;

import financialmanager.objectFolder.usersFolder.Users;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BaseCategoryService {

    private final CategoryRepository categoryRepository;

    public void save(Category category) {
        categoryRepository.save(category);
    }

    public void deleteAll(List<Category> categories) {
        categoryRepository.deleteAll(categories);
    }

    public Category findByIdAndUsers(Long id, Users user) {
        return categoryRepository.findByIdAndUsers(id, user);
    }

    public List<Category> findAllByUsers(Users user) {
        return categoryRepository.findByUsers(user);
    }
}
