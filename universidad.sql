-- ----------------------------------------------------
-- DDL (Data Definition Language) - Creación de Tablas
-- ----------------------------------------------------

-- Eliminar tablas si existen para empezar desde cero
DROP TABLE IF EXISTS "Subnota";
DROP TABLE IF EXISTS "Calificacion";
DROP TABLE IF EXISTS "Curso";
DROP TABLE IF EXISTS "Parcial";
DROP TABLE IF EXISTS "Estudiante";
DROP TABLE IF EXISTS "Profesor";
DROP TABLE IF EXISTS "Administrador";
DROP TABLE IF EXISTS "Usuario";
DROP TABLE IF EXISTS "Materia";
DROP TABLE IF EXISTS "Carrera";
DROP TABLE IF EXISTS "Facultad";

-- Tabla: Facultad
CREATE TABLE "Facultad" (
	"id_facultad"	INTEGER,
	"nombre_facultad"	TEXT NOT NULL UNIQUE,
	PRIMARY KEY("id_facultad" AUTOINCREMENT)
);

-- Tabla: Carrera
CREATE TABLE "Carrera" (
	"id_carrera"	INTEGER,
	"nombre_carrera"	TEXT NOT NULL UNIQUE,
	"id_facultad"	INTEGER NOT NULL,
	PRIMARY KEY("id_carrera" AUTOINCREMENT),
	FOREIGN KEY("id_facultad") REFERENCES "Facultad"("id_facultad")
);

-- Tabla: Usuario
CREATE TABLE "Usuario" (
	"id_usuario"	INTEGER,
	"cedula"	TEXT NOT NULL UNIQUE,
	"nombre_usuario"	TEXT NOT NULL,
	"apellido_usuario"	TEXT NOT NULL,
	"password"	TEXT NOT NULL,
	"rol"	TEXT NOT NULL CHECK("rol" IN ('ESTUDIANTE', 'PROFESOR', 'ADMIN')),
	PRIMARY KEY("id_usuario" AUTOINCREMENT)
);

-- Tabla: Administrador
CREATE TABLE "Administrador" (
	"id_administrador"	INTEGER,
	"id_usuario"	INTEGER NOT NULL UNIQUE,
	PRIMARY KEY("id_administrador" AUTOINCREMENT),
	FOREIGN KEY("id_usuario") REFERENCES "Usuario"("id_usuario")
);

-- Tabla: Profesor
CREATE TABLE "Profesor" (
	"id_profesor"	INTEGER,
	"id_usuario"	INTEGER NOT NULL UNIQUE,
	PRIMARY KEY("id_profesor" AUTOINCREMENT),
	FOREIGN KEY("id_usuario") REFERENCES "Usuario"("id_usuario")
);

-- Tabla: Estudiante
CREATE TABLE "Estudiante" (
	"id_estudiante"	INTEGER,
	"id_usuario"	INTEGER NOT NULL UNIQUE,
	"id_carrera"	INTEGER NOT NULL,
	PRIMARY KEY("id_estudiante" AUTOINCREMENT),
	FOREIGN KEY("id_carrera") REFERENCES "Carrera"("id_carrera"),
	FOREIGN KEY("id_usuario") REFERENCES "Usuario"("id_usuario")
);

-- Tabla: Materia
CREATE TABLE "Materia" (
	"id_materia"	INTEGER,
	"nombre_materia"	TEXT NOT NULL,
	"id_carrera"	INTEGER NOT NULL,
	PRIMARY KEY("id_materia" AUTOINCREMENT),
	FOREIGN KEY("id_carrera") REFERENCES "Carrera"("id_carrera")
);

-- Tabla: Curso
CREATE TABLE "Curso" (
	"id_curso"	INTEGER,
	"id_materia"	INTEGER NOT NULL,
	"id_profesor"	INTEGER NOT NULL,
	"periodo"	TEXT NOT NULL,
	"seccion"	TEXT NOT NULL,
	"cupo"	INTEGER NOT NULL,
	PRIMARY KEY("id_curso" AUTOINCREMENT),
	FOREIGN KEY("id_materia") REFERENCES "Materia"("id_materia"),
	FOREIGN KEY("id_profesor") REFERENCES "Profesor"("id_profesor")
);

-- Tabla: Calificacion
CREATE TABLE "Calificacion" (
	"id_calificacion"	INTEGER,
	"id_estudiante"	INTEGER NOT NULL,
	"id_curso"	INTEGER NOT NULL,
	"nota_final"	DECIMAL(4, 2),
	"estado"	TEXT NOT NULL DEFAULT 'NO_CALIFICADO' CHECK("estado" IN ('NO_CALIFICADO', 'CALIFICADO')),
	PRIMARY KEY("id_calificacion" AUTOINCREMENT),
	UNIQUE("id_estudiante", "id_curso"),
	FOREIGN KEY("id_curso") REFERENCES "Curso"("id_curso"),
	FOREIGN KEY("id_estudiante") REFERENCES "Estudiante"("id_estudiante")
);

-- Tabla: Parcial
CREATE TABLE "Parcial" (
	"id_parcial"	INTEGER,
	"nombre"	TEXT NOT NULL,
	"porcentaje"	INTEGER NOT NULL,
	PRIMARY KEY("id_parcial" AUTOINCREMENT)
);

-- Tabla: Subnota
CREATE TABLE "Subnota" (
	"id_subnota"	INTEGER,
	"id_calificacion"	INTEGER NOT NULL,
	"id_parcial"	INTEGER NOT NULL,
	"numero_nota"	INTEGER NOT NULL,
	"valor"	NUMERIC,
	PRIMARY KEY("id_subnota" AUTOINCREMENT),
	FOREIGN KEY("id_calificacion") REFERENCES "Calificacion"("id_calificacion"),
	FOREIGN KEY("id_parcial") REFERENCES "Parcial"("id_parcial")
);


-- ----------------------------------------------------
-- DML (Data Manipulation Language) - Inserción de Datos
-- ----------------------------------------------------

-- 1. Insertar datos maestros (Facultades, Carreras, Materias, Parciales)
INSERT INTO Facultad (id_facultad, nombre_facultad) VALUES (1, 'Ingeniería y Ciencias Aplicadas'), (2, 'Ciencias Sociales y Humanidades');
INSERT INTO Carrera (id_carrera, nombre_carrera, id_facultad) VALUES (1, 'Ingeniería de Software', 1), (2, 'Ingeniería Civil', 1), (3, 'Psicología', 2), (4, 'Comunicación Social', 2);
INSERT INTO Materia (id_materia, nombre_materia, id_carrera) VALUES
(1, 'Bases de Datos', 1), (2, 'Programación Avanzada', 1), (3, 'Ingeniería de Software I', 1),
(4, 'Cálculo Estructural', 2), (5, 'Mecánica de Suelos', 2),
(6, 'Psicología Clínica', 3), (7, 'Neurociencia Cognitiva', 3),
(8, 'Teoría de la Comunicación', 4), (9, 'Periodismo Digital', 4);
INSERT INTO Parcial (id_parcial, nombre, porcentaje) VALUES (1, 'Primer Parcial', 30), (2, 'Segundo Parcial', 30), (3, 'Examen Final', 40);

-- 2. Insertar Usuarios (1 Admin, 5 Profesores, 30 Estudiantes)
INSERT INTO Usuario (id_usuario, cedula, nombre_usuario, apellido_usuario, password, rol) VALUES
(1, '1754198487', 'Admin', 'Principal', 'aF5#hJkL9!sD', 'ADMIN'),
(2, '1753797065', 'Engelees', 'Alvarado', 'gH4@lPqW2$eR', 'PROFESOR'),
(3, '1725080327', 'Kamilah', 'Arcentale', 'tY7*uI1!oP5@', 'PROFESOR'),
(4, '1752202323', 'Aaron', 'Barriga', 'zX3$cV8%bN6#', 'PROFESOR'),
(5, '1727295006', 'Katherine', 'Bolaños', 'aS9*dF2!gH7@', 'PROFESOR'),
(6, '1150350682', 'Andres', 'Bustos', 'jK5$lO1%pQ4#', 'PROFESOR'),
(7, '1728854686', 'Giullyana', 'Calvache', 'wE6@rT2*yU8!', 'ESTUDIANTE'), (8, '1751445550', 'Madelin', 'Calvopiña', 'iO3%pL7@kS1#', 'ESTUDIANTE'),
(9, '1729354637', 'Benjamin', 'Carrasco', 'bN9#mK2$jH5@', 'ESTUDIANTE'), (10, '1728544543', 'Wendy', 'Castro', 'vC6*xZ1!qA8%', 'ESTUDIANTE'),
(11, '1725091324', 'Esteban', 'Castro', 'pL4%kO9@jU2#', 'ESTUDIANTE'), (12, '1004536312', 'Mónica', 'Chalá', 'tY7*uI5!oP1@', 'ESTUDIANTE'),
(13, '2101073100', 'Karla', 'Checa', 'gH2@jK8%lP5#', 'ESTUDIANTE'), (14, '1727291823', 'Alonso', 'Constante', 'zX9$cV4*bN1!', 'ESTUDIANTE'),
(15, '1751371020', 'Scarlet', 'Cordova', 'qW5#eR1@tY6*', 'ESTUDIANTE'), (16, '1755999032', 'Camila', 'De la Torre', 'aD8*sF3!gH2#', 'ESTUDIANTE'),
(17, '1750144279', 'Patricia', 'Garrido', 'lK7%jH4@pL9!', 'ESTUDIANTE'), (18, '1723787535', 'Angeles', 'Gavilema', 'mN1@bV5*cX8#', 'ESTUDIANTE'),
(19, '1753413333', 'Genessis', 'Goya', 'oP4#iU2@yT6!', 'ESTUDIANTE'), (20, '1729091098', 'Jennifer', 'Guachamin', 'rE9%tY3*uI7@', 'ESTUDIANTE'),
(21, '1726456450', 'Jehieli', 'Herrera', 'fG6$hJ1!kS5#', 'ESTUDIANTE'), (22, '2200557136', 'Camila', 'Jaramillo', 'xZ2*c V7%bN4!', 'ESTUDIANTE'),
(23, '1755731815', 'Ariel', 'Luna', 'dF8%gH3@jK1#', 'ESTUDIANTE'), (24, '1753559820', 'Juda', 'Martinez', 'eR5@tY9*uI4!', 'ESTUDIANTE'),
(25, '1753728201', 'Alison', 'Minas', 'lK2#pL6@kS9$', 'ESTUDIANTE'), (26, '1722580873', 'Jean', 'Molina', 'bV7*mN3!cX1@', 'ESTUDIANTE'),
(27, '1755854294', 'Ana', 'Morales', 'iU6@oP2#yT8!', 'ESTUDIANTE'), (28, '1753859352', 'Gabriel', 'Moreno', 'tY1*uI8!oP3@', 'ESTUDIANTE'),
(29, '1751657089', 'Martín', 'Moya', 'hJ4#kS9$fG2!', 'ESTUDIANTE'), (30, '1750998575', 'Eduardo', 'Parra', 'cV5@bN1*xZ7#', 'ESTUDIANTE'),
(31, '1754482329', 'Daniela', 'Pazmiño', 'gH9%jK4@lP1!', 'ESTUDIANTE'), (32, '1728693589', 'Willian', 'Pillajo', 'rT6*yU2!eW8@', 'ESTUDIANTE'),
(33, '1751421379', 'Emilie', 'Romero', 'pL3@kS7#jH1$', 'ESTUDIANTE'), (34, '1754206744', 'Daniela', 'Salazar', 'mN8*bV4!cX2@', 'ESTUDIANTE'),
(35, '2300696537', 'Leandro', 'Sarzosa', 'oP5!iU1@yT7#', 'ESTUDIANTE'), (36, '1756079768', 'Emily', 'Socasi', 'tY4%uI9*oP6!', 'ESTUDIANTE');

-- 3. Asignar Roles y Carreras
INSERT INTO Administrador (id_usuario) VALUES (1);
INSERT INTO Profesor (id_profesor, id_usuario) VALUES (1, 2), (2, 3), (3, 4), (4, 5), (5, 6);
INSERT INTO Estudiante (id_estudiante, id_usuario, id_carrera) VALUES
(1, 7, 1), (2, 8, 1), (3, 9, 1), (4, 10, 1), (5, 11, 1), (6, 12, 1), (7, 13, 1), (8, 14, 1),
(9, 15, 2), (10, 16, 2), (11, 17, 2), (12, 18, 2), (13, 19, 2), (14, 20, 2), (15, 21, 2),
(16, 22, 3), (17, 23, 3), (18, 24, 3), (19, 25, 3), (20, 26, 3), (21, 27, 3), (22, 28, 3),
(23, 29, 4), (24, 30, 4), (25, 31, 4), (26, 32, 4), (27, 33, 4), (28, 34, 4), (29, 35, 4), (30, 36, 4);

-- 4. Crear Cursos y Asignar Profesores
INSERT INTO Curso (id_curso, id_materia, id_profesor, periodo, seccion, cupo) VALUES
(1, 1, 1, '2025-S1', 'A', 20), (2, 2, 2, '2025-S1', 'A', 20), (3, 3, 1, '2025-S1', 'B', 15),
(4, 4, 3, '2025-S1', 'A', 25), (5, 5, 3, '2025-S1', 'B', 25),
(6, 6, 4, '2025-S1', 'A', 30), (7, 7, 4, '2025-S1', 'B', 30),
(8, 8, 5, '2025-S1', 'A', 25), (9, 9, 5, '2025-S1', 'B', 25);

-- 5. Inscribir Estudiantes a todos los cursos de su carrera y asignar notas
-- Los ID de Calificacion van desde 1 hasta 62
INSERT INTO Calificacion (id_calificacion, id_estudiante, id_curso, nota_final, estado) VALUES
(1, 1, 1, 75.50, 'CALIFICADO'),(2, 1, 2, 81.00, 'CALIFICADO'),(3, 1, 3, 76.00, 'CALIFICADO'),
(4, 2, 1, 88.50, 'CALIFICADO'),(5, 2, 2, 82.50, 'CALIFICADO'),(6, 2, 3, 95.00, 'CALIFICADO'),
(7, 3, 1, 69.50, 'CALIFICADO'),(8, 3, 2, 73.00, 'CALIFICADO'),(9, 3, 3, 85.50, 'CALIFICADO'),
(10, 4, 1, 92.00, 'CALIFICADO'),(11, 4, 2, 78.50, 'CALIFICADO'),(12, 4, 3, 84.00, 'CALIFICADO'),
(13, 5, 1, 89.00, 'CALIFICADO'),(14, 5, 2, 71.50, 'CALIFICADO'),(15, 5, 3, 94.00, 'CALIFICADO'),
(16, 6, 1, 80.50, 'CALIFICADO'),(17, 6, 2, 66.50, 'CALIFICADO'),(18, 6, 3, 88.00, 'CALIFICADO'),
(19, 7, 1, 74.50, 'CALIFICADO'),(20, 7, 2, 90.50, 'CALIFICADO'),(21, 7, 3, 79.00, 'CALIFICADO'),
(22, 8, 1, 86.00, 'CALIFICADO'),(23, 8, 2, 93.50, 'CALIFICADO'),(24, 8, 3, 70.00, 'CALIFICADO'),
(25, 9, 4, 83.00, 'CALIFICADO'),(26, 9, 5, 75.00, 'CALIFICADO'),
(27, 10, 4, 89.50, 'CALIFICADO'),(28, 10, 5, 81.50, 'CALIFICADO'),
(29, 11, 4, 72.50, 'CALIFICADO'),(30, 11, 5, 75.50, 'CALIFICADO'),
(31, 12, 4, 81.00, 'CALIFICADO'),(32, 12, 5, 68.00, 'CALIFICADO'),
(33, 13, 4, 91.50, 'CALIFICADO'),(34, 13, 5, 77.00, 'CALIFICADO'),
(35, 14, 4, 88.50, 'CALIFICADO'),(36, 14, 5, 76.00, 'CALIFICADO'),
(37, 15, 4, 82.50, 'CALIFICADO'),(38, 15, 5, 95.00, 'CALIFICADO'),
(39, 16, 6, 69.50, 'CALIFICADO'),(40, 16, 7, 73.00, 'CALIFICADO'),
(41, 17, 6, 85.50, 'CALIFICADO'),(42, 17, 7, 92.00, 'CALIFICADO'),
(43, 18, 6, 78.50, 'CALIFICADO'),(44, 18, 7, 84.00, 'CALIFICADO'),
(45, 19, 6, 89.00, 'CALIFICADO'),(46, 19, 7, 71.50, 'CALIFICADO'),
(47, 20, 6, 94.00, 'CALIFICADO'),(48, 20, 7, 80.50, 'CALIFICADO'),
(49, 21, 6, 66.50, 'CALIFICADO'),(50, 21, 7, 88.00, 'CALIFICADO'),
(51, 22, 6, 74.50, 'CALIFICADO'),(52, 22, 7, 90.50, 'CALIFICADO'),
(53, 23, 8, 79.00, 'CALIFICADO'),(54, 23, 9, 86.00, 'CALIFICADO'),
(55, 24, 8, 93.50, 'CALIFICADO'),(56, 24, 9, 70.00, 'CALIFICADO'),
(57, 25, 8, 83.00, 'CALIFICADO'),(58, 25, 9, 75.00, 'CALIFICADO'),
(59, 26, 8, 89.50, 'CALIFICADO'),(60, 26, 9, 81.50, 'CALIFICADO'),
(61, 27, 8, 72.50, 'CALIFICADO'),(62, 27, 9, 75.50, 'CALIFICADO'),
(63, 28, 8, 81.00, 'CALIFICADO'),(64, 28, 9, 68.00, 'CALIFICADO'),
(65, 29, 8, 91.50, 'CALIFICADO'),(66, 29, 9, 77.00, 'CALIFICADO'),
(67, 30, 8, 88.50, 'CALIFICADO'),(68, 30, 9, 76.00, 'CALIFICADO');

-- 6. Insertar SUBNOTAS para CADA una de las calificaciones
INSERT INTO Subnota (id_calificacion, id_parcial, numero_nota, valor) VALUES
(1, 1, 1, 8), (1, 1, 2, 7), (1, 1, 3, 9), (1, 2, 1, 6), (1, 2, 2, 8), (1, 2, 3, 7), (1, 3, 1, 8), (1, 3, 2, 7), (1, 3, 3, 6), (1, 3, 4, 8),
(2, 1, 1, 9), (2, 1, 2, 8), (2, 1, 3, 9), (2, 2, 1, 7), (2, 2, 2, 8), (2, 2, 3, 7), (2, 3, 1, 8), (2, 3, 2, 9), (2, 3, 3, 7), (2, 3, 4, 8),
(3, 1, 1, 8), (3, 1, 2, 7), (3, 1, 3, 8), (3, 2, 1, 7), (3, 2, 2, 8), (3, 2, 3, 7), (3, 3, 1, 8), (3, 3, 2, 7), (3, 3, 3, 8), (3, 3, 4, 7),
(4, 1, 1, 9), (4, 1, 2, 9), (4, 1, 3, 9), (4, 2, 1, 8), (4, 2, 2, 8), (4, 2, 3, 8), (4, 3, 1, 9), (4, 3, 2, 9), (4, 3, 3, 8), (4, 3, 4, 9),
(5, 1, 1, 8), (5, 1, 2, 7), (5, 1, 3, 8), (5, 2, 1, 8), (5, 2, 2, 7), (5, 2, 3, 8), (5, 3, 1, 9), (5, 3, 2, 8), (5, 3, 3, 8), (5, 3, 4, 7),
(6, 1, 1, 10), (6, 1, 2, 10), (6, 1, 3, 9), (6, 2, 1, 9), (6, 2, 2, 9), (6, 2, 3, 8), (6, 3, 1, 10), (6, 3, 2, 10), (6, 3, 3, 9), (6, 3, 4, 9),
(7, 1, 1, 7), (7, 1, 2, 8), (7, 1, 3, 6), (7, 2, 1, 7), (7, 2, 2, 7), (7, 2, 3, 6), (7, 3, 1, 6), (7, 3, 2, 7), (7, 3, 3, 7), (7, 3, 4, 7),
(8, 1, 1, 8), (8, 1, 2, 7), (8, 1, 3, 8), (8, 2, 1, 7), (8, 2, 2, 8), (8, 2, 3, 7), (8, 3, 1, 8), (8, 3, 2, 7), (8, 3, 3, 8), (8, 3, 4, 7),
(9, 1, 1, 9), (9, 1, 2, 9), (9, 1, 3, 9), (9, 2, 1, 8), (9, 2, 2, 8), (9, 2, 3, 8), (9, 3, 1, 9), (9, 3, 2, 9), (9, 3, 3, 8), (9, 3, 4, 9),
(10, 1, 1, 10), (10, 1, 2, 9), (10, 1, 3, 10), (10, 2, 1, 9), (10, 2, 2, 8), (10, 2, 3, 9), (10, 3, 1, 10), (10, 3, 2, 9), (10, 3, 3, 10), (10, 3, 4, 9),
(11, 1, 1, 7), (11, 1, 2, 8), (11, 1, 3, 7), (11, 2, 1, 8), (11, 2, 2, 7), (11, 2, 3, 8), (11, 3, 1, 7), (11, 3, 2, 8), (11, 3, 3, 7), (11, 3, 4, 8),
(12, 1, 1, 8), (12, 1, 2, 9), (12, 1, 3, 8), (12, 2, 1, 9), (12, 2, 2, 8), (12, 2, 3, 9), (12, 3, 1, 8), (12, 3, 2, 9), (12, 3, 3, 8), (12, 3, 4, 9),
(13, 1, 1, 10), (13, 1, 2, 10), (13, 1, 3, 9), (13, 2, 1, 9), (13, 2, 2, 9), (13, 2, 3, 10), (13, 3, 1, 10), (13, 3, 2, 9), (13, 3, 3, 9), (13, 3, 4, 10),
(14, 1, 1, 8), (14, 1, 2, 7), (14, 1, 3, 8), (14, 2, 1, 7), (14, 2, 2, 8), (14, 2, 3, 8), (14, 3, 1, 8), (14, 3, 2, 8), (14, 3, 3, 7), (14, 3, 4, 8),
(15, 1, 1, 10), (15, 1, 2, 9), (15, 1, 3, 9), (15, 2, 1, 9), (15, 2, 2, 10), (15, 2, 3, 9), (15, 3, 1, 9), (15, 3, 2, 9), (15, 3, 3, 9), (15, 3, 4, 10),
(16, 1, 1, 8), (16, 1, 2, 7), (16, 1, 3, 8), (16, 2, 1, 8), (16, 2, 2, 7), (16, 2, 3, 8), (16, 3, 1, 7), (16, 3, 2, 8), (16, 3, 3, 8), (16, 3, 4, 7),
(17, 1, 1, 7), (17, 1, 2, 6), (17, 1, 3, 7), (17, 2, 1, 6), (17, 2, 2, 7), (17, 2, 3, 6), (17, 3, 1, 7), (17, 3, 2, 6), (17, 3, 3, 7), (17, 3, 4, 6),
(18, 1, 1, 9), (18, 1, 2, 9), (18, 1, 3, 8), (18, 2, 1, 8), (18, 2, 2, 9), (18, 2, 3, 8), (18, 3, 1, 9), (18, 3, 2, 8), (18, 3, 3, 9), (18, 3, 4, 9),
(19, 1, 1, 8), (19, 1, 2, 7), (19, 1, 3, 7), (19, 2, 1, 7), (19, 2, 2, 8), (19, 2, 3, 7), (19, 3, 1, 8), (19, 3, 2, 7), (19, 3, 3, 7), (19, 3, 4, 8),
(20, 1, 1, 9), (20, 1, 2, 10), (20, 1, 3, 9), (20, 2, 1, 9), (20, 2, 2, 8), (20, 2, 3, 9), (20, 3, 1, 9), (20, 3, 2, 10), (20, 3, 3, 9), (20, 3, 4, 8),
(21, 1, 1, 8), (21, 1, 2, 8), (21, 1, 3, 7), (21, 2, 1, 8), (21, 2, 2, 7), (21, 2, 3, 8), (21, 3, 1, 8), (21, 3, 2, 8), (21, 3, 3, 8), (21, 3, 4, 7),
(22, 1, 1, 9), (22, 1, 2, 8), (22, 1, 3, 9), (22, 2, 1, 8), (22, 2, 2, 9), (22, 2, 3, 8), (22, 3, 1, 9), (22, 3, 2, 8), (22, 3, 3, 9), (22, 3, 4, 8),
(23, 1, 1, 10), (23, 1, 2, 9), (23, 1, 3, 9), (23, 2, 1, 9), (23, 2, 2, 10), (23, 2, 3, 9), (23, 3, 1, 9), (23, 3, 2, 9), (23, 3, 3, 9), (23, 3, 4, 10),
(24, 1, 1, 7), (24, 1, 2, 7), (24, 1, 3, 6), (24, 2, 1, 7), (24, 2, 2, 7), (24, 2, 3, 7), (24, 3, 1, 7), (24, 3, 2, 7), (24, 3, 3, 7), (24, 3, 4, 7),
(25, 1, 1, 8), (25, 1, 2, 8), (25, 1, 3, 8), (25, 2, 1, 8), (25, 2, 2, 9), (25, 2, 3, 8), (25, 3, 1, 8), (25, 3, 2, 8), (25, 3, 3, 9), (25, 3, 4, 8),
(26, 1, 1, 7), (26, 1, 2, 8), (26, 1, 3, 7), (26, 2, 1, 7), (26, 2, 2, 8), (26, 2, 3, 7), (26, 3, 1, 8), (26, 3, 2, 7), (26, 3, 3, 7), (26, 3, 4, 8),
(27, 1, 1, 9), (27, 1, 2, 9), (27, 1, 3, 9), (27, 2, 1, 9), (27, 2, 2, 8), (27, 2, 3, 9), (27, 3, 1, 9), (27, 3, 2, 9), (27, 3, 3, 9), (27, 3, 4, 9),
(28, 1, 1, 8), (28, 1, 2, 8), (28, 1, 3, 8), (28, 2, 1, 8), (28, 2, 2, 8), (28, 2, 3, 8), (28, 3, 1, 9), (28, 3, 2, 8), (28, 3, 3, 8), (28, 3, 4, 8),
(29, 1, 1, 7), (29, 1, 2, 7), (29, 1, 3, 8), (29, 2, 1, 7), (29, 2, 2, 7), (29, 2, 3, 7), (29, 3, 1, 8), (29, 3, 2, 7), (29, 3, 3, 7), (29, 3, 4, 7),
(30, 1, 1, 8), (30, 1, 2, 8), (30, 1, 3, 8), (30, 2, 1, 9), (30, 2, 2, 8), (30, 2, 3, 8), (30, 3, 1, 9), (30, 3, 2, 8), (30, 3, 3, 9), (30, 3, 4, 8),
(31, 1, 1, 7), (31, 1, 2, 7), (31, 1, 3, 7), (31, 2, 1, 8), (31, 2, 2, 7), (31, 2, 3, 7), (31, 3, 1, 8), (31, 3, 2, 7), (31, 3, 3, 8), (31, 3, 4, 7),
(32, 1, 1, 7), (32, 1, 2, 6), (32, 1, 3, 7), (32, 2, 1, 5), (32, 2, 2, 7), (32, 2, 3, 6), (32, 3, 1, 7), (32, 3, 2, 8), (32, 3, 3, 7), (32, 3, 4, 7),
(33, 1, 1, 10), (33, 1, 2, 9), (33, 1, 3, 9), (33, 2, 1, 8), (33, 2, 2, 9), (33, 2, 3, 8), (33, 3, 1, 10), (33, 3, 2, 9), (33, 3, 3, 8), (33, 3, 4, 9),
(34, 1, 1, 8), (34, 1, 2, 7), (34, 1, 3, 8), (34, 2, 1, 7), (34, 2, 2, 8), (34, 2, 3, 7), (34, 3, 1, 8), (34, 3, 2, 7), (34, 3, 3, 8), (34, 3, 4, 7),
(35, 1, 1, 9), (35, 1, 2, 9), (35, 1, 3, 9), (35, 2, 1, 8), (35, 2, 2, 8), (35, 2, 3, 8), (35, 3, 1, 9), (35, 3, 2, 9), (35, 3, 3, 8), (35, 3, 4, 9),
(36, 1, 1, 8), (36, 1, 2, 7), (36, 1, 3, 8), (36, 2, 1, 8), (36, 2, 2, 7), (36, 2, 3, 8), (36, 3, 1, 7), (36, 3, 2, 8), (36, 3, 3, 8), (36, 3, 4, 7),
(37, 1, 1, 9), (37, 1, 2, 8), (37, 1, 3, 8), (37, 2, 1, 8), (37, 2, 2, 8), (37, 2, 3, 8), (37, 3, 1, 8), (37, 3, 2, 9), (37, 3, 3, 8), (37, 3, 4, 9),
(38, 1, 1, 10), (38, 1, 2, 10), (38, 1, 3, 9), (38, 2, 1, 9), (38, 2, 2, 9), (38, 2, 3, 8), (38, 3, 1, 10), (38, 3, 2, 10), (38, 3, 3, 9), (38, 3, 4, 9),
(39, 1, 1, 7), (39, 1, 2, 8), (39, 1, 3, 6), (39, 2, 1, 7), (39, 2, 2, 7), (39, 2, 3, 6), (39, 3, 1, 6), (39, 3, 2, 7), (39, 3, 3, 7), (39, 3, 4, 7),
(40, 1, 1, 8), (40, 1, 2, 7), (40, 1, 3, 8), (40, 2, 1, 7), (40, 2, 2, 8), (40, 2, 3, 7), (40, 3, 1, 8), (40, 3, 2, 7), (40, 3, 3, 8), (40, 3, 4, 7),
(41, 1, 1, 9), (41, 1, 2, 9), (41, 1, 3, 9), (41, 2, 1, 8), (41, 2, 2, 8), (41, 2, 3, 8), (41, 3, 1, 9), (41, 3, 2, 9), (41, 3, 3, 8), (41, 3, 4, 9),
(42, 1, 1, 10), (42, 1, 2, 9), (42, 1, 3, 10), (42, 2, 1, 9), (42, 2, 2, 8), (42, 2, 3, 9), (42, 3, 1, 10), (42, 3, 2, 9), (42, 3, 3, 10), (42, 3, 4, 9),
(43, 1, 1, 7), (43, 1, 2, 8), (43, 1, 3, 7), (43, 2, 1, 8), (43, 2, 2, 7), (43, 2, 3, 8), (43, 3, 1, 7), (43, 3, 2, 8), (43, 3, 3, 7), (43, 3, 4, 8),
(44, 1, 1, 8), (44, 1, 2, 9), (44, 1, 3, 8), (44, 2, 1, 9), (44, 2, 2, 8), (44, 2, 3, 9), (44, 3, 1, 8), (44, 3, 2, 9), (44, 3, 3, 8), (44, 3, 4, 9),
(45, 1, 1, 10), (45, 1, 2, 10), (45, 1, 3, 9), (45, 2, 1, 9), (45, 2, 2, 9), (45, 2, 3, 10), (45, 3, 1, 10), (45, 3, 2, 9), (45, 3, 3, 9), (45, 3, 4, 10),
(46, 1, 1, 8), (46, 1, 2, 7), (46, 1, 3, 8), (46, 2, 1, 7), (46, 2, 2, 8), (46, 2, 3, 8), (46, 3, 1, 8), (46, 3, 2, 8), (46, 3, 3, 7), (46, 3, 4, 8),
(47, 1, 1, 10), (47, 1, 2, 9), (47, 1, 3, 9), (47, 2, 1, 9), (47, 2, 2, 10), (47, 2, 3, 9), (47, 3, 1, 9), (47, 3, 2, 9), (47, 3, 3, 9), (47, 3, 4, 10),
(48, 1, 1, 8), (48, 1, 2, 7), (48, 1, 3, 8), (48, 2, 1, 8), (48, 2, 2, 7), (48, 2, 3, 8), (48, 3, 1, 7), (48, 3, 2, 8), (48, 3, 3, 8), (48, 3, 4, 7),
(49, 1, 1, 7), (49, 1, 2, 6), (49, 1, 3, 7), (49, 2, 1, 6), (49, 2, 2, 7), (49, 2, 3, 6), (49, 3, 1, 7), (49, 3, 2, 6), (49, 3, 3, 7), (49, 3, 4, 6),
(50, 1, 1, 9), (50, 1, 2, 9), (50, 1, 3, 8), (50, 2, 1, 8), (50, 2, 2, 9), (50, 2, 3, 8), (50, 3, 1, 9), (50, 3, 2, 8), (50, 3, 3, 9), (50, 3, 4, 9),
(51, 1, 1, 8), (51, 1, 2, 7), (51, 1, 3, 7), (51, 2, 1, 7), (51, 2, 2, 8), (51, 2, 3, 7), (51, 3, 1, 8), (51, 3, 2, 7), (51, 3, 3, 7), (51, 3, 4, 8),
(52, 1, 1, 9), (52, 1, 2, 10), (52, 1, 3, 9), (52, 2, 1, 9), (52, 2, 2, 8), (52, 2, 3, 9), (52, 3, 1, 9), (52, 3, 2, 10), (52, 3, 3, 9), (52, 3, 4, 8),
(53, 1, 1, 8), (53, 1, 2, 8), (53, 1, 3, 7), (53, 2, 1, 8), (53, 2, 2, 7), (53, 2, 3, 8), (53, 3, 1, 8), (53, 3, 2, 8), (53, 3, 3, 8), (53, 3, 4, 7),
(54, 1, 1, 9), (54, 1, 2, 8), (54, 1, 3, 9), (54, 2, 1, 8), (54, 2, 2, 9), (54, 2, 3, 8), (54, 3, 1, 9), (54, 3, 2, 8), (54, 3, 3, 9), (54, 3, 4, 8),
(55, 1, 1, 10), (55, 1, 2, 9), (55, 1, 3, 9), (55, 2, 1, 9), (55, 2, 2, 10), (55, 2, 3, 9), (55, 3, 1, 9), (55, 3, 2, 9), (55, 3, 3, 9), (55, 3, 4, 10),
(56, 1, 1, 7), (56, 1, 2, 7), (56, 1, 3, 6), (56, 2, 1, 7), (56, 2, 2, 7), (56, 2, 3, 7), (56, 3, 1, 7), (56, 3, 2, 7), (56, 3, 3, 7), (56, 3, 4, 7),
(57, 1, 1, 8), (57, 1, 2, 8), (57, 1, 3, 8), (57, 2, 1, 8), (57, 2, 2, 9), (57, 2, 3, 8), (57, 3, 1, 8), (57, 3, 2, 8), (57, 3, 3, 9), (57, 3, 4, 8),
(58, 1, 1, 7), (58, 1, 2, 8), (58, 1, 3, 7), (58, 2, 1, 7), (58, 2, 2, 8), (58, 2, 3, 7), (58, 3, 1, 8), (58, 3, 2, 7), (58, 3, 3, 7), (58, 3, 4, 8),
(59, 1, 1, 9), (59, 1, 2, 9), (59, 1, 3, 9), (59, 2, 1, 9), (59, 2, 2, 8), (59, 2, 3, 9), (59, 3, 1, 9), (59, 3, 2, 9), (59, 3, 3, 9), (59, 3, 4, 9),
(60, 1, 1, 8), (60, 1, 2, 8), (60, 1, 3, 8), (60, 2, 1, 8), (60, 2, 2, 8), (60, 2, 3, 8), (60, 3, 1, 9), (60, 3, 2, 8), (60, 3, 3, 8), (60, 3, 4, 8),
(61, 1, 1, 7), (61, 1, 2, 7), (61, 1, 3, 8), (61, 2, 1, 7), (61, 2, 2, 7), (61, 2, 3, 7), (61, 3, 1, 8), (61, 3, 2, 7), (61, 3, 3, 7), (61, 3, 4, 7),
(62, 1, 1, 8), (62, 1, 2, 8), (62, 1, 3, 8), (62, 2, 1, 9), (62, 2, 2, 8), (62, 2, 3, 8), (62, 3, 1, 9), (62, 3, 2, 8), (62, 3, 3, 9), (62, 3, 4, 8),
(63, 1, 1, 7), (63, 1, 2, 7), (63, 1, 3, 7), (63, 2, 1, 8), (63, 2, 2, 7), (63, 2, 3, 7), (63, 3, 1, 8), (63, 3, 2, 7), (63, 3, 3, 8), (63, 3, 4, 7),
(64, 1, 1, 7), (64, 1, 2, 6), (64, 1, 3, 7), (64, 2, 1, 5), (64, 2, 2, 7), (64, 2, 3, 6), (64, 3, 1, 7), (64, 3, 2, 8), (64, 3, 3, 7), (64, 3, 4, 7),
(65, 1, 1, 10), (65, 1, 2, 9), (65, 1, 3, 9), (65, 2, 1, 8), (65, 2, 2, 9), (65, 2, 3, 8), (65, 3, 1, 10), (65, 3, 2, 9), (65, 3, 3, 8), (65, 3, 4, 9),
(66, 1, 1, 8), (66, 1, 2, 7), (66, 1, 3, 8), (66, 2, 1, 7), (66, 2, 2, 8), (66, 2, 3, 7), (66, 3, 1, 8), (66, 3, 2, 7), (66, 3, 3, 8), (66, 3, 4, 7),
(67, 1, 1, 9), (67, 1, 2, 9), (67, 1, 3, 9), (67, 2, 1, 8), (67, 2, 2, 8), (67, 2, 3, 8), (67, 3, 1, 9), (67, 3, 2, 9), (67, 3, 3, 8), (67, 3, 4, 9),
(68, 1, 1, 8), (68, 1, 2, 7), (68, 1, 3, 8), (68, 2, 1, 8), (68, 2, 2, 7), (68, 2, 3, 8), (68, 3, 1, 7), (68, 3, 2, 8), (68, 3, 3, 8), (68, 3, 4, 7);
