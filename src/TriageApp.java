import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TriageApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Cargar el FXML de login primero
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
            Scene scene = new Scene(loader.load());
            
            primaryStage.setTitle("Hospital Santa Vida - Iniciar Sesión");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
            
            System.out.println("✅ Sistema de Login iniciado correctamente!");
            System.out.println("👨‍⚕️ Usuarios de prueba:");
            System.out.println("   - admin / admin123");
            System.out.println("   - doctor / doctor123"); 
            System.out.println("   - enfermera / enfermera123");
            
        } catch (Exception e) {
            System.err.println("❌ Error al cargar la interfaz de login: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}