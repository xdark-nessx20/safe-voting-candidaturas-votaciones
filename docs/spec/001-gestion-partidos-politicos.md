# Feature Specification: Gestión de Partidos Políticos

**Created**: 2026-06-19

## User Scenarios & Testing

### User Story 1 - Crear partido político (Priority: P1)

El Gestor Candidaturas registra un nuevo partido político en el sistema para que pueda postular candidatos en las
jornadas electorales.

**Why this priority**: Sin partidos registrados no existe oferta electoral. Es la base sobre la que se construye todo el
módulo de candidaturas.

**Independent Test**: Puede probarse creando un partido con nombre, descripción y logo, y verificando que queda
registrado y disponible para ser consultado.

**Acceptance Scenarios**:

1. **Scenario**: Creación exitosa de un partido
    - **Given** el Gestor Candidaturas está autenticado
    - **When** ingresa nombre, descripción y URL del logo
    - **Then** el partido queda registrado y es visible en el listado de partidos

2. **Scenario**: Creación con nombre duplicado
    - **Given** ya existe un partido con el nombre "Partido X"
    - **When** el Gestor Candidaturas intenta crear otro partido con el mismo nombre
    - **Then** el sistema rechaza la operación e informa que el nombre ya está en uso

3. **Scenario**: Creación sin logo
    - **Given** el Gestor Candidaturas está autenticado
    - **When** ingresa nombre y descripción, pero omite la URL del logo
    - **Then** el partido queda registrado sin imagen de logo

---

### User Story 2 - Listar partidos (Priority: P1)

El Gestor Candidaturas consulta todos los partidos políticos registrados en el sistema.

**Why this priority**: Es necesario poder ver qué partidos existen antes de editarlos, inhabilitarlos o asignarles
candidatos.

**Independent Test**: Puede probarse creando uno o más partidos y verificando que el listado los devuelve correctamente.

**Acceptance Scenarios**:

1. **Scenario**: Listado con partidos registrados
    - **Given** existen varios partidos registrados en el sistema
    - **When** el Gestor Candidaturas solicita el listado de partidos
    - **Then** el sistema devuelve todos los partidos con sus datos (nombre, descripción, logo)

2. **Scenario**: Listado vacío
    - **Given** no hay partidos registrados
    - **When** el Gestor Candidaturas solicita el listado de partidos
    - **Then** el sistema devuelve un listado vacío sin errores

---

### User Story 3 - Editar partido político (Priority: P2)

El Gestor Candidaturas modifica los datos de un partido existente para corregir o actualizar su información.

**Why this priority**: Permite mantener actualizada la información de los partidos sin necesidad de borrar y recrear,
pero requiere que el partido ya exista.

**Independent Test**: Puede probarse creando un partido, luego modificando su descripción o logo, y verificando que los
cambios se reflejan al listarlo.

**Acceptance Scenarios**:

1. **Scenario**: Edición exitosa
    - **Given** existe un partido registrado
    - **When** el Gestor Candidaturas modifica su descripción y/o logo
    - **Then** los cambios quedan persistidos y son visibles en el listado

2. **Scenario**: Edición de un partido inexistente
    - **Given** no existe un partido con el identificador proporcionado
    - **When** el Gestor Candidaturas intenta editarlo
    - **Then** el sistema rechaza la operación e informa que el partido no existe

3. **Scenario**: Cambio de nombre a uno ya en uso
    - **Given** existen los partidos "Partido A" y "Partido B"
    - **When** el Gestor Candidaturas intenta cambiar el nombre de "Partido A" a "Partido B"
    - **Then** el sistema rechaza la operación por duplicidad de nombre

---

### User Story 4 - Inhabilitar partido político (Priority: P2)

El Gestor Candidaturas da de baja un partido para que no pueda participar en nuevas votaciones, sin eliminar su registro
histórico.

**Why this priority**: Es una operación administrativa menos frecuente que las anteriores, y requiere que el partido ya
exista y esté habilitado.

**Independent Test**: Puede probarse creando un partido, inhabilitándolo, y verificando que ya no aparece como opción
para nuevas votaciones pero sí en registros históricos.

**Acceptance Scenarios**:

1. **Scenario**: Inhabilitación exitosa
    - **Given** existe un partido habilitado
    - **When** el Gestor Candidaturas lo inhabilita
    - **Then** el partido queda inhabilitado y no puede ser asignado a nuevas votaciones

2. **Scenario**: Inhabilitar un partido ya inhabilitado
    - **Given** un partido ya está inhabilitado
    - **When** el Gestor Candidaturas intenta inhabilitarlo nuevamente
    - **Then** el sistema rechaza la operación e informa que el partido ya está inhabilitado

3. **Scenario**: Inhabilitar un partido inexistente
    - **Given** no existe un partido con el identificador proporcionado
    - **When** el Gestor Candidaturas intenta inhabilitarlo
    - **Then** el sistema rechaza la operación e informa que el partido no existe

---

### Edge Cases

- ¿Qué sucede si se intenta inhabilitar un partido que tiene candidatos asignados a una votación en curso (EN_PROGRESO)?
    - El sistema DEBE impedir inhabilitar un partido que tenga candidatos asignados a una votación en curso. La
      inhabilitación solo se permite cuando ninguna votación EN_PROGRESO incluya candidatos de ese partido.
- ¿Qué sucede si la URL del logo no es accesible o no es una URL válida?
    - La subida y validación de imágenes se delega a un servicio externo (ej. Cloudinary). El sistema solo persiste la
      URL pública devuelta por ese servicio. Si el servicio externo falla, el partido puede crearse sin logo, ya que
      este es opcional.
- ¿El nombre del partido tiene un límite de longitud? ¿Qué caracteres se permiten?
    - Tiene un límite de 100 caracteres y solo se admiten letras mayúsculas (A-Z).

## Requirements

### Functional Requirements

- **FR-001**: El sistema DEBE permitir al Gestor Candidaturas crear partidos políticos con nombre, descripción (
  opcional) y logo URL (opcional).
- **FR-002**: El sistema DEBE validar que el nombre del partido sea único, tenga máximo 100 caracteres y solo contenga
  letras mayúsculas (A-Z).
- **FR-003**: El sistema DEBE permitir al Gestor Candidaturas listar todos los partidos registrados.
- **FR-004**: El sistema DEBE permitir al Gestor Candidaturas editar los datos de un partido existente.
- **FR-005**: El sistema DEBE validar que el nuevo nombre no esté en uso por otro partido y cumpla las mismas reglas de
  formato al editar.
- **FR-006**: El sistema DEBE permitir al Gestor Candidaturas inhabilitar un partido.
- **FR-007**: El sistema DEBE impedir inhabilitar un partido que tenga candidatos asignados a una votación EN_PROGRESO.
- **FR-008**: El sistema DEBE impedir que un partido inhabilitado sea asignado a nuevas votaciones.
- **FR-009**: El sistema NO DEBE eliminar físicamente los partidos inhabilitados, preservando la integridad de los
  registros históricos.
- **FR-010**: Solo el rol Gestor Candidaturas DEBE tener acceso a estas operaciones.

### Key Entities

- **Partido Político**: Agrupación que postula candidatos. Atributos: nombre (único), descripción (opcional), logo (URL
  pública, opcional — se permite crear el partido sin logo si el servicio
  de almacenamiento de imágenes falla), estado (habilitado/inhabilitado).

## Success Criteria

### Measurable Outcomes

- **SC-001**: El Gestor Candidaturas puede crear un partido en menos de 1 minuto.
- **SC-002**: El listado de partidos se obtiene sin errores incluso con cientos de registros.
- **SC-003**: Un partido inhabilitado no aparece como opción en nuevas votaciones pero se conserva en consultas
  históricas.
