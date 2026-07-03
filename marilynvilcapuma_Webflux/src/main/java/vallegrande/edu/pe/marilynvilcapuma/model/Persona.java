package vallegrande.edu.pe.marilynvilcapuma.model;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "persona")
public class Persona {
     @Id
    private Long id;
    private String first_name;
    private String last_name;
    private Integer dni;
    private Integer promotion;
    private LocalDateTime registration_date;
}
