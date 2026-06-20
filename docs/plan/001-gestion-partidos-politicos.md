# Implementation Plan: Gestión de Partidos Políticos

**Date**: 2026-06-19
**Spec**: [001-gestion-partidos-politicos.md](../spec/001-gestion-partidos-politicos.md)

## Summary

Implementar el CRUD de partidos políticos (crear, editar, listar, inhabilitar). Acceso exclusivo para el rol
`GESTOR_CANDIDATURAS`. Validar nombres únicos con máximo 100 caracteres en mayúsculas (A-Z). Las imágenes (logo) se
delegan a Cloudinary y solo se persiste la URL pública. Si el servicio de imágenes falla, el partido se crea sin logo (
opcional). No se puede inhabilitar un partido con candidatos en votación `EN_PROGRESO`.

**Technical approach**: Arquitectura hexagonal con Spring Boot WebFlux. Nueva entidad de dominio `PartidoPolitico` con
sus propios estados (`HABILITADO`, `INHABILITADO`). Casos de uso en `application/partido/`. Endpoints REST bajo
`/api/v1/partidos`. Integración con Cloudinary para imágenes.

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 3.x (WebFlux), Spring Data R2DBC, Flyway, jjwt, Lombok, MapStruct,
springdoc-openapi, Spring Boot Actuator, Bean Validation
**Storage**: PostgreSQL 16 (via R2DBC)
**Testing**: JUnit 5, Mockito, WebTestClient, Testcontainers
**Target Platform**: Linux server (Docker)
**Project Type**: Microservicio REST reactivo — Módulo 2 (Partidos, Candidatos y Votaciones)
**Performance Goals**: <200ms p95 en endpoints de partidos
**Constraints**: Nombre único, máx. 100 caracteres, solo A-Z. Logo URL delegado a Cloudinary (opcional). No inhabilitar
partido con candidatos en votación EN_PROGRESO.
**Scale/Scope**: ~50 partidos políticos concurrentes

## Project Structure

### Documentation (this feature)

```text
docs/
├── plan/
│   └── 001-gestion-partidos-politicos.md   # Este archivo
├── spec/
│   └── 001-gestion-partidos-politicos.md
├── guia-uso-modulo-2.md
└── stakeholders.md
```

### Source Code (repository root — nuevo)

```text
src/main/java/com/safevoting/elecciones/
│
├── domain/
│   ├── model/
│   │   └── partido/
│   │       ├── PartidoPolitico.java         # Builder + estados
│   │       └── EstadoPartido.java           # HABILITADO, INHABILITADO
│   ├── exception/
│   │   └── partido/
│   │       ├── NombreDuplicadoException.java
│   │       ├── PartidoNoEncontradoException.java
│   │       ├── PartidoYaInhabilitadoException.java
│   │       └── PartidoConCandidatosEnVotacionException.java
│   └── repository/
│       ├── PartidoPoliticoRepository.java   # Puerto
│       └── CandidaturaRepository.java       # Para validar inhabilitación (solo findActivasByPartido)
│
├── application/
│   └── partido/
│       ├── CrearPartidoUseCase.java
│       ├── EditarPartidoUseCase.java
│       ├── ListarPartidosUseCase.java
│       └── InhabilitarPartidoUseCase.java
│
└── infrastructure/
    ├── config/
    │   ├── BeanConfiguration.java
    │   ├── SecurityConfig.java              # .hasRole("GESTOR_CANDIDATURAS") en /api/v1/partidos/**
    │   ├── JwtProvider.java
    │   └── JwtFilter.java
    └── adapter/
        ├── in/
        │   └── rest/
        │       └── partido/
        │           ├── dto/
        │           │   ├── PartidoRequest.java
        │           │   └── PartidoResponse.java
        │           ├── mapper/
        │           │   └── PartidoDtoMapper.java   # MapStruct
        │           └── PartidoController.java
        └── out/
            ├── persistence/
            │   └── partido/
            │       ├── PartidoEntity.java
            │       ├── PartidoPersistenceMapper.java
            │       ├── PartidoReactiveRepository.java
            │       └── PartidoRepositoryAdapter.java
            └── cloudinary/
                └── CloudinaryAdapter.java

src/main/resources/
└── db/migration/
    └── V1__crear_tabla_partido.sql

src/test/java/com/safevoting/elecciones/
├── unit/
│   ├── domain/
│   │   └── model/partido/
│   │       └── PartidoPoliticoTest.java
│   └── application/
│       └── partido/
│           ├── CrearPartidoUseCaseTest.java
│           ├── EditarPartidoUseCaseTest.java
│           ├── ListarPartidosUseCaseTest.java
│           └── InhabilitarPartidoUseCaseTest.java
└── integration/
    └── rest/
        └── partido/
            └── PartidoControllerIntegrationTest.java
```

**Structure Decision**: Misma arquitectura hexagonal. `PartidoPolitico` es una entidad de dominio en
`domain/model/partido/`. Las excepciones nuevas van en `exception/partido/`. El controlador se expone bajo
`/api/v1/partidos`. El logo se delega a `CloudinaryAdapter` que implementa un puerto `ImageStorageService` en dominio.
Todas las rutas usan el prefijo `/api/v1/`.

---

## Phase 1: Foundational — Nueva entidad y modelo de dominio (Blocking Prerequisites)

**Purpose**: Crear la tabla, entidad, enumerados, excepciones y repositorio para `PartidoPolitico`.

**⚠️ CRITICAL**: Ningún user story de partidos puede comenzar antes de esta fase.

- [ ] T001 Crear migración Flyway `V1__crear_tabla_partido.sql`:
    - Tabla `partido`:
        - `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
        - `nombre VARCHAR(100) NOT NULL UNIQUE`
        - `descripcion TEXT`
        - `logo_url VARCHAR(500)`
        - `estado VARCHAR(20) NOT NULL DEFAULT 'HABILITADO'`
        - `created_at TIMESTAMP NOT NULL DEFAULT NOW()`
        - `updated_at TIMESTAMP NOT NULL DEFAULT NOW()`
    - `CONSTRAINT chk_estado_partido CHECK (estado IN ('HABILITADO','INHABILITADO'))`
    - `CONSTRAINT chk_nombre_uppercase CHECK (nombre ~ '^[A-Z ]+$')`
    - Validación en constructor: no vacío, `length <= 100`, `matches("^[A-Z ]+$")`.
    - Lanza `DatosInvalidosException` si falla.
- [ ] T003 Crear enum `EstadoPartido` en `domain/model/partido/`:
    - `HABILITADO`, `INHABILITADO`.
    - Método `esHabilitado()` → `this == HABILITADO`.
- [ ] T004 Crear entidad `PartidoPolitico` en `domain/model/partido/`:
    - `@Builder`, campos: `id` (UUID), `nombre` (NombrePartido), `descripcion` (String, nullable), `logoUrl` (String,
      nullable), `estado` (EstadoPartido, default HABILITADO).
    - Métodos de validación:
        - `validateNombre()`: no nulo, invoca validación inline en la entidad.
    - Método público `validateInfo()`: invoca `validateNombre()`.
    - Métodos de mutación:
        - `editar(String nombre, String descripcion, String logoUrl)`: actualiza campos, `validateInfo()`.
        - `inhabilitar()`: valida `esHabilitado()`. Si no → `PartidoYaInhabilitadoException`. Asigna
          `estado = INHABILITADO`.
- [ ] T005 Crear excepciones de dominio nuevas:
    - `NombreDuplicadoException(String nombre)` → errorCode `NOMBRE_DUPLICADO`.
    - `PartidoNoEncontradoException(UUID id)` → errorCode `PARTIDO_NO_ENCONTRADO`.
    - `PartidoYaInhabilitadoException(UUID id)` → errorCode `PARTIDO_YA_INHABILITADO`.
    - `PartidoConCandidatosEnVotacionException(UUID id)` → errorCode `PARTIDO_CON_CANDIDATOS_EN_VOTACION`.
- [ ] T006 Crear puerto `PartidoPoliticoRepository` en `domain/repository/`:
    - `save(PartidoPolitico)` → `Mono<PartidoPolitico>`.
    - `findById(UUID id)` → `Mono<PartidoPolitico>`.
    - `findAll()` → `Flux<PartidoPolitico>`.
    - `existsByNombre(String nombre)` → `Mono<Boolean>`.
    - `update(PartidoPolitico)` → `Mono<PartidoPolitico>`.
- [ ] T007 Crear adaptador `PartidoRepositoryAdapter` en `infrastructure/adapter/out/persistence/partido/`:
    - Implementa el puerto usando `R2dbcEntityTemplate`.
    - `save`: inserta fila y devuelve entidad con id generado.
    - `existsByNombre`: `SELECT COUNT(*) FROM partido WHERE UPPER(nombre) = UPPER(:nombre)`.
    - `update`:
      `UPDATE partido SET nombre = :nombre, descripcion = :descripcion, logo_url = :logoUrl, estado = :estado, updated_at = NOW() WHERE id = :id`.
- [ ] T008 Modificar `GlobalExceptionHandler`:
    - Agregar mapeos:
        - `NombreDuplicadoException` → 409
        - `PartidoNoEncontradoException` → 404
        - `PartidoYaInhabilitadoException` → 409
        - `PartidoConCandidatosEnVotacionException` → 409
- [ ] T009 Modificar `SecurityConfig`:
    - `/api/v1/partidos/**` requiere autenticación y rol `GESTOR_CANDIDATURAS`.
- [ ] T010 Crear `CloudinaryAdapter` en `infrastructure/adapter/out/cloudinary/`:
    - Puerto `ImageStorageService` en `domain/repository/` con método `upload(byte[] image)` → `Mono<String>` (URL).
    - Si falla (timeout, error de red), retorna `Mono.empty()` — el use case permite crear sin logo.

**Checkpoint**: Modelo de `PartidoPolitico` completo. Repositorio funcional. Seguridad configurada para
`GESTOR_CANDIDATURAS`.

---

## Phase 2: User Story 1 — Crear partido político (Priority: P1)

**Goal**: El Gestor Candidaturas registra un nuevo partido político con nombre, descripción y logo opcional.

**Independent Test**: Autenticar como `GESTOR_CANDIDATURAS` → `POST /api/v1/partidos` → 201 con datos del partido.
Nombre duplicado → 409. Nombre con minúsculas → 422. Sin logo → 201 (logo nullable).

### Tests for User Story 1

- [ ] T011 [P] [US1] Unit test `PartidoPoliticoTest`: construir con Builder + `validateInfo()` → OK. Nombre vacío →
  `DatosInvalidosException`. Nombre > 100 caracteres → `DatosInvalidosException`. Nombre con minúsculas →
  `DatosInvalidosException`.
- [ ] T012 [P] [US1] Unit test `CrearPartidoUseCaseTest`: mock repos → partido creado exitosamente. Nombre duplicado →
  `NombreDuplicadoException`. Logo upload falla → partido creado sin logo.
- [ ] T013 [P] [US1] Integration test `PartidoControllerIntegrationTest`: `POST /api/v1/partidos` → 201. Nombre
  duplicado → 409.

### Implementation for User Story 1

- [ ] T014 [P] [US1] Crear `PartidoRequest` DTO: `@NotBlank @Size(max=100) @Pattern(regexp="^[A-Z ]+$") String nombre`,
  `String descripcion`, `String logoBase64` (opcional, para subir a Cloudinary).
- [ ] T015 [P] [US1] Crear `PartidoResponse` DTO: `UUID id`, `String nombre`, `String descripcion`, `String logoUrl`,
  `String estado`.
- [ ] T016 [P] [US1] Crear `PartidoDtoMapper` (MapStruct): `PartidoRequest → PartidoPolitico` (solo nombre y
  descripcion; logoUrl lo asigna el use case). `PartidoPolitico → PartidoResponse`.
- [ ] T017 [US1] Crear `CrearPartidoUseCase` en `application/partido/`:
    - Inyecta `PartidoPoliticoRepository`, `ImageStorageService`.
    - Método `ejecutar(PartidoRequest request)`:
        1. Validar que nombre no exista: `repository.existsByNombre(request.nombre)`. Si true →
           `NombreDuplicadoException`.
        2. Subir logo si `request.logoBase64 != null`: `imageStorageService.upload(decodeBase64)`. Si retorna URL →
           asignar. Si falla → continuar sin logo.
        3. Construir `PartidoPolitico` con Builder: nombre, descripcion, logoUrl (nullable), estado = HABILITADO.
        4. `partido.validateInfo()`.
        5. `repository.save(partido)` → retorna `PartidoPolitico`.
- [ ] T018 [US1] Crear `PartidoController` en `infrastructure/adapter/in/rest/partido/`:
    - `POST /api/v1/partidos`: recibe `@Valid @RequestBody PartidoRequest`, invoca use case, retorna `201` con
      `PartidoResponse`.
    - Anotar con `@Tag(name = "Partidos Políticos")`.

**Checkpoint**: Gestor Candidaturas puede crear partidos con validaciones de nombre y logo.

---

## Phase 3: User Story 2 — Listar partidos (Priority: P2)

**Goal**: El Gestor Candidaturas consulta todos los partidos registrados.

**Independent Test**: `GET /api/v1/partidos` → 200 con lista. Sin partidos → 200 con lista vacía.

### Tests for User Story 2

- [ ] T019 [P] [US2] Unit test `ListarPartidosUseCaseTest`: repos con varios partidos → lista con todos. Repos vacío →
  lista vacía.
- [ ] T020 [P] [US2] Integration test: `GET /api/v1/partidos` con partidos creados → 200 con lista.

### Implementation for User Story 2

- [ ] T021 [US2] Crear `ListarPartidosUseCase` en `application/partido/`:
    - Inyecta `PartidoPoliticoRepository`.
    - Método `ejecutar()`: `repository.findAll().map(mapper::toResponse)`. Retorna `Flux<PartidoResponse>`.
- [ ] T022 [US2] Agregar endpoint a `PartidoController`:
    - `GET /api/v1/partidos`: invoca use case, retorna `200` con `List<PartidoResponse>`.
    - `GET /api/v1/partidos/{id}`: invoca `repository.findById(id)`, retorna `200` o 404.

**Checkpoint**: Listado de partidos funcional.

---

## Phase 4: User Story 3 — Editar partido político (Priority: P3)

**Goal**: El Gestor Candidaturas modifica descripción y logo de un partido existente. El nombre también puede cambiarse
si no está duplicado.

**Independent Test**: `PUT /api/v1/partidos/{id}` → 200 con datos actualizados. Partido inexistente → 404. Nuevo nombre
duplicado → 409.

### Tests for User Story 3

- [ ] T023 [P] [US3] Unit test `EditarPartidoUseCaseTest`: edición exitosa de descripción y logo. Cambio de nombre a uno
  duplicado → `NombreDuplicadoException`. Partido no encontrado → `PartidoNoEncontradoException`.
- [ ] T024 [P] [US3] Integration test: `PUT /api/v1/partidos/{id}` → 200.

### Implementation for User Story 3

- [ ] T025 [US3] Crear `EditarPartidoUseCase` en `application/partido/`:
    - Inyecta `PartidoPoliticoRepository`, `ImageStorageService`.
    - Método `ejecutar(UUID id, PartidoRequest request)`:
        1. Buscar partido por id. Si no existe → `PartidoNoEncontradoException`.
        2. Si el nombre cambió, validar que no exista otro partido con ese nombre:
           `repository.existsByNombre(request.nombre)`. Si true → `NombreDuplicadoException`.
        3. Si hay nuevo logo en base64 → subir a Cloudinary. Si falla → mantener el logo anterior.
        4. `partido.editar(nuevoNombre, nuevaDescripcion, nuevoLogoUrl)`.
        5. `repository.update(partido)` → retorna `PartidoPolitico`.
- [ ] T026 [US3] Agregar endpoint a `PartidoController`:
    - `PUT /api/v1/partidos/{id}`: recibe `@Valid @RequestBody PartidoRequest`, invoca use case, retorna `200` con
      `PartidoResponse`.

**Checkpoint**: Edición de partidos funcional.

---

## Phase 5: User Story 4 — Inhabilitar partido político (Priority: P4)

**Goal**: El Gestor Candidaturas inhabilita un partido. No puede inhabilitarse si tiene candidatos en votación
`EN_PROGRESO`.

**Independent Test**: `PATCH /api/v1/partidos/{id}/inhabilitar` → 200. Partido ya inhabilitado → 409. Partido con
candidatos en votación EN_PROGRESO → 409. Partido inexistente → 404.

### Tests for User Story 4

- [ ] T027 [P] [US4] Unit test `InhabilitarPartidoUseCaseTest`: inhabilitación exitosa. Partido ya inhabilitado →
  `PartidoYaInhabilitadoException`. Partido con candidatos en votación EN_PROGRESO →
  `PartidoConCandidatosEnVotacionException`.
- [ ] T028 [P] [US4] Unit test: verificar que `inhabilitar()` cambia estado correctamente.

### Implementation for User Story 4

- [ ] T029 [US4] Crear puerto parcial `CandidaturaRepository.findActivasByPartidoId(UUID partidoId)` →
  `Flux<Candidatura>` en `domain/repository/`. Necesario para validar la restricción de inhabilitación.
- [ ] T030 [US4] Crear `InhabilitarPartidoUseCase` en `application/partido/`:
    - Inyecta `PartidoPoliticoRepository`, `CandidaturaRepository`.
    - Método `ejecutar(UUID partidoId)`:
        1. Buscar partido por id. Si no existe → `PartidoNoEncontradoException`.
        2. Buscar candidaturas activas del partido: `candidaturaRepository.findActivasByPartidoId(partidoId)`.
        3. Para cada candidatura, verificar si su votación está `EN_PROGRESO` (join o consulta adicional).
        4. Si hay al menos una candidatura en votación `EN_PROGRESO` → `PartidoConCandidatosEnVotacionException`.
        5. `partido.inhabilitar()` → `repository.update(partido)`.
        6. Retorna `PartidoPolitico`.
- [ ] T031 [US4] Agregar endpoint a `PartidoController`:
    - `PATCH /api/v1/partidos/{id}/inhabilitar`: invoca use case, retorna `200` con `PartidoResponse`.

**Checkpoint**: Inhabilitación de partidos con validación de candidatos en votación activa.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Foundational (Phase 1)**: Sin dependencias externas. BLOQUEA todos los user stories.
- **US1 — Crear (Phase 2)**: Depende de Phase 1.
- **US2 — Listar (Phase 3)**: Depende de Phase 1. Independiente de US1.
- **US3 — Editar (Phase 4)**: Depende de Phase 1. Puede iniciarse en paralelo con US1/US2.
- **US4 — Inhabilitar (Phase 5)**: Depende de Phase 1. Requiere `CandidaturaRepository` para la validación cruzada.

### User Story Dependencies

- **US1 (P1)**: Solo depende de Foundational.
- **US2 (P2)**: Solo depende de Foundational.
- **US3 (P3)**: Solo depende de Foundational.
- **US4 (P4)**: Solo depende de Foundational. Requiere que el repositorio de candidaturas esté disponible (plan 004).

### Within Each User Story

- DTOs y mapper primero.
- Caso de uso antes que controlador.
- Tests unitarios de dominio y caso de uso, luego tests de integración del endpoint.

---

## Notes

- **Cloudinary**: el `CloudinaryAdapter` debe tener timeout de 10s. Si falla, no debe bloquear la creación del partido (
  logo es opcional). Usar `onErrorResume` para devolver `Mono.empty()`.
- **Inhabilitación con candidatos**: validar contra las tablas `candidato`, `candidatura` y `votacion`. Si el plan 004
  no está implementado aún, esta validación puede ser mockeada en tests unitarios.
- **Nombre único case-insensitive**: el índice unique de PostgreSQL ya maneja esto si se usa `UPPER(nombre)`. La
  constraint `chk_nombre_uppercase` garantiza que solo se almacenan mayúsculas.
