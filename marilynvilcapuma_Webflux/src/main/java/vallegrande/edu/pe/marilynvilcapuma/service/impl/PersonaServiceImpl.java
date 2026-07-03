package vallegrande.edu.pe.marilynvilcapuma.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.marilynvilcapuma.model.Persona;
import vallegrande.edu.pe.marilynvilcapuma.repository.PersonaRepository;
import vallegrande.edu.pe.marilynvilcapuma.service.PersonaService;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonaServiceImpl implements PersonaService {

    private final PersonaRepository repository;
    @Override
    public Flux<Persona> findAll() {
        log.info("Invocar - Listar Personas");
        return repository.findAll();
    }

    @Override
    public Mono<Persona> findById(Long id) {
        log.info("Invovar - Buscando persona por id={}, ", id);
        return repository.findById(id);
    }

    @Override
    public Mono<Persona> save(Persona persona) {
        persona.setRegistration_date(LocalDateTime.now());
        return repository.save(persona)
        .doOnSuccess(p -> log.info("Registrar - Persona creada firstName={}, lastName={}, dni={}", p.getFirst_name(), p.getLast_name(), p.getDni()));
    }

    @Override
    public Mono<Persona> update(Long id, Persona persona) {
        return repository.findById(id)
                .flatMap( existing ->{
                    existing.setFirst_name(persona.getFirst_name());
                    existing.setLast_name(persona.getLast_name());
                    existing.setDni(persona.getDni());
                    existing.setPromotion(persona.getPromotion());
                    existing.setRegistration_date(LocalDateTime.now());
                    return repository.save(existing);

                })
                .doOnSuccess(p -> log.info("Actualizar - Persona actualizada firstName={}, lastName={}, dni={}", p.getFirst_name(), p.getLast_name(), p.getDni()));
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return repository.deleteById(id)
                .doOnSuccess(v -> log.info(" Eliminar - Persona  id={} eliminada " , id));
    }
}
