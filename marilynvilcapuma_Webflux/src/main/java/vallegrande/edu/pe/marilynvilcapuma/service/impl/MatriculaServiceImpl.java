package vallegrande.edu.pe.marilynvilcapuma.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.marilynvilcapuma.model.Matricula;
import vallegrande.edu.pe.marilynvilcapuma.repository.MatriculaRepository;
import vallegrande.edu.pe.marilynvilcapuma.repository.PersonaRepository;
import vallegrande.edu.pe.marilynvilcapuma.service.MatriculaService;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatriculaServiceImpl implements MatriculaService {

    private final MatriculaRepository repository;
    private final PersonaRepository personaRepository;

    @Override
    public Flux<Matricula> findAll() {
        log.info("Invocar - Listar Matriculas");
        return repository.findAll();
    }

    @Override
    public Mono<Matricula> findById(Long id) {
        log.info("Invocar - Buscando matricula por id={}", id);
        return repository.findById(id);
    }

    @Override
    public Flux<Matricula> findByPersonaId(Long personaId) {
        log.info("Invocar - Listar matriculas de la persona id={}", personaId);
        return repository.findByPersonaId(personaId);
    }

    @Override
    public Mono<Matricula> save(Matricula matricula) {
        return personaRepository.findById(matricula.getPersona_id())
                .flatMap(persona -> {
                    matricula.setEnrollment_date(LocalDateTime.now());
                    if (matricula.getStatus() == null) {
                        matricula.setStatus("A");
                    }
                    return repository.save(matricula);
                })
                .doOnNext(m -> log.info("Registrar - Matricula creada personaId={}, course={}, amount={}", m.getPersona_id(), m.getCourse(), m.getAmount()));
    }

    @Override
    public Mono<Matricula> update(Long id, Matricula matricula) {
        return repository.findById(id)
                .flatMap(existing -> {
                    existing.setCourse(matricula.getCourse());
                    existing.setCycle(matricula.getCycle());
                    existing.setAmount(matricula.getAmount());
                    existing.setStatus(matricula.getStatus());
                    return repository.save(existing);

                })
                .doOnNext(m -> log.info("Actualizar - Matricula actualizada id={}, course={}, status={}", m.getId(), m.getCourse(), m.getStatus()));
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return repository.deleteById(id)
                .doOnSuccess(v -> log.info("Eliminar - Matricula id={} eliminada", id));
    }
}
