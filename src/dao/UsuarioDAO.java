package dao;

import models.Usuario;
import models.TipoUsuario;
import utils.PasswordUtils;
import utils.ValidationUtils;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * DAO para la gestión de usuarios en el sistema hospitalario
 * Maneja todas las operaciones CRUD para la tabla usuarios
 * Incluye funcionalidades específicas de autenticación y autorización
 */
public class UsuarioDAO extends BaseDAO<Usuario> {
    
    private static final String TABLA = "usuarios";
    
    // Consultas SQL predefinidas
    private static final String SQL_INSERTAR = 
        "INSERT INTO " + TABLA + " (username, password_hash, nombre_completo, tipo_usuario, " +
        "email, telefono, activo, fecha_creacion, ultimo_acceso) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SQL_ACTUALIZAR = 
        "UPDATE " + TABLA + " SET username = ?, password_hash = ?, nombre_completo = ?, " +
        "tipo_usuario = ?, email = ?, telefono = ?, activo = ?, ultimo_acceso = ? " +
        "WHERE id = ?";
    
    private static final String SQL_ELIMINAR = 
        "DELETE FROM " + TABLA + " WHERE id = ?";
    
    private static final String SQL_BUSCAR_POR_ID = 
        "SELECT * FROM " + TABLA + " WHERE id = ?";
    
    private static final String SQL_OBTENER_TODOS = 
        "SELECT * FROM " + TABLA + " ORDER BY nombre_completo";
    
    private static final String SQL_BUSCAR_POR_USUARIO = 
        "SELECT * FROM " + TABLA + " WHERE username = ?";
    
    private static final String SQL_BUSCAR_POR_EMAIL = 
        "SELECT * FROM " + TABLA + " WHERE email = ?";
    
    private static final String SQL_BUSCAR_ACTIVOS = 
        "SELECT * FROM " + TABLA + " WHERE activo = 1 ORDER BY nombre_completo";
    
    private static final String SQL_BUSCAR_POR_TIPO = 
        "SELECT * FROM " + TABLA + " WHERE tipo_usuario = ? ORDER BY nombre_completo";
    
    private static final String SQL_ACTUALIZAR_ULTIMO_ACCESO = 
        "UPDATE " + TABLA + " SET ultimo_acceso = ? WHERE id = ?";
    
    private static final String SQL_CAMBIAR_PASSWORD = 
        "UPDATE " + TABLA + " SET password_hash = ? WHERE id = ?";
    
    private static final String SQL_ACTIVAR_DESACTIVAR = 
        "UPDATE " + TABLA + " SET activo = ? WHERE id = ?";
    
    /**
     * Inserta un nuevo usuario en la base de datos
     * @param usuario Usuario a insertar
     * @return true si se insertó correctamente
     * @throws SQLException si hay error en la operación
     */
    @Override
    public boolean insertar(Usuario usuario) throws SQLException {
        validarUsuario(usuario);
        
        // Verificar que el nombre de usuario no exista
        if (existeNombreUsuario(usuario.getNombreUsuario())) {
            throw new SQLException("El nombre de usuario '" + usuario.getNombreUsuario() + "' ya existe");
        }
        
        // Verificar que el email no exista
        if (existeEmail(usuario.getEmail())) {
            throw new SQLException("El email '" + usuario.getEmail() + "' ya está registrado");
        }
        
        // Encriptar password si es necesario
        String passwordHash = usuario.getPasswordHash();
        if (!PasswordUtils.esHashValido(passwordHash)) {
            passwordHash = PasswordUtils.hashPassword(passwordHash);
        }
        
        int idGenerado = ejecutarInsercionConClave(SQL_INSERTAR,
            usuario.getNombreUsuario(),
            passwordHash,
            usuario.getNombreCompleto(),
            usuario.getTipoUsuario().name(),
            usuario.getEmail(),
            usuario.getTelefono(),
            usuario.isActivo(),
            convertirATimestamp(usuario.getFechaCreacion()),
            convertirATimestamp(usuario.getUltimoAcceso())
        );
        
        if (idGenerado > 0) {
            usuario.setId(idGenerado);
            return true;
        }
        
        return false;
    }
    
    /**
     * Actualiza un usuario existente
     * @param usuario Usuario a actualizar
     * @return true si se actualizó correctamente
     * @throws SQLException si hay error en la operación
     */
    @Override
    public boolean actualizar(Usuario usuario) throws SQLException {
        validarUsuario(usuario);
        
        if (usuario.getId() <= 0) {
            throw new IllegalArgumentException("ID de usuario inválido");
        }
        
        // Verificar que el nombre de usuario no esté en uso por otro usuario
        Usuario existente = buscarPorNombreUsuario(usuario.getNombreUsuario());
        if (existente != null && existente.getId() != usuario.getId()) {
            throw new SQLException("El nombre de usuario '" + usuario.getNombreUsuario() + "' ya existe");
        }
        
        // Verificar que el email no esté en uso por otro usuario
        existente = buscarPorEmail(usuario.getEmail());
        if (existente != null && existente.getId() != usuario.getId()) {
            throw new SQLException("El email '" + usuario.getEmail() + "' ya está registrado");
        }
        
        int filasActualizadas = ejecutarActualizacion(SQL_ACTUALIZAR,
            usuario.getNombreUsuario(),
            usuario.getPasswordHash(),
            usuario.getNombreCompleto(),
            usuario.getTipoUsuario().name(),
            usuario.getEmail(),
            usuario.getTelefono(),
            usuario.isActivo(),
            convertirATimestamp(usuario.getUltimoAcceso()),
            usuario.getId()
        );
        
        return filasActualizadas > 0;
    }
    
    /**
     * Elimina un usuario por su ID
     * @param id ID del usuario a eliminar
     * @return true si se eliminó correctamente
     * @throws SQLException si hay error en la operación
     */
    @Override
    public boolean eliminar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de usuario inválido");
        }
        
        int filasEliminadas = ejecutarActualizacion(SQL_ELIMINAR, id);
        return filasEliminadas > 0;
    }
    
    /**
     * Busca un usuario por su ID
     * @param id ID del usuario
     * @return Usuario encontrado o null si no existe
     * @throws SQLException si hay error en la operación
     */
    @Override
    public Usuario buscarPorId(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de usuario inválido");
        }
        
        return ejecutarConsultaUnica(SQL_BUSCAR_POR_ID, id);
    }
    
    /**
     * Obtiene todos los usuarios
     * @return Lista de todos los usuarios
     * @throws SQLException si hay error en la operación
     */
    @Override
    public List<Usuario> obtenerTodos() throws SQLException {
        return ejecutarConsulta(SQL_OBTENER_TODOS);
    }
    
    /**
     * Busca un usuario por nombre de usuario
     * @param nombreUsuario Nombre de usuario a buscar
     * @return Usuario encontrado o null si no existe
     * @throws SQLException si hay error en la operación
     */
    public Usuario buscarPorNombreUsuario(String nombreUsuario) throws SQLException {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre de usuario no puede estar vacío");
        }
        
        return ejecutarConsultaUnica(SQL_BUSCAR_POR_USUARIO, nombreUsuario.trim());
    }
    
    /**
     * Busca un usuario por email
     * @param email Email a buscar
     * @return Usuario encontrado o null si no existe
     * @throws SQLException si hay error en la operación
     */
    public Usuario buscarPorEmail(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email no puede estar vacío");
        }
        
        return ejecutarConsultaUnica(SQL_BUSCAR_POR_EMAIL, email.trim().toLowerCase());
    }
    
    /**
     * Obtiene todos los usuarios activos
     * @return Lista de usuarios activos
     * @throws SQLException si hay error en la operación
     */
    public List<Usuario> obtenerActivos() throws SQLException {
        return ejecutarConsulta(SQL_BUSCAR_ACTIVOS);
    }
    
    /**
     * Obtiene usuarios por tipo
     * @param tipoUsuario Tipo de usuario a buscar
     * @return Lista de usuarios del tipo especificado
     * @throws SQLException si hay error en la operación
     */
    public List<Usuario> obtenerPorTipo(TipoUsuario tipoUsuario) throws SQLException {
        if (tipoUsuario == null) {
            throw new IllegalArgumentException("Tipo de usuario no puede ser nulo");
        }
        
        return ejecutarConsulta(SQL_BUSCAR_POR_TIPO, tipoUsuario.name());
    }
    
    /**
     * Actualiza la fecha de último acceso de un usuario
     * @param usuarioId ID del usuario
     * @param fechaAcceso Fecha y hora del acceso
     * @return true si se actualizó correctamente
     * @throws SQLException si hay error en la operación
     */
    public boolean actualizarUltimoAcceso(int usuarioId, LocalDateTime fechaAcceso) throws SQLException {
        if (usuarioId <= 0) {
            throw new IllegalArgumentException("ID de usuario inválido");
        }
        
        if (fechaAcceso == null) {
            fechaAcceso = LocalDateTime.now();
        }
        
        int filasActualizadas = ejecutarActualizacion(SQL_ACTUALIZAR_ULTIMO_ACCESO,
            convertirATimestamp(fechaAcceso), usuarioId);
        
        return filasActualizadas > 0;
    }
    
    /**
     * Cambia la contraseña de un usuario
     * @param usuarioId ID del usuario
     * @param nuevaPassword Nueva contraseña (será encriptada)
     * @return true si se cambió correctamente
     * @throws SQLException si hay error en la operación
     */
    public boolean cambiarPassword(int usuarioId, String nuevaPassword) throws SQLException {
        if (usuarioId <= 0) {
            throw new IllegalArgumentException("ID de usuario inválido");
        }
        
        if (!ValidationUtils.validarPassword(nuevaPassword)) {
            throw new IllegalArgumentException("La contraseña no cumple con los requisitos de seguridad");
        }
        
        String passwordHash = PasswordUtils.hashPassword(nuevaPassword);
        int filasActualizadas = ejecutarActualizacion(SQL_CAMBIAR_PASSWORD, passwordHash, usuarioId);
        
        return filasActualizadas > 0;
    }
    
    /**
     * Activa o desactiva un usuario
     * @param usuarioId ID del usuario
     * @param activo true para activar, false para desactivar
     * @return true si se actualizó correctamente
     * @throws SQLException si hay error en la operación
     */
    public boolean activarDesactivar(int usuarioId, boolean activo) throws SQLException {
        if (usuarioId <= 0) {
            throw new IllegalArgumentException("ID de usuario inválido");
        }
        
        int filasActualizadas = ejecutarActualizacion(SQL_ACTIVAR_DESACTIVAR, activo, usuarioId);
        return filasActualizadas > 0;
    }
    
    /**
     * Autentica un usuario con nombre de usuario y contraseña
     * @param nombreUsuario Nombre de usuario
     * @param password Contraseña sin encriptar
     * @return Usuario autenticado o null si las credenciales son incorrectas
     * @throws SQLException si hay error en la operación
     */
    public Usuario autenticar(String nombreUsuario, String password) throws SQLException {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre de usuario no puede estar vacío");
        }
        
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Contraseña no puede estar vacía");
        }
        
        Usuario usuario = buscarPorNombreUsuario(nombreUsuario.trim());
        
        if (usuario == null) {
            return null; // Usuario no encontrado
        }
        
        if (!usuario.isActivo()) {
            return null; // Usuario desactivado
        }
        
        // Verificar contraseña
        if (PasswordUtils.verifyPassword(password, usuario.getPasswordHash())) {
            // Actualizar último acceso
            actualizarUltimoAcceso(usuario.getId(), LocalDateTime.now());
            return usuario;
        }
        
        return null; // Contraseña incorrecta
    }
    
    /**
     * Verifica si existe un nombre de usuario
     * @param nombreUsuario Nombre de usuario a verificar
     * @return true si existe
     * @throws SQLException si hay error en la operación
     */
    public boolean existeNombreUsuario(String nombreUsuario) throws SQLException {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            return false;
        }
        
        Usuario usuario = buscarPorNombreUsuario(nombreUsuario.trim());
        return usuario != null;
    }
    
    /**
     * Verifica si existe un email
     * @param email Email a verificar
     * @return true si existe
     * @throws SQLException si hay error en la operación
     */
    public boolean existeEmail(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        Usuario usuario = buscarPorEmail(email.trim());
        return usuario != null;
    }
    
    /**
     * Obtiene estadísticas de usuarios por tipo
     * @return Lista con conteos por tipo de usuario
     * @throws SQLException si hay error en la operación
     */
    public List<EstadisticaUsuario> obtenerEstadisticasPorTipo() throws SQLException {
        String sql = "SELECT tipo_usuario, COUNT(*) as total, " +
                    "SUM(CASE WHEN activo = 1 THEN 1 ELSE 0 END) as activos " +
                    "FROM " + TABLA + " GROUP BY tipo_usuario";
        
        List<EstadisticaUsuario> estadisticas = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                EstadisticaUsuario estadistica = new EstadisticaUsuario();
                estadistica.tipoUsuario = TipoUsuario.valueOf(rs.getString("tipo_usuario"));
                estadistica.total = rs.getInt("total");
                estadistica.activos = rs.getInt("activos");
                estadisticas.add(estadistica);
            }
            
        } finally {
            cerrarRecursos(rs, stmt, conn);
        }
        
        return estadisticas;
    }
    
    /**
     * Mapea un ResultSet a un objeto Usuario
     * @param rs ResultSet con los datos del usuario
     * @return Usuario mapeado
     * @throws SQLException si hay error en el mapeo
     */
    @Override
    protected Usuario mapearResultSet(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        
        usuario.setId(rs.getInt("id"));
        usuario.setNombreUsuario(rs.getString("username"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        usuario.setNombreCompleto(rs.getString("nombre_completo"));
        usuario.setTipoUsuario(TipoUsuario.valueOf(rs.getString("tipo_usuario")));
        usuario.setEmail(rs.getString("email"));
        usuario.setTelefono(rs.getString("telefono"));
        usuario.setActivo(rs.getBoolean("activo"));
        
        // Conversión de fechas
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            usuario.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        Timestamp ultimoAcceso = rs.getTimestamp("ultimo_acceso");
        if (ultimoAcceso != null) {
            usuario.setUltimoAcceso(ultimoAcceso.toLocalDateTime());
        }
        
        return usuario;
    }
    
    /**
     * Valida los datos de un usuario antes de insertarlo/actualizarlo
     * @param usuario Usuario a validar
     * @throws IllegalArgumentException si los datos no son válidos
     */
    private void validarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no puede ser nulo");
        }
        
        if (!ValidationUtils.validarTexto(usuario.getNombreUsuario(), 3, 50)) {
            throw new IllegalArgumentException("Nombre de usuario debe tener entre 3 y 50 caracteres");
        }
        
        if (!ValidationUtils.validarTexto(usuario.getNombreCompleto(), 2, 100)) {
            throw new IllegalArgumentException("Nombre completo debe tener entre 2 y 100 caracteres");
        }
        
        if (usuario.getTipoUsuario() == null) {
            throw new IllegalArgumentException("Tipo de usuario es obligatorio");
        }
        
        if (!ValidationUtils.validarEmailBoolean(usuario.getEmail())) {
            throw new IllegalArgumentException("Email no es válido");
        }
        
        if (usuario.getTelefono() != null && !usuario.getTelefono().isEmpty()) {
            if (!ValidationUtils.validarTelefonoBoolean(usuario.getTelefono())) {
                throw new IllegalArgumentException("Teléfono no es válido");
            }
        }
        
        if (usuario.getFechaCreacion() == null) {
            usuario.setFechaCreacion(LocalDateTime.now());
        }
    }
    
    /**
     * Contar total de usuarios
     */
    public int contarTotal() {
        String sql = "SELECT COUNT(*) FROM " + TABLA;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar usuarios", e);
        }
    }

    /**
     * Contar usuarios activos
     */
    public int contarActivos() {
        String sql = "SELECT COUNT(*) FROM " + TABLA + " WHERE activo = 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar usuarios activos", e);
        }
    }

    /**
     * Contar usuarios por tipo
     */
    public java.util.Map<TipoUsuario, Integer> contarPorTipo() {
        String sql = "SELECT tipo_usuario, COUNT(*) FROM " + TABLA + " GROUP BY tipo_usuario";
        java.util.Map<TipoUsuario, Integer> resultado = new java.util.HashMap<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                TipoUsuario tipo = TipoUsuario.valueOf(rs.getString("tipo_usuario"));
                int cantidad = rs.getInt(2);
                resultado.put(tipo, cantidad);
            }
            
            return resultado;
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar usuarios por tipo", e);
        }
    }

    /**
     * Clase interna para estadísticas de usuarios
     */
    public static class EstadisticaUsuario {
        public TipoUsuario tipoUsuario;
        public int total;
        public int activos;
        
        @Override
        public String toString() {
            return String.format("Tipo: %s, Total: %d, Activos: %d", 
                               tipoUsuario, total, activos);
        }
    }
}