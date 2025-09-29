import utils.PasswordUtils;

/**
 * Script temporal para hashear contraseñas existentes
 */
public class HashPasswords {
    public static void main(String[] args) {
        // Contraseñas que viste en la base de datos
        String[] passwords = {"password123", "pass123", "admin123"};
        
        System.out.println("=== HASHES PARA ACTUALIZAR EN LA BASE DE DATOS ===");
        System.out.println();
        
        for (String password : passwords) {
            String hashedPassword = PasswordUtils.hashPassword(password);
            System.out.println("Texto plano: " + password);
            System.out.println("Hash: " + hashedPassword);
            System.out.println("SQL UPDATE: UPDATE usuarios SET password_hash = '" + hashedPassword + "' WHERE password_hash = '" + password + "';");
            System.out.println("---");
        }
    }
}