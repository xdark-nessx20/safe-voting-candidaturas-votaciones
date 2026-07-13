# Implementation Plan: Vista del Tarjetón

**Date**: 2026-06-19
**Spec**: [004-vista-tarjeton.md](../spec/004-vista-tarjeton.md)

## Summary

Implementar la gestión de candidaturas (inscripción y cancelación de miembros de partido como candidatos en votaciones)
y la consulta pública del tarjetón digital. La inscripción/cancelación es exclusiva del rol `GESTOR_ELECTORAL`. La
consulta del tarjetón está disponible para cualquier usuario autenticado. Las candidaturas solo pueden modificarse con la
votación en estado `ACTIVA`. Cuando un partido es inhabilitado, todas sus candidaturas pasan a `CANCELADA`. El orden en el
tarjetón es por fecha de inscripción por defecto.

**Technical approach**: Actualización de la entidad `Candidatura` que ahora referencia `MiembroPartido` (plan 002) en vez
del antiguo `Candidato`. La candidatura vincula `MiembroPartido ↔ Votacion` y lleva `partidoId` como redundancia
controlada para facilitar consultas. Casos de uso en `application/candidatura/`. Endpoints REST bajo
`/api/v1/candidaturas` (gestor) y `/api/v1/votaciones/{id}/tarjeton` (público). La inhabilitación de partido dispara una
cancelación en cascada. **El nombre y datos del candidato en el tarjetón se obtienen directamente del snapshot en
`MiembroPartido`, sin necesidad de llamar al Módulo 1.**

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 3.x (WebFlux), Spring Data R2DBC, Flyway, jjwt, Lombok, MapStruct,
springdoc-openapi, Spring Boot Actuator, Bean Validation
**Storage**: PostgreSQL 16 (via R2DBC + DatabaseClient para queries con JOIN)
**Testing**: JUnit 5, Mockito, WebTestClient, Testcontainers
**Target Platform**: Linux server (Docker)
**Project Type**: Microservicio REST reactivo — extensión de los planes 001, 002 (MiembrosPartido) y 003
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
│   │       ├── Candidatura.java            # Builder + miembroPartidoId, partidoId, votacionId, fecha_inscripcion
│   │       └── EstadoCandidatura.java      # ACTIVA, CANCELADA
│   ├── exception/
│   │   └── candidatura/
│   │       ├── CandidaturaNoEncontradaException.java
│   │       ├── CandidaturaYaCanceladaException.java
│   │       ├── VotacionNoActivaException.java
│   │       ├── MiembroNoActivoException.java
│   │       └── PartidoInhabilitadoException.java
│   └── repository/
│       ├── CandidaturaRepository.java      # Puerto
│       ├── VotacionRepository.java         # Para validar estado ACTIVA
│       └── MiembroPartidoRepository.java     # Para validar estado ACTIVO (del plan 002)
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

**Structure Decision**: `Candidatura` como entidad intermedia entre `MiembroPartido` (plan 002) y `Votacion` (plan 003).
Lleva `partidoId` como redundancia controlada para facilitar consultas sin JOIN adicional al miembro. La consulta del
tarjetón usa `DatabaseClient` con JOIN entre `candidatura`, `miembro_partido` y `partido`. **Los nombres y datos de los
candidatos vienen del snapshot en `MiembroPartido`** — no se requiere llamada al Módulo 1. Controlador separado:
operaciones de escritura bajo `/api/v1/candidaturas` (GESTOR_ELECTORAL), lectura bajo
`/api/v1/votaciones/{id}/tarjeton` (authenticated).

---

## Phase 1: Foundational — Nueva entidad y modelo de dominio (Blocking Prerequisites)

**Purpose**: Crear la tabla, entidad, enumerados, excepciones y repositorio para `Candidatura`.

**⚠️ CRITICAL**: Ningún user story de candidaturas puede comenzar antes de esta fase. Requiere planes 001 (partidos),
002 (MiembrosPartido) y 003 (votaciones) completos.

- [ ] T001 Crear migración Flyway `V5__crear_tabla_candidatura.sql` (renumerada por V3 y V4 del plan 002):
    - Tabla `candidatura`:
        - `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
        - `miembro_partido_id UUID NOT NULL REFERENCES miembro_partido(id)`
        - `partido_id UUID NOT NULL REFERENCES partidos(id)` (redundancia controlada)
        - `votacion_id UUID NOT NULL REFERENCES votacion(id)`
        - `fecha_inscripcion TIMESTAMP NOT NULL DEFAULT NOW()`
        - `estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA'`
        - `created_at TIMESTAMP NOT NULL DEFAULT NOW()`
    - `CONSTRAINT chk_estado_candidatura CHECK (estado IN ('ACTIVA','CANCELADA'))`
    - `UNIQUE(miembro_partido_id, votacion_id)`
    - Índice en `(votacion_id, estado)` para consulta del tarjetón.
    - Índice en `(miembro_partido_id)` para búsqueda de candidaturas por miembro.
    - Índice en `(partido_id, estado)` para inhabilitación de partido en cascada.
- [ ] T002 Crear enum `EstadoCandidatura` en `domain/model/candidatura/`:
    - `ACTIVA`, `CANCELADA`. Método `esActiva()`.
- [ ] T003 Crear entidad `Candidatura` en `domain/model/candidatura/`:
    - `@Builder`, campos: `id` (UUID), `miembroPartidoId` (UUID), `partidoId` (UUID, redundancia controlada), `votacionId` (UUID), `fechaInscripcion` (Instant, default
      NOW()), `estado` (EstadoCandidatura, default ACTIVA).
    - Métodos de validación:
        - `validate()`: `miembroPartidoId != null && partidoId != null && votacionId != null`.
    - Método público `validateInfo()`: invoca `validate()`.
    - Métodos de mutación:
        - `cancelar()`: valida `esActiva()`. Si no → `CandidaturaYaCanceladaException`. Asigna `estado = CANCELADA`.
- [ ] T004 Crear excepciones de dominio nuevas:
    - `CandidaturaNoEncontradaException(UUID id)` → 404.
    - `CandidaturaYaCanceladaException(UUID id)` → 409.
    - `VotacionNoActivaException(UUID votacionId)` → 409.
    - `MiembroNoActivoException(UUID miembroPartidoId)` → 422.
    - `PartidoInhabilitadoException(UUID partidoId)` → 422.
    - `PartidoNoCoincideException(UUID miembroPartidoId, UUID partidoId)` → 422 (el partidoId de la candidatura no coincide con el del MiembroPartido).
- [ ] T005 Crear puerto `CandidaturaRepository` en `domain/repository/`:
    - `save(Candidatura)` → `Mono<Candidatura>`.
    - `findById(UUID id)` → `Mono<Candidatura>`.
    - `findActivasByVotacionId(UUID votacionId)` → `Flux<Candidatura>`.
    - `countActivasByVotacionId(UUID votacionId)` → `Mono<Long>`.
    - `findByMiembroPartidoId(UUID miembroPartidoId)` → `Flux<Candidatura>`.
    - `findActivasByPartidoId(UUID partidoId)` → `Flux<Candidatura>`. # Usa partidoId redundante (sin JOIN)
    - `findActivasByMiembroId(UUID miembroPartidoId)` → `Flux<Candidatura>`. # Para validar baja de miembro
    - `cancelarByPartidoId(UUID partidoId)` → `Mono<Long>`. # UPDATE en cascada
    - `update(Candidatura)` → `Mono<Candidatura>`.
- [ ] T006 Crear adaptador `CandidaturaRepositoryAdapter` en `infrastructure/adapter/out/persistence/candidatura/`:
    - `save`, `findById`, `update` con `R2dbcEntityTemplate`.
    - `findActivasByVotacionId`: `SELECT * FROM candidatura WHERE votacion_id = :id AND estado = 'ACTIVA'`.
    - `countActivasByVotacionId`: `SELECT COUNT(*) FROM candidatura WHERE votacion_id = :id AND estado = 'ACTIVA'`.
    - `findActivasByPartidoId`:
      `SELECT * FROM candidatura WHERE partido_id = :partidoId AND estado = 'ACTIVA'` (gracias al partidoId redundante, sin JOIN).
    - `findActivasByMiembroId`:
      `SELECT * FROM candidatura WHERE miembro_partido_id = :miembroPartidoId AND estado = 'ACTIVA'`.
    - `cancelarByPartidoId`:
      `UPDATE candidatura SET estado = 'CANCELADA' WHERE partido_id = :partidoId AND estado = 'ACTIVA'`.
- [ ] T007 Modificar `GlobalExceptionHandler`:
    - Mapear todas las excepciones nuevas.
- [ ] T008 Modificar `SecurityConfig`:
    - `POST /api/v1/candidaturas` → `hasRole("GESTOR_ELECTORAL")`.
    - `PATCH /api/v1/candidaturas/{id}/cancelar` → `hasRole("GESTOR_ELECTORAL")`.
    - `GET /api/v1/votaciones/{id}/tarjeton` → `authenticated()` (cualquier rol autenticado).

**Checkpoint**: Modelo de `Candidatura` completo. Repositorio con queries JOIN. Seguridad dual (gestor/autenticado).

---

## Phase 2: User Story 1 — Inscribir candidatura (Priority: P1)

**Goal**: El Gestor Electoral inscribe un MiembroPartido como candidato en una votación, creando una candidatura. Solo en
votación `ACTIVA`, miembro `ACTIVO` y partido `HABILITADO`. El `partidoId` de la candidatura debe coincidir con el del
`MiembroPartido`.

**Independent Test**: `POST /api/v1/candidaturas` → 201. Votación no ACTIVA → 409. Miembro inactivo → 422. Partido
inhabilitado → 422. PartidoId no coincide con el del miembro → 422. Candidatura duplicada (mismo miembro+votación) → 409.

### Tests for User Story 1

- [ ] T009 [P] [US1] Unit test `CandidaturaTest`: construir + `validateInfo()` → OK. `cancelar()` sobre ACTIVA → OK.
  `cancelar()` sobre CANCELADA → `CandidaturaYaCanceladaException`.
- [ ] T010 [P] [US1] Unit test `InscribirCandidaturaUseCaseTest`: inscripción exitosa. Votación no ACTIVA →
  `VotacionNoActivaException`. Miembro INACTIVO → `MiembroNoActivoException`. Partido INHABILITADO →
  `PartidoInhabilitadoException`. PartidoId no coincide con miembro → `PartidoNoCoincideException`.
- [ ] T011 [P] [US1] Integration test: `POST /api/v1/candidaturas` → 201.

### Implementation for User Story 1

- [ ] T012 [P] [US1] Crear `InscripcionRequest` DTO: `@NotNull UUID miembroPartidoId`, `@NotNull UUID votacionId`.
- [ ] T013 [P] [US1] Crear `TarjetonResponse` DTO (usado también en US3): `UUID id`, `UUID miembroPartidoId`,
  `String nombreCandidato`, `String documentoIdentidad`, `String nombrePartido`, `String logoPartido`, `String fotoUrl`,
  `UUID votacionId`, `Instant fechaInscripcion`, `String estado`.
- [ ] T014 [P] [US1] Crear `CandidaturaDtoMapper` (MapStruct).
- [ ] T015 [US1] Crear `InscribirCandidaturaUseCase` en `application/candidatura/`:
    - Inyecta `CandidaturaRepository`, `VotacionRepository`, `MiembroPartidoRepository`, `PartidoPoliticoRepository`.
    - Método `ejecutar(UUID miembroPartidoId, UUID votacionId)`:
        1. Buscar votación. Si no → `VotacionNoEncontradaException`. Si `!votacion.getEstado().esActiva()` →
           `VotacionNoActivaException`.
        2. Buscar MiembroPartido. Si no → `MiembroNoEncontradoException`. Si no está ACTIVO → `MiembroNoActivoException`.
        3. Validar que `partidoId` del miembro coincida con el `partidoId` de la candidatura (si se envía) → si no coincide → `PartidoNoCoincideException`.
        4. Buscar partido del miembro. Si `!partido.esHabilitado()` → `PartidoInhabilitadoException`.
        5. Construir `Candidatura` con `miembroPartidoId`, `partidoId` (del miembro), `votacionId`. `validateInfo()`.
        6. `repository.save(candidatura)`. La constraint UNIQUE en BD maneja duplicados → `DataIntegrityViolationException` → traducir a 409.
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
nombre, documento, partido y foto. Visible en cualquier estado de votación. **Los datos de identidad se obtienen
directamente del snapshot en `MiembroPartido`, sin llamadas HTTP al Módulo 1.**

**Independent Test**: `GET /api/v1/votaciones/{id}/tarjeton` → 200 con lista de candidaturas ACTIVAS. Sin candidaturas →
lista vacía. Usuario no autenticado → 401.

### Tests for User Story 3

- [ ] T021 [P] [US3] Unit test `ListarCandidaturasPorVotacionUseCaseTest`: tarjetón con candidaturas ACTIVAS → lista
  ordenada por fecha_inscripcion. Candidaturas CANCELADAS excluidas. Sin candidaturas → lista vacía.
- [ ] T022 [P] [US3] Integration test: `GET /api/v1/votaciones/{id}/tarjeton` → 200 con datos del snapshot de
  MiembroPartido (nombre, documento, foto).

### Implementation for User Story 3

- [ ] T023 [US3] Crear `ListarCandidaturasPorVotacionUseCase`:
    - Inyecta `CandidaturaRepository`.
    - Método `ejecutar(UUID votacionId)`:
        1. Usar `DatabaseClient` con JOIN para obtener todos los datos en una sola query:
           ```sql
           SELECT c.id, c.estado, c.fecha_inscripcion,
                  mp.nombre_completo, mp.documento_identidad, mp.foto_url,
                  p.nombre AS nombre_partido, p.logo_url AS logo_partido
           FROM candidatura c
           JOIN miembro_partido mp ON c.miembro_partido_id = mp.id
           JOIN partidos p ON c.partido_id = p.id
           WHERE c.votacion_id = :votacionId AND c.estado = 'ACTIVA'
           ORDER BY c.fecha_inscripcion ASC
           ```
        2. Mapear directamente a `TarjetonResponse` (sin N+1, sin llamadas externas).
        3. Retornar `Flux<TarjetonResponse>`.
- [ ] T024 [US3] Agregar endpoint a `CandidaturaController` (o directamente en `VotacionController`):
    - `GET /api/v1/votaciones/{id}/tarjeton`: invoca use case, retorna `200` con `List<TarjetonResponse>`.

**Checkpoint**: Tarjetón digital funcional con datos locales — sin dependencia del Módulo 1 en consultas de lectura.

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
- **US3 — Tarjetón (Phase 4)**: Depende de Phase 1. Todos los datos vienen del snapshot en MiembroPartido — no requiere Módulo 1.
- **Phase 5 — Cascada**: Depende de Phase 1 + plan 001 US4 (InhabilitarPartidoUseCase).

### User Story Dependencies

- **US1 (P1)**: Solo Foundational.
- **US2 (P2)**: Solo Foundational.
- **US3 (P2)**: Solo Foundational.
- Los tres user stories pueden implementarse en paralelo.

---

## Notes

- **Tarjetón sin dependencia del Módulo 1**: gracias al snapshot en `MiembroPartido`, los datos de identidad del
  candidato (nombre, documento, lugar de inscripción) están disponibles localmente. Una sola query con JOIN entre
  `candidatura`, `miembro_partido` y `partidos` resuelve todo el tarjetón sin llamadas HTTP externas.
- **partidoId redundante en Candidatura**: permite queries directas sin JOIN a `miembro_partido` para casos como
  "cancelar todas las candidaturas de un partido inhabilitado" o "listar candidaturas de un partido". La redundancia es
  controlada (el valor se toma del MiembroPartido al crear la candidatura y no cambia).
- **Constraint UNIQUE**: PostgreSQL maneja la restricción de duplicados (mismo miembro en misma votación:
  `UNIQUE(miembro_partido_id, votacion_id)`). El adaptador debe mapear `DataIntegrityViolationException` → 409.
- **Orden en tarjetón**: por defecto por `fecha_inscripcion ASC`. Si se requiere orden personalizado en el futuro,
  agregar un campo `orden INTEGER` a `candidatura` y un endpoint `PATCH /api/v1/votaciones/{id}/candidaturas/orden`.
