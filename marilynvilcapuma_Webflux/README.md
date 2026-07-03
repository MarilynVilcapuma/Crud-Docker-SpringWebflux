# Microservicio Persona + Matrícula — Spring Boot WebFlux

Microservicio **reactivo** con un CRUD maestro (**Persona**) y un CRUD
transaccional (**Matrícula**, ligada por FK) sobre una base de datos
**H2 en memoria**, empaquetado en una imagen Docker ultra liviana (**72 MB**, base
`scratch`), desplegado con Docker Compose detrás de un proxy **nginx** y también
en **Kubernetes** (manifiestos en [k8s/](k8s/)).

**Autora:** Marilyn Vilcapuma Trujillo — Valle Grande

---

## 1. Tecnologías

| Tecnología | Versión | Rol |
|---|---|---|
| Java | 17 | Lenguaje |
| Spring Boot | 4.0.7 | Framework base |
| Spring WebFlux | (Netty) | API REST reactiva, no bloqueante |
| Spring Data R2DBC | — | Acceso reactivo a la base de datos |
| H2 | — | Base de datos en memoria (se reinicia con la app) |
| Lombok | — | Menos código repetitivo (getters, constructores) |
| Docker / Docker Compose | — | Contenedores y orquestación |
| nginx | alpine | Proxy inverso |

## 2. Dependencias en Spring Initializr

Para crear este proyecto desde cero en [start.spring.io](https://start.spring.io):

- **Project:** Maven · **Language:** Java 17 · **Packaging:** Jar
- **Dependencias a seleccionar:**
  1. `Spring Reactive Web` (WebFlux + Netty)
  2. `Spring Data R2DBC` (repositorios reactivos)
  3. `H2 Database` (incluye el driver `r2dbc-h2`)
  4. `Lombok`

> La tabla se crea con [src/main/resources/schema.sql](src/main/resources/schema.sql)
> gracias a `spring.sql.init.mode: always` en el
> [application.yaml](src/main/resources/application.yaml).

## 3. Estructura del proyecto

```
marilynvilcapuma_Webflux/
├── src/
│   └── main/
│       ├── java/vallegrande/edu/pe/marilynvilcapuma/
│       │   ├── MarilynvilcapumaWebfluxApplication.java  → clase principal (arranque)
│       │   ├── model/
│       │   │   ├── Persona.java                → entidad maestro (tabla "persona")
│       │   │   └── Matricula.java              → entidad transaccional (tabla "matricula")
│       │   ├── repository/
│       │   │   ├── PersonaRepository.java      → acceso a datos (R2DBC)
│       │   │   └── MatriculaRepository.java    → acceso a datos + query por FK
│       │   ├── service/
│       │   │   ├── PersonaService.java         → contrato de negocio
│       │   │   ├── MatriculaService.java       → contrato de negocio
│       │   │   └── impl/
│       │   │       ├── PersonaServiceImpl.java → lógica + logs de cada acción
│       │   │       └── MatriculaServiceImpl.java → lógica + validación del maestro
│       │   └── rest/
│       │       ├── PersonaRest.java            → endpoints REST (CRUD)
│       │       └── MatriculaRest.java          → endpoints REST (CRUD + por persona)
│       └── resources/
│           ├── application.yaml                → config: puerto (APP_PORT), H2, R2DBC
│           └── schema.sql                      → creación de la tabla + dato inicial
├── Dockerfile                                  → imagen multistage (scratch, 72 MB)
├── docker-compose.yml                          → stack: app + mysql + nginx
├── nginx.conf                                  → proxy inverso (8097 → app:9096)
├── .dockerignore                               → excluye target/, .git/, etc. del build
├── pom.xml                                     → dependencias y build de Maven
└── README.md                                   → esta documentación
```

## 4. Endpoints

### Maestro — base `/v1/api/persona`

| Método | Ruta | Acción | Respuesta |
|---|---|---|---|
| GET | `/v1/api/persona` | Listar todas | 200 |
| GET | `/v1/api/persona/{id}` | Buscar por id | 200 / 404 |
| POST | `/v1/api/persona` | Registrar | 201 |
| PUT | `/v1/api/persona/{id}` | Actualizar | 200 / 404 |
| DELETE | `/v1/api/persona/{id}` | Eliminar | 204 |

### Transaccional — base `/v1/api/matricula`

| Método | Ruta | Acción | Respuesta |
|---|---|---|---|
| GET | `/v1/api/matricula` | Listar todas | 200 |
| GET | `/v1/api/matricula/{id}` | Buscar por id | 200 / 404 |
| GET | `/v1/api/matricula/persona/{personaId}` | Matrículas de una persona | 200 |
| POST | `/v1/api/matricula` | Registrar (valida que la persona exista) | 201 / 400 |
| PUT | `/v1/api/matricula/{id}` | Actualizar | 200 / 404 |
| DELETE | `/v1/api/matricula/{id}` | Eliminar | 204 |

## 5. El JSON de la entidad y su mapeo

> En la rúbrica del curso la entidad de ejemplo se llama *student*; en este
> proyecto el maestro es **Persona** (estudiante) — el patrón es el mismo.

Cuerpo del **POST/PUT** de Persona (el JSON del "student"):

```json
{
  "first_name": "Marilyn",
  "last_name": "Vilcapuma Trujillo",
  "dni": 74124567,
  "promotion": 2026
}
```

Cada clave del JSON se mapea 1:1 con un atributo de la clase Java
([Persona.java](src/main/java/vallegrande/edu/pe/marilynvilcapuma/model/Persona.java))
y este a su vez con una columna de la tabla (`@Table(name = "persona")`):

| Clave JSON | Atributo Java | Columna SQL | Tipo | ¿Quién lo pone? |
|---|---|---|---|---|
| — | `id` (`@Id`) | `id BIGINT AUTO_INCREMENT` | `Long` | La BD al insertar |
| `first_name` | `first_name` | `first_name VARCHAR(50)` | `String` | El cliente |
| `last_name` | `last_name` | `last_name VARCHAR(60)` | `String` | El cliente |
| `dni` | `dni` | `dni INT` | `Integer` | El cliente |
| `promotion` | `promotion` | `promotion INT` | `Integer` | El cliente |
| — | `registration_date` | `registration_date TIMESTAMP` | `LocalDateTime` | El servidor (`save`) |

> El cliente **no envía** `id` ni `registration_date`: el id lo genera la base
> de datos y la fecha la pone el servidor. Todo lo que llega en el JSON,
> Jackson (el convertidor de Spring) lo transforma automáticamente al objeto
> Java, casando cada clave con el atributo del **mismo nombre**.

Cuerpo del **POST** de Matrícula (transaccional):

```json
{
  "persona_id": 1,
  "course": "Desarrollo de Software",
  "cycle": "2026-I",
  "amount": 350.00
}
```

Aquí `persona_id` es la **clave foránea** al maestro: no se envía el objeto
persona completo, solo su id. `status` es opcional (nace `"A"` de activa) y
`enrollment_date` también la pone el servidor.

## 6. Cómo funciona el Repository

El repository es **solo una interfaz** — nunca se escribe la clase que la
implementa. Al arrancar, **Spring Data R2DBC genera la implementación
automáticamente** a partir de la firma:

```java
@Repository
public interface PersonaRepository extends ReactiveCrudRepository<Persona, Long> {
}
```

Por extender `ReactiveCrudRepository<Persona, Long>` (entidad + tipo de la
clave primaria) hereda gratis: `findAll()`, `findById()`, `save()`,
`deleteById()`, `count()`, etc. Por eso está "vacío" y aun así funciona.

El del transaccional agrega **una consulta propia**, porque los métodos
heredados solo saben buscar por la clave primaria y aquí se necesita buscar
por **otra columna** (la FK):

```java
@Repository
public interface MatriculaRepository extends ReactiveCrudRepository<Matricula, Long> {
    @Query("SELECT * FROM \"matricula\" WHERE persona_id = :personaId")
    Flux<Matricula> findByPersonaId(Long personaId);
}
```

- `:personaId` es un **parámetro con nombre**: Spring inyecta el argumento del
  método de forma segura (evita inyección SQL).
- Devuelve `Flux` (0..N resultados) porque una persona puede tener **muchas**
  matrículas — relación 1:N maestro-transaccional. Buscar por id devuelve
  `Mono` (0..1).
- Cada fila del resultado se convierte automáticamente en un objeto
  `Matricula` gracias al mapeo `@Table`/`@Id` del modelo.

## 7. El `save` y el `update` en el ServiceImpl

Regla general de Reactor: **nada se ejecuta al llamar al método** — solo se
arma la "receta" reactiva. Se ejecuta cuando WebFlux se suscribe al llegar la
petición HTTP.

### `save` — validar el maestro antes de insertar

```java
public Mono<Matricula> save(Matricula matricula) {
    return personaRepository.findById(matricula.getPersona_id())   // (1)
            .flatMap(persona -> {                                  // (2)
                matricula.setEnrollment_date(LocalDateTime.now()); // (3)
                if (matricula.getStatus() == null) {
                    matricula.setStatus("A");                      // (4)
                }
                return repository.save(matricula);                 // (5)
            })
            .doOnNext(m -> log.info("Registrar - Matricula creada ..."));
}
```

1. **Valida el maestro primero**: busca la persona referenciada por la FK.
2. `flatMap` y no `map`, porque adentro se llama otra operación asíncrona
   (`repository.save`) que ya devuelve un `Mono` — con `map` quedaría un
   `Mono<Mono<Matricula>>` anidado.
3. La fecha la pone **el servidor**, nunca el cliente.
4. Valor por defecto de negocio: sin estado ⇒ nace activa (`"A"`).
5. Como `id` es `null`, `save()` hace un **INSERT** y devuelve la entidad con
   el id que generó la base de datos.

**El camino vacío**: si la persona NO existe, `findById` devuelve un `Mono`
vacío ⇒ el `flatMap` **no se ejecuta** ⇒ no se inserta nada ⇒ el REST lo
convierte con `defaultIfEmpty(badRequest())` en un **HTTP 400**. No hay ningún
`if (persona == null)`: la ausencia viaja por el flujo como "flujo vacío".

> Se usa `doOnNext` (solo corre si hay valor) y no `doOnSuccess` (también corre
> con el flujo vacío pasando `null`, lo que causaba un `NullPointerException`
> → error 500).

### `update` — buscar → copiar → guardar

```java
public Mono<Matricula> update(Long id, Matricula matricula) {
    return repository.findById(id)                    // (1)
            .flatMap(existing -> {                    // (2)
                existing.setCourse(matricula.getCourse());
                existing.setCycle(matricula.getCycle());
                existing.setAmount(matricula.getAmount());
                existing.setStatus(matricula.getStatus());
                return repository.save(existing);     // (3)
            })
            .doOnNext(m -> log.info("Actualizar - Matricula actualizada ..."));
}
```

1. Carga la fila **real** de la base de datos; no confía en el objeto del cliente.
2. Copia sobre `existing` **solo los campos editables**. No se copian: el `id`
   (viene de la URL), el `persona_id` (una matrícula no se transfiere a otra
   persona) ni `enrollment_date` (la fecha de inscripción es histórica).
   Eso es una decisión de **negocio**, no técnica.
3. Como `existing` **sí** tiene `id`, el mismo `save()` ahora hace un
   **UPDATE ... WHERE id = ?** en lugar de INSERT — se decide por el `@Id`.

Y de nuevo el camino vacío: id inexistente ⇒ `findById` vacío ⇒ `flatMap` no
corre ⇒ el REST responde **404** con `defaultIfEmpty(notFound())`.

## 8. Cómo funcionan los puertos

El puerto **no está fijo en el código**: el `application.yaml` lo lee de la
variable de entorno `APP_PORT` (si no existe, usa 9095):

```yaml
server:
  port: ${APP_PORT:9095}
```

Cadena completa cuando corre con Docker Compose:

```
Navegador ── 9097 ──> nginx (escucha 8097) ── proxy ──> app:9096 (Netty)
Navegador ── 9096 ──────────────────────────────────────> app:9096 (directo)
```

| Servicio | Puerto en tu PC | Puerto interno | Definido en |
|---|---|---|---|
| app | 9096 | 9096 (`APP_PORT`) | Dockerfile + docker-compose.yml |
| nginx | 9097 | 8097 (`listen`) | nginx.conf + docker-compose.yml |
| mysql | 3306 | 3306 | docker-compose.yml |

> En `-p PUERTO_PC:PUERTO_CONTENEDOR` el lado izquierdo debe estar libre en tu
> máquina; el derecho es donde escucha el proceso dentro del contenedor.

## 9. El Dockerfile (multistage → imagen de 72 MB)

El [Dockerfile](Dockerfile) tiene **3 etapas** para que la imagen final no
arrastre nada del proceso de compilación:

1. **builder** — Maven compila y empaqueta el JAR. Se copia primero solo el
   `pom.xml` para que Docker cachee la descarga de dependencias.
2. **jre-builder** — `jlink` genera un **JRE mínimo** (~49 MB) solo con los
   módulos de Java que la app usa, en lugar del JDK completo (~300 MB).
   Después, `strip` elimina símbolos de depuración de las librerías nativas.
3. **scratch** — imagen final **totalmente vacía**: solo entra el JRE mínimo,
   el loader de musl, `/tmp` y el JAR. Corre con usuario **no-root** (`1000:1000`).

Puntos clave si se reutiliza en otro proyecto (comentarios `🔧 ADAPTAR` en el archivo):
la versión de Java (etapas 1 y 2), la lista de módulos de `jlink` (si una
dependencia nueva pide una clase `java.*` al arrancar, el error indica el módulo
que falta) y el puerto (`ENV APP_PORT` + `EXPOSE`).

## 10. Configuración de nginx

[nginx.conf](nginx.conf): nginx escucha en el puerto **8097** y reenvía todo el
tráfico al servicio `app` por la red interna de Docker:

```nginx
server {
    listen 8097;
    location / {
        proxy_pass http://app:9096;   # "app" = nombre del servicio en el compose
    }
}
```

`app` se resuelve por DNS interno de Docker: todos los servicios del
`docker-compose.yml` comparten la red `tru-network`.

## 11. Comandos

> Guía completa paso a paso (Docker Hub, Compose y Kubernetes) en
> [COMANDOS.md](COMANDOS.md).

### Ejecutar con Maven (sin Docker)

```powershell
mvn spring-boot:run                # puerto 9095 (default del yaml)
$env:APP_PORT='9096'; mvn spring-boot:run   # con otro puerto
```

### Docker (imagen sola)

```powershell
# Construir la imagen
docker build -t marilynvilcapuma-webflux:latest .

# Ver tamaño
docker images marilynvilcapuma-webflux

# Correr el contenedor
docker run -d --name mi-app -p 9096:9096 marilynvilcapuma-webflux:latest

# Logs / detener / eliminar
docker logs -f mi-app
docker stop mi-app
docker rm mi-app
```

### Docker Compose (stack completo: app + mysql + nginx)

```powershell
docker compose up -d --build      # construir y levantar todo
docker compose ps                 # los 3 contenedores deben estar "Up"
docker compose logs -f app        # logs de un servicio
docker compose down               # detener y eliminar todo
```

Pruebas: <http://localhost:9096/v1/api/persona> (directo) y
<http://localhost:9097/v1/api/persona> (vía nginx).

### Subir a Docker Hub

```powershell
docker login
docker tag marilynvilcapuma_webflux:latest TUUSUARIO/ht-232-##-marilyn-vilcapuma:v1
docker push TUUSUARIO/ht-232-##-marilyn-vilcapuma:v1
```

> Los nombres de imagen van **siempre en minúsculas** y con el prefijo de tu
> usuario de Docker Hub.
