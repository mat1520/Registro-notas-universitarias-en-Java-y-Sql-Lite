-- Crear tabla Facultad
CREATE TABLE IF NOT EXISTS Facultad (
    id_facultad INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    fecha_creacion DATE NOT NULL
);

-- Crear tabla Carrera
CREATE TABLE IF NOT EXISTS Carrera (
    id_carrera INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    id_facultad INTEGER NOT NULL,
    FOREIGN KEY (id_facultad) REFERENCES Facultad(id_facultad)
);

-- Crear tabla Materia
CREATE TABLE IF NOT EXISTS Materia (
    id_materia INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo VARCHAR(10) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    creditos INTEGER NOT NULL,
    id_carrera INTEGER NOT NULL,
    FOREIGN KEY (id_carrera) REFERENCES Carrera(id_carrera)
);

-- Crear tabla Usuario (tabla base para roles)
CREATE TABLE IF NOT EXISTS Usuario (
    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
    cedula VARCHAR(10) NOT NULL UNIQUE,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('ESTUDIANTE', 'PROFESOR', 'ADMIN'))
);

-- Crear tabla Estudiante
CREATE TABLE IF NOT EXISTS Estudiante (
    id_estudiante INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario INTEGER NOT NULL,
    matricula VARCHAR(10) NOT NULL UNIQUE,
    id_carrera INTEGER NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (id_carrera) REFERENCES Carrera(id_carrera)
);

-- Crear tabla Profesor
CREATE TABLE IF NOT EXISTS Profesor (
    id_profesor INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario INTEGER NOT NULL,
    titulo VARCHAR(50) NOT NULL,
    especialidad VARCHAR(100),
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario)
);

-- Crear tabla Curso
CREATE TABLE IF NOT EXISTS Curso (
    id_curso INTEGER PRIMARY KEY AUTOINCREMENT,
    id_materia INTEGER NOT NULL,
    id_profesor INTEGER NOT NULL,
    periodo VARCHAR(20) NOT NULL,
    seccion VARCHAR(10) NOT NULL,
    cupo INTEGER NOT NULL,
    FOREIGN KEY (id_materia) REFERENCES Materia(id_materia),
    FOREIGN KEY (id_profesor) REFERENCES Profesor(id_profesor)
);

-- Crear tabla Inscripcion
CREATE TABLE IF NOT EXISTS Inscripcion (
    id_inscripcion INTEGER PRIMARY KEY AUTOINCREMENT,
    id_estudiante INTEGER NOT NULL,
    id_curso INTEGER NOT NULL,
    fecha_inscripcion DATE NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('ACTIVA', 'CANCELADA', 'COMPLETADA')),
    FOREIGN KEY (id_estudiante) REFERENCES Estudiante(id_estudiante),
    FOREIGN KEY (id_curso) REFERENCES Curso(id_curso),
    UNIQUE(id_estudiante, id_curso)
);

-- Crear tabla Calificacion
CREATE TABLE IF NOT EXISTS Calificacion (
    id_calificacion INTEGER PRIMARY KEY AUTOINCREMENT,
    id_inscripcion INTEGER NOT NULL,
    nota DECIMAL(4,2) NOT NULL CHECK (nota >= 0 AND nota <= 100),
    fecha_calificacion DATE NOT NULL,
    observaciones TEXT,
    FOREIGN KEY (id_inscripcion) REFERENCES Inscripcion(id_inscripcion)
);

-- Insertar datos de prueba para Facultad
INSERT INTO Facultad (nombre, descripcion, fecha_creacion) VALUES
('Facultad de Ingeniería', 'Facultad dedicada a las ciencias de la ingeniería', '2020-01-01'),
('Facultad de Ciencias', 'Facultad dedicada a las ciencias puras', '2020-01-01');

-- Insertar datos de prueba para Carrera
INSERT INTO Carrera (nombre, descripcion, id_facultad) VALUES
('Ingeniería en Sistemas', 'Carrera de desarrollo de software', 1),
('Ingeniería Civil', 'Carrera de construcción y diseño', 1),
('Matemáticas', 'Carrera de ciencias matemáticas', 2);

-- Insertar datos de prueba para Materia
INSERT INTO Materia (codigo, nombre, creditos, id_carrera) VALUES
('BD101', 'Bases de Datos', 4, 1),
('PE101', 'Programación Estructurada', 4, 1),
('MAT101', 'Cálculo I', 4, 3);

-- Insertar datos de prueba para Usuario
INSERT INTO Usuario (cedula, nombre, apellido, email, password, rol) VALUES
('1234567890', 'Admin', 'Sistema', 'admin@universidad.edu', 'admin123', 'ADMIN'),
('1111111111', 'Juan', 'Pérez', 'juan@universidad.edu', 'estudiante123', 'ESTUDIANTE'),
('2222222222', 'María', 'González', 'maria@universidad.edu', 'estudiante123', 'ESTUDIANTE'),
('3333333333', 'Carlos', 'Rodríguez', 'carlos@universidad.edu', 'profesor123', 'PROFESOR');

-- Insertar datos de prueba para Estudiante
INSERT INTO Estudiante (id_usuario, matricula, id_carrera) VALUES
(2, 'EST001', 1),
(3, 'EST002', 1);

-- Insertar datos de prueba para Profesor
INSERT INTO Profesor (id_usuario, titulo, especialidad) VALUES
(4, 'MSc', 'Bases de Datos');

-- Insertar datos de prueba para Curso
INSERT INTO Curso (id_materia, id_profesor, periodo, seccion, cupo) VALUES
(1, 1, '2024-1', 'A', 30),
(2, 1, '2024-1', 'B', 30);

-- Insertar datos de prueba para Inscripcion
INSERT INTO Inscripcion (id_estudiante, id_curso, fecha_inscripcion, estado) VALUES
(1, 1, '2024-01-15', 'ACTIVA'),
(2, 1, '2024-01-15', 'ACTIVA');

-- Insertar datos de prueba para Calificacion
INSERT INTO Calificacion (id_inscripcion, nota, fecha_calificacion, observaciones) VALUES
(1, 85.50, '2024-03-15', 'Buen desempeño'),
(2, 90.00, '2024-03-15', 'Excelente trabajo'); 