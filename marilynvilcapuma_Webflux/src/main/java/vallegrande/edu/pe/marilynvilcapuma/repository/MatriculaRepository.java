package vallegrande.edu.pe.marilynvilcapuma.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import vallegrande.edu.pe.marilynvilcapuma.model.Matricula;

@Repository
public interface MatriculaRepository extends ReactiveCrudRepository<Matricula, Long>{
    @Query("SELECT * FROM \"matricula\" WHERE persona_id = :personaId")
    Flux<Matricula> findByPersonaId(Long personaId);
}
