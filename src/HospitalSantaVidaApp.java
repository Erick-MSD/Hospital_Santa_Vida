import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Aplicación principal del Sistema de Triage Hospitalario
 * Hospital Santa Vida
 */
public class HospitalSantaVidaApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Cargar la interfaz de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
            Scene scene = new Scene(loader.load());
            
            // Configurar la ventana principal
            primaryStage.setTitle("Hospital Santa Vida - Sistema de Triage");
            primaryStage.setScene(scene);
            
            // Configurar tamaño de ventana para aplicación de escritorio
            primaryStage.setWidth(1200);
            primaryStage.setHeight(800);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.setResizable(true);
            primaryStage.setMaximized(false);
            primaryStage.centerOnScreen();
            
            // Agregar icono de la aplicación
            try {
                Image icon = new Image(getClass().getResourceAsStream("/assets/img/Hospital_santa_vida.png"));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("No se pudo cargar el icono de la aplicación");
            }
            
            // Mostrar la ventana
            primaryStage.show();
            
            System.out.println("🏥 Hospital Santa Vida - Sistema de Triage iniciado");
            System.out.println("✅ Interfaz de login cargada correctamente");
            
            // Información de usuarios de prueba
            System.out.println("\n📋 USUARIOS DE PRUEBA DISPONIBLES:");
            System.out.println("   👨‍💼 Administrador: admin");
            System.out.println("   👨‍⚕️ Médico Triage: dr.garcia");
            System.out.println("   👩‍⚕️ Asistente Médica: asist.maria");
            System.out.println("   👩‍💼 Trabajador Social: social.ana");
            System.out.println("   👨‍⚕️ Médico Urgencias: dr.martinez");
            System.out.println("   🔑 Contraseña para todos: password123");
            
        } catch (Exception e) {
            System.err.println("❌ Error al iniciar la aplicación:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("🚀 Iniciando Hospital Santa Vida - Sistema de Triage...");
        launch(args);
    }
}