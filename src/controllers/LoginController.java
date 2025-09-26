package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.Usuario;
import services.AuthenticationService;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de login
 * Maneja la autenticación de usuarios y redirección según el rol
 */
public class LoginController extends BaseController implements Initializable {
    
    @FXML
    private TextField txtUsername;
    
    @FXML
    private PasswordField txtPassword;
    
    @FXML
    private Button btnLogin;
    
    @FXML
    private Label lblMessage;
    
    @FXML
    private ImageView imgLogo;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarInterfaz();
        cargarDatos();
    }
    
    @Override
    protected void configurarInterfaz() {
        logAction("Configurando interfaz de login");
        
        // Limpiar mensaje inicial
        if (lblMessage != null) {
            lblMessage.setText("");
        }
    }

    @Override
    protected void cargarDatos() {
        // Verificar si ya hay una sesión activa
        if (isAuthenticated()) {
            redirectToMainScreen();
        }
    }

    @Override
    protected void limpiarFormulario() {
        if (txtUsername != null) txtUsername.clear();
        if (txtPassword != null) txtPassword.clear();
        if (lblMessage != null) lblMessage.setText("");
    }

    /**
     * Maneja el evento de login cuando se presiona el botón
     */
    @FXML
    public void handleLogin() {
        logAction("Intento de login iniciado");
        
        if (txtUsername == null || txtPassword == null) {
            mostrarMensaje("Error: Componentes de interfaz no inicializados", "#D32F2F");
            return;
        }
        
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        
        // Limpiar mensaje anterior
        lblMessage.setText("");
        
        // Validar campos obligatorios
        if (!validarCampos(username, password)) {
            return;
        }
        
        try {
            // Deshabilitar botón durante autenticación
            btnLogin.setDisable(true);
            
            // Validación simple de usuarios (temporal)
            boolean loginSuccessful = validarUsuarioTemporal(username, password);
            
            if (loginSuccessful) {
                logAction("Login exitoso para usuario: " + username);
                mostrarMensaje("Login exitoso. Redirigiendo...", "#2E7D32");
                
                System.out.println("✅ Usuario autenticado: " + username);
                
                // Redireccionar según el rol del usuario
                redirigirSegunRol();
                
            } else {
                logAction("Login fallido para usuario: " + username);
                mostrarMensaje("Usuario o contraseña incorrectos", "#D32F2F");
            }
            
        } catch (Exception e) {
            logAction("Error durante login: " + e.getMessage());
            mostrarMensaje("Error de conexión. Intenta nuevamente.", "#D32F2F");
            e.printStackTrace();
        } finally {
            // Rehabilitar botón
            btnLogin.setDisable(false);
        }
    }
    
    /**
     * Valida que los campos obligatorios estén llenos
     */
    private boolean validarCampos(String username, String password) {
        if (username.isEmpty()) {
            mostrarMensaje("Por favor ingresa tu usuario o email", "#D32F2F");
            txtUsername.requestFocus();
            return false;
        }
        
        if (password.isEmpty()) {
            mostrarMensaje("Por favor ingresa tu contraseña", "#D32F2F");
            txtPassword.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Validación temporal de usuarios (para pruebas)
     * Incluye mapeo de roles para cada usuario
     */
    private boolean validarUsuarioTemporal(String username, String password) {
        // Usuarios de prueba con sus respectivos roles
        // Usuarios de la base de datos (únicos usuarios válidos)
        if (username.equals("admin") && password.equals("password123")) {
            // Crear usuario administrador
            Usuario adminUser = new Usuario();
            adminUser.setNombreCompleto("Administrador General");
            adminUser.setUsername("admin");
            adminUser.setTipoUsuario(Usuario.TipoUsuario.ADMINISTRADOR);
            authService.setUsuarioActual(adminUser);
            return true;
        } else if (username.equals("dr.garcia") && password.equals("password123")) {
            // Crear médico de triage
            Usuario doctorUser = new Usuario();
            doctorUser.setNombreCompleto("Dr. García");
            doctorUser.setUsername("dr.garcia");
            doctorUser.setTipoUsuario(Usuario.TipoUsuario.MEDICO_TRIAGE);
            authService.setUsuarioActual(doctorUser);
            return true;
        } else if (username.equals("asist.maria") && password.equals("password123")) {
            // Crear asistente médica - encargada de registro de pacientes
            Usuario assistantUser = new Usuario();
            assistantUser.setNombreCompleto("Asistente María");
            assistantUser.setUsername("asist.maria");
            assistantUser.setTipoUsuario(Usuario.TipoUsuario.ASISTENTE_MEDICA);
            authService.setUsuarioActual(assistantUser);
            return true;
        } else if (username.equals("social.ana") && password.equals("password123")) {
            // Crear trabajador social
            Usuario socialUser = new Usuario();
            socialUser.setNombreCompleto("Trabajadora Social Ana");
            socialUser.setUsername("social.ana");
            socialUser.setTipoUsuario(Usuario.TipoUsuario.TRABAJADOR_SOCIAL);
            authService.setUsuarioActual(socialUser);
            return true;
        } else if (username.equals("dr.martinez") && password.equals("password123")) {
            // Crear médico de urgencias
            Usuario urgenciesUser = new Usuario();
            urgenciesUser.setNombreCompleto("Dr. Martínez");
            urgenciesUser.setUsername("dr.martinez");
            urgenciesUser.setTipoUsuario(Usuario.TipoUsuario.MEDICO_URGENCIAS);
            authService.setUsuarioActual(urgenciesUser);
            return true;
        }
        
        return false;
    }
    
    /**
     * Redirige al usuario según su rol después del login
     */
    private void redirigirSegunRol() {
        Usuario usuario = authService.getUsuarioActual();
        if (usuario == null) {
            mostrarMensaje("Error: Usuario no autenticado", "#D32F2F");
            return;
        }
        
        System.out.println("\n🚀 REDIRIGIENDO SEGÚN ROL DE USUARIO");
        System.out.println("Usuario: " + usuario.getNombreCompleto());
        System.out.println("Tipo: " + usuario.getTipoUsuario());
        System.out.println("=========================================");
        
        try {
            switch (usuario.getTipoUsuario()) {
                case ADMINISTRADOR:
                    System.out.println("🔧 Iniciando Panel de Administración...");
                    // TODO: Abrir pantalla de administración
                    abrirConsola("ADMINISTRADOR");
                    break;
                    
                case MEDICO_TRIAGE:
                    System.out.println("🩺 Iniciando Evaluación de Triage...");
                    // Abrir pantalla de triage
                    abrirPantallaTriage();
                    break;
                    
                case ASISTENTE_MEDICA:
                    System.out.println("👩‍⚕️ Iniciando Registro de Pacientes...");
                    // Abrir registro de pacientes
                    abrirRegistroPaciente();
                    break;
                    
                case TRABAJADOR_SOCIAL:
                    System.out.println("🤝 Iniciando Entrevista Social...");
                    // TODO: Abrir pantalla de trabajo social
                    abrirConsola("TRABAJADOR_SOCIAL");
                    break;
                    
                case MEDICO_URGENCIAS:
                    System.out.println("🚨 Iniciando Atención Médica...");
                    // TODO: Abrir pantalla de atención médica
                    abrirConsola("MEDICO_URGENCIAS");
                    break;
                    
                default:
                    mostrarMensaje("Error: Tipo de usuario no reconocido", "#D32F2F");
                    System.err.println("❌ Tipo de usuario no reconocido: " + usuario.getTipoUsuario());
                    return;
            }
            
            // Cerrar ventana de login
            System.out.println("✅ Login completado exitosamente");
            
        } catch (Exception e) {
            mostrarMensaje("Error al cargar la pantalla principal", "#D32F2F");
            System.err.println("❌ Error al cargar pantalla: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Abre la consola específica para un tipo de usuario
     */
    private void abrirConsola(String tipoUsuario) {
        System.out.println("⏳ Funcionalidad de " + tipoUsuario + " en desarrollo...");
        System.out.println("Por ahora se muestra información por consola");
        System.out.println("Presione Enter para continuar...");
        
        try {
            System.in.read();
        } catch (Exception e) {
            // Ignorar errores de lectura
        }
    }
    
    /**
     * Abre la pantalla de registro de paciente para asistentes médicas
     */
    private void abrirRegistroPaciente() {
        try {
            System.out.println("👩‍⚕️ Abriendo interfaz de Registro de Paciente...");
            
            // Cargar la interfaz FXML de registro de paciente
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/registro-paciente.fxml"));
            Scene registroScene = new Scene(loader.load());
            
            // Obtener el controlador y configurarlo
            RegistroPacienteController controller = loader.getController();
            if (controller != null) {
                controller.initialize();
            }
            
            // Crear nueva ventana
            Stage registroStage = new Stage();
            registroStage.setTitle("Hospital Santa Vida - Registro de Pacientes");
            registroStage.setScene(registroScene);
            registroStage.setResizable(true);
            registroStage.show();
            
            System.out.println("✅ Interfaz de registro de pacientes abierta correctamente!");
            
            // Cerrar ventana de login
            Stage currentStage = (Stage) btnLogin.getScene().getWindow();
            currentStage.close();
            
        } catch (Exception e) {
            System.err.println("❌ Error al abrir interfaz de registro de paciente: " + e.getMessage());
            e.printStackTrace();
            mostrarMensaje("Error al cargar interfaz de registro de paciente", "#D32F2F");
        }
    }
    
    /**
     * Muestra un mensaje en la interfaz
     */
    private void mostrarMensaje(String mensaje, String color) {
        if (lblMessage != null) {
            lblMessage.setText(mensaje);
            lblMessage.setStyle("-fx-text-fill: " + color + ";");
        }
        // También mostrar en consola para depuración
        System.out.println("💬 " + mensaje);
    }
    
    /**
     * Redireccionar a pantalla principal (implementación básica)
     */
    private void redirectToMainScreen() {
        if (isAuthenticated()) {
            Usuario usuario = authService.getUsuarioActual();
            System.out.println("Usuario ya autenticado: " + usuario.getNombreCompleto());
        }
    }
    
    /**
     * Abre la pantalla de triage y cierra la ventana de login
     */
    private void abrirPantallaTriage() {
        try {
            // Cargar el FXML de triage
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/triage.fxml"));
            Scene triageScene = new Scene(loader.load());
            
            // Crear nueva ventana para triage
            Stage triageStage = new Stage();
            triageStage.setTitle("Evaluación de Triage - Hospital Santa Vida");
            triageStage.setScene(triageScene);
            triageStage.setMaximized(true);
            
            // Mostrar nueva ventana
            triageStage.show();
            
            // Cerrar ventana de login actual
            Stage currentStage = (Stage) btnLogin.getScene().getWindow();
            currentStage.close();
            
            System.out.println("✅ Pantalla de triage abierta correctamente!");
            
        } catch (IOException e) {
            System.err.println("❌ Error al abrir pantalla de triage: " + e.getMessage());
            mostrarMensaje("Error al cargar la aplicación principal", "#D32F2F");
            e.printStackTrace();
        }
    }
}