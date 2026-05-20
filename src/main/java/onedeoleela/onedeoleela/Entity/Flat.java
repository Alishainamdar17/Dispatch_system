

package onedeoleela.onedeoleela.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Flat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flatId;

    private String flatNumber;

    @ManyToOne
    @JoinColumn(name = "floor_id")
    @JsonIgnore
    private Floor floor;
}