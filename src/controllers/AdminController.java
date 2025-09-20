package controllers;

import models.Usuario;

/**
 * Controlador para el dashboard del administrador
 * Permite gestionar usuarios, ver estadísticas y configurar el sistema
 */
public class AdminController extends BaseController {
    
    @Override
    protected void configurarInterfaz() {
        logAction("Configurando interfaz de administrador");
        
        // Verificar permisos de administrador
        Usuario usuarioActual = authService.getUsuarioActual();
        if (usuarioActual == null || usuarioActual.getTipoUsuario() != Usuario.TipoUsuario.ADMINISTRADOR) {
            showError("Acceso denegado: Se requieren permisos de administrador");
            navigateTo("/views/Login.fxml");
            return;
        }
        
        // Configurar elementos de la interfaz
        configurarMenuAdministrador();
        cargarEstadisticasGenerales();
    }
    
    @Override
    protected void cargarDatos() {
        logAction("Cargando datos del panel de administración");
        
        try {
            // Cargar estadísticas del sistema
            cargarEstadisticasSistema();
            
            // Cargar lista de usuarios activos
            cargarUsuariosActivos();
            
            // Cargar configuraciones del sistema
            cargarConfiguracionSistema();
            
        } catch (Exception e) {
            logAction("Error al cargar datos de administración: " + e.getMessage());
            handleDatabaseError(e);
        }
    }
    
    @Override
    protected void limpiarFormulario() {
        // No hay formularios específicos que limpiar en el dashboard principal
        logAction("Limpiando vista de administrador");
    }
    
    /**
     * Maneja la navegación al módulo de gestión de usuarios
     */
    public void handleGestionUsuarios() {
        logAction("Navegando a gestión de usuarios");
        navigateTo("/views/UserManagement.fxml");
    }
    
    /**
     * Maneja la navegación al módulo de reportes
     */
    public void handleReportes() {
        logAction("Navegando a módulo de reportes");
        navigateTo("/views/Reports.fxml");
    }
    
    /**
     * Maneja la navegación al módulo de configuración del sistema
     */
    public void handleConfiguracionSistema() {
        logAction("Navegando a configuración del sistema");
        navigateTo("/views/SystemConfig.fxml");
    }
    
    /**
     * Maneja la navegación a auditoría y logs
     */
    public void handleAuditoria() {
        logAction("Navegando a módulo de auditoría");
        navigateTo("/views/Audit.fxml");
    }
    
    /**
     * Maneja el respaldo de la base de datos
     */
    public void handleRespaldoBD() {
        logAction("Iniciando respaldo de base de datos");
        
        if (showConfirmation("¿Está seguro que desea iniciar el respaldo de la base de datos?")) {
            try {
                // En una implementación real se ejecutaría el respaldo
                showInfo("Respaldo iniciado. Recibirá una notificación cuando termine.");
                logAction("Respaldo de BD iniciado por: " + authService.getUsuarioActual().getNombreCompleto());
                
            } catch (Exception e) {
                logAction("Error al iniciar respaldo: " + e.getMessage());
                showError("Error al iniciar el respaldo: " + e.getMessage());
            }
        }
    }
    
    /**
     * Maneja la creación de nuevo usuario
     */
    public void handleNuevoUsuario() {
        logAction("Iniciando creación de nuevo usuario");
        navigateTo("/views/NewUser.fxml");
    }
    
    /**
     * Maneja la vista de estadísticas en tiempo real
     */
    public void handleEstadisticasEnTiempoReal() {
        logAction("Abriendo estadísticas en tiempo real");
        navigateTo("/views/RealTimeStats.fxml");
    }
    
    /**
     * Maneja el cambio de configuración de turnos
     */
    public void handleConfiguracionTurnos() {
        logAction("Configurando turnos del personal");
        navigateTo("/views/ShiftConfig.fxml");
    }
    
    /**
     * Maneja la gestión de especialidades médicas
     */
    public void handleGestionEspecialidades() {
        logAction("Navegando a gestión de especialidades");
        navigateTo("/views/MedicalSpecialties.fxml");
    }
    
    /**
     * Configura el menú del administrador
     */
    private void configurarMenuAdministrador() {
        logAction("Configurando menú de administrador");
        
        // En JavaFX se configurarían los botones y sus eventos
        // btnGestionUsuarios.setOnAction(e -> handleGestionUsuarios());
        // btnReportes.setOnAction(e -> handleReportes());
        // btnConfiguracion.setOnAction(e -> handleConfiguracionSistema());
        // etc.
    }
    
    /**
     * Carga estadísticas generales del hospital
     */
    private void cargarEstadisticasGenerales() {
        try {
            logAction("Cargando estadísticas generales");
            
            // En una implementación real consultaría la base de datos
            // int pacientesHoy = estadisticasService.getPacientesHoy();
            // int usuariosActivos = usuarioService.getUsuariosActivos().size();
            // etc.
            
        } catch (Exception e) {
            logAction("Error al cargar estadísticas generales: " + e.getMessage());
        }
    }
    
    /**
     * Carga estadísticas del sistema
     */
    private void cargarEstadisticasSistema() {
        logAction("Cargando estadísticas del sistema");
        
        // Estadísticas que se mostrarían:
        // - Usuarios conectados
        // - Pacientes en espera
        // - Tiempo promedio de atención
        // - Disponibilidad del sistema
        // - Espacio en disco
        // - Estado de la base de datos
    }
    
    /**
     * Carga lista de usuarios activos
     */
    private void cargarUsuariosActivos() {
        try {
            logAction("Cargando usuarios activos");
            
            // En JavaFX actualizaría una TableView
            // List<Usuario> usuariosActivos = usuarioService.getUsuariosActivos();
            // tblUsuarios.setItems(FXCollections.observableArrayList(usuariosActivos));
            
        } catch (Exception e) {
            logAction("Error al cargar usuarios activos: " + e.getMessage());
        }
    }
    
    /**
     * Carga configuración del sistema
     */
    private void cargarConfiguracionSistema() {
        logAction("Cargando configuración del sistema");
        
        // Configuraciones que se cargarían:
        // - Tiempo de sesión
        // - Configuración de respaldos
        // - Configuración de notificaciones
        // - Parámetros de triage
        // - Configuración de impresión
    }
    
    /**
     * Obtiene estadísticas rápidas para mostrar en cards/widgets
     */
    public String[] getEstadisticasRapidas() {
        // En una implementación real consultaría servicios
        return new String[]{
            "Pacientes Hoy: 45",
            "En Triage: 12", 
            "En Atención: 8",
            "Usuarios Activos: 15",
            "Tiempo Prom. Espera: 25 min",
            "Sistema: OPERATIVO"
        };
    }
    
    /**
     * Maneja la exportación de datos
     */
    public void handleExportarDatos() {
        logAction("Iniciando exportación de datos");
        
        if (showConfirmation("¿Desea exportar los datos del sistema?")) {
            try {
                // En una implementación real se generaría el archivo
                showInfo("Datos exportados exitosamente");
                logAction("Datos exportados por: " + authService.getUsuarioActual().getNombreCompleto());
                
            } catch (Exception e) {
                logAction("Error en exportación: " + e.getMessage());
                showError("Error al exportar datos: " + e.getMessage());
            }
        }
    }
    
    /**
     * Maneja la importación de datos
     */
    public void handleImportarDatos() {
        logAction("Iniciando importación de datos");
        
        if (showConfirmation("ADVERTENCIA: La importación puede sobrescribir datos existentes. ¿Continuar?")) {
            try {
                // En una implementación real se abriría un FileChooser
                showInfo("Seleccione el archivo de datos a importar");
                logAction("Importación iniciada por: " + authService.getUsuarioActual().getNombreCompleto());
                
            } catch (Exception e) {
                logAction("Error en importación: " + e.getMessage());
                showError("Error al importar datos: " + e.getMessage());
            }
        }
    }
    
    /**
     * Maneja el reinicio del sistema
     */
    public void handleReiniciarSistema() {
        logAction("Solicitud de reinicio del sistema");
        
        if (showConfirmation("ADVERTENCIA: Se desconectarán todos los usuarios. ¿Continuar con el reinicio?")) {
            logAction("Sistema reiniciado por: " + authService.getUsuarioActual().getNombreCompleto());
            showInfo("El sistema se reiniciará en 30 segundos. Guarde su trabajo.");
            
            // En una implementación real se programaría el reinicio
        }
    }
    
    /**
     * Muestra información del sistema
     */
    public void handleInfoSistema() {
        String info = "Hospital Santa Vida - Sistema de Triage\n" +
                     "Versión: 1.0.0\n" +
                     "Base de Datos: MySQL 8.0\n" +
                     "Usuario Actual: " + authService.getUsuarioActual().getNombreCompleto() + "\n" +
                     "Rol: " + authService.getUsuarioActual().getTipoUsuario() + "\n" +
                     "Sesión Iniciada: " + java.time.LocalDateTime.now().toString();
        
        showInfo(info);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        logAction("Limpiando recursos de administrador");
    }
}