package dao;

import models.EvaluacionTriage;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operaciones de evaluaciones de triage
 */
public class EvaluacionTriageDAO {
    private DatabaseConnection dbConnection;

    public EvaluacionTriageDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Insertar nueva evaluación de triage
     */
    public boolean insertarEvaluacion(EvaluacionTriage evaluacion) {
        String sql = """
            INSERT INTO evaluaciones_triage (
                numero_folio, fecha_ingreso, motivo_consulta, presion_arterial,
                frecuencia_cardiaca, temperatura, frecuencia_respiratoria,
                saturacion_o2, nivel_dolor, observaciones_clinicas,
                nivel_triage, especialidad, doctor_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, evaluacion.getNumeroFolio());
            pstmt.setTimestamp(2, Timestamp.valueOf(evaluacion.getFechaIngreso()));
            pstmt.setString(3, evaluacion.getMotivoConsulta());
            pstmt.setString(4, evaluacion.getPresionArterial());
            
            if (evaluacion.getFrecuenciaCardiaca() != null) {
                pstmt.setInt(5, evaluacion.getFrecuenciaCardiaca());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            
            if (evaluacion.getTemperatura() != null) {
                pstmt.setDouble(6, evaluacion.getTemperatura());
            } else {
                pstmt.setNull(6, Types.DECIMAL);
            }
            
            if (evaluacion.getFrecuenciaRespiratoria() != null) {
                pstmt.setInt(7, evaluacion.getFrecuenciaRespiratoria());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }
            
            if (evaluacion.getSaturacionO2() != null) {
                pstmt.setInt(8, evaluacion.getSaturacionO2());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }
            
            if (evaluacion.getNivelDolor() != null) {
                pstmt.setInt(9, evaluacion.getNivelDolor());
            } else {
                pstmt.setNull(9, Types.INTEGER);
            }
            
            pstmt.setString(10, evaluacion.getObservacionesClinicas());
            pstmt.setString(11, evaluacion.getNivelTriage());
            pstmt.setString(12, evaluacion.getEspecialidad());
            pstmt.setInt(13, evaluacion.getDoctorId());

            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        evaluacion.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("✅ Evaluación de triage guardada exitosamente con ID: " + evaluacion.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al insertar evaluación de triage: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtener evaluación por número de folio
     */
    public EvaluacionTriage obtenerPorFolio(String numeroFolio) {
        String sql = "SELECT * FROM evaluaciones_triage WHERE numero_folio = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, numeroFolio);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToEvaluacion(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener evaluación: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Obtener todas las evaluaciones
     */
    public List<EvaluacionTriage> obtenerTodas() {
        List<EvaluacionTriage> evaluaciones = new ArrayList<>();
        String sql = "SELECT * FROM evaluaciones_triage ORDER BY fecha_creacion DESC";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                evaluaciones.add(mapResultSetToEvaluacion(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener evaluaciones: " + e.getMessage());
            e.printStackTrace();
        }
        return evaluaciones;
    }

    /**
     * Obtener evaluaciones por nivel de triage
     */
    public List<EvaluacionTriage> obtenerPorNivelTriage(String nivelTriage) {
        List<EvaluacionTriage> evaluaciones = new ArrayList<>();
        String sql = "SELECT * FROM evaluaciones_triage WHERE nivel_triage = ? ORDER BY fecha_creacion DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nivelTriage);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                evaluaciones.add(mapResultSetToEvaluacion(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener evaluaciones por nivel: " + e.getMessage());
            e.printStackTrace();
        }
        return evaluaciones;
    }

    /**
     * Mapear ResultSet a objeto EvaluacionTriage
     */
    private EvaluacionTriage mapResultSetToEvaluacion(ResultSet rs) throws SQLException {
        EvaluacionTriage evaluacion = new EvaluacionTriage();
        
        evaluacion.setId(rs.getInt("id"));
        evaluacion.setNumeroFolio(rs.getString("numero_folio"));
        evaluacion.setFechaIngreso(rs.getTimestamp("fecha_ingreso").toLocalDateTime());
        evaluacion.setMotivoConsulta(rs.getString("motivo_consulta"));
        evaluacion.setPresionArterial(rs.getString("presion_arterial"));
        
        // Campos que pueden ser null
        Integer frecCard = rs.getInt("frecuencia_cardiaca");
        evaluacion.setFrecuenciaCardiaca(rs.wasNull() ? null : frecCard);
        
        Double temp = rs.getDouble("temperatura");
        evaluacion.setTemperatura(rs.wasNull() ? null : temp);
        
        Integer frecResp = rs.getInt("frecuencia_respiratoria");
        evaluacion.setFrecuenciaRespiratoria(rs.wasNull() ? null : frecResp);
        
        Integer satO2 = rs.getInt("saturacion_o2");
        evaluacion.setSaturacionO2(rs.wasNull() ? null : satO2);
        
        Integer dolor = rs.getInt("nivel_dolor");
        evaluacion.setNivelDolor(rs.wasNull() ? null : dolor);
        
        evaluacion.setObservacionesClinicas(rs.getString("observaciones_clinicas"));
        evaluacion.setNivelTriage(rs.getString("nivel_triage"));
        evaluacion.setEspecialidad(rs.getString("especialidad"));
        evaluacion.setDoctorId(rs.getInt("doctor_id"));
        evaluacion.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        
        return evaluacion;
    }

    /**
     * Verificar si existe folio
     */
    public boolean existeFolio(String numeroFolio) {
        String sql = "SELECT COUNT(*) FROM evaluaciones_triage WHERE numero_folio = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, numeroFolio);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al verificar folio: " + e.getMessage());
        }
        return false;
    }
}