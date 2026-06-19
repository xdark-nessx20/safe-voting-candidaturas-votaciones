# Feature Specification: Emisión de Votos

**Created**: 2026-06-19

## User Scenarios & Testing

### User Story 1 - Emitir voto (Priority: P1)

Un usuario autenticado emite su voto seleccionando un candidato en una votación que está `EN_PROGRESO`.

**Why this priority**: Es el acto central de todo el sistema electoral. Sin emisión de votos, nada de lo demás tiene
sentido.

**Independent Test**: Puede probarse con un usuario habilitado, una votación `EN_PROGRESO` con candidatos asignados,
emitiendo el voto y verificando que queda registrado como `VÁLIDO`.

**Acceptance Scenarios**:

1. **Scenario**: Emisión exitosa de voto
    - **Given** un usuario autenticado y una votación `EN_PROGRESO` con candidatos
    - **When** el usuario selecciona un candidato y emite su voto
    - **Then** el voto queda registrado con estado `VÁLIDO` y se asocia al candidato y a la votación

2. **Scenario**: Votar en votación no `EN_PROGRESO`
    - **Given** una votación está en estado `ACTIVA`, `FINALIZADA`, `CANCELADA` o `COMPLETADA`
    - **When** un usuario intenta emitir su voto
    - **Then** el sistema rechaza la operación e informa que la votación no está recibiendo votos

3. **Scenario**: Usuario ya votó en esa votación
    - **Given** un usuario ya emitió un voto (válido o anulado) en una votación
    - **When** intenta emitir otro voto en la misma votación
    - **Then** el sistema rechaza la operación e informa que ya registró su voto. La anulación del voto no libera al
      usuario para votar nuevamente.

4. **Scenario**: Seleccionar candidato no asignado a la votación
    - **Given** una votación `EN_PROGRESO` con ciertos candidatos asignados
    - **When** un usuario intenta votar por un candidato que no está asignado a esa votación
    - **Then** el sistema rechaza la operación e informa que el candidato no participa en esta votación

5. **Scenario**: Usuario no autenticado
    - **Given** una votación `EN_PROGRESO`
    - **When** un usuario no autenticado intenta emitir un voto
    - **Then** el sistema rechaza la solicitud y requiere autenticación

---

### User Story 2 - Ver historial de votos propios (Priority: P2)

Un usuario autenticado consulta el historial de todas las votaciones en las que ha participado, sin revelar por qué
candidato votó, respetando el principio de voto secreto.

**Why this priority**: Brinda trazabilidad y confianza al votante sobre su participación, pero no es esencial para el
acto de votar.

**Independent Test**: Puede probarse con un usuario que haya emitido votos, consultando su historial y verificando que
aparecen todas sus participaciones sin mostrar el candidato elegido.

**Acceptance Scenarios**:

1. **Scenario**: Historial con votos
    - **Given** un usuario ha emitido votos en varias votaciones
    - **When** consulta su historial de votos
    - **Then** el sistema muestra cada votación en la que participó, con el estado del voto (`VÁLIDO` o `ANULADO`), sin
      revelar el candidato elegido

2. **Scenario**: Historial vacío
    - **Given** un usuario no ha emitido ningún voto
    - **When** consulta su historial de votos
    - **Then** el sistema devuelve un historial vacío sin errores

3. **Scenario**: Historial con votos válidos y anulados
    - **Given** un usuario tiene votos en estado `VÁLIDO` y `ANULADO`
    - **When** consulta su historial
    - **Then** el sistema muestra ambos con el estado correspondiente, sin revelar el candidato

4. **Scenario**: Usuario no autenticado
    - **Given** un usuario no ha iniciado sesión
    - **When** intenta consultar su historial de votos
    - **Then** el sistema rechaza la solicitud y requiere autenticación

---

### User Story 3 - Anular voto (Priority: P3)

El Gestor Votaciones marca un voto como `ANULADO` por inconsistencias detectadas durante el proceso de auditoría, que se
realiza una vez que la votación ha sido `FINALIZADA` y antes de pasar a `COMPLETADA`. El voto anulado se excluye del
conteo de resultados finales.

**Why this priority**: Es una operación de control de integridad, reactiva y menos frecuente. Solo se aplica cuando se
detectan irregularidades durante la auditoría.

**Independent Test**: Puede probarse emitiendo un voto válido en una votación `FINALIZADA`, anulándolo durante la
auditoría, y verificando que cambia a `ANULADO` y no se contabiliza en los resultados públicos de `COMPLETADA`.

**Acceptance Scenarios**:

1. **Scenario**: Anulación exitosa
    - **Given** existe un voto en estado `VÁLIDO`
    - **When** el Gestor Votaciones lo anula
    - **Then** el voto pasa a estado `ANULADO` y se excluye del conteo de resultados

2. **Scenario**: Anular voto ya anulado
    - **Given** un voto ya está en estado `ANULADO`
    - **When** el Gestor Votaciones intenta anularlo nuevamente
    - **Then** el sistema rechaza la operación e informa que el voto ya está anulado

3. **Scenario**: Anular voto inexistente
    - **Given** no existe un voto con el identificador proporcionado
    - **When** el Gestor Votaciones intenta anularlo
    - **Then** el sistema rechaza la operación e informa que el voto no existe

---

### Edge Cases

- ¿Un voto anulado debería liberar al usuario para que pueda votar nuevamente en esa misma votación?
    - No. Un usuario solo puede emitir un voto por votación, independientemente de si ese voto es marcado después como
      `ANULADO`. La anulación no libera al usuario para votar de nuevo.
- ¿Se debe registrar quién y cuándo anuló un voto con fines de auditoría?
    - Sí, se debe registrar qué Gestor Votaciones anuló el voto y en qué momento. Estos registros de auditoría deben
      almacenarse en una entidad separada de la entidad principal `Voto`, para mantener la trazabilidad sin alterar el
      voto original.
- ¿Se puede anular un voto después de que la votación haya FINALIZADO?
    - Sí, de hecho lo ideal es que la revisión y anulación de votos se haga durante la etapa de auditoría, una vez que
      la votación ha sido `FINALIZADA`. Para ello, la votación cuenta con un nuevo estado `COMPLETADA` como estado
      terminal. Durante `FINALIZADA` se realiza el proceso de auditoría (incluyendo anulaciones), y al transicionar a
      `COMPLETADA`, los resultados se hacen públicos y ya no se aceptan más cambios.
- ¿El historial de votos propios muestra el candidato o solo la votación en que participó?
    - El historial solo muestra la votación en la que el usuario participó. No se muestra el candidato por el que votó,
      ya que debe respetarse el principio de voto secreto.

## Requirements

### Functional Requirements

- **FR-001**: El sistema DEBE permitir a cualquier usuario autenticado emitir un voto en una votación `EN_PROGRESO`.
- **FR-002**: El sistema DEBE registrar cada voto con estado `VÁLIDO` y asociarlo al candidato seleccionado y a la
  votación correspondiente.
- **FR-003**: El sistema DEBE impedir que un usuario emita más de un voto en la misma votación, incluso si su voto fue
  anulado posteriormente.
- **FR-004**: El sistema DEBE validar que el candidato seleccionado tenga una candidatura `ACTIVA` en la votación.
- **FR-005**: El sistema DEBE permitir a cualquier usuario autenticado consultar su historial de votos.
- **FR-006**: El historial DEBE mostrar únicamente la votación en la que participó y el estado del voto (`VÁLIDO` o
  `ANULADO`), sin revelar el candidato elegido (principio de voto secreto).
- **FR-007**: El sistema DEBE permitir al Gestor Votaciones anular un voto durante la etapa de auditoría (votación
  `FINALIZADA`), cambiando su estado a `ANULADO`.
- **FR-008**: El sistema DEBE impedir anular un voto que ya esté en estado `ANULADO`.
- **FR-009**: El sistema DEBE registrar en una entidad de auditoría separada qué Gestor Votaciones anuló cada voto y en
  qué momento.
- **FR-010**: Los votos `ANULADOS` NO DEBEN contabilizarse para resultados, pero DEBEN registrarse para auditoría.
- **FR-011**: Solo el rol Gestor Votaciones DEBE poder anular votos.

### Key Entities

- **Voto**: Registro individual de la decisión de un votante. Atributos: relación con Usuario, relación con Candidato,
  relación con Votación, estado (`VÁLIDO` | `ANULADO`). Los datos de auditoría de anulación (quién anuló, cuándo) se
  registran en una entidad separada.

## Success Criteria

### Measurable Outcomes

- **SC-001**: Un usuario puede emitir su voto en menos de 1 minuto desde que ve el tarjetón.
- **SC-002**: El sistema rechaza votos duplicados con un mensaje claro.
- **SC-003**: El historial de votos refleja correctamente todas las votaciones en las que participó el usuario, sin
  revelar el candidato elegido, respetando el voto secreto.
