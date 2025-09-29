package dao;

import models.AtencionMedica;
import models.Especialidad;
import utils.ValidationUtils;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * DAO para la gestión de atención médica en el sistema hospitalario
 * Maneja todas las operaciones CRUD para la tabla atencion_medica
 * Incluye funcionalidades específicas de consultas médicas
 */
public class AtencionMedicaDAO extends BaseDAO<AtencionMedica> {
    
    private static final String TABLA = "atencion_medica";
    
    // Consultas SQL predefinidas
    private static final String SQL_INSERTAR = 
        "INSERT INTO " + TABLA + " (paciente_id, medico_id, fecha_consulta, " +
        "especialidad_medica, motivo_consulta, exploracion_fisica, diagnostico, " +
        "tratamiento_prescrito, medicamentos_prescritos, observaciones_medicas, " +
        "proxima_cita, requiere_hospitalizacion, requiere_cirugia, " +
        "requiere_interconsulta, especialidad_interconsulta) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SQL_ACTUALIZAR = 
        "UPDATE " + TABLA + " SET paciente_id = ?, medico_id = ?, fecha_consulta = ?, " +
        "especialidad_medica = ?, motivo_consulta = ?, exploracion_fisica = ?, " +
        "diagnostico = ?, tratamiento_prescrito = ?, medicamentos_prescritos = ?, " +
        "observaciones_medicas = ?, proxima_cita = ?, requiere_hospitalizacion = ?, " +
        "requiere_cirugia = ?, requiere_interconsulta = ?, especialidad_interconsulta = ? " +
        "WHERE id = ?";
    
    private static final String SQL_ELIMINAR = 
        "DELETE FROM " + TABLA + " WHERE id = ?";
    
    private static final String SQL_BUSCAR_POR_ID = 
        "SELECT am.*, p.nombre_completo as paciente_nombre, p.numero_expediente, " +
        "u.nombre_completo as medico_nombre " +
        "FROM " + TABLA + " am " +
        "JOIN pacientes p ON am.paciente_id = p.id " +
        "JOIN usuarios u ON am.medico_id = u.id " +
        "WHERE am.id = ?";
    
    private static final String SQL_OBTENER_TODOS = 
        "SELECT am.*, p.nombre_completo as paciente_nombre, p.numero_expediente, " +
        "u.nombre_completo as medico_nombre " +
        "FROM " + TABLA + " am " +
        "JOIN pacientes p ON am.paciente_id = p.id " +
        "JOIN usuarios u ON am.medico_id = u.id " +
        "ORDER BY am.fecha_consulta DESC";
    
    private static final String SQL_BUSCAR_POR_PACIENTE = 
        "SELECT am.*, p.nombre_completo as paciente_nombre, p.numero_expediente, " +
        "u.nombre_completo as medico_nombre " +
        "FROM " + TABLA + " am " +
        "JOIN pacientes p ON am.paciente_id = p.id " +
        "JOIN usuarios u ON am.medico_id = u.id " +
        "WHERE am.paciente_id = ? ORDER BY am.fecha_consulta DESC";
    
    private static final String SQL_BUSCAR_POR_MEDICO = 
        "SELECT am.*, p.nombre_completo as paciente_nombre, p.numero_expediente, " +
        "u.nombre_completo as medico_nombre " +
        "FROM " + TABLA + " am " +
        "JOIN pacientes p ON am.paciente_id = p.id " +
        "JOIN usuarios u ON am.medico_id = u.id " +
        "WHERE am.medico_id = ? ORDER BY am.fecha_consulta DESC";
    
    private static final String SQL_BUSCAR_POR_ESPECIALIDAD = 
        "SELECT am.*, p.nombre_completo as paciente_nombre, p.numero_expediente, " +
        "u.nombre_completo as medico_nombre " +
        "FROM " + TABLA + " am " +
        "JOIN pacientes p ON am.paciente_id = p.id " +
        "JOIN usuarios u ON am.medico_id = u.id " +
        "WHERE am.especialidad_medica = ? ORDER BY am.fecha_consulta DESC";
    
    private static final String SQL_BUSCAR_REQUIEREN_HOSPITALIZACION = 
        "SELECT am.*, p.nombre_completo as paciente_nombre, p.numero_expediente, " +
        "u.nombre_completo as medico_nombre " +
        "FROM " + TABLA + " am " +
        "JOIN pacientes p ON am.paciente_id = p.id " +
        "JOIN usuarios u ON am.medico_id = u.id " +
        "WHERE am.requiere_hospitalizacion = 1 ORDER BY am.fecha_consulta DESC";
    
    private static final String SQL_BUSCAR_REQUIEREN_CIRUGIA = 
        "SELECT am.*, p.nombre_completo as paciente_nombre, p.numero_expediente, " +
        "u.nombre_completo as medico_nombre " +
        "FROM " + TABLA + " am " +
        "JOIN pacientes p ON am.paciente_id = p.id " +
        "JOIN usuarios u ON am.medico_id = u.id " +
        "WHERE am.requiere_cirugia = 1 ORDER BY am.fecha_consulta DESC";
    
    private static final String SQL_BUSCAR_REQUIEREN_INTERCONSULTA = 
        "SELECT am.*, p.nombre_completo as paciente_nombre, p.numero_expediente, " +
        "u.nombre_completo as medico_nombre " +
        "FROM " + TABLA + " am " +
        "JOIN pacientes p ON am.paciente_id = p.id " +
        "JOIN usuarios u ON am.medico_id = u.id " +
        "WHERE am.requiere_interconsulta = 1 ORDER BY am.fecha_consulta DESC";
    
    private static final String SQL_BUSCAR_POR_FECHA = 
        "SELECT am.*, p.nombre_completo as paciente_nombre, p.numero_expediente, " +
        "u.nombre_completo as medico_nombre " +
        "FROM " + TABLA + " am " +
        "JOIN pacientes p ON am.paciente_id = p.id " +
        "JOIN usuarios u ON am.medico_id = u.id " +
        "WHERE DATE(am.fecha_consulta) = ? ORDER BY am.fecha_consulta DESC";
    
    /**
     * Inserta una nueva atención médica en la base de datos
     * @param atencion Atención médica a insertar
     * @return true si se insertó correctamente
     * @throws SQLException si hay error en la operación
     */
    @Override
    public boolean insertar(AtencionMedica atencion) throws SQLException {
        validarAtencionMedica(atencion);
        
        int idGenerado = ejecutarInsercionConClave(SQL_INSERTAR,
            atencion.getPacienteId(),
            atencion.getMedicoId(),
            convertirATimestamp(atencion.getFechaConsulta()),
            atencion.getEspecialidadMedica().name(),
            atencion.getMotivoConsulta(),
            atencion.getExploracionFisica(),
            atencion.getDiagnostico(),
            atencion.getTratamientoPrescrito(),
            atencion.getMedicamentosPrescritos(),
            atencion.getObservacionesMedicas(),
            convertirATimestamp(atencion.getProximaCita()),
            atencion.isRequiereHospitalizacion(),
            atencion.isRequiereCirugia(),
            atencion.isRequiereInterconsulta(),
            atencion.getEspecialidadInterconsulta() != null ? 
                atencion.getEspecialidadInterconsulta().name() : null
        );
        
        if (idGenerado > 0) {
            atencion.setId(idGenerado);
            return true;
        }
        
        return false;
    }
    
    /**
     * Actualiza una atención médica existente
     * @param atencion Atención médica a actualizar
     * @return true si se actualizó correctamente
     * @throws SQLException si hay error en la operación
     */
    @Override
    public boolean actualizar(AtencionMedica atencion) throws SQLException {
        validarAtencionMedica(atencion);
        
        if (atencion.getId() <= 0) {
            throw new IllegalArgumentException("ID de atención médica inválido");
        }
        
        int filasActualizadas = ejecutarActualizacion(SQL_ACTUALIZAR,
            atencion.getPacienteId(),
            atencion.getMedicoId(),
            convertirATimestamp(atencion.getFechaConsulta()),
            atencion.getEspecialidadMedica().name(),
            atencion.getMotivoConsulta(),
            atencion.getExploracionFisica(),
            atencion.getDiagnostico(),
            atencion.getTratamientoPrescrito(),
            atencion.getMedicamentosPrescritos(),
            atencion.getObservacionesMedicas(),
            convertirATimestamp(atencion.getProximaCita()),
            atencion.isRequiereHospitalizacion(),
            atencion.isRequiereCirugia(),
            atencion.isRequiereInterconsulta(),
            atencion.getEspecialidadInterconsulta() != null ? 
                atencion.getEspecialidadInterconsulta().name() : null,
            atencion.getId()
        );
        
        return filasActualizadas > 0;
    }
    
    /**
     * Elimina una atención médica por su ID
     * @param id ID de la atención médica a eliminar
     * @return true si se eliminó correctamente
     * @throws SQLException si hay error en la operación
     */
    @Override
    public boolean eliminar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de atención médica inválido");
        }
        
        int filasEliminadas = ejecutarActualizacion(SQL_ELIMINAR, id);
        return filasEliminadas > 0;
    }
    
    /**
     * Busca una atención médica por su ID
     * @param id ID de la atención médica
     * @return Atención médica encontrada o null si no existe
     * @throws SQLException si hay error en la operación
     */
    @Override
    public AtencionMedica buscarPorId(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de atención médica inválido");
        }
        
        return ejecutarConsultaUnica(SQL_BUSCAR_POR_ID, id);
    }
    
    /**
     * Obtiene todas las atenciones médicas
     * @return Lista de todas las atenciones médicas
     * @throws SQLException si hay error en la operación
     */
    @Override
    public List<AtencionMedica> obtenerTodos() throws SQLException {
        return ejecutarConsulta(SQL_OBTENER_TODOS);
    }
    
    /**
     * Obtiene todas las atenciones médicas de un paciente
     * @param pacienteId ID del paciente
     * @return Lista de atenciones del paciente
     * @throws SQLException si hay error en la operación
     */
    public List<AtencionMedica> obtenerPorPaciente(int pacienteId) throws SQLException {
        if (pacienteId <= 0) {
            throw new IllegalArgumentException("ID de paciente inválido");
        }
        
        return ejecutarConsulta(SQL_BUSCAR_POR_PACIENTE, pacienteId);
    }
    
    /**
     * Obtiene todas las atenciones realizadas por un médico
     * @param medicoId ID del médico
     * @return Lista de atenciones del médico
     * @throws SQLException si hay error en la operación
     */
    public List<AtencionMedica> obtenerPorMedico(int medicoId) throws SQLException {
        if (medicoId <= 0) {
            throw new IllegalArgumentException("ID de médico inválido");
        }
        
        return ejecutarConsulta(SQL_BUSCAR_POR_MEDICO, medicoId);
    }
    
    /**
     * Obtiene atenciones por especialidad médica
     * @param especialidad Especialidad médica a buscar
     * @return Lista de atenciones de la especialidad
     * @throws SQLException si hay error en la operación
     */
    public List<AtencionMedica> obtenerPorEspecialidad(Especialidad especialidad) throws SQLException {
        if (especialidad == null) {
            throw new IllegalArgumentException("Especialidad no puede ser nula");
        }
        
        return ejecutarConsulta(SQL_BUSCAR_POR_ESPECIALIDAD, especialidad.name());
    }
    
    /**
     * Obtiene pacientes que requieren hospitalización
     * @return Lista de atenciones que requieren hospitalización
     * @throws SQLException si hay error en la operación
     */
    public List<AtencionMedica> obtenerQueRequierenHospitalizacion() throws SQLException {
        return ejecutarConsulta(SQL_BUSCAR_REQUIEREN_HOSPITALIZACION);
    }
    
    /**
     * Obtiene pacientes que requieren cirugía
     * @return Lista de atenciones que requieren cirugía
     * @throws SQLException si hay error en la operación
     */
    public List<AtencionMedica> obtenerQueRequierenCirugia() throws SQLException {
        return ejecutarConsulta(SQL_BUSCAR_REQUIEREN_CIRUGIA);
    }
    
    /**
     * Obtiene pacientes que requieren interconsulta
     * @return Lista de atenciones que requieren interconsulta
     * @throws SQLException si hay error en la operación
     */
    public List<AtencionMedica> obtenerQueRequierenInterconsulta() throws SQLException {
        return ejecutarConsulta(SQL_BUSCAR_REQUIEREN_INTERCONSULTA);
    }
    
    /**
     * Obtiene atenciones médicas de una fecha específica
     * @param fecha Fecha a buscar
     * @return Lista de atenciones de esa fecha
     * @throws SQLException si hay error en la operación
     */
    public List<AtencionMedica> obtenerPorFecha(LocalDateTime fecha) throws SQLException {
        if (fecha == null) {
            throw new IllegalArgumentException("Fecha no puede ser nula");
        }
        
        return ejecutarConsulta(SQL_BUSCAR_POR_FECHA, convertirADate(fecha.toLocalDate()));
    }
    
    /**
     * Obtiene la última atención médica de un paciente
     * @param pacienteId ID del paciente
     * @return Última atención o null si no tiene atenciones
     * @throws SQLException si hay error en la operación
     */
    public AtencionMedica obtenerUltimaPorPaciente(int pacienteId) throws SQLException {
        if (pacienteId <= 0) {
            throw new IllegalArgumentException("ID de paciente inválido");
        }
        
        String sql = SQL_BUSCAR_POR_PACIENTE + " LIMIT 1";
        return ejecutarConsultaUnica(sql, pacienteId);
    }
    
    /**
     * Cuenta atenciones por especialidad
     * @return Lista de conteos por especialidad
     * @throws SQLException si hay error en la operación
     */
    public List<ConteoEspecialidad> contarPorEspecialidad() throws SQLException {
        String sql = "SELECT especialidad_medica, COUNT(*) as cantidad " +
                    "FROM " + TABLA + " " +
                    "GROUP BY especialidad_medica";
        
        List<ConteoEspecialidad> conteos = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                ConteoEspecialidad conteo = new ConteoEspecialidad();
                conteo.especialidad = Especialidad.valueOf(rs.getString("especialidad_medica"));
                conteo.cantidad = rs.getInt("cantidad");
                conteos.add(conteo);
            }
            
        } finally {
            cerrarRecursos(rs, stmt, conn);
        }
        
        return conteos;
    }
    
    /**
     * Obtiene estadísticas de requerimientos médicos
     * @return Estadísticas de hospitalizaciones, cirugías e interconsultas
     * @throws SQLException si hay error en la operación
     */
    public EstadisticasRequerimientos obtenerEstadisticasRequerimientos() throws SQLException {
        String sql = "SELECT " +
                    "SUM(CASE WHEN requiere_hospitalizacion = 1 THEN 1 ELSE 0 END) as hospitalizaciones, " +
                    "SUM(CASE WHEN requiere_cirugia = 1 THEN 1 ELSE 0 END) as cirugias, " +
                    "SUM(CASE WHEN requiere_interconsulta = 1 THEN 1 ELSE 0 END) as interconsultas, " +
                    "COUNT(*) as total_atenciones " +
                    "FROM " + TABLA;
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                EstadisticasRequerimientos stats = new EstadisticasRequerimientos();
                stats.hospitalizaciones = rs.getInt("hospitalizaciones");
                stats.cirugias = rs.getInt("cirugias");
                stats.interconsultas = rs.getInt("interconsultas");
                stats.totalAtenciones = rs.getInt("total_atenciones");
                return stats;
            }
            
            return null;
            
        } finally {
            cerrarRecursos(rs, stmt, conn);
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto AtencionMedica
     * @param rs ResultSet con los datos
     * @return AtencionMedica mapeado
     * @throws SQLException si hay error en el mapeo
     */
    @Override
    protected AtencionMedica mapearResultSet(ResultSet rs) throws SQLException {
        AtencionMedica atencion = new AtencionMedica();
        
        atencion.setId(rs.getInt("id"));
        atencion.setPacienteId(rs.getInt("paciente_id"));
        atencion.setMedicoId(rs.getInt("medico_id"));
        
        // Conversión de fechas
        Timestamp fechaConsulta = rs.getTimestamp("fecha_consulta");
        if (fechaConsulta != null) {
            atencion.setFechaConsulta(fechaConsulta.toLocalDateTime());
        }
        
        String especialidadMedica = rs.getString("especialidad_medica");
        if (especialidadMedica != null) {
            atencion.setEspecialidadMedica(Especialidad.valueOf(especialidadMedica));
        }
        
        atencion.setMotivoConsulta(rs.getString("motivo_consulta"));
        atencion.setExploracionFisica(rs.getString("exploracion_fisica"));
        atencion.setDiagnostico(rs.getString("diagnostico"));
        atencion.setTratamientoPrescrito(rs.getString("tratamiento_prescrito"));
        atencion.setMedicamentosPrescritos(rs.getString("medicamentos_prescritos"));
        atencion.setObservacionesMedicas(rs.getString("observaciones_medicas"));
        
        Timestamp proximaCita = rs.getTimestamp("proxima_cita");
        if (proximaCita != null) {
            atencion.setProximaCita(proximaCita.toLocalDateTime());
        }
        
        atencion.setRequiereHospitalizacion(rs.getBoolean("requiere_hospitalizacion"));
        atencion.setRequiereCirugia(rs.getBoolean("requiere_cirugia"));
        atencion.setRequiereInterconsulta(rs.getBoolean("requiere_interconsulta"));
        
        String especialidadInterconsulta = rs.getString("especialidad_interconsulta");
        if (especialidadInterconsulta != null) {
            atencion.setEspecialidadInterconsulta(Especialidad.valueOf(especialidadInterconsulta));
        }
        
        // Campos adicionales de los JOINs (si están disponibles)
        try {
            atencion.setPacienteNombre(rs.getString("paciente_nombre"));
            atencion.setNumeroExpediente(rs.getString("numero_expediente"));
            atencion.setMedicoNombre(rs.getString("medico_nombre"));
        } catch (SQLException e) {
            // Los campos no están disponibles en esta consulta
        }
        
        return atencion;
    }
    
    /**
     * Valida los datos de atención médica antes de insertarlos/actualizarlos
     * @param atencion Atención médica a validar
     * @throws IllegalArgumentException si los datos no son válidos
     */
    private void validarAtencionMedica(AtencionMedica atencion) {
        if (atencion == null) {
            throw new IllegalArgumentException("Atención médica no puede ser nula");
        }
        
        if (atencion.getPacienteId() <= 0) {
            throw new IllegalArgumentException("ID de paciente es obligatorio");
        }
        
        if (atencion.getMedicoId() <= 0) {
            throw new IllegalArgumentException("ID de médico es obligatorio");
        }
        
        if (atencion.getFechaConsulta() == null) {
            atencion.setFechaConsulta(LocalDateTime.now());
        }
        
        if (atencion.getEspecialidadMedica() == null) {
            throw new IllegalArgumentException("Especialidad médica es obligatoria");
        }
        
        if (!ValidationUtils.validarTexto(atencion.getMotivoConsulta(), 5, 500)) {
            throw new IllegalArgumentException("Motivo de consulta debe tener entre 5 y 500 caracteres");
        }
        
        if (atencion.getExploracionFisica() != null && !atencion.getExploracionFisica().isEmpty()) {
            if (!ValidationUtils.validarTexto(atencion.getExploracionFisica(), 5, 1000)) {
                throw new IllegalArgumentException("Exploración física debe tener entre 5 y 1000 caracteres");
            }
        }
        
        if (atencion.getDiagnostico() != null && !atencion.getDiagnostico().isEmpty()) {
            if (!ValidationUtils.validarTexto(atencion.getDiagnostico(), 5, 500)) {
                throw new IllegalArgumentException("Diagnóstico debe tener entre 5 y 500 caracteres");
            }
        }
        
        if (atencion.getTratamientoPrescrito() != null && !atencion.getTratamientoPrescrito().isEmpty()) {
            if (!ValidationUtils.validarTexto(atencion.getTratamientoPrescrito(), 5, 1000)) {
                throw new IllegalArgumentException("Tratamiento prescrito debe tener entre 5 y 1000 caracteres");
            }
        }
        
        if (atencion.getMedicamentosPrescritos() != null && !atencion.getMedicamentosPrescritos().isEmpty()) {
            if (!ValidationUtils.validarTexto(atencion.getMedicamentosPrescritos(), 5, 1000)) {
                throw new IllegalArgumentException("Medicamentos prescritos deben tener entre 5 y 1000 caracteres");
            }
        }
        
        if (atencion.getObservacionesMedicas() != null && !atencion.getObservacionesMedicas().isEmpty()) {
            if (!ValidationUtils.validarTexto(atencion.getObservacionesMedicas(), 5, 1000)) {
                throw new IllegalArgumentException("Observaciones médicas deben tener entre 5 y 1000 caracteres");
            }
        }
        
        // Si requiere interconsulta, debe especificar la especialidad
        if (atencion.isRequiereInterconsulta()) {
            if (atencion.getEspecialidadInterconsulta() == null) {
                throw new IllegalArgumentException(
                    "Si requiere interconsulta, debe especificar la especialidad");
            }
        }
    }
    
    /**
     * Contar atenciones de hoy
     */
    public int contarAtencionesHoy() {
        String sql = "SELECT COUNT(*) FROM " + TABLA + " WHERE DATE(fecha_consulta) = CURDATE()";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar atenciones de hoy", e);
        }
    }

    /**
     * Contar atenciones de la semana
     */
    public int contarAtencionesSemana() {
        String sql = "SELECT COUNT(*) FROM " + TABLA + 
                    " WHERE YEARWEEK(fecha_consulta, 1) = YEARWEEK(CURDATE(), 1)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar atenciones de la semana", e);
        }
    }

    /**
     * Contar consultas en progreso
     */
    public int contarConsultasEnProgreso() {
        String sql = "SELECT COUNT(*) FROM " + TABLA + " WHERE DATE(fecha_consulta) = CURDATE() AND proxima_cita IS NOT NULL";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar consultas en progreso", e);
        }
    }

    /**
     * Contar atenciones por rango de fecha
     */
    public int contarAtenciones(java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin) {
        String sql = "SELECT COUNT(*) FROM " + TABLA + " WHERE DATE(fecha_consulta) BETWEEN ? AND ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(fechaInicio));
            stmt.setDate(2, java.sql.Date.valueOf(fechaFin));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar atenciones", e);
        }
    }

    /**
     * Obtener estadísticas por médico
     */
    public List<EstadisticasMedico> obtenerEstadisticasPorMedico(java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin) {
        String sql = "SELECT medico_id, COUNT(*) as total_consultas " +
                    "FROM " + TABLA + " WHERE DATE(fecha_consulta) BETWEEN ? AND ? GROUP BY medico_id";
        List<EstadisticasMedico> estadisticas = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(fechaInicio));
            stmt.setDate(2, java.sql.Date.valueOf(fechaFin));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    EstadisticasMedico est = new EstadisticasMedico();
                    est.medicoId = rs.getInt("medico_id");
                    est.totalConsultas = rs.getInt("total_consultas");
                    estadisticas.add(est);
                }
            }
            
            return estadisticas;
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener estadísticas por médico", e);
        }
    }

    /**
     * Clase interna para estadísticas de médico
     */
    public static class EstadisticasMedico {
        public int medicoId;
        public String nombreMedico;
        public int totalConsultas;
        public int hospitalizaciones;
        public int cirugias;
        public int interconsultas;
        
        // Getters para compatibilidad
        public String getMedicoNombre() {
            return nombreMedico;
        }
        
        public int getTotalConsultas() {
            return totalConsultas;
        }
        
        @Override
        public String toString() {
            return String.format("Médico: %s (%d), Consultas: %d", nombreMedico, medicoId, totalConsultas);
        }
    }

    /**
     * Clase interna para conteo de atenciones por especialidad
     */
    public static class ConteoEspecialidad {
        public Especialidad especialidad;
        public int cantidad;
        
        @Override
        public String toString() {
            return String.format("Especialidad: %s, Cantidad: %d", especialidad, cantidad);
        }
    }
    
    /**
     * Clase interna para estadísticas de requerimientos
     */
    public static class EstadisticasRequerimientos {
        public int hospitalizaciones;
        public int cirugias;
        public int interconsultas;
        public int totalAtenciones;
        
        @Override
        public String toString() {
            return String.format("Total: %d, Hospitalizaciones: %d, Cirugías: %d, Interconsultas: %d",
                               totalAtenciones, hospitalizaciones, cirugias, interconsultas);
        }
    }
}