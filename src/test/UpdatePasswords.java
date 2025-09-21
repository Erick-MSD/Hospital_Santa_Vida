package test;
import utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Clase para actualizar las contraseñas en la base de datos
 */
public class UpdatePasswords {
    
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("    ACTUALIZANDO CONTRASEÑAS - HOSPITAL SANTA VIDA");
        System.out.println("=================================================");
        
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        
        try {
            // Actualizar todas las contraseñas a texto plano
            String updateSQL = "UPDATE usuarios SET password_hash = 'password123' WHERE password_hash LIKE '$2a%'";
            
            int rowsAffected = dbConnection.executeUpdate(updateSQL);
            System.out.println("✅ Contraseñas actualizadas: " + rowsAffected + " usuarios");
            
            // Verificar los usuarios
            System.out.println("\n📋 USUARIOS ACTUALIZADOS:");
            try (ResultSet rs = dbConnection.executeQuery("SELECT username, password_hash FROM usuarios")) {
                while (rs.next()) {
                    String username = rs.getString("username");
                    String password = rs.getString("password_hash");
                    System.out.println("   " + username + " -> " + password);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            dbConnection.closeConnection();
        }
    }
}