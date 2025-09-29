package dao;

import models.DatosSociales;
import utils.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad DatosSociales
 * Maneja todas las operaciones CRUD con la tabla datos_sociales
 */
public class DatosSocialesDAO extends BaseDAO<DatosSociales> {
    
    private final DatabaseConnection dbConnection;
    
    public DatosSocialesDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Inserta nuevos datos sociales en la base de datos
     */
    @Override
    public boolean insertar(DatosSociales datos) throws SQLException {
        String sql = """
            INSERT INTO datos_sociales (
                registro_triage_id, trabajador_social_id, fecha_hora_entrevista,
                antecedentes_familiares, enfermedades_cronicas, medicamentos_actuales,
                alergias_conocidas, cirugias_previas, hospitalizaciones_previas,
                vacunas_recientes, habitos_toxicos, situacion_socioeconomica,
                observaciones_adicionales
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        return dbConnection.executeUpdate(sql,
            datos.getRegistroTriageId(),
            datos.getTrabajadorSocialId(),
            datos.getFechaHoraEntrevista(),
            datos.getAntecedentesFamiliares(),
            datos.getEnfermedadesCronicas(),
            datos.getMedicamentosActuales(),
            datos.getAlergiasConocidas(),
            datos.getCirugiasPrevias(),
            datos.getHospitalizacionesPrevias(),
            datos.getVacunasRecientes(),
            datos.getHabitosToxicos(),
            datos.getSituacionSocioeconomica(),
            datos.getObservacionesAdicionales()
        ) > 0;
    }
    
    /**
     * Actualiza datos sociales existentes
     */
    @Override
    public boolean actualizar(DatosSociales datos) throws SQLException {
        String sql = """
            UPDATE datos_sociales SET
                antecedentes_familiares = ?, enfermedades_cronicas = ?, 
                medicamentos_actuales = ?, alergias_conocidas = ?,
                cirugias_previas = ?, hospitalizaciones_previas = ?,
                vacunas_recientes = ?, habitos_toxicos = ?,
                situacion_socioeconomica = ?, observaciones_adicionales = ?
            WHERE id = ?
            """;
        
        return dbConnection.executeUpdate(sql,
            datos.getAntecedentesFamiliares(),
            datos.getEnfermedadesCronicas(),
            datos.getMedicamentosActuales(),
            datos.getAlergiasConocidas(),
            datos.getCirugiasPrevias(),
            datos.getHospitalizacionesPrevias(),
            datos.getVacunasRecientes(),
            datos.getHabitosToxicos(),
            datos.getSituacionSocioeconomica(),
            datos.getObservacionesAdicionales(),
            datos.getId()
        ) > 0;
    }
    
    /**
     * Elimina datos sociales por ID
     */
    @Override
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM datos_sociales WHERE id = ?";
        return dbConnection.executeUpdate(sql, id) > 0;
    }
    
    /**
     * Obtiene datos sociales por ID
     */
    @Override
    public DatosSociales buscarPorId(int id) throws SQLException {
        String sql = """
            SELECT ds.*, rt.paciente_id, rt.motivo_consulta, rt.nivel_urgencia,
                   u.nombre_completo as trabajador_nombre
            FROM datos_sociales ds
            LEFT JOIN registros_triage rt ON ds.registro_triage_id = rt.id
            LEFT JOIN usuarios u ON ds.trabajador_social_id = u.id
            WHERE ds.id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Obtiene todos los datos sociales
     */
    @Override
    public List<DatosSociales> obtenerTodos() throws SQLException {
        String sql = """
            SELECT ds.*, rt.paciente_id, rt.motivo_consulta, rt.nivel_urgencia,
                   u.nombre_completo as trabajador_nombre
            FROM datos_sociales ds
            LEFT JOIN registros_triage rt ON ds.registro_triage_id = rt.id
            LEFT JOIN usuarios u ON ds.trabajador_social_id = u.id
            ORDER BY ds.fecha_hora_entrevista DESC
            """;
        
        List<DatosSociales> lista = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        }
        
        return lista;
    }
    
    /**
     * Obtiene datos sociales por ID de registro de triage
     */
    public DatosSociales obtenerPorRegistroTriage(int registroTriageId) throws SQLException {
        String sql = """
            SELECT ds.*, rt.paciente_id, rt.motivo_consulta, rt.nivel_urgencia,
                   u.nombre_completo as trabajador_nombre
            FROM datos_sociales ds
            LEFT JOIN registros_triage rt ON ds.registro_triage_id = rt.id
            LEFT JOIN usuarios u ON ds.trabajador_social_id = u.id
            WHERE ds.registro_triage_id = ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, registroTriageId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Obtiene todos los datos sociales de un paciente
     */
    public List<DatosSociales> obtenerPorPaciente(int pacienteId) throws SQLException {
        String sql = """
            SELECT ds.*, rt.paciente_id, rt.motivo_consulta, rt.nivel_urgencia,
                   u.nombre_completo as trabajador_nombre
            FROM datos_sociales ds
            INNER JOIN registros_triage rt ON ds.registro_triage_id = rt.id
            LEFT JOIN usuarios u ON ds.trabajador_social_id = u.id
            WHERE rt.paciente_id = ?
            ORDER BY ds.fecha_hora_entrevista DESC
            """;
        
        List<DatosSociales> lista = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, pacienteId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        }
        
        return lista;
    }
    
    /**
     * Obtiene datos sociales por trabajador social
     */
    public List<DatosSociales> obtenerPorTrabajadorSocial(int trabajadorSocialId) throws SQLException {
        String sql = """
            SELECT ds.*, rt.paciente_id, rt.motivo_consulta, rt.nivel_urgencia,
                   u.nombre_completo as trabajador_nombre
            FROM datos_sociales ds
            LEFT JOIN registros_triage rt ON ds.registro_triage_id = rt.id
            LEFT JOIN usuarios u ON ds.trabajador_social_id = u.id
            WHERE ds.trabajador_social_id = ?
            ORDER BY ds.fecha_hora_entrevista DESC
            """;
        
        List<DatosSociales> lista = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, trabajadorSocialId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        }
        
        return lista;
    }
    
    /**
     * Verifica si ya existen datos sociales para un registro de triage
     */
    public boolean existenDatos(int registroTriageId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM datos_sociales WHERE registro_triage_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, registroTriageId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Contar evaluaciones por rango de fecha
     */
    public int contarEvaluaciones(LocalDate fechaInicio, LocalDate fechaFin) {
        String sql = """
            SELECT COUNT(*) 
            FROM datos_sociales 
            WHERE DATE(fecha_hora_entrevista) BETWEEN ? AND ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(fechaInicio));
            stmt.setDate(2, Date.valueOf(fechaFin));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar evaluaciones sociales", e);
        }
        
        return 0;
    }
    
    /**
     * Mapea un ResultSet a un objeto DatosSociales
     */
    @Override
    protected DatosSociales mapearResultSet(ResultSet rs) throws SQLException {
        DatosSociales datos = new DatosSociales();
        
        // Campos básicos
        datos.setId(rs.getInt("id"));
        datos.setRegistroTriageId(rs.getInt("registro_triage_id"));
        datos.setTrabajadorSocialId(rs.getInt("trabajador_social_id"));
        
        Timestamp timestamp = rs.getTimestamp("fecha_hora_entrevista");
        if (timestamp != null) {
            datos.setFechaHoraEntrevista(timestamp.toLocalDateTime());
        }
        
        // Campos de información médica y social
        datos.setAntecedentesFamiliares(rs.getString("antecedentes_familiares"));
        datos.setEnfermedadesCronicas(rs.getString("enfermedades_cronicas"));
        datos.setMedicamentosActuales(rs.getString("medicamentos_actuales"));
        datos.setAlergiasConocidas(rs.getString("alergias_conocidas"));
        datos.setCirugiasPrevias(rs.getString("cirugias_previas"));
        datos.setHospitalizacionesPrevias(rs.getString("hospitalizaciones_previas"));
        datos.setVacunasRecientes(rs.getString("vacunas_recientes"));
        datos.setHabitosToxicos(rs.getString("habitos_toxicos"));
        datos.setSituacionSocioeconomica(rs.getString("situacion_socioeconomica"));
        datos.setObservacionesAdicionales(rs.getString("observaciones_adicionales"));
        
        return datos;
    }
    
    /**
     * Obtiene los últimos datos sociales registrados para un paciente
     */
    public DatosSociales obtenerUltimoPorPaciente(int pacienteId) {
        String sql = """
            SELECT ds.*, rt.id as registro_triage_id, rt.paciente_id, rt.medico_triage_id,
                   u.nombre as usuario_nombre, u.apellido_paterno as usuario_apellido
            FROM datos_sociales ds
            INNER JOIN registro_triage rt ON ds.registro_triage_id = rt.id
            LEFT JOIN usuarios u ON rt.medico_triage_id = u.id
            WHERE rt.paciente_id = ?
            ORDER BY ds.fecha_evaluacion DESC
            LIMIT 1
            """;
        
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, pacienteId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener último dato social del paciente: " + e.getMessage());
        }
        
        return null;
    }
}
