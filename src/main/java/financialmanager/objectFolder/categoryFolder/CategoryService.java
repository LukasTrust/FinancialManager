package financialmanager.objectFolder.categoryFolder;

import financialmanager.objectFolder.usersFolder.Users;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public void saveAll(List<Category> categories) {
        categoryRepository.saveAll(categories);
    }

    public List<Category> findAllByUsers(Users user) {
        return categoryRepository.findByUsers(user);
    }
}
