package utils;

import java.sql.*;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Utility class para manejar conexiones a la base de datos MySQL
 * Implementa patrón Singleton para gestionar la conexión de forma eficiente
 */
public class DatabaseConnection {
    
    private static DatabaseConnection instance;
    private Connection connection;
    private String url;
    private String username;
    private String password;
    private String driver;
    
    // Configuración por defecto
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/hospital_santa_vida?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USERNAME = "hospital_user";
    private static final String DEFAULT_PASSWORD = "hospital_pass123";
    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    // Constructor privado para patrón Singleton
    private DatabaseConnection() {
        try {
            // Intentar cargar configuración desde archivo de propiedades
            loadConfigFromFile();
            
            // Si no se pudo cargar del archivo, usar valores por defecto
            if (url == null) {
                url = DEFAULT_URL;
                username = DEFAULT_USERNAME;
                password = DEFAULT_PASSWORD;
                driver = DEFAULT_DRIVER;
            }
            
            // Cargar el driver de MySQL
            Class.forName(driver);
            
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Driver de MySQL no encontrado.");
            System.err.println("Asegúrate de tener mysql-connector-java.jar en el classpath.");
            e.printStackTrace();
        }
    }
    
    /**
     * Obtiene la instancia única de DatabaseConnection (Singleton)
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Carga la configuración desde un archivo de propiedades
     */
    private void loadConfigFromFile() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                
                url = prop.getProperty("db.url");
                username = prop.getProperty("db.username");
                password = prop.getProperty("db.password");
                driver = prop.getProperty("db.driver");
                
                System.out.println("Configuración de BD cargada desde archivo de propiedades");
            }
        } catch (IOException e) {
            System.out.println("No se pudo cargar archivo de propiedades. Usando configuración por defecto.");
        }
    }
    
    /**
     * Obtiene una conexión activa a la base de datos
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("Conexión a MySQL establecida exitosamente");
                
                // Configurar la conexión para UTF-8
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("SET NAMES utf8mb4");
                    stmt.execute("SET CHARACTER SET utf8mb4");
                    stmt.execute("SET character_set_connection=utf8mb4");
                }
                
            } catch (SQLException e) {
                System.err.println("Error al conectar con la base de datos:");
                System.err.println("URL: " + url);
                System.err.println("Usuario: " + username);
                System.err.println("Error: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }
    
    /**
     * Cierra la conexión a la base de datos
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Conexión a MySQL cerrada");
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
    
    /**
     * Verifica si la conexión está activa
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Ejecuta una consulta SELECT y retorna el ResultSet
     */
    public ResultSet executeQuery(String sql) throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }
    
    /**
     * Ejecuta una consulta SELECT con parámetros usando PreparedStatement
     */
    public ResultSet executeQuery(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        
        // Establecer parámetros
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
        
        return pstmt.executeQuery();
    }
    
    /**
     * Ejecuta una consulta INSERT, UPDATE o DELETE
     */
    public int executeUpdate(String sql) throws SQLException {
        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }
    
    /**
     * Ejecuta una consulta INSERT, UPDATE o DELETE con parámetros
     */
    public int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Establecer parámetros
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return pstmt.executeUpdate();
        }
    }
    
    /**
     * Ejecuta un INSERT y retorna el ID generado
     */
    public int executeInsertWithGeneratedKey(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Establecer parámetros
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Error al insertar, no se afectaron filas.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Error al insertar, no se obtuvo ID.");
                }
            }
        }
    }
    
    /**
     * Inicia una transacción
     */
    public void beginTransaction() throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
    }
    
    /**
     * Confirma una transacción
     */
    public void commitTransaction() throws SQLException {
        if (connection != null) {
            connection.commit();
            connection.setAutoCommit(true);
        }
    }
    
    /**
     * Revierte una transacción
     */
    public void rollbackTransaction() throws SQLException {
        if (connection != null) {
            connection.rollback();
            connection.setAutoCommit(true);
        }
    }
    
    /**
     * Verifica la conectividad con la base de datos
     */
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT 1")) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    System.out.println("Prueba de conexión exitosa");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en prueba de conexión: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Obtiene información de la base de datos
     */
    public void printDatabaseInfo() {
        try {
            Connection conn = getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            
            System.out.println("=== INFORMACIÓN DE LA BASE DE DATOS ===");
            System.out.println("URL: " + metaData.getURL());
            System.out.println("Usuario: " + metaData.getUserName());
            System.out.println("Producto: " + metaData.getDatabaseProductName());
            System.out.println("Versión: " + metaData.getDatabaseProductVersion());
            System.out.println("Driver: " + metaData.getDriverName());
            System.out.println("Versión Driver: " + metaData.getDriverVersion());
            System.out.println("=====================================");
            
        } catch (SQLException e) {
            System.err.println("Error al obtener información de BD: " + e.getMessage());
        }
    }
    
    /**
     * Cierra recursos (ResultSet, Statement, etc.)
     */
    public static void closeResources(ResultSet rs, Statement stmt) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar ResultSet: " + e.getMessage());
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar Statement: " + e.getMessage());
            }
        }
    }
    
    /**
     * Cierra recursos con Connection
     */
    public static void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        closeResources(rs, stmt);
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar Connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Método para configurar manualmente la conexión (útil para testing)
     */
    public void setConnectionParams(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        
        // Cerrar conexión existente para forzar reconexión
        closeConnection();
    }
    
    // Getters para información de conexión (sin revelar password)
    public String getUrl() {
        return url;
    }
    
    public String getUsername() {
        return username;
    }
    
    public boolean hasValidConfig() {
        return url != null && username != null && password != null;
    }
}