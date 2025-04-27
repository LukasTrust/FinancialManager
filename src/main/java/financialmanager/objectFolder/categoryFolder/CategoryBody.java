package financialmanager.objectFolder.categoryFolder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryBody {

    private String name;
    private String description;
    private Double maxSpendingPerMonth;
    private List<String> counterParties;
}
