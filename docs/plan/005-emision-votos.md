# Implementation Plan: Emisión de Votos

**Date**: 2026-06-19
**Spec**: [005-emision-votos.md](../spec/005-emision-votos.md)

## Summary

Implementar la emisión de votos con el modelo de voto secreto de dos entidades: `Voto` (inmutable, sin referencia al
usuario: `votacion_id`, `candidato_id`) y `Participación` (conecta usuario con voto: `usuario_id`, `voto_id`, `estado`,
`fecha_emision`). Cualquier usuario con rol `VOTANTE` puede votar una vez por votación. El historial del votante se construye desde `Participación` sin revelar el candidato. La anulación de participaciones y la auditoría corresponden al Módulo 3.

**Technical approach**: Dos entidades de dominio independientes. Creación atómica `Voto` + `Participación` en una
transacción reactiva. Validación del estado del usuario contra Módulo 1 (`GET /users/me`). Endpoints bajo
`/api/v1/votos` (votante) y `/api/v1/participaciones` (gestor y consulta). El historial solo expone votación, estado y
fecha.

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 3.x (WebFlux), Spring Data R2DBC, Flyway, jjwt, Lombok, MapStruct,
springdoc-openapi, Spring Boot Actuator, Bean Validation
**Storage**: PostgreSQL 16 (via R2DBC + DatabaseClient)
**Testing**: JUnit 5, Mockito, WebTestClient, Testcontainers
**Target Platform**: Linux server (Docker)
**Project Type**: Microservicio REST reactivo — extensión de los planes 001-004
**Performance Goals**: <500ms p95 en emisión de voto; <200ms en historial
**Constraints**: Una participación por usuario por votación. Voto inmutable (sin UPDATE). Historial no revela candidato.
Anulación solo en FINALIZADA. Conteo resultados solo VÁLIDOS.
**Scale/Scope**: ~10k votos por hora pico

## Project Structure

### Documentation (this feature)

```text
docs/
├── plan/
│   └── 005-emision-votos.md                # Este archivo
├── spec/
│   └── 005-emision-votos.md
├── guia-uso-modulo-2.md
└── stakeholders.md
```

### Source Code (repository root — nuevo)

```text
src/main/java/com/safevoting/elecciones/
│
├── domain/
│   ├── model/
│   │   ├── voto/
│   │   │   └── Voto.java                   # Builder + votacion_id, candidato_id. INMUTABLE.
│   │   ├── participacion/
│   │   │   ├── Participacion.java          # Builder + usuario_id, voto_id, votacion_id, estado, fecha
│   │   │   └── EstadoParticipacion.java    # VALIDO, ANULADO
│   ├── exception/
│   │   ├── voto/
│   │   │   ├── VotoNoEncontradoException.java
│   │   │   └── VotacionNoEnProgresoException.java
│   │   └── participacion/
│   │       ├── ParticipacionNoEncontradaException.java
│   │       ├── UsuarioYaVotoException.java
│   │       ├── UsuarioNoHabilitadoException.java
│   │       ├── CandidaturaNoActivaException.java
│   └── repository/
│       ├── VotoRepository.java              # Puerto: solo INSERT
│       ├── ParticipacionRepository.java     # Puerto: INSERT, find by usuario
│       ├── VotacionRepository.java          # Para validar estados
│       ├── CandidaturaRepository.java       # Para validar candidatura ACTIVA
│
├── application/
│   └── voto/
│       ├── EmitirVotoUseCase.java
│       └── ConsultarHistorialUseCase.java
│
└── infrastructure/
    ├── config/
    │   ├── BeanConfiguration.java
    │   └── SecurityConfig.java
    │       # POST /votos → .hasRole("VOTANTE")
    │       # GET /participaciones/mis-votos → .authenticated()
    └── adapter/
        ├── in/
        │   └── rest/
        │       ├── voto/
        │       │   ├── dto/
        │       │   │   ├── VotoRequest.java
        │       │   │   └── VotoResponse.java
        │       │   └── VotoController.java
        │       └── participacion/
        │           ├── dto/
        │           │   └── HistorialResponse.java
        │           └── ParticipacionController.java
        ├── out/
        │   ├── persistence/
        │   │   ├── voto/
        │   │   │   ├── VotoEntity.java
        │   │   │   └── VotoRepositoryAdapter.java
        │   │   ├── participacion/
        │   │   │   ├── ParticipacionEntity.java
        │   │   │   └── ParticipacionRepositoryAdapter.java
        │   └── client/
        │       └── Modulo1Client.java        # GET /users/me, GET /users/{uid}

src/main/resources/
└── db/migration/
    ├── V5__crear_tabla_voto.sql
    ├── V6__crear_tabla_participacion.sql

src/test/java/com/safevoting/elecciones/
├── unit/
│   ├── domain/
│   │   ├── model/voto/
│   │   │   └── VotoTest.java
│   │   └── model/participacion/
│   │       └── ParticipacionTest.java
│   └── application/
│       └── voto/
│           ├── EmitirVotoUseCaseTest.java
│           └── ConsultarHistorialUseCaseTest.java
└── integration/
    └── rest/
        └── voto/
            └── VotoIntegrationTest.java
```

**Structure Decision**: `Voto` es una entidad inmutable sin métodos de mutación. La emisión atómica se maneja en el use case con encadenamiento reactivo de `flatMap`. El historial consulta `Participacion` por `usuario_id` y `votacion_id`, uniendo con `votacion` para obtener el nombre, pero sin resolver `Voto` (el candidato queda inaccesible). La anulación de participaciones corresponde al Módulo 3.

---

## Phase 1: Foundational — Entidades Voto y Participación (Blocking Prerequisites)

**Purpose**: Crear tablas, entidades, excepciones y repositorios para `Voto` y `Participacion`.

**⚠️ CRITICAL**: Ningún user story de emisión de votos puede comenzar antes de esta fase. Requiere planes 003 (
votaciones) y 004 (candidaturas) completos.

- [ ] T001 Crear migración `V5__crear_tabla_voto.sql`:
    - Tabla `voto`:
        - `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
        - `votacion_id UUID NOT NULL REFERENCES votacion(id)`
        - `candidato_id UUID NOT NULL REFERENCES candidato(id)`
        - `created_at TIMESTAMP NOT NULL DEFAULT NOW()`
    - NOTA: NO tiene `usuario_id`. NO tiene columna `estado`.
    - Índice en `(votacion_id)` para conteo de resultados.
- [ ] T002 Crear migración `V6__crear_tabla_participacion.sql`:
    - Tabla `participacion`:
        - `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
        - `usuario_id UUID NOT NULL`
        - `voto_id UUID NOT NULL REFERENCES voto(id)`
        - `votacion_id UUID NOT NULL REFERENCES votacion(id)`
        - `estado VARCHAR(20) NOT NULL DEFAULT 'VALIDO'`
        - `fecha_emision TIMESTAMP NOT NULL DEFAULT NOW()`
    - `CONSTRAINT chk_estado_participacion CHECK (estado IN ('VALIDO','))`
    - `CREATE UNIQUE INDEX idx_participacion_usuario_votacion ON participacion(usuario_id, votacion_id)` — garantiza un
      voto por usuario por votación.
- [ ] T003 Crear entidad `Voto` en `domain/model/voto/`:
    - `@Builder`, campos: `id` (UUID), `votacionId` (UUID), `candidatoId` (UUID).
    - Sin métodos de mutación (inmutable). Solo factory `crear(votacionId, candidatoId)`.
    - `validateInfo()`: valida que `votacionId` y `candidatoId` no sean null.
- [ ] T004 Crear entidad `Participacion` en `domain/model/participacion/`:
    - `@Builder`, campos: `id` (UUID), `usuarioId` (UUID), `votoId` (UUID), `votacionId` (UUID), `estado` (EstadoParticipacion, default VALIDO), `fechaEmision` (Instant, default NOW()).
    - `validateInfo()`: valida `usuarioId`, `votoId`, `votacionId` no null.
- [ ] T005 Crear enum `EstadoParticipacion`: `VALIDO`, `. Método `esValido()`.
- [ ] T006 Crear excepciones de dominio:
    - `VotoNoEncontradoException` → 404.
    - `VotacionNoEnProgresoException` → 409.
    - `ParticipacionNoEncontradaException` → 404.
    - `UsuarioYaVotoException` → 409.
    - `UsuarioNoHabilitadoException` → 403.
    - `CandidaturaNoActivaException` → 422.
- [ ] T007 Crear puertos en `domain/repository/`:
    - `VotoRepository`: `save(Voto)` → `Mono<Voto>`.
    - `ParticipacionRepository`:
        - `save(Participacion)` → `Mono<Participacion>`.
        - `findByUsuarioId(UUID usuarioId)` → `Flux<Participacion>`.
        - `existsByUsuarioIdAndVotacionId(UUID usuarioId, UUID votacionId)` → `Mono<Boolean>`.
- [ ] T008 Crear adaptadores de persistencia:
    - `VotoRepositoryAdapter`: solo `save` con `R2dbcEntityTemplate`. Sin `update` (Voto inmutable).
    - `ParticipacionRepositoryAdapter`: `save` con INSERT. `findByUsuarioId` con SELECT por `usuario_id`.
        - `findByUsuarioId`: `SELECT * FROM participacion WHERE usuario_id = :uid`.
        - `existsByUsuarioIdAndVotacionId`:
          `SELECT COUNT(*) FROM participacion WHERE usuario_id = :uid AND votacion_id = :vid`.
- [ ] T009 Modificar `GlobalExceptionHandler`: mapear todas las excepciones nuevas.
- [ ] T010 Modificar `SecurityConfig`:
    - `POST /api/v1/votos` → `hasRole("VOTANTE")`.
    - `GET /api/v1/participaciones/mis-votos` → `authenticated()`.

**Checkpoint**: Entidades Voto y Participación completas. Índice único garantiza un voto por usuario por votación.
Repositorios funcionales.

---

## Phase 2: User Story 1 — Emitir voto (Priority: P1)

**Goal**: Un votante autenticado y habilitado emite su voto. Se crean atómicamente `Voto` + `Participacion`.

**Independent Test**: `POST /api/v1/votos` → 200. Usuario no HABILITADO → 403. Votación no EN_PROGRESO → 409.
Candidatura no ACTIVA → 422. Ya votó → 409. Usuario no autenticado → 401.

### Tests for User Story 1

- [ ] T013 [P] [US1] Unit test `VotoTest`: crear con factory → OK. `validateInfo()` con campos null → excepción.
  votó → `UsuarioYaVotoException`.
- [ ] T016 [P] [US1] Integration test: `POST /api/v1/votos` → 200. Voto duplicado → 409.

### Implementation for User Story 1

- [ ] T017 [P] [US1] Crear `VotoRequest` DTO: `@NotNull UUID candidatoId`, `@NotNull UUID votacionId`.
- [ ] T018 [P] [US1] Crear `VotoResponse` DTO: `String mensaje`, `UUID votoId`, `Instant fechaEmision`.
- [ ] T019 [US1] Crear `EmitirVotoUseCase` en `application/voto/`:
    - Inyecta `VotoRepository`, `ParticipacionRepository`, `VotacionRepository`, `CandidaturaRepository`,
      `Modulo1Client`.
    - Método `ejecutar(UUID usuarioId, UUID votacionId, UUID candidatoId)`:
        1. Validar usuario en Módulo 1: `modulo1Client.getUsuario(usuarioId)`. Si `estado != HABILITADO` →
           `UsuarioNoHabilitadoException`. Si 404 → `UsuarioNoEncontradoException`.
        2. Validar votación: buscar por id. Si `estado != EN_PROGRESO` → `VotacionNoEnProgresoException`.
        3. Validar candidatura: `candidaturaRepository.findByVotacionAndCandidato(votacionId, candidatoId)`. Si no
           existe o `estado != ACTIVA` → `CandidaturaNoActivaException`.
        4. Validar que no haya votado antes:
           `participacionRepository.existsByUsuarioIdAndVotacionId(usuarioId, votacionId)`. Si true →
           `UsuarioYaVotoException`.
        5. Validar alcance geográfico: el `municipioNombre` del usuario debe estar dentro del alcance de la votación (
           según guía-uso-modulo-2 sección 4.6).
        6. Crear `Voto` → `votoRepository.save(voto)`.
        7. Crear `Participacion` con `votoId`, `usuarioId`, `votacionId`, `VALIDO` →
           `participacionRepository.save(participacion)`.
        8. Retornar confirmación.
    - **Atomicidad**: los pasos 6 y 7 deben ejecutarse en secuencia. Si `participacionRepository.save` falla, el `Voto`
      queda huérfano. En R2DBC sin transacciones distribuidas, aceptar este riesgo (el Voto huérfano no afecta
      resultados porque no tiene Participacion asociada). Alternativa: usar `@Transactional` con `TransactionManager`
      reactivo.
- [ ] T020 [US1] Crear `VotoController` en `infrastructure/adapter/in/rest/voto/`:
    - `POST /api/v1/votos`: extrae `uid` del JWT, recibe `@Valid @RequestBody VotoRequest`, invoca use case, retorna
      `200`.

**Checkpoint**: Emisión de votos funcional con validación completa.

---

## Phase 3: User Story 2 — Ver historial de participaciones (Priority: P2)

**Goal**: Un usuario consulta su historial de votaciones en las que participó. No se muestra el candidato (voto
secreto).

**Independent Test**: `GET /api/v1/participaciones/mis-votos` → 200 con lista (votacion_nombre, estado, fecha). Sin
votos → lista vacía.

### Tests for User Story 2

- [ ] T021 [P] [US2] Unit test `ConsultarHistorialUseCaseTest`: usuario con participaciones → lista con votación, estado
  y fecha, sin candidato. Sin participaciones → lista vacía.
- [ ] T022 [P] [US2] Unit test: verificar que la respuesta NO incluye `candidatoId` ni `candidatoNombre`.
- [ ] T023 [P] [US2] Integration test: `GET /api/v1/participaciones/mis-votos` → 200.

### Implementation for User Story 2

- [ ] T024 [P] [US2] Crear `HistorialResponse` DTO: `UUID participacionId`, `String votacionNombre`, `String estado`,
  `Instant fechaEmision`. **Sin** `candidatoId`, `candidatoNombre`, `votoId`.
- [ ] T025 [US2] Crear `ConsultarHistorialUseCase`:
    - Inyecta `ParticipacionRepository`, `VotacionRepository`.
    - Método `ejecutar(UUID usuarioId)`:
        1. `participacionRepository.findByUsuarioId(usuarioId)`.
        2. Para cada participación, buscar el nombre de la votación:
           `votacionRepository.findById(votacionId).map(Votacion::getNombre)`.
        3. **No** resolver `Voto` → el `candidatoId` es inaccesible desde el historial.
        4. Mapear a `HistorialResponse` y retornar.
- [ ] T026 [US2] Crear `ParticipacionController`:
    - `GET /api/v1/participaciones/mis-votos`: extrae `uid` del JWT, invoca use case, retorna `200`.

**Checkpoint**: Historial de votante funcional, respetando voto secreto.

---

---

## Dependencies & Execution Order

### Phase Dependencies

- **Foundational (Phase 1)**: Requiere planes 003 (votaciones) y 004 (candidaturas) completos. BLOQUEA todos los user
  stories.
- **US1 — Emitir (Phase 2)**: Depende de Phase 1. Requiere `Modulo1Client` para validar estado del votante.
- **US2 — Historial (Phase 3)**: Depende de Phase 1.

### User Story Dependencies

- **US1 (P1)**: Solo Foundational. Es el caso de uso central.
- **US2 (P2)**: Solo Foundational. Independiente de US1.
- Ambos user stories pueden implementarse en paralelo.

---

## Notes

- **Voto inmutable**: la tabla `voto` no tiene endpoint de UPDATE ni DELETE. El `Voto` es un registro histórico inmutable.
- **Atomicidad Voto + Participacion**: en R2DBC sin JPA, las transacciones requieren `@Transactional` + `TransactionManager` reactivo. Si no se configura, aceptar que un `Voto` huérfano (sin Participacion) no afecta resultados porque el conteo filtra por `participacion.estado = 'VALIDO'`.
- **Conteo y anulación de resultados**: la anulación de `Participaciones` y el conteo de resultados corresponde al Módulo 3 (Auditoría). La query de referencia para el módulo de auditoría es:
  ```sql
  SELECT v.candidato_id, COUNT(*)
  FROM voto v
  JOIN participacion p ON p.voto_id = v.id
  WHERE v.votacion_id = ? AND p.estado = 'VALIDO'
  GROUP BY v.candidato_id;
  ```
- **Validación de alcance geográfico en US1**: según la guía `guia-uso-modulo-2.md` sección 4.6, se debe validar que el municipio del usuario esté dentro del alcance de la votación. Esto requiere comparar `municipioNombre`/`departamentoNombre` del usuario contra el `alcance`, `departamento_id` y `municipio_id` de la votación.
- **Límite Módulo 2 ↔ Módulo 3**: Módulo 2 es dueño de las tablas `voto` y `participacion` y las escribe durante la jornada. Módulo 3 necesita **acceso de solo lectura** a ambas para: (a) anular `Participaciones` cambiando su `estado` a `ANULADO`, (b) hacer el conteo `Voto + Participacion.VALIDO`, y (c) generar snapshots. Módulo 3 debe exponerse como un cliente de BD con credenciales de solo lectura, o Módulo 2 debe exponer endpoints internos `GET /api/v1/participaciones?votacion_id=` y `PATCH /api/v1/participaciones/{id}/estado` para que Módulo 3 los consuma. La decisión de arquitectura (acceso directo a BD vs API) queda a definir.
