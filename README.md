# Sistema de Registro de Notas Universitarias

Aplicación de escritorio en Java y JavaFX con SQLite para la gestión de calificaciones universitarias. Incluye roles de estudiante, profesor y administrador con funciones de consulta, asignación de notas y operaciones CRUD.

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

### Estado Actual

- ✅ Sistema de autenticación
- ✅ Gestión de calificaciones y subnotas con reglas estrictas
- ✅ Interfaz de estudiante (solo subnotas y materias)
- ✅ Interfaz de profesor (asignación y edición de subnotas)
- ✅ Interfaz de administrador (gestión de usuarios, carreras y materias)
- ✅ Base de datos SQLite
- ✅ Cálculo automático de notas

### Próximas Mejoras

- Mejorar el diseño de la interfaz
- Agregar reportes y estadísticas
- Implementar sistema de recuperación de contraseña

---

> **Repositorio:**  
> [https://github.com/mat1520/Registro-notas-universitarias-en-Java-y-Sql-Lite](https://github.com/mat1520/Registro-notas-universitarias-en-Java-y-Sql-Lite) 