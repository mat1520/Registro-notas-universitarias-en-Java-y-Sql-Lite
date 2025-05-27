# Sistema de Registro de Notas Universitarias

Sistema desarrollado en Java con JavaFX y SQLite para la gestión de calificaciones universitarias.

## Características Implementadas

### Roles de Usuario
- **Estudiante**: Visualiza sus calificaciones por materia
- **Profesor**: Asigna y gestiona calificaciones
- **Administrador**: Gestiona usuarios y roles

### Funcionalidades por Rol

#### Estudiante
- Visualización de calificaciones por materia
- Desglose de subnotas por parcial
- Cálculo automático de totales y porcentajes
- Interfaz intuitiva con tabla de calificaciones

#### Profesor
- Selección de curso a gestionar
- Lista de estudiantes inscritos
- Asignación de subnotas por parcial
- Sistema de 3 parciales:
  - Parcial 1: 3 subnotas (30%)
  - Parcial 2: 3 subnotas (30%)
  - Parcial 3: 4 subnotas (40%)
- Visualización de resumen de subnotas asignadas

#### Administrador
- Gestión de usuarios (crear, modificar, eliminar)
- Asignación de roles
- Gestión de carreras y materias

## Tecnologías Utilizadas
- Java 8+
- JavaFX para la interfaz gráfica
- SQLite como base de datos
- Maven para gestión de dependencias

## Estructura del Proyecto
```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── universidad/
│   │           ├── controller/    # Controladores JavaFX
│   │           ├── dao/          # Capa de acceso a datos
│   │           ├── model/        # Modelos de datos
│   │           └── util/         # Utilidades
│   └── resources/
│       ├── fxml/                 # Archivos FXML
│       └── sql/                  # Scripts SQL
```

## Estado Actual
- ✅ Sistema de autenticación
- ✅ Gestión de calificaciones
- ✅ Interfaz de estudiante
- ✅ Interfaz de profesor
- ✅ Interfaz de administrador
- ✅ Base de datos SQLite
- ✅ Cálculo automático de notas

## Próximas Mejoras
- [ ] Implementar validaciones adicionales
- [ ] Mejorar el diseño de la interfaz
- [ ] Agregar reportes y estadísticas
- [ ] Implementar sistema de recuperación 