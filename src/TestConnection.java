import utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Clase para probar la conexión a la base de datos MySQL
 * Verifica que la configuración esté correcta y la BD responda
 */
public class TestConnection {
    
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("    PRUEBA DE CONEXIÓN - HOSPITAL SANTA VIDA");
        System.out.println("=================================================");
        
        DatabaseConnection dbConnection = null;
        
        try {
            // Obtener instancia de la conexión
            System.out.println("1. Obteniendo instancia de DatabaseConnection...");
            dbConnection = DatabaseConnection.getInstance();
            System.out.println("   ✅ Instancia obtenida correctamente");
            
            // Mostrar información de configuración
            System.out.println("\n2. Información de configuración:");
            System.out.println("   URL: " + dbConnection.getUrl());
            System.out.println("   Usuario: " + dbConnection.getUsername());
            System.out.println("   Config válida: " + dbConnection.hasValidConfig());
            
            // Probar conexión básica
            System.out.println("\n3. Probando conexión básica...");
            boolean isConnected = dbConnection.testConnection();
            if (isConnected) {
                System.out.println("   ✅ Conexión básica exitosa");
            } else {
                System.out.println("   ❌ Error en conexión básica");
                return;
            }
            
            // Obtener información detallada de la BD
            System.out.println("\n4. Información detallada de la base de datos:");
            dbConnection.printDatabaseInfo();
            
            // Probar consulta simple
            System.out.println("\n5. Probando consulta SQL...");
            Connection conn = dbConnection.getConnection();
            
            // Verificar si existe la base de datos hospital_santa_vida
            try (ResultSet rs = dbConnection.executeQuery("SHOW DATABASES LIKE 'hospital_santa_vida'")) {
                if (rs.next()) {
                    System.out.println("   ✅ Base de datos 'hospital_santa_vida' encontrada");
                    
                    // Verificar tablas existentes
                    System.out.println("\n6. Verificando tablas existentes...");
                    try (ResultSet tables = dbConnection.executeQuery("SHOW TABLES")) {
                        int tableCount = 0;
                        System.out.println("   Tablas encontradas:");
                        while (tables.next()) {
                            String tableName = tables.getString(1);
                            System.out.println("   - " + tableName);
                            tableCount++;
                        }
                        System.out.println("   Total de tablas: " + tableCount);
                        
                        if (tableCount == 0) {
                            System.out.println("   ⚠️ No hay tablas. Ejecuta el script SQL primero.");
                        }
                    }
                    
                } else {
                    System.out.println("   ⚠️ Base de datos 'hospital_santa_vida' NO encontrada");
                    System.out.println("   📝 Debes crear la base de datos primero con el script SQL");
                }
            }
            
            // Probar estado de conexión
            System.out.println("\n7. Estado de la conexión:");
            System.out.println("   Conectado: " + dbConnection.isConnected());
            System.out.println("   Conexión válida: " + (conn != null && !conn.isClosed()));
            
            System.out.println("\n=================================================");
            System.out.println("    ✅ PRUEBA DE CONEXIÓN COMPLETADA EXITOSAMENTE");
            System.out.println("=================================================");
            
        } catch (SQLException e) {
            System.err.println("\n❌ ERROR DE SQL:");
            System.err.println("   Código: " + e.getErrorCode());
            System.err.println("   Estado: " + e.getSQLState());
            System.err.println("   Mensaje: " + e.getMessage());
            
            // Sugerencias según el tipo de error
            if (e.getErrorCode() == 1049) {
                System.err.println("\n💡 SOLUCIÓN: La base de datos no existe.");
                System.err.println("   1. Conecta a MySQL sin especificar base de datos");
                System.err.println("   2. Ejecuta: CREATE DATABASE hospital_santa_vida;");
                System.err.println("   3. Ejecuta el script SQL completo");
            } else if (e.getErrorCode() == 1045) {
                System.err.println("\n💡 SOLUCIÓN: Credenciales incorrectas.");
                System.err.println("   Verifica usuario y contraseña en DatabaseConnection.java");
            } else if (e.getErrorCode() == 0) {
                System.err.println("\n💡 SOLUCIÓN: MySQL server no está ejecutándose.");
                System.err.println("   Inicia MySQL Server en tu sistema");
            }
            
        } catch (Exception e) {
            System.err.println("\n❌ ERROR GENERAL:");
            System.err.println("   Tipo: " + e.getClass().getSimpleName());
            System.err.println("   Mensaje: " + e.getMessage());
            e.printStackTrace();
            
        } finally {
            // Limpiar recursos
            if (dbConnection != null) {
                System.out.println("\n8. Cerrando conexión...");
                dbConnection.closeConnection();
                System.out.println("   ✅ Conexión cerrada correctamente");
            }
        }
    }
    
    /**
     * Método adicional para probar operaciones específicas
     */
    public static void testSpecificOperations() {
        System.out.println("\n=== PRUEBAS ESPECÍFICAS ===");
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        
        try {
            // Probar inserción simple (si las tablas existen)
            System.out.println("Probando operaciones CRUD...");
            
            // Solo si existe la tabla usuarios
            try (ResultSet rs = db.executeQuery("SHOW TABLES LIKE 'usuarios'")) {
                if (rs.next()) {
                    System.out.println("✅ Tabla 'usuarios' encontrada");
                    
                    // Contar usuarios existentes
                    try (ResultSet count = db.executeQuery("SELECT COUNT(*) as total FROM usuarios")) {
                        if (count.next()) {
                            int total = count.getInt("total");
                            System.out.println("   Total de usuarios: " + total);
                        }
                    }
                } else {
                    System.out.println("⚠️ Tabla 'usuarios' no encontrada");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error en pruebas específicas: " + e.getMessage());
        }
    }
}