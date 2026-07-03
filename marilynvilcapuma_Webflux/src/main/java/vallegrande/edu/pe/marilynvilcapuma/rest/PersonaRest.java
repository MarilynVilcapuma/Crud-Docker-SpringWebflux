package vallegrande.edu.pe.marilynvilcapuma.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.marilynvilcapuma.model.Persona;
import vallegrande.edu.pe.marilynvilcapuma.service.PersonaService;

@RestController
@RequestMapping("v1/api/persona")
@RequiredArgsConstructor
public class PersonaRest {
    private final PersonaService service;

    @GetMapping
    public Flux<Persona> findAll(){
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Persona>> findById(@PathVariable Long id){
        return service.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Persona> create(@RequestBody Persona persona) {
        return service.save(persona);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Persona>> update(@PathVariable Long id,@RequestBody Persona persona){
        return service.update(id, persona)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id){
        return service.deleteById(id);
    }

}
