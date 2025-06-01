# Sistema de Gestión de Notas Universitarias

## Descripción
Sistema universitario para la gestión de notas, usuarios, materias y carreras. Incluye roles de Administrador, Profesor y Estudiante. Interfaz moderna, validaciones robustas y sincronización automática de datos.

---

## 🚀 Instalación y Primeros Pasos

1. **Clona el repositorio:**
   ```bash
   git clone https://github.com/mat1520/Registro-notas-universitarias-en-Java-y-Sql-Lite
   cd POO-FINAL
   ```
2. **Restaura la base de datos de ejemplo:**
   - Asegúrate de tener SQLite instalado.
   - Ejecuta el script `init.sql`:
     ```bash
     sqlite3 universidad.db < src/main/resources/sql/init.sql
     ```
   - O usa tu gestor SQLite favorito para importar el archivo.

3. **Compila y ejecuta:**
   ```bash
   mvnd javafx:run
   ```

---

## 🗃️ Datos de Ejemplo Incluidos
- **3 Carreras:**
  - Ingeniería en Sistemas
  - Ingeniería Mecatrónica
  - Ingeniería Civil
- **30 Estudiantes:**
  - Creados con cédulas y nombres reales (ver sección de credenciales)
- **10 Profesores:**
  - Nombres y cédulas generados, contraseñas seguras
- **1 Administrador:**
  - Cédula: `1754198487`
  - Contraseña: `UIDE.ASU.123`
- **Materias y cursos** distribuidos entre carreras y profesores

---

## 🔑 Credenciales de Ejemplo

### Administrador
- **Cédula:** `1754198487`
- **Contraseña:** `UIDE.ASU.123`

### Profesores
- **Cédulas:** `1750000001` a `1750000010`
- **Contraseñas:** `P@ssw0rd1!`, `P@ssw0rd2!`, ...

### Estudiantes
- **Cédulas y nombres:**
  - `1753797065` – Engelees Gregori Alvarado Arbiza – `S3guro1!`
  - `1725080327` – Kamilah Domenica Arcentales Egas – `S3guro2!`
  - ...
  - (Ver `init.sql` para la lista completa de estudiantes y contraseñas)

---

## 📝 Uso Rápido
1. Inicia sesión como **Administrador** para gestionar carreras, materias, usuarios y asignaciones.
2. Inicia sesión como **Profesor** para ver y calificar estudiantes en sus materias.
3. Inicia sesión como **Estudiante** para consultar sus notas y materias.

---

## 💡 Notas Técnicas
- El sistema se adapta a pantalla completa automáticamente.
- Los combos de carrera y materia se actualizan dinámicamente.
- Validaciones robustas para cédula, campos obligatorios y relaciones.
- CRUD completo para usuarios, materias y carreras.

---

## 📂 Estructura de la Base de Datos
- Ver `src/main/resources/sql/init.sql` para la estructura y datos de ejemplo.

---

## 👨‍💻 Autores y Créditos
- Proyecto UTIA – Universidad Tecnológica de Inteligencia Artificial
- Desarrollado por: Ariel Melo, Mateo Yánez, Maria Chango

## 📝 Cómo Funciona
El sistema de gestión de notas universitarias está diseñado para facilitar la administración de notas, usuarios, materias y carreras. Utiliza JavaFX para la interfaz gráfica y SQLite para el almacenamiento de datos, siguiendo el patrón MVC (Modelo-Vista-Controlador).

### Características Principales
- **Gestión de Usuarios**: Administración de roles (Administrador, Profesor, Estudiante) con validaciones de contraseñas y permisos.
- **Gestión de Materias y Carreras**: Creación, edición y eliminación de materias y carreras, con validaciones para evitar conflictos.
- **Gestión de Notas**: Registro y consulta de notas por parte de profesores y estudiantes.
- **Interfaz Adaptativa**: Diseño responsivo que se adapta a diferentes tamaños de pantalla.

### Tecnologías Utilizadas
- **JavaFX**: Para la interfaz de usuario.
- **SQLite**: Para el almacenamiento de datos.
- **Maven**: Para la gestión de dependencias y compilación del proyecto.

### Patrones de Diseño
- **MVC**: Separación clara entre la lógica de negocio, la interfaz de usuario y el control de datos.
- **DAO**: Acceso a datos a través de objetos de acceso a datos, facilitando la interacción con la base de datos.

### Validaciones y Reglas de Negocio
- Las contraseñas deben tener al menos 12 caracteres.
- No se pueden eliminar carreras con estudiantes o materias asociadas.
- Se realizan validaciones de cédula y campos obligatorios.

### Instalación y Uso
1. Clona el repositorio.
2. Configura la base de datos SQLite con el script de ejemplo si es necesario.
3. Ejecuta la aplicación con JavaFX.

Para más detalles, consulta la documentación técnica incluida en el proyecto.

¿Dudas o sugerencias? ¡Contáctanos! 
arielmelo1520@hotmail.com

## Credenciales de acceso

- **Administrador**
  - Usuario: `1754198487`
  - Contraseña: `UIDE.ASU.123`

- **Estudiantes y Profesores**
  - Usuario: Cédula del usuario
  - Contraseña: Cédula del usuario

(Recuerda que puedes cambiar las contraseñas desde la aplicación si tienes permisos de administrador.)

## Instalación y uso

1. Clona el repositorio
2. Configura la base de datos SQLite con el script de ejemplo si es necesario
3. Ejecuta la aplicación con JavaFX

## Estructura principal
- JavaFX + SQLite
- Patrón MVC
- Usuarios: Admin, Profesor, Estudiante

Para más detalles, consulta la documentación técnica incluida en el proyecto.