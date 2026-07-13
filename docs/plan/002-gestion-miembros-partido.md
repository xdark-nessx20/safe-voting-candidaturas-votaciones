# Implementation Plan: Gestión de Miembros del Partido

**Date**: 2026-07-07
**Spec**: [002-gestion-miembros-partido.md](../spec/002-gestion-miembros-partido.md)

> **Nota**: Este plan reemplaza al anterior `002-gestion-candidatos.md`. La entidad `Candidato` se ha descompuesto en dos: `MiembroPartido` (afiliación a un partido) y `Candidatura` (postulación a una votación, ver plan 004). El nombre del plan antiguo se conserva como referencia histórica.

## Summary

Implementar el CRUD de miembros de partido (registrar, editar, listar, dar de baja). Acceso exclusivo para los roles `ADMIN` y `GESTOR_CANDIDATURAS`. Cada `MiembroPartido` vincula un usuario del Módulo 1 (`usuarioId` como FK lógica) a un partido político, tomando un snapshot inmutable de los datos de identidad (nombre, documento, lugar de inscripción) en el momento del registro. Las imágenes (foto) se delegan a Cloudinary.

La validación de existencia del usuario en el Módulo 1 se hace con una estrategia híbrida (best-effort HTTP + eventos asíncronos vía RabbitMQ):

- **Al crear**: se intenta validar el usuario vía HTTP al Módulo 1. Si responde OK, se usan sus datos como fuente de verdad para el snapshot. Si falla (timeout, 5xx), se acepta el registro con los datos del frontend y un flag `verificado = false`.
- **Sincronización continua**: el Módulo 1 emite eventos (`usuario.habilitado`, `usuario.inhabilitado`, `usuario.actualizado`) que el Módulo 2 consume para mantener actualizado el estado de los miembros y sus snapshots.

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 3.x (WebFlux), Spring Data R2DBC, Flyway, jjwt, Lombok, MapStruct, springdoc-openapi, Spring Boot Actuator, Bean Validation, Spring AMQP (RabbitMQ), Resilience4j (circuit breaker)
**Storage**: PostgreSQL 16 (via R2DBC)
**Testing**: JUnit 5, Mockito, WebTestClient, Testcontainers
**Target Platform**: Linux server (Docker)
**Project Type**: Microservicio REST reactivo + consumidor de eventos — extensión del plan 001 (partidos)
**Performance Goals**: <200ms p95 en endpoints de miembros; procesamiento de eventos <1s
**Constraints**: usuarioId debe existir en Módulo 1 (validación best-effort). Un mismo usuarioId no puede estar duplicado en el mismo partido. Foto Cloudinary (opcional). Baja solo si no tiene candidaturas en votación `EN_PROGRESO`. La sincronización vía eventos es eventualmente consistente (ventana <5s).
**Scale/Scope**: ~500 miembros de partido concurrentes

## Project Structure

### Documentation (this feature)

```text
docs/
├── plan/
│   ├── 002-gestion-miembros-partido.md           # Este archivo
│   ├── 002-gestion-candidatos.md                 # Obsoleto — conservado como referencia
│   └── modulo1-eventos-implementacion.md         # Guía de implementación para el Módulo 1
├── spec/
│   └── 002-gestion-miembros-partido.md
├── guia-uso-modulo-2.md
└── stakeholders.md
```

### Source Code (repository root — nuevo y modificado)

```text
src/main/java/com/safevoting/elecciones/
│
├── domain/
│   ├── model/
│   │   └── partido/
│   │       ├── MiembroPartido.java              # Builder + usuarioId, partidoId, snapshot, fotoUrl, estado
│   │       └── EstadoMiembro.java               # ACTIVO, INACTIVO
│   ├── exception/
│   │   └── miembro/
│   │       ├── MiembroNoEncontradoException.java
│   │       ├── MiembroDuplicadoException.java
│   │       ├── MiembroYaInactivoException.java
│   │       ├── PartidoInhabilitadoException.java  # (reutilizar del plan 001 si existe)
│   │       ├── UsuarioNoEncontradoException.java
│   │       └── MiembroInscritoEnVotacionException.java
│   └── repository/
│       ├── MiembroPartidoRepository.java        # Puerto
│       └── PartidoPoliticoRepository.java       # (del plan 001: validar que partido existe)
│
├── application/
│   └── miembro/
│       ├── CrearMiembroUseCase.java             # Registro con validación best-effort HTTP
│       ├── ObtenerMiembroUseCase.java           # Consulta por ID
│       ├── ListarMiembrosUseCase.java            # Listar miembros por partido
│       ├── EditarMiembroUseCase.java            # Editar foto y snapshot tentativo
│       ├── DarBajaMiembroUseCase.java           # INACTIVO (administrativo o voluntario)
│       ├── SincronizarEstadoMiembroUseCase.java  # Procesa eventos usuario.habilitado/inhabilitado
│       └── ActualizarSnapshotUseCase.java        # Procesa eventos usuario.actualizado
│
└── infrastructure/
    ├── config/
    │   ├── BeanConfiguration.java               # Wiring de nuevos use cases
    │   ├── SecurityConfig.java                   # Rutas /api/v1/partidos/*/miembros/**
    │   ├── RabbitMqConfig.java                   # NUEVO: exchange, colas, bindings
    │   └── WebClientConfig.java                  # NUEVO: WebClient para Módulo 1 con timeouts
    │
    └── adapter/
        ├── in/
        │   ├── rest/
        │   │   └── miembro/
        │   │       ├── dto/
        │   │       │   ├── MiembroRequest.java
        │   │       │   └── MiembroResponse.java
        │   │       ├── mapper/
        │   │       │   └── MiembroDtoMapper.java
        │   │       └── MiembroController.java
        │   │
        │   └── events/                          # NUEVO: consumidores RabbitMQ
        │       └── UsuarioEventConsumer.java    # @RabbitListener para routing keys
        │
        └── out/
            ├── persistence/
            │   └── miembro/                     # NUEVO
            │       ├── MiembroEntity.java
            │       ├── MiembroPersistenceMapper.java
            │       ├── MiembroReactiveRepository.java
            │       └── MiembroRepositoryAdapter.java
            │
            └── http/                            # NUEVO: cliente al Módulo 1
                ├── UsuarioServiceClient.java
                └── dto/
                    └── UsuarioResponse.java

src/main/resources/
└── db/migration/
    └── V3__crear_tabla_miembro_partido.sql

src/test/java/com/safevoting/elecciones/
├── unit/
│   ├── domain/
│   │   └── model/partido/
│   │       └── MiembroPartidoTest.java
│   └── application/
│       └── miembro/
│           ├── CrearMiembroUseCaseTest.java
│           ├── DarBajaMiembroUseCaseTest.java
│           ├── SincronizarEstadoMiembroUseCaseTest.java
│           └── ActualizarSnapshotUseCaseTest.java
└── integration/
    └── rest/
        └── miembro/
            └── MiembroControllerIntegrationTest.java
```

**Structure Decision**: `MiembroPartido` es una entidad de dominio en `domain/model/partido/` (mismo paquete que `PartidoPolitico` por su cercanía semántica). El `UsuarioServiceClient` usa WebClient con circuit breaker (Resilience4j) y timeout 2s/3s. La sincronización de estado se maneja vía eventos RabbitMQ consumidos en `UsuarioEventConsumer`. Los eventos se procesan de forma idempotente (tabla `event_log` con `eventId` único).

---

## Phase 0: Infraestructura de mensajería (Prerrequisito)

**Purpose**: Configurar RabbitMQ y las dependencias necesarias para la comunicación asíncrona con el Módulo 1.

**⚠️ CRITICAL**: Esta fase es prerrequisito para las fases 1 y 6 (event consumers).

- [ ] T000-1 Agregar dependencia `spring-boot-starter-amqp` en `build.gradle.kts`
- [ ] T000-2 Agregar dependencia `spring-cloud-starter-circuitbreaker-reactor-resilience4j` y BOM de Spring Cloud en `build.gradle.kts`
- [ ] T000-3 Agregar servicio RabbitMQ en `docker-compose.yml`:
    ```yaml
    rabbitmq:
      image: rabbitmq:4.0-management-alpine
      container_name: safevoting_rabbitmq
      ports:
        - "5672:5672"
        - "15672:15672"
      healthcheck:
        test: rabbitmq-diagnostics check_port_connectivity
        interval: 5s
        timeout: 3s
        retries: 10
    ```
- [ ] T000-4 Agregar configuración RabbitMQ en `application.yml`:
    ```yaml
    spring:
      rabbitmq:
        host: ${RABBITMQ_HOST:localhost}
        port: 5672
        username: ${RABBITMQ_USER:guest}
        password: ${RABBITMQ_PASS:guest}
        listener:
          simple:
            retry:
              enabled: true
              max-attempts: 3
              initial-interval: 1000

    app:
      mod1:
        base-url: ${MOD1_BASE_URL:http://localhost:8080}
        connect-timeout: 2000
        read-timeout: 3000

      resilience4j:
        circuitbreaker:
          instances:
            modulo1:
              sliding-window-size: 10
              failure-rate-threshold: 50
              wait-duration-in-open-state: 30s
    ```
- [ ] T000-5 Crear `RabbitMqConfig` en `infrastructure/config/`:
    - Exchange: `safevoting.usuarios.events` (topic)
    - Cola: `m2.miembros.usuario-sync`
    - Bindings con routing keys: `usuario.habilitado`, `usuario.inhabilitado`, `usuario.actualizado`
    - Configurar `Jackson2JsonMessageConverter` para serialización JSON automática
- [ ] T000-6 Crear `WebClientConfig` en `infrastructure/config/`:
    - `WebClient.Builder` con timeout conexión 2s, lectura 3s
    - Base URL desde propiedad `app.mod1.base-url`

**Checkpoint**: RabbitMQ configurado y funcional. WebClient listo para llamadas al Módulo 1.

---

## Phase 1: Foundational — Nueva entidad y modelo de dominio (Blocking Prerequisites)

**Purpose**: Crear la tabla, entidad, enumerados, excepciones y repositorio para `MiembroPartido`. Integración básica con Módulo 1 (cliente HTTP) y sistema de eventos (schema de mensajes).

**⚠️ CRITICAL**: Ningún user story de miembros puede comenzar antes de esta fase. Requiere que el plan 001 (partidos) esté completo.

- [ ] T001 Crear migración Flyway `V3__crear_tabla_miembro_partido.sql`:
    - Tabla `miembro_partido`:
        - `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
        - `usuario_id UUID NOT NULL`
        - `partido_id UUID NOT NULL REFERENCES partidos(id)`
        - `nombre_completo VARCHAR(200) NOT NULL`
        - `documento_identidad VARCHAR(50) NOT NULL`
        - `lugar_inscripcion VARCHAR(200) NOT NULL`
        - `foto_url VARCHAR(500)`
        - `estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'`
        - `verificado BOOLEAN NOT NULL DEFAULT FALSE`
        - `motivo_baja VARCHAR(500)`
        - `created_at TIMESTAMP NOT NULL DEFAULT NOW()`
        - `updated_at TIMESTAMP NOT NULL DEFAULT NOW()`
    - `CONSTRAINT chk_estado_miembro CHECK (estado IN ('ACTIVO','INACTIVO'))`
    - `CONSTRAINT uq_miembro_usuario_partido UNIQUE (usuario_id, partido_id)`
- [ ] T002 Crear migración Flyway `V4__crear_tabla_event_log.sql`:
    - Tabla `event_log` para idempotencia:
        - `event_id VARCHAR(100) PRIMARY KEY`
        - `processed_at TIMESTAMP NOT NULL DEFAULT NOW()`
    - Usada por los consumidores de eventos para evitar procesar duplicados
- [ ] T003 Crear enum `EstadoMiembro` en `domain/model/partido/`:
    - `ACTIVO`, `INACTIVO`.
    - Métodos `esActivo()`, `esInactivo()`.
- [ ] T004 Crear entidad `MiembroPartido` en `domain/model/partido/`:
    - `@Builder`, campos:
        - `id` (UUID)
        - `usuarioId` (UUID, obligatorio)
        - `partidoId` (UUID, obligatorio)
        - `nombreCompleto` (String, snapshot del Módulo 1, obligatorio)
        - `documentoIdentidad` (String, snapshot, obligatorio)
        - `lugarInscripcion` (String, snapshot, obligatorio)
        - `fotoUrl` (String, nullable)
        - `estado` (EstadoMiembro, default ACTIVO)
        - `verificado` (boolean, default false)
        - `motivoBaja` (String, nullable)
    - Métodos de validación:
        - `validateUsuarioPartido()`: `usuarioId != null && partidoId != null`.
        - `validateSnapshot()`: `nombreCompleto != null && documentoIdentidad != null && lugarInscripcion != null`.
    - Método público `validateInfo()`: invoca `validateUsuarioPartido()` + `validateSnapshot()`.
    - Métodos de mutación:
        - `editar(String fotoUrl)`: actualiza solo la foto. El snapshot es inmutable desde el frontend (solo se actualiza vía eventos).
        - `desactivar(String motivo)`: valida `esActivo()`. Si no → `MiembroYaInactivoException`. Asigna `estado = INACTIVO`, `motivoBaja = motivo`.
        - `reactivar()`: valida `esInactivo()`. Asigna `estado = ACTIVO`, `motivoBaja = null`.
        - `marcarVerificado()`: asigna `verificado = true`.
    - Método `actualizarSnapshot(String nombre, String documento, String lugar)`: actualiza los tres campos del snapshot. Usado solo internamente por `ActualizarSnapshotUseCase`.
- [ ] T005 Crear excepciones de dominio nuevas:
    - `MiembroNoEncontradoException(UUID id)` → errorCode `MIEMBRO_NO_ENCONTRADO`.
    - `MiembroDuplicadoException(UUID usuarioId, UUID partidoId)` → errorCode `MIEMBRO_DUPLICADO`.
    - `MiembroYaInactivoException(UUID id)` → errorCode `MIEMBRO_YA_INACTIVO`.
    - `UsuarioNoEncontradoException(UUID usuarioId)` → errorCode `USUARIO_NO_ENCONTRADO`.
    - `MiembroInscritoEnVotacionException(UUID id)` → errorCode `MIEMBRO_INSCRITO_EN_VOTACION`.
- [ ] T006 Crear puerto `MiembroPartidoRepository` en `domain/repository/`:
    - `save(MiembroPartido)` → `Mono<MiembroPartido>`.
    - `findById(UUID id)` → `Mono<MiembroPartido>`.
    - `findByPartidoId(UUID partidoId)` → `Flux<MiembroPartido>`.
    - `findByUsuarioId(UUID usuarioId)` → `Flux<MiembroPartido>` (un usuario puede estar en varios partidos).
    - `existsByUsuarioIdAndPartidoId(UUID usuarioId, UUID partidoId)` → `Mono<Boolean>`.
    - `update(MiembroPartido)` → `Mono<MiembroPartido>`.
- [ ] T007 Crear adaptador `MiembroRepositoryAdapter` en `infrastructure/adapter/out/persistence/miembro/`:
    - Implementa el puerto usando `R2dbcEntityTemplate`.
    - `findByPartidoId`: `SELECT * FROM miembro_partido WHERE partido_id = :partidoId`.
    - `findByUsuarioId`: `SELECT * FROM miembro_partido WHERE usuario_id = :usuarioId`.
    - `existsByUsuarioIdAndPartidoId`: `SELECT COUNT(*) FROM miembro_partido WHERE usuario_id = :usuarioId AND partido_id = :partidoId`.
    - `update`: `UPDATE miembro_partido SET foto_url = :fotoUrl, estado = :estado, motivo_baja = :motivoBaja, verificado = :verificado, nombre_completo = :nombreCompleto, documento_identidad = :documentoIdentidad, lugar_inscripcion = :lugarInscripcion, updated_at = NOW() WHERE id = :id`.
- [ ] T008 Crear `UsuarioServiceClient` en `infrastructure/adapter/out/http/`:
    - `WebClient` con base URL `app.mod1.base-url`.
    - Método `getUsuario(UUID usuarioId)` → `Mono<UsuarioResponse>`:
        - `GET /api/v1/users/{usuarioId}` → si 200 retorna `UsuarioResponse`, si 404 retorna `Mono.empty()`.
    - Anotar con `@CircuitBreaker(name = "modulo1")` para fallos del Módulo 1.
    - `UsuarioResponse`: `UUID id`, `String nombreCompleto`, `String documentoIdentidad`, `String lugarInscripcion`, `String estado`.
- [ ] T009 Crear DTOs de eventos (schemas de mensajes RabbitMQ) en `infrastructure/adapter/in/events/`:
    - `UsuarioHabilitadoEvent`: `String eventId`, `Instant timestamp`, `UUID usuarioId`, `String nombreCompleto`, `String documentoIdentidad`, `String lugarInscripcion`.
    - `UsuarioInhabilitadoEvent`: `String eventId`, `Instant timestamp`, `UUID usuarioId`, `String motivo` (opcional).
    - `UsuarioActualizadoEvent`: `String eventId`, `Instant timestamp`, `UUID usuarioId`, `String nombreCompleto`, `String documentoIdentidad`, `String lugarInscripcion`.
- [ ] T010 Modificar `GlobalExceptionHandler`:
    - Agregar mapeos para todas las excepciones nuevas:
        - `MiembroNoEncontradoException` → 404
        - `MiembroDuplicadoException` → 409
        - `MiembroYaInactivoException` → 409
        - `UsuarioNoEncontradoException` → 404 (si falla validación HTTP y no hay datos tentativos)
        - `MiembroInscritoEnVotacionException` → 409
- [ ] T011 Modificar `SecurityConfig`:
    - `/api/v1/partidos/{partidoId}/miembros/**` requiere autenticación y roles `ADMIN` o `GESTOR_CANDIDATURAS`.

**Checkpoint**: Modelo de `MiembroPartido` completo. Cliente Módulo 1 funcional con circuit breaker. Esquemas de eventos definidos.

---

## Phase 2: User Story 1 — Registrar miembro (Priority: P1)

**Goal**: El Gestor Candidaturas registra un miembro vinculando un usuario del Módulo 1 a un partido habilitado, con foto opcional y snapshot de datos de identidad.

**Independent Test**: `POST /api/v1/partidos/{partidoId}/miembros` → 201. Usuario inexistente sin datos tentativos → 404. Partido inhabilitado → 422. Usuario ya miembro del mismo partido → 409.

### Tests for User Story 1

- [ ] T012 [P] [US1] Unit test `MiembroPartidoTest`: construir con Builder + `validateInfo()` → OK. usuarioId null → `DatosInvalidosException`. Snapshot incompleto → `DatosInvalidosException`.
- [ ] T013 [P] [US1] Unit test `CrearMiembroUseCaseTest`: mock Módulo 1 responde OK → usa datos del Módulo 1 para snapshot. Módulo 1 falla (circuit breaker abierto) pero datos tentativos presentes → éxito con `verificado = false`. Módulo 1 retorna 404 sin datos tentativos → `UsuarioNoEncontradoException`. Partido inhabilitado → `PartidoInhabilitadoException`. Usuario ya miembro → `MiembroDuplicadoException`.
- [ ] T014 [P] [US1] Integration test `MiembroControllerIntegrationTest`: `POST /api/v1/partidos/{id}/miembros` → 201.

### Implementation for User Story 1

- [ ] T015 [P] [US1] Crear `MiembroRequest` DTO: `@NotNull UUID usuarioId`, `String nombreCompleto` (snapshot tentativo), `String documentoIdentidad` (snapshot tentativo), `String lugarInscripcion` (snapshot tentativo), `String fotoBase64` (opcional).
- [ ] T016 [P] [US1] Crear `MiembroResponse` DTO: `UUID id`, `UUID usuarioId`, `String nombreCompleto`, `String documentoIdentidad`, `String lugarInscripcion`, `UUID partidoId`, `String nombrePartido`, `String fotoUrl`, `String estado`, `boolean verificado`.
- [ ] T017 [P] [US1] Crear `MiembroDtoMapper` (MapStruct): `MiembroRequest → MiembroPartido` (parcial, sin snapshot definitivo ni fotoUrl). `MiembroPartido → MiembroResponse`.
- [ ] T018 [US1] Crear `CrearMiembroUseCase` en `application/miembro/`:
    - Inyecta `MiembroPartidoRepository`, `PartidoPoliticoRepository`, `UsuarioServiceClient`, `ImageStorageService`.
    - Método `ejecutar(UUID partidoId, MiembroRequest request)`:
        1. Validar partido: buscar por id, verificar `esHabilitado()`. Si no → `PartidoInhabilitadoException`.
        2. Validar duplicado: `repository.existsByUsuarioIdAndPartidoId(request.usuarioId, partidoId)`. Si true → `MiembroDuplicadoException`.
        3. Validar usuario (best-effort HTTP): `usuarioServiceClient.getUsuario(request.usuarioId)`
            - Si retorna `UsuarioResponse` con estado `HABILITADO` → usar sus datos como snapshot, marcar `verificado = true`.
            - Si retorna 404 o el estado no es `HABILITADO` → si el request tiene snapshot tentativo completo, usarlo con `verificado = false`. Si no → `UsuarioNoEncontradoException`.
            - Si el circuit breaker está abierto → misma lógica que 404 (usar datos tentativos o fallar).
        4. Subir foto si `request.fotoBase64 != null`: `imageStorageService.upload(fotoBase64)`. Si falla → continuar sin foto.
        5. Construir `MiembroPartido` con Builder, `validateInfo()`.
        6. `repository.save(miembro)` → retorna `MiembroPartido`.
- [ ] T019 [US1] Crear `MiembroController` en `infrastructure/adapter/in/rest/miembro/`:
    - `POST /api/v1/partidos/{partidoId}/miembros`: recibe `@Valid @RequestBody MiembroRequest`, invoca use case, retorna `201` con `MiembroResponse`.

**Checkpoint**: Registro de miembros funcional con validación best-effort al Módulo 1 y soporte para modo degradado.

---

## Phase 3: User Story 2 — Listar miembros (Priority: P2)

**Goal**: El Gestor Candidaturas consulta todos los miembros de un partido.

**Independent Test**: `GET /api/v1/partidos/{partidoId}/miembros` → 200 con lista. Sin miembros → lista vacía.

### Tests for User Story 2

- [ ] T020 [P] [US2] Unit test `ListarMiembrosUseCaseTest`: repos con miembros → lista. Repos vacío → lista vacía.
- [ ] T021 [P] [US2] Integration test: `GET /api/v1/partidos/{id}/miembros` → 200.

### Implementation for User Story 2

- [ ] T022 [US2] Crear `ListarMiembrosUseCase` en `application/miembro/`:
    - Inyecta `MiembroPartidoRepository`.
    - Método `ejecutar(UUID partidoId)`: `repository.findByPartidoId(partidoId)`. Retorna `Flux<MiembroResponse>`.
- [ ] T023 [US2] Crear `ObtenerMiembroUseCase` en `application/miembro/`:
    - Inyecta `MiembroPartidoRepository`.
    - Método `ejecutar(UUID id)`: `repository.findById(id)`. Si no → `MiembroNoEncontradoException`. Retorna `Mono<MiembroResponse>`.
- [ ] T024 [US2] Agregar endpoints a `MiembroController`:
    - `GET /api/v1/partidos/{partidoId}/miembros`: invoca `ListarMiembrosUseCase`, retorna `200`.
    - `GET /api/v1/partidos/{partidoId}/miembros/{id}`: invoca `ObtenerMiembroUseCase`, retorna `200` o `404`.

**Checkpoint**: Listado y consulta de miembros funcional.

---

## Phase 4: User Story 3 — Editar miembro (Priority: P3)

**Goal**: El Gestor Candidaturas modifica la foto de un miembro. El snapshot de datos de identidad NO se edita manualmente (solo se actualiza vía eventos del Módulo 1).

**Independent Test**: `PUT /api/v1/partidos/{partidoId}/miembros/{id}` → 200. Miembro inexistente → 404.

### Tests for User Story 3

- [ ] T025 [P] [US3] Unit test `EditarMiembroUseCaseTest`: edición exitosa de foto. Miembro no encontrado → `MiembroNoEncontradoException`.
- [ ] T026 [P] [US3] Integration test: `PUT /api/v1/partidos/{id}/miembros/{id}` → 200.

### Implementation for User Story 3

- [ ] T027 [US3] Crear `EditarMiembroUseCase` en `application/miembro/`:
    - Inyecta `MiembroPartidoRepository`, `ImageStorageService`.
    - Método `ejecutar(UUID id, MiembroRequest request)`:
        1. Buscar miembro por id. Si no → `MiembroNoEncontradoException`.
        2. Si `request.fotoBase64 != null` → subir a Cloudinary. Si falla → mantener foto anterior.
        3. `miembro.editar(nuevaFotoUrl)`.
        4. `repository.update(miembro)`.
- [ ] T028 [US3] Agregar endpoint a `MiembroController`:
    - `PUT /api/v1/partidos/{partidoId}/miembros/{id}`: recibe `@Valid @RequestBody MiembroRequest` (solo fotoBase64 es relevante), invoca use case, retorna `200`.

**Checkpoint**: Edición de foto de miembros funcional. Snapshot permanece inmutable desde el frontend.

---

## Phase 5: User Story 4 — Dar de baja miembro (Priority: P2)

**Goal**: El Gestor Candidaturas desactiva a un miembro. No se permite si el miembro tiene candidaturas en votación `EN_PROGRESO`. El miembro puede ser reactivado posteriormente si el evento `usuario.habilitado` llega desde el Módulo 1.

**Independent Test**: `PATCH /api/v1/partidos/{partidoId}/miembros/{id}/baja` → 200. Miembro ya inactivo → 409. Con candidaturas en EN_PROGRESO → 409.

### Tests for User Story 4

- [ ] T029 [P] [US4] Unit test `DarBajaMiembroUseCaseTest`: baja exitosa con motivo. Miembro ya inactivo → `MiembroYaInactivoException`. Con candidaturas activas → `MiembroInscritoEnVotacionException`.
- [ ] T030 [P] [US4] Integration test: `PATCH .../miembros/{id}/baja` → 200.

### Implementation for User Story 4

- [ ] T031 [US4] Crear `DarBajaMiembroUseCase` en `application/miembro/`:
    - Inyecta `MiembroPartidoRepository`, `CandidaturaRepository`.
    - Método `ejecutar(UUID miembroId, String motivo)`:
        1. Buscar miembro por id. Si no → `MiembroNoEncontradoException`.
        2. Validar que no tenga candidaturas en votación `EN_PROGRESO`: `candidaturaRepository.findActivasByMiembroId(miembroId)` → si alguna tiene votación EN_PROGRESO → `MiembroInscritoEnVotacionException`.
        3. `miembro.desactivar(motivo)` → `repository.update(miembro)`.
- [ ] T032 [US4] Agregar endpoint a `MiembroController`:
    - `PATCH /api/v1/partidos/{partidoId}/miembros/{id}/baja`: recibe `@RequestBody BajaRequest { String motivo }`, retorna `200`.

**Checkpoint**: Baja de miembros funcional con validación de candidaturas activas.

---

## Phase 6: Sincronización asíncrona con Módulo 1 (Event Consumers)

**Purpose**: Consumir eventos del Módulo 1 para mantener sincronizado el estado de los miembros y sus snapshots de identidad.

**⚠️ CRITICAL**: Requiere que Phase 0 (RabbitMQ) y Phase 1 (entidad + repositorio) estén completas.

- [ ] T033 [P] [EVT] Crear `SincronizarEstadoMiembroUseCase` en `application/miembro/`:
    - Inyecta `MiembroPartidoRepository`.
    - Método `ejecutarPorHabilitacion(UsuarioHabilitadoEvent event)`:
        1. Verificar idempotencia (eventId en `event_log`). Si ya procesado → skip.
        2. Buscar miembros por `usuarioId`: `repository.findByUsuarioId(event.usuarioId)`.
        3. Para cada miembro INACTIVO con motivo relacionado a inhabilitación de usuario → `reactivar()`.
        4. Guardar eventId en `event_log`.
    - Método `ejecutarPorInhabilitacion(UsuarioInhabilitadoEvent event)`:
        1. Verificar idempotencia.
        2. Buscar miembros por `usuarioId`.
        3. Para cada miembro ACTIVO → `desactivar("USUARIO_INHABILITADO: " + event.motivo)`.
        4. Guardar eventId.
- [ ] T034 [P] [EVT] Crear `ActualizarSnapshotUseCase` en `application/miembro/`:
    - Inyecta `MiembroPartidoRepository`, `CandidaturaRepository`.
    - Método `ejecutar(UsuarioActualizadoEvent event)`:
        1. Verificar idempotencia.
        2. Buscar miembros por `usuarioId`.
        3. Para cada miembro:
            - Verificar si el miembro tiene candidaturas activas en votación `EN_PROGRESO`.
            - Si NO tiene → `miembro.actualizarSnapshot(event.nombreCompleto, event.documentoIdentidad, event.lugarInscripcion)` + `repository.update(miembro)`.
            - Si SÍ tiene → solo loggear advertencia (snapshot inmutable durante elección activa).
        4. Guardar eventId.
- [ ] T035 [EVT] Crear `UsuarioEventConsumer` en `infrastructure/adapter/in/events/`:
    - Clase anotada con `@Component` y `@RabbitListener`.
    - Métodos:
        - `@RabbitListener(queues = "m2.miembros.usuario-sync")` con filtro por routing key.
        - `handleUsuarioHabilitado(UsuarioHabilitadoEvent event)`: deserializa JSON, invoca `SincronizarEstadoMiembroUseCase.ejecutarPorHabilitacion(event)`.
        - `handleUsuarioInhabilitado(UsuarioInhabilitadoEvent event)`: deserializa JSON, invoca `SincronizarEstadoMiembroUseCase.ejecutarPorInhabilitacion(event)`.
        - `handleUsuarioActualizado(UsuarioActualizadoEvent event)`: deserializa JSON, invoca `ActualizarSnapshotUseCase.ejecutar(event)`.
    - Manejo de errores: log + DLQ (Dead Letter Queue) para mensajes que fallen tras reintentos.
    - `default-requeue-rejected: false` para evitar bucles infinitos.
- [ ] T036 [EVT] Crear repositorio `EventLogRepository` (o extender el adaptador existente):
    - Método `existsByEventId(String eventId)` → `Mono<Boolean>`.
    - Método `saveEventId(String eventId)` → `Mono<Void>`.
    - Implementación con `DatabaseClient`: `INSERT INTO event_log (event_id) VALUES (:eventId) ON CONFLICT DO NOTHING`.

### Tests for Phase 6

- [ ] T037 [P] [EVT] Unit test `SincronizarEstadoMiembroUseCaseTest`:
    - Usuario habilitado reactiva miembros INACTIVOS por inhabilitación.
    - Usuario inhabilitado desactiva miembros ACTIVOS.
    - Evento duplicado (mismo eventId) → no procesa.
    - Usuario habilitado sobre miembro ya ACTIVO → no-op.
- [ ] T038 [P] [EVT] Unit test `ActualizarSnapshotUseCaseTest`:
    - Miembro sin candidaturas activas → snapshot actualizado.
    - Miembro con candidatura en EN_PROGRESO → snapshot NO actualizado (log de advertencia).
    - Evento duplicado → no procesa.

**Checkpoint**: Sincronización asíncrona funcional. Cambios de estado en Módulo 1 se reflejan en Módulo 2 en <5s.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 0 (RabbitMQ)**: Sin dependencias internas. PRERREQUISITO para Phase 1 y Phase 6.
- **Foundational (Phase 1)**: Requiere plan 001 (partidos) completo y Phase 0. BLOQUEA todos los user stories.
- **US1 — Registrar (Phase 2)**: Depende de Phase 1.
- **US2 — Listar (Phase 3)**: Depende de Phase 1.
- **US3 — Editar (Phase 4)**: Depende de Phase 1.
- **US4 — Baja (Phase 5)**: Depende de Phase 1. Requiere `CandidaturaRepository` (plan 004) para validar EN_PROGRESO.
- **Sync Events (Phase 6)**: Depende de Phase 1. Puede implementarse en paralelo con US1-US4.

### User Story Dependencies

- **US1 (P1)**: Solo depende de Foundational.
- **US2 (P2)**: Solo depende de Foundational.
- **US3 (P3)**: Solo depende de Foundational.
- **US4 (P2)**: Depende de Foundational + plan 004 (CandidaturaRepository).
- Todos los user stories pueden implementarse en paralelo una vez completada la Phase 1.

### Within Each User Story

- DTOs y mapper primero.
- Caso de uso antes que controlador.
- Tests unitarios de dominio y caso de uso, luego tests de integración del endpoint.

---

## Notes

- **Estrategia híbrida de validación**: La validación HTTP al Módulo 1 es best-effort. El circuit breaker (Resilience4j) evita saturar al Módulo 1 si está caído. En modo degradado, se acepta el registro con datos tentativos del frontend y `verificado = false`. Un job batch futuro puede reintentar la verificación para miembros no verificados.
- **Idempotencia de eventos**: Cada evento tiene un `eventId` único. La tabla `event_log` garantiza procesamiento exactly-once. `INSERT ON CONFLICT DO NOTHING` en PostgreSQL maneja la condición de carrera.
- **Snapshot inmutable durante elecciones**: Si un miembro está en una candidatura de una votación `EN_PROGRESO`, el evento `usuario.actualizado` no modifica el snapshot. Esto garantiza que el tarjetón digital no cambie durante la votación.
- **Un usuario en múltiples partidos**: A diferencia del plan original (que restringía a un usuario a un solo partido), el nuevo modelo permite que un usuario sea miembro de varios partidos. La restricción de unicidad es por par `(usuario_id, partido_id)`. La restricción de "un solo partido a la vez" se traslada a la entidad `Candidatura` (un `MiembroPartido` solo puede ser candidato en un partido por votación).
- **Migración V2**: La migración `V2__crear_tabla_candidato.sql` del plan original NUNCA se ejecutó en producción (el código de Candidato era un stub). Se salta ese número de versión y se usa `V3` para `miembro_partido`. Si ya se ejecutó en desarrollo, hacer `flyway:clean` o crear una migración de rollback manual.
- **Foto del miembro**: Es independiente de la foto del `Usuario` en el Módulo 1. El miembro puede tener una foto electoral distinta a su foto de perfil civil. La URL de Cloudinary se persiste en este módulo.
