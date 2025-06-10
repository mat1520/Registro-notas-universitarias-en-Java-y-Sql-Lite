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

-- 1. Insertar datos maestros
INSERT INTO Facultad (id_facultad, nombre_facultad) VALUES (1, 'Ingeniería y Ciencias Aplicadas'), (2, 'Ciencias Sociales y Humanidades');
INSERT INTO Carrera (id_carrera, nombre_carrera, id_facultad) VALUES (1, 'Ingeniería de Software', 1), (2, 'Ingeniería Civil', 1), (3, 'Psicología', 2), (4, 'Comunicación Social', 2);
INSERT INTO Materia (id_materia, nombre_materia, id_carrera) VALUES
(1, 'Bases de Datos', 1), (2, 'Programación Avanzada', 1), (3, 'Ingeniería de Software I', 1),
(4, 'Cálculo Estructural', 2), (5, 'Mecánica de Suelos', 2),
(6, 'Psicología Clínica', 3), (7, 'Neurociencia Cognitiva', 3),
(8, 'Teoría de la Comunicación', 4), (9, 'Periodismo Digital', 4);
INSERT INTO Parcial (id_parcial, nombre, porcentaje) VALUES (1, 'Primer Parcial', 30), (2, 'Segundo Parcial', 30), (3, 'Examen Final', 40);

-- 2. Insertar Usuarios
INSERT INTO Usuario (id_usuario, cedula, nombre_usuario, apellido_usuario, password, rol) VALUES
(1, '1754198487', 'Admin', 'Principal', 'aF5#hJkL9!sD', 'ADMIN'),
(2, '1753797065', 'Engelees', 'Alvarado', 'gH4@lPqW2$eR', 'PROFESOR'),(3, '1725080327', 'Kamilah', 'Arcentale', 'tY7*uI1!oP5@', 'PROFESOR'),(4, '1752202323', 'Aaron', 'Barriga', 'zX3$cV8%bN6#', 'PROFESOR'),(5, '1727295006', 'Katherine', 'Bolaños', 'aS9*dF2!gH7@', 'PROFESOR'),(6, '1150350682', 'Andres', 'Bustos', 'jK5$lO1%pQ4#', 'PROFESOR'),
(7, '1728854686', 'Giullyana', 'Calvache', 'wE6@rT2*yU8!', 'ESTUDIANTE'),(8, '1751445550', 'Madelin', 'Calvopiña', 'iO3%pL7@kS1#', 'ESTUDIANTE'),(9, '1729354637', 'Benjamin', 'Carrasco', 'bN9#mK2$jH5@', 'ESTUDIANTE'),(10, '1728544543', 'Wendy', 'Castro', 'vC6*xZ1!qA8%', 'ESTUDIANTE'),(11, '1725091324', 'Esteban', 'Castro', 'pL4%kO9@jU2#', 'ESTUDIANTE'),(12, '1004536312', 'Mónica', 'Chalá', 'tY7*uI5!oP1@', 'ESTUDIANTE'),(13, '2101073100', 'Karla', 'Checa', 'gH2@jK8%lP5#', 'ESTUDIANTE'),(14, '1727291823', 'Alonso', 'Constante', 'zX9$cV4*bN1!', 'ESTUDIANTE'),(15, '1751371020', 'Scarlet', 'Cordova', 'qW5#eR1@tY6*', 'ESTUDIANTE'),(16, '1755999032', 'Camila', 'De la Torre', 'aD8*sF3!gH2#', 'ESTUDIANTE'),(17, '1750144279', 'Patricia', 'Garrido', 'lK7%jH4@pL9!', 'ESTUDIANTE'),(18, '1723787535', 'Angeles', 'Gavilema', 'mN1@bV5*cX8#', 'ESTUDIANTE'),(19, '1753413333', 'Genessis', 'Goya', 'oP4#iU2@yT6!', 'ESTUDIANTE'),(20, '1729091098', 'Jennifer', 'Guachamin', 'rE9%tY3*uI7@', 'ESTUDIANTE'),(21, '1726456450', 'Jehieli', 'Herrera', 'fG6$hJ1!kS5#', 'ESTUDIANTE'),(22, '2200557136', 'Camila', 'Jaramillo', 'xZ2*c V7%bN4!', 'ESTUDIANTE'),(23, '1755731815', 'Ariel', 'Luna', 'dF8%gH3@jK1#', 'ESTUDIANTE'),(24, '1753559820', 'Juda', 'Martinez', 'eR5@tY9*uI4!', 'ESTUDIANTE'),(25, '1753728201', 'Alison', 'Minas', 'lK2#pL6@kS9$', 'ESTUDIANTE'),(26, '1722580873', 'Jean', 'Molina', 'bV7*mN3!cX1@', 'ESTUDIANTE'),(27, '1755854294', 'Ana', 'Morales', 'iU6@oP2#yT8!', 'ESTUDIANTE'),(28, '1753859352', 'Gabriel', 'Moreno', 'tY1*uI8!oP3@', 'ESTUDIANTE'),(29, '1751657089', 'Martín', 'Moya', 'hJ4#kS9$fG2!', 'ESTUDIANTE'),(30, '1750998575', 'Eduardo', 'Parra', 'cV5@bN1*xZ7#', 'ESTUDIANTE'),(31, '1754482329', 'Daniela', 'Pazmiño', 'gH9%jK4@lP1!', 'ESTUDIANTE'),(32, '1728693589', 'Willian', 'Pillajo', 'rT6*yU2!eW8@', 'ESTUDIANTE'),(33, '1751421379', 'Emilie', 'Romero', 'pL3@kS7#jH1$', 'ESTUDIANTE'),(34, '1754206744', 'Daniela', 'Salazar', 'mN8*bV4!cX2@', 'ESTUDIANTE'),(35, '2300696537', 'Leandro', 'Sarzosa', 'oP5!iU1@yT7#', 'ESTUDIANTE'),(36, '1756079768', 'Emily', 'Socasi', 'tY4%uI9*oP6!', 'ESTUDIANTE');

-- 3. Asignar Roles y Carreras
INSERT INTO Administrador (id_usuario) VALUES (1);
INSERT INTO Profesor (id_profesor, id_usuario) VALUES (1, 2), (2, 3), (3, 4), (4, 5), (5, 6);
INSERT INTO Estudiante (id_estudiante, id_usuario, id_carrera) VALUES
(1, 7, 1),(2, 8, 1),(3, 9, 1),(4, 10, 1),(5, 11, 1),(6, 12, 1),(7, 13, 1),(8, 14, 1),
(9, 15, 2),(10, 16, 2),(11, 17, 2),(12, 18, 2),(13, 19, 2),(14, 20, 2),(15, 21, 2),
(16, 22, 3),(17, 23, 3),(18, 24, 3),(19, 25, 3),(20, 26, 3),(21, 27, 3),(22, 28, 3),
(23, 29, 4),(24, 30, 4),(25, 31, 4),(26, 32, 4),(27, 33, 4),(28, 34, 4),(29, 35, 4),(30, 36, 4);

-- 4. Crear Cursos y Asignar Profesores
INSERT INTO Curso (id_curso, id_materia, id_profesor, periodo, seccion, cupo) VALUES
(1, 1, 1, '2025-S1', 'A', 20),(2, 2, 2, '2025-S1', 'A', 20),(3, 3, 1, '2025-S1', 'B', 15),
(4, 4, 3, '2025-S1', 'A', 25),(5, 5, 3, '2025-S1', 'B', 25),(6, 6, 4, '2025-S1', 'A', 30),
(7, 7, 4, '2025-S1', 'B', 30),(8, 8, 5, '2025-S1', 'A', 25),(9, 9, 5, '2025-S1', 'B', 25);

-- 5. Inscribir Estudiantes a todos los cursos de su carrera y asignar notas
INSERT INTO Calificacion (id_calificacion, id_estudiante, id_curso, nota_final, estado) VALUES
(1,1,1,78.3,'CALIFICADO'),(2,1,2,74.8,'CALIFICADO'),(3,1,3,81.4,'CALIFICADO'),(4,2,1,77.7,'CALIFICADO'),(5,2,2,82.3,'CALIFICADO'),(6,2,3,76,'CALIFICADO'),(7,3,1,85.2,'CALIFICADO'),(8,3,2,79.5,'CALIFICADO'),(9,3,3,71.2,'CALIFICADO'),(10,4,1,88.4,'CALIFICADO'),(11,4,2,79.5,'CALIFICADO'),(12,4,3,81.8,'CALIFICADO'),(13,5,1,76.5,'CALIFICADO'),(14,5,2,84.6,'CALIFICADO'),(15,5,3,80.1,'CALIFICADO'),(16,6,1,73.1,'CALIFICADO'),(17,6,2,86.2,'CALIFICADO'),(18,6,3,77.5,'CALIFICADO'),(19,7,1,82.4,'CALIFICADO'),(20,7,2,78.2,'CALIFICADO'),(21,7,3,89.5,'CALIFICADO'),(22,8,1,70.1,'CALIFICADO'),(23,8,2,83,'CALIFICADO'),(24,8,3,78.1,'CALIFICADO'),
(25,9,4,74.1,'CALIFICADO'),(26,9,5,83.9,'CALIFICADO'),(27,10,4,77.4,'CALIFICADO'),(28,10,5,86.2,'CALIFICADO'),(29,11,4,79.8,'CALIFICADO'),(30,11,5,71.2,'CALIFICADO'),(31,12,4,84.1,'CALIFICADO'),(32,12,5,78.4,'CALIFICADO'),(33,13,4,89.2,'CALIFICADO'),(34,13,5,76,'CALIFICADO'),(35,14,4,72.4,'CALIFICADO'),(36,14,5,81.3,'CALIFICADO'),(37,15,4,86.2,'CALIFICADO'),(38,15,5,79.4,'CALIFICADO'),
(39,16,6,74.8,'CALIFICADO'),(40,16,7,81.2,'CALIFICADO'),(41,17,6,85.1,'CALIFICADO'),(42,17,7,79.9,'CALIFICADO'),(43,18,6,76.9,'CALIFICADO'),(44,18,7,88.5,'CALIFICADO'),(45,19,6,71.8,'CALIFICADO'),(46,19,7,82.4,'CALIFICADO'),(47,20,6,86.7,'CALIFICADO'),(48,20,7,75.4,'CALIFICADO'),(49,21,6,80.1,'CALIFICADO'),(50,21,7,89.1,'CALIFICADO'),(51,22,6,77.7,'CALIFICADO'),(52,22,7,72.9,'CALIFICADO'),
(53,23,8,81.7,'CALIFICADO'),(54,23,9,78.5,'CALIFICADO'),(55,24,8,88.9,'CALIFICADO'),(56,24,9,74.2,'CALIFICADO'),(57,25,8,79.3,'CALIFICADO'),(58,25,9,85.6,'CALIFICADO'),(59,26,8,72.1,'CALIFICADO'),(60,26,9,80.8,'CALIFICADO'),(61,27,8,84.7,'CALIFICADO'),(62,27,9,77.9,'CALIFICADO'),(63,28,8,76.3,'CALIFICADO'),(64,28,9,89.7,'CALIFICADO'),(65,29,8,82.8,'CALIFICADO'),(66,29,9,71.4,'CALIFICADO'),(67,30,8,78.9,'CALIFICADO'),(68,30,9,87.3,'CALIFICADO');

-- 6. Insertar SUBNOTAS para CADA una de las calificaciones
INSERT INTO Subnota (id_calificacion, id_parcial, numero_nota, valor) VALUES
(1,1,1,8.5),(1,1,2,7),(1,1,3,9.2),(1,2,4,6.5),(1,2,5,8.1),(1,2,6,7.8),(1,3,7,8),(1,3,8,7.5),(1,3,9,6.9),(1,3,10,8.8),
(2,1,1,7),(2,1,2,8.2),(2,1,3,7.5),(2,2,4,8),(2,2,5,6.5),(2,2,6,7.9),(2,3,7,7.2),(2,3,8,8.1),(2,3,9,7.5),(2,3,10,9),
(3,1,1,9),(1,1,2,7.5),(3,1,3,8.8),(3,2,4,7.1),(3,2,5,8.5),(3,2,6,6.9),(3,3,7,8.2),(3,3,8,9.1),(3,3,9,7.3),(3,3,10,8.4),
(4,1,1,8.2),(4,1,2,7.1),(4,1,3,8.5),(4,2,4,7.9),(4,2,5,6.8),(4,2,6,8.2),(4,3,7,7.5),(4,3,8,8.3),(4,3,9,6.9),(4,3,10,9.1),
(5,1,1,9.1),(5,1,2,8.4),(5,1,3,7.9),(5,2,4,8.8),(5,2,5,7.5),(5,2,6,8.1),(5,3,7,7.9),(5,3,8,8.7),(5,3,9,7.2),(5,3,10,9.3),
(6,1,1,7.5),(6,1,2,7.9),(6,1,3,8.1),(6,2,4,7.2),(6,2,5,8.3),(6,2,6,6.5),(6,3,7,7.1),(6,3,8,8.9),(6,3,9,7.4),(6,3,10,8.2),
(7,1,1,9.3),(7,1,2,8.1),(7,1,3,8.9),(7,2,4,8.2),(7,2,5,7.9),(7,2,6,9.5),(7,3,7,7.8),(7,3,8,8.5),(7,3,9,7.1),(7,3,10,9),
(8,1,1,8.1),(8,1,2,7.6),(8,1,3,8.3),(8,2,4,7.8),(8,2,5,8.5),(8,2,6,7.1),(8,3,7,8),(8,3,8,9.2),(8,3,9,7.7),(8,3,10,8.6),
(9,1,1,7.2),(9,1,2,6.8),(9,1,3,7.5),(9,2,4,6.9),(9,2,5,7.2),(9,2,6,8),(9,3,7,7.1),(9,3,8,6.5),(9,3,9,7.9),(9,3,10,8.1),
(10,1,1,9.5),(10,1,2,8.8),(10,1,3,9.1),(10,2,4,8.5),(10,2,5,7.9),(10,2,6,9.2),(10,3,7,8.1),(10,3,8,8.8),(10,3,9,7.6),(10,3,10,9.4),
(11,1,1,7.8),(11,1,2,8.2),(11,1,3,7.6),(11,2,4,8.1),(11,2,5,7.4),(11,2,6,8.5),(11,3,7,7.7),(11,3,8,8.9),(11,3,9,7.3),(11,3,10,8.8),
(12,1,1,8.5),(12,1,2,7.9),(12,1,3,8.8),(12,2,4,7.6),(12,2,5,8.2),(12,2,6,7.1),(12,3,7,8.4),(12,3,8,9.1),(12,3,9,7.8),(12,3,10,8.7),
(13,1,1,7.9),(13,1,2,7.1),(13,1,3,8.2),(13,2,4,7.5),(13,2,5,8.6),(13,2,6,6.8),(13,3,7,7.3),(13,3,8,8.1),(13,3,9,7.9),(13,3,10,8.5),
(14,1,1,9.2),(14,1,2,8.5),(14,1,3,8.9),(14,2,4,8.1),(14,2,5,7.8),(14,2,6,9.3),(14,3,7,8.4),(14,3,8,8.9),(14,3,9,7.5),(14,3,10,9.1),
(15,1,1,8.8),(15,1,2,7.6),(15,1,3,8.1),(15,2,4,7.9),(15,2,5,8.2),(15,2,6,7.3),(15,3,7,8.1),(15,3,8,9),(15,3,9,7.7),(15,3,10,8.6),
(16,1,1,7.4),(16,1,2,8.1),(16,1,3,7.8),(16,2,4,7.1),(16,2,5,8.5),(16,2,6,6.9),(16,3,7,7.6),(16,3,8,8.3),(16,3,9,7.2),(16,3,10,8.9),
(17,1,1,6.8),(17,1,2,7.5),(17,1,3,7.1),(17,2,4,6.5),(17,2,5,7.8),(17,2,6,8.2),(17,3,7,7.3),(17,3,8,6.9),(17,3,9,7.6),(17,3,10,8.1),
(18,1,1,9.1),(18,1,2,8.6),(18,1,3,9.3),(18,2,4,8.2),(18,2,5,8.9),(18,2,6,7.8),(18,3,7,8.7),(18,3,8,9.2),(18,3,9,8.1),(18,3,10,9.5),
(19,1,1,7.1),(19,1,2,8.5),(19,1,3,7.9),(19,2,4,7.6),(19,2,5,8.1),(19,2,6,8.4),(19,3,7,8),(19,3,8,7.3),(19,3,9,8.8),(19,3,10,7.7),
(20,1,1,9.4),(20,1,2,8.9),(20,1,3,9.2),(20,2,4,8.7),(20,2,5,9.1),(20,2,6,8.5),(20,3,7,8.9),(20,3,8,9.4),(20,3,9,8.2),(20,3,10,9.6),
(21,1,1,7.6),(21,1,2,8.3),(21,1,3,8.1),(21,2,4,7.9),(21,2,5,8.5),(21,2,6,7.2),(21,3,7,8.3),(21,3,8,8.8),(21,3,9,7.5),(21,3,10,8.9),
(22,1,1,8.9),(22,1,2,8.1),(22,1,3,8.6),(22,2,4,8.3),(22,2,5,7.9),(22,2,6,9),(22,3,7,8.5),(22,3,8,9.1),(22,3,9,7.8),(22,3,10,9.3),
(23,1,1,9.6),(23,1,2,9.2),(23,1,3,9.5),(23,2,4,9.1),(23,2,5,8.8),(23,2,6,9.7),(23,3,7,9.3),(23,3,8,9.8),(23,3,9,8.6),(23,3,10,9.9),
(24,1,1,6.5),(24,1,2,7.2),(24,1,3,7),(24,2,4,6.8),(24,2,5,7.5),(24,2,6,6.9),(24,3,7,7.1),(24,3,8,7.3),(24,3,9,6.7),(24,3,10,7.8),
(25,1,1,7.8),(25,1,2,7.2),(25,1,3,8.1),(25,2,4,6.9),(25,2,5,7.6),(25,2,6,8.5),(25,3,7,7.4),(25,3,8,7.9),(25,3,9,8.3),(25,3,10,7),
(26,1,1,9),(26,1,2,8.5),(26,1,3,8.8),(26,2,4,8.2),(26,2,5,9.1),(26,2,6,7.9),(26,3,7,8.6),(26,3,8,9.3),(26,3,9,8.1),(26,3,10,9.5),
(27,1,1,8.1),(27,1,2,7.8),(27,1,3,8.5),(27,2,4,7.6),(27,2,5,8.9),(27,2,6,7.2),(27,3,7,8.3),(27,3,8,9.1),(27,3,9,7.7),(27,3,10,8.8),
(28,1,1,9.2),(28,1,2,8.7),(28,1,3,9.1),(28,2,4,8.5),(28,2,5,9.3),(28,2,6,8.1),(28,3,7,8.8),(28,3,8,9.5),(28,3,9,8.3),(28,3,10,9.7),
(29,1,1,7.5),(29,1,2,6.9),(29,1,3,7.8),(29,2,4,7.1),(29,2,5,6.5),(29,2,6,7.9),(29,3,7,7.3),(29,3,8,6.8),(29,3,9,7.6),(29,3,10,8.2),
(30,1,1,8.2),(30,1,2,7.5),(30,1,3,8.1),(30,2,4,7.8),(30,2,5,8.3),(30,2,6,7.1),(30,3,7,7.9),(30,3,8,8.7),(30,3,9,7.4),(30,3,10,8.9),
(31,1,1,7.9),(31,1,2,8.4),(31,1,3,7.8),(31,2,4,8.2),(31,2,5,7.5),(31,2,6,8.6),(31,3,7,7.7),(31,3,8,8.1),(31,3,9,7.3),(31,3,10,8.5),
(32,1,1,6.9),(32,1,2,7.3),(32,1,3,6.5),(32,2,4,7.1),(32,2,5,6.8),(32,2,6,7.5),(32,3,7,6.7),(32,3,8,7.2),(32,3,9,6.9),(32,3,10,7.7),
(33,1,1,9.5),(33,1,2,9.1),(33,1,3,9.7),(33,2,4,8.9),(33,2,5,9.4),(33,2,6,8.7),(33,3,7,9.2),(33,3,8,9.8),(33,3,9,8.5),(33,3,10,9.9),
(34,1,1,8.3),(34,1,2,7.8),(34,1,3,8.6),(34,2,4,7.5),(34,2,5,8.1),(34,2,6,7.9),(34,3,7,8.4),(34,3,8,8.9),(34,3,9,7.6),(34,3,10,9.1),
(35,1,1,8.9),(35,1,2,8.2),(35,1,3,8.7),(35,2,4,8.4),(35,2,5,7.9),(35,2,6,9.1),(35,3,7,8.6),(35,3,8,9.2),(35,3,9,8),(35,3,10,9.4),
(36,1,1,7.6),(36,1,2,8.1),(36,1,3,7.9),(36,2,4,7.3),(36,2,5,8.5),(36,2,6,7.1),(36,3,7,7.8),(36,3,8,8.4),(36,3,9,7.2),(36,3,10,8.7),
(37,1,1,8.5),(37,1,2,7.9),(37,1,3,8.8),(37,2,4,8.1),(37,2,5,8.7),(37,2,6,7.6),(37,3,7,8.3),(37,3,8,9),(37,3,9,7.9),(37,3,10,9.2),
(38,1,1,9.8),(38,1,2,9.4),(38,1,3,9.7),(38,2,4,9.2),(38,2,5,9.6),(38,2,6,9),(38,3,7,9.5),(38,3,8,9.9),(38,3,9,9.1),(38,3,10,10),
(39,1,1,7.3),(39,1,2,6.8),(39,1,3,7.5),(39,2,4,6.9),(39,2,5,7.1),(39,2,6,7.7),(39,3,7,7),(39,3,8,6.5),(39,3,9,7.4),(39,3,10,8),
(40,1,1,7.5),(40,1,2,8.1),(40,1,3,7.3),(40,2,4,7.9),(40,2,5,8.4),(40,2,6,7.1),(40,3,7,8.2),(40,3,8,8.8),(40,3,9,7.5),(40,3,10,9),
(41,1,1,8.8),(41,1,2,8.3),(41,1,3,9),(41,2,4,8.1),(41,2,5,8.7),(41,2,6,8.4),(41,3,7,8.9),(41,3,8,9.2),(41,3,9,8.5),(41,3,10,9.6),
(42,1,1,9.4),(42,1,2,9),(42,1,3,9.6),(42,2,4,8.8),(42,2,5,9.3),(42,2,6,9.1),(42,3,7,9.5),(42,3,8,9.7),(42,3,9,8.9),(42,3,10,9.8),
(43,1,1,8.2),(43,1,2,7.7),(43,1,3,8.4),(43,2,4,7.9),(43,2,5,8.1),(43,2,6,8.6),(43,3,7,8.3),(43,3,8,8.8),(43,3,9,7.5),(43,3,10,9),
(44,1,1,9.1),(44,1,2,8.5),(44,1,3,8.9),(44,2,4,8.3),(44,2,5,9.2),(44,2,6,8),(44,3,7,8.7),(44,3,8,9.4),(44,3,9,8.2),(44,3,10,9.5),
(45,1,1,8.7),(45,1,2,9.3),(45,1,3,8.9),(45,2,4,9.1),(45,2,5,8.5),(45,2,6,9.4),(45,3,7,9),(45,3,8,9.6),(45,3,9,8.8),(45,3,10,9.7),
(46,1,1,7.4),(46,1,2,6.9),(46,1,3,7.7),(46,2,4,7.2),(46,2,5,6.8),(46,2,6,8),(46,3,7,7.5),(46,3,8,7.1),(46,3,9,7.9),(46,3,10,8.3),
(47,1,1,9.6),(47,1,2,9.3),(47,1,3,9.8),(47,2,4,9.1),(47,2,5,9.7),(47,2,6,8.9),(47,3,7,9.4),(47,3,8,9.9),(47,3,9,9),(47,3,10,9.9),
(48,1,1,8.4),(48,1,2,7.9),(48,1,3,8.2),(48,2,4,8.1),(48,2,5,8.6),(48,2,6,7.8),(48,3,7,8.5),(48,3,8,9),(48,3,9,8.3),(48,3,10,9.2),
(49,1,1,6.7),(49,1,2,7.1),(49,1,3,6.9),(49,2,4,7.3),(49,2,5,6.5),(49,2,6,7.8),(49,3,7,7),(49,3,8,6.8),(49,3,9,7.2),(49,3,10,7.9),
(50,1,1,9.2),(50,1,2,8.8),(50,1,3,9.1),(50,2,4,8.5),(50,2,5,9.3),(50,2,6,8.7),(50,3,7,9),(50,3,8,9.5),(50,3,9,8.6),(50,3,10,9.8),
(51,1,1,7.9),(51,1,2,7.4),(51,1,3,8),(51,2,4,7.6),(51,2,5,8.1),(51,2,6,8.3),(51,3,7,8.2),(51,3,8,7.8),(51,3,9,8.5),(51,3,10,7.9),
(52,1,1,9.3),(52,1,2,9.1),(52,1,3,9.5),(52,2,4,8.9),(52,2,5,9.2),(52,2,6,9.4),(52,3,7,9.6),(52,3,8,9.8),(52,3,9,9.1),(52,3,10,9.7),
(53,1,1,7.7),(53,1,2,8.1),(53,1,3,8.4),(53,2,4,7.9),(53,2,5,8.3),(53,2,6,7.5),(53,3,7,8.2),(53,3,8,8.7),(53,3,9,8),(53,3,10,8.9),
(54,1,1,8.8),(54,1,2,8.4),(54,1,3,8.9),(54,2,4,8.2),(54,2,5,8.7),(54,2,6,8.5),(54,3,7,8.9),(54,3,8,9.1),(54,3,9,8.6),(54,3,10,9.3),
(55,1,1,9.7),(55,1,2,9.4),(55,1,3,9.8),(55,2,4,9.2),(55,2,5,9.6),(55,2,6,9.1),(55,3,7,9.5),(55,3,8,9.9),(55,3,9,9.3),(55,3,10,9.8),
(56,1,1,7.2),(56,1,2,6.8),(56,1,3,7.5),(56,2,4,7),(56,2,5,7.3),(56,2,6,7.6),(56,3,7,7.1),(56,3,8,6.9),(56,3,9,7.4),(56,3,10,7.9),
(57,1,1,8.6),(57,1,2,8.1),(57,1,3,8.8),(57,2,4,7.9),(57,2,5,8.4),(57,2,6,8.2),(57,3,7,8.5),(57,3,8,9),(57,3,9,8.1),(57,3,10,9.2),
(58,1,1,7.8),(58,1,2,8.3),(58,1,3,7.6),(58,2,4,8),(58,2,5,8.5),(58,2,6,7.7),(58,3,7,8.1),(58,3,8,8.6),(58,3,9,7.9),(58,3,10,8.8),
(59,1,1,9.3),(59,1,2,8.9),(59,1,3,9.5),(59,2,4,8.8),(59,2,5,9.2),(59,2,6,9),(59,3,7,9.4),(59,3,8,9.7),(59,3,9,8.9),(59,3,10,9.6),
(60,1,1,8.4),(60,1,2,8),(60,1,3,8.7),(60,2,4,8.2),(60,2,5,8.5),(60,2,6,8.1),(60,3,7,8.6),(60,3,8,9),(60,3,9,8.3),(60,3,10,9.1),
(61,1,1,7.5),(61,1,2,8),(61,1,3,7.8),(61,2,4,7.7),(61,2,5,8.2),(61,2,6,7.4),(61,3,7,8),(61,3,8,8.5),(61,3,9,7.6),(61,3,10,8.7),
(62,1,1,8.1),(62,1,2,8.6),(62,1,3,8.3),(62,2,4,8.4),(62,2,5,8.9),(62,2,6,8.2),(62,3,7,8.7),(62,3,8,9.1),(62,3,9,8.5),(62,3,10,9.3),
(63,1,1,8.4),(63,1,2,7.9),(63,1,3,8.2),(63,2,4,8.1),(63,2,5,8.6),(63,2,6,7.8),(63,3,7,8.5),(63,3,8,9),(63,3,9,8.3),(63,3,10,9.2),
(64,1,1,7.2),(64,1,2,6.8),(64,1,3,7.5),(64,2,4,7),(64,2,5,7.3),(64,2,6,7.6),(64,3,7,7.1),(64,3,8,6.9),(64,3,9,7.4),(64,3,10,7.9),
(65,1,1,9.5),(65,1,2,9.1),(65,1,3,9.7),(65,2,4,8.9),(65,2,5,9.4),(65,2,6,8.7),(65,3,7,9.2),(65,3,8,9.8),(65,3,9,8.5),(65,3,10,9.9),
(66,1,1,8.3),(66,1,2,7.8),(66,1,3,8.6),(66,2,4,7.5),(66,2,5,8.1),(66,2,6,7.9),(66,3,7,8.4),(66,3,8,8.9),(66,3,9,7.6),(66,3,10,9.1),
(67,1,1,8.9),(67,1,2,8.2),(67,1,3,8.7),(67,2,4,8.4),(67,2,5,7.9),(67,2,6,9.1),(67,3,7,8.6),(67,3,8,9.2),(67,3,9,8),(67,3,10,9.4),
(68,1,1,7.6),(68,1,2,8.1),(68,1,3,7.9),(68,2,4,7.3),(68,2,5,8.5),(68,2,6,7.1),(68,3,7,7.8),(68,3,8,8.4),(68,3,9,7.2),(68,3,10,8.7);