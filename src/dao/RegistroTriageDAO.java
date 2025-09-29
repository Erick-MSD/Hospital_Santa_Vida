package dao;

import models.RegistroTriage;
import models.NivelUrgencia;
import utils.ValidationUtils;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * DAO para la gestión de registros de triage en el sistema hospitalario
 * Maneja todas las operaciones CRUD para la tabla registros_triage
 * Incluye funcionalidades específicas de búsqueda por urgencia y fecha
 */
public class RegistroTriageDAO extends BaseDAO<RegistroTriage> {
    
    private static final String TABLA = "registros_triage";
    
    // Consultas SQL predefinidas
    private static final String SQL_INSERTAR = 
        "INSERT INTO " + TABLA + " (paciente_id, medico_triage_id, fecha_hora_triage, " +
        "motivo_consulta, sintomas_principales, signos_vitales_presion, " +
        "signos_vitales_pulso, signos_vitales_temperatura, signos_vitales_respiracion, " +
        "signos_vitales_saturacion, nivel_dolor, escala_glasgow, observaciones_triage, " +
        "nivel_urgencia, tiempo_estimado_atencion, prioridad_orden) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SQL_ACTUALIZAR = 
        "UPDATE " + TABLA + " SET paciente_id = ?, medico_triage_id = ?, fecha_hora_triage = ?, " +
        "motivo_consulta = ?, sintomas_principales = ?, signos_vitales_presion = ?, " +
        "signos_vitales_pulso = ?, signos_vitales_temperatura = ?, signos_vitales_respiracion = ?, " +
        "signos_vitales_saturacion = ?, nivel_dolor = ?, escala_glasgow = ?, " +
        "observaciones_triage = ?, nivel_urgencia = ?, tiempo_estimado_atencion = ?, " +
        "prioridad_orden = ? WHERE id = ?";
    
    private static final String SQL_ELIMINAR = 
        "DELETE FROM " + TABLA + " WHERE id = ?";
    
    private static final String SQL_BUSCAR_POR_ID = 
        "SELECT rt.*, CONCAT(p.nombre, ' ', p.apellido_paterno, ' ', IFNULL(p.apellido_materno, '')) as paciente_nombre, p.id as numero_expediente, " +
        "u.nombre_completo as usuario_nombre " +
        "FROM " + TABLA + " rt " +
        "JOIN pacientes p ON rt.paciente_id = p.id " +
        "JOIN usuarios u ON rt.medico_triage_id = u.id " +
        "WHERE rt.id = ?";
    
    private static final String SQL_OBTENER_TODOS = 
        "SELECT rt.*, CONCAT(p.nombre, ' ', p.apellido_paterno, ' ', IFNULL(p.apellido_materno, '')) as paciente_nombre, p.id as numero_expediente, " +
        "u.nombre_completo as usuario_nombre " +
        "FROM " + TABLA + " rt " +
        "JOIN pacientes p ON rt.paciente_id = p.id " +
        "JOIN usuarios u ON rt.medico_triage_id = u.id " +
        "ORDER BY rt.fecha_hora_triage DESC";
    
    private static final String SQL_BUSCAR_POR_PACIENTE = 
        "SELECT rt.*, CONCAT(p.nombre, ' ', p.apellido_paterno, ' ', IFNULL(p.apellido_materno, '')) as paciente_nombre, p.id as numero_expediente, " +
        "u.nombre_completo as usuario_nombre " +
        "FROM " + TABLA + " rt " +
        "JOIN pacientes p ON rt.paciente_id = p.id " +
        "JOIN usuarios u ON rt.medico_triage_id = u.id " +
        "WHERE rt.paciente_id = ? ORDER BY rt.fecha_hora_triage DESC";
    
    private static final String SQL_BUSCAR_POR_USUARIO = 
        "SELECT rt.*, CONCAT(p.nombre, ' ', p.apellido_paterno, ' ', IFNULL(p.apellido_materno, '')) as paciente_nombre, p.id as numero_expediente, " +
        "u.nombre_completo as usuario_nombre " +
        "FROM " + TABLA + " rt " +
        "JOIN pacientes p ON rt.paciente_id = p.id " +
        "JOIN usuarios u ON rt.medico_triage_id = u.id " +
        "WHERE rt.medico_triage_id = ? ORDER BY rt.fecha_hora_triage DESC";
    
    private static final String SQL_BUSCAR_POR_NIVEL_URGENCIA = 
        "SELECT rt.*, CONCAT(p.nombre, ' ', p.apellido_paterno, ' ', IFNULL(p.apellido_materno, '')) as paciente_nombre, p.id as numero_expediente, " +
        "u.nombre_completo as usuario_nombre " +
        "FROM " + TABLA + " rt " +
        "JOIN pacientes p ON rt.paciente_id = p.id " +
        "JOIN usuarios u ON rt.medico_triage_id = u.id " +
        "WHERE rt.nivel_urgencia = ? ORDER BY rt.prioridad_orden, rt.fecha_hora_triage";
    
    private static final String SQL_BUSCAR_POR_FECHA = 
        "SELECT rt.*, CONCAT(p.nombre, ' ', p.apellido_paterno, ' ', IFNULL(p.apellido_materno, '')) as paciente_nombre, p.id as numero_expediente, " +
        "u.nombre_completo as usuario_nombre " +
        "FROM " + TABLA + " rt " +
        "JOIN pacientes p ON rt.paciente_id = p.id " +
        "JOIN usuarios u ON rt.medico_triage_id = u.id " +
        "WHERE DATE(rt.fecha_hora_triage) = ? ORDER BY rt.prioridad_orden, rt.fecha_hora_triage";
    
    private static final String SQL_BUSCAR_POR_RANGO_FECHAS = 
        "SELECT rt.*, CONCAT(p.nombre, ' ', p.apellido_paterno, ' ', IFNULL(p.apellido_materno, '')) as paciente_nombre, p.id as numero_expediente, " +
        "u.nombre_completo as usuario_nombre " +
        "FROM " + TABLA + " rt " +
        "JOIN pacientes p ON rt.paciente_id = p.id " +
        "JOIN usuarios u ON rt.medico_triage_id = u.id " +
        "WHERE rt.fecha_hora_triage BETWEEN ? AND ? " +
        "ORDER BY rt.prioridad_orden, rt.fecha_hora_triage";
    
    private static final String SQL_OBTENER_PENDIENTES = 
        "SELECT rt.*, CONCAT(p.nombre, ' ', p.apellido_paterno, ' ', IFNULL(p.apellido_materno, '')) as paciente_nombre, p.id as numero_expediente, " +
        "u.nombre_completo as usuario_nombre " +
        "FROM " + TABLA + " rt " +
        "JOIN pacientes p ON rt.paciente_id = p.id " +
        "JOIN usuarios u ON rt.medico_triage_id = u.id " +
        "WHERE rt.estado IN ('ESPERANDO_MEDICO', 'EN_ATENCION') " +
        "ORDER BY rt.prioridad_orden, rt.fecha_hora_triage";
    
    private static final String SQL_OBTENER_URGENTES = 
        "SELECT rt.*, CONCAT(p.nombre, ' ', p.apellido_paterno, ' ', IFNULL(p.apellido_materno, '')) as paciente_nombre, p.id as numero_expediente, " +
        "u.nombre_completo as usuario_nombre " +
        "FROM " + TABLA + " rt " +
        "JOIN pacientes p ON rt.paciente_id = p.id " +
        "JOIN usuarios u ON rt.medico_triage_id = u.id " +
        "WHERE rt.nivel_urgencia IN ('EMERGENCIA', 'URGENTE') " +
        "AND rt.estado IN ('ESPERANDO_MEDICO', 'EN_ATENCION') " +
        "ORDER BY rt.prioridad_orden, rt.fecha_hora_triage";
    
    /**
     * Inserta un nuevo registro de triage en la base de datos
     * @param registro Registro de triage a insertar
     * @return true si se insertó correctamente
     * @throws SQLException si hay error en la operación
     */
    @Override
    public boolean insertar(RegistroTriage registro) throws SQLException {
        validarRegistro(registro);
        
        int idGenerado = ejecutarInsercionConClave(SQL_INSERTAR,
            registro.getPacienteId(),
            registro.getUsuarioTriageId(),
            convertirATimestamp(registro.getFechaTriage()),
            registro.getMotivoConsulta(),
            registro.getSintomasPrincipales(),
            registro.getSignosVitalesPresion(),
            registro.getSignosVitalesPulso(),
            registro.getSignosVitalesTemperatura(),
            registro.getSignosVitalesRespiracion(),
            registro.getSignosVitalesSaturacion(),
            registro.getNivelDolor(),
            registro.getEscalaGlasgow(),
            registro.getObservacionesTriage(),
            registro.getNivelUrgencia().name(),
            registro.getTiempoEstimadoAtencion(),
            registro.getPrioridadNumerica()
        );
        
        if (idGenerado > 0) {
            registro.setId(idGenerado);
            return true;
        }
        
        return false;
    }
    
    /**
     * Actualiza un registro de triage existente
     * @param registro Registro de triage a actualizar
     * @return true si se actualizó correctamente
     * @throws SQLException si hay error en la operación
     */
    @Override
    public boolean actualizar(RegistroTriage registro) throws SQLException {
        validarRegistro(registro);
        
        if (registro.getId() <= 0) {
            throw new IllegalArgumentException("ID de registro inválido");
        }
        
        int filasActualizadas = ejecutarActualizacion(SQL_ACTUALIZAR,
            registro.getPacienteId(),
            registro.getUsuarioTriageId(),
            convertirATimestamp(registro.getFechaTriage()),
            registro.getMotivoConsulta(),
            registro.getSintomasPrincipales(),
            registro.getSignosVitalesPresion(),
            registro.getSignosVitalesPulso(),
            registro.getSignosVitalesTemperatura(),
            registro.getSignosVitalesRespiracion(),
            registro.getSignosVitalesSaturacion(),
            registro.getNivelDolor(),
            registro.getEscalaGlasgow(),
            registro.getObservacionesTriage(),
            registro.getNivelUrgencia().name(),
            registro.getTiempoEstimadoAtencion(),
            registro.getPrioridadNumerica(),
            registro.getId()
        );
        
        return filasActualizadas > 0;
    }
    
    /**
     * Elimina un registro de triage por su ID
     * @param id ID del registro a eliminar
     * @return true si se eliminó correctamente
     * @throws SQLException si hay error en la operación
     */
    @Override
    public boolean eliminar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de registro inválido");
        }
        
        int filasEliminadas = ejecutarActualizacion(SQL_ELIMINAR, id);
        return filasEliminadas > 0;
    }
    
    /**
     * Busca un registro de triage por su ID
     * @param id ID del registro
     * @return Registro encontrado o null si no existe
     * @throws SQLException si hay error en la operación
     */
    @Override
    public RegistroTriage buscarPorId(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de registro inválido");
        }
        
        return ejecutarConsultaUnica(SQL_BUSCAR_POR_ID, id);
    }
    
    /**
     * Obtiene todos los registros de triage
     * @return Lista de todos los registros
     * @throws SQLException si hay error en la operación
     */
    @Override
    public List<RegistroTriage> obtenerTodos() throws SQLException {
        return ejecutarConsulta(SQL_OBTENER_TODOS);
    }
    
    /**
     * Obtiene todos los registros de triage de un paciente
     * @param pacienteId ID del paciente
     * @return Lista de registros del paciente
     * @throws SQLException si hay error en la operación
     */
    public List<RegistroTriage> obtenerPorPaciente(int pacienteId) throws SQLException {
        if (pacienteId <= 0) {
            throw new IllegalArgumentException("ID de paciente inválido");
        }
        
        return ejecutarConsulta(SQL_BUSCAR_POR_PACIENTE, pacienteId);
    }
    
    /**
     * Obtiene todos los registros realizados por un usuario
     * @param usuarioId ID del usuario de triage
     * @return Lista de registros del usuario
     * @throws SQLException si hay error en la operación
     */
    public List<RegistroTriage> obtenerPorUsuario(int usuarioId) throws SQLException {
        if (usuarioId <= 0) {
            throw new IllegalArgumentException("ID de usuario inválido");
        }
        
        return ejecutarConsulta(SQL_BUSCAR_POR_USUARIO, usuarioId);
    }
    
    /**
     * Obtiene registros por nivel de urgencia
     * @param nivelUrgencia Nivel de urgencia a buscar
     * @return Lista de registros con el nivel especificado
     * @throws SQLException si hay error en la operación
     */
    public List<RegistroTriage> obtenerPorNivelUrgencia(NivelUrgencia nivelUrgencia) throws SQLException {
        if (nivelUrgencia == null) {
            throw new IllegalArgumentException("Nivel de urgencia no puede ser nulo");
        }
        
        return ejecutarConsulta(SQL_BUSCAR_POR_NIVEL_URGENCIA, nivelUrgencia.name());
    }
    
    /**
     * Obtiene registros de una fecha específica
     * @param fecha Fecha a buscar
     * @return Lista de registros de esa fecha
     * @throws SQLException si hay error en la operación
     */
    public List<RegistroTriage> obtenerPorFecha(LocalDateTime fecha) throws SQLException {
        if (fecha == null) {
            throw new IllegalArgumentException("Fecha no puede ser nula");
        }
        
        return ejecutarConsulta(SQL_BUSCAR_POR_FECHA, convertirADate(fecha.toLocalDate()));
    }
    
    /**
     * Obtiene registros en un rango de fechas
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de registros en el rango
     * @throws SQLException si hay error en la operación
     */
    public List<RegistroTriage> obtenerPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) 
            throws SQLException {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }
        
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin");
        }
        
        return ejecutarConsulta(SQL_BUSCAR_POR_RANGO_FECHAS,
            convertirATimestamp(fechaInicio), convertirATimestamp(fechaFin));
    }
    
    /**
     * Obtiene registros pendientes de atención (pacientes en triage o esperando)
     * @return Lista de registros pendientes ordenados por prioridad
     * @throws SQLException si hay error en la operación
     */
    public List<RegistroTriage> obtenerPendientes() throws SQLException {
        return ejecutarConsulta(SQL_OBTENER_PENDIENTES);
    }
    
    /**
     * Obtiene registros urgentes pendientes de atención
     * @return Lista de registros urgentes ordenados por prioridad
     * @throws SQLException si hay error en la operación
     */
    public List<RegistroTriage> obtenerUrgentes() throws SQLException {
        return ejecutarConsulta(SQL_OBTENER_URGENTES);
    }
    
    /**
     * Obtiene el último registro de triage de un paciente
     * @param pacienteId ID del paciente
     * @return Último registro o null si no tiene registros
     * @throws SQLException si hay error en la operación
     */
    public RegistroTriage obtenerUltimoPorPaciente(int pacienteId) throws SQLException {
        if (pacienteId <= 0) {
            throw new IllegalArgumentException("ID de paciente inválido");
        }
        
        String sql = SQL_BUSCAR_POR_PACIENTE + " LIMIT 1";
        return ejecutarConsultaUnica(sql, pacienteId);
    }
    
    /**
     * Cuenta los registros por nivel de urgencia en una fecha específica
     * @param fecha Fecha a consultar
     * @return Lista de conteos por nivel de urgencia
     * @throws SQLException si hay error en la operación
     */
    public List<ConteoUrgencia> contarPorUrgenciaEnFecha(LocalDateTime fecha) throws SQLException {
        if (fecha == null) {
            throw new IllegalArgumentException("Fecha no puede ser nula");
        }
        
        String sql = "SELECT nivel_urgencia, COUNT(*) as cantidad " +
                    "FROM " + TABLA + " " +
                    "WHERE DATE(fecha_hora_triage) = ? " +
                    "GROUP BY nivel_urgencia";
        
        List<ConteoUrgencia> conteos = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, convertirADate(fecha.toLocalDate()));
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                ConteoUrgencia conteo = new ConteoUrgencia();
                conteo.nivel = NivelUrgencia.valueOf(rs.getString("nivel_urgencia"));
                conteo.cantidad = rs.getInt("cantidad");
                conteos.add(conteo);
            }
            
        } finally {
            cerrarRecursos(rs, stmt, conn);
        }
        
        return conteos;
    }
    
    /**
     * Obtiene estadísticas de tiempo promedio por nivel de urgencia
     * @return Lista de estadísticas de tiempo
     * @throws SQLException si hay error en la operación
     */
    public List<EstadisticaTiempo> obtenerEstadisticasTiempo() throws SQLException {
        String sql = "SELECT nivel_urgencia, " +
                    "AVG(tiempo_estimado_atencion) as tiempo_promedio, " +
                    "MIN(tiempo_estimado_atencion) as tiempo_minimo, " +
                    "MAX(tiempo_estimado_atencion) as tiempo_maximo, " +
                    "COUNT(*) as total_casos " +
                    "FROM " + TABLA + " " +
                    "GROUP BY nivel_urgencia";
        
        List<EstadisticaTiempo> estadisticas = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                EstadisticaTiempo estadistica = new EstadisticaTiempo();
                estadistica.nivel = NivelUrgencia.valueOf(rs.getString("nivel_urgencia"));
                estadistica.tiempoPromedio = rs.getInt("tiempo_promedio");
                estadistica.tiempoMinimo = rs.getInt("tiempo_minimo");
                estadistica.tiempoMaximo = rs.getInt("tiempo_maximo");
                estadistica.totalCasos = rs.getInt("total_casos");
                estadisticas.add(estadistica);
            }
            
        } finally {
            cerrarRecursos(rs, stmt, conn);
        }
        
        return estadisticas;
    }
    
    /**
     * Mapea un ResultSet a un objeto RegistroTriage
     * @param rs ResultSet con los datos del registro
     * @return RegistroTriage mapeado
     * @throws SQLException si hay error en el mapeo
     */
    @Override
    protected RegistroTriage mapearResultSet(ResultSet rs) throws SQLException {
        RegistroTriage registro = new RegistroTriage();
        
        registro.setId(rs.getInt("id"));
        registro.setPacienteId(rs.getInt("paciente_id"));
        registro.setUsuarioTriageId(rs.getInt("medico_triage_id"));
        
        // Conversión de fecha
        Timestamp fechaTriage = rs.getTimestamp("fecha_hora_triage");
        if (fechaTriage != null) {
            registro.setFechaTriage(fechaTriage.toLocalDateTime());
        }
        
        registro.setMotivoConsulta(rs.getString("motivo_consulta"));
        registro.setSintomasPrincipales(rs.getString("sintomas_principales"));
        registro.setSignosVitalesPresion(rs.getString("signos_vitales_presion"));
        registro.setSignosVitalesPulso(rs.getInt("signos_vitales_pulso"));
        registro.setSignosVitalesTemperatura(rs.getDouble("signos_vitales_temperatura"));
        registro.setSignosVitalesRespiracion(rs.getInt("signos_vitales_respiracion"));
        registro.setSignosVitalesSaturacion(rs.getInt("signos_vitales_saturacion"));
        registro.setNivelDolor(rs.getInt("nivel_dolor"));
        registro.setEscalaGlasgow(rs.getInt("escala_glasgow"));
        registro.setObservacionesTriage(rs.getString("observaciones_triage"));
        
        String nivelUrgencia = rs.getString("nivel_urgencia");
        if (nivelUrgencia != null) {
            registro.setNivelUrgencia(NivelUrgencia.valueOf(nivelUrgencia));
        }
        
        registro.setTiempoEstimadoAtencion(rs.getInt("tiempo_estimado_atencion"));
        registro.setPrioridadNumerica(rs.getInt("prioridad_orden"));
        
        // Campos adicionales de los JOINs (si están disponibles)
        try {
            registro.setPacienteNombre(rs.getString("paciente_nombre"));
            registro.setNumeroExpediente(rs.getString("numero_expediente"));
            registro.setUsuarioNombre(rs.getString("usuario_nombre"));
        } catch (SQLException e) {
            // Los campos no están disponibles en esta consulta
        }
        
        return registro;
    }
    
    /**
     * Valida los datos de un registro de triage antes de insertarlo/actualizarlo
     * @param registro Registro a validar
     * @throws IllegalArgumentException si los datos no son válidos
     */
    private void validarRegistro(RegistroTriage registro) {
        if (registro == null) {
            throw new IllegalArgumentException("Registro no puede ser nulo");
        }
        
        if (registro.getPacienteId() <= 0) {
            throw new IllegalArgumentException("ID de paciente es obligatorio");
        }
        
        if (registro.getUsuarioTriageId() <= 0) {
            throw new IllegalArgumentException("ID de usuario de triage es obligatorio");
        }
        
        if (registro.getFechaTriage() == null) {
            registro.setFechaTriage(LocalDateTime.now());
        }
        
        if (!ValidationUtils.validarTexto(registro.getMotivoConsulta(), 5, 500)) {
            throw new IllegalArgumentException("Motivo de consulta debe tener entre 5 y 500 caracteres");
        }
        
        if (!ValidationUtils.validarTexto(registro.getSintomasPrincipales(), 5, 500)) {
            throw new IllegalArgumentException("Síntomas principales deben tener entre 5 y 500 caracteres");
        }
        
        // Validar signos vitales
        if (registro.getSignosVitalesPresion() != null && !registro.getSignosVitalesPresion().isEmpty()) {
            if (!ValidationUtils.validarPresionArterial(registro.getSignosVitalesPresion())) {
                throw new IllegalArgumentException("Presión arterial no es válida");
            }
        }
        
        if (registro.getSignosVitalesPulso() > 0) {
            if (!ValidationUtils.validarFrecuenciaCardiacaBoolean(registro.getSignosVitalesPulso())) {
                throw new IllegalArgumentException("Pulso no es válido");
            }
        }
        
        if (registro.getSignosVitalesTemperatura() > 0) {
            if (!ValidationUtils.validarTemperaturaBoolean(registro.getSignosVitalesTemperatura())) {
                throw new IllegalArgumentException("Temperatura no es válida");
            }
        }
        
        if (registro.getSignosVitalesSaturacion() > 0) {
            if (!ValidationUtils.validarSaturacionOxigenoBoolean(registro.getSignosVitalesSaturacion())) {
                throw new IllegalArgumentException("Saturación de oxígeno no es válida");
            }
        }
        
        if (registro.getEscalaGlasgow() > 0) {
            if (!ValidationUtils.validarEscalaGlasgow(registro.getEscalaGlasgow())) {
                throw new IllegalArgumentException("Escala de Glasgow no es válida");
            }
        }
        
        if (registro.getNivelUrgencia() == null) {
            throw new IllegalArgumentException("Nivel de urgencia es obligatorio");
        }
        
        if (registro.getTiempoEstimadoAtencion() <= 0) {
            throw new IllegalArgumentException("Tiempo estimado de atención debe ser mayor a cero");
        }
        
        if (registro.getPrioridadNumerica() <= 0) {
            throw new IllegalArgumentException("Prioridad numérica debe ser mayor a cero");
        }
    }
    
    /**
     * Clase interna para conteo de registros por urgencia
     */
    public static class ConteoUrgencia {
        public NivelUrgencia nivel;
        public int cantidad;
        
        @Override
        public String toString() {
            return String.format("Nivel: %s, Cantidad: %d", nivel, cantidad);
        }
    }
    
    /**
     * Cuenta los registros de triage de hoy
     */
    public int contarRegistrosHoy() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + TABLA + " WHERE DATE(fecha_hora_triage) = CURDATE()";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
    
    /**
     * Cuenta los pacientes evaluados hoy
     */
    public int contarEvaluadosHoy() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + TABLA + " WHERE DATE(fecha_hora_triage) = CURDATE() AND nivel_urgencia IS NOT NULL";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
    
    /**
     * Contar por urgencia (sin parámetros)
     */
    public java.util.Map<NivelUrgencia, Integer> contarPorUrgencia() {
        String sql = "SELECT nivel_urgencia, COUNT(*) FROM " + TABLA + " GROUP BY nivel_urgencia";
        java.util.Map<NivelUrgencia, Integer> resultado = new java.util.HashMap<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                NivelUrgencia nivel = NivelUrgencia.valueOf(rs.getString("nivel_urgencia"));
                int cantidad = rs.getInt(2);
                resultado.put(nivel, cantidad);
            }
            
            return resultado;
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar por urgencia", e);
        }
    }

    /**
     * Contar triage de hoy
     */
    public int contarTriageHoy() {
        String sql = "SELECT COUNT(*) FROM " + TABLA + " WHERE DATE(fecha_hora_triage) = CURDATE()";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar triage de hoy", e);
        }
    }

    /**
     * Contar pacientes en espera
     */
    public int contarPacientesEnEspera() {
        String sql = "SELECT COUNT(*) FROM " + TABLA + " WHERE DATE(fecha_hora_triage) = CURDATE() AND nivel_urgencia IS NOT NULL";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar pacientes en espera", e);
        }
    }

    /**
     * Contar evaluaciones por rango de fecha
     */
    public int contarEvaluaciones(java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin) {
        String sql = "SELECT COUNT(*) FROM " + TABLA + " WHERE DATE(fecha_hora_triage) BETWEEN ? AND ?";
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
            throw new RuntimeException("Error al contar evaluaciones", e);
        }
    }

    /**
     * Contar por urgencia con rango de fecha
     */
    public java.util.Map<NivelUrgencia, Integer> contarPorUrgencia(java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin) {
        String sql = "SELECT nivel_urgencia, COUNT(*) FROM " + TABLA + " WHERE DATE(fecha_hora_triage) BETWEEN ? AND ? GROUP BY nivel_urgencia";
        java.util.Map<NivelUrgencia, Integer> resultado = new java.util.HashMap<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(fechaInicio));
            stmt.setDate(2, java.sql.Date.valueOf(fechaFin));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    NivelUrgencia nivel = NivelUrgencia.valueOf(rs.getString("nivel_urgencia"));
                    int cantidad = rs.getInt(2);
                    resultado.put(nivel, cantidad);
                }
            }
            
            return resultado;
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar por urgencia con fechas", e);
        }
    }

    /**
     * Calcular tiempo promedio por urgencia
     */
    public java.util.Map<NivelUrgencia, Double> calcularTiempoPromedioPorUrgencia(java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin) {
        String sql = "SELECT nivel_urgencia, AVG(tiempo_estimado_atencion) FROM " + TABLA + " WHERE DATE(fecha_hora_triage) BETWEEN ? AND ? GROUP BY nivel_urgencia";
        java.util.Map<NivelUrgencia, Double> resultado = new java.util.HashMap<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(fechaInicio));
            stmt.setDate(2, java.sql.Date.valueOf(fechaFin));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    NivelUrgencia nivel = NivelUrgencia.valueOf(rs.getString("nivel_urgencia"));
                    double promedio = rs.getDouble(2);
                    resultado.put(nivel, promedio);
                }
            }
            
            return resultado;
        } catch (SQLException e) {
            throw new RuntimeException("Error al calcular tiempo promedio", e);
        }
    }

    /**
     * Obtener distribución por día
     */
    public java.util.Map<String, Integer> obtenerDistribucionPorDia(java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin) {
        String sql = "SELECT DATE(fecha_hora_triage) as dia, COUNT(*) FROM " + TABLA + " WHERE DATE(fecha_hora_triage) BETWEEN ? AND ? GROUP BY DATE(fecha_hora_triage)";
        java.util.Map<String, Integer> resultado = new java.util.HashMap<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(fechaInicio));
            stmt.setDate(2, java.sql.Date.valueOf(fechaFin));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String dia = rs.getString("dia");
                    int cantidad = rs.getInt(2);
                    resultado.put(dia, cantidad);
                }
            }
            
            return resultado;
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener distribución por día", e);
        }
    }

    /**
     * Clase interna para estadísticas de tiempo
     */
    public static class EstadisticaTiempo {
        public NivelUrgencia nivel;
        public int tiempoPromedio;
        public int tiempoMinimo;
        public int tiempoMaximo;
        public int totalCasos;
        
        @Override
        public String toString() {
            return String.format("Nivel: %s, Promedio: %d min, Min: %d min, Max: %d min, Casos: %d",
                               nivel, tiempoPromedio, tiempoMinimo, tiempoMaximo, totalCasos);
        }
    }
}
