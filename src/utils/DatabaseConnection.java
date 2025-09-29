package utils;

import java.sql.*;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Clase utilitaria para gestionar la conexión a la base de datos MySQL
 * Implementa patrón Singleton para garantizar una sola instancia
 * Maneja pool de conexiones básico y configuración flexible
 */
public class DatabaseConnection {
    
    // Instancia singleton
    private static DatabaseConnection instance;
    
    // Configuración de conexión
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DATABASE = "hospital_santa_vida";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "Erick1234";
    
    // Configuración actual
    private String host;
    private String port;
    private String database;
    private String username;
    private String password;
    private String url;
    
    // Estado de la conexión
    private Connection connection;
    private boolean connected;
    
    /**
     * Constructor privado para implementar Singleton
     */
    private DatabaseConnection() {
        cargarConfiguracion();
        construirUrl();
    }
    
    /**
     * Obtiene la instancia singleton de DatabaseConnection
     * @return La instancia única de DatabaseConnection
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }
    
    /**
     * Carga la configuración de la base de datos
     * Intenta cargar desde archivo de propiedades, usa valores por defecto si no existe
     */
    private void cargarConfiguracion() {
        Properties props = new Properties();
        
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input != null) {
                props.load(input);
                
                this.host = props.getProperty("db.host", DEFAULT_HOST);
                this.port = props.getProperty("db.port", DEFAULT_PORT);
                this.database = props.getProperty("db.database", DEFAULT_DATABASE);
                this.username = props.getProperty("db.username", DEFAULT_USERNAME);
                this.password = props.getProperty("db.password", DEFAULT_PASSWORD);
            } else {
                // Usar configuración por defecto
                usarConfiguracionPorDefecto();
            }
        } catch (IOException e) {
            System.err.println("Error al cargar configuración de BD: " + e.getMessage());
            usarConfiguracionPorDefecto();
        }
    }
    
    /**
     * Establece configuración por defecto
     */
    private void usarConfiguracionPorDefecto() {
        this.host = DEFAULT_HOST;
        this.port = DEFAULT_PORT;
        this.database = DEFAULT_DATABASE;
        this.username = DEFAULT_USERNAME;
        this.password = DEFAULT_PASSWORD;
    }
    
    /**
     * Construye la URL de conexión a MySQL
     */
    private void construirUrl() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("jdbc:mysql://")
                  .append(host)
                  .append(":")
                  .append(port)
                  .append("/")
                  .append(database);
        
        // Parámetros adicionales para MySQL 8.0+
        urlBuilder.append("?useSSL=false")
                  .append("&serverTimezone=UTC")
                  .append("&allowPublicKeyRetrieval=true")
                  .append("&useUnicode=true")
                  .append("&characterEncoding=utf8");
        
        this.url = urlBuilder.toString();
    }
    
    /**
     * Establece conexión con la base de datos
     * @return true si se conectó exitosamente, false en caso contrario
     */
    public boolean conectar() {
        try {
            System.out.println("[DB] Classpath actual: " + System.getProperty("java.class.path"));
            System.out.println("[DB] Intentando cargar driver MySQL 'com.mysql.cj.jdbc.Driver'");
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[DB] Driver cargado OK");
            
            // Establecer conexión
            Properties connProps = new Properties();
            connProps.put("user", username);
            connProps.put("password", password);
            
            this.connection = DriverManager.getConnection(url, connProps);
            this.connected = true;
            
            System.out.println("Conexión exitosa a la base de datos: " + database);
            return true;
            
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] NO se encontró el driver en el classpath");
            System.err.println(e);
            this.connected = false;
            return false;
            
        } catch (SQLException e) {
            System.err.println("[DB] SQLException al conectar: " + e.getMessage());
            this.connected = false;
            return false;
        }
    }
    
    /**
     * Obtiene la conexión activa
     * Intenta reconectar si la conexión se perdió
     * @return Connection activa o null si no se puede conectar
     */
    public Connection getConnection() {
        try {
            // Verificar si la conexión sigue activa
            if (connection == null || connection.isClosed() || !connection.isValid(5)) {
                conectar();
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("Error al verificar conexión: " + e.getMessage());
            conectar();
            return connection;
        }
    }
    
    /**
     * Método estático para obtener conexión - usado por DAOs
     */
    public static Connection getStaticConnection() throws SQLException {
        return getInstance().getConnection();
    }
    
    /**
     * Método estático de conveniencia para usar en aplicación principal
     */
    public static Connection obtenerConexion() throws SQLException {
        return getInstance().getConnection();
    }
    
    /**
     * Cierra la conexión con la base de datos
     */
    public void desconectar() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                this.connected = false;
                System.out.println("Conexión cerrada exitosamente");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }
    
    /**
     * Verifica si hay conexión activa con la base de datos
     * @return true si está conectado, false en caso contrario
     */
    public boolean estaConectado() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Prueba la conexión ejecutando una consulta simple
     * @return true si la prueba fue exitosa
     */
    public boolean probarConexion() {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT 1");
                boolean resultado = rs.next();
                rs.close();
                stmt.close();
                return resultado;
            }
        } catch (SQLException e) {
            System.err.println("Error en prueba de conexión: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Ejecuta un script SQL (útil para inicialización)
     * @param scriptPath Ruta del archivo SQL
     * @return true si se ejecutó exitosamente
     */
    public boolean ejecutarScript(String scriptPath) {
        try {
            Connection conn = getConnection();
            if (conn == null) return false;
            
            // Leer script desde recursos
            InputStream input = getClass().getClassLoader().getResourceAsStream(scriptPath);
            if (input == null) {
                System.err.println("Script no encontrado: " + scriptPath);
                return false;
            }
            
            // Leer contenido del script
            Scanner scanner = new Scanner(input);
            scanner.useDelimiter("\\A");
            String script = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
            
            // Ejecutar script por bloques (separar por ;)
            String[] statements = script.split(";");
            Statement stmt = conn.createStatement();
            
            for (String sql : statements) {
                sql = sql.trim();
                if (!sql.isEmpty() && !sql.startsWith("--")) {
                    stmt.execute(sql);
                }
            }
            
            stmt.close();
            System.out.println("Script ejecutado exitosamente: " + scriptPath);
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error ejecutando script: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Inicia una transacción
     * @return true si se inició correctamente
     */
    public boolean iniciarTransaccion() {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                conn.setAutoCommit(false);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al iniciar transacción: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Confirma la transacción actual
     * @return true si se confirmó correctamente
     */
    public boolean confirmarTransaccion() {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                conn.commit();
                conn.setAutoCommit(true);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al confirmar transacción: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Revierte la transacción actual
     * @return true si se revirtió correctamente
     */
    public boolean revertirTransaccion() {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                conn.rollback();
                conn.setAutoCommit(true);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al revertir transacción: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Configuración manual de los parámetros de conexión
     * Útil para pruebas o configuración dinámica
     */
    public void configurar(String host, String port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        construirUrl();
    }
    
    /**
     * Obtiene información de la configuración actual
     * @return String con información de conexión (sin password)
     */
    public String obtenerInfoConexion() {
        return String.format("Host: %s:%s, Database: %s, User: %s, Connected: %s",
                           host, port, database, username, estaConectado());
    }
    
    /**
     * Obtiene estadísticas básicas de la base de datos
     * @return Map con estadísticas o null si hay error
     */
    public java.util.Map<String, Object> obtenerEstadisticas() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        try {
            Connection conn = getConnection();
            if (conn == null) return null;
            
            DatabaseMetaData metaData = conn.getMetaData();
            
            stats.put("database_product_name", metaData.getDatabaseProductName());
            stats.put("database_product_version", metaData.getDatabaseProductVersion());
            stats.put("driver_name", metaData.getDriverName());
            stats.put("driver_version", metaData.getDriverVersion());
            stats.put("url", url);
            stats.put("username", username);
            stats.put("connected", estaConectado());
            
            // Obtener número de tablas en la base de datos
            ResultSet tables = metaData.getTables(database, null, "%", new String[]{"TABLE"});
            int tableCount = 0;
            while (tables.next()) {
                tableCount++;
            }
            tables.close();
            stats.put("table_count", tableCount);
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo estadísticas: " + e.getMessage());
            return null;
        }
        
        return stats;
    }
    
    // Importar Scanner para el método ejecutarScript
    private static class Scanner {
        private final java.util.Scanner scanner;
        
        Scanner(InputStream input) {
            this.scanner = new java.util.Scanner(input, "UTF-8");
        }
        
        void useDelimiter(String pattern) {
            scanner.useDelimiter(pattern);
        }
        
        boolean hasNext() {
            return scanner.hasNext();
        }
        
        String next() {
            return scanner.next();
        }
        
        void close() {
            scanner.close();
        }
    }
    
    /**
     * Ejecuta una consulta de actualización (INSERT, UPDATE, DELETE)
     * @param sql La consulta SQL a ejecutar
     * @return El número de filas afectadas
     * @throws SQLException Si ocurre un error en la ejecución
     */
    public int executeUpdate(String sql) throws SQLException {
        if (!estaConectado()) {
            conectar();
        }
        
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }
    
    /**
     * Ejecuta una consulta de actualización con parámetros preparados
     * @param sql La consulta SQL con parámetros (?)
     * @param parametros Los valores de los parámetros
     * @return El número de filas afectadas
     * @throws SQLException Si ocurre un error en la ejecución
     */
    public int executeUpdate(String sql, Object... parametros) throws SQLException {
        if (!estaConectado()) {
            conectar();
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                pstmt.setObject(i + 1, parametros[i]);
            }
            return pstmt.executeUpdate();
        }
    }
    
    /**
     * Método estático para cerrar todas las conexiones
     */
    public static void closeAllConnections() {
        if (instance != null) {
            instance.desconectar();
            instance = null;
        }
    }
    
    @Override
    public String toString() {
        return "DatabaseConnection{" +
               "host='" + host + "'" +
               ", port='" + port + "'" +
               ", database='" + database + "'" +
               ", username='" + username + "'" +
               ", connected=" + estaConectado() +
               "}";
    }
}