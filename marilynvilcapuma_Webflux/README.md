# Microservicio Persona — Spring Boot WebFlux

Microservicio **reactivo** que expone un CRUD de personas sobre una base de datos
**H2 en memoria**, empaquetado en una imagen Docker ultra liviana (**72 MB**, base
`scratch`) y desplegado con Docker Compose detrás de un proxy **nginx**.

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
│       │   │   └── Persona.java                → entidad (tabla "persona")
│       │   ├── repository/
│       │   │   └── PersonaRepository.java      → acceso a datos (R2DBC)
│       │   ├── service/
│       │   │   ├── PersonaService.java         → contrato de negocio
│       │   │   └── impl/
│       │   │       └── PersonaServiceImpl.java → lógica + logs de cada acción
│       │   └── rest/
│       │       └── PersonaRest.java            → endpoints REST (CRUD)
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

Base: `/v1/api/persona`

| Método | Ruta | Acción | Respuesta |
|---|---|---|---|
| GET | `/v1/api/persona` | Listar todas | 200 |
| GET | `/v1/api/persona/{id}` | Buscar por id | 200 / 404 |
| POST | `/v1/api/persona` | Registrar | 201 |
| PUT | `/v1/api/persona/{id}` | Actualizar | 200 / 404 |
| DELETE | `/v1/api/persona/{id}` | Eliminar | 204 |

Ejemplo de cuerpo (POST/PUT):

```json
{
  "first_name": "Marilyn",
  "last_name": "Vilcapuma Trujillo",
  "dni": 74124567,
  "promotion": 2026
}
```

## 5. Cómo funcionan los puertos

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

## 6. El Dockerfile (multistage → imagen de 72 MB)

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

## 7. Configuración de nginx

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

## 8. Comandos

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
