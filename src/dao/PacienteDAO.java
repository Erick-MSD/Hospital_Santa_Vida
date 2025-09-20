package dao;

import models.Paciente;
import utils.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad Paciente
 * Maneja todas las operaciones CRUD con la tabla pacientes
 */
public class PacienteDAO {
    
    private final DatabaseConnection dbConnection;
    
    public PacienteDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Crea un nuevo paciente en la base de datos
     */
    public int crear(Paciente paciente) throws SQLException {
        String sql = """
            INSERT INTO pacientes (nombre, apellido_paterno, apellido_materno, 
                                 fecha_nacimiento, sexo, curp, rfc, telefono_principal, 
                                 telefono_secundario, email, direccion_calle, direccion_numero, 
                                 direccion_colonia, direccion_ciudad, direccion_estado, direccion_cp, 
                                 seguro_medico, numero_poliza, contacto_emergencia_nombre, 
                                 contacto_emergencia_telefono, contacto_emergencia_relacion) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        return dbConnection.executeInsertWithGeneratedKey(sql,
                paciente.getNombre(),
                paciente.getApellidoPaterno(),
                paciente.getApellidoMaterno(),
                paciente.getFechaNacimiento(),
                paciente.getSexo().name(),
                paciente.getCurp(),
                paciente.getRfc(),
                paciente.getTelefonoPrincipal(),
                paciente.getTelefonoSecundario(),
                paciente.getEmail(),
                paciente.getDireccionCalle(),
                paciente.getDireccionNumero(),
                paciente.getDireccionColonia(),
                paciente.getDireccionCiudad(),
                paciente.getDireccionEstado(),
                paciente.getDireccionCp(),
                paciente.getSeguroMedico(),
                paciente.getNumeroPoliza(),
                paciente.getContactoEmergenciaNombre(),
                paciente.getContactoEmergenciaTelefono(),
                paciente.getContactoEmergenciaRelacion()
        );
    }
    
    /**
     * Busca un paciente por ID
     */
    public Paciente buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM pacientes WHERE id = ?";
        
        try (ResultSet rs = dbConnection.executeQuery(sql, id)) {
            if (rs.next()) {
                return mapearPaciente(rs);
            }
        }
        return null;
    }
    
    /**
     * Busca un paciente por CURP
     */
    public Paciente buscarPorCurp(String curp) throws SQLException {
        String sql = "SELECT * FROM pacientes WHERE curp = ?";
        
        try (ResultSet rs = dbConnection.executeQuery(sql, curp)) {
            if (rs.next()) {
                return mapearPaciente(rs);
            }
        }
        return null;
    }
    
    /**
     * Busca pacientes por nombre completo (búsqueda parcial)
     */
    public List<Paciente> buscarPorNombre(String nombre) throws SQLException {
        String sql = """
            SELECT * FROM pacientes 
            WHERE CONCAT(nombre, ' ', apellido_paterno, ' ', IFNULL(apellido_materno, '')) LIKE ?
            ORDER BY nombre, apellido_paterno
            """;
        
        List<Paciente> pacientes = new ArrayList<>();
        String parametroBusqueda = "%" + nombre + "%";
        
        try (ResultSet rs = dbConnection.executeQuery(sql, parametroBusqueda)) {
            while (rs.next()) {
                pacientes.add(mapearPaciente(rs));
            }
        }
        return pacientes;
    }
    
    /**
     * Busca un paciente por teléfono
     */
    public Paciente buscarPorTelefono(String telefono) throws SQLException {
        String sql = "SELECT * FROM pacientes WHERE telefono_principal = ? OR telefono_secundario = ?";
        
        try (ResultSet rs = dbConnection.executeQuery(sql, telefono, telefono)) {
            if (rs.next()) {
                return mapearPaciente(rs);
            }
        }
        return null;
    }
    
    /**
     * Obtiene todos los pacientes con paginación
     */
    public List<Paciente> obtenerTodos(int limite, int offset) throws SQLException {
        String sql = """
            SELECT * FROM pacientes 
            ORDER BY fecha_registro DESC 
            LIMIT ? OFFSET ?
            """;
        
        List<Paciente> pacientes = new ArrayList<>();
        try (ResultSet rs = dbConnection.executeQuery(sql, limite, offset)) {
            while (rs.next()) {
                pacientes.add(mapearPaciente(rs));
            }
        }
        return pacientes;
    }
    
    /**
     * Obtiene pacientes registrados en un rango de fechas
     */
    public List<Paciente> obtenerPorFechaRegistro(LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        String sql = """
            SELECT * FROM pacientes 
            WHERE DATE(fecha_registro) BETWEEN ? AND ?
            ORDER BY fecha_registro DESC
            """;
        
        List<Paciente> pacientes = new ArrayList<>();
        try (ResultSet rs = dbConnection.executeQuery(sql, fechaInicio, fechaFin)) {
            while (rs.next()) {
                pacientes.add(mapearPaciente(rs));
            }
        }
        return pacientes;
    }
    
    /**
     * Obtiene pacientes por seguro médico
     */
    public List<Paciente> obtenerPorSeguroMedico(String seguro) throws SQLException {
        String sql = "SELECT * FROM pacientes WHERE seguro_medico LIKE ? ORDER BY nombre, apellido_paterno";
        
        List<Paciente> pacientes = new ArrayList<>();
        String parametroBusqueda = "%" + seguro + "%";
        
        try (ResultSet rs = dbConnection.executeQuery(sql, parametroBusqueda)) {
            while (rs.next()) {
                pacientes.add(mapearPaciente(rs));
            }
        }
        return pacientes;
    }
    
    /**
     * Actualiza un paciente existente
     */
    public boolean actualizar(Paciente paciente) throws SQLException {
        String sql = """
            UPDATE pacientes SET 
                nombre = ?, apellido_paterno = ?, apellido_materno = ?, 
                fecha_nacimiento = ?, sexo = ?, curp = ?, rfc = ?, 
                telefono_principal = ?, telefono_secundario = ?, email = ?, 
                direccion_calle = ?, direccion_numero = ?, direccion_colonia = ?, 
                direccion_ciudad = ?, direccion_estado = ?, direccion_cp = ?, 
                seguro_medico = ?, numero_poliza = ?, 
                contacto_emergencia_nombre = ?, contacto_emergencia_telefono = ?, 
                contacto_emergencia_relacion = ?
            WHERE id = ?
            """;
        
        int filasAfectadas = dbConnection.executeUpdate(sql,
                paciente.getNombre(),
                paciente.getApellidoPaterno(),
                paciente.getApellidoMaterno(),
                paciente.getFechaNacimiento(),
                paciente.getSexo().name(),
                paciente.getCurp(),
                paciente.getRfc(),
                paciente.getTelefonoPrincipal(),
                paciente.getTelefonoSecundario(),
                paciente.getEmail(),
                paciente.getDireccionCalle(),
                paciente.getDireccionNumero(),
                paciente.getDireccionColonia(),
                paciente.getDireccionCiudad(),
                paciente.getDireccionEstado(),
                paciente.getDireccionCp(),
                paciente.getSeguroMedico(),
                paciente.getNumeroPoliza(),
                paciente.getContactoEmergenciaNombre(),
                paciente.getContactoEmergenciaTelefono(),
                paciente.getContactoEmergenciaRelacion(),
                paciente.getId()
        );
        
        return filasAfectadas > 0;
    }
    
    /**
     * Verifica si un CURP ya existe
     */
    public boolean existeCurp(String curp) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pacientes WHERE curp = ?";
        
        try (ResultSet rs = dbConnection.executeQuery(sql, curp)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    /**
     * Cuenta el total de pacientes registrados
     */
    public int contarTotalPacientes() throws SQLException {
        String sql = "SELECT COUNT(*) FROM pacientes";
        
        try (ResultSet rs = dbConnection.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    /**
     * Obtiene pacientes por rango de edad
     */
    public List<Paciente> obtenerPorRangoEdad(int edadMinima, int edadMaxima) throws SQLException {
        String sql = """
            SELECT * FROM pacientes 
            WHERE TIMESTAMPDIFF(YEAR, fecha_nacimiento, CURDATE()) BETWEEN ? AND ?
            ORDER BY fecha_nacimiento DESC
            """;
        
        List<Paciente> pacientes = new ArrayList<>();
        try (ResultSet rs = dbConnection.executeQuery(sql, edadMinima, edadMaxima)) {
            while (rs.next()) {
                pacientes.add(mapearPaciente(rs));
            }
        }
        return pacientes;
    }
    
    /**
     * Obtiene pacientes por sexo
     */
    public List<Paciente> obtenerPorSexo(Paciente.Sexo sexo) throws SQLException {
        String sql = "SELECT * FROM pacientes WHERE sexo = ? ORDER BY nombre, apellido_paterno";
        
        List<Paciente> pacientes = new ArrayList<>();
        try (ResultSet rs = dbConnection.executeQuery(sql, sexo.name())) {
            while (rs.next()) {
                pacientes.add(mapearPaciente(rs));
            }
        }
        return pacientes;
    }
    
    /**
     * Busca pacientes por ciudad
     */
    public List<Paciente> obtenerPorCiudad(String ciudad) throws SQLException {
        String sql = "SELECT * FROM pacientes WHERE direccion_ciudad LIKE ? ORDER BY nombre, apellido_paterno";
        
        List<Paciente> pacientes = new ArrayList<>();
        String parametroBusqueda = "%" + ciudad + "%";
        
        try (ResultSet rs = dbConnection.executeQuery(sql, parametroBusqueda)) {
            while (rs.next()) {
                pacientes.add(mapearPaciente(rs));
            }
        }
        return pacientes;
    }
    
    /**
     * Obtiene pacientes que no tienen seguro médico
     */
    public List<Paciente> obtenerSinSeguroMedico() throws SQLException {
        String sql = """
            SELECT * FROM pacientes 
            WHERE seguro_medico IS NULL OR seguro_medico = '' 
            ORDER BY nombre, apellido_paterno
            """;
        
        List<Paciente> pacientes = new ArrayList<>();
        try (ResultSet rs = dbConnection.executeQuery(sql)) {
            while (rs.next()) {
                pacientes.add(mapearPaciente(rs));
            }
        }
        return pacientes;
    }
    
    /**
     * Busca pacientes con información incompleta
     */
    public List<Paciente> obtenerConDatosIncompletos() throws SQLException {
        String sql = """
            SELECT * FROM pacientes 
            WHERE nombre IS NULL OR nombre = '' 
               OR apellido_paterno IS NULL OR apellido_paterno = ''
               OR fecha_nacimiento IS NULL
               OR telefono_principal IS NULL OR telefono_principal = ''
               OR contacto_emergencia_nombre IS NULL OR contacto_emergencia_nombre = ''
               OR contacto_emergencia_telefono IS NULL OR contacto_emergencia_telefono = ''
               OR direccion_calle IS NULL OR direccion_calle = ''
            ORDER BY fecha_registro DESC
            """;
        
        List<Paciente> pacientes = new ArrayList<>();
        try (ResultSet rs = dbConnection.executeQuery(sql)) {
            while (rs.next()) {
                pacientes.add(mapearPaciente(rs));
            }
        }
        return pacientes;
    }
    
    /**
     * Obtiene los pacientes más recientes
     */
    public List<Paciente> obtenerMasRecientes(int limite) throws SQLException {
        String sql = "SELECT * FROM pacientes ORDER BY fecha_registro DESC LIMIT ?";
        
        List<Paciente> pacientes = new ArrayList<>();
        try (ResultSet rs = dbConnection.executeQuery(sql, limite)) {
            while (rs.next()) {
                pacientes.add(mapearPaciente(rs));
            }
        }
        return pacientes;
    }
    
    /**
     * Mapea un ResultSet a un objeto Paciente
     */
    private Paciente mapearPaciente(ResultSet rs) throws SQLException {
        Paciente paciente = new Paciente();
        
        paciente.setId(rs.getInt("id"));
        paciente.setNombre(rs.getString("nombre"));
        paciente.setApellidoPaterno(rs.getString("apellido_paterno"));
        paciente.setApellidoMaterno(rs.getString("apellido_materno"));
        
        Date fechaNacimiento = rs.getDate("fecha_nacimiento");
        if (fechaNacimiento != null) {
            paciente.setFechaNacimiento(fechaNacimiento.toLocalDate());
        }
        
        paciente.setSexo(Paciente.Sexo.valueOf(rs.getString("sexo")));
        paciente.setCurp(rs.getString("curp"));
        paciente.setRfc(rs.getString("rfc"));
        paciente.setTelefonoPrincipal(rs.getString("telefono_principal"));
        paciente.setTelefonoSecundario(rs.getString("telefono_secundario"));
        paciente.setEmail(rs.getString("email"));
        
        // Dirección
        paciente.setDireccionCalle(rs.getString("direccion_calle"));
        paciente.setDireccionNumero(rs.getString("direccion_numero"));
        paciente.setDireccionColonia(rs.getString("direccion_colonia"));
        paciente.setDireccionCiudad(rs.getString("direccion_ciudad"));
        paciente.setDireccionEstado(rs.getString("direccion_estado"));
        paciente.setDireccionCp(rs.getString("direccion_cp"));
        
        // Seguro médico
        paciente.setSeguroMedico(rs.getString("seguro_medico"));
        paciente.setNumeroPoliza(rs.getString("numero_poliza"));
        
        // Contacto de emergencia
        paciente.setContactoEmergenciaNombre(rs.getString("contacto_emergencia_nombre"));
        paciente.setContactoEmergenciaTelefono(rs.getString("contacto_emergencia_telefono"));
        paciente.setContactoEmergenciaRelacion(rs.getString("contacto_emergencia_relacion"));
        
        // Fecha de registro
        Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
        if (fechaRegistro != null) {
            paciente.setFechaRegistro(fechaRegistro.toLocalDateTime());
        }
        
        return paciente;
    }
    
    /**
     * Elimina físicamente un paciente (usar con precaución)
     */
    public boolean eliminarFisicamente(int pacienteId) throws SQLException {
        String sql = "DELETE FROM pacientes WHERE id = ?";
        
        int filasAfectadas = dbConnection.executeUpdate(sql, pacienteId);
        return filasAfectadas > 0;
    }
    
    /**
     * Obtiene estadísticas de pacientes
     */
    public String obtenerEstadisticas() throws SQLException {
        StringBuilder stats = new StringBuilder();
        stats.append("=== ESTADÍSTICAS DE PACIENTES ===\n");
        
        // Total de pacientes
        stats.append("Total registrados: ").append(contarTotalPacientes()).append("\n");
        
        // Por sexo
        for (Paciente.Sexo sexo : Paciente.Sexo.values()) {
            String sql = "SELECT COUNT(*) FROM pacientes WHERE sexo = ?";
            try (ResultSet rs = dbConnection.executeQuery(sql, sexo.name())) {
                if (rs.next()) {
                    stats.append(sexo.name()).append(": ").append(rs.getInt(1)).append("\n");
                }
            }
        }
        
        // Con y sin seguro médico
        String sql = "SELECT COUNT(*) FROM pacientes WHERE seguro_medico IS NOT NULL AND seguro_medico != ''";
        try (ResultSet rs = dbConnection.executeQuery(sql)) {
            if (rs.next()) {
                stats.append("Con seguro médico: ").append(rs.getInt(1)).append("\n");
            }
        }
        
        sql = "SELECT COUNT(*) FROM pacientes WHERE seguro_medico IS NULL OR seguro_medico = ''";
        try (ResultSet rs = dbConnection.executeQuery(sql)) {
            if (rs.next()) {
                stats.append("Sin seguro médico: ").append(rs.getInt(1)).append("\n");
            }
        }
        
        stats.append("==================================");
        return stats.toString();
    }
}