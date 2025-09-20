package services;

import dao.UsuarioDAO;
import models.Usuario;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Servicio de autenticación y autorización
 * Maneja login, logout, validación de sesiones y encriptación de contraseñas
 */
public class AuthenticationService {
    
    private final UsuarioDAO usuarioDAO;
    private Usuario usuarioActual;
    private LocalDateTime inicioSesion;
    private static final int TIEMPO_INACTIVIDAD_MINUTOS = 30;
    
    public AuthenticationService() {
        this.usuarioDAO = new UsuarioDAO();
        this.usuarioActual = null;
        this.inicioSesion = null;
    }
    
    /**
     * Autentica un usuario con username/email y contraseña
     */
    public boolean login(String usernameOEmail, String password) {
        try {
            // Encriptar la contraseña proporcionada
            String passwordHash = hashPassword(password);
            
            // Buscar y validar credenciales
            Usuario usuario = usuarioDAO.validarCredenciales(usernameOEmail, passwordHash);
            
            if (usuario != null) {
                this.usuarioActual = usuario;
                this.inicioSesion = LocalDateTime.now();
                
                System.out.println("Login exitoso para: " + usuario.getNombreCompleto());
                System.out.println("Tipo de usuario: " + usuario.getTipoUsuario());
                
                return true;
            } else {
                System.out.println("Credenciales inválidas para: " + usernameOEmail);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Error de base de datos durante login: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Error durante login: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cierra la sesión actual
     */
    public void logout() {
        if (usuarioActual != null) {
            System.out.println("Logout para: " + usuarioActual.getNombreCompleto());
        }
        
        this.usuarioActual = null;
        this.inicioSesion = null;
    }
    
    /**
     * Verifica si hay un usuario autenticado actualmente
     */
    public boolean isAuthenticated() {
        return usuarioActual != null && !sesionExpirada();
    }
    
    /**
     * Verifica si la sesión actual ha expirado por inactividad
     */
    public boolean sesionExpirada() {
        if (inicioSesion == null) {
            return true;
        }
        
        LocalDateTime tiempoLimite = inicioSesion.plusMinutes(TIEMPO_INACTIVIDAD_MINUTOS);
        return LocalDateTime.now().isAfter(tiempoLimite);
    }
    
    /**
     * Renueva la sesión actual (actualiza el tiempo de actividad)
     */
    public void renovarSesion() {
        if (isAuthenticated()) {
            this.inicioSesion = LocalDateTime.now();
        }
    }
    
    /**
     * Obtiene el usuario actualmente autenticado
     */
    public Usuario getUsuarioActual() {
        if (isAuthenticated()) {
            return usuarioActual;
        }
        return null;
    }
    
    /**
     * Verifica si el usuario actual tiene un tipo específico
     */
    public boolean hasRole(Usuario.TipoUsuario tipoRequerido) {
        Usuario usuario = getUsuarioActual();
        return usuario != null && usuario.getTipoUsuario() == tipoRequerido;
    }
    
    /**
     * Verifica si el usuario actual es administrador
     */
    public boolean isAdmin() {
        return hasRole(Usuario.TipoUsuario.ADMINISTRADOR);
    }
    
    /**
     * Verifica si el usuario actual puede realizar triage
     */
    public boolean canPerformTriage() {
        return hasRole(Usuario.TipoUsuario.MEDICO_TRIAGE);
    }
    
    /**
     * Verifica si el usuario actual puede registrar pacientes
     */
    public boolean canRegisterPatients() {
        return hasRole(Usuario.TipoUsuario.ASISTENTE_MEDICA);
    }
    
    /**
     * Verifica si el usuario actual puede realizar entrevista social
     */
    public boolean canPerformSocialInterview() {
        return hasRole(Usuario.TipoUsuario.TRABAJADOR_SOCIAL);
    }
    
    /**
     * Verifica si el usuario actual puede atender en urgencias
     */
    public boolean canAttendEmergencies() {
        return hasRole(Usuario.TipoUsuario.MEDICO_URGENCIAS);
    }
    
    /**
     * Verifica si el usuario actual puede ver estadísticas
     */
    public boolean canViewStatistics() {
        Usuario usuario = getUsuarioActual();
        return usuario != null && 
               (usuario.getTipoUsuario() == Usuario.TipoUsuario.ADMINISTRADOR ||
                usuario.getTipoUsuario() == Usuario.TipoUsuario.MEDICO_URGENCIAS);
    }
    
    /**
     * Requiere que el usuario esté autenticado, lanza excepción si no
     */
    public void requireAuthentication() throws SecurityException {
        if (!isAuthenticated()) {
            throw new SecurityException("Se requiere autenticación para esta operación");
        }
    }
    
    /**
     * Requiere un rol específico, lanza excepción si no lo tiene
     */
    public void requireRole(Usuario.TipoUsuario tipoRequerido) throws SecurityException {
        requireAuthentication();
        
        if (!hasRole(tipoRequerido)) {
            throw new SecurityException("Se requiere rol " + tipoRequerido + " para esta operación");
        }
    }
    
    /**
     * Cambia la contraseña del usuario actual
     */
    public boolean cambiarPassword(String passwordActual, String nuevoPassword) {
        try {
            requireAuthentication();
            
            // Verificar contraseña actual
            String passwordActualHash = hashPassword(passwordActual);
            if (!passwordActualHash.equals(usuarioActual.getPasswordHash())) {
                System.err.println("Contraseña actual incorrecta");
                return false;
            }
            
            // Validar nueva contraseña
            if (!esPasswordValido(nuevoPassword)) {
                System.err.println("La nueva contraseña no cumple con los requisitos de seguridad");
                return false;
            }
            
            // Actualizar contraseña
            String nuevoPasswordHash = hashPassword(nuevoPassword);
            boolean actualizado = usuarioDAO.actualizarPassword(usuarioActual.getId(), nuevoPasswordHash);
            
            if (actualizado) {
                usuarioActual.setPasswordHash(nuevoPasswordHash);
                System.out.println("Contraseña actualizada exitosamente");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error de base de datos al cambiar contraseña: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error al cambiar contraseña: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Crea un nuevo usuario (solo administradores)
     */
    public boolean crearUsuario(Usuario nuevoUsuario, String password) {
        try {
            requireRole(Usuario.TipoUsuario.ADMINISTRADOR);
            
            // Validar datos del nuevo usuario
            if (!validarDatosUsuario(nuevoUsuario, password)) {
                return false;
            }
            
            // Verificar que no exista username o email
            if (usuarioDAO.existeUsername(nuevoUsuario.getUsername())) {
                System.err.println("Ya existe un usuario con username: " + nuevoUsuario.getUsername());
                return false;
            }
            
            if (usuarioDAO.existeEmail(nuevoUsuario.getEmail())) {
                System.err.println("Ya existe un usuario con email: " + nuevoUsuario.getEmail());
                return false;
            }
            
            // Encriptar contraseña y establecer metadatos
            nuevoUsuario.setPasswordHash(hashPassword(password));
            nuevoUsuario.setCreatedBy(usuarioActual.getId());
            
            // Crear en base de datos
            int nuevoId = usuarioDAO.crear(nuevoUsuario);
            nuevoUsuario.setId(nuevoId);
            
            System.out.println("Usuario creado exitosamente: " + nuevoUsuario.getUsername());
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error de base de datos al crear usuario: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("Error de permisos: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error al crear usuario: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Encripta una contraseña usando SHA-256 con salt
     */
    public static String hashPassword(String password) {
        try {
            // Por simplicidad académica, usamos SHA-256 simple
            // En producción se recomendaría bcrypt o argon2
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            // Agregar salt fijo para consistencia (en producción sería único por usuario)
            String saltedPassword = password + "hospital_santa_vida_salt_2024";
            
            byte[] hashedBytes = md.digest(saltedPassword.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al encriptar contraseña", e);
        }
    }
    
    /**
     * Valida que una contraseña cumple con los requisitos de seguridad
     */
    private boolean esPasswordValido(String password) {
        if (password == null || password.length() < 8) {
            System.err.println("La contraseña debe tener al menos 8 caracteres");
            return false;
        }
        
        boolean tieneMinuscula = password.matches(".*[a-z].*");
        boolean tieneMayuscula = password.matches(".*[A-Z].*");
        boolean tieneNumero = password.matches(".*[0-9].*");
        
        if (!tieneMinuscula || !tieneMayuscula || !tieneNumero) {
            System.err.println("La contraseña debe contener al menos una minúscula, una mayúscula y un número");
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida los datos de un nuevo usuario
     */
    private boolean validarDatosUsuario(Usuario usuario, String password) {
        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            System.err.println("Username es requerido");
            return false;
        }
        
        if (usuario.getEmail() == null || !usuario.getEmail().contains("@")) {
            System.err.println("Email válido es requerido");
            return false;
        }
        
        if (usuario.getNombreCompleto() == null || usuario.getNombreCompleto().trim().isEmpty()) {
            System.err.println("Nombre completo es requerido");
            return false;
        }
        
        if (usuario.getTipoUsuario() == null) {
            System.err.println("Tipo de usuario es requerido");
            return false;
        }
        
        if (!esPasswordValido(password)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Obtiene información de la sesión actual
     */
    public String getSessionInfo() {
        if (!isAuthenticated()) {
            return "Sin sesión activa";
        }
        
        return String.format("Usuario: %s (%s) - Sesión iniciada: %s - Tiempo restante: %d min",
                usuarioActual.getNombreCompleto(),
                usuarioActual.getTipoUsuario(),
                inicioSesion.toString(),
                getTiempoRestanteSesion()
        );
    }
    
    /**
     * Obtiene el tiempo restante de sesión en minutos
     */
    public long getTiempoRestanteSesion() {
        if (inicioSesion == null) {
            return 0;
        }
        
        LocalDateTime tiempoLimite = inicioSesion.plusMinutes(TIEMPO_INACTIVIDAD_MINUTOS);
        return java.time.Duration.between(LocalDateTime.now(), tiempoLimite).toMinutes();
    }
    
    /**
     * Genera un token de sesión simple (para uso futuro con APIs)
     */
    public String generarTokenSesion() {
        if (!isAuthenticated()) {
            return null;
        }
        
        try {
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            System.err.println("Error al generar token: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public String toString() {
        return "AuthenticationService{" +
                "usuarioActual=" + (usuarioActual != null ? usuarioActual.getUsername() : "null") +
                ", sesionActiva=" + isAuthenticated() +
                ", tiempoRestante=" + getTiempoRestanteSesion() + " min" +
                '}';
    }
}