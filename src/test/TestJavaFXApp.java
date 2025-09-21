package test;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Test real de JavaFX que abre una ventana
 */
public class TestJavaFXApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Crear elementos de la interfaz
        Label lblTitulo = new Label("🏥 Hospital Santa Vida - JavaFX Funcionando!");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
        
        Label lblInfo = new Label("✅ JavaFX 21.0.8 configurado correctamente");
        lblInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #1976D2;");
        
        Button btnCerrar = new Button("Cerrar Aplicación");
        btnCerrar.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold;");
        btnCerrar.setOnAction(e -> primaryStage.close());
        
        Button btnSiguiente = new Button("Listo para Interfaces");
        btnSiguiente.setStyle("-fx-background-color: #388E3C; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSiguiente.setOnAction(e -> {
            System.out.println("🚀 JavaFX listo para crear las interfaces del hospital!");
            primaryStage.close();
        });
        
        // Layout
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 40; -fx-alignment: center; -fx-background-color: #F5F5F5;");
        root.getChildren().addAll(lblTitulo, lblInfo, btnSiguiente, btnCerrar);
        
        // Escena y ventana
        Scene scene = new Scene(root, 400, 250);
        primaryStage.setTitle("Test JavaFX - Hospital Santa Vida");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        
        System.out.println("✅ Ventana JavaFX abierta correctamente!");
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 Iniciando aplicación JavaFX...");
        launch(args);
        System.out.println("✅ Aplicación JavaFX cerrada correctamente!");
    }
}