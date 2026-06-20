# Implementation Plan: Vista del Tarjetón

**Date**: 2026-06-19
**Spec**: [004-vista-tarjeton.md](../spec/004-vista-tarjeton.md)

## Summary

Implementar la gestión de candidaturas (inscripción y cancelación de candidatos en votaciones) y la consulta pública del
tarjetón digital. La inscripción/cancelación es exclusiva del rol `GESTOR_ELECTORAL`. La consulta del tarjetón está
disponible para cualquier usuario autenticado. Las candidaturas solo pueden modificarse con la votación en estado
`ACTIVA`. Cuando un partido es inhabilitado, todas sus candidaturas pasan a `CANCELADA`. El orden en el tarjetón es por
fecha de inscripción por defecto.

**Technical approach**: Nueva entidad de dominio `Candidatura` que vincula `Candidato ↔ Votacion`. Casos de uso en
`application/candidatura/`. Endpoints REST bajo `/api/v1/candidaturas` (gestor) y `/api/v1/votaciones/{id}/tarjeton` (
público). La inhabilitación de partido dispara una cancelación en cascada. El tarjetón enriquece datos del Módulo 1 (
nombres de candidatos).

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 3.x (WebFlux), Spring Data R2DBC, Flyway, jjwt, Lombok, MapStruct,
springdoc-openapi, Spring Boot Actuator, Bean Validation
**Storage**: PostgreSQL 16 (via R2DBC + DatabaseClient para queries con JOIN)
**Testing**: JUnit 5, Mockito, WebTestClient, Testcontainers
**Target Platform**: Linux server (Docker)
**Project Type**: Microservicio REST reactivo — extensión de los planes 001, 002 y 003
**Performance Goals**: <500ms p95 en consulta del tarjetón con 50+ candidatos
**Constraints**: Candidaturas solo editables en votación ACTIVA. Inhabilitación de partido cancela candidaturas en
cascada. Tarjetón visible en cualquier estado de votación.
**Scale/Scope**: ~200 candidaturas por votación

## Project Structure

### Documentation (this feature)

```text
docs/
├── plan/
│   └── 004-vista-tarjeton.md               # Este archivo
├── spec/
│   └── 004-vista-tarjeton.md
├── guia-uso-modulo-2.md
└── stakeholders.md
```

### Source Code (repository root — nuevo y modificado)

```text
src/main/java/com/safevoting/elecciones/
│
├── domain/
│   ├── model/
│   │   └── candidatura/
│   │       ├── Candidatura.java            # Builder + candidato_id, votacion_id, fecha_inscripcion
│   │       └── EstadoCandidatura.java      # ACTIVA, CANCELADA
│   ├── exception/
│   │   └── candidatura/
│   │       ├── CandidaturaNoEncontradaException.java
│   │       ├── CandidaturaYaCanceladaException.java
│   │       ├── VotacionNoActivaException.java
│   │       ├── CandidatoNoActivoException.java
│   │       └── PartidoInhabilitadoException.java
│   └── repository/
│       ├── CandidaturaRepository.java      # Puerto
│       ├── VotacionRepository.java         # Para validar estado ACTIVA
│       └── CandidatoRepository.java        # Para validar estado ACTIVO
│
├── application/
│   └── candidatura/
│       ├── InscribirCandidaturaUseCase.java
│       ├── CancelarCandidaturaUseCase.java
│       ├── ListarCandidaturasPorVotacionUseCase.java   # Tarjetón
│       └── CancelarCandidaturasPorPartidoUseCase.java  # Cascada
│
└── infrastructure/
    ├── config/
    │   ├── BeanConfiguration.java
    │   └── SecurityConfig.java
    │       # POST/PATCH /candidaturas → .hasRole("GESTOR_ELECTORAL")
    │       # GET /votaciones/{id}/tarjeton → .authenticated()
    └── adapter/
        ├── in/
        │   └── rest/
        │       └── candidatura/
        │           ├── dto/
        │           │   ├── InscripcionRequest.java
        │           │   └── TarjetonResponse.java
        │           ├── mapper/
        │           │   └── CandidaturaDtoMapper.java
        │           └── CandidaturaController.java
        └── out/
            └── persistence/
                └── candidatura/
                    ├── CandidaturaEntity.java
                    ├── CandidaturaPersistenceMapper.java
                    ├── CandidaturaReactiveRepository.java
                    └── CandidaturaRepositoryAdapter.java

src/main/resources/
└── db/migration/
    └── V4__crear_tabla_candidatura.sql

src/test/java/com/safevoting/elecciones/
├── unit/
│   ├── domain/
│   │   └── model/candidatura/
│   │       └── CandidaturaTest.java
│   └── application/
│       └── candidatura/
│           ├── InscribirCandidaturaUseCaseTest.java
│           ├── CancelarCandidaturaUseCaseTest.java
│           ├── ListarCandidaturasPorVotacionUseCaseTest.java
│           └── CancelarCandidaturasPorPartidoUseCaseTest.java
└── integration/
    └── rest/
        └── candidatura/
            └── CandidaturaControllerIntegrationTest.java
```

**Structure Decision**: `Candidatura` como entidad intermedia. La consulta del tarjetón usa `DatabaseClient` con JOIN
entre `candidatura`, `candidato` y `partido`, más enriquecimiento con `Modulo1Client` para nombres de usuario.
Controlador separado: operaciones de escritura bajo `/api/v1/candidaturas` (GESTOR_ELECTORAL), lectura bajo
`/api/v1/votaciones/{id}/tarjeton` (authenticated).

---

## Phase 1: Foundational — Nueva entidad y modelo de dominio (Blocking Prerequisites)

**Purpose**: Crear la tabla, entidad, enumerados, excepciones y repositorio para `Candidatura`.

**⚠️ CRITICAL**: Ningún user story de candidaturas puede comenzar antes de esta fase. Requiere planes 001 (partidos),
002 (candidatos) y 003 (votaciones) completos.

- [ ] T001 Crear migración Flyway `V4__crear_tabla_candidatura.sql`:
    - Tabla `candidatura`:
        - `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
        - `candidato_id UUID NOT NULL REFERENCES candidato(id)`
        - `votacion_id UUID NOT NULL REFERENCES votacion(id)`
        - `fecha_inscripcion TIMESTAMP NOT NULL DEFAULT NOW()`
        - `estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA'`
        - `created_at TIMESTAMP NOT NULL DEFAULT NOW()`
    - `CONSTRAINT chk_estado_candidatura CHECK (estado IN ('ACTIVA','CANCELADA'))`
    - `UNIQUE(candidato_id, votacion_id)`
    - Índice en `(votacion_id, estado)` para consulta del tarjetón.
    - Índice en `(candidato_id)` para búsqueda de candidaturas por candidato.
- [ ] T002 Crear enum `EstadoCandidatura` en `domain/model/candidatura/`:
    - `ACTIVA`, `CANCELADA`. Método `esActiva()`.
- [ ] T003 Crear entidad `Candidatura` en `domain/model/candidatura/`:
    - `@Builder`, campos: `id` (UUID), `candidatoId` (UUID), `votacionId` (UUID), `fechaInscripcion` (Instant, default
      NOW()), `estado` (EstadoCandidatura, default ACTIVA).
    - Métodos de validación:
        - `validate()`: `candidatoId != null && votacionId != null`.
    - Método público `validateInfo()`: invoca `validate()`.
    - Métodos de mutación:
        - `cancelar()`: valida `esActiva()`. Si no → `CandidaturaYaCanceladaException`. Asigna `estado = CANCELADA`.
- [ ] T004 Crear excepciones de dominio nuevas:
    - `CandidaturaNoEncontradaException(UUID id)` → 404.
    - `CandidaturaYaCanceladaException(UUID id)` → 409.
    - `VotacionNoActivaException(UUID votacionId)` → 409.
    - `CandidatoNoActivoException(UUID candidatoId)` → 422.
    - `PartidoInhabilitadoException(UUID partidoId)` → 422.
- [ ] T005 Crear puerto `CandidaturaRepository` en `domain/repository/`:
    - `save(Candidatura)` → `Mono<Candidatura>`.
    - `findById(UUID id)` → `Mono<Candidatura>`.
    - `findActivasByVotacionId(UUID votacionId)` → `Flux<Candidatura>`.
    - `countActivasByVotacionId(UUID votacionId)` → `Mono<Long>`.
    - `findByCandidatoId(UUID candidatoId)` → `Flux<Candidatura>`.
    - `findActivasByPartidoId(UUID partidoId)` → `Flux<Candidatura>`. # JOIN candidatura + candidato
    - `cancelarByPartidoId(UUID partidoId)` → `Mono<Long>`. # UPDATE en cascada
    - `update(Candidatura)` → `Mono<Candidatura>`.
- [ ] T006 Crear adaptador `CandidaturaRepositoryAdapter` en `infrastructure/adapter/out/persistence/candidatura/`:
    - `save`, `findById`, `update` con `R2dbcEntityTemplate`.
    - `findActivasByVotacionId`: `SELECT * FROM candidatura WHERE votacion_id = :id AND estado = 'ACTIVA'`.
    - `countActivasByVotacionId`: `SELECT COUNT(*) FROM candidatura WHERE votacion_id = :id AND estado = 'ACTIVA'`.
    - `findActivasByPartidoId`:
      `SELECT c.* FROM candidatura c JOIN candidato ca ON c.candidato_id = ca.id WHERE ca.partido_id = :partidoId AND c.estado = 'ACTIVA'` (
      usar `DatabaseClient`).
    - `cancelarByPartidoId`:
      `UPDATE candidatura SET estado = 'CANCELADA' WHERE candidato_id IN (SELECT id FROM candidato WHERE partido_id = :partidoId) AND estado = 'ACTIVA'`.
- [ ] T007 Modificar `GlobalExceptionHandler`:
    - Mapear todas las excepciones nuevas.
- [ ] T008 Modificar `SecurityConfig`:
    - `POST /api/v1/candidaturas` → `hasRole("GESTOR_ELECTORAL")`.
    - `PATCH /api/v1/candidaturas/{id}/cancelar` → `hasRole("GESTOR_ELECTORAL")`.
    - `GET /api/v1/votaciones/{id}/tarjeton` → `authenticated()` (cualquier rol autenticado).

**Checkpoint**: Modelo de `Candidatura` completo. Repositorio con queries JOIN. Seguridad dual (gestor/autenticado).

---

## Phase 2: User Story 1 — Inscribir candidatura (Priority: P1)

**Goal**: El Gestor Electoral inscribe un candidato en una votación, creando una candidatura. Solo en votación `ACTIVA`,
candidato `ACTIVO` y partido `HABILITADO`.

**Independent Test**: `POST /api/v1/candidaturas` → 201. Votación no ACTIVA → 409. Candidato inactivo → 422. Partido
inhabilitado → 422. Candidatura duplicada (mismo candidato+votación) → 409.

### Tests for User Story 1

- [ ] T009 [P] [US1] Unit test `CandidaturaTest`: construir + `validateInfo()` → OK. `cancelar()` sobre ACTIVA → OK.
  `cancelar()` sobre CANCELADA → `CandidaturaYaCanceladaException`.
- [ ] T010 [P] [US1] Unit test `InscribirCandidaturaUseCaseTest`: inscripción exitosa. Votación no ACTIVA →
  `VotacionNoActivaException`. Candidato SUSPENDIDO → `CandidatoNoActivoException`. Partido INHABILITADO →
  `PartidoInhabilitadoException`.
- [ ] T011 [P] [US1] Integration test: `POST /api/v1/candidaturas` → 201.

### Implementation for User Story 1

- [ ] T012 [P] [US1] Crear `InscripcionRequest` DTO: `@NotNull UUID candidatoId`, `@NotNull UUID votacionId`.
- [ ] T013 [P] [US1] Crear `TarjetonResponse` DTO (usado también en US3): `UUID id`, `UUID candidatoId`,
  `String nombreCandidato`, `String nombrePartido`, `String logoPartido`, `String lema`, `String fotoUrl`,
  `UUID votacionId`, `Instant fechaInscripcion`, `String estado`.
- [ ] T014 [P] [US1] Crear `CandidaturaDtoMapper` (MapStruct).
- [ ] T015 [US1] Crear `InscribirCandidaturaUseCase` en `application/candidatura/`:
    - Inyecta `CandidaturaRepository`, `VotacionRepository`, `CandidatoRepository`, `PartidoPoliticoRepository`.
    - Método `ejecutar(UUID candidatoId, UUID votacionId)`:
        1. Buscar votación. Si no → `VotacionNoEncontradaException`. Si `!votacion.getEstado().esActiva()` →
           `VotacionNoActivaException`.
        2. Buscar candidato. Si no → `CandidatoNoEncontradoException`. Si no está ACTIVO → `CandidatoNoActivoException`.
        3. Buscar partido del candidato. Si `!partido.esHabilitado()` → `PartidoInhabilitadoException`.
        4. Construir `Candidatura`, `validateInfo()`. La constraint UNIQUE en BD maneja duplicados →
           `DataIntegrityViolationException` → traducir a 409.
        5. `repository.save(candidatura)`.
- [ ] T016 [US1] Crear `CandidaturaController` en `infrastructure/adapter/in/rest/candidatura/`:
    - `POST /api/v1/candidaturas`: recibe `@Valid @RequestBody InscripcionRequest`, retorna `201`.

**Checkpoint**: Inscripción de candidaturas funcional con todas las validaciones.

---

## Phase 3: User Story 2 — Cancelar candidatura (Priority: P2)

**Goal**: El Gestor Electoral cancela una candidatura. Solo en votación `ACTIVA`.

**Independent Test**: `PATCH /api/v1/candidaturas/{id}/cancelar` → 200. Ya cancelada → 409. Votación no ACTIVA → 409.

### Tests for User Story 2

- [ ] T017 [P] [US2] Unit test `CancelarCandidaturaUseCaseTest`: cancelación exitosa. Ya cancelada →
  `CandidaturaYaCanceladaException`. Votación no ACTIVA → `VotacionNoActivaException`.
- [ ] T018 [P] [US2] Integration test: `PATCH /api/v1/candidaturas/{id}/cancelar` → 200.

### Implementation for User Story 2

- [ ] T019 [US2] Crear `CancelarCandidaturaUseCase`:
    - Inyecta `CandidaturaRepository`, `VotacionRepository`.
    - Método `ejecutar(UUID candidaturaId)`:
        1. Buscar candidatura. Si no → `CandidaturaNoEncontradaException`.
        2. Buscar votación asociada. Si `!votacion.getEstado().esActiva()` → `VotacionNoActivaException`.
        3. `candidatura.cancelar()` → `repository.update(candidatura)`.
- [ ] T020 [US2] Agregar endpoint a `CandidaturaController`:
    - `PATCH /api/v1/candidaturas/{id}/cancelar`: invoca use case, retorna `200`.

**Checkpoint**: Cancelación de candidaturas funcional.

---

## Phase 4: User Story 3 — Ver tarjetón digital (Priority: P2)

**Goal**: Cualquier usuario autenticado consulta el tarjetón digital de una votación, mostrando los candidatos con su
nombre, partido, lema y foto. Visible en cualquier estado de votación.

**Independent Test**: `GET /api/v1/votaciones/{id}/tarjeton` → 200 con lista de candidaturas ACTIVAS. Sin candidaturas →
lista vacía. Usuario no autenticado → 401.

### Tests for User Story 3

- [ ] T021 [P] [US3] Unit test `ListarCandidaturasPorVotacionUseCaseTest`: tarjetón con candidaturas ACTIVAS → lista
  ordenada por fecha_inscripcion. Candidaturas CANCELADAS excluidas. Sin candidaturas → lista vacía.
- [ ] T022 [P] [US3] Integration test: `GET /api/v1/votaciones/{id}/tarjeton` → 200 con datos enriquecidos (nombre
  candidato del Módulo 1).

### Implementation for User Story 3

- [ ] T023 [US3] Crear `ListarCandidaturasPorVotacionUseCase`:
    - Inyecta `CandidaturaRepository`, `Modulo1Client`.
    - Método `ejecutar(UUID votacionId)`:
        1. `repository.findActivasByVotacionId(votacionId)` → ordenadas por `fecha_inscripcion ASC`.
        2. Para cada candidatura, enriquecer con:
            - Datos del candidato (local: `candidatoRepository.findById`) → lema, foto.
            - Datos del partido (local: `partidoRepository.findById`) → nombre, logo.
            - Nombre del candidato (Módulo 1: `GET /users/{candidato.usuarioId}`) → nombre.
        3. Mapear a `TarjetonResponse`.
        4. Retornar `Flux<TarjetonResponse>`.
    - **Optimización**: para evitar N+1 al Módulo 1, recolectar todos los `usuarioId`, hacer batch o cachear.
- [ ] T024 [US3] Agregar endpoint a `CandidaturaController` (o directamente en `VotacionController`):
    - `GET /api/v1/votaciones/{id}/tarjeton`: invoca use case, retorna `200` con `List<TarjetonResponse>`.

**Checkpoint**: Tarjetón digital funcional con datos enriquecidos.

---

## Phase 5: Cascada — Cancelar candidaturas al inhabilitar partido

**Purpose**: Integración con `InhabilitarPartidoUseCase` del plan 001. Cuando un partido se inhabilita, todas sus
candidaturas ACTIVAS pasan a CANCELADA.

- [ ] T025 [US4] Crear `CancelarCandidaturasPorPartidoUseCase`:
    - Inyecta `CandidaturaRepository`.
    - Método `ejecutar(UUID partidoId)`:
        1. `repository.cancelarByPartidoId(partidoId)` (UPDATE atómico en BD).
        2. Retorna cantidad de candidaturas canceladas.
- [ ] T026 Modificar `InhabilitarPartidoUseCase` (plan 001, Phase 5) para invocar
  `CancelarCandidaturasPorPartidoUseCase` después de inhabilitar el partido, en la misma transacción lógica.

**Checkpoint**: Inhabilitación de partido cancela candidaturas en cascada.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Foundational (Phase 1)**: Requiere planes 001, 002 y 003 completos. BLOQUEA todos los user stories.
- **US1 — Inscribir (Phase 2)**: Depende de Phase 1.
- **US2 — Cancelar (Phase 3)**: Depende de Phase 1.
- **US3 — Tarjetón (Phase 4)**: Depende de Phase 1. Requiere `Modulo1Client` para enriquecimiento.
- **Phase 5 — Cascada**: Depende de Phase 1 + plan 001 US4 (InhabilitarPartidoUseCase).

### User Story Dependencies

- **US1 (P1)**: Solo Foundational.
- **US2 (P2)**: Solo Foundational.
- **US3 (P2)**: Solo Foundational.
- Los tres user stories pueden implementarse en paralelo.

---

## Notes

- **Enriquecimiento del tarjetón**: el nombre del candidato se obtiene del Módulo 1. Para el tarjetón (que puede tener
  50+ candidatos), hacer 50 llamadas HTTP es ineficiente. Opciones: (a) caché local con Caffeine TTL 60s, (b) pedir al
  equipo del Módulo 1 un endpoint batch `GET /api/v1/users?ids=...`, (c) aceptar latencia y usar reactivo para
  paralelizar.
- **Constraint UNIQUE**: PostgreSQL maneja la restricción de duplicados (mismo candidato en misma votación). El
  adaptador debe mapear `DataIntegrityViolationException` a una excepción de dominio 409.
- **Orden en tarjetón**: si se requiere orden personalizado en el futuro, agregar un campo `orden INTEGER` a
  `candidatura` y un endpoint `PATCH /api/v1/votaciones/{id}/orden`.
