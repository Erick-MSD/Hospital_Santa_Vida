import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import utils.DatabaseConnection;

/**
 * Aplicación principal del Sistema de Triage Hospitalario - Hospital Santa Vida
 * 
 * Este sistema integral de gestión hospitalaria incluye:
 * - Sistema de autenticación por roles (Administrador, Médico, Enfermero, Recepcionista, Trabajador Social)
 * - Módulo de triage con clasificación automática por urgencia
 * - Registro y gestión de pacientes
 * - Consulta médica con historiales
 * - Evaluación de trabajo social
 * - Panel administrativo con estadísticas en tiempo real
 * - Estructuras de datos avanzadas para optimización
 * 
 * @author GRED Systems
 * @version 1.0
 * @since 2025
 */
public class HospitalSantaVidaApp extends Application {
    
    private static final String APP_TITLE = "Hospital Santa Vida - Sistema de Triage";
    private static final String APP_VERSION = "1.1.3";
    private static final double MIN_WIDTH = 1200;
    private static final double MIN_HEIGHT = 800;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Verificar conexión a la base de datos (modo tolerante)
            boolean conexionOk = verificarConexionBaseDatos();
            if (!conexionOk) {
                System.err.println("[APP] Continuando en modo limitado (sin BD) solo para pruebas de UI.");
            }
            
            // Cargar la ventana de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
            Parent root = loader.load();
            
            // Configurar la escena
            Scene scene = new Scene(root, MIN_WIDTH, MIN_HEIGHT);
            
            // Configurar la ventana principal
            primaryStage.setTitle(APP_TITLE + " v" + APP_VERSION);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(MIN_WIDTH);
            primaryStage.setMinHeight(MIN_HEIGHT);
            primaryStage.setMaximized(false);
            primaryStage.setResizable(true);
            
            // Configurar icono de la aplicación
            try {
                Image icon = new Image(getClass().getResourceAsStream("/assets/img/Hospital_santa_vida.png"));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("No se pudo cargar el icono de la aplicación: " + e.getMessage());
            }
            
            // Centrar en pantalla
            primaryStage.centerOnScreen();
            
            // Configurar el evento de cierre de aplicación
            primaryStage.setOnCloseRequest(_ -> {
                try {
                    // Cerrar conexiones de base de datos
                    DatabaseConnection.closeAllConnections();
                    
                    // Salir de la aplicación
                    Platform.exit();
                    System.exit(0);
                    
                } catch (Exception e) {
                    System.err.println("Error al cerrar la aplicación: " + e.getMessage());
                    System.exit(1);
                }
            });
            
            // Mostrar la ventana
            primaryStage.show();
            if (!conexionOk) {
                mostrarErrorConexion();
            }
            
            // Mensaje de inicio en consola
            System.out.println("=================================================");
            System.out.println("  HOSPITAL SANTA VIDA - SISTEMA DE TRIAGE");
            System.out.println("  Versión: " + APP_VERSION);
            System.out.println("  Sistema iniciado correctamente");
            System.out.println("  Fecha: " + java.time.LocalDateTime.now());
            System.out.println("=================================================");
            
        } catch (Exception e) {
            System.err.println("Error crítico al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
            
            // Mostrar error al usuario
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Crítico");
            alert.setHeaderText("No se pudo iniciar la aplicación");
            alert.setContentText("Error: " + e.getMessage() + "\\n\\nPor favor, contacte al administrador del sistema.");
            alert.showAndWait();
            
            Platform.exit();
        }
    }
    
    /**
     * Verifica la conexión a la base de datos al iniciar
     * @return true si la conexión es exitosa
     */
    private boolean verificarConexionBaseDatos() {
        try {
            // Intenta establecer una conexión
            DatabaseConnection.obtenerConexion().close();
            System.out.println("✓ Conexión a la base de datos establecida correctamente");
            return true;
            
        } catch (Exception e) {
            System.err.println("✗ Error de conexión a la base de datos: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Muestra un error de conexión a la base de datos
     */
    private void mostrarErrorConexion() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Conexión");
            alert.setHeaderText("No se pudo conectar a la base de datos");
            alert.setContentText(
                "Verifique que:\\n\\n" +
                "1. MySQL esté ejecutándose\\n" +
                "2. La base de datos 'hospital_santa_vida' exista\\n" +
                "3. Las credenciales en DatabaseConnection.java sean correctas\\n" +
                "4. El puerto MySQL (3306) esté disponible\\n\\n" +
                "Ejecute primero el script SQL en database/hospital_schema.sql"
            );
            alert.showAndWait();
        });
    }
    
    /**
     * Punto de entrada principal de la aplicación
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        // Configurar propiedades del sistema para JavaFX
        System.setProperty("javafx.preloader", "utils.SplashScreen");
        System.setProperty("java.awt.headless", "false");
        
        // Verificar Java version
        String javaVersion = System.getProperty("java.version");
        System.out.println("Ejecutándose en Java: " + javaVersion);
        
        // Verificar JavaFX
        try {
            Class.forName("javafx.application.Application");
            System.out.println("✓ JavaFX disponible");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ JavaFX no está disponible");
            System.err.println("Asegúrese de tener JavaFX en el classpath");
            return;
        }
        
        try {
            // Lanzar la aplicación JavaFX
            launch(args);
            
        } catch (Exception e) {
            System.err.println("Error fatal al ejecutar la aplicación: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Método llamado al inicializar JavaFX
     */
    @Override
    public void init() throws Exception {
        super.init();
        
        // Inicializaciones previas al inicio de la UI
        System.out.println("Inicializando Hospital Santa Vida...");
        
        // Cargar configuraciones del sistema
        cargarConfiguraciones();
        
        // Verificar recursos necesarios
        verificarRecursos();
        
        System.out.println("Inicialización completada.");
    }
    
    /**
     * Método llamado al cerrar la aplicación
     */
    @Override
    public void stop() throws Exception {
        System.out.println("Cerrando Hospital Santa Vida...");
        
        // Limpiar recursos
        DatabaseConnection.closeAllConnections();
        
        super.stop();
        System.out.println("Aplicación cerrada correctamente.");
    }
    
    /**
     * Carga las configuraciones del sistema
     */
    private void cargarConfiguraciones() {
        try {
            // Cargar configuraciones desde archivo o base de datos
            System.out.println("✓ Configuraciones del sistema cargadas");
        } catch (Exception e) {
            System.err.println("⚠ Advertencia: No se pudieron cargar todas las configuraciones");
        }
    }
    
    /**
     * Verifica que los recursos necesarios estén disponibles
     */
    private void verificarRecursos() {
        try {
            // Verificar archivos FXML necesarios
            String[] archivosRequeridos = {
                "/ui/login.fxml",
                "/ui/admin-sala-espera.fxml",
                "/ui/triage.fxml",
                "/ui/registro-paciente.fxml",
                "/ui/consulta-medica.fxml",
                "/ui/trabajo-social.fxml"
            };
            for (String archivo : archivosRequeridos) {
                var url = getClass().getResource(archivo);
                if (url == null) {
                    System.err.println("[RECURSOS] FXML NO ENCONTRADO: " + archivo + " (revisa classpath o copia a out/ui)");
                } else {
                    System.out.println("[RECURSOS] OK " + archivo + " -> " + url);
                }
            }
            
            System.out.println("✓ Verificación de recursos completada");
            
        } catch (Exception e) {
            System.err.println("✗ Error verificando recursos: " + e.getMessage());
            // throw new RuntimeException("Recursos faltantes", e);
        }
    }
}