package dao;

import utils.DatabaseConnection;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Clase base para todos los DAOs del sistema
 * Proporciona funcionalidades comunes para acceso a datos
 * Implementa patrones de diseño para manejo eficiente de conexiones
 * @param <T> Tipo de entidad que maneja este DAO
 */
public abstract class BaseDAO<T> {
    
    protected DatabaseConnection dbConnection;
    
    /**
     * Constructor que inicializa la conexión a la base de datos
     */
    public BaseDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Obtiene una conexión a la base de datos
     * @return Connection activa
     * @throws SQLException si hay error en la conexión
     */
    protected Connection getConnection() throws SQLException {
        Connection conn = dbConnection.getConnection();
        if (conn == null) {
            throw new SQLException("No se pudo obtener conexión a la base de datos");
        }
        return conn;
    }
    
    // Métodos abstractos que deben implementar las clases hijas
    
    /**
     * Inserta una nueva entidad en la base de datos
     * @param entity La entidad a insertar
     * @return true si se insertó correctamente
     * @throws SQLException si hay error en la operación
     */
    public abstract boolean insertar(T entity) throws SQLException;
    
    /**
     * Actualiza una entidad existente
     * @param entity La entidad a actualizar
     * @return true si se actualizó correctamente
     * @throws SQLException si hay error en la operación
     */
    public abstract boolean actualizar(T entity) throws SQLException;
    
    /**
     * Elimina una entidad por su ID
     * @param id ID de la entidad a eliminar
     * @return true si se eliminó correctamente
     * @throws SQLException si hay error en la operación
     */
    public abstract boolean eliminar(int id) throws SQLException;
    
    /**
     * Busca una entidad por su ID
     * @param id ID de la entidad
     * @return La entidad encontrada o null si no existe
     * @throws SQLException si hay error en la operación
     */
    public abstract T buscarPorId(int id) throws SQLException;
    
    /**
     * Obtiene todas las entidades
     * @return Lista de todas las entidades
     * @throws SQLException si hay error en la operación
     */
    public abstract List<T> obtenerTodos() throws SQLException;
    
    /**
     * Mapea un ResultSet a una entidad
     * @param rs El ResultSet con los datos
     * @return La entidad mapeada
     * @throws SQLException si hay error en el mapeo
     */
    protected abstract T mapearResultSet(ResultSet rs) throws SQLException;
    
    // Métodos utilitarios comunes
    
    /**
     * Cierra recursos de base de datos de forma segura
     * @param rs ResultSet a cerrar
     * @param stmt Statement a cerrar
     * @param conn Connection a cerrar
     */
    protected void cerrarRecursos(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("Error cerrando ResultSet: " + e.getMessage());
            }
        }
        
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Error cerrando Statement: " + e.getMessage());
            }
        }
        
        // No cerramos la conexión aquí porque es administrada por DatabaseConnection
    }
    
    /**
     * Cierra Statement y ResultSet
     */
    protected void cerrarRecursos(ResultSet rs, Statement stmt) {
        cerrarRecursos(rs, stmt, null);
    }
    
    /**
     * Cierra solo Statement
     */
    protected void cerrarRecursos(Statement stmt) {
        cerrarRecursos(null, stmt, null);
    }
    
    /**
     * Ejecuta una consulta y devuelve una lista de resultados
     * @param sql La consulta SQL
     * @param parametros Los parámetros de la consulta
     * @return Lista de entidades
     * @throws SQLException si hay error
     */
    protected List<T> ejecutarConsulta(String sql, Object... parametros) throws SQLException {
        List<T> resultados = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            
            // Establecer parámetros
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                T entity = mapearResultSet(rs);
                resultados.add(entity);
            }
            
        } finally {
            cerrarRecursos(rs, stmt, conn);
        }
        
        return resultados;
    }
    
    /**
     * Ejecuta una consulta que devuelve un solo resultado
     * @param sql La consulta SQL
     * @param parametros Los parámetros de la consulta
     * @return La entidad encontrada o null
     * @throws SQLException si hay error
     */
    protected T ejecutarConsultaUnica(String sql, Object... parametros) throws SQLException {
        List<T> resultados = ejecutarConsulta(sql, parametros);
        return resultados.isEmpty() ? null : resultados.get(0);
    }
    
    /**
     * Ejecuta una operación de actualización (INSERT, UPDATE, DELETE)
     * @param sql La consulta SQL
     * @param parametros Los parámetros de la consulta
     * @return Número de filas afectadas
     * @throws SQLException si hay error
     */
    protected int ejecutarActualizacion(String sql, Object... parametros) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            
            // Establecer parámetros
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            
            return stmt.executeUpdate();
            
        } finally {
            cerrarRecursos(stmt);
        }
    }
    
    /**
     * Ejecuta una inserción y devuelve la clave generada
     * @param sql La consulta SQL INSERT
     * @param parametros Los parámetros de la consulta
     * @return La clave generada o -1 si no hay clave generada
     * @throws SQLException si hay error
     */
    protected int ejecutarInsercionConClave(String sql, Object... parametros) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            // Establecer parámetros
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
            return -1;
            
        } finally {
            cerrarRecursos(rs, stmt, conn);
        }
    }
    
    /**
     * Verifica si existe una entidad con el ID especificado
     * @param id ID a verificar
     * @param tabla Nombre de la tabla
     * @return true si existe
     * @throws SQLException si hay error
     */
    protected boolean existe(int id, String tabla) throws SQLException {
        String sql = "SELECT 1 FROM " + tabla + " WHERE id = ? LIMIT 1";
        List<T> resultado = ejecutarConsulta(sql, id);
        return !resultado.isEmpty();
    }
    
    /**
     * Cuenta el número total de registros en una tabla
     * @param tabla Nombre de la tabla
     * @param condicion Condición WHERE opcional (puede ser null)
     * @param parametros Parámetros para la condición
     * @return Número de registros
     * @throws SQLException si hay error
     */
    protected int contar(String tabla, String condicion, Object... parametros) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ").append(tabla);
        
        if (condicion != null && !condicion.trim().isEmpty()) {
            sql.append(" WHERE ").append(condicion);
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql.toString());
            
            // Establecer parámetros
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            return 0;
            
        } finally {
            cerrarRecursos(rs, stmt, conn);
        }
    }
    
    /**
     * Ejecuta múltiples operaciones en una transacción
     * @param operaciones Lista de operaciones a ejecutar
     * @return true si todas las operaciones fueron exitosas
     */
    protected boolean ejecutarEnTransaccion(List<Runnable> operaciones) {
        Connection conn = null;
        
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            for (Runnable operacion : operaciones) {
                operacion.run();
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
            System.err.println("Error en transacción: " + e.getMessage());
            return false;
            
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Error restaurando autocommit: " + e.getMessage());
            }
        }
    }
    
    /**
     * Obtiene registros con paginación
     * @param sql Consulta SQL base
     * @param pagina Número de página (iniciando en 1)
     * @param tamañoPagina Número de registros por página
     * @param parametros Parámetros de la consulta
     * @return Lista paginada de entidades
     * @throws SQLException si hay error
     */
    protected List<T> obtenerPaginado(String sql, int pagina, int tamañoPagina, Object... parametros) throws SQLException {
        if (pagina < 1) pagina = 1;
        if (tamañoPagina < 1) tamañoPagina = 10;
        
        int offset = (pagina - 1) * tamañoPagina;
        String sqlPaginado = sql + " LIMIT ? OFFSET ?";
        
        Object[] parametrosCompletos = new Object[parametros.length + 2];
        System.arraycopy(parametros, 0, parametrosCompletos, 0, parametros.length);
        parametrosCompletos[parametros.length] = tamañoPagina;
        parametrosCompletos[parametros.length + 1] = offset;
        
        return ejecutarConsulta(sqlPaginado, parametrosCompletos);
    }
    
    /**
     * Valida que los parámetros obligatorios no sean nulos
     * @param parametros Array de parámetros a validar
     * @throws IllegalArgumentException si algún parámetro es nulo
     */
    protected void validarParametrosObligatorios(Object... parametros) {
        for (int i = 0; i < parametros.length; i++) {
            if (parametros[i] == null) {
                throw new IllegalArgumentException("El parámetro " + (i + 1) + " no puede ser nulo");
            }
        }
    }
    
    /**
     * Convierte un LocalDateTime a Timestamp para la base de datos
     */
    protected Timestamp convertirATimestamp(java.time.LocalDateTime dateTime) {
        return dateTime != null ? Timestamp.valueOf(dateTime) : null;
    }
    
    /**
     * Convierte un LocalDate a java.sql.Date
     */
    protected java.sql.Date convertirADate(java.time.LocalDate date) {
        return date != null ? java.sql.Date.valueOf(date) : null;
    }
    
    /**
     * Convierte un LocalTime a java.sql.Time
     */
    protected java.sql.Time convertirATime(java.time.LocalTime time) {
        return time != null ? java.sql.Time.valueOf(time) : null;
    }
}