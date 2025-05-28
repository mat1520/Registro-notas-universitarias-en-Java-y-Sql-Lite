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

-- Carreras
INSERT INTO Carrera (nombre) VALUES
('Ingeniería en Sistemas'),
('Ingeniería Mecatrónica'),
('Ingeniería Civil');

-- Admin
INSERT INTO Usuario (cedula, nombre, apellido, password, rol) VALUES
('1754198487', 'Admin', 'Principal', 'ADMIN', 'ADMIN');

-- Profesores (10)
INSERT INTO Usuario (cedula, nombre, apellido, password, rol) VALUES
('1750000001', 'Profesor1', 'Apellido1', 'P@ssw0rd1!', 'PROFESOR'),
('1750000002', 'Profesor2', 'Apellido2', 'P@ssw0rd2!', 'PROFESOR'),
('1750000003', 'Profesor3', 'Apellido3', 'P@ssw0rd3!', 'PROFESOR'),
('1750000004', 'Profesor4', 'Apellido4', 'P@ssw0rd4!', 'PROFESOR'),
('1750000005', 'Profesor5', 'Apellido5', 'P@ssw0rd5!', 'PROFESOR'),
('1750000006', 'Profesor6', 'Apellido6', 'P@ssw0rd6!', 'PROFESOR'),
('1750000007', 'Profesor7', 'Apellido7', 'P@ssw0rd7!', 'PROFESOR'),
('1750000008', 'Profesor8', 'Apellido8', 'P@ssw0rd8!', 'PROFESOR'),
('1750000009', 'Profesor9', 'Apellido9', 'P@ssw0rd9!', 'PROFESOR'),
('1750000010', 'Profesor10', 'Apellido10', 'P@ssw0rd10!', 'PROFESOR');

-- Estudiantes (30)
INSERT INTO Usuario (cedula, nombre, apellido, password, rol) VALUES
('1753797065', 'Engelees Gregori', 'Alvarado Arbiza', 'S3guro1!', 'ESTUDIANTE'),
('1725080327', 'Kamilah Domenica', 'Arcentales Egas', 'S3guro2!', 'ESTUDIANTE'),
('1752202323', 'Aaron Henry', 'Barriga Semanate', 'S3guro3!', 'ESTUDIANTE'),
('1727295006', 'Katherine Renata', 'Bolaños Ortiz', 'S3guro4!', 'ESTUDIANTE'),
('1150350682', 'Andres Mauricio', 'Bustos Tomala', 'S3guro5!', 'ESTUDIANTE'),
('1728854686', 'Giullyana Salomé', 'Calvache Garces', 'S3guro6!', 'ESTUDIANTE'),
('1751445550', 'Madelin Lisbeth', 'Calvopiña Asimbaya', 'S3guro7!', 'ESTUDIANTE'),
('1729354637', 'Benjamin Alessandro', 'Carrasco Pastaz', 'S3guro8!', 'ESTUDIANTE'),
('1728544543', 'Wendy Cristina', 'Castro Correa', 'S3guro9!', 'ESTUDIANTE'),
('1725091324', 'Esteban Nicolás', 'Castro Flores', 'S3guro10!', 'ESTUDIANTE'),
('1004536312', 'Mónica Danae', 'Chalá Pavón', 'S3guro11!', 'ESTUDIANTE'),
('2101073100', 'Karla Emilia', 'Checa Naranjo', 'S3guro12!', 'ESTUDIANTE'),
('1727291823', 'Alonso Gabriel', 'Constante Mosquera', 'S3guro13!', 'ESTUDIANTE'),
('1751371020', 'Scarlet Salomé', 'Córdova Barrionuevo', 'S3guro14!', 'ESTUDIANTE'),
('1755999032', 'Camila Alejandra', 'De La Torre Cedeño', 'S3guro15!', 'ESTUDIANTE'),
('1750144279', 'Patricia Juliana', 'Garrido Quintana', 'S3guro16!', 'ESTUDIANTE'),
('1723787535', 'Ángeles Daniela', 'Gavilema Changotasic', 'S3guro17!', 'ESTUDIANTE'),
('1753413333', 'Genessis Daniela', 'Goya Meneses', 'S3guro18!', 'ESTUDIANTE'),
('1729091098', 'Jennifer Daniela', 'Guachamín Veloz', 'S3guro19!', 'ESTUDIANTE'),
('1726456450', 'Jehieli Alejandra', 'Herrera Molina', 'S3guro20!', 'ESTUDIANTE'),
('2200557136', 'Camila Stephanie', 'Jaramillo Vidal', 'S3guro21!', 'ESTUDIANTE'),
('1755731815', 'Ariel Tsaik', 'Luna Vizuete', 'S3guro22!', 'ESTUDIANTE'),
('1753559820', 'Juda Benjamin', 'Martínez Arteaga', 'S3guro23!', 'ESTUDIANTE'),
('1753728201', 'Alison Joely', 'Minas Cedeño', 'S3guro24!', 'ESTUDIANTE'),
('1722580873', 'Jean Pierre', 'Molina Arcos', 'S3guro25!', 'ESTUDIANTE'),
('1755854294', 'Ana Paula', 'Morales Martínez', 'S3guro26!', 'ESTUDIANTE'),
('1753859352', 'Gabriel Sebastián', 'Moreno Yambay', 'S3guro27!', 'ESTUDIANTE'),
('1751657089', 'Martín Esteban', 'Moya Guerrero', 'S3guro28!', 'ESTUDIANTE'),
('1750998575', 'Eduardo Sebastián', 'Parra Egas', 'S3guro29!', 'ESTUDIANTE'),
('1754482329', 'Daniela Isabel', 'Pazmiño Ortiz', 'S3guro30!', 'ESTUDIANTE');

-- Asignar profesores y estudiantes a carreras y materias (ejemplo, balanceado)
-- Aquí puedes agregar los INSERT para Estudiante, Profesor, Materia, Curso, etc. según la estructura de tu base de datos. 