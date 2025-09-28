package dao;

import models.DatosSociales;
import models.RegistroTriage;
import models.Usuario;
import utils.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad DatosSociales
 * Maneja todas las operaciones CRUD con la tabla datos_sociales
 */
public class DatosSocialesDAO {
    
    private final DatabaseConnection dbConnection;
    
    public DatosSocialesDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Inserta nuevos datos sociales en la base de datos
     */
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
     * Obtiene datos sociales por ID de registro de triage
     */
    public DatosSociales obtenerPorRegistroTriage(int registroTriageId) throws SQLException {
        String sql = """
            SELECT ds.*, rt.paciente_id, rt.motivo_consulta, rt.clasificacion,
                   u.nombre as trabajador_nombre, u.apellido_paterno as trabajador_apellido
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
                    return mapearResultadoADatosSociales(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Obtiene datos sociales por ID
     */
    public DatosSociales obtenerPorId(int id) throws SQLException {
        String sql = """
            SELECT ds.*, rt.paciente_id, rt.motivo_consulta, rt.clasificacion,
                   u.nombre as trabajador_nombre, u.apellido_paterno as trabajador_apellido
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
                    return mapearResultadoADatosSociales(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Actualiza datos sociales existentes
     */
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
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM datos_sociales WHERE id = ?";
        return dbConnection.executeUpdate(sql, id) > 0;
    }
    
    /**
     * Obtiene todos los datos sociales de un paciente
     */
    public List<DatosSociales> obtenerPorPaciente(int pacienteId) throws SQLException {
        String sql = """
            SELECT ds.*, rt.paciente_id, rt.motivo_consulta, rt.clasificacion,
                   u.nombre as trabajador_nombre, u.apellido_paterno as trabajador_apellido
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
                    lista.add(mapearResultadoADatosSociales(rs));
                }
            }
        }
        
        return lista;
    }
    
    /**
     * Obtiene datos sociales por trabajador social y rango de fechas
     */
    public List<DatosSociales> obtenerPorTrabajadorSocialYFecha(int trabajadorSocialId, 
                                                               LocalDateTime fechaInicio, 
                                                               LocalDateTime fechaFin) throws SQLException {
        String sql = """
            SELECT ds.*, rt.paciente_id, rt.motivo_consulta, rt.clasificacion,
                   u.nombre as trabajador_nombre, u.apellido_paterno as trabajador_apellido
            FROM datos_sociales ds
            LEFT JOIN registros_triage rt ON ds.registro_triage_id = rt.id
            LEFT JOIN usuarios u ON ds.trabajador_social_id = u.id
            WHERE ds.trabajador_social_id = ? 
            AND ds.fecha_hora_entrevista BETWEEN ? AND ?
            ORDER BY ds.fecha_hora_entrevista DESC
            """;
        
        List<DatosSociales> lista = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, trabajadorSocialId);
            stmt.setTimestamp(2, Timestamp.valueOf(fechaInicio));
            stmt.setTimestamp(3, Timestamp.valueOf(fechaFin));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultadoADatosSociales(rs));
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
     * Mapea un ResultSet a un objeto DatosSociales
     */
    private DatosSociales mapearResultadoADatosSociales(ResultSet rs) throws SQLException {
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
        
        // Información del registro de triage (si está disponible)
        try {
            int pacienteId = rs.getInt("paciente_id");
            if (pacienteId > 0) {
                RegistroTriage registro = new RegistroTriage();
                registro.setId(datos.getRegistroTriageId());
                registro.setMotivoConsulta(rs.getString("motivo_consulta"));
                // Nota: clasificacion se maneja internamente en RegistroTriage
                datos.setRegistroTriage(registro);
            }
        } catch (SQLException e) {
            // Los campos opcionales pueden no estar presentes, ignorar
        }
        
        // Información del trabajador social (si está disponible)
        try {
            String trabajadorNombre = rs.getString("trabajador_nombre");
            if (trabajadorNombre != null) {
                Usuario trabajador = new Usuario();
                trabajador.setId(datos.getTrabajadorSocialId());
                trabajador.setNombreCompleto(trabajadorNombre + " " + rs.getString("trabajador_apellido"));
                datos.setTrabajadorSocial(trabajador);
            }
        } catch (SQLException e) {
            // Los campos opcionales pueden no estar presentes, ignorar
        }
        
        return datos;
    }
    
    /**
     * Obtiene estadísticas de evaluaciones sociales por período
     */
    public int contarEvaluacionesPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws SQLException {
        String sql = """
            SELECT COUNT(*) 
            FROM datos_sociales 
            WHERE fecha_hora_entrevista BETWEEN ? AND ?
            """;
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(fechaInicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fechaFin));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
}