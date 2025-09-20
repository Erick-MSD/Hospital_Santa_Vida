package controllers;

import models.Usuario;

/**
 * Controlador para la pantalla de login
 * Maneja la autenticación de usuarios y redirección según el rol
 */
public class LoginController extends BaseController {
    
    // En una implementación real con JavaFX estos serían @FXML
    private String txtUsername;
    private String txtPassword;
    
    @Override
    protected void configurarInterfaz() {
        // Configurar elementos de la interfaz de login
        logAction("Configurando interfaz de login");
        
        // En JavaFX real sería algo como:
        // txtUsername.clear();
        // txtPassword.clear();
        // lblError.setText("");
        // btnLogin.setOnAction(this::handleLogin);
    }
    
    @Override
    protected void cargarDatos() {
        // No hay datos específicos que cargar en login
        // Solo verificar si ya hay una sesión activa
        if (isAuthenticated()) {
            redirectToMainScreen();
        }
    }
    
    @Override
    protected void limpiarFormulario() {
        txtUsername = "";
        txtPassword = "";
    }
    
    /**
     * Maneja el evento de login
     */
    public void handleLogin() {
        logAction("Intento de login iniciado");
        
        // Validar campos
        if (!validarCamposObligatorios(txtUsername, txtPassword)) {
            return;
        }
        
        try {
            // Intentar autenticación
            boolean loginExitoso = authService.login(txtUsername, txtPassword);
            
            if (loginExitoso) {
                logAction("Login exitoso para: " + txtUsername);
                showInfo("Bienvenido " + authService.getUsuarioActual().getNombreCompleto());
                redirectToMainScreen();
            } else {
                logAction("Login fallido para: " + txtUsername);
                showError("Usuario o contraseña incorrectos");
                limpiarPassword();
            }
            
        } catch (Exception e) {
            logAction("Error durante login: " + e.getMessage());
            handleDatabaseError(e);
            limpiarPassword();
        }
    }
    
    /**
     * Redirecciona a la pantalla principal según el tipo de usuario
     */
    private void redirectToMainScreen() {
        Usuario usuario = authService.getUsuarioActual();
        
        if (usuario == null) {
            showError("Error al obtener información del usuario");
            return;
        }
        
        String targetScreen = determineTargetScreen(usuario.getTipoUsuario());
        navigateTo(targetScreen);
    }
    
    /**
     * Determina a qué pantalla debe ir el usuario según su rol
     */
    private String determineTargetScreen(Usuario.TipoUsuario tipoUsuario) {
        switch (tipoUsuario) {
            case ADMINISTRADOR:
                return "/views/AdminDashboard.fxml";
            case MEDICO_TRIAGE:
                return "/views/TriageScreen.fxml";
            case ASISTENTE_MEDICA:
                return "/views/PatientRegistration.fxml";
            case TRABAJADOR_SOCIAL:
                return "/views/SocialInterview.fxml";
            case MEDICO_URGENCIAS:
                return "/views/EmergencyScreen.fxml";
            default:
                return "/views/Dashboard.fxml";
        }
    }
    
    /**
     * Limpia solo el campo de contraseña
     */
    private void limpiarPassword() {
        txtPassword = "";
        // En JavaFX: txtPassword.clear();
    }
    
    /**
     * Maneja el evento de "Olvidé mi contraseña"
     */
    public void handleForgotPassword() {
        logAction("Solicitud de recuperación de contraseña");
        showInfo("Contacte al administrador del sistema para recuperar su contraseña");
    }
    
    /**
     * Maneja el evento de salir de la aplicación
     */
    public void handleExit() {
        logAction("Saliendo de la aplicación");
        
        if (showConfirmation("¿Está seguro que desea salir de la aplicación?")) {
            // En JavaFX: Platform.exit();
            System.exit(0);
        }
    }
    
    /**
     * Valida el formato del username
     */
    private boolean validarUsername() {
        if (txtUsername == null || txtUsername.trim().length() < 3) {
            showError("El usuario debe tener al menos 3 caracteres");
            return false;
        }
        return true;
    }
    
    /**
     * Valida el formato de la contraseña
     */
    private boolean validarPassword() {
        if (txtPassword == null || txtPassword.length() < 6) {
            showError("La contraseña debe tener al menos 6 caracteres");
            return false;
        }
        return true;
    }
    
    /**
     * Validación específica para login
     */
    @Override
    protected boolean validarCamposObligatorios(String... campos) {
        return validarUsername() && validarPassword();
    }
    
    /**
     * Simula el ingreso de credenciales (para testing)
     */
    public void setCredentials(String username, String password) {
        this.txtUsername = username;
        this.txtPassword = password;
    }
    
    /**
     * Obtiene las credenciales actuales (para testing)
     */
    public String[] getCredentials() {
        return new String[]{txtUsername, txtPassword};
    }
    
    /**
     * Maneja atajos de teclado
     */
    public void handleKeyPressed(String keyCode) {
        if ("ENTER".equals(keyCode)) {
            handleLogin();
        } else if ("ESCAPE".equals(keyCode)) {
            limpiarFormulario();
        }
    }
    
    /**
     * Configura usuarios de prueba para desarrollo
     */
    public void setupTestUsers() {
        // Solo en modo desarrollo
        logAction("Configurando usuarios de prueba");
        
        // Esto se llamaría solo durante desarrollo
        // En producción estos usuarios ya estarían en la BD
    }
    
    /**
     * Obtiene información del sistema para mostrar en login
     */
    public String getSystemInfo() {
        return "Hospital Santa Vida - Sistema de Triage v1.0\n" +
               "Desarrollado para gestión de urgencias médicas\n" +
               "© 2024 Universidad Tecmilenio";
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        // Limpiar recursos específicos del login
        limpiarFormulario();
    }
}