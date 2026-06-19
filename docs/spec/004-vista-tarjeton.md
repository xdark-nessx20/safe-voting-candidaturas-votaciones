# Feature Specification: Vista del Tarjetón

**Created**: 2026-06-19

## User Scenarios & Testing

### User Story 1 - Inscribir candidatura (Priority: P1)

El Gestor Votaciones inscribe un candidato en una votación creando una candidatura, definiendo así la oferta electoral
que se mostrará en el tarjetón digital.

**Why this priority**: Sin candidaturas inscritas el tarjetón está vacío. Esta operación es el puente entre candidatos y
votaciones.

**Independent Test**: Puede probarse con una votación `ACTIVA` y candidatos existentes, inscribiendo una candidatura y
verificando que aparece en el tarjetón.

**Acceptance Scenarios**:

1. **Scenario**: Inscripción exitosa de candidatura
    - **Given** existe una votación en estado `ACTIVA` y un candidato activo con partido habilitado
    - **When** el Gestor Votaciones inscribe la candidatura
    - **Then** la candidatura queda registrada con estado `ACTIVA`, fecha de inscripción y es visible en el tarjetón

2. **Scenario**: Inscribir candidatura en votación no `ACTIVA`
    - **Given** existe una votación en estado `EN_PROGRESO`
    - **When** el Gestor Votaciones intenta inscribir una candidatura
    - **Then** el sistema rechaza la operación e informa que solo se pueden inscribir candidaturas en votaciones
      `ACTIVAS`

3. **Scenario**: Inscribir candidato inactivo o suspendido
    - **Given** existe una votación `ACTIVA` y un candidato dado de baja o suspendido
    - **When** el Gestor Votaciones intenta inscribir su candidatura
    - **Then** el sistema rechaza la operación e informa que el candidato no está activo

4. **Scenario**: Inscribir candidato de partido inhabilitado
    - **Given** existe una votación `ACTIVA` y un candidato cuyo partido está inhabilitado
    - **When** el Gestor Votaciones intenta inscribir su candidatura
    - **Then** el sistema rechaza la operación e informa que el partido del candidato no está habilitado

---

### User Story 2 - Cancelar candidatura (Priority: P2)

El Gestor Votaciones desasigna un candidato de una votación cancelando su candidatura mientras la votación esté en
estado `ACTIVA`.

**Why this priority**: Permite corregir la oferta electoral antes de abrir la votación, sin eliminar el registro
histórico.

**Independent Test**: Puede probarse inscribiendo una candidatura en una votación `ACTIVA`, cancelándola y verificando
que ya no aparece en el tarjetón pero se conserva en el historial.

**Acceptance Scenarios**:

1. **Scenario**: Cancelación exitosa de candidatura
    - **Given** existe una candidatura `ACTIVA` en una votación `ACTIVA`
    - **When** el Gestor Votaciones la cancela
    - **Then** la candidatura pasa a estado `CANCELADA` y deja de mostrarse en el tarjetón

2. **Scenario**: Cancelar candidatura en votación no `ACTIVA`
    - **Given** existe una candidatura en una votación `EN_PROGRESO`
    - **When** el Gestor Votaciones intenta cancelarla
    - **Then** el sistema rechaza la operación e informa que las candidaturas no pueden modificarse una vez iniciada la
      votación

3. **Scenario**: Cancelar candidatura ya cancelada
    - **Given** una candidatura ya está en estado `CANCELADA`
    - **When** el Gestor Votaciones intenta cancelarla nuevamente
    - **Then** el sistema rechaza la operación e informa que la candidatura ya está cancelada

---

### User Story 3 - Ver tarjetón digital (Priority: P1)

Un usuario autenticado consulta el tarjetón digital de una votación para ver los candidatos y partidos por los cuales
puede votar.

**Why this priority**: Sin esta vista el votante no puede informarse antes de emitir su voto. Es un requisito previo a
la emisión del voto.

**Independent Test**: Puede probarse con una votación `EN_PROGRESO` que tenga candidatos asignados, accediendo al
tarjetón
y verificando que muestra los candidatos con su foto, nombre y partido.

**Acceptance Scenarios**:

1. **Scenario**: Tarjetón con candidatos
    - **Given** existe una votación `EN_PROGRESO` con candidatos asignados
    - **When** un usuario autenticado solicita ver el tarjetón
    - **Then** el sistema muestra todos los candidatos con su foto, nombre y partido

2. **Scenario**: Tarjetón de votación no iniciada
    - **Given** existe una votación `ACTIVA` con candidatos asignados
    - **When** un usuario autenticado solicita ver el tarjetón
    - **Then** el sistema muestra todos los candidatos con su foto, nombre y partido, indicando que la votación aún no
      ha comenzado

3. **Scenario**: Tarjetón de votación `FINALIZADA`
    - **Given** existe una votación `FINALIZADA`
    - **When** un usuario autenticado solicita ver el tarjetón
    - **Then** el sistema muestra los candidatos, pero indica que la votación ya finalizó y no se puede votar

4. **Scenario**: Tarjetón vacío
    - **Given** existe una votación `EN_PROGRESO` sin candidatos asignados
    - **When** un usuario autenticado solicita ver el tarjetón
    - **Then** el sistema muestra un tarjetón vacío informando que no hay candidatos

5. **Scenario**: Usuario no autenticado
    - **Given** existe una votación `EN_PROGRESO`
    - **When** un usuario no autenticado intenta ver el tarjetón
    - **Then** el sistema rechaza la solicitud y requiere autenticación

---

### Edge Cases

- ¿Se puede desasignar un candidato de una votación después de asignarlo? ¿En qué estados de la votación?
    - Sí, se puede desasignar una candidatura mientras la votación esté en estado `ACTIVA`. Una vez que la votación pasa
      a `EN_PROGRESO`, las candidaturas no pueden modificarse.
- ¿Qué sucede si un candidato es dado de baja después de haber sido asignado a una votación EN_PROGRESO?
    - No se permite dar de baja un candidato que tenga candidaturas en una votación `EN_PROGRESO`. Las candidaturas se
      gestionan durante la etapa `ACTIVA` de la votación. Si la votación está en progreso, el candidato no puede ser
      dado de baja.
- ¿El tarjetón debe ocultar candidatos de partidos inhabilitados aunque ya estuvieran asignados?
    - Cuando un partido es inhabilitado, todas las candidaturas de ese partido deben ser inhabilitadas automáticamente,
      por lo que dejarán de mostrarse en el tarjetón.
- ¿Se debe mostrar algún orden específico de candidatos en el tarjetón?
    - El Gestor Votaciones decide el criterio de ordenamiento. Por defecto, los candidatos se muestran ordenados por la
      fecha de inscripción de la candidatura.

## Requirements

### Functional Requirements

- **FR-001**: El sistema DEBE permitir al Gestor Votaciones inscribir una candidatura (vincular un candidato a una
  votación) solo cuando la votación esté en estado `ACTIVA`.
- **FR-002**: El sistema DEBE validar que el candidato esté activo y su partido esté habilitado al inscribir una
  candidatura.
- **FR-003**: El sistema DEBE permitir al Gestor Votaciones cancelar una candidatura solo cuando la votación esté en
  estado `ACTIVA`.
- **FR-004**: El sistema DEBE registrar la fecha de inscripción al crear una candidatura.
- **FR-005**: El sistema DEBE permitir a cualquier usuario autenticado ver el tarjetón digital de una votación en
  cualquier estado (`ACTIVA`, `EN_PROGRESO`, `FINALIZADA`).
- **FR-006**: El sistema DEBE inhabilitar automáticamente todas las candidaturas de un partido cuando este es
  inhabilitado.
- **FR-007**: El tarjetón DEBE mostrar las candidaturas `ACTIVAS` con foto, nombre del candidato, partido y lema.
- **FR-008**: El sistema DEBE indicar el estado de la votación al mostrar el tarjetón.
- **FR-009**: El sistema DEBE ordenar las candidaturas en el tarjetón según el criterio definido por el Gestor
  Votaciones. Por defecto, se ordenan por fecha de inscripción.
- **FR-010**: Solo el rol Gestor Votaciones DEBE poder inscribir y cancelar candidaturas.
- **FR-011**: Cualquier usuario autenticado (independientemente de su rol) DEBE poder ver el tarjetón.

### Key Entities

- **Candidatura**: Relación que vincula un candidato a una votación específica, definiendo la oferta electoral del
  tarjetón. Atributos: id, candidato, votación, fecha_inscripcion, estado (`ACTIVA` | `CANCELADA`). Orden por defecto en
  el tarjetón: fecha de inscripción.

## Success Criteria

### Measurable Outcomes

- **SC-001**: El Gestor Votaciones puede asignar múltiples candidatos a una votación en una sola operación.
- **SC-002**: El tarjetón digital se muestra correctamente con todos los candidatos y partidos en menos de 3 segundos.
- **SC-003**: Un usuario puede identificar claramente por quién votar a partir de la información mostrada en el
  tarjetón.
