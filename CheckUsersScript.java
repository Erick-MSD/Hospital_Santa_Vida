import java.sql.*;
import utils.DatabaseConnection;

/**
 * Script para verificar los usuarios y sus permisos en la base de datos
 */
public class CheckUsersScript {
    
    public static void main(String[] args) {
        System.out.println("=== VERIFICACIÓN DE USUARIOS Y PERMISOS ===");
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            // Conectar a la base de datos
            conn = DatabaseConnection.obtenerConexion();
            System.out.println("✓ Conectado a la base de datos");
            
            // Consultar todos los usuarios
            String sql = "SELECT id, username, email, tipo_usuario, nombre_completo, activo FROM usuarios ORDER BY id";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            System.out.println("\n=== USUARIOS EN LA BASE DE DATOS ===");
            System.out.printf("%-5s %-15s %-25s %-20s %-25s %-8s%n", 
                "ID", "USERNAME", "EMAIL", "TIPO_USUARIO", "NOMBRE", "ACTIVO");
            System.out.println("".repeat(100));
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String tipoUsuario = rs.getString("tipo_usuario");
                String nombreCompleto = rs.getString("nombre_completo");
                boolean activo = rs.getBoolean("activo");
                
                System.out.printf("%-5d %-15s %-25s %-20s %-25s %-8s%n", 
                    id, username, email, tipoUsuario, nombreCompleto, activo ? "SÍ" : "NO");
            }
            
            System.out.println("\n=== ESTRUCTURA DE LA TABLA USUARIOS ===");
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "usuarios", null);
            
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                boolean nullable = columns.getBoolean("NULLABLE");
                
                System.out.printf("%-20s %-15s %-10d %-8s%n", 
                    columnName, columnType, columnSize, nullable ? "NULL" : "NOT NULL");
            }
            columns.close();
            
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
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
                System.out.println("✓ Conexión cerrada");
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}