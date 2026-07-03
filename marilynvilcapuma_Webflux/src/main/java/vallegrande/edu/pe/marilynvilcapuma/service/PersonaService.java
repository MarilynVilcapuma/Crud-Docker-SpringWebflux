package vallegrande.edu.pe.marilynvilcapuma.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.marilynvilcapuma.model.Persona;

public interface PersonaService {
    Flux<Persona> findAll();
    Mono<Persona> findById(Long id);
    Mono<Persona> save(Persona persona);
    Mono<Persona> update(Long id,Persona persona);
    Mono<Void> deleteById (Long id);

}
 