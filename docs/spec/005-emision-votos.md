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
    - **Given** un usuario ya tiene una `Participación` (válida o anulada) en una votación
    - **When** intenta emitir otro voto en la misma votación
    - **Then** el sistema rechaza la operación e informa que ya registró su participación. La anulación de la
      `Participación` no libera al usuario para votar nuevamente.

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

### User Story 3 - Anular participación (Priority: P3)

El Gestor Votaciones marca una `Participación` como `ANULADA` por inconsistencias detectadas durante el proceso de
auditoría, que se realiza una vez que la votación ha sido `FINALIZADA` y antes de pasar a `COMPLETADA`. El `Voto`
asociado permanece intacto, pero la `Participación` anulada hace que ese `Voto` se excluya del conteo de resultados
finales.

**Why this priority**: Es una operación de control de integridad, reactiva y menos frecuente. Solo se aplica cuando se
detectan irregularidades durante la auditoría.

**Independent Test**: Puede probarse emitiendo un voto en una votación `FINALIZADA`, anulando su `Participación` durante
la auditoría, y verificando que el `Voto` sigue existiendo pero la `Participación` pasa a `ANULADO` y no se contabiliza
en los resultados públicos de `COMPLETADA`.

**Acceptance Scenarios**:

1. **Scenario**: Anulación exitosa
    - **Given** existe una `Participación` en estado `VÁLIDO`
    - **When** el Gestor Votaciones la anula
    - **Then** la `Participación` pasa a estado `ANULADO` y el `Voto` asociado se excluye del conteo de resultados. El
      `Voto` permanece intacto.

2. **Scenario**: Anular participación ya anulada
    - **Given** una `Participación` ya está en estado `ANULADO`
    - **When** el Gestor Votaciones intenta anularla nuevamente
    - **Then** el sistema rechaza la operación e informa que la participación ya está anulada

3. **Scenario**: Anular participación inexistente
    - **Given** no existe una `Participación` con el identificador proporcionado
    - **When** el Gestor Votaciones intenta anularla
    - **Then** el sistema rechaza la operación e informa que la participación no existe

---

### Edge Cases

- ¿Una participación anulada debería liberar al usuario para que pueda votar nuevamente en esa misma votación?
    - No. Un usuario solo puede tener una `Participación` por votación, independientemente de si fue anulada después. La
      anulación no libera al usuario para votar de nuevo.
- ¿Se debe registrar quién y cuándo anuló una participación con fines de auditoría?
    - Sí, se debe registrar qué Gestor Votaciones anuló la `Participación` y en qué momento. Estos registros de
      auditoría se almacenan en una entidad separada, sin modificar el `Voto` ni la `Participación` original.
- ¿Se puede anular una participación después de que la votación haya FINALIZADO?
    - Sí, de hecho lo ideal es que la revisión y anulación de participaciones se haga durante la etapa de auditoría, una
      vez que la votación ha sido `FINALIZADA`. Durante `FINALIZADA` se realiza el proceso de auditoría (incluyendo
      anulaciones), y al transicionar a `COMPLETADA`, los resultados se hacen públicos y ya no se aceptan más cambios.
- ¿El historial de participaciones muestra el candidato o solo la votación en que participó?
    - El historial solo muestra la votación, el estado y la fecha. No muestra el candidato, ya que la `Participación`
      referencia un `Voto` (que contiene el `candidato_id`), pero el historial se construye desde `Participación` sin
      resolver la entidad `Voto`, garantizando el voto secreto.

## Requirements

### Functional Requirements

- **FR-001**: El sistema DEBE permitir a cualquier usuario autenticado emitir un voto en una votación `EN_PROGRESO`.
- **FR-002**: Al emitir un voto, el sistema DEBE crear un `Voto` (inmutable, `votacion_id`, `candidato_id`) y una
  `Participación` (`usuario_id`, `voto_id`, estado=`VÁLIDO`, `fecha_emision`).
- **FR-003**: El sistema DEBE impedir que un usuario tenga más de una `Participación` en la misma votación, incluso si
  su participación anterior fue anulada.
- **FR-004**: El sistema DEBE validar que el candidato seleccionado tenga una candidatura `ACTIVA` en la votación.
- **FR-005**: El sistema DEBE permitir a cualquier usuario autenticado consultar su historial de `Participaciones`.
- **FR-006**: El historial de `Participaciones` DEBE mostrar la votación, el estado y la fecha de emisión, sin revelar
  el candidato (principio de voto secreto). El `Voto` no contiene referencia al usuario, por lo que el candidato es
  inaccesible desde el historial.
- **FR-007**: El sistema DEBE permitir al Gestor Votaciones anular una `Participación` durante la etapa de auditoría (
  votación `FINALIZADA`), cambiando su estado a `ANULADO`. El `Voto` asociado permanece intacto.
- **FR-008**: El sistema DEBE impedir anular una `Participación` que ya esté en estado `ANULADO`.
- **FR-009**: El sistema DEBE registrar en una entidad de auditoría separada qué Gestor Votaciones anuló cada
  `Participación` y en qué momento.
- **FR-010**: Para el conteo de resultados, solo se contabilizan los `Voto` cuya `Participación` asociada tenga estado
  `VÁLIDO`. Los `Voto` con `Participación` `ANULADO` se excluyen del conteo.
- **FR-011**: Solo el rol Gestor Votaciones DEBE poder anular `Participaciones`.

### Key Entities

- **Voto**: Registro inmutable de la decisión electoral. No contiene referencia al usuario que votó. Atributos: `id`,
  `votacion_id`, `candidato_id`. Un `Voto` nunca se modifica; existe desde que se emite y perdura aunque su
  `Participación` sea anulada.
- **Participación**: Entidad que vincula un usuario con su voto, permitiendo trazabilidad sin comprometer el voto
  secreto. Atributos: `id`, `usuario_id`, `voto_id`, `estado` (`VÁLIDO` | `ANULADO`), `fecha_emision`. El estado de la
  participación determina si el `Voto` asociado se contabiliza en resultados. Los datos de auditoría de anulación (quién
  anuló, cuándo) se registran en una entidad separada.

## Success Criteria

### Measurable Outcomes

- **SC-001**: Un usuario puede emitir su voto en menos de 1 minuto desde que ve el tarjetón.
- **SC-002**: El sistema rechaza votos duplicados con un mensaje claro.
- **SC-003**: El historial de votos refleja correctamente todas las votaciones en las que participó el usuario, sin
  revelar el candidato elegido, respetando el voto secreto.
