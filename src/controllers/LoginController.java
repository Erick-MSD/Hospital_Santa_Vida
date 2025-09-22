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
                
                // Redireccionar a la pantalla de triage
                abrirPantallaTriage();
                
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
     */
    private boolean validarUsuarioTemporal(String username, String password) {
        // Usuarios de prueba
        return (username.equals("admin") && password.equals("admin123")) ||
               (username.equals("doctor") && password.equals("doctor123")) ||
               (username.equals("enfermera") && password.equals("enfermera123"));
    }
    
    /**
     * Muestra un mensaje en la interfaz
     */
    private void mostrarMensaje(String mensaje, String color) {
        if (lblMessage != null) {
            lblMessage.setText(mensaje);
            lblMessage.setStyle("-fx-text-fill: " + color + ";");
        }
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