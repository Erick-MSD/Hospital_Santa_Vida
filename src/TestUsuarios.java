import utils.DatabaseConnection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Test específico para verificar la tabla usuarios
 */
public class TestUsuarios {
    
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("     VERIFICACIÓN DE TABLA USUARIOS");
        System.out.println("==============================================");
        
        DatabaseConnection db = DatabaseConnection.getInstance();
        
        try {
            // Verificar estructura de la tabla
            System.out.println("1. Estructura de la tabla usuarios:");
            try (ResultSet rs = db.executeQuery("DESCRIBE usuarios")) {
                System.out.println("   Columnas:");
                while (rs.next()) {
                    String field = rs.getString("Field");
                    String type = rs.getString("Type");
                    String nullable = rs.getString("Null");
                    String key = rs.getString("Key");
                    String defaultValue = rs.getString("Default");
                    
                    System.out.printf("   - %-20s %-20s %-5s %-5s %s%n", 
                                    field, type, nullable, key, 
                                    (defaultValue != null ? defaultValue : ""));
                }
            }
            
            // Contar usuarios
            System.out.println("\n2. Cantidad de usuarios:");
            try (ResultSet rs = db.executeQuery("SELECT COUNT(*) as total FROM usuarios")) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    System.out.println("   Total de usuarios: " + total);
                }
            }
            
            // Mostrar todos los usuarios
            System.out.println("\n3. Lista de usuarios:");
            try (ResultSet rs = db.executeQuery("SELECT * FROM usuarios ORDER BY id")) {
                System.out.println("   +----+---------------+---------------------------+---------------+----------+");
                System.out.println("   | ID | Username      | Nombre Completo           | Tipo Usuario  | Estado   |");
                System.out.println("   +----+---------------+---------------------------+---------------+----------+");
                
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String username = rs.getString("username");
                    String nombreCompleto = rs.getString("nombre_completo");
                    String tipoUsuario = rs.getString("tipo_usuario");
                    String activo = rs.getBoolean("activo") ? "Activo" : "Inactivo";
                    
                    System.out.printf("   | %-2d | %-13s | %-25s | %-13s | %-8s |%n", 
                                    id, username, nombreCompleto, tipoUsuario, activo);
                }
                System.out.println("   +----+---------------+---------------------------+---------------+----------+");
            }
            
            // Verificar tipos de usuario únicos
            System.out.println("\n4. Tipos de usuario disponibles:");
            try (ResultSet rs = db.executeQuery("SELECT DISTINCT tipo_usuario, COUNT(*) as cantidad FROM usuarios GROUP BY tipo_usuario")) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo_usuario");
                    int cantidad = rs.getInt("cantidad");
                    System.out.println("   - " + tipo + " (" + cantidad + " usuarios)");
                }
            }
            
            // Verificar usuarios activos
            System.out.println("\n5. Estado de usuarios:");
            try (ResultSet rs = db.executeQuery("SELECT activo, COUNT(*) as cantidad FROM usuarios GROUP BY activo")) {
                while (rs.next()) {
                    boolean activo = rs.getBoolean("activo");
                    int cantidad = rs.getInt("cantidad");
                    String estado = activo ? "Activos" : "Inactivos";
                    System.out.println("   - " + estado + ": " + cantidad + " usuarios");
                }
            }
            
            System.out.println("\n==============================================");
            System.out.println("     ✅ VERIFICACIÓN COMPLETADA");
            System.out.println("==============================================");
            
        } catch (SQLException e) {
            System.err.println("❌ Error al verificar tabla usuarios:");
            System.err.println("   " + e.getMessage());
        } finally {
            db.closeConnection();
        }
    }
}