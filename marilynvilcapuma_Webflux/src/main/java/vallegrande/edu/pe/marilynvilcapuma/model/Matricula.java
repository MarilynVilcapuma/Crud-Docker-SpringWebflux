package vallegrande.edu.pe.marilynvilcapuma.model;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "matricula")
public class Matricula {
    @Id
    private Long id;
    private Long persona_id;
    private String course;
    private String cycle;
    private BigDecimal amount;
    private String status;
    private LocalDateTime enrollment_date;
}
