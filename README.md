# Sistema de Registro de Notas Universitarias

Aplicación de escritorio en Java y JavaFX con SQLite para la gestión de calificaciones universitarias. Incluye roles de estudiante, profesor y administrador con funciones de consulta, asignación de notas y operaciones CRUD.

## Características principales

- **CRUD completo** de usuarios (estudiantes, profesores, admins), materias y carreras, con validaciones robustas.
- **Vista de profesor:** permite calificar a cualquier estudiante de la carrera asociada a la materia, aunque no tenga notas previas.
- **Sincronización automática** de la tabla de calificaciones tras cualquier cambio en el CRUD (agregar, editar, eliminar estudiantes, profesores, materias o cursos).
- **Validaciones estrictas**: cédula única y válida, campos obligatorios, no duplicados, restricciones de asignación de materias y carreras.
- **Mensajes claros** de error y éxito en todas las operaciones.
- **UX mejorada:** formularios dinámicos, refresco automático de tablas, botones de acción intuitivos.

## Características Implementadas

### Roles de Usuario

- **Estudiante**: Visualiza sus calificaciones y subnotas por materia.
- **Profesor**: Asigna y gestiona subnotas por parcial y materia, con validaciones estrictas.
- **Administrador**: Gestiona usuarios, roles, carreras y materias.

### Funcionalidades por Rol

#### Estudiante
- Visualización de calificaciones por materia.
- Desglose de subnotas por parcial.
- Cálculo automático de totales y porcentajes.
- Interfaz limpia y enfocada solo en subnotas y materias.

#### Profesor
- Selección de curso a gestionar.
- Lista de estudiantes inscritos.
- Asignación y edición de subnotas por parcial, con reglas:
  - **Máximo 10 subnotas por materia/calificación.**
  - **No se puede repetir el número de subnota en el mismo parcial/calificación.**
  - **Solo se permiten los números de subnota válidos para cada parcial:**
    - Parcial 1: subnotas 1, 2, 3
    - Parcial 2: subnotas 4, 5, 6
    - Parcial 3: subnotas 7, 8, 9, 10
  - **El valor de cada subnota debe estar entre 0 y 10.**
  - **Si se intenta ingresar un valor fuera de rango, se muestra error y NO se guarda.**
- Visualización de resumen de subnotas asignadas.

#### Administrador
- Gestión de usuarios (crear, modificar, eliminar).
- Asignación de roles.
- Gestión de carreras y materias.
- Inscripción automática de estudiantes en cursos según la materia y carrera.

### Tecnologías Utilizadas

- Java 8+
- JavaFX para la interfaz gráfica
- SQLite como base de datos
- Maven para gestión de dependencias

### Estructura del Proyecto

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── universidad/
│   │           ├── controller/    # Controladores JavaFX
│   │           ├── dao/           # Capa de acceso a datos
│   │           ├── model/         # Modelos de datos
│   │           └── util/          # Utilidades
│   └── resources/
│       ├── fxml/                  # Archivos FXML
│       └── sql/                   # Scripts SQL
```

### Estado actual

- ✅ Sistema de autenticación
- ✅ Gestión de calificaciones y subnotas con reglas estrictas
- ✅ Interfaz de estudiante (solo subnotas y materias)
- ✅ Interfaz de profesor (asignación y edición de subnotas)
- ✅ Interfaz de administrador (gestión de usuarios, carreras y materias)
- ✅ Base de datos SQLite
- ✅ Cálculo automático de notas
- ✅ CRUD de usuarios, materias y carreras
- ✅ Validaciones y sincronización de datos
- ✅ Vista de profesor universal y robusta
- ✅ Mensajes claros y experiencia de usuario mejorada

### Próximas Mejoras

- Mejorar el diseño de la interfaz
- Agregar reportes y estadísticas
- Implementar sistema de recuperación de contraseña

## Cómo probar la funcionalidad

1. **Agregar estudiantes, profesores, materias y carreras** desde el panel de administrador.
2. **Asignar materias a estudiantes y profesores** (los profesores solo pueden calificar materias que les han sido asignadas).
3. **Iniciar sesión como profesor:**
   - Selecciona un curso (materia).
   - Verás todos los estudiantes de la carrera, aunque sean nuevos o no tengan notas.
   - Puedes calificar a cualquier estudiante; si no tiene registro previo, el sistema lo crea automáticamente.
4. **Editar o eliminar usuarios, materias o carreras:**
   - El sistema sincroniza automáticamente las calificaciones y relaciones.
   - No se pueden eliminar carreras o materias si tienen dependencias activas.

## Recomendaciones

- Siempre refresca la tabla tras cualquier operación para ver los cambios reflejados.
- Usa el botón de "Generar contraseña segura" al crear usuarios para mayor seguridad.
- Si tienes problemas con la visualización de estudiantes en la vista de profesor, asegúrate de que la sincronización de calificaciones esté activa (esto ya está automatizado en el sistema).

---

> **Repositorio:**  
> [https://github.com/mat1520/Registro-notas-universitarias-en-Java-y-Sql-Lite](https://github.com/mat1520/Registro-notas-universitarias-en-Java-y-Sql-Lite) 