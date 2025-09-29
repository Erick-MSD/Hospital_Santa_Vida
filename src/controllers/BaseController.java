package controllers;

import models.Usuario;
import services.AuthenticationService;

/**
 * Clase base para todos los controladores
 * Proporciona funcionalidades comunes de sesión y navegación
 */
public abstract class BaseController {
    
    protected Usuario usuarioActual;
    protected String tokenSesion;
    protected AuthenticationService authService;
    
    /**
     * Inicializa la sesión del usuario en el controlador
     * @param usuario Usuario autenticado
     * @param token Token de sesión
     */
    public void inicializarSesion(Usuario usuario, String token) {
        this.usuarioActual = usuario;
        this.tokenSesion = token;
        this.authService = new AuthenticationService();
        
        // Llamar método de inicialización específica del controlador
        onSesionInicializada();
    }
    
    /**
     * Método que se ejecuta después de inicializar la sesión
     * Los controladores hijos pueden sobrescribir este método
     */
    protected void onSesionInicializada() {
        // Implementación por defecto vacía
    }
    
    /**
     * Verifica si el usuario tiene un permiso específico
     * @param permiso Permiso a verificar
     * @return true si tiene el permiso
     */
    protected boolean tienePermiso(AuthenticationService.Permiso permiso) {
        return authService != null && authService.tienePermiso(tokenSesion, permiso);
    }
    
    /**
     * Obtiene el usuario actual
     * @return Usuario actual o null si no hay sesión
     */
    protected Usuario getUsuarioActual() {
        return usuarioActual;
    }
    
    /**
     * Obtiene el token de sesión
     * @return Token de sesión o null si no hay sesión
     */
    protected String getTokenSesion() {
        return tokenSesion;
    }
    
    /**
     * Verifica si hay una sesión activa
     * @return true si hay sesión activa
     */
    protected boolean haySesionActiva() {
        return usuarioActual != null && tokenSesion != null;
    }
    
    /**
     * Cierra la sesión actual
     */
    protected void cerrarSesion() {
        if (authService != null && tokenSesion != null) {
            authService.cerrarSesion(tokenSesion);
        }
        usuarioActual = null;
        tokenSesion = null;
        authService = null;
    }
}