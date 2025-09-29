-- =====================================================
-- INSERTAR PACIENTES DE PRUEBA
-- Para resolver el problema de foreign key constraint
-- =====================================================

USE hospital_santa_vida;

-- Insertar pacientes de prueba
INSERT INTO pacientes (
    nombre, 
    apellido_paterno, 
    apellido_materno, 
    fecha_nacimiento, 
    sexo, 
    curp, 
    telefono, 
    email, 
    direccion_calle, 
    direccion_numero, 
    direccion_colonia, 
    direccion_ciudad, 
    direccion_estado, 
    direccion_cp
) VALUES 
(
    'Juan Carlos', 
    'Pérez', 
    'García', 
    '1985-03-15', 
    'MASCULINO', 
    'PEGJ850315HDFRRL01', 
    '5551234567', 
    'juan.perez@email.com', 
    'Av. Revolución', 
    '123', 
    'Centro', 
    'Monterrey', 
    'Nuevo León', 
    '64000'
),
(
    'María Elena', 
    'Rodríguez', 
    'López', 
    '1990-07-22', 
    'FEMENINO', 
    'ROLM900722MDFRPR08', 
    '5559876543', 
    'maria.rodriguez@email.com', 
    'Calle Morelos', 
    '456', 
    'San Pedro', 
    'Monterrey', 
    'Nuevo León', 
    '66200'
),
(
    'Carlos Alberto', 
    'Martínez', 
    'Hernández', 
    '1978-12-10', 
    'MASCULINO', 
    'MAHC781210HDFRRL09', 
    '5551122334', 
    'carlos.martinez@email.com', 
    'Blvd. Constitución', 
    '789', 
    'Mitras', 
    'Monterrey', 
    'Nuevo León', 
    '64460'
),
(
    'Ana Sofía', 
    'González', 
    'Ruiz', 
    '1995-05-08', 
    'FEMENINO', 
    'GORA950508MDFNZN06', 
    '5554433221', 
    'ana.gonzalez@email.com', 
    'Calle Hidalgo', 
    '321', 
    'Del Valle', 
    'Monterrey', 
    'Nuevo León', 
    '64750'
),
(
    'Pedro Miguel', 
    'Sánchez', 
    'Morales', 
    '1982-11-25', 
    'MASCULINO', 
    'SAMP821125HDFRDR04', 
    '5556677889', 
    'pedro.sanchez@email.com', 
    'Av. Lincoln', 
    '654', 
    'Residencial', 
    'Monterrey', 
    'Nuevo León', 
    '64920'
);

-- Verificar los pacientes insertados
SELECT id, nombre, apellido_paterno, apellido_materno, fecha_nacimiento 
FROM pacientes 
ORDER BY id;