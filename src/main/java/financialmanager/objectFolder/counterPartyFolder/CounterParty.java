package financialmanager.objectFolder.counterPartyFolder;

import financialmanager.objectFolder.usersFolder.Users;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Entity
@Data
@NoArgsConstructor
public class CounterParty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users users;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column
    private String description;

    @Setter
    @Column(nullable = false)
    private boolean isHidden;

    @Type(JsonBinaryType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "counter_party_search_strings", columnDefinition = "jsonb")
    private List<String> counterPartySearchStrings;

    public CounterParty(Users users, String name) {
        this.users = users;
        this.name = name;
        this.counterPartySearchStrings = new ArrayList<>();
        counterPartySearchStrings.add(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CounterParty that = (CounterParty) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
