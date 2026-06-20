# Stakeholders

## Gestor Candidaturas

Representa al funcionario electoral encargado de administrar la oferta electoral del sistema. Crea, edita e inhabilita
partidos políticos (`nombre`, `descripción`, `logo`) y registra, modifica o da de baja candidatos, vinculándolos a un
partido y a un usuario ya registrado. También gestiona la foto del candidato y demás atributos propios del contexto
electoral que no pertenecen al perfil de usuario base. Es el responsable de que el tarjetón digital refleje
correctamente los partidos y candidatos que compiten en cada jornada.

## Gestor Electoral

Representa al administrador electoral que configura y controla el ciclo de vida de una jornada de votación. Crea
votaciones definiendo su nombre, tipo (`PRESIDENCIA`, `CONGRESO`, `ALCALDIA`, `GOBERNACION`) y alcance geográfico (
`MUNICIPAL`, `DEPARTAMENTAL`, `REGIONAL`, `NACIONAL`), les asigna los candidatos y partidos que compiten, y gestiona sus
transiciones de estado: de `ACTIVA` a `EN_PROGRESO`, de `EN_PROGRESO` a `FINALIZADA`, o a `CANCELADA` si la jornada se
suspende antes de completarse. Es quien apertura y cierra oficialmente la jornada electoral en el sistema.

## Votante

Representa al ciudadano registrado que participa emitiendo su voto. Se registra en el sistema con sus datos de
identidad (`nombre`, `email`, `teléfono`, `documento`, `lugar de inscripción`), debe ser habilitado por la autoridad
electoral para poder votar (transición de `ACTIVO` a `HABILITADO`) y, una vez habilitado, se autentica y emite su voto
de forma individual durante una votación en estado `EN_PROGRESO`. Cada voto queda registrado con estado `VÁLIDO` y se
contabiliza en los resultados, o `ANULADO` si se detectan inconsistencias.
