# Sistema de Gestión de Notas Universitarias (UTIA)

![Logo UTIA](https://i.imgur.com/O4q5dJ5.png)

Un sistema de escritorio robusto y completo para la gestión académica, desarrollado en Java con JavaFX y SQLite. Permite la administración de estudiantes, profesores, materias, carreras y calificaciones, con un sistema de roles bien definido.

---

## 🚀 Características Principales

-   **Gestión por Roles:** Tres niveles de acceso con distintas funcionalidades:
    -   **Administrador:** Control total sobre usuarios, carreras, y materias.
    -   **Profesor:** Asignación y modificación de notas para las materias a su cargo.
    -   **Estudiante:** Consulta de calificaciones y progreso académico.
-   **Interfaz Gráfica Intuitiva:** Desarrollada con JavaFX para una experiencia de usuario moderna y amigable.
-   **Base de Datos Integrada:** Utiliza SQLite, una base de datos embebida que no requiere instalación ni configuración externa. ¡Solo ejecuta la aplicación!
-   **Seguridad:** Implementación de contraseñas seguras y aleatorias para cada usuario.
-   **Lógica de Negocio Coherente:** El sistema asegura que los estudiantes solo se inscriban en materias de su carrera y que los profesores solo califiquen en los cursos que imparten.

---

## 🛠️ Tecnologías Utilizadas

-   **Lenguaje:** Java 17
-   **Interfaz Gráfica:** JavaFX
-   **Base de Datos:** SQLite
-   **Gestor de Dependencias:** Maven

---

## 📋 Requisitos e Instalación

### Requisitos

-   JDK (Java Development Kit) 17 o superior.
-   Apache Maven.

### Pasos para la Ejecución

1.  **Clonar el Repositorio:**
    ```bash
    git clone [https://github.com/tu_usuario/tu_repositorio.git](https://github.com/tu_usuario/tu_repositorio.git)
    cd tu_repositorio
    ```

2.  **Base de Datos:**
    El proyecto incluye el archivo `universidad.db` preconfigurado con todos los datos necesarios (usuarios, materias, notas, etc.). No es necesario ejecutar ningún script SQL adicional.

3.  **Compilar y Ejecutar con Maven:**
    Abre una terminal en la raíz del proyecto y ejecuta el siguiente comando:
    ```bash
    mvn clean javafx:run
    ```
    La aplicación se iniciará y mostrará la ventana de login.

---

## 🔑 Credenciales de Acceso

Usa las siguientes credenciales para acceder al sistema. El **usuario** es siempre el número de **cédula**.

| Rol | Nombre Completo | Cédula (Usuario) | Contraseña |
| :--- | :--- | :--- | :--- |
| **ADMIN** | Admin Principal | `1754198487` | `aF5#hJkL9!sD` |
| **PROFESOR** | Engelees Alvarado | `1753797065` | `gH4@lPqW2$eR` |
| **PROFESOR** | Kamilah Arcentale | `1725080327` | `tY7*uI1!oP5@` |
| **PROFESOR** | Aaron Barriga | `1752202323` | `zX3$cV8%bN6#` |
| **PROFESOR** | Katherine Bolaños | `1727295006` | `aS9*dF2!gH7@` |
| **PROFESOR** | Andres Bustos | `1150350682` | `jK5$lO1%pQ4#` |
| **ESTUDIANTE**| Giullyana Calvache | `1728854686` | `wE6@rT2*yU8!` |
| **ESTUDIANTE**| Madelin Calvopiña | `1751445550` | `iO3%pL7@kS1#` |
| **ESTUDIANTE**| Benjamin Carrasco | `1729354637` | `bN9#mK2$jH5@` |

---

## 🗄️ Estructura de la Base de Datos

El sistema utiliza una base de datos relacional con las siguientes tablas principales para mantener la integridad de los datos:

-   `Usuario`: Almacena la información base y credenciales de todos los usuarios.
-   `Administrador`, `Profesor`, `Estudiante`: Tablas de roles que extienden la información de `Usuario`.
-   `Facultad`, `Carrera`, `Materia`: Definen la estructura académica.
-   `Curso`: Vincula una materia con un profesor para un período específico.
-   `Calificacion`, `Parcial`, `Subnota`: Gestionan todo el sistema de evaluación.

---

## 🧑‍💻 Autores

Este proyecto fue desarrollado por:

-   Ariel Melo
-   Mateo Yánez
-   Maria Chango