package financialmanager.objectFolder.categoryFolder;

import financialmanager.objectFolder.usersFolder.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>  {

    List<Category> findByUsers(Users users);

    Category findByIdAndUsers(Long id, Users users);
}
