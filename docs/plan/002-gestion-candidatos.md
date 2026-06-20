# Implementation Plan: Gestión de Candidatos

**Date**: 2026-06-19
**Spec**: [002-gestion-candidatos.md](../spec/002-gestion-candidatos.md)

## Summary

Implementar el CRUD de candidatos (registrar, editar, listar, dar de baja administrativa, cancelar voluntariamente).
Acceso exclusivo para el rol `GESTOR_CANDIDATURAS`. Cada candidato vincula un usuario del Módulo 1 (`usuario_id` como FK
lógica) a un partido político. La baja administrativa (`SUSPENDIDO`) registra motivo obligatorio; la cancelación
voluntaria (`INACTIVO`) permite reactivación futura. Un mismo usuario solo puede pertenecer a un partido a la vez. Las
imágenes (foto) se delegan a Cloudinary.

**Technical approach**: Nueva entidad de dominio `Candidato` con ciclo de vida de tres estados (`ACTIVO`, `INACTIVO`,
`SUSPENDIDO`). Casos de uso en `application/candidato/`. Endpoints REST bajo `/api/v1/candidatos`. Cliente HTTP para
validar existencia de usuario contra Módulo 1 (`GET /api/v1/users/{id}`).

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 3.x (WebFlux), Spring Data R2DBC, Flyway, jjwt, Lombok, MapStruct,
springdoc-openapi, Spring Boot Actuator, Bean Validation
**Storage**: PostgreSQL 16 (via R2DBC)
**Testing**: JUnit 5, Mockito, WebTestClient, Testcontainers
**Target Platform**: Linux server (Docker)
**Project Type**: Microservicio REST reactivo — extensión del plan 001 (partidos)
**Performance Goals**: <200ms p95 en endpoints de candidatos
**Constraints**: usuario_id debe existir en Módulo 1. Un usuario no puede ser candidato en >1 partido. Foto Cloudinary (
opcional). Baja administrativa requiere motivo. Cancelación voluntaria no requiere motivo. Baja solo si no tiene
candidaturas en votación EN_PROGRESO.
**Scale/Scope**: ~200 candidatos concurrentes

## Project Structure

### Documentation (this feature)

```text
docs/
├── plan/
│   └── 002-gestion-candidatos.md           # Este archivo
├── spec/
│   └── 002-gestion-candidatos.md
├── guia-uso-modulo-2.md
└── stakeholders.md
```

### Source Code (repository root — nuevo y modificado)

```text
src/main/java/com/safevoting/elecciones/
│
├── domain/
│   ├── model/
│   │   └── candidato/
│   │       ├── Candidato.java              # Builder + usuario_id, partido_id, lema, foto_url, motivo_baja
│   │       └── EstadoCandidato.java        # ACTIVO, INACTIVO, SUSPENDIDO
│   ├── exception/
│   │   └── candidato/
│   │       ├── CandidatoNoEncontradoException.java
│   │       ├── CandidatoYaInactivoException.java
│   │       ├── CandidatoYaSuspendidoException.java
│   │       ├── UsuarioYaCandidatoException.java
│   │       ├── PartidoInhabilitadoException.java
│   │       ├── UsuarioNoEncontradoException.java
│   │       ├── MotivoBajaRequeridoException.java
│   │       └── CandidatoInscritoEnVotacionException.java
│   └── repository/
│       ├── CandidatoRepository.java        # Puerto
│       └── PartidoPoliticoRepository.java  # Para validar que partido existe y está HABILITADO
│
├── application/
│   └── candidato/
│       ├── RegistrarCandidatoUseCase.java
│       ├── EditarCandidatoUseCase.java
│       ├── ListarCandidatosUseCase.java
│       ├── DarBajaCandidatoUseCase.java    # SUSPENDIDO + motivo
│       └── CancelarCandidaturaUseCase.java # INACTIVO voluntario
│
└── infrastructure/
    ├── config/
    │   ├── BeanConfiguration.java
    │   └── SecurityConfig.java             # .hasRole("GESTOR_CANDIDATURAS") en /api/v1/candidatos/**
    └── adapter/
        ├── in/
        │   └── rest/
        │       └── candidato/
        │           ├── dto/
        │           │   ├── CandidatoRequest.java
        │           │   ├── BajaRequest.java
        │           │   └── CandidatoResponse.java
        │           ├── mapper/
        │           │   └── CandidatoDtoMapper.java
        │           └── CandidatoController.java
        ├── out/
        │   ├── persistence/
        │   │   └── candidato/
        │   │       ├── CandidatoEntity.java
        │   │       ├── CandidatoPersistenceMapper.java
        │   │       ├── CandidatoReactiveRepository.java
        │   │       └── CandidatoRepositoryAdapter.java
        │   └── client/
        │       └── Modulo1Client.java      # WebClient: GET /users/{id}

src/main/resources/
└── db/migration/
    └── V2__crear_tabla_candidato.sql

src/test/java/com/safevoting/elecciones/
├── unit/
│   ├── domain/
│   │   └── model/candidato/
│   │       └── CandidatoTest.java
│   └── application/
│       └── candidato/
│           ├── RegistrarCandidatoUseCaseTest.java
│           ├── EditarCandidatoUseCaseTest.java
│           ├── DarBajaCandidatoUseCaseTest.java
│           └── CancelarCandidaturaUseCaseTest.java
└── integration/
    └── rest/
        └── candidato/
            └── CandidatoControllerIntegrationTest.java
```

**Structure Decision**: `Candidato` es entidad de dominio en `domain/model/candidato/`. El `Modulo1Client` usa WebClient
con circuit breaker y timeout 5s/10s. Las excepciones de Módulo 1 (404) se traducen a `UsuarioNoEncontradoException`.

---

## Phase 1: Foundational — Nueva entidad y modelo de dominio (Blocking Prerequisites)

**Purpose**: Crear la tabla, entidad, enumerados, excepciones y repositorio para `Candidato`. Integración básica con
Módulo 1.

**⚠️ CRITICAL**: Ningún user story de candidatos puede comenzar antes de esta fase. Requiere que el plan 001 (partidos)
esté completo.

- [ ] T001 Crear migración Flyway `V2__crear_tabla_candidato.sql`:
    - Tabla `candidato`:
        - `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
        - `usuario_id UUID NOT NULL`
        - `partido_id UUID NOT NULL REFERENCES partido(id)`
        - `lema VARCHAR(255)`
        - `foto_url VARCHAR(500)`
        - `estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'`
        - `motivo_baja TEXT`
        - `created_at TIMESTAMP NOT NULL DEFAULT NOW()`
        - `updated_at TIMESTAMP NOT NULL DEFAULT NOW()`
    - `CONSTRAINT chk_estado_candidato CHECK (estado IN ('ACTIVO','INACTIVO','SUSPENDIDO'))`
    - Índice único parcial:
      `CREATE UNIQUE INDEX idx_candidato_usuario_unique ON candidato(usuario_id) WHERE estado != 'SUSPENDIDO'`.
- [ ] T002 Crear enum `EstadoCandidato` en `domain/model/candidato/`:
    - `ACTIVO`, `INACTIVO`, `SUSPENDIDO`.
    - Métodos `esActivo()`, `esInactivo()`, `esSuspendido()`.
- [ ] T003 Crear entidad `Candidato` en `domain/model/candidato/`:
    - `@Builder`, campos: `id` (UUID), `usuarioId` (UUID), `partidoId` (UUID), `lema` (String, nullable), `fotoUrl` (
      String, nullable), `estado` (EstadoCandidato, default ACTIVO), `motivoBaja` (String, nullable).
    - Métodos de validación:
        - `validateUsuarioPartido()`: `usuarioId != null && partidoId != null`.
    - Método público `validateInfo()`: invoca `validateUsuarioPartido()`.
    - Métodos de mutación:
        - `editar(UUID nuevoPartidoId, String lema, String fotoUrl)`: actualiza campos editables.
        - `suspender(String motivo)`: valida `esActivo()` o `esInactivo()`. Si ya está `SUSPENDIDO` →
          `CandidatoYaSuspendidoException`. Asigna `estado = SUSPENDIDO`, `motivoBaja = motivo`.
        - `cancelar()`: valida `esActivo()`. Si no → `CandidatoYaInactivoException`. Asigna `estado = INACTIVO`.
        - `reactivar()`: valida `esInactivo()`. Si no → `CandidatoYaInactivoException`. Asigna `estado = ACTIVO`.
- [ ] T004 Crear excepciones de dominio nuevas:
    - `CandidatoNoEncontradoException(UUID id)` → errorCode `CANDIDATO_NO_ENCONTRADO`.
    - `CandidatoYaInactivoException(UUID id)` → errorCode `CANDIDATO_YA_INACTIVO`.
    - `CandidatoYaSuspendidoException(UUID id)` → errorCode `CANDIDATO_YA_SUSPENDIDO`.
    - `UsuarioYaCandidatoException(UUID usuarioId)` → errorCode `USUARIO_YA_CANDIDATO`.
    - `PartidoInhabilitadoException(UUID partidoId)` → errorCode `PARTIDO_INHABILITADO`.
    - `UsuarioNoEncontradoException(UUID usuarioId)` → errorCode `USUARIO_NO_ENCONTRADO`.
    - `MotivoBajaRequeridoException()` → errorCode `MOTIVO_BAJA_REQUERIDO`.
    - `CandidatoInscritoEnVotacionException(UUID id)` → errorCode `CANDIDATO_INSCRITO_EN_VOTACION`.
- [ ] T005 Crear puerto `CandidatoRepository` en `domain/repository/`:
    - `save(Candidato)` → `Mono<Candidato>`.
    - `findById(UUID id)` → `Mono<Candidato>`.
    - `findAll()` → `Flux<Candidato>`.
    - `existsByUsuarioId(UUID usuarioId)` → `Mono<Boolean>` (excluyendo SUSPENDIDO).
    - `update(Candidato)` → `Mono<Candidato>`.
- [ ] T006 Crear adaptador `CandidatoRepositoryAdapter` en `infrastructure/adapter/out/persistence/candidato/`:
    - Implementa el puerto usando `R2dbcEntityTemplate`.
    - `existsByUsuarioId`: `SELECT COUNT(*) FROM candidato WHERE usuario_id = :usuarioId AND estado != 'SUSPENDIDO'`.
    - `update`:
      `UPDATE candidato SET partido_id = :partidoId, lema = :lema, foto_url = :fotoUrl, estado = :estado, motivo_baja = :motivoBaja, updated_at = NOW() WHERE id = :id`.
- [ ] T007 Crear `Modulo1Client` en `infrastructure/adapter/out/client/`:
    - `WebClient` con base URL configurable (`modules.users.base-url`).
    - Método `getUsuario(UUID usuarioId)` → `Mono<UsuarioResponse>`:
        - `GET /api/v1/users/{usuarioId}` → si 200 retorna `UsuarioResponse`, si 404 retorna `Mono.empty()`.
    - Timeout conexión 5s, lectura 10s. Circuit breaker.
- [ ] T008 Modificar `GlobalExceptionHandler`:
    - Agregar mapeos para todas las excepciones nuevas:
        - `CandidatoNoEncontradoException` → 404
        - `CandidatoYaInactivoException` → 409
        - `CandidatoYaSuspendidoException` → 409
        - `UsuarioYaCandidatoException` → 409
        - `PartidoInhabilitadoException` → 422
        - `UsuarioNoEncontradoException` → 404
        - `MotivoBajaRequeridoException` → 422
        - `CandidatoInscritoEnVotacionException` → 409
- [ ] T009 Modificar `SecurityConfig`:
    - `/api/v1/candidatos/**` requiere autenticación y rol `GESTOR_CANDIDATURAS`.

**Checkpoint**: Modelo de `Candidato` completo. Cliente Módulo 1 funcional. Seguridad configurada.

---

## Phase 2: User Story 1 — Registrar candidato (Priority: P1)

**Goal**: El Gestor Candidaturas registra un candidato vinculando un usuario del Módulo 1 a un partido habilitado, con
lema y foto opcionales.

**Independent Test**: `POST /api/v1/candidatos` → 201. Usuario inexistente → 404. Partido inhabilitado → 422. Usuario ya
candidato en otro partido → 409.

### Tests for User Story 1

- [ ] T010 [P] [US1] Unit test `CandidatoTest`: construir con Builder + `validateInfo()` → OK. usuarioId null →
  `DatosInvalidosException`.
- [ ] T011 [P] [US1] Unit test `RegistrarCandidatoUseCaseTest`: mock Módulo 1 + partido repo → éxito. Módulo 1 retorna
  404 → `UsuarioNoEncontradoException`. Partido inhabilitado → `PartidoInhabilitadoException`. Usuario ya candidato →
  `UsuarioYaCandidatoException`.
- [ ] T012 [P] [US1] Integration test `CandidatoControllerIntegrationTest`: `POST /api/v1/candidatos` → 201.

### Implementation for User Story 1

- [ ] T013 [P] [US1] Crear `CandidatoRequest` DTO: `@NotNull UUID usuarioId`, `@NotNull UUID partidoId`, `String lema`,
  `String fotoBase64` (opcional).
- [ ] T014 [P] [US1] Crear `CandidatoResponse` DTO: `UUID id`, `UUID usuarioId`, `String nombreUsuario`,
  `UUID partidoId`, `String nombrePartido`, `String lema`, `String fotoUrl`, `String estado`, `String motivoBaja`.
- [ ] T015 [P] [US1] Crear `CandidatoDtoMapper` (MapStruct): `CandidatoRequest → Candidato` parcial.
  `Candidato → CandidatoResponse`.
- [ ] T016 [US1] Crear `RegistrarCandidatoUseCase` en `application/candidato/`:
    - Inyecta `CandidatoRepository`, `PartidoPoliticoRepository`, `Modulo1Client`, `ImageStorageService`.
    - Método `ejecutar(CandidatoRequest request)`:
        1. Validar usuario: `modulo1Client.getUsuario(request.usuarioId)`. Si vacío → `UsuarioNoEncontradoException`.
        2. Validar partido: buscar por id, verificar `esHabilitado()`. Si no → `PartidoInhabilitadoException`.
        3. Validar unicidad: `repository.existsByUsuarioId(request.usuarioId)`. Si true → `UsuarioYaCandidatoException`.
        4. Subir foto si aplica: `imageStorageService.upload(fotoBase64)`. Si falla → continuar sin foto.
        5. Construir `Candidato` con Builder, `validateInfo()`.
        6. `repository.save(candidato)`.
- [ ] T017 [US1] Crear `CandidatoController` en `infrastructure/adapter/in/rest/candidato/`:
    - `POST /api/v1/candidatos`: recibe `@Valid @RequestBody CandidatoRequest`, invoca use case, retorna `201` con
      `CandidatoResponse`.

**Checkpoint**: Registro de candidatos funcional con validación Módulo 1 y partido.

---

## Phase 3: User Story 2 — Listar candidatos (Priority: P2)

**Goal**: El Gestor Candidaturas consulta todos los candidatos registrados.

**Independent Test**: `GET /api/v1/candidatos` → 200 con lista. Sin candidatos → lista vacía.

### Tests for User Story 2

- [ ] T018 [P] [US2] Unit test `ListarCandidatosUseCaseTest`: repos con candidatos → lista. Repos vacío → lista vacía.
- [ ] T019 [P] [US2] Integration test: `GET /api/v1/candidatos` → 200.

### Implementation for User Story 2

- [ ] T020 [US2] Crear `ListarCandidatosUseCase` en `application/candidato/`:
    - Inyecta `CandidatoRepository`, `Modulo1Client`.
    - Método `ejecutar()`: `repository.findAll()`. Para cada candidato, enriquecer con `nombreUsuario` desde Módulo 1 (
      cache o batch).
    - Retorna `Flux<CandidatoResponse>`.
- [ ] T021 [US2] Agregar endpoint a `CandidatoController`:
    - `GET /api/v1/candidatos`: invoca use case, retorna `200`.
    - `GET /api/v1/candidatos/{id}`: búsqueda por id, retorna `200` o 404.

**Checkpoint**: Listado de candidatos funcional con datos enriquecidos del Módulo 1.

---

## Phase 4: User Story 3 — Editar candidato (Priority: P3)

**Goal**: El Gestor Candidaturas modifica lema, foto y partido de un candidato. No se puede cambiar de partido si está
inscrito en una votación.

**Independent Test**: `PUT /api/v1/candidatos/{id}` → 200. Candidato inexistente → 404. Cambio a partido inhabilitado →
422. Candidato inscrito en votación → 409.

### Tests for User Story 3

- [ ] T022 [P] [US3] Unit test `EditarCandidatoUseCaseTest`: edición exitosa. Partido inhabilitado →
  `PartidoInhabilitadoException`. Candidato no encontrado → `CandidatoNoEncontradoException`. Inscrito en votación →
  `CandidatoInscritoEnVotacionException`.
- [ ] T023 [P] [US3] Integration test: `PUT /api/v1/candidatos/{id}` → 200.

### Implementation for User Story 3

- [ ] T024 [US3] Crear `EditarCandidatoUseCase` en `application/candidato/`:
    - Inyecta `CandidatoRepository`, `PartidoPoliticoRepository`, `ImageStorageService`, `CandidaturaRepository`.
    - Método `ejecutar(UUID id, CandidatoRequest request)`:
        1. Buscar candidato por id. Si no → `CandidatoNoEncontradoException`.
        2. Si cambia partido: validar nuevo partido habilitado. Validar que candidato no esté inscrito en ninguna
           votación (`candidaturaRepository.findByCandidatoId(id)`). Si tiene → `CandidatoInscritoEnVotacionException`.
        3. Si hay nueva foto → subir. Si falla → mantener anterior.
        4. `candidato.editar(nuevoPartidoId, lema, fotoUrl)`.
        5. `repository.update(candidato)`.
- [ ] T025 [US3] Agregar endpoint a `CandidatoController`:
    - `PUT /api/v1/candidatos/{id}`: recibe `@Valid @RequestBody CandidatoRequest`, retorna `200`.

**Checkpoint**: Edición de candidatos funcional con validación de inscripción en votaciones.

---

## Phase 5: User Story 4 — Dar de baja candidato (Priority: P4)

**Goal**: El Gestor Candidaturas da de baja administrativa a un candidato con motivo obligatorio. Pasa a `SUSPENDIDO`.
No se permite si el candidato tiene candidaturas en votación `EN_PROGRESO`.

**Independent Test**: `PATCH /api/v1/candidatos/{id}/baja` con motivo → 200. Sin motivo → 422. Candidato ya suspendido →
409. Con candidaturas en EN_PROGRESO → 409.

### Tests for User Story 4

- [ ] T026 [P] [US4] Unit test `DarBajaCandidatoUseCaseTest`: suspensión exitosa con motivo. Sin motivo →
  `MotivoBajaRequeridoException`. Ya suspendido → `CandidatoYaSuspendidoException`. Con candidaturas en EN_PROGRESO →
  `CandidatoInscritoEnVotacionException`.
- [ ] T027 [P] [US4] Unit test: verificar que `suspender()` asigna estado SUSPENDIDO y motivo.

### Implementation for User Story 4

- [ ] T028 [P] [US4] Crear `BajaRequest` DTO: `@NotBlank @Size(min=10, max=500) String motivo`.
- [ ] T029 [US4] Crear `DarBajaCandidatoUseCase` en `application/candidato/`:
    - Inyecta `CandidatoRepository`, `CandidaturaRepository`.
    - Método `ejecutar(UUID candidatoId, String motivo)`:
        1. Buscar candidato por id. Si no → `CandidatoNoEncontradoException`.
        2. Validar `motivo != null && !motivo.isBlank()`. Si no → `MotivoBajaRequeridoException`.
        3. Validar que el candidato no tenga candidaturas en votación `EN_PROGRESO`.
        4. `candidato.suspender(motivo)` → `repository.update(candidato)`.
- [ ] T030 [US4] Agregar endpoint a `CandidatoController`:
    - `PATCH /api/v1/candidatos/{id}/baja`: recibe `@Valid @RequestBody BajaRequest`, retorna `200`.

**Checkpoint**: Baja administrativa funcional con motivo y validación de votaciones activas.

---

## Phase 6: User Story 5 — Cancelar candidatura (Priority: P4)

**Goal**: El Gestor Candidaturas cancela voluntariamente la candidatura. Pasa a `INACTIVO`. No requiere motivo. El
candidato puede reactivarse para otros procesos.

**Independent Test**: `PATCH /api/v1/candidatos/{id}/cancelar` → 200. Ya inactivo → 409.

### Tests for User Story 5

- [ ] T031 [P] [US5] Unit test `CancelarCandidaturaUseCaseTest`: cancelación exitosa. Ya inactivo →
  `CandidatoYaInactivoException`. Suspendido intenta cancelar → error.
- [ ] T032 [P] [US5] Integration test: `PATCH /api/v1/candidatos/{id}/cancelar` → 200.

### Implementation for User Story 5

- [ ] T033 [US5] Crear `CancelarCandidaturaUseCase` en `application/candidato/`:
    - Inyecta `CandidatoRepository`.
    - Método `ejecutar(UUID candidatoId)`:
        1. Buscar candidato por id. Si no → `CandidatoNoEncontradoException`.
        2. `candidato.cancelar()` (valida internamente `esActivo()`).
        3. `repository.update(candidato)`.
- [ ] T034 [US5] Agregar endpoint a `CandidatoController`:
    - `PATCH /api/v1/candidatos/{id}/cancelar`: invoca use case, retorna `200`.

**Checkpoint**: Cancelación voluntaria funcional.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Foundational (Phase 1)**: Requiere que el plan 001 (partidos) esté completo. BLOQUEA todos los user stories.
- **US1 — Registrar (Phase 2)**: Depende de Phase 1.
- **US2 — Listar (Phase 3)**: Depende de Phase 1.
- **US3 — Editar (Phase 4)**: Depende de Phase 1. Requiere `CandidaturaRepository` del plan 004.
- **US4 — Baja (Phase 5)**: Depende de Phase 1. Requiere `CandidaturaRepository`.
- **US5 — Cancelar (Phase 6)**: Depende de Phase 1.

### User Story Dependencies

- **US1 (P1)**: Solo depende de Foundational.
- **US2 (P2)**: Solo depende de Foundational.
- **US3 (P3)**: Solo depende de Foundational.
- **US4 (P4)**: Solo depende de Foundational.
- **US5 (P4)**: Solo depende de Foundational.
- Todos los user stories pueden implementarse en paralelo una vez completada la Phase 1.

### Within Each User Story

- DTOs y mapper primero.
- Caso de uso antes que controlador.
- Tests unitarios de dominio y caso de uso, luego tests de integración del endpoint.

---

## Notes

- **Módulo 1 Client**: usar `WebClient` con filtro de error para mapear 404 → `Mono.empty()`. Configurar
  `spring.cloud.circuitbreaker` o Resilience4j para fallos del Módulo 1.
- **Índice único parcial**: PostgreSQL no soporta `UNIQUE WHERE` nativamente con R2DBC de forma sencilla. Alternativa:
  validar en aplicación antes de INSERT. O crear un índice único funcional:
  `CREATE UNIQUE INDEX ON candidato (usuario_id, (CASE WHEN estado != 'SUSPENDIDO' THEN 1 END))`.
- **Candidaturas EN_PROGRESO**: la validación en US4 (baja) requiere cruzar `candidato`, `candidatura` y `votacion`.
  Esto se puede hacer con un JOIN en un método del repositorio de candidaturas:
  `existsByCandidatoIdAndVotacionEstado(UUID candidatoId, EstadoVotacion EN_PROGRESO)`.
- **Enriquecimiento Módulo 1 en US2**: para evitar N+1, usar caché local (Caffeine, TTL 60s) o un batch
  `GET /api/v1/users?ids=...` si el Módulo 1 lo expone.
