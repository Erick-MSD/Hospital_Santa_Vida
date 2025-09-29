-- =====================================================
-- Script de Base de Datos: Hospital Santa Vida
-- Sistema de Triage Hospitalario
-- Fecha: Septiembre 2025
-- =====================================================

-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS hospital_santa_vida 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hospital_santa_vida;

-- =====================================================
-- TABLA USUARIOS
-- Empleados del hospital que pueden acceder al sistema
-- =====================================================
CREATE TABLE usuarios (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    tipo_usuario ENUM('ADMINISTRADOR', 'MEDICO_TRIAGE', 'ASISTENTE_MEDICA', 'TRABAJADOR_SOCIAL', 'MEDICO_URGENCIAS') NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    cedula_profesional VARCHAR(20),
    especialidad VARCHAR(100),
    telefono VARCHAR(15),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso TIMESTAMP NULL,
    created_by INT,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_tipo_usuario (tipo_usuario),
    FOREIGN KEY (created_by) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- =====================================================
-- TABLA PACIENTES
-- Información personal de todos los pacientes
-- Esta tabla mantiene el historial completo, nunca se borra
-- =====================================================
CREATE TABLE pacientes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100),
    fecha_nacimiento DATE NOT NULL,
    sexo ENUM('MASCULINO', 'FEMENINO', 'OTRO') NOT NULL,
    curp VARCHAR(18) UNIQUE,
    rfc VARCHAR(13),
    telefono_principal VARCHAR(15) NOT NULL,
    telefono_secundario VARCHAR(15),
    email VARCHAR(100),
    direccion_calle VARCHAR(200) NOT NULL,
    direccion_numero VARCHAR(20) NOT NULL,
    direccion_colonia VARCHAR(100) NOT NULL,
    direccion_ciudad VARCHAR(100) NOT NULL,
    direccion_estado VARCHAR(100) NOT NULL,
    direccion_cp VARCHAR(5) NOT NULL,
    seguro_medico VARCHAR(100),
    numero_poliza VARCHAR(50),
    contacto_emergencia_nombre VARCHAR(150) NOT NULL,
    contacto_emergencia_telefono VARCHAR(15) NOT NULL,
    contacto_emergencia_relacion VARCHAR(50) NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_nombre_completo (nombre, apellido_paterno, apellido_materno),
    INDEX idx_curp (curp),
    INDEX idx_telefono (telefono_principal)
);

-- =====================================================
-- TABLA REGISTROS_TRIAGE
-- Cada visita a urgencias genera un nuevo registro
-- Aquí se almacena la evaluación de triage y estado actual
-- =====================================================
CREATE TABLE registros_triage (
    id INT PRIMARY KEY AUTO_INCREMENT,
    folio VARCHAR(20) UNIQUE NOT NULL,
    paciente_id INT NOT NULL,
    medico_triage_id INT NOT NULL,
    fecha_hora_llegada TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_hora_triage TIMESTAMP NULL,
    motivo_consulta TEXT NOT NULL,
    sintomas_principales TEXT,
    -- Signos vitales
    presion_sistolica INT,
    presion_diastolica INT,
    frecuencia_cardiaca INT,
    frecuencia_respiratoria INT,
    temperatura DECIMAL(4,2),
    saturacion_oxigeno INT,
    glasgow INT,
    -- Clasificación de triage
    nivel_urgencia ENUM('ROJO', 'NARANJA', 'AMARILLO', 'VERDE', 'AZUL') NOT NULL,
    especialidad_asignada VARCHAR(100),
    observaciones_triage TEXT,
    -- Estado del proceso
    estado ENUM('ESPERANDO_ASISTENTE', 'ESPERANDO_TRABAJO_SOCIAL', 'ESPERANDO_MEDICO', 'EN_ATENCION', 'COMPLETADO', 'CITA_PROGRAMADA') DEFAULT 'ESPERANDO_ASISTENTE',
    prioridad_orden INT, -- Para manejo de colas
    fecha_ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_folio (folio),
    INDEX idx_paciente (paciente_id),
    INDEX idx_nivel_urgencia (nivel_urgencia),
    INDEX idx_estado (estado),
    INDEX idx_fecha_llegada (fecha_hora_llegada),
    FOREIGN KEY (paciente_id) REFERENCES pacientes(id) ON DELETE CASCADE,
    FOREIGN KEY (medico_triage_id) REFERENCES usuarios(id) ON DELETE RESTRICT
);

-- =====================================================
-- TABLA DATOS_SOCIALES
-- Información capturada por trabajador social
-- Antecedentes médicos familiares e historia clínica
-- =====================================================
CREATE TABLE datos_sociales (
    id INT PRIMARY KEY AUTO_INCREMENT,
    registro_triage_id INT UNIQUE NOT NULL,
    trabajador_social_id INT NOT NULL,
    fecha_hora_entrevista TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    antecedentes_familiares TEXT,
    enfermedades_cronicas TEXT,
    medicamentos_actuales TEXT,
    alergias_conocidas TEXT,
    cirugias_previas TEXT,
    hospitalizaciones_previas TEXT,
    vacunas_recientes TEXT,
    habitos_toxicos TEXT, -- tabaco, alcohol, drogas
    situacion_socioeconomica TEXT,
    observaciones_adicionales TEXT,
    FOREIGN KEY (registro_triage_id) REFERENCES registros_triage(id) ON DELETE CASCADE,
    FOREIGN KEY (trabajador_social_id) REFERENCES usuarios(id) ON DELETE RESTRICT
);

-- =====================================================
-- TABLA CITAS_MEDICAS
-- Solo para pacientes nivel AZUL que reciben cita ambulatoria
-- =====================================================
CREATE TABLE citas_medicas (
    id INT PRIMARY KEY AUTO_INCREMENT,
    registro_triage_id INT UNIQUE NOT NULL,
    asistente_medica_id INT NOT NULL,
    fecha_programada DATE NOT NULL,
    hora_programada TIME NOT NULL,
    especialidad VARCHAR(100) NOT NULL,
    medico_asignado VARCHAR(150),
    consultorio VARCHAR(20),
    estado_cita ENUM('PROGRAMADA', 'CONFIRMADA', 'CANCELADA', 'COMPLETADA', 'NO_ASISTIO') DEFAULT 'PROGRAMADA',
    motivo_cancelacion TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observaciones TEXT,
    INDEX idx_fecha_programada (fecha_programada, hora_programada),
    INDEX idx_estado_cita (estado_cita),
    FOREIGN KEY (registro_triage_id) REFERENCES registros_triage(id) ON DELETE CASCADE,
    FOREIGN KEY (asistente_medica_id) REFERENCES usuarios(id) ON DELETE RESTRICT
);

-- =====================================================
-- TABLA ATENCION_MEDICA
-- Diagnóstico y tratamiento final por médico de urgencias
-- =====================================================
CREATE TABLE atencion_medica (
    id INT PRIMARY KEY AUTO_INCREMENT,
    registro_triage_id INT UNIQUE NOT NULL,
    medico_urgencias_id INT NOT NULL,
    fecha_hora_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_hora_fin TIMESTAMP NULL,
    diagnostico_principal VARCHAR(500) NOT NULL,
    diagnosticos_secundarios TEXT,
    tratamiento_aplicado TEXT NOT NULL,
    medicamentos_prescritos TEXT,
    instrucciones_alta TEXT,
    tipo_alta ENUM('DOMICILIO', 'HOSPITALIZACION', 'REFERENCIA', 'DEFUNCION') NOT NULL,
    hospital_referencia VARCHAR(200),
    tiempo_total_atencion INT, -- minutos desde llegada hasta alta
    seguimiento_requerido BOOLEAN DEFAULT FALSE,
    fecha_seguimiento DATE,
    observaciones_medicas TEXT,
    INDEX idx_fecha_atencion (fecha_hora_inicio),
    INDEX idx_tipo_alta (tipo_alta),
    FOREIGN KEY (registro_triage_id) REFERENCES registros_triage(id) ON DELETE CASCADE,
    FOREIGN KEY (medico_urgencias_id) REFERENCES usuarios(id) ON DELETE RESTRICT
);

-- =====================================================
-- TRIGGERS PARA GENERACIÓN AUTOMÁTICA DE FOLIOS
-- =====================================================
DELIMITER //

CREATE TRIGGER generate_folio_triage
    BEFORE INSERT ON registros_triage
    FOR EACH ROW
BEGIN
    DECLARE next_number INT;
    DECLARE folio_year VARCHAR(4);
    
    SET folio_year = YEAR(NOW());
    
    SELECT COALESCE(MAX(CAST(SUBSTRING(folio, 10) AS UNSIGNED)), 0) + 1 
    INTO next_number
    FROM registros_triage 
    WHERE folio LIKE CONCAT('TRG-', folio_year, '-%');
    
    SET NEW.folio = CONCAT('TRG-', folio_year, '-', LPAD(next_number, 4, '0'));
END//

DELIMITER ;

-- =====================================================
-- DATOS INICIALES - Usuario Administrador por defecto
-- =====================================================
INSERT INTO usuarios (
    username, 
    email, 
    password_hash, 
    tipo_usuario, 
    nombre_completo,
    cedula_profesional,
    especialidad,
    telefono,
    activo
) VALUES (
    'admin',
    'admin@hospitalsantavida.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- password: "password123"
    'ADMINISTRADOR',
    'Administrador del Sistema',
    NULL,
    'Administración',
    '8001234567',
    TRUE
);

-- Usuarios de ejemplo para testing
INSERT INTO usuarios (
    username, 
    email, 
    password_hash, 
    tipo_usuario, 
    nombre_completo,
    cedula_profesional,
    especialidad,
    telefono,
    activo,
    created_by
) VALUES 
(
    'dr.garcia',
    'garcia@hospitalsantavida.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'MEDICO_TRIAGE',
    'Dr. Carlos García Mendoza',
    '12345678',
    'Medicina de Urgencias',
    '8001234568',
    TRUE,
    1
),
(
    'asist.maria',
    'maria@hospitalsantavida.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'ASISTENTE_MEDICA',
    'María Elena Rodríguez',
    NULL,
    'Asistente Médica',
    '8001234569',
    TRUE,
    1
),
(
    'social.ana',
    'ana@hospitalsantavida.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'TRABAJADOR_SOCIAL',
    'Ana Patricia López',
    NULL,
    'Trabajo Social',
    '8001234570',
    TRUE,
    1
),
(
    'dr.martinez',
    'martinez@hospitalsantavida.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'MEDICO_URGENCIAS',
    'Dr. Roberto Martínez Silva',
    '87654321',
    'Medicina Interna',
    '8001234571',
    TRUE,
    1
);

-- =====================================================
-- ÍNDICES ADICIONALES PARA OPTIMIZACIÓN
-- =====================================================
CREATE INDEX idx_registros_activos ON registros_triage(estado, nivel_urgencia, fecha_hora_llegada);
CREATE INDEX idx_pacientes_nombre ON pacientes(nombre, apellido_paterno);
CREATE INDEX idx_usuarios_activos ON usuarios(activo, tipo_usuario);

-- =====================================================
-- VISTAS ÚTILES PARA CONSULTAS FRECUENTES
-- =====================================================

-- Vista de pacientes en sala de espera
CREATE VIEW v_sala_espera AS
SELECT 
    rt.folio,
    CONCAT(p.nombre, ' ', p.apellido_paterno) as nombre_paciente,
    rt.nivel_urgencia,
    rt.estado,
    rt.fecha_hora_llegada,
    rt.especialidad_asignada,
    TIMESTAMPDIFF(MINUTE, rt.fecha_hora_llegada, NOW()) as minutos_espera
FROM registros_triage rt
JOIN pacientes p ON rt.paciente_id = p.id
WHERE rt.estado IN ('ESPERANDO_ASISTENTE', 'ESPERANDO_TRABAJO_SOCIAL', 'ESPERANDO_MEDICO')
ORDER BY 
    CASE rt.nivel_urgencia 
        WHEN 'ROJO' THEN 1
        WHEN 'NARANJA' THEN 2  
        WHEN 'AMARILLO' THEN 3
        WHEN 'VERDE' THEN 4
        WHEN 'AZUL' THEN 5
    END,
    rt.fecha_hora_llegada;

-- Vista de estadísticas diarias
CREATE VIEW v_estadisticas_dia AS
SELECT 
    DATE(fecha_hora_llegada) as fecha,
    COUNT(*) as total_pacientes,
    SUM(CASE WHEN nivel_urgencia = 'ROJO' THEN 1 ELSE 0 END) as nivel_rojo,
    SUM(CASE WHEN nivel_urgencia = 'NARANJA' THEN 1 ELSE 0 END) as nivel_naranja,
    SUM(CASE WHEN nivel_urgencia = 'AMARILLO' THEN 1 ELSE 0 END) as nivel_amarillo,
    SUM(CASE WHEN nivel_urgencia = 'VERDE' THEN 1 ELSE 0 END) as nivel_verde,
    SUM(CASE WHEN nivel_urgencia = 'AZUL' THEN 1 ELSE 0 END) as nivel_azul,
    SUM(CASE WHEN estado = 'COMPLETADO' THEN 1 ELSE 0 END) as completados,
    AVG(CASE WHEN estado = 'COMPLETADO' THEN 
        TIMESTAMPDIFF(MINUTE, fecha_hora_llegada, fecha_ultima_actualizacion) 
        ELSE NULL END) as tiempo_promedio_atencion
FROM registros_triage
GROUP BY DATE(fecha_hora_llegada)
ORDER BY fecha DESC;

-- =====================================================
-- VERIFICACIÓN DE INTEGRIDAD
-- =====================================================
-- Verificar que las tablas se crearon correctamente
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    TABLE_COMMENT
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'hospital_santa_vida'
ORDER BY TABLE_NAME;

-- Verificar usuarios creados
SELECT id, username, tipo_usuario, nombre_completo, activo FROM usuarios;

COMMIT;