package vallegrande.edu.pe.marilynvilcapuma.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import vallegrande.edu.pe.marilynvilcapuma.model.Persona;

@Repository
public interface PersonaRepository extends ReactiveCrudRepository<Persona, Long>{
}
