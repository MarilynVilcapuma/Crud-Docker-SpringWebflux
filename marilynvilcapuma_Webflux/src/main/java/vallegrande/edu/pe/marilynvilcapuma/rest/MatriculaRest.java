package vallegrande.edu.pe.marilynvilcapuma.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.marilynvilcapuma.model.Matricula;
import vallegrande.edu.pe.marilynvilcapuma.service.MatriculaService;

@RestController
@RequestMapping("v1/api/matricula")
@RequiredArgsConstructor
public class MatriculaRest {
    private final MatriculaService service;

    @GetMapping
    public Flux<Matricula> findAll(){
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Matricula>> findById(@PathVariable Long id){
        return service.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/persona/{personaId}")
    public Flux<Matricula> findByPersonaId(@PathVariable Long personaId){
        return service.findByPersonaId(personaId);
    }

    @PostMapping
    public Mono<ResponseEntity<Matricula>> create(@RequestBody Matricula matricula) {
        return service.save(matricula)
                .map(m -> ResponseEntity.status(HttpStatus.CREATED).body(m))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Matricula>> update(@PathVariable Long id,@RequestBody Matricula matricula){
        return service.update(id, matricula)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id){
        return service.deleteById(id);
    }

}
