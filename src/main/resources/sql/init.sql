-- Crear tablas
CREATE TABLE IF NOT EXISTS Usuario (
    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
    cedula TEXT NOT NULL UNIQUE,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    password TEXT NOT NULL,
    rol TEXT NOT NULL CHECK (rol IN ('ESTUDIANTE', 'PROFESOR', 'ADMIN'))
);

CREATE TABLE IF NOT EXISTS Carrera (
    id_carrera INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS Materia (
    id_materia INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    id_carrera INTEGER NOT NULL,
    FOREIGN KEY (id_carrera) REFERENCES Carrera(id_carrera)
);

CREATE TABLE IF NOT EXISTS Estudiante (
    id_estudiante INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario INTEGER NOT NULL,
    id_carrera INTEGER NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (id_carrera) REFERENCES Carrera(id_carrera)
);

CREATE TABLE IF NOT EXISTS Profesor (
    id_profesor INTEGER PRIMARY KEY AUTOINCREMENT,
    id_usuario INTEGER NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario)
);

CREATE TABLE IF NOT EXISTS Curso (
    id_curso INTEGER PRIMARY KEY AUTOINCREMENT,
    id_materia INTEGER NOT NULL,
    id_profesor INTEGER NOT NULL,
    FOREIGN KEY (id_materia) REFERENCES Materia(id_materia),
    FOREIGN KEY (id_profesor) REFERENCES Profesor(id_profesor)
);

CREATE TABLE IF NOT EXISTS Calificacion (
    id_calificacion INTEGER PRIMARY KEY AUTOINCREMENT,
    id_estudiante INTEGER NOT NULL,
    id_curso INTEGER NOT NULL,
    FOREIGN KEY (id_estudiante) REFERENCES Estudiante(id_estudiante),
    FOREIGN KEY (id_curso) REFERENCES Curso(id_curso)
);

CREATE TABLE IF NOT EXISTS Subnota (
    id_subnota INTEGER PRIMARY KEY AUTOINCREMENT,
    id_calificacion INTEGER NOT NULL,
    parcial INTEGER NOT NULL,
    numero INTEGER NOT NULL,
    valor REAL NOT NULL CHECK (valor >= 0 AND valor <= 10),
    FOREIGN KEY (id_calificacion) REFERENCES Calificacion(id_calificacion)
);

-- Insertar datos de prueba
INSERT OR IGNORE INTO Usuario (cedula, nombre, apellido, password, rol) VALUES
    ('1753797065', 'Admin', 'Sistema', 'admin123', 'ADMIN'),
    ('1725080327', 'Juan', 'Pérez', 'estudiante123', 'ESTUDIANTE'),
    ('1751445550', 'María', 'González', 'estudiante123', 'ESTUDIANTE'),
    ('2200557136', 'Carlos', 'Rodríguez', 'profesor123', 'PROFESOR'),
    ('1728854686', 'Ana', 'Martínez', 'profesor123', 'PROFESOR');

DELETE FROM Estudiante;
INSERT OR IGNORE INTO Estudiante (id_usuario, id_carrera) VALUES
    (2, 2),  -- Juan Pérez en Mecatrónica
    (3, 1);  -- María González en Sistemas

INSERT OR IGNORE INTO Profesor (id_usuario) VALUES
    (4),
    (5);

INSERT OR IGNORE INTO Carrera (nombre) VALUES
    ('ING SISTEMAS'),
    ('ING MECATRONICA');

-- Materias para Ingeniería en Sistemas
INSERT OR IGNORE INTO Materia (id_carrera, nombre) VALUES
    (1, 'Programación I'),
    (1, 'Bases de Datos'),
    (1, 'Redes de Computadoras'),
    (1, 'Sistemas Operativos'),
    (1, 'Desarrollo Web');

-- Materias para Ingeniería en Mecatrónica
INSERT OR IGNORE INTO Materia (id_carrera, nombre) VALUES
    (2, 'Mecánica I'),
    (2, 'Electrónica Digital'),
    (2, 'Control Automático'),
    (2, 'Robótica'),
    (2, 'Programación para Mecatrónica');

-- Cursos (asignación automática)
DELETE FROM Curso;

-- Crear cursos para las 5 materias de Mecatrónica y 5 de Sistemas
-- Asignar profesor 1 a Mecatrónica, profesor 2 a Sistemas
INSERT OR IGNORE INTO Curso (id_materia, id_profesor) SELECT id_materia, 1 FROM Materia WHERE id_carrera = 2;
INSERT OR IGNORE INTO Curso (id_materia, id_profesor) SELECT id_materia, 2 FROM Materia WHERE id_carrera = 1;

-- Inscribir a Juan en los cursos de Mecatrónica y María en los de Sistemas
-- Crear calificaciones y subnotas para cada uno
-- Juan: subnotas 1,2,3 en cada materia de Mecatrónica
-- María: subnotas 1,2,3,4 en cada materia de Sistemas

-- Para Mecatrónica (id_carrera=2)
INSERT INTO Calificacion (id_estudiante, id_curso) SELECT 1, c.id_curso FROM Curso c JOIN Materia m ON c.id_materia = m.id_materia WHERE m.id_carrera = 2;
-- Para Sistemas (id_carrera=1)
INSERT INTO Calificacion (id_estudiante, id_curso) SELECT 2, c.id_curso FROM Curso c JOIN Materia m ON c.id_materia = m.id_materia WHERE m.id_carrera = 1;

-- Subnotas para Juan (id_estudiante=1)
DELETE FROM Subnota;
INSERT INTO Subnota (id_calificacion, parcial, numero, valor)
SELECT cal.id_calificacion, p, n, 8.5
FROM Calificacion cal
JOIN Curso c ON cal.id_curso = c.id_curso
JOIN Materia m ON c.id_materia = m.id_materia
CROSS JOIN (
    SELECT 1 AS p, 1 AS n UNION SELECT 1,2 UNION SELECT 1,3
    UNION SELECT 2,4 UNION SELECT 2,5 UNION SELECT 2,6
    UNION SELECT 3,7 UNION SELECT 3,8 UNION SELECT 3,9 UNION SELECT 3,10
) AS distribucion
WHERE cal.id_estudiante = 1 AND m.id_carrera = 2 AND (
    (distribucion.p = 1 AND distribucion.n BETWEEN 1 AND 3) OR
    (distribucion.p = 2 AND distribucion.n BETWEEN 4 AND 6) OR
    (distribucion.p = 3 AND distribucion.n BETWEEN 7 AND 10)
);

-- Subnotas para María (id_estudiante=2)
INSERT INTO Subnota (id_calificacion, parcial, numero, valor)
SELECT cal.id_calificacion, p, n, 9.0
FROM Calificacion cal
JOIN Curso c ON cal.id_curso = c.id_curso
JOIN Materia m ON c.id_materia = m.id_materia
CROSS JOIN (
    SELECT 1 AS p, 1 AS n UNION SELECT 1,2 UNION SELECT 1,3
    UNION SELECT 2,4 UNION SELECT 2,5 UNION SELECT 2,6
    UNION SELECT 3,7 UNION SELECT 3,8 UNION SELECT 3,9 UNION SELECT 3,10
) AS distribucion
WHERE cal.id_estudiante = 2 AND m.id_carrera = 1 AND (
    (distribucion.p = 1 AND distribucion.n BETWEEN 1 AND 3) OR
    (distribucion.p = 2 AND distribucion.n BETWEEN 4 AND 6) OR
    (distribucion.p = 3 AND distribucion.n BETWEEN 7 AND 10)
); 