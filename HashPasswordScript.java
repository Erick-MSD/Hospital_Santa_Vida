import java.sql.*;
import utils.DatabaseConnection;
import utils.PasswordUtils;

/**
 * Script para hashear todas las contraseñas existentes en texto plano en la base de datos
 */
public class HashPasswordScript {
    
    public static void main(String[] args) {
        System.out.println("=== SCRIPT DE HASHING DE CONTRASEÑAS ===");
        System.out.println("Iniciando proceso de hash de contraseñas...");
        
        Connection conn = null;
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        
        try {
            // Conectar a la base de datos
            conn = DatabaseConnection.obtenerConexion();
            System.out.println("✓ Conectado a la base de datos");
            
            // Preparar consultas
            String selectSQL = "SELECT id, username, password_hash FROM usuarios";
            String updateSQL = "UPDATE usuarios SET password_hash = ? WHERE id = ?";
            
            selectStmt = conn.prepareStatement(selectSQL);
            updateStmt = conn.prepareStatement(updateSQL);
            
            // Obtener todos los usuarios
            rs = selectStmt.executeQuery();
            
            int totalUsuarios = 0;
            int usuariosActualizados = 0;
            
            while (rs.next()) {
                totalUsuarios++;
                int idUsuario = rs.getInt("id");
                String usuario = rs.getString("username");
                String passwordActual = rs.getString("password_hash");
                
                System.out.println("\nProcesando usuario: " + usuario);
                System.out.println("Password actual: " + passwordActual);
                
                // Verificar si ya está hasheada (contiene ':' que indica formato salt:hash)
                if (passwordActual.contains(":")) {
                    System.out.println("→ Ya está hasheada, omitiendo...");
                    continue;
                }
                
                // Hashear la contraseña en texto plano
                String hashedPassword = PasswordUtils.hashPassword(passwordActual);
                System.out.println("→ Password hasheada: " + hashedPassword.substring(0, 20) + "...");
                
                // Actualizar en la base de datos
                updateStmt.setString(1, hashedPassword);
                updateStmt.setInt(2, idUsuario);
                
                int rowsUpdated = updateStmt.executeUpdate();
                if (rowsUpdated > 0) {
                    usuariosActualizados++;
                    System.out.println("✓ Usuario actualizado correctamente");
                } else {
                    System.out.println("✗ Error al actualizar usuario");
                }
            }
            
            System.out.println("\n=== RESUMEN ===");
            System.out.println("Total de usuarios procesados: " + totalUsuarios);
            System.out.println("Usuarios actualizados: " + usuariosActualizados);
            System.out.println("Proceso completado exitosamente");
            
        } catch (SQLException e) {
            System.err.println("Error SQL: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error general: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cerrar recursos
            try {
                if (rs != null) rs.close();
                if (selectStmt != null) selectStmt.close();
                if (updateStmt != null) updateStmt.close();
                if (conn != null) conn.close();
                System.out.println("✓ Conexión cerrada");
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}