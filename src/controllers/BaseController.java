package controllers;

import services.AuthenticationService;
import services.TriageService;
import models.Usuario;

/**
 * Controlador base que contiene funcionalidad común para todos los controladores
 * Maneja la autenticación y servicios compartidos
 */
public abstract class BaseController {
    
    // Servicios compartidos
    protected static AuthenticationService authService;
    protected static TriageService triageService;
    
    // Estado común
    protected Usuario usuarioActual;
    
    // Inicialización estática de servicios
    static {
        authService = new AuthenticationService();
        triageService = new TriageService(authService);
    }
    
    /**
     * Inicializa el controlador con el usuario actual
     */
    public void initialize() {
        // Se ejecuta automáticamente al cargar la vista JavaFX
        actualizarUsuarioActual();
        configurarInterfaz();
        cargarDatos();
    }
    
    /**
     * Actualiza la referencia al usuario actual
     */
    protected void actualizarUsuarioActual() {
        this.usuarioActual = authService.getUsuarioActual();
    }
    
    /**
     * Configura elementos de la interfaz según el usuario actual
     * Debe ser implementado por cada controlador específico
     */
    protected abstract void configurarInterfaz();
    
    /**
     * Carga los datos necesarios para la vista
     * Debe ser implementado por cada controlador específico
     */
    protected abstract void cargarDatos();
    
    /**
     * Verifica si el usuario está autenticado
     */
    protected boolean isAuthenticated() {
        return authService.isAuthenticated();
    }
    
    /**
     * Verifica permisos para una operación
     */
    protected boolean checkPermission(Usuario.TipoUsuario tipoRequerido) {
        return authService.hasRole(tipoRequerido);
    }
    
    /**
     * Muestra mensaje de error de permisos
     */
    protected void showPermissionError() {
        showError("No tiene permisos para realizar esta operación");
    }
    
    /**
     * Muestra mensaje de error genérico
     */
    protected void showError(String mensaje) {
        // En una implementación real con JavaFX sería:
        // Alert alert = new Alert(Alert.AlertType.ERROR);
        // alert.setContentText(mensaje);
        // alert.showAndWait();
        
        // Por ahora solo imprimir
        System.err.println("ERROR: " + mensaje);
    }
    
    /**
     * Muestra mensaje de información
     */
    protected void showInfo(String mensaje) {
        // En una implementación real con JavaFX sería:
        // Alert alert = new Alert(Alert.AlertType.INFORMATION);
        // alert.setContentText(mensaje);
        // alert.showAndWait();
        
        // Por ahora solo imprimir
        System.out.println("INFO: " + mensaje);
    }
    
    /**
     * Muestra mensaje de confirmación
     */
    protected boolean showConfirmation(String mensaje) {
        // En una implementación real con JavaFX sería:
        // Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        // alert.setContentText(mensaje);
        // return alert.showAndWait().get() == ButtonType.OK;
        
        // Por ahora siempre retornar true
        System.out.println("CONFIRMACIÓN: " + mensaje);
        return true;
    }
    
    /**
     * Navega a otra vista
     */
    protected void navigateTo(String fxmlFile) {
        // En una implementación real con JavaFX:
        // try {
        //     FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        //     Scene scene = new Scene(loader.load());
        //     Stage stage = (Stage) currentNode.getScene().getWindow();
        //     stage.setScene(scene);
        // } catch (IOException e) {
        //     showError("Error al cargar la vista: " + e.getMessage());
        // }
        
        System.out.println("Navegando a: " + fxmlFile);
    }
    
    /**
     * Refresca los datos de la vista actual
     */
    protected void refreshData() {
        actualizarUsuarioActual();
        cargarDatos();
    }
    
    /**
     * Valida campos obligatorios
     */
    protected boolean validarCamposObligatorios(String... campos) {
        for (String campo : campos) {
            if (campo == null || campo.trim().isEmpty()) {
                showError("Todos los campos obligatorios deben ser completados");
                return false;
            }
        }
        return true;
    }
    
    /**
     * Valida formato de email
     */
    protected boolean validarEmail(String email) {
        if (email == null || !email.contains("@") || !email.contains(".")) {
            showError("Formato de email inválido");
            return false;
        }
        return true;
    }
    
    /**
     * Valida formato de teléfono
     */
    protected boolean validarTelefono(String telefono) {
        if (telefono == null || telefono.length() < 10) {
            showError("El teléfono debe tener al menos 10 dígitos");
            return false;
        }
        return true;
    }
    
    /**
     * Limpia los campos del formulario
     * Debe ser implementado por cada controlador específico
     */
    protected abstract void limpiarFormulario();
    
    /**
     * Obtiene el nombre del controlador para logging
     */
    protected String getControllerName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * Log de acciones del usuario
     */
    protected void logAction(String accion) {
        String usuario = usuarioActual != null ? usuarioActual.getUsername() : "Anónimo";
        System.out.println(String.format("[%s] %s - %s", getControllerName(), usuario, accion));
    }
    
    /**
     * Maneja errores de base de datos
     */
    protected void handleDatabaseError(Exception e) {
        System.err.println("Error de base de datos en " + getControllerName() + ": " + e.getMessage());
        showError("Error de conexión con la base de datos. Intente nuevamente.");
    }
    
    /**
     * Verifica el estado de la conexión
     */
    protected boolean checkDatabaseConnection() {
        // En una implementación real verificaría la conexión
        return true;
    }
    
    /**
     * Finaliza el controlador liberando recursos
     */
    public void cleanup() {
        // Liberar recursos específicos del controlador
        logAction("Controlador finalizado");
    }
    
    // Getters para servicios (útil para controladores hijos)
    protected AuthenticationService getAuthService() {
        return authService;
    }
    
    protected TriageService getTriageService() {
        return triageService;
    }
    
    protected Usuario getUsuarioActual() {
        return usuarioActual;
    }
}