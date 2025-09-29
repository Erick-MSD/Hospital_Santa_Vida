package services;

import dao.UsuarioDAO;
import models.Usuario;
import models.TipoUsuario;
import utils.PasswordUtils;
import utils.ValidationUtils;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio de autenticación y autorización para el sistema hospitalario
 * Maneja el login, logout, gestión de sesiones y permisos de usuarios
 * Implementa seguridad con sesiones temporales y control de acceso
 */
public class AuthenticationService {
    
    private final UsuarioDAO usuarioDAO;
    private final Map<String, SesionUsuario> sesionesActivas;
    private static final long DURACION_SESION_MINUTOS = 480; // 8 horas
    private static final int MAX_INTENTOS_LOGIN = 3;
    private final Map<String, IntentoLogin> intentosLogin;
    
    /**
     * Constructor del servicio de autenticación
     */
    public AuthenticationService() {
        this.usuarioDAO = new UsuarioDAO();
        this.sesionesActivas = new ConcurrentHashMap<>();
        this.intentosLogin = new ConcurrentHashMap<>();
    }
    
    /**
     * Autentica un usuario con sus credenciales
     * @param nombreUsuario Nombre de usuario
     * @param password Contraseña
     * @return ResultadoLogin con el resultado de la autenticación
     */
    public ResultadoLogin login(String nombreUsuario, String password) {
        try {
            // Validar parámetros de entrada
            if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
                return new ResultadoLogin(false, "Nombre de usuario es obligatorio", null, null);
            }
            
            if (password == null || password.isEmpty()) {
                return new ResultadoLogin(false, "Contraseña es obligatoria", null, null);
            }
            
            // Verificar si el usuario está bloqueado por intentos fallidos
            String claveIntentos = nombreUsuario.toLowerCase().trim();
            IntentoLogin intentos = intentosLogin.get(claveIntentos);
            
            if (intentos != null && intentos.estaBloqueado()) {
                return new ResultadoLogin(false, 
                    "Usuario bloqueado temporalmente. Intente más tarde.", null, null);
            }
            
            // Autenticar con el DAO
            Usuario usuario = usuarioDAO.autenticar(nombreUsuario.trim(), password);
            
            if (usuario == null) {
                // Registrar intento fallido
                registrarIntentoFallido(claveIntentos);
                return new ResultadoLogin(false, 
                    "Credenciales incorrectas", null, null);
            }
            
            if (!usuario.isActivo()) {
                return new ResultadoLogin(false, 
                    "Usuario desactivado. Contacte al administrador.", null, null);
            }
            
            // Limpiar intentos fallidos al tener éxito
            intentosLogin.remove(claveIntentos);
            
            // Crear sesión
            String tokenSesion = crearSesion(usuario);
            
            return new ResultadoLogin(true, 
                "Login exitoso", usuario, tokenSesion);
            
        } catch (SQLException e) {
            System.err.println("Error en autenticación: " + e.getMessage());
            return new ResultadoLogin(false, 
                "Error del sistema. Intente más tarde.", null, null);
        }
    }
    
    /**
     * Alias del método login para compatibilidad
     * @param nombreUsuario Nombre de usuario
     * @param password Contraseña
     * @return ResultadoLogin con el resultado de la autenticación
     */
    public ResultadoLogin iniciarSesion(String nombreUsuario, String password) {
        return login(nombreUsuario, password);
    }
    
    /**
     * Cierra la sesión de un usuario
     * @param tokenSesion Token de la sesión a cerrar
     * @return true si se cerró correctamente
     */
    public boolean logout(String tokenSesion) {
        if (tokenSesion == null || tokenSesion.trim().isEmpty()) {
            return false;
        }
        
        SesionUsuario sesion = sesionesActivas.remove(tokenSesion.trim());
        return sesion != null;
    }
    
    /**
     * Alias del método logout para compatibilidad
     * @param tokenSesion Token de la sesión a cerrar
     * @return true si se cerró correctamente
     */
    public boolean cerrarSesion(String tokenSesion) {
        return logout(tokenSesion);
    }
    
    /**
     * Valida si un token de sesión es válido y no ha expirado
     * @param tokenSesion Token a validar
     * @return true si la sesión es válida
     */
    public boolean validarSesion(String tokenSesion) {
        if (tokenSesion == null || tokenSesion.trim().isEmpty()) {
            return false;
        }
        
        SesionUsuario sesion = sesionesActivas.get(tokenSesion.trim());
        
        if (sesion == null) {
            return false;
        }
        
        // Verificar si la sesión ha expirado
        if (sesion.haExpirado()) {
            sesionesActivas.remove(tokenSesion.trim());
            return false;
        }
        
        // Actualizar último acceso
        sesion.actualizarAcceso();
        return true;
    }
    
    /**
     * Obtiene el usuario asociado a un token de sesión
     * @param tokenSesion Token de sesión
     * @return Usuario asociado o null si no es válido
     */
    public Usuario obtenerUsuarioPorToken(String tokenSesion) {
        if (!validarSesion(tokenSesion)) {
            return null;
        }
        
        SesionUsuario sesion = sesionesActivas.get(tokenSesion.trim());
        return sesion != null ? sesion.getUsuario() : null;
    }
    
    /**
     * Verifica si un usuario tiene un permiso específico
     * @param tokenSesion Token de sesión del usuario
     * @param permiso Permiso a verificar
     * @return true si tiene el permiso
     */
    public boolean tienePermiso(String tokenSesion, Permiso permiso) {
        Usuario usuario = obtenerUsuarioPorToken(tokenSesion);
        if (usuario == null) {
            return false;
        }
        
        return tienePermiso(usuario.getTipoUsuario(), permiso);
    }
    
    /**
     * Verifica si un tipo de usuario tiene un permiso específico
     * @param tipoUsuario Tipo de usuario
     * @param permiso Permiso a verificar
     * @return true si tiene el permiso
     */
    public boolean tienePermiso(TipoUsuario tipoUsuario, Permiso permiso) {
        if (tipoUsuario == null || permiso == null) {
            return false;
        }
        
        switch (tipoUsuario) {
            case ADMINISTRADOR:
                return true; // El admin tiene todos los permisos
                
            case MEDICO:
            case MEDICO_URGENCIAS:
                return permiso == Permiso.VER_PACIENTES ||
                       permiso == Permiso.CREAR_ATENCION_MEDICA ||
                       permiso == Permiso.VER_ATENCION_MEDICA ||
                       permiso == Permiso.ACTUALIZAR_ATENCION_MEDICA ||
                       permiso == Permiso.CREAR_CITAS ||
                       permiso == Permiso.VER_CITAS ||
                       permiso == Permiso.ACTUALIZAR_CITAS ||
                       permiso == Permiso.VER_REPORTES_MEDICOS ||
                       permiso == Permiso.REALIZAR_CONSULTAS;
                       
            case MEDICO_TRIAGE:
            case ENFERMERO_TRIAGE:
                return permiso == Permiso.VER_PACIENTES ||
                       permiso == Permiso.CREAR_PACIENTES ||
                       permiso == Permiso.ACTUALIZAR_PACIENTES ||
                       permiso == Permiso.CREAR_TRIAGE ||
                       permiso == Permiso.VER_TRIAGE ||
                       permiso == Permiso.ACTUALIZAR_TRIAGE ||
                       permiso == Permiso.VER_COLA_TRIAGE ||
                       permiso == Permiso.REALIZAR_TRIAGE;
                       
            case ASISTENTE_MEDICA:
            case RECEPCIONISTA:
                return permiso == Permiso.VER_PACIENTES ||
                       permiso == Permiso.CREAR_PACIENTES ||
                       permiso == Permiso.ACTUALIZAR_PACIENTES ||
                       permiso == Permiso.REGISTRAR_PACIENTES ||
                       permiso == Permiso.CREAR_CITAS ||
                       permiso == Permiso.VER_CITAS ||
                       permiso == Permiso.ACTUALIZAR_CITAS;
                       
            case TRABAJADOR_SOCIAL:
                return permiso == Permiso.VER_PACIENTES ||
                       permiso == Permiso.CREAR_DATOS_SOCIALES ||
                       permiso == Permiso.VER_DATOS_SOCIALES ||
                       permiso == Permiso.ACTUALIZAR_DATOS_SOCIALES ||
                       permiso == Permiso.VER_REPORTES_SOCIALES ||
                       permiso == Permiso.REALIZAR_EVALUACION_SOCIAL;
                       
            default:
                return false;
        }
    }
    
    /**
     * Cambia la contraseña de un usuario
     * @param tokenSesion Token de sesión del usuario
     * @param passwordActual Contraseña actual
     * @param nuevaPassword Nueva contraseña
     * @return ResultadoCambioPassword con el resultado
     */
    public ResultadoCambioPassword cambiarPassword(String tokenSesion, 
            String passwordActual, String nuevaPassword) {
        try {
            Usuario usuario = obtenerUsuarioPorToken(tokenSesion);
            if (usuario == null) {
                return new ResultadoCambioPassword(false, "Sesión inválida");
            }
            
            // Verificar contraseña actual
            if (!PasswordUtils.verificarPassword(passwordActual, usuario.getPasswordHash())) {
                return new ResultadoCambioPassword(false, "Contraseña actual incorrecta");
            }
            
            // Validar nueva contraseña
            if (!ValidationUtils.validarPassword(nuevaPassword)) {
                return new ResultadoCambioPassword(false, 
                    "La nueva contraseña no cumple con los requisitos de seguridad");
            }
            
            // Cambiar contraseña
            boolean exito = usuarioDAO.cambiarPassword(usuario.getId(), nuevaPassword);
            
            if (exito) {
                return new ResultadoCambioPassword(true, "Contraseña cambiada exitosamente");
            } else {
                return new ResultadoCambioPassword(false, "Error al cambiar la contraseña");
            }
            
        } catch (SQLException e) {
            System.err.println("Error al cambiar contraseña: " + e.getMessage());
            return new ResultadoCambioPassword(false, "Error del sistema");
        }
    }
    
    /**
     * Obtiene lista de usuarios activos por tipo
     * @param tokenSesion Token de sesión (debe tener permisos)
     * @param tipoUsuario Tipo de usuario a buscar
     * @return Lista de usuarios o null si no tiene permisos
     */
    public List<Usuario> obtenerUsuariosPorTipo(String tokenSesion, TipoUsuario tipoUsuario) {
        if (!tienePermiso(tokenSesion, Permiso.VER_USUARIOS)) {
            return null;
        }
        
        try {
            return usuarioDAO.obtenerPorTipo(tipoUsuario);
        } catch (SQLException e) {
            System.err.println("Error al obtener usuarios por tipo: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtiene estadísticas de usuarios (solo para administradores)
     * @param tokenSesion Token de sesión del administrador
     * @return Estadísticas o null si no tiene permisos
     */
    public List<UsuarioDAO.EstadisticaUsuario> obtenerEstadisticasUsuarios(String tokenSesion) {
        if (!tienePermiso(tokenSesion, Permiso.VER_ESTADISTICAS_SISTEMA)) {
            return null;
        }
        
        try {
            return usuarioDAO.obtenerEstadisticasPorTipo();
        } catch (SQLException e) {
            System.err.println("Error al obtener estadísticas de usuarios: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Limpia sesiones expiradas del sistema
     */
    public void limpiarSesionesExpiradas() {
        sesionesActivas.entrySet().removeIf(entry -> entry.getValue().haExpirado());
    }
    
    /**
     * Obtiene el número de sesiones activas
     * @return Número de sesiones activas
     */
    public int contarSesionesActivas() {
        limpiarSesionesExpiradas();
        return sesionesActivas.size();
    }
    
    // Métodos privados auxiliares
    
    private String crearSesion(Usuario usuario) {
        String token = UUID.randomUUID().toString();
        SesionUsuario sesion = new SesionUsuario(usuario);
        sesionesActivas.put(token, sesion);
        return token;
    }
    
    private void registrarIntentoFallido(String claveUsuario) {
        IntentoLogin intento = intentosLogin.getOrDefault(claveUsuario, new IntentoLogin());
        intento.registrarIntento();
        intentosLogin.put(claveUsuario, intento);
    }
    
    // Clases internas
    
    /**
     * Representa una sesión de usuario activa
     */
    private static class SesionUsuario {
        private final Usuario usuario;
        private final LocalDateTime fechaCreacion;
        private LocalDateTime ultimoAcceso;
        
        public SesionUsuario(Usuario usuario) {
            this.usuario = usuario;
            this.fechaCreacion = LocalDateTime.now();
            this.ultimoAcceso = LocalDateTime.now();
        }
        
        public Usuario getUsuario() {
            return usuario;
        }
        
        public boolean haExpirado() {
            return LocalDateTime.now().isAfter(ultimoAcceso.plusMinutes(DURACION_SESION_MINUTOS));
        }
        
        public void actualizarAcceso() {
            this.ultimoAcceso = LocalDateTime.now();
        }
    }
    
    /**
     * Controla los intentos fallidos de login
     */
    private static class IntentoLogin {
        private int intentos = 0;
        private LocalDateTime ultimoIntento;
        private static final int MINUTOS_BLOQUEO = 15;
        
        public void registrarIntento() {
            this.intentos++;
            this.ultimoIntento = LocalDateTime.now();
        }
        
        public boolean estaBloqueado() {
            if (intentos < MAX_INTENTOS_LOGIN) {
                return false;
            }
            
            if (ultimoIntento == null) {
                return false;
            }
            
            // Verificar si el bloqueo ha expirado
            if (LocalDateTime.now().isAfter(ultimoIntento.plusMinutes(MINUTOS_BLOQUEO))) {
                intentos = 0; // Resetear intentos
                return false;
            }
            
            return true;
        }
    }
    
    /**
     * Resultado de un intento de login
     */
    public static class ResultadoLogin {
        private final boolean exitoso;
        private final String mensaje;
        private final Usuario usuario;
        private final String tokenSesion;
        
        public ResultadoLogin(boolean exitoso, String mensaje, Usuario usuario, String tokenSesion) {
            this.exitoso = exitoso;
            this.mensaje = mensaje;
            this.usuario = usuario;
            this.tokenSesion = tokenSesion;
        }
        
        public boolean isExitoso() { return exitoso; }
        public String getMensaje() { return mensaje; }
        public Usuario getUsuario() { return usuario; }
        public String getTokenSesion() { return tokenSesion; }
    }
    
    /**
     * Resultado de un cambio de contraseña
     */
    public static class ResultadoCambioPassword {
        private final boolean exitoso;
        private final String mensaje;
        
        public ResultadoCambioPassword(boolean exitoso, String mensaje) {
            this.exitoso = exitoso;
            this.mensaje = mensaje;
        }
        
        public boolean isExitoso() { return exitoso; }
        public String getMensaje() { return mensaje; }
    }
    
    /**
     * Enum de permisos del sistema
     */
    public enum Permiso {
        // Permisos de usuarios
        VER_USUARIOS,
        CREAR_USUARIOS,
        ACTUALIZAR_USUARIOS,
        ELIMINAR_USUARIOS,
        
        // Permisos de pacientes
        VER_PACIENTES,
        CREAR_PACIENTES,
        ACTUALIZAR_PACIENTES,
        ELIMINAR_PACIENTES,
        REGISTRAR_PACIENTES,
        
        // Permisos de triage
        VER_TRIAGE,
        CREAR_TRIAGE,
        ACTUALIZAR_TRIAGE,
        VER_COLA_TRIAGE,
        REALIZAR_TRIAGE,
        
        // Permisos de atención médica
        VER_ATENCION_MEDICA,
        CREAR_ATENCION_MEDICA,
        ACTUALIZAR_ATENCION_MEDICA,
        REALIZAR_CONSULTAS,
        
        // Permisos de datos sociales
        VER_DATOS_SOCIALES,
        CREAR_DATOS_SOCIALES,
        ACTUALIZAR_DATOS_SOCIALES,
        REALIZAR_EVALUACION_SOCIAL,
        
        // Permisos de citas
        VER_CITAS,
        CREAR_CITAS,
        ACTUALIZAR_CITAS,
        CANCELAR_CITAS,
        
        // Permisos de reportes
        VER_REPORTES_MEDICOS,
        VER_REPORTES_SOCIALES,
        VER_REPORTES_ADMINISTRATIVOS,
        VER_ESTADISTICAS_SISTEMA,
        EXPORTAR_REPORTES,
        
        // Permisos de dashboard
        VER_DASHBOARD_ADMIN,
        VER_DASHBOARD_MEDICO,
        VER_DASHBOARD_ENFERMERO
    }
}