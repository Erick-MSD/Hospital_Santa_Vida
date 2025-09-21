package test;
/**
 * Test simple para verificar si JavaFX está disponible
 */
public class TestJavaFX {
    
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("     VERIFICACIÓN DE JAVAFX");
        System.out.println("==============================================");
        
        // Test 1: Verificar si las clases de JavaFX están disponibles
        System.out.println("1. Verificando disponibilidad de clases JavaFX...");
        
        try {
            Class.forName("javafx.application.Application");
            System.out.println("   ✅ javafx.application.Application - Encontrada");
        } catch (ClassNotFoundException e) {
            System.out.println("   ❌ javafx.application.Application - NO encontrada");
        }
        
        try {
            Class.forName("javafx.scene.Scene");
            System.out.println("   ✅ javafx.scene.Scene - Encontrada");
        } catch (ClassNotFoundException e) {
            System.out.println("   ❌ javafx.scene.Scene - NO encontrada");
        }
        
        try {
            Class.forName("javafx.stage.Stage");
            System.out.println("   ✅ javafx.stage.Stage - Encontrada");
        } catch (ClassNotFoundException e) {
            System.out.println("   ❌ javafx.stage.Stage - NO encontrada");
        }
        
        try {
            Class.forName("javafx.scene.control.Button");
            System.out.println("   ✅ javafx.scene.control.Button - Encontrada");
        } catch (ClassNotFoundException e) {
            System.out.println("   ❌ javafx.scene.control.Button - NO encontrada");
        }
        
        try {
            Class.forName("javafx.fxml.FXMLLoader");
            System.out.println("   ✅ javafx.fxml.FXMLLoader - Encontrada");
        } catch (ClassNotFoundException e) {
            System.out.println("   ❌ javafx.fxml.FXMLLoader - NO encontrada");
        }
        
        // Test 2: Verificar propiedades del sistema
        System.out.println("\n2. Información del sistema:");
        System.out.println("   Java Version: " + System.getProperty("java.version"));
        System.out.println("   Java Home: " + System.getProperty("java.home"));
        System.out.println("   OS: " + System.getProperty("os.name"));
        System.out.println("   Arch: " + System.getProperty("os.arch"));
        
        // Test 3: Verificar module path
        System.out.println("\n3. Module Path:");
        String modulePath = System.getProperty("jdk.module.path");
        if (modulePath != null) {
            System.out.println("   Module Path: " + modulePath);
        } else {
            System.out.println("   ⚠️ Module Path no configurado");
        }
        
        System.out.println("\n==============================================");
        System.out.println("     VERIFICACIÓN COMPLETADA");
        System.out.println("==============================================");
        
        // Sugerencias basadas en los resultados
        System.out.println("\n💡 PRÓXIMOS PASOS:");
        System.out.println("   1. Si las clases NO se encuentran:");
        System.out.println("      - Descargar JavaFX SDK 24 desde gluonhq.com");
        System.out.println("      - Extraer en C:\\javafx-sdk-24");
        System.out.println("   2. Para compilar con JavaFX:");
        System.out.println("      javac --module-path C:\\javafx-sdk-24\\lib --add-modules javafx.controls,javafx.fxml -cp lib/* -d out src/**/*.java");
        System.out.println("   3. Para ejecutar con JavaFX:");
        System.out.println("      java --module-path C:\\javafx-sdk-24\\lib --add-modules javafx.controls,javafx.fxml -cp \"out;lib/*\" MiApp");
    }
}