-- =====================================================
-- SCRIPT PARA LIMPIAR BASE DE DATOS
-- Hospital Santa Vida - Limpiar datos para demo
-- =====================================================

USE hospital_santa_vida;

-- Deshabilitar verificación de claves foráneas temporalmente
SET FOREIGN_KEY_CHECKS = 0;

-- Limpiar todas las tablas de datos (mantener estructura)
DELETE FROM datos_sociales;
DELETE FROM evaluaciones_medicas;  
DELETE FROM atenciones_medicas;
DELETE FROM registros_triage;
DELETE FROM pacientes;

-- Reiniciar los contadores AUTO_INCREMENT
ALTER TABLE datos_sociales AUTO_INCREMENT = 1;
ALTER TABLE evaluaciones_medicas AUTO_INCREMENT = 1;
ALTER TABLE atenciones_medicas AUTO_INCREMENT = 1;
ALTER TABLE registros_triage AUTO_INCREMENT = 1;
ALTER TABLE pacientes AUTO_INCREMENT = 1;

-- Reactivar verificación de claves foráneas
SET FOREIGN_KEY_CHECKS = 1;

-- Mostrar confirmación
SELECT 'Base de datos limpiada exitosamente. Todas las tablas están vacías y listas para nuevos registros.' AS mensaje;

-- Verificar que las tablas estén vacías
SELECT 
    'pacientes' as tabla, 
    COUNT(*) as registros 
FROM pacientes
UNION ALL
SELECT 
    'registros_triage' as tabla, 
    COUNT(*) as registros 
FROM registros_triage
UNION ALL
SELECT 
    'datos_sociales' as tabla, 
    COUNT(*) as registros 
FROM datos_sociales
UNION ALL
SELECT 
    'evaluaciones_medicas' as tabla, 
    COUNT(*) as registros 
FROM evaluaciones_medicas
UNION ALL
SELECT 
    'atenciones_medicas' as tabla, 
    COUNT(*) as registros 
FROM atenciones_medicas;