package controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import services.AuthenticationService;
import models.Usuario;
import models.TipoUsuario;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para la ventana de login del sistema
 * Maneja la autenticación de usuarios y redirección a interfaces específicas
 */
public class LoginController implements Initializable {
    
    @FXML private ImageView imgLogo;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblMessage;
    
    private AuthenticationService authService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.authService = new AuthenticationService();
        
        // Configurar eventos
        setupEventHandlers();
        
        // Configurar estilos dinámicos
        setupDynamicStyles();
        
        // Limpiar mensajes al escribir
        txtUsername.textProperty().addListener((observable, oldValue, newValue) -> clearMessage());
        txtPassword.textProperty().addListener((observable, oldValue, newValue) -> clearMessage());
        
        // Enter para login
        txtPassword.setOnAction(event -> handleLogin());
        
        // Focus inicial
        Platform.runLater(() -> txtUsername.requestFocus());
    }
    
    /**
     * Maneja el evento de login
     */
    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        
        // Validaciones básicas
        if (username.isEmpty()) {
            showError("Por favor ingrese su usuario o email");
            txtUsername.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("Por favor ingrese su contraseña");
            txtPassword.requestFocus();
            return;
        }
        
        // Deshabilitar interfaz durante autenticación
        setUIEnabled(false);
        showInfo("Verificando credenciales...");
        
        // Crear tarea de autenticación en hilo separado
        Task<AuthenticationService.ResultadoLogin> loginTask = new Task<AuthenticationService.ResultadoLogin>() {
            @Override
            protected AuthenticationService.ResultadoLogin call() throws Exception {
                return authService.iniciarSesion(username, password);
            }
        };
        
        // Manejar resultado de la tarea
        loginTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                AuthenticationService.ResultadoLogin resultado = loginTask.getValue();
                
                if (resultado.isExitoso()) {
                    showSuccess("¡Bienvenido " + resultado.getUsuario().getNombreCompleto() + "!");
                    
                    // Pequeña pausa para mostrar mensaje de éxito
                    Task<Void> redirectTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            Thread.sleep(1500);
                            return null;
                        }
                    };
                    
                    redirectTask.setOnSucceeded(e -> Platform.runLater(() -> {
                        redirectToMainInterface(resultado.getUsuario(), resultado.getTokenSesion());
                    }));
                    
                    new Thread(redirectTask).start();
                } else {
                    showError(resultado.getMensaje());
                    setUIEnabled(true);
                    txtPassword.clear();
                    txtUsername.requestFocus();
                }
            });
        });
        
        loginTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                showError("Error del sistema. Intente nuevamente.");
                setUIEnabled(true);
                txtPassword.clear();
            });
        });
        
        // Ejecutar tarea
        new Thread(loginTask).start();
    }
    
    /**
     * Redirecciona a la interfaz principal según el tipo de usuario
     */
    private void redirectToMainInterface(Usuario usuario, String token) {
        try {
            String fxmlPath;
            String windowTitle;
            
            // Determinar interfaz según tipo de usuario
            switch (usuario.getTipoUsuario()) {
                case ADMINISTRADOR:
                    fxmlPath = "/ui/admin-sala-espera.fxml";
                    windowTitle = "Hospital Santa Vida - Panel Administrativo";
                    break;
                    
                case MEDICO_TRIAGE:
                    fxmlPath = "/ui/triage.fxml";
                    windowTitle = "Hospital Santa Vida - Triage Médico";
                    break;
                    
                case ENFERMERO_TRIAGE:
                    fxmlPath = "/ui/triage.fxml";
                    windowTitle = "Hospital Santa Vida - Triage Enfermería";
                    break;
                    
                case ASISTENTE_MEDICA:
                    fxmlPath = "/ui/registro-paciente.fxml";
                    windowTitle = "Hospital Santa Vida - Registro de Pacientes";
                    break;
                    
                case RECEPCIONISTA:
                    fxmlPath = "/ui/registro-paciente.fxml";
                    windowTitle = "Hospital Santa Vida - Recepción";
                    break;
                    
                case TRABAJADOR_SOCIAL:
                    fxmlPath = "/ui/trabajo-social.fxml";
                    windowTitle = "Hospital Santa Vida - Trabajo Social";
                    break;
                    
                case MEDICO_URGENCIAS:
                    fxmlPath = "/ui/consulta-medica.fxml";
                    windowTitle = "Hospital Santa Vida - Consulta de Urgencias";
                    break;
                    
                case MEDICO:
                    fxmlPath = "/ui/consulta-medica.fxml";
                    windowTitle = "Hospital Santa Vida - Consulta Médica";
                    break;
                    
                case ENFERMERO:
                    fxmlPath = "/ui/triage.fxml";
                    windowTitle = "Hospital Santa Vida - Enfermería";
                    break;
                    
                default:
                    showError("Tipo de usuario no válido: " + usuario.getTipoUsuario());
                    setUIEnabled(true);
                    return;
            }
            
            // Cargar nueva interfaz
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Obtener controlador e inicializar sesión
            Object controller = loader.getController();
            if (controller instanceof BaseController) {
                ((BaseController) controller).inicializarSesion(usuario, token);
            }
            
            // Crear nueva escena
            Scene scene = new Scene(root);
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            
            stage.setTitle(windowTitle);
            stage.setScene(scene);
            stage.centerOnScreen();
            
            // Maximizar para interfaces principales
            if (usuario.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) {
                stage.setMaximized(true);
            }
            
        } catch (IOException e) {
            showError("Error al cargar la interfaz principal");
            setUIEnabled(true);
            System.err.println("Error al cargar FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Configura los manejadores de eventos
     */
    private void setupEventHandlers() {
        // Hover effects para el botón
        btnLogin.setOnMouseEntered(e -> {
            btnLogin.setStyle(btnLogin.getStyle() + "; -fx-scale-x: 1.02; -fx-scale-y: 1.02;");
        });
        
        btnLogin.setOnMouseExited(e -> {
            btnLogin.setStyle(btnLogin.getStyle().replace("; -fx-scale-x: 1.02; -fx-scale-y: 1.02;", ""));
        });
    }
    
    /**
     * Configura estilos dinámicos
     */
    private void setupDynamicStyles() {
        // Estilos de focus para campos de texto
        txtUsername.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                txtUsername.setStyle(txtUsername.getStyle() + "; -fx-border-color: #4A90E2; -fx-border-width: 2;");
            } else {
                txtUsername.setStyle(txtUsername.getStyle().replace("; -fx-border-color: #4A90E2; -fx-border-width: 2;", ""));
            }
        });
        
        txtPassword.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                txtPassword.setStyle(txtPassword.getStyle() + "; -fx-border-color: #4A90E2; -fx-border-width: 2;");
            } else {
                txtPassword.setStyle(txtPassword.getStyle().replace("; -fx-border-color: #4A90E2; -fx-border-width: 2;", ""));
            }
        });
    }
    
    /**
     * Habilita/deshabilita la interfaz de usuario
     */
    private void setUIEnabled(boolean enabled) {
        txtUsername.setDisable(!enabled);
        txtPassword.setDisable(!enabled);
        btnLogin.setDisable(!enabled);
        
        if (!enabled) {
            btnLogin.setText("Verificando...");
            btnLogin.setStyle(btnLogin.getStyle().replace("#2E5984, #4A90E2", "#9E9E9E, #BDBDBD"));
        } else {
            btnLogin.setText("🔑 Acceder al Sistema");
            btnLogin.setStyle(btnLogin.getStyle().replace("#9E9E9E, #BDBDBD", "#2E5984, #4A90E2"));
        }
    }
    
    /**
     * Muestra mensaje de error
     */
    private void showError(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: #E74C3C;");
        
        // Animación sutil
        lblMessage.setOpacity(0);
        lblMessage.setOpacity(1);
    }
    
    /**
     * Muestra mensaje informativo
     */
    private void showInfo(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: #4A90E2;");
    }
    
    /**
     * Muestra mensaje de éxito
     */
    private void showSuccess(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: #27AE60;");
    }
    
    /**
     * Limpia el mensaje
     */
    private void clearMessage() {
        lblMessage.setText("");
    }
}