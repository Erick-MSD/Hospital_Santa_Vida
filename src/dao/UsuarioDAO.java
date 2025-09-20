package dao;

import models.Usuario;
import utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad Usuario
 * Maneja todas las operaciones CRUD con la tabla usuarios
 */
public class UsuarioDAO {
    
    private final DatabaseConnection dbConnection;
    
    public UsuarioDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Crea un nuevo usuario en la base de datos
     */
    public int crear(Usuario usuario) throws SQLException {
        String sql = """
            INSERT INTO usuarios (username, email, password_hash, tipo_usuario, 
                                nombre_completo, cedula_profesional, especialidad, 
                                telefono, activo, created_by) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        return dbConnection.executeInsertWithGeneratedKey(sql,
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getPasswordHash(),
                usuario.getTipoUsuario().name(),
                usuario.getNombreCompleto(),
                usuario.getCedulaProfesional(),
                usuario.getEspecialidad(),
                usuario.getTelefono(),
                usuario.isActivo(),
                usuario.getCreatedBy()
        );
    }
    
    /**
     * Busca un usuario por ID
     */
    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        
        try (ResultSet rs = dbConnection.executeQuery(sql, id)) {
            if (rs.next()) {
                return mapearUsuario(rs);
            }
        }
        return null;
    }
    
    /**
     * Busca un usuario por username
     */
    public Usuario buscarPorUsername(String username) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE username = ? AND activo = true";
        
        try (ResultSet rs = dbConnection.executeQuery(sql, username)) {
            if (rs.next()) {
                return mapearUsuario(rs);
            }
        }
        return null;
    }
    
    /**
     * Busca un usuario por email
     */
    public Usuario buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE email = ? AND activo = true";
        
        try (ResultSet rs = dbConnection.executeQuery(sql, email)) {
            if (rs.next()) {
                return mapearUsuario(rs);
            }
        }
        return null;
    }
    
    /**
     * Obtiene todos los usuarios activos
     */
    public List<Usuario> obtenerTodos() throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE activo = true ORDER BY nombre_completo";
        
        List<Usuario> usuarios = new ArrayList<>();
        try (ResultSet rs = dbConnection.executeQuery(sql)) {
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }
        return usuarios;
    }
    
    /**
     * Obtiene usuarios por tipo
     */
    public List<Usuario> obtenerPorTipo(Usuario.TipoUsuario tipo) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE tipo_usuario = ? AND activo = true ORDER BY nombre_completo";
        
        List<Usuario> usuarios = new ArrayList<>();
        try (ResultSet rs = dbConnection.executeQuery(sql, tipo.name())) {
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }
        return usuarios;
    }
    
    /**
     * Actualiza un usuario existente
     */
    public boolean actualizar(Usuario usuario) throws SQLException {
        String sql = """
            UPDATE usuarios SET 
                email = ?, tipo_usuario = ?, nombre_completo = ?, 
                cedula_profesional = ?, especialidad = ?, telefono = ?, activo = ?
            WHERE id = ?
            """;
        
        int filasAfectadas = dbConnection.executeUpdate(sql,
                usuario.getEmail(),
                usuario.getTipoUsuario().name(),
                usuario.getNombreCompleto(),
                usuario.getCedulaProfesional(),
                usuario.getEspecialidad(),
                usuario.getTelefono(),
                usuario.isActivo(),
                usuario.getId()
        );
        
        return filasAfectadas > 0;
    }
    
    /**
     * Actualiza la contraseña de un usuario
     */
    public boolean actualizarPassword(int usuarioId, String nuevoPasswordHash) throws SQLException {
        String sql = "UPDATE usuarios SET password_hash = ? WHERE id = ?";
        
        int filasAfectadas = dbConnection.executeUpdate(sql, nuevoPasswordHash, usuarioId);
        return filasAfectadas > 0;
    }
    
    /**
     * Actualiza la fecha del último acceso
     */
    public boolean actualizarUltimoAcceso(int usuarioId) throws SQLException {
        String sql = "UPDATE usuarios SET ultimo_acceso = CURRENT_TIMESTAMP WHERE id = ?";
        
        int filasAfectadas = dbConnection.executeUpdate(sql, usuarioId);
        return filasAfectadas > 0;
    }
    
    /**
     * Desactiva un usuario (soft delete)
     */
    public boolean desactivar(int usuarioId) throws SQLException {
        String sql = "UPDATE usuarios SET activo = false WHERE id = ?";
        
        int filasAfectadas = dbConnection.executeUpdate(sql, usuarioId);
        return filasAfectadas > 0;
    }
    
    /**
     * Reactiva un usuario
     */
    public boolean reactivar(int usuarioId) throws SQLException {
        String sql = "UPDATE usuarios SET activo = true WHERE id = ?";
        
        int filasAfectadas = dbConnection.executeUpdate(sql, usuarioId);
        return filasAfectadas > 0;
    }
    
    /**
     * Verifica si un username ya existe
     */
    public boolean existeUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        
        try (ResultSet rs = dbConnection.executeQuery(sql, username)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    /**
     * Verifica si un email ya existe
     */
    public boolean existeEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        
        try (ResultSet rs = dbConnection.executeQuery(sql, email)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    /**
     * Obtiene el conteo de usuarios por tipo
     */
    public int contarUsuariosPorTipo(Usuario.TipoUsuario tipo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE tipo_usuario = ? AND activo = true";
        
        try (ResultSet rs = dbConnection.executeQuery(sql, tipo.name())) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    /**
     * Busca usuarios por nombre (búsqueda parcial)
     */
    public List<Usuario> buscarPorNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE nombre_completo LIKE ? AND activo = true ORDER BY nombre_completo";
        
        List<Usuario> usuarios = new ArrayList<>();
        String parametroBusqueda = "%" + nombre + "%";
        
        try (ResultSet rs = dbConnection.executeQuery(sql, parametroBusqueda)) {
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }
        return usuarios;
    }
    
    /**
     * Obtiene usuarios creados por un administrador específico
     */
    public List<Usuario> obtenerCreadosPor(int creadorId) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE created_by = ? ORDER BY fecha_creacion DESC";
        
        List<Usuario> usuarios = new ArrayList<>();
        try (ResultSet rs = dbConnection.executeQuery(sql, creadorId)) {
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }
        return usuarios;
    }
    
    /**
     * Valida credenciales de login
     */
    public Usuario validarCredenciales(String username, String passwordHash) throws SQLException {
        String sql = """
            SELECT * FROM usuarios 
            WHERE (username = ? OR email = ?) 
            AND password_hash = ? 
            AND activo = true
            """;
        
        try (ResultSet rs = dbConnection.executeQuery(sql, username, username, passwordHash)) {
            if (rs.next()) {
                Usuario usuario = mapearUsuario(rs);
                // Actualizar último acceso
                actualizarUltimoAcceso(usuario.getId());
                return usuario;
            }
        }
        return null;
    }
    
    /**
     * Mapea un ResultSet a un objeto Usuario
     */
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        
        usuario.setId(rs.getInt("id"));
        usuario.setUsername(rs.getString("username"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        usuario.setTipoUsuario(Usuario.TipoUsuario.valueOf(rs.getString("tipo_usuario")));
        usuario.setNombreCompleto(rs.getString("nombre_completo"));
        usuario.setCedulaProfesional(rs.getString("cedula_profesional"));
        usuario.setEspecialidad(rs.getString("especialidad"));
        usuario.setTelefono(rs.getString("telefono"));
        usuario.setActivo(rs.getBoolean("activo"));
        
        // Manejar fechas que pueden ser null
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            usuario.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        Timestamp ultimoAcceso = rs.getTimestamp("ultimo_acceso");
        if (ultimoAcceso != null) {
            usuario.setUltimoAcceso(ultimoAcceso.toLocalDateTime());
        }
        
        usuario.setCreatedBy(rs.getObject("created_by", Integer.class));
        
        return usuario;
    }
    
    /**
     * Elimina físicamente un usuario (usar con precaución)
     */
    public boolean eliminarFisicamente(int usuarioId) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        
        int filasAfectadas = dbConnection.executeUpdate(sql, usuarioId);
        return filasAfectadas > 0;
    }
    
    /**
     * Obtiene estadísticas generales de usuarios
     */
    public String obtenerEstadisticas() throws SQLException {
        StringBuilder stats = new StringBuilder();
        stats.append("=== ESTADÍSTICAS DE USUARIOS ===\n");
        
        for (Usuario.TipoUsuario tipo : Usuario.TipoUsuario.values()) {
            int count = contarUsuariosPorTipo(tipo);
            stats.append(tipo.name()).append(": ").append(count).append("\n");
        }
        
        // Total de usuarios activos
        String sql = "SELECT COUNT(*) FROM usuarios WHERE activo = true";
        try (ResultSet rs = dbConnection.executeQuery(sql)) {
            if (rs.next()) {
                stats.append("TOTAL ACTIVOS: ").append(rs.getInt(1)).append("\n");
            }
        }
        
        // Total de usuarios inactivos
        sql = "SELECT COUNT(*) FROM usuarios WHERE activo = false";
        try (ResultSet rs = dbConnection.executeQuery(sql)) {
            if (rs.next()) {
                stats.append("TOTAL INACTIVOS: ").append(rs.getInt(1)).append("\n");
            }
        }
        
        stats.append("===============================");
        return stats.toString();
    }
}