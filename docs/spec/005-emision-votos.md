# Feature Specification: Emisión de Votos

**Created**: 2026-06-19

## User Scenarios & Testing

### User Story 1 - Emitir voto (Priority: P1)

Un usuario autenticado emite su voto seleccionando un candidato en una votación que está `EN_PROGRESO`. Al emitir el
voto, el sistema crea un `Voto` (inmutable, sin referencia al usuario) y una `Participación` que vincula al usuario con
ese voto.

**Why this priority**: Es el acto central de todo el sistema electoral. Sin emisión de votos, nada de lo demás tiene
sentido.

**Independent Test**: Puede probarse con un usuario habilitado, una votación `EN_PROGRESO` con candidatos asignados,
emitiendo el voto y verificando que se crean tanto el `Voto` como la `Participación` con estado `VÁLIDO`.

**Acceptance Scenarios**:

1. **Scenario**: Emisión exitosa de voto
    - **Given** un usuario autenticado y una votación `EN_PROGRESO` con candidatos
    - **When** el usuario selecciona un candidato y emite su voto
    - **Then** se crea un `Voto` (votacion_id, candidato_id) y una `Participación` (usuario_id, voto_id, estado=
      `VÁLIDO`, fecha_emision)

2. **Scenario**: Votar en votación no `EN_PROGRESO`
    - **Given** una votación está en estado `ACTIVA`, `FINALIZADA`, `CANCELADA` o `COMPLETADA`
    - **When** un usuario intenta emitir su voto
    - **Then** el sistema rechaza la operación e informa que la votación no está recibiendo votos

3. **Scenario**: Usuario ya votó en esa votación
    - **Given** un usuario ya tiene una `Participación` en una votación
    - **When** intenta emitir otro voto en la misma votación
    - **Then** el sistema rechaza la operación e informa que ya registró su participación

4. **Scenario**: Seleccionar candidato no asignado a la votación
    - **Given** una votación `EN_PROGRESO` con ciertos candidatos asignados
    - **When** un usuario intenta votar por un candidato que no está asignado a esa votación
    - **Then** el sistema rechaza la operación e informa que el candidato no participa en esta votación

5. **Scenario**: Usuario no autenticado
    - **Given** una votación `EN_PROGRESO`
    - **When** un usuario no autenticado intenta emitir un voto
    - **Then** el sistema rechaza la solicitud y requiere autenticación

---

### User Story 2 - Ver historial de participaciones (Priority: P2)

Un usuario autenticado consulta el historial de todas las votaciones en las que ha participado, sin revelar por qué
candidato votó. El historial se construye a partir de sus `Participaciones`, que solo contienen referencias al `Voto`,
no al `Candidato` directamente.

**Why this priority**: Brinda trazabilidad y confianza al votante sobre su participación, pero no es esencial para el
acto de votar.

**Independent Test**: Puede probarse con un usuario que haya emitido votos, consultando su historial de participaciones
y verificando que aparecen todas sin revelar el candidato elegido.

**Acceptance Scenarios**:

1. **Scenario**: Historial con participaciones
    - **Given** un usuario tiene varias `Participaciones` en distintas votaciones
    - **When** consulta su historial
    - **Then** el sistema muestra cada votación, el estado de la participación (`VÁLIDO` o `ANULADO`) y la fecha de
      emisión, sin revelar el candidato

2. **Scenario**: Historial vacío
    - **Given** un usuario no tiene ninguna `Participación`
    - **When** consulta su historial
    - **Then** el sistema devuelve un historial vacío sin errores

3. **Scenario**: Historial con participaciones válidas y anuladas
    - **Given** un usuario tiene `Participaciones` en estado `VÁLIDO` y `ANULADO`
    - **When** consulta su historial
    - **Then** el sistema muestra ambas con el estado correspondiente, sin revelar el candidato

4. **Scenario**: Usuario no autenticado
    - **Given** un usuario no ha iniciado sesión
    - **When** intenta consultar su historial de participaciones
    - **Then** el sistema rechaza la solicitud y requiere autenticación

---

### Edge Cases

- ¿El historial de participaciones muestra el candidato o solo la votación en que participó?
    - El historial solo muestra la votación, el estado y la fecha. No muestra el candidato, ya que la `Participación`
      referencia un `Voto` (que contiene el `candidato_id`), pero el historial se construye desde `Participación` sin
      resolver la entidad `Voto`, garantizando el voto secreto.

## Requirements

### Functional Requirements

- **FR-001**: El sistema DEBE permitir a cualquier usuario autenticado emitir un voto en una votación `EN_PROGRESO`.
- **FR-002**: Al emitir un voto, el sistema DEBE crear un `Voto` (inmutable, `votacion_id`, `candidato_id`) y una
  `Participación` (`usuario_id`, `voto_id`, estado=`VÁLIDO`, `fecha_emision`).
- **FR-003**: El sistema DEBE impedir que un usuario tenga más de una `Participación` en la misma votación.
- **FR-004**: El sistema DEBE validar que el candidato seleccionado tenga una candidatura `ACTIVA` en la votación.
- **FR-005**: El sistema DEBE permitir a cualquier usuario autenticado consultar su historial de `Participaciones`.
- **FR-006**: El historial de `Participaciones` DEBE mostrar la votación, el estado y la fecha de emisión, sin revelar el candidato (principio de voto secreto). El `Voto` no contiene referencia al usuario, por lo que el candidato es inaccesible desde el historial.

### Key Entities

- **Voto**: Registro inmutable de la decisión electoral. No contiene referencia al usuario que votó. Atributos: `id`, `votacion_id`, `candidato_id`. Un `Voto` nunca se modifica; existe desde que se emite.
- **Participación**: Entidad que vincula un usuario con su voto, permitiendo trazabilidad sin comprometer el voto secreto. Atributos: `id`, `usuario_id`, `voto_id`, `estado` (`VÁLIDO` | `ANULADO`), `fecha_emision`. La anulación y auditoría de participaciones corresponde al Módulo 3.

## Success Criteria

### Measurable Outcomes

- **SC-001**: Un usuario puede emitir su voto en menos de 1 minuto desde que ve el tarjetón.
- **SC-002**: El sistema rechaza votos duplicados con un mensaje claro.
- **SC-003**: El historial de votos refleja correctamente todas las votaciones en las que participó el usuario, sin
  revelar el candidato elegido, respetando el voto secreto.
