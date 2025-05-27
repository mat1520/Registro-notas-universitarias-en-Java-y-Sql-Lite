-- Crear tablas
CREATE TABLE IF NOT EXISTS Usuario (
    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
    cedula TEXT NOT NULL UNIQUE,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    password TEXT NOT NULL,
    rol TEXT NOT NULL CHECK (rol IN ('ESTUDIANTE', 'PROFESOR', 'ADMIN'))
);

CREATE TABLE IF NOT EXISTS Estudiante (
    id_estudiante INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario INTEGER NOT NULL,
    matricula TEXT NOT NULL UNIQUE,
    id_carrera INTEGER NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (id_carrera) REFERENCES Carrera(id_carrera)
);

CREATE TABLE IF NOT EXISTS Profesor (
    id_profesor INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario INTEGER NOT NULL,
    especialidad TEXT NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario)
);

CREATE TABLE IF NOT EXISTS Facultad (
    id_facultad INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS Carrera (
    id_carrera INTEGER PRIMARY KEY AUTOINCREMENT,
    id_facultad INTEGER NOT NULL,
    nombre TEXT NOT NULL,
    FOREIGN KEY (id_facultad) REFERENCES Facultad(id_facultad)
);

CREATE TABLE IF NOT EXISTS Materia (
    id_materia INTEGER PRIMARY KEY AUTOINCREMENT,
    id_carrera INTEGER NOT NULL,
    codigo TEXT NOT NULL UNIQUE,
    nombre TEXT NOT NULL,
    creditos INTEGER NOT NULL,
    FOREIGN KEY (id_carrera) REFERENCES Carrera(id_carrera)
);

CREATE TABLE IF NOT EXISTS Curso (
    id_curso INTEGER PRIMARY KEY AUTOINCREMENT,
    id_materia INTEGER NOT NULL,
    id_profesor INTEGER NOT NULL,
    periodo TEXT NOT NULL,
    seccion TEXT NOT NULL,
    FOREIGN KEY (id_materia) REFERENCES Materia(id_materia),
    FOREIGN KEY (id_profesor) REFERENCES Profesor(id_profesor)
);

CREATE TABLE IF NOT EXISTS Inscripcion (
    id_inscripcion INTEGER PRIMARY KEY AUTOINCREMENT,
    id_estudiante INTEGER NOT NULL,
    id_curso INTEGER NOT NULL,
    fecha_inscripcion DATE NOT NULL,
    estado TEXT NOT NULL CHECK (estado IN ('ACTIVA', 'CANCELADA')),
    FOREIGN KEY (id_estudiante) REFERENCES Estudiante(id_estudiante),
    FOREIGN KEY (id_curso) REFERENCES Curso(id_curso)
);

CREATE TABLE IF NOT EXISTS Calificacion (
    id_calificacion INTEGER PRIMARY KEY AUTOINCREMENT,
    id_inscripcion INTEGER NOT NULL,
    nota REAL NOT NULL CHECK (nota >= 0 AND nota <= 100),
    fecha_calificacion DATE NOT NULL,
    observaciones TEXT,
    FOREIGN KEY (id_inscripcion) REFERENCES Inscripcion(id_inscripcion)
);

-- Tabla de parciales
CREATE TABLE IF NOT EXISTS Parcial (
    id_parcial INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    porcentaje REAL NOT NULL
);

-- Tabla de subnotas
CREATE TABLE IF NOT EXISTS Subnota (
    id_subnota INTEGER PRIMARY KEY AUTOINCREMENT,
    id_calificacion INTEGER NOT NULL,
    id_parcial INTEGER NOT NULL,
    numero INTEGER NOT NULL,
    valor REAL NOT NULL CHECK (valor >= 0 AND valor <= 10),
    FOREIGN KEY (id_calificacion) REFERENCES Calificacion(id_calificacion),
    FOREIGN KEY (id_parcial) REFERENCES Parcial(id_parcial)
);

-- Insertar datos de prueba
INSERT OR IGNORE INTO Usuario (cedula, nombre, apellido, password, rol) VALUES
    ('1753797065', 'Admin', 'Sistema', 'admin123', 'ADMIN'),
    ('1725080327', 'Juan', 'Pérez', 'estudiante123', 'ESTUDIANTE'),
    ('1751445550', 'María', 'González', 'estudiante123', 'ESTUDIANTE'),
    ('2200557136', 'Carlos', 'Rodríguez', 'profesor123', 'PROFESOR'),
    ('1728854686', 'Ana', 'Martínez', 'profesor123', 'PROFESOR');

INSERT OR IGNORE INTO Estudiante (id_usuario, matricula, id_carrera) VALUES
    (2, '2024001', 1),  -- Juan Pérez en Sistemas
    (3, '2024002', 2);  -- María González en Mecatrónica

INSERT OR IGNORE INTO Profesor (id_usuario, especialidad) VALUES
    (4, 'Sistemas Embebidos'),
    (5, 'Desarrollo de Software');

INSERT OR IGNORE INTO Facultad (nombre) VALUES
    ('Ingeniería'),
    ('Ciencias');

INSERT OR IGNORE INTO Carrera (id_facultad, nombre) VALUES
    (1, 'Ingeniería en Sistemas'),
    (1, 'Ingeniería en Mecatrónica'),
    (2, 'Matemáticas');

-- Materias para Ingeniería en Sistemas
INSERT OR IGNORE INTO Materia (id_carrera, codigo, nombre, creditos) VALUES
    (1, 'PRO101', 'Programación I', 4),
    (1, 'BD101', 'Bases de Datos', 4),
    (1, 'RED101', 'Redes de Computadoras', 4),
    (1, 'SIS101', 'Sistemas Operativos', 4),
    (1, 'WEB101', 'Desarrollo Web', 4);

-- Materias para Ingeniería en Mecatrónica
INSERT OR IGNORE INTO Materia (id_carrera, codigo, nombre, creditos) VALUES
    (2, 'MEC101', 'Mecánica I', 4),
    (2, 'ELE101', 'Electrónica Digital', 4),
    (2, 'CON101', 'Control Automático', 4),
    (2, 'ROB101', 'Robótica', 4),
    (2, 'PRO101', 'Programación para Mecatrónica', 4);

-- Cursos
INSERT OR IGNORE INTO Curso (id_materia, id_profesor, periodo, seccion) VALUES
    (1, 1, '2024-1', 'A'),  -- Programación I
    (2, 1, '2024-1', 'A'),  -- Bases de Datos
    (6, 2, '2024-1', 'A'),  -- Mecánica I
    (7, 2, '2024-1', 'A');  -- Electrónica Digital

-- Inscripciones
INSERT OR IGNORE INTO Inscripcion (id_estudiante, id_curso, fecha_inscripcion, estado) VALUES
    (1, 1, '2024-01-15', 'ACTIVA'),  -- Juan en Programación I
    (1, 2, '2024-01-15', 'ACTIVA'),  -- Juan en Bases de Datos
    (2, 3, '2024-01-15', 'ACTIVA'),  -- María en Mecánica I
    (2, 4, '2024-01-15', 'ACTIVA');  -- María en Electrónica Digital

INSERT OR IGNORE INTO Calificacion (id_inscripcion, nota, fecha_calificacion, observaciones) VALUES
    (1, 85.5, '2024-02-15', 'Buen desempeño'),
    (2, 90.0, '2024-02-15', 'Excelente trabajo'),
    (3, 80.0, '2024-02-15', 'Buen desempeño'),
    (4, 85.0, '2024-02-15', 'Buen desempeño');

-- Insertar parciales
INSERT OR IGNORE INTO Parcial (id_parcial, nombre, porcentaje) VALUES
    (1, 'Parcial 1', 0.25),
    (2, 'Parcial 2', 0.25),
    (3, 'Parcial 3', 0.25),
    (4, 'Final', 0.25);

-- Ejemplo de subnotas para calificaciones existentes
-- (asumiendo que hay calificaciones con id_calificacion 1, 2, 3, 4)
INSERT OR IGNORE INTO Subnota (id_calificacion, id_parcial, numero, valor) VALUES
    (1, 1, 1, 8.5), (1, 1, 2, 9.0), (1, 1, 3, 7.5),
    (1, 2, 1, 8.0), (1, 2, 2, 8.5), (1, 2, 3, 9.0),
    (1, 3, 1, 7.0), (1, 3, 2, 8.0), (1, 3, 3, 8.5),
    (1, 4, 1, 9.0), (1, 4, 2, 8.5), (1, 4, 3, 9.5), (1, 4, 4, 10.0); 