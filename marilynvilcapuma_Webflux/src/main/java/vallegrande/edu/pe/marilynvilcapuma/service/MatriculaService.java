package vallegrande.edu.pe.marilynvilcapuma.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.marilynvilcapuma.model.Matricula;

public interface MatriculaService {
    Flux<Matricula> findAll();
    Mono<Matricula> findById(Long id);
    Flux<Matricula> findByPersonaId(Long personaId);
    Mono<Matricula> save(Matricula matricula);
    Mono<Matricula> update(Long id, Matricula matricula);
    Mono<Void> deleteById(Long id);

}
