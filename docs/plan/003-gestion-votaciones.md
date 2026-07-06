# Implementation Plan: Gestión de Votaciones

**Date**: 2026-06-19
**Spec**: [003-gestion-votaciones.md](../spec/003-gestion-votaciones.md)

## Summary

Implementar la configuración y control del ciclo de vida de las votaciones: crear, establecer fechas, abrir, cerrar, cancelar y completar. Acceso exclusivo para el rol `GESTOR_ELECTORAL`. Validar compatibilidad tipo ↔ alcance (PRESIDENCIA → NACIONAL, CONGRESO → NACIONAL, ALCALDIA → MUNICIPAL, GOBERNACION → DEPARTAMENTAL). El alcance del gestor limita el alcance de la votación. No se puede abrir sin candidatos asignados ni antes de `fecha_inicio`. Ciclo de vida con estados: `ACTIVA → EN_PROGRESO → FINALIZADA → COMPLETADA`, con `CANCELADA` reactivable. La transición `FINALIZADA → COMPLETADA` la dispara el Módulo 3 (Auditoría) al finalizar la revisión, consumiendo el endpoint de este módulo.

**Technical approach**: Nueva entidad de dominio `Votacion` con máquina de estados en el dominio. Casos de uso en `application/votacion/`. Endpoints REST bajo `/api/v1/votaciones`. El endpoint de completado es invocado por Módulo 3, no directamente por el Gestor Electoral — se expone con un rol de servicio interno. Integración con Módulo 1 para validar alcance del gestor (`GET /users/{uid}/alcance`).

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 3.x (WebFlux), Spring Data R2DBC, Flyway, jjwt, Lombok, MapStruct,
springdoc-openapi, Spring Boot Actuator, Bean Validation
**Storage**: PostgreSQL 16 (via R2DBC)
**Testing**: JUnit 5, Mockito, WebTestClient, Testcontainers
**Target Platform**: Linux server (Docker)
**Project Type**: Microservicio REST reactivo — extensión de los planes 001 y 002
**Performance Goals**: <300ms p95 en transiciones de estado
**Constraints**: Estado inicial ACTIVA. Nombre único e inmutable. Tipo y alcance inmutables. Fechas editables solo en
ACTIVA o CANCELADA. Sin candidatos → no se puede abrir. Cancelación y cancelación requieren motivo obligatorio.
**Scale/Scope**: ~20 votaciones activas simultáneas

## Project Structure

### Documentation (this feature)

```text
docs/
├── plan/
│   └── 003-gestion-votaciones.md           # Este archivo
├── spec/
│   └── 003-gestion-votaciones.md
├── guia-uso-modulo-2.md
└── stakeholders.md
```

### Source Code (repository root — nuevo y modificado)

```text
src/main/java/com/safevoting/elecciones/
│
├── domain/
│   ├── model/
│   │   └── votacion/
│   │       ├── Votacion.java              # Builder + máquina de estados
│   │       ├── EstadoVotacion.java        # ACTIVA, EN_PROGRESO, FINALIZADA, CANCELADA, COMPLETADA
│   │       ├── TipoVotacion.java          # PRESIDENCIA, CONGRESO, ALCALDIA, GOBERNACION
│   │       └── AlcanceVotacion.java        # MUNICIPAL, DEPARTAMENTAL, REGIONAL, NACIONAL
│   ├── exception/
│   │   └── votacion/
│   │       ├── VotacionNoEncontradaException.java
│   │       ├── NombreDuplicadoException.java
│   │       ├── TransicionEstadoInvalidaException.java
│   │       ├── TipoAlcanceIncompatibleException.java
│   │       ├── SinCandidatosException.java
│   │       ├── FechaInicioFuturaException.java
│   │       ├── MotivoRequeridoException.java
│   │       └── AlcanceExcedidoException.java
│   └── repository/
│       ├── VotacionRepository.java         # Puerto
│       └── CandidaturaRepository.java      # Para validar si hay candidatos antes de abrir
│
├── application/
│   └── votacion/
│       ├── CrearVotacionUseCase.java
│       ├── EstablecerFechasUseCase.java
│       ├── AbrirVotacionUseCase.java
│       ├── CerrarVotacionUseCase.java
│       ├── CancelarVotacionUseCase.java     # ACTIVA/EN_PROGRESO → CANCELADA (motivo)
│       └── CompletarVotacionUseCase.java    # FINALIZADA → COMPLETADA
│
└── infrastructure/
    ├── config/
    │   ├── BeanConfiguration.java
    │   └── SecurityConfig.java              # .hasRole("GESTOR_ELECTORAL") en /api/v1/votaciones/**, completar interno
    └── adapter/
        ├── in/
        │   └── rest/
        │       └── votacion/
        │           ├── dto/
        │           │   ├── VotacionRequest.java
        │           │   ├── FechasRequest.java
        │           │   ├── MotivoRequest.java
        │           │   └── VotacionResponse.java
        │           ├── mapper/
        │           │   └── VotacionDtoMapper.java
        │           └── VotacionController.java
        └── out/
            ├── persistence/
            │   └── votacion/
            │       ├── VotacionEntity.java
            │       ├── VotacionPersistenceMapper.java
            │       ├── VotacionReactiveRepository.java
            │       └── VotacionRepositoryAdapter.java
            └── client/
                └── Modulo1Client.java       # GET /users/{id}/alcance

src/main/resources/
└── db/migration/
    └── V3__crear_tabla_votacion.sql

src/test/java/com/safevoting/elecciones/
├── unit/
│   ├── domain/
│   │   └── model/votacion/
│   │       └── VotacionTest.java
│   └── application/
│       └── votacion/
│           ├── CrearVotacionUseCaseTest.java
│           ├── AbrirVotacionUseCaseTest.java
│           ├── CerrarVotacionUseCaseTest.java
│           ├── CancelarVotacionUseCaseTest.java
│           └── CompletarVotacionUseCaseTest.java
└── integration/
    └── rest/
        └── votacion/
            └── VotacionControllerIntegrationTest.java
```

**Structure Decision**: `Votacion` con máquina de estados propia: métodos `abrir()`, `cerrar()`, `cancelar(motivo)`,
`completar()` que validan la transición internamente y lanzan `TransicionEstadoInvalidaException` si no es permitida. El
`Modulo1Client` se extiende con `getAlcanceGestor(UUID uid)`.

---

## Phase 1: Foundational — Nueva entidad y modelo de dominio (Blocking Prerequisites)

**Purpose**: Crear la tabla, entidad, enumerados, excepciones y repositorio para `Votacion`. Integración con Módulo 1
para alcance del gestor.

**⚠️ CRITICAL**: Ningún user story de votaciones puede comenzar antes de esta fase.

- [ ] T001 Crear migración Flyway `V3__crear_tabla_votacion.sql`:
    - Tabla `votacion`:
        - `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
        - `nombre VARCHAR(200) NOT NULL UNIQUE`
        - `tipo VARCHAR(20) NOT NULL`
        - `alcance VARCHAR(20) NOT NULL`
        - `departamento_id UUID`
        - `municipio_id UUID`
        - `fecha_inicio TIMESTAMP`
        - `fecha_fin TIMESTAMP`
        - `estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA'`
        - `motivo TEXT`
        - `created_at TIMESTAMP NOT NULL DEFAULT NOW()`
        - `updated_at TIMESTAMP NOT NULL DEFAULT NOW()`
    - `CONSTRAINT chk_estado_votacion CHECK (estado IN ('ACTIVA','EN_PROGRESO','FINALIZADA','CANCELADA','COMPLETADA'))`
    - `CONSTRAINT chk_tipo_votacion CHECK (tipo IN ('PRESIDENCIA','CONGRESO','ALCALDIA','GOBERNACION'))`
    - `CONSTRAINT chk_alcance_votacion CHECK (alcance IN ('MUNICIPAL','DEPARTAMENTAL','REGIONAL','NACIONAL'))`
- [ ] T002 Crear enums en `domain/model/votacion/`:
    - `EstadoVotacion`: `ACTIVA`, `EN_PROGRESO`, `FINALIZADA`, `CANCELADA`, `COMPLETADA`. Método `esActiva()`,
      `esEnProgreso()`.
    - `TipoVotacion`: `PRESIDENCIA`, `CONGRESO`, `ALCALDIA`, `GOBERNACION`. Método `alcanceCompatible()` que retorna el
      `AlcanceVotacion` correspondiente.
    - `AlcanceVotacion`: `MUNICIPAL`, `DEPARTAMENTAL`, `REGIONAL`, `NACIONAL`. Método `cubre(AlcanceVotacion otro)` →
      true si `this` es >= `otro`.
- [ ] T003 Crear entidad `Votacion` en `domain/model/votacion/`:
    - `@Builder`, campos: `id` (UUID), `nombre` (NombreVotacion), `tipo` (TipoVotacion), `alcance` (AlcanceVotacion),
      `departamentoId` (UUID, nullable), `municipioId` (UUID, nullable), `fechaInicio` (Instant, nullable), `fechaFin` (
      Instant, nullable), `estado` (EstadoVotacion, default ACTIVA), `motivo` (String, nullable).
    - Método público `validateInfo()`: valida nombre no nulo, tipo y alcance no nulos. Si alcance es MUNICIPAL →
      `municipioId != null`. Si DEPARTAMENTAL → `departamentoId != null`.
    - Métodos de transición (máquina de estados):
        - `abrir(boolean tieneCandidatos, Instant ahora)`: valida `estado == ACTIVA`. Si no →
          `TransicionEstadoInvalidaException`. Si `!tieneCandidatos` → `SinCandidatosException`. Si
          `fechaInicio != null && ahora.isBefore(fechaInicio)` → `FechaInicioFuturaException`. Asigna
          `estado = EN_PROGRESO`.
        - `cerrar()`: valida `estado == EN_PROGRESO`. Asigna `estado = FINALIZADA`.
        - `cancelar(String motivo)`: valida `estado == ACTIVA || estado == EN_PROGRESO`. Si motivo vacío →
          `MotivoRequeridoException`. Asigna `estado = CANCELADA`, `motivo`.
        - `completar()`: valida `estado == FINALIZADA`. Asigna `estado = COMPLETADA`.
        - `reactivar()`: valida `estado == CANCELADA`. Asigna `estado = ACTIVA`.
    - Método `establecerFechas(Instant inicio, Instant fin)`: valida `estado == ACTIVA || estado == CANCELADA`. Si no →
      `TransicionEstadoInvalidaException`. Asigna `fechaInicio`, `fechaFin`.
    - Método `getAlcanceEfectivo()`: retorna el alcance asignado (ya validado por compatibilidad en creación).
- [ ] T004 Crear excepciones de dominio nuevas:
    - `VotacionNoEncontradaException(UUID id)` → 404.
    - `NombreDuplicadoException(String nombre)` → 409.
    - `TransicionEstadoInvalidaException(EstadoVotacion actual, EstadoVotacion destino)` → 409.
    - `TipoAlcanceIncompatibleException(TipoVotacion tipo, AlcanceVotacion alcance)` → 422.
    - `SinCandidatosException()` → 422.
    - `FechaInicioFuturaException()` → 422.
    - `MotivoRequeridoException()` → 422.
    - `AlcanceExcedidoException(AlcanceVotacion alcanceGestor, AlcanceVotacion alcanceVotacion)` → 403.
- [ ] T005 Crear puerto `VotacionRepository` en `domain/repository/`:
    - `save(Votacion)` → `Mono<Votacion>`.
    - `findById(UUID id)` → `Mono<Votacion>`.
    - `findAll()` → `Flux<Votacion>`.
    - `existsByNombre(String nombre)` → `Mono<Boolean>`.
    - `update(Votacion)` → `Mono<Votacion>`.
- [ ] T006 Crear adaptador `VotacionRepositoryAdapter` en `infrastructure/adapter/out/persistence/votacion/`:
    - Implementa el puerto usando `R2dbcEntityTemplate`.
    - `update`:
      `UPDATE votacion SET fecha_inicio = :fechaInicio, fecha_fin = :fechaFin, estado = :estado, motivo = :motivo, updated_at = NOW() WHERE id = :id`.
- [ ] T007 Extender `Modulo1Client` con método `getAlcanceGestor(UUID uid)`:
    - `GET /api/v1/users/{uid}/alcance` → `Mono<AlcanceGestorResponse>` (alcanceOperacion, municipioId, municipioNombre,
      departamentoId, departamentoNombre).
    - Timeout 5s/10s, circuit breaker.
- [ ] T008 Modificar `GlobalExceptionHandler`: mapear todas las excepciones nuevas.
- [ ] T009 Modificar `SecurityConfig`:
    - `/api/v1/votaciones/**` requiere autenticación y rol `GESTOR_ELECTORAL`.
    - `PATCH /api/v1/votaciones/{id}/completar` requiere rol `SERVICE_AUDITORIA` o API key (consumido por Módulo 3).

**Checkpoint**: Modelo de `Votacion` con máquina de estados completa. Repositorio funcional. Cliente Módulo 1 para
alcance.

---

## Phase 2: User Story 1 — Crear votación (Priority: P1)

**Goal**: El Gestor Electoral configura una nueva votación con nombre, tipo y alcance. El alcance se valida contra el
del gestor.

**Independent Test**: `POST /api/v1/votaciones` → 201. Nombre duplicado → 409. Tipo incompatible con alcance → 422.
Alcance excede al del gestor → 403.

### Tests for User Story 1

- [ ] T010 [P] [US1] Unit test `VotacionTest`: construir con Builder + `validateInfo()` → OK. Tipo PRESIDENCIA, alcance
  MUNICIPAL → builder acepta pero `validateInfo()` lanza `DatosInvalidosException` (la validación de compatibilidad la
  hace el use case).
- [ ] T011 [P] [US1] Unit test `CrearVotacionUseCaseTest`: creación exitosa. Nombre duplicado →
  `NombreDuplicadoException`. Tipo+alcance incompatibles → `TipoAlcanceIncompatibleException`. Alcance gestor
  insuficiente → `AlcanceExcedidoException`.
- [ ] T012 [P] [US1] Integration test: `POST /api/v1/votaciones` → 201.

### Implementation for User Story 1

- [ ] T013 [P] [US1] Crear `VotacionRequest` DTO: `@NotBlank String nombre`, `@NotNull TipoVotacion tipo`,
  `UUID departamentoId` (si aplica), `UUID municipioId` (si aplica). El alcance se deriva automáticamente del tipo:
  `request.tipo.alcanceCompatible()`.
- [ ] T014 [P] [US1] Crear `VotacionResponse` DTO: `UUID id`, `String nombre`, `String tipo`, `String alcance`,
  `UUID departamentoId`, `UUID municipioId`, `Instant fechaInicio`, `Instant fechaFin`, `String estado`,
  `String motivo`.
- [ ] T015 [P] [US1] Crear `VotacionDtoMapper` (MapStruct).
- [ ] T016 [US1] Crear `CrearVotacionUseCase` en `application/votacion/`:
    - Inyecta `VotacionRepository`, `Modulo1Client`.
    - Método `ejecutar(VotacionRequest request, UUID gestorUid)`:
        1. Validar nombre único: `repository.existsByNombre(request.nombre)`. Si true → `NombreDuplicadoException`.
        2. Validar compatibilidad tipo ↔ alcance: `AlcanceVotacion alcance = request.tipo.alcanceCompatible()`. Si el
           request especifica un alcance manual → validar que coincide con el del tipo.
        3. Obtener alcance del gestor: `modulo1Client.getAlcanceGestor(gestorUid)`. Validar que
           `alcanceGestor.cubre(alcance)` → si no, `AlcanceExcedidoException`.
        4. Construir `Votacion` con Builder: nombre, tipo, alcance, departamentoId/municipioId si aplica.
           `validateInfo()`.
        5. `repository.save(votacion)`.
- [ ] T017 [US1] Crear `VotacionController` en `infrastructure/adapter/in/rest/votacion/`:
    - `POST /api/v1/votaciones`: extrae `uid` del JWT, recibe `@Valid @RequestBody VotacionRequest`, invoca use case,
      retorna `201` con `VotacionResponse`.

**Checkpoint**: Creación de votaciones con validación de alcance del gestor y compatibilidad tipo↔alcance.

---

## Phase 3: User Story 2 — Establecer fechas de votación (Priority: P3)

**Goal**: El Gestor Electoral define o modifica `fecha_inicio` y `fecha_fin`. Solo en estados `ACTIVA` o `CANCELADA`.

**Independent Test**: `PATCH /api/v1/votaciones/{id}/fechas` → 200. Votación EN_PROGRESO → 409.

### Tests for User Story 2

- [ ] T018 [P] [US2] Unit test `EstablecerFechasUseCaseTest`: fechas establecidas en ACTIVA → OK. En EN_PROGRESO →
  `TransicionEstadoInvalidaException`. En FINALIZADA → `TransicionEstadoInvalidaException`.
- [ ] T019 [P] [US2] Integration test: `PATCH /api/v1/votaciones/{id}/fechas` → 200.

### Implementation for User Story 2

- [ ] T020 [P] [US2] Crear `FechasRequest` DTO: `@NotNull Instant fechaInicio`, `@NotNull Instant fechaFin`.
- [ ] T021 [US2] Crear `EstablecerFechasUseCase`:
    - Método `ejecutar(UUID votacionId, FechasRequest request)`:
        1. Buscar votación. Si no → `VotacionNoEncontradaException`.
        2. `votacion.establecerFechas(request.fechaInicio, request.fechaFin)`.
        3. `repository.update(votacion)`.
- [ ] T022 [US2] Agregar endpoint a `VotacionController`:
    - `PATCH /api/v1/votaciones/{id}/fechas`: recibe `@Valid @RequestBody FechasRequest`, retorna `200`.

**Checkpoint**: Establecimiento de fechas funcional.

---

## Phase 4: User Story 3 — Abrir votación (Priority: P2)

**Goal**: El Gestor Electoral abre la votación (`ACTIVA → EN_PROGRESO`). Requiere candidatos asignados y
`fecha_inicio ≤ now`.

**Independent Test**: `PATCH /api/v1/votaciones/{id}/abrir` → 200. Sin candidatos → 422. Antes de fecha_inicio → 422. Ya
abierta → 409.

### Tests for User Story 3

- [ ] T023 [P] [US3] Unit test `VotacionTest.abrir()`: ACTIVA con candidatos → éxito. Sin candidatos →
  `SinCandidatosException`. Antes de fechaInicio → `FechaInicioFuturaException`.
- [ ] T024 [P] [US3] Unit test `AbrirVotacionUseCaseTest`: apertura exitosa. Sin candidatos → `SinCandidatosException`.
  Ya EN_PROGRESO → `TransicionEstadoInvalidaException`.
- [ ] T025 [P] [US3] Integration test: `PATCH /api/v1/votaciones/{id}/abrir` → 200.

### Implementation for User Story 3

- [ ] T026 [US3] Crear `AbrirVotacionUseCase`:
    - Inyecta `VotacionRepository`, `CandidaturaRepository`.
    - Método `ejecutar(UUID votacionId)`:
        1. Buscar votación. Si no → `VotacionNoEncontradaException`.
        2. Validar que tenga candidatos: `candidaturaRepository.countActivasByVotacionId(votacionId)`. Si 0 →
           `SinCandidatosException`.
        3. `votacion.abrir(true, Instant.now())` → `repository.update(votacion)`.
- [ ] T027 [US3] Agregar endpoint a `VotacionController`:
    - `PATCH /api/v1/votaciones/{id}/abrir`: invoca use case, retorna `200`.

**Checkpoint**: Apertura de votaciones con validación de candidatos y fecha.

---

## Phase 5: User Stories 4 & 5 — Cerrar y cancelar votación (Priority: P2/P3)

**Goal**: Cerrar (`EN_PROGRESO → FINALIZADA`) y cancelar (`ACTIVA/EN_PROGRESO → CANCELADA` con motivo). Completar (
`FINALIZADA → COMPLETADA`, terminal).

### Tests for User Stories 4 & 5

- [ ] T028 [P] [US4] Unit test `CerrarVotacionUseCaseTest`: cierre exitoso. No EN_PROGRESO →
  `TransicionEstadoInvalidaException`.
- [ ] T029 [P] [US5] Unit test `CancelarVotacionUseCaseTest`: cancelación exitosa con motivo. Sin motivo →
  `MotivoRequeridoException`. Desde FINALIZADA → `TransicionEstadoInvalidaException`.
- [ ] T030 [P] [US6] Unit test `CompletarVotacionUseCaseTest`: completado exitoso. No FINALIZADA →
  `TransicionEstadoInvalidaException`.

### Implementation for User Stories 4 & 5

- [ ] T031 [P] [US5] Crear `MotivoRequest` DTO: `@NotBlank @Size(min=10, max=500) String motivo`.
- [ ] T032 [US4] Crear `CerrarVotacionUseCase`:
    - Busca votación, `votacion.cerrar()`, `repository.update(votacion)`.
- [ ] T033 [US5] Crear `CancelarVotacionUseCase`:
    - Busca votación, `votacion.cancelar(motivo)`, `repository.update(votacion)`.
- [ ] T034 [US6] Crear `CompletarVotacionUseCase`:
    - Busca votación, `votacion.completar()`, `repository.update(votacion)`.
    - **Nota**: este use case lo consume Módulo 3 (Auditoría) al finalizar la revisión de votos. El endpoint se expone con un rol de servicio interno (`SERVICE_AUDITORIA`) o se protege con API key.
- [ ] T035 Agregar endpoints a `VotacionController`:
    - `PATCH /api/v1/votaciones/{id}/cerrar` → 200 (`GESTOR_ELECTORAL`).
    - `PATCH /api/v1/votaciones/{id}/cancelar` (recibe `MotivoRequest`) → 200 (`GESTOR_ELECTORAL`).
    - `PATCH /api/v1/votaciones/{id}/completar` → 200. Consumido por Módulo 3. Autenticación: API key o `SERVICE_AUDITORIA`.
- [ ] T036 Agregar endpoints de consulta:
    - `GET /api/v1/votaciones` → 200 con lista.
    - `GET /api/v1/votaciones/{id}` → 200 o 404.

**Checkpoint**: Ciclo de vida completo de votaciones funcional.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Foundational (Phase 1)**: Sin dependencias externas fuertes (salvo endpoints de Módulo 1). BLOQUEA todos los user
  stories.
- **US1 — Crear (Phase 2)**: Depende de Phase 1.
- **US2 — Fechas (Phase 3)**: Depende de Phase 1.
- **US3 — Abrir (Phase 4)**: Depende de Phase 1 y de que exista el `CandidaturaRepository` (plan 004 Phase 1).
- **US4/US5 — Cerrar/Cancelar (Phase 5)**: Dependen de Phase 1.

### User Story Dependencies

- **US1 (P1)**: Solo Foundational.
- **US2 (P3)**: Solo Foundational.
- **US3 (P2)**: Foundational + CandidaturaRepository.
- **US4 (P2), US5 (P3), US6 (P3)**: Solo Foundational.
- US1, US2, US4, US5, US6 pueden implementarse en paralelo. US3 requiere candidaturas.

---

## Notes

- **Compatibilidad tipo↔alcance**: el enum `TipoVotacion` tiene el método `alcanceCompatible()` que retorna el único
  alcance válido. Esto evita que el frontend envíe combinaciones inválidas.
- **Máquina de estados**: implementada en la entidad de dominio con guardas explícitas. Si en el futuro se requiere un
  workflow más complejo (ej. Spring State Machine), la lógica está encapsulada en `Votacion` y es fácil de migrar.
- **Alcance del gestor**: `AlcanceVotacion.cubre(otro)` implementa la jerarquía: `NACIONAL.cubre(TODO)`,
  `DEPARTAMENTAL.cubre(DEPARTAMENTAL, MUNICIPAL)`, `MUNICIPAL.cubre(MUNICIPAL)`.
- **Candidatos antes de abrir**: el `CandidaturaRepository.countActivasByVotacionId` requiere que el plan 004 esté al menos en Phase 1. Si no, US3 no puede implementarse.
- **Límite Módulo 2 ↔ Módulo 3**: el Módulo 2 es dueño de la entidad `Votacion` y su máquina de estados. El endpoint `PATCH /api/v1/votaciones/{id}/completar` lo expone Módulo 2 con un rol de servicio interno — lo consume Módulo 3 cuando finaliza la auditoría. Módulo 3 necesita acceso de lectura a las tablas `voto` y `participacion` para hacer el conteo y las anulaciones, pero las transiciones de estado de `Votacion` siempre pasan por Módulo 2.
