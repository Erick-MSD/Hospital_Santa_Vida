package utils;

import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Clase utilitaria para el manejo seguro de contraseñas
 * Implementa hashing con BCrypt-like utilizando PBKDF2 con SHA-256
 * Incluye validación de fortaleza de contraseñas y generación segura
 */
public class PasswordUtils {
    
    // Constantes para el algoritmo de hashing
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 10000; // Número de iteraciones para PBKDF2
    private static final int SALT_LENGTH = 16;   // Longitud del salt en bytes
    private static final int HASH_LENGTH = 32;   // Longitud del hash en bytes
    
    // Patrones para validación de contraseñas
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    
    // Generador seguro de números aleatorios
    private static final SecureRandom RANDOM = new SecureRandom();
    
    /**
     * Genera un hash seguro de la contraseña
     * @param password La contraseña en texto plano
     * @return String con el hash en formato: salt:hash (Base64)
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede ser nula o vacía");
        }
        
        try {
            // Generar salt aleatorio
            byte[] salt = generarSalt();
            
            // Generar hash usando PBKDF2
            byte[] hash = generarHash(password.toCharArray(), salt);
            
            // Codificar salt y hash en Base64 y combinarlos
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);
            
            return saltBase64 + ":" + hashBase64;
            
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error al generar hash de contraseña", e);
        }
    }
    
    /**
     * Verifica si una contraseña coincide con su hash
     * @param password La contraseña en texto plano
     * @param hashedPassword El hash almacenado en formato salt:hash
     * @return true si la contraseña es correcta, false en caso contrario
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        if (password == null || hashedPassword == null) {
            return false;
        }
        
        try {
            // Separar salt y hash
            String[] parts = hashedPassword.split(":");
            if (parts.length != 2) {
                return false; // Formato inválido
            }
            
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHash = Base64.getDecoder().decode(parts[1]);
            
            // Generar hash de la contraseña proporcionada con el mismo salt
            byte[] testHash = generarHash(password.toCharArray(), salt);
            
            // Comparar hashes de forma segura (evitar timing attacks)
            return compararArraysSeguro(storedHash, testHash);
            
        } catch (Exception e) {
            // En caso de cualquier error, retornar false
            return false;
        }
    }
    
    /**
     * Valida la fortaleza de una contraseña
     * @param password La contraseña a validar
     * @return ValidationResult con el resultado de la validación
     */
    public static ValidationResult validarFortaleza(String password) {
        ValidationResult result = new ValidationResult();
        
        if (password == null) {
            result.setValida(false);
            result.addError("La contraseña no puede ser nula");
            return result;
        }
        
        // Validar longitud mínima
        if (password.length() < 8) {
            result.addError("La contraseña debe tener al menos 8 caracteres");
        }
        
        // Validar longitud máxima (evitar ataques DoS)
        if (password.length() > 128) {
            result.addError("La contraseña no puede exceder 128 caracteres");
        }
        
        // Validar presencia de mayúsculas
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            result.addError("Debe contener al menos una letra mayúscula");
        }
        
        // Validar presencia de minúsculas
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            result.addError("Debe contener al menos una letra minúscula");
        }
        
        // Validar presencia de dígitos
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            result.addError("Debe contener al menos un dígito");
        }
        
        // Validar presencia de caracteres especiales
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            result.addError("Debe contener al menos un carácter especial");
        }
        
        // Validar patrones comunes débiles
        if (contienePatronesDebiles(password)) {
            result.addError("La contraseña contiene patrones comunes débiles");
        }
        
        // Calcular puntuación de fortaleza
        int puntuacion = calcularPuntuacionFortaleza(password);
        result.setPuntuacion(puntuacion);
        
        if (puntuacion < 60) {
            result.setNivel(ValidationResult.Nivel.DEBIL);
        } else if (puntuacion < 80) {
            result.setNivel(ValidationResult.Nivel.MEDIA);
        } else {
            result.setNivel(ValidationResult.Nivel.FUERTE);
        }
        
        result.setValida(result.getErrores().isEmpty());
        
        return result;
    }
    
    /**
     * Genera una contraseña segura aleatoria
     * @param longitud La longitud deseada de la contraseña
     * @param incluirEspeciales Si incluir caracteres especiales
     * @return String con la contraseña generada
     */
    public static String generarPasswordSegura(int longitud, boolean incluirEspeciales) {
        if (longitud < 8) {
            throw new IllegalArgumentException("La longitud mínima es 8 caracteres");
        }
        if (longitud > 128) {
            throw new IllegalArgumentException("La longitud máxima es 128 caracteres");
        }
        
        String mayusculas = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String minusculas = "abcdefghijklmnopqrstuvwxyz";
        String digitos = "0123456789";
        String especiales = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        
        StringBuilder charset = new StringBuilder();
        charset.append(mayusculas).append(minusculas).append(digitos);
        
        if (incluirEspeciales) {
            charset.append(especiales);
        }
        
        StringBuilder password = new StringBuilder();
        
        // Garantizar al menos un carácter de cada tipo
        password.append(mayusculas.charAt(RANDOM.nextInt(mayusculas.length())));
        password.append(minusculas.charAt(RANDOM.nextInt(minusculas.length())));
        password.append(digitos.charAt(RANDOM.nextInt(digitos.length())));
        
        if (incluirEspeciales) {
            password.append(especiales.charAt(RANDOM.nextInt(especiales.length())));
        }
        
        // Completar con caracteres aleatorios
        String charsetStr = charset.toString();
        for (int i = password.length(); i < longitud; i++) {
            password.append(charsetStr.charAt(RANDOM.nextInt(charsetStr.length())));
        }
        
        // Mezclar la contraseña para evitar patrones predecibles
        return mezclarString(password.toString());
    }
    
    /**
     * Genera una contraseña temporal para nuevos usuarios
     * @return String con contraseña temporal de 12 caracteres
     */
    public static String generarPasswordTemporal() {
        return generarPasswordSegura(12, true);
    }
    
    /**
     * Verifica si la contraseña ha expirado
     * @param fechaUltimoCambio Timestamp de la última modificación
     * @param diasExpiracion Días hasta expiración
     * @return true si ha expirado
     */
    public static boolean haExpirado(java.time.LocalDateTime fechaUltimoCambio, int diasExpiracion) {
        if (fechaUltimoCambio == null) {
            return true; // Si no hay fecha, se considera expirada
        }
        
        java.time.LocalDateTime fechaExpiracion = fechaUltimoCambio.plusDays(diasExpiracion);
        return java.time.LocalDateTime.now().isAfter(fechaExpiracion);
    }
    
    // Métodos auxiliares privados
    
    /**
     * Genera salt aleatorio
     */
    private static byte[] generarSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return salt;
    }
    
    /**
     * Genera hash usando PBKDF2
     */
    private static byte[] generarHash(char[] password, byte[] salt) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, HASH_LENGTH * 8);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        
        try {
            return factory.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword(); // Limpiar password de memoria
        }
    }
    
    /**
     * Compara arrays de bytes de forma segura (evita timing attacks)
     */
    private static boolean compararArraysSeguro(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        
        return result == 0;
    }
    
    /**
     * Detecta patrones comunes débiles en contraseñas
     */
    private static boolean contienePatronesDebiles(String password) {
        String lower = password.toLowerCase();
        
        // Patrones comunes
        String[] patronesDebiles = {
            "123456", "password", "admin", "qwerty", "abc123", 
            "111111", "123123", "password123", "admin123",
            "hospital", "medico", "doctor", "enfermera"
        };
        
        for (String patron : patronesDebiles) {
            if (lower.contains(patron)) {
                return true;
            }
        }
        
        // Detectar secuencias repetitivas
        return tieneSecuenciasRepetitivas(password);
    }
    
    /**
     * Detecta secuencias repetitivas en la contraseña
     */
    private static boolean tieneSecuenciasRepetitivas(String password) {
        // Verificar caracteres repetidos consecutivos
        int repeticiones = 1;
        for (int i = 1; i < password.length(); i++) {
            if (password.charAt(i) == password.charAt(i - 1)) {
                repeticiones++;
                if (repeticiones >= 3) {
                    return true; // 3+ caracteres consecutivos iguales
                }
            } else {
                repeticiones = 1;
            }
        }
        
        return false;
    }
    
    /**
     * Calcula puntuación de fortaleza de 0 a 100
     */
    private static int calcularPuntuacionFortaleza(String password) {
        int puntuacion = 0;
        
        // Puntos por longitud
        puntuacion += Math.min(password.length() * 2, 20);
        
        // Puntos por variedad de caracteres
        if (UPPERCASE_PATTERN.matcher(password).matches()) puntuacion += 15;
        if (LOWERCASE_PATTERN.matcher(password).matches()) puntuacion += 15;
        if (DIGIT_PATTERN.matcher(password).matches()) puntuacion += 15;
        if (SPECIAL_CHAR_PATTERN.matcher(password).matches()) puntuacion += 20;
        
        // Puntos por complejidad adicional
        if (password.length() >= 12) puntuacion += 10;
        if (!contienePatronesDebiles(password)) puntuacion += 15;
        
        return Math.min(puntuacion, 100);
    }
    
    /**
     * Mezcla los caracteres de un string aleatoriamente
     */
    private static String mezclarString(String input) {
        char[] chars = input.toCharArray();
        
        // Algoritmo Fisher-Yates para mezclar
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        
        return new String(chars);
    }
    
    /**
     * Clase para encapsular el resultado de validación de contraseña
     */
    public static class ValidationResult {
        public enum Nivel { DEBIL, MEDIA, FUERTE }
        
        private boolean valida;
        private Nivel nivel;
        private int puntuacion;
        private java.util.List<String> errores;
        
        public ValidationResult() {
            this.errores = new java.util.ArrayList<>();
            this.valida = true;
            this.nivel = Nivel.DEBIL;
            this.puntuacion = 0;
        }
        
        // Getters y Setters
        public boolean isValida() { return valida; }
        public void setValida(boolean valida) { this.valida = valida; }
        
        public Nivel getNivel() { return nivel; }
        public void setNivel(Nivel nivel) { this.nivel = nivel; }
        
        public int getPuntuacion() { return puntuacion; }
        public void setPuntuacion(int puntuacion) { this.puntuacion = puntuacion; }
        
        public java.util.List<String> getErrores() { return errores; }
        public void addError(String error) { this.errores.add(error); }
        
        public String getMensajeErrores() {
            return String.join(", ", errores);
        }
        
        @Override
        public String toString() {
            return String.format("Validación{válida=%s, nivel=%s, puntuación=%d, errores=%d}", 
                               valida, nivel, puntuacion, errores.size());
        }
    }
    
    /**
     * Método de utilidad para crear hash compatible con sistemas legacy
     * Usado principalmente para migración de datos existentes
     */
    public static String hashPasswordLegacy(String password) {
        // Implementación simplificada para compatibilidad
        return "$2a$10$" + hashPassword(password);
    }
    
    /**
     * Método para verificar hashes legacy
     */
    public static boolean verifyPasswordLegacy(String password, String hashedPassword) {
        if (hashedPassword.startsWith("$2a$10$")) {
            return verifyPassword(password, hashedPassword.substring(7));
        }
        return verifyPassword(password, hashedPassword);
    }
    
    /**
     * Verifica si un hash de contraseña es válido
     * @param passwordHash El hash a verificar
     * @return true si el hash tiene el formato correcto
     */
    public static boolean esHashValido(String passwordHash) {
        if (passwordHash == null || passwordHash.isEmpty()) {
            return false;
        }
        
        // Verificar formato básico (salt:hash en Base64)
        String[] partes = passwordHash.split(":");
        if (partes.length != 2) {
            return false;
        }
        
        try {
            // Intentar decodificar el salt y hash
            Base64.getDecoder().decode(partes[0]);
            Base64.getDecoder().decode(partes[1]);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Verifica una contraseña contra su hash
     * @param password La contraseña en texto plano
     * @param passwordHash El hash almacenado
     * @return true si la contraseña coincide con el hash
     */
    public static boolean verificarPassword(String password, String passwordHash) {
        return verifyPassword(password, passwordHash);
    }
}