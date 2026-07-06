# Feature Specification: Gestión de Votaciones

**Created**: 2026-06-19

## User Scenarios & Testing

### User Story 1 - Crear votación (Priority: P1)

El Gestor Electoral configura una nueva jornada electoral definiendo su nombre, tipo de cargo en disputa y alcance
geográfico.

**Why this priority**: Sin una votación creada no existe jornada electoral. Es la operación fundacional de todo el
proceso de votación.

**Independent Test**: Puede probarse creando una votación con nombre, tipo y alcance, y verificando que queda registrada
en estado ACTIVA.

**Acceptance Scenarios**:

1. **Scenario**: Creación exitosa de una votación
    - **Given** el Gestor Electoral está autenticado
    - **When** ingresa nombre, tipo (`PRESIDENCIA`) y alcance (`NACIONAL`)
    - **Then** la votación queda registrada en estado `ACTIVA`

2. **Scenario**: Creación con nombre duplicado
    - **Given** ya existe una votación con el nombre "Elecciones Presidenciales 2026"
    - **When** el Gestor Electoral intenta crear otra con el mismo nombre
    - **Then** el sistema rechaza la operación e informa que el nombre ya está en uso

3. **Scenario**: Creación con tipo incompatible con el alcance
    - **Given** el Gestor Electoral está autenticado
    - **When** intenta crear una votación de tipo `PRESIDENCIA` con alcance `MUNICIPAL`
    - **Then** el sistema rechaza la operación e informa que el tipo y el alcance no son compatibles. El sistema DEBE
      validar la compatibilidad según las siguientes reglas fijas:
        - `PRESIDENCIA` → alcance `NACIONAL`
        - `CONGRESO` → alcance `NACIONAL`
        - `ALCALDIA` → alcance `MUNICIPAL`
        - `GOBERNACION` → alcance `DEPARTAMENTAL`

4. **Scenario**: Creación con alcance `MUNICIPAL` o `DEPARTAMENTAL` sin especificar la entidad
    - **Given** el Gestor Electoral está autenticado
    - **When** intenta crear una votación de tipo `ALCALDIA` o `GOBERNACION` sin especificar el municipio o departamento
      correspondiente
    - **Then** el sistema rechaza la operación e informa que debe especificarse el municipio o departamento del alcance

---

### User Story 2 - Abrir votación (Priority: P1)

El Gestor Electoral transiciona una votación de estado `ACTIVA` a `EN_PROGRESO`, habilitando la recepción de votos.

**Why this priority**: Es el acto que da inicio a la jornada electoral. Sin abrir la votación, nadie puede votar.

**Independent Test**: Puede probarse creando una votación con candidatos asignados, abriéndola, y verificando que su
estado cambia a EN_PROGRESO y se pueden recibir votos.

**Acceptance Scenarios**:

1. **Scenario**: Apertura exitosa
    - **Given** existe una votación en estado `ACTIVA` con candidatos asignados
    - **When** el Gestor Electoral la abre
    - **Then** la votación pasa a estado `EN_PROGRESO` y está lista para recibir votos

2. **Scenario**: Abrir votación sin candidatos asignados
    - **Given** existe una votación en estado `ACTIVA` sin candidatos
    - **When** el Gestor Electoral intenta abrirla
    - **Then** el sistema rechaza la operación e informa que no se puede abrir una votación sin candidatos asignados

3. **Scenario**: Abrir votación antes de `fecha_inicio`
    - **Given** existe una votación en estado `ACTIVA` con candidatos y `fecha_inicio` futura
    - **When** el Gestor Electoral intenta abrirla antes de esa fecha
    - **Then** el sistema rechaza la operación e informa que la votación no puede abrirse antes de su fecha de inicio

4. **Scenario**: Abrir votación ya `EN_PROGRESO`
    - **Given** una votación ya está en estado `EN_PROGRESO`
    - **When** el Gestor Electoral intenta abrirla nuevamente
    - **Then** el sistema rechaza la operación e informa que la votación ya está en progreso

5. **Scenario**: Abrir votación `FINALIZADA` o `CANCELADA`
    - **Given** una votación está en estado `FINALIZADA` o `CANCELADA`
    - **When** el Gestor Electoral intenta abrirla
    - **Then** el sistema rechaza la operación e informa que la votación no puede reabrirse

---

### User Story 3 - Cerrar votación (Priority: P1)

El Gestor Electoral transiciona una votación de estado `EN_PROGRESO` a `FINALIZADA`, cerrando la recepción de votos e iniciando la etapa de auditoría, donde el Módulo 3 podrá revisar y anular votos antes de que los resultados se hagan públicos.

**Why this priority**: Es el cierre oficial de la jornada. Marca el fin de la recepción de votos y da paso al proceso de auditoría.

**Independent Test**: Puede probarse con una votación `EN_PROGRESO`, cerrándola y verificando que su estado cambia a `FINALIZADA` y no se aceptan más votos.

**Acceptance Scenarios**:

1. **Scenario**: Cierre exitoso
    - **Given** existe una votación en estado `EN_PROGRESO`
    - **When** el Gestor Electoral la cierra
    - **Then** la votación pasa a estado `FINALIZADA`, no se aceptan más votos y se inicia la etapa de auditoría

2. **Scenario**: Cerrar votación que no está `EN_PROGRESO`
    - **Given** una votación está en estado `ACTIVA`, `FINALIZADA`, `CANCELADA` o `COMPLETADA`
    - **When** el Gestor Electoral intenta cerrarla
    - **Then** el sistema rechaza la operación e informa que solo se puede cerrar una votación en progreso

---

### User Story 4 - Cancelar votación (Priority: P2)

El Gestor Electoral suspende una votación antes de que finalice, marcándola como `CANCELADA`. La cancelación requiere
un motivo sólido que respalde la decisión.

**Why this priority**: Es una operación excepcional para situaciones de fuerza mayor. Menos frecuente que abrir o
cerrar.

**Independent Test**: Puede probarse creando una votación (`ACTIVA` o `EN_PROGRESO`), cancelándola con un motivo, y
verificando que queda
en estado `CANCELADA` y no se pueden emitir votos.

**Acceptance Scenarios**:

1. **Scenario**: Cancelación de votación `ACTIVA`
    - **Given** existe una votación en estado `ACTIVA`
    - **When** el Gestor Electoral la cancela registrando un motivo
    - **Then** la votación pasa a estado `CANCELADA` y el motivo queda registrado

2. **Scenario**: Cancelación de votación `EN_PROGRESO`
    - **Given** existe una votación en estado `EN_PROGRESO` con votos emitidos
    - **When** el Gestor Electoral la cancela registrando un motivo
    - **Then** la votación pasa a estado `CANCELADA`, los votos emitidos se conservan para auditoría y el motivo queda
      registrado

3. **Scenario**: Cancelación sin motivo
    - **Given** existe una votación en estado `ACTIVA`
    - **When** el Gestor Electoral intenta cancelarla sin especificar un motivo
    - **Then** el sistema rechaza la operación e informa que el motivo es obligatorio

4. **Scenario**: Cancelar votación `FINALIZADA`
    - **Given** una votación está en estado `FINALIZADA`
    - **When** el Gestor Electoral intenta cancelarla
    - **Then** el sistema rechaza la operación e informa que no se puede cancelar una votación finalizada

---

### User Story 5 - Establecer fechas de votación (Priority: P1)

El Gestor Electoral define y modifica las fechas de inicio y fin de una votación mientras esté en estado `ACTIVA` o
`CANCELADA`. No se permite editar nombre, tipo ni alcance de la votación.

**Why this priority**: Permite ajustar el calendario electoral sin eliminar y recrear la votación.

**Independent Test**: Puede probarse creando una votación, estableciendo sus fechas de inicio y fin, y verificando que
los cambios persisten y que la votación no puede abrirse antes de la fecha de inicio.

**Acceptance Scenarios**:

1. **Scenario**: Establecer fechas exitosamente
    - **Given** existe una votación en estado `ACTIVA`
    - **When** el Gestor Electoral define `fecha_inicio` y `fecha_fin`
    - **Then** las fechas quedan persistidas

2. **Scenario**: Modificar fechas en votación `ACTIVA`
    - **Given** existe una votación en estado `ACTIVA` con fechas ya definidas
    - **When** el Gestor Electoral modifica `fecha_inicio` o `fecha_fin`
    - **Then** las nuevas fechas quedan persistidas

3. **Scenario**: Modificar fechas en votación `CANCELADA`
    - **Given** existe una votación en estado `CANCELADA`
    - **When** el Gestor Electoral modifica sus fechas
    - **Then** las fechas quedan persistidas, permitiendo reabrir la votación en el futuro

4. **Scenario**: Modificar fechas en votación `EN_PROGRESO` o `FINALIZADA`
    - **Given** una votación está en estado `EN_PROGRESO` o `FINALIZADA`
    - **When** el Gestor Electoral intenta modificar sus fechas
    - **Then** el sistema rechaza la operación e informa que las fechas no pueden modificarse en ese estado

---

### User Story 6 - Completar votación (Priority: P1)

El Gestor Electoral transiciona una votación de estado `FINALIZADA` a `COMPLETADA`, una vez finalizado el proceso de
auditoría. A partir de este momento los resultados se hacen públicos y no se aceptan más modificaciones.

**Why this priority**: Es el paso que cierra definitivamente la jornada electoral. Sin este paso, los resultados no son
oficiales.

**Independent Test**: Puede probarse con una votación `FINALIZADA` cuya auditoría haya concluido, completándola y
verificando que pasa a `COMPLETADA` y los resultados son visibles públicamente.

**Acceptance Scenarios**:

1. **Scenario**: Completar exitosamente
    - **Given** existe una votación en estado `FINALIZADA` cuyo proceso de auditoría ha concluido
    - **When** el Gestor Electoral la completa
    - **Then** la votación pasa a estado `COMPLETADA` y los resultados se hacen públicos

2. **Scenario**: Completar votación no `FINALIZADA`
    - **Given** una votación está en estado `ACTIVA`, `EN_PROGRESO` o `CANCELADA`
    - **When** el Gestor Electoral intenta completarla
    - **Then** el sistema rechaza la operación e informa que solo se puede completar una votación finalizada

---

### Edge Cases

- ¿Se puede cancelar una votación que tiene votos ya emitidos? ¿Qué implicaciones de auditoría tiene?
    - Sí, se puede cancelar una votación con votos emitidos. Debe registrarse un motivo sólido que respalde la decisión.
      Los votos emitidos se conservan íntegramente para fines de auditoría.
- ¿Se puede editar la información de una votación (nombre, tipo, alcance)?
    - No. La información de una votación (nombre, tipo, alcance) no se puede editar una vez creada. Solo se permite
      modificar su estado (mediante transiciones) y sus fechas de inicio y fin. Las candidaturas asignadas son
      gestionadas por separado y no forman parte de la entidad votación.
- ¿Qué sucede si se intenta abrir una votación antes de su fecha de inicio?
    - No se permite abrir una votación antes de su `fecha_inicio`. El sistema DEBE rechazar la apertura si la fecha
      actual es anterior a `fecha_inicio`. Sin embargo, el Gestor Electoral puede modificar `fecha_inicio` y
      `fecha_fin` mientras la votación esté en estado `ACTIVA` o `CANCELADA`.
- ¿Existe un orden estricto de transiciones de estado?
    - Sí. Las transiciones permitidas son:
        - `ACTIVA` → `EN_PROGRESO` o `CANCELADA`
        - `EN_PROGRESO` → `FINALIZADA` o `CANCELADA`
        - `FINALIZADA` → `COMPLETADA` (etapa de auditoría)
        - `CANCELADA` → `ACTIVA` (puede reactivarse)
        - `COMPLETADA` → ningún otro estado (terminal)

## Requirements

### Functional Requirements

- **FR-001**: El sistema DEBE permitir al Gestor Electoral crear votaciones con nombre único, tipo y alcance.
- **FR-002**: El sistema DEBE validar la compatibilidad entre tipo y alcance al crear una votación (`PRESIDENCIA` →
  `NACIONAL`, `CONGRESO` → `NACIONAL`, `ALCALDIA` → `MUNICIPAL`, `GOBERNACION` → `DEPARTAMENTAL`).
- **FR-003**: El sistema DEBE exigir la especificación de municipio o departamento cuando el alcance sea `MUNICIPAL` o
  `DEPARTAMENTAL`.
- **FR-004**: El sistema DEBE crear las votaciones en estado `ACTIVA`.
- **FR-005**: El sistema DEBE permitir al Gestor Electoral establecer y modificar `fecha_inicio` y `fecha_fin` en
  votaciones en estado `ACTIVA` o `CANCELADA`.
- **FR-006**: El sistema DEBE impedir la modificación de nombre, tipo y alcance una vez creada la votación.
- **FR-007**: El sistema DEBE permitir al Gestor Electoral abrir una votación (`ACTIVA` → `EN_PROGRESO`), solo si tiene
  candidatos asignados y la fecha actual es mayor o igual a `fecha_inicio`.
- **FR-008**: El sistema DEBE permitir al Gestor Electoral cerrar una votación (`EN_PROGRESO` → `FINALIZADA`), dando
  inicio a la etapa de auditoría.
- **FR-009**: El sistema DEBE permitir al Gestor Electoral cancelar una votación (`ACTIVA` o `EN_PROGRESO` →
  `CANCELADA`), registrando obligatoriamente un motivo.
- **FR-010**: El sistema DEBE permitir reactivar una votación cancelada (`CANCELADA` → `ACTIVA`).
- **FR-011**: El sistema DEBE permitir al Gestor Electoral completar una votación (`FINALIZADA` → `COMPLETADA`),
  haciendo públicos los resultados.
- **FR-012**: El sistema DEBE impedir que una votación `COMPLETADA` cambie a cualquier otro estado.
- **FR-013**: El sistema DEBE impedir transiciones de estado no contempladas en el ciclo de vida.
- **FR-014**: El sistema DEBE rechazar votos en votaciones que no estén en estado `EN_PROGRESO`.
- **FR-015**: Solo el rol Gestor Electoral DEBE tener acceso a estas operaciones.

### Key Entities

- **Votación**: Representa una jornada electoral. Atributos: nombre (único, inmutable tras creación), tipo (
  `PRESIDENCIA` | `CONGRESO` | `ALCALDIA` | `GOBERNACION`, inmutable), alcance (`MUNICIPAL` | `DEPARTAMENTAL` |
  `REGIONAL` | `NACIONAL`, inmutable), departamento (requerido si alcance `DEPARTAMENTAL`), municipio (requerido si
  alcance `MUNICIPAL`), fecha_inicio, fecha_fin, estado (`ACTIVA` | `EN_PROGRESO` | `FINALIZADA` | `CANCELADA` |
  `COMPLETADA`) con transiciones: `ACTIVA` → `EN_PROGRESO` o `CANCELADA`; `EN_PROGRESO` → `FINALIZADA` o `CANCELADA`;
  `FINALIZADA` → `COMPLETADA` (resultados públicos); `CANCELADA` → `ACTIVA`; `COMPLETADA` terminal.

## Success Criteria

### Measurable Outcomes

- **SC-001**: El Gestor Electoral puede crear y abrir una votación en menos de 2 minutos.
- **SC-002**: Una votación `EN_PROGRESO` acepta votos; una votación `FINALIZADA`, `CANCELADA` o `COMPLETADA` los
  rechaza.
- **SC-003**: Las transiciones de estado inválidas se rechazan con un mensaje claro.
