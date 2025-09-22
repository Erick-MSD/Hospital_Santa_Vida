package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador simplificado para el login
 */
public class SimpleLoginController implements Initializable {
    
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblMessage;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Limpiar mensaje inicial
        if (lblMessage != null) {
            lblMessage.setText("");
        }
    }
    
    /**
     * Maneja el evento de login
     */
    @FXML
    private void handleLogin() {
        if (txtUsername == null || txtPassword == null) {
            mostrarMensaje("Error: Componentes no inicializados", "#D32F2F");
            return;
        }
        
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        
        // Limpiar mensaje anterior
        lblMessage.setText("");
        
        // Validar campos
        if (username.isEmpty()) {
            mostrarMensaje("Por favor ingresa tu usuario", "#D32F2F");
            txtUsername.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            mostrarMensaje("Por favor ingresa tu contraseña", "#D32F2F");
            txtPassword.requestFocus();
            return;
        }
        
        try {
            // Deshabilitar botón
            btnLogin.setDisable(true);
            
            // Validar credenciales
            if (validarCredenciales(username, password)) {
                mostrarMensaje("Login exitoso. Abriendo triage...", "#2E7D32");
                System.out.println("✅ Login exitoso para: " + username);
                
                // Esperar un momento para mostrar el mensaje
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        javafx.application.Platform.runLater(() -> abrirTriage());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                
            } else {
                mostrarMensaje("Usuario o contraseña incorrectos", "#D32F2F");
                System.out.println("❌ Login fallido para: " + username);
            }
            
        } catch (Exception e) {
            mostrarMensaje("Error de conexión", "#D32F2F");
            e.printStackTrace();
        } finally {
            // Rehabilitar botón después de un momento
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> btnLogin.setDisable(false));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    /**
     * Validar credenciales (simple)
     */
    private boolean validarCredenciales(String username, String password) {
        return (username.equals("admin") && password.equals("admin123")) ||
               (username.equals("doctor") && password.equals("doctor123")) ||
               (username.equals("enfermera") && password.equals("enfermera123"));
    }
    
    /**
     * Mostrar mensaje en la interfaz
     */
    private void mostrarMensaje(String mensaje, String color) {
        if (lblMessage != null) {
            lblMessage.setText(mensaje);
            lblMessage.setStyle("-fx-text-fill: " + color + ";");
        }
    }
    
    /**
     * Abrir la pantalla de triage
     */
    private void abrirTriage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/triage.fxml"));
            Scene triageScene = new Scene(loader.load());
            
            Stage triageStage = new Stage();
            triageStage.setTitle("Evaluación de Triage - Hospital Santa Vida");
            triageStage.setScene(triageScene);
            triageStage.setMaximized(true);
            triageStage.show();
            
            // Cerrar ventana de login
            Stage currentStage = (Stage) btnLogin.getScene().getWindow();
            currentStage.close();
            
            System.out.println("✅ Pantalla de triage abierta correctamente!");
            
        } catch (IOException e) {
            System.err.println("❌ Error al abrir triage: " + e.getMessage());
            mostrarMensaje("Error al cargar triage", "#D32F2F");
            e.printStackTrace();
        }
    }
}