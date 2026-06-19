# Feature Specification: Gestión de Candidatos

**Created**: 2026-06-19

## User Scenarios & Testing

### User Story 1 - Registrar candidato (Priority: P1)

El Gestor Candidaturas registra un nuevo candidato vinculando un usuario del sistema a un partido político, añadiendo la
foto y demás atributos electorales.

**Why this priority**: Sin candidatos no hay oferta electoral sobre la cual votar. Es la operación fundamental del
módulo.

**Independent Test**: Puede probarse teniendo un usuario y un partido existentes, registrando al candidato y verificando
que queda vinculado correctamente.

**Acceptance Scenarios**:

1. **Scenario**: Registro exitoso de un candidato
    - **Given** existe un usuario registrado y un partido habilitado
    - **When** el Gestor Candidaturas asigna el usuario como candidato del partido con su foto (URL)
    - **Then** el candidato queda registrado y es visible en el listado

2. **Scenario**: Registro con usuario inexistente
    - **Given** no existe un usuario con el identificador proporcionado
    - **When** el Gestor Candidaturas intenta registrar el candidato
    - **Then** el sistema rechaza la operación e informa que el usuario no existe

3. **Scenario**: Registro con partido inhabilitado
    - **Given** existe un usuario y un partido inhabilitado
    - **When** el Gestor Candidaturas intenta registrar al candidato en ese partido
    - **Then** el sistema rechaza la operación e informa que el partido no está habilitado

4. **Scenario**: Usuario ya registrado como candidato en el mismo partido
    - **Given** un usuario ya está registrado como candidato del "Partido A"
    - **When** el Gestor Candidaturas intenta registrarlo nuevamente en el "Partido A"
    - **Then** el sistema rechaza la operación por duplicidad

---

### User Story 2 - Listar candidatos (Priority: P1)

El Gestor Candidaturas consulta todos los candidatos registrados en el sistema, con su partido y usuario asociado.

**Why this priority**: Es necesario visualizar los candidatos existentes antes de editarlos, darlos de baja o asignarlos
a votaciones.

**Independent Test**: Puede probarse registrando candidatos y verificando que el listado los devuelve con los datos
esperados.

**Acceptance Scenarios**:

1. **Scenario**: Listado con candidatos registrados
    - **Given** existen varios candidatos registrados con distintos partidos
    - **When** el Gestor Candidaturas solicita el listado
    - **Then** el sistema devuelve todos los candidatos con nombre del usuario, partido y foto

2. **Scenario**: Listado vacío
    - **Given** no hay candidatos registrados
    - **When** el Gestor Candidaturas solicita el listado
    - **Then** el sistema devuelve un listado vacío sin errores

---

### User Story 3 - Editar candidato (Priority: P3)

El Gestor Candidaturas modifica los datos de un candidato existente, como su foto o el partido al que pertenece.

**Why this priority**: Permite corregir datos sin borrar y recrear el candidato, pero requiere que el candidato ya
exista.

**Independent Test**: Puede probarse registrando un candidato, luego modificando su foto o partido, y verificando que
los cambios persisten.

**Acceptance Scenarios**:

1. **Scenario**: Edición exitosa de foto y partido
    - **Given** existe un candidato registrado
    - **When** el Gestor Candidaturas modifica su foto, partido, o ambos
    - **Then** los cambios quedan persistidos y se reflejan en el listado

2. **Scenario**: Edición de candidato inexistente
    - **Given** no existe un candidato con el identificador proporcionado
    - **When** el Gestor Candidaturas intenta editarlo
    - **Then** el sistema rechaza la operación e informa que el candidato no existe

3. **Scenario**: Cambio a un partido inhabilitado
    - **Given** existe un candidato y un partido inhabilitado
    - **When** el Gestor Candidaturas intenta cambiar el partido del candidato al partido inhabilitado
    - **Then** el sistema rechaza la operación e informa que el partido no está habilitado

---

### User Story 4 - Dar de baja candidato (Priority: P2)

El Gestor Candidaturas da de baja un candidato de forma administrativa, registrando un motivo sólido. El candidato pasa
a estado `suspendido` y queda permanentemente inhabilitado para participar en futuras elecciones, pero su registro
histórico se conserva para auditoría.

**Why this priority**: Operación administrativa necesaria cuando un candidato es descalificado por motivos legales,
disciplinarios u otras causas que deben constar en su historial.

**Independent Test**: Puede probarse registrando un candidato, dándolo de baja con un motivo, y verificando que queda
inactivo, que el motivo queda registrado, y que ya no puede ser asignado a ninguna votación futura.

**Acceptance Scenarios**:

1. **Scenario**: Baja administrativa exitosa
    - **Given** existe un candidato activo
    - **When** el Gestor Candidaturas lo da de baja registrando un motivo
    - **Then** el candidato queda suspendido, el motivo queda registrado y el candidato no puede ser asignado a ninguna
      votación

2. **Scenario**: Baja sin motivo
    - **Given** existe un candidato activo
    - **When** el Gestor Candidaturas intenta darlo de baja sin especificar un motivo
    - **Then** el sistema rechaza la operación e informa que el motivo es obligatorio

3. **Scenario**: Dar de baja un candidato ya suspendido
    - **Given** un candidato ya está suspendido
    - **When** el Gestor Candidaturas intenta darlo de baja nuevamente
    - **Then** el sistema rechaza la operación e informa que el candidato ya está suspendido

4. **Scenario**: Dar de baja un candidato inexistente
    - **Given** no existe un candidato con el identificador proporcionado
    - **When** el Gestor Candidaturas intenta darlo de baja
    - **Then** el sistema rechaza la operación e informa que el candidato no existe

---

### User Story 5 - Cancelar candidatura (Priority: P2)

Un candidato solicita al Gestor Candidaturas la cancelación voluntaria de su candidatura, por ejemplo para apoyar a otro
candidato. El candidato pasa a estado `inactivo`. Esta operación no registra motivo ni inhabilita al candidato para
futuros procesos electorales, permitiéndole volver a `activo` si desea postularse a otra elección.

**Why this priority**: Responde a un escenario real donde candidatos renuncian a una candidatura específica sin que ello
implique una descalificación permanente.

**Independent Test**: Puede probarse con un candidato activo inscrito en una votación, cancelando su candidatura y
verificando que el cambio se aplica pero el candidato sigue habilitado para otras elecciones.

**Acceptance Scenarios**:

1. **Scenario**: Cancelación voluntaria exitosa
    - **Given** existe un candidato activo
    - **When** el Gestor Candidaturas cancela su candidatura
    - **Then** el candidato pasa a estado inactivo pero permanece habilitado para ser postulado en otros procesos
      electorales

2. **Scenario**: Cancelar candidatura inexistente
    - **Given** no existe un candidato con el identificador proporcionado
    - **When** el Gestor Candidaturas intenta cancelar su candidatura
    - **Then** el sistema rechaza la operación e informa que el candidato no existe

3. **Scenario**: Candidato suspendido intenta cancelar
    - **Given** un candidato está suspendido
    - **When** el Gestor Candidaturas intenta cancelar su candidatura
    - **Then** el sistema rechaza la operación e informa que el candidato no está activo

---

### Edge Cases

- ¿Qué sucede si se da de baja un candidato que está asignado a una votación EN_PROGRESO?
    - No se permite esta operación. La baja administrativa solo puede realizarse mientras la votación esté en estado
      ACTIVA y dentro del rango de fechas establecido por el Gestor Votaciones. El motivo de la baja queda registrado
      obligatoriamente y será tenido en cuenta para futuros procesos electorales.
- ¿Qué sucede si la URL de la foto no es accesible o no es una URL válida?
    - La subida y validación de imágenes se delega a un servicio externo (ej. Cloudinary). El sistema solo persiste la
      URL pública devuelta por ese servicio. Si el servicio externo falla, el candidato puede registrarse sin foto, ya
      que esta es opcional.
- ¿Un mismo usuario puede ser candidato en múltiples partidos simultáneamente?
    - No. Un candidato solo puede pertenecer a un partido a la vez. El sistema DEBE impedir registrar al mismo usuario
      como candidato en más de un partido simultáneamente.
- ¿Un candidato puede cambiarse de partido durante una votación activa?
    - El cambio de partido de un candidato solo se permite si el candidato no está inscrito en ninguna votación. Si está
      asignado a una votación, el sistema DEBE rechazar el cambio de partido.

## Requirements

### Functional Requirements

- **FR-001**: El sistema DEBE permitir al Gestor Candidaturas registrar un candidato vinculando un usuario existente a
  un partido habilitado.
- **FR-002**: El sistema DEBE validar que el usuario y el partido existan y estén en estado válido al registrar un
  candidato.
- **FR-003**: El sistema DEBE impedir registrar al mismo usuario como candidato del mismo partido más de una vez, y
  también impedir que un mismo usuario sea candidato en más de un partido simultáneamente.
- **FR-004**: El sistema DEBE permitir al Gestor Candidaturas listar todos los candidatos con sus datos y relaciones.
- **FR-005**: El sistema DEBE permitir al Gestor Candidaturas editar los datos de un candidato existente (lema, foto,
  partido).
- **FR-006**: El sistema DEBE validar que el nuevo partido esté habilitado al editar un candidato.
- **FR-007**: El sistema DEBE permitir al Gestor Candidaturas dar de baja un candidato de forma administrativa,
  registrando obligatoriamente un motivo, y cambiando su estado a `suspendido`. Esta operación solo se permite cuando la
  votación está en estado ACTIVA.
- **FR-008**: El sistema DEBE conservar el motivo de la baja administrativa en el historial del candidato para ser
  tenido en cuenta en futuros procesos electorales.
- **FR-009**: El sistema DEBE permitir al Gestor Candidaturas cancelar la candidatura de un candidato de forma
  voluntaria, cambiando su estado a `inactivo`. Esta operación no inhabilita al candidato para otros procesos
  electorales.
- **FR-010**: El sistema DEBE impedir cambiar el partido de un candidato que esté inscrito en alguna votación.
- **FR-011**: El sistema DEBE impedir que un candidato suspendido sea asignado a nuevas votaciones.
- **FR-012**: El sistema DEBE impedir que un candidato inactivo sea asignado a nuevas votaciones mientras permanezca en
  ese estado.
- **FR-013**: El sistema NO DEBE eliminar físicamente los candidatos dados de baja o cancelados.
- **FR-014**: Solo el rol Gestor Candidaturas DEBE tener acceso a estas operaciones.

### Key Entities

- **Candidato**: Representa a un usuario postulado por un partido. Atributos: relación con Usuario (obligatorio),
  relación con Partido (obligatorio, un solo partido a la vez), lema (opcional), foto (URL, opcional — se permite crear
  el candidato sin foto si el servicio de almacenamiento de imágenes falla), estado (activo/inactivo/suspendido). El
  estado `suspendido` corresponde a una baja administrativa con motivo obligatorio registrado en el historial. El estado
  `inactivo` corresponde a una cancelación voluntaria de candidatura, que permite al candidato volver a `activo` para
  postularse a otros procesos electorales.

## Success Criteria

### Measurable Outcomes

- **SC-001**: El Gestor Candidaturas puede registrar un candidato en menos de 2 minutos.
- **SC-002**: El listado de candidatos muestra correctamente el nombre del usuario y el partido asociado.
- **SC-003**: Un candidato dado de baja no aparece en nuevas asignaciones a votaciones pero se conserva en registros
  históricos.
