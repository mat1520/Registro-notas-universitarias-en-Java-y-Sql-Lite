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

-- Limpiar todas las tablas antes de insertar datos de ejemplo
DELETE FROM Subnota;
DELETE FROM Calificacion;
DELETE FROM Curso;
DELETE FROM Profesor;
DELETE FROM Estudiante;
DELETE FROM Materia;
DELETE FROM Carrera;
DELETE FROM Usuario;

-- Insertar datos de prueba
-- Carreras
INSERT INTO Carrera (nombre) VALUES
('Ingeniería en Sistemas'),
('Ingeniería Mecatrónica'),
('Ingeniería Civil');

-- Admin
INSERT INTO Usuario (cedula, nombre, apellido, password, rol) VALUES
('1754198487', 'Admin', 'Principal', 'ADMIN', 'ADMIN');

-- Profesores (5)
INSERT INTO Usuario (cedula, nombre, apellido, password, rol) VALUES
('1717213457', 'Juan', 'Pérez', 'P@ssw0rd1!', 'PROFESOR'),
('1710034065', 'Ana', 'Torres', 'P@ssw0rd2!', 'PROFESOR'),
('0926687855', 'Carlos', 'García', 'P@ssw0rd3!', 'PROFESOR'),
('1104680135', 'María', 'López', 'P@ssw0rd4!', 'PROFESOR'),
('0910985993', 'Luis', 'Martínez', 'P@ssw0rd5!', 'PROFESOR');

-- Estudiantes (15)
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
('1755999032', 'Camila Alejandra', 'De La Torre Cedeño', 'S3guro15!', 'ESTUDIANTE');

-- Asignar profesores a la tabla Profesor
INSERT INTO Profesor (id_usuario) SELECT id_usuario FROM Usuario WHERE rol = 'PROFESOR';

-- Asignar estudiantes a la tabla Estudiante (ejemplo: asignar a la carrera 1)
INSERT INTO Estudiante (id_usuario, id_carrera) SELECT id_usuario, 1 FROM Usuario WHERE rol = 'ESTUDIANTE';

-- Materias para Ingeniería en Sistemas
INSERT INTO Materia (id_carrera, nombre) VALUES
(1, 'Programación I'),
(1, 'Bases de Datos'),
(1, 'Redes de Computadoras'),
(1, 'Sistemas Operativos'),
(1, 'Desarrollo Web');

-- Materias para Ingeniería en Mecatrónica
INSERT INTO Materia (id_carrera, nombre) VALUES
(2, 'Mecánica I'),
(2, 'Electrónica Digital'),
(2, 'Control Automático'),
(2, 'Robótica'),
(2, 'Programación para Mecatrónica');

-- Cursos (asignación automática)
-- Asignar profesor 1 a Mecatrónica, profesor 2 a Sistemas
INSERT INTO Curso (id_materia, id_profesor) SELECT id_materia, 1 FROM Materia WHERE id_carrera = 2;
INSERT INTO Curso (id_materia, id_profesor) SELECT id_materia, 2 FROM Materia WHERE id_carrera = 1;

-- Inscribir a los estudiantes en los cursos de su carrera
-- Para Mecatrónica (id_carrera=2)
INSERT INTO Calificacion (id_estudiante, id_curso) SELECT e.id_estudiante, c.id_curso FROM Estudiante e JOIN Curso c JOIN Materia m ON c.id_materia = m.id_materia WHERE m.id_carrera = 2;
-- Para Sistemas (id_carrera=1)
INSERT INTO Calificacion (id_estudiante, id_curso) SELECT e.id_estudiante, c.id_curso FROM Estudiante e JOIN Curso c JOIN Materia m ON c.id_materia = m.id_materia WHERE m.id_carrera = 1;

-- Subnotas para los estudiantes (ejemplo: subnotas 1,2,3 en cada materia)
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
WHERE (
    (distribucion.p = 1 AND distribucion.n BETWEEN 1 AND 3) OR
    (distribucion.p = 2 AND distribucion.n BETWEEN 4 AND 6) OR
    (distribucion.p = 3 AND distribucion.n BETWEEN 7 AND 10)
); 