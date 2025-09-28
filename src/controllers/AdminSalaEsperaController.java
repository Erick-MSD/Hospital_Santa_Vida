package controllers;

import dao.PacienteDAO;
import models.*;
import services.TriageService;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Controlador para la sala de espera y panel de administración
 * Muestra el estado de todos los pacientes para que los familiares puedan ver el progreso
 * También permite al administrador navegar a todas las pantallas del sistema
 */
public class AdminSalaEsperaController extends BaseController implements Initializable {
    
    // Componentes FXML - Header y navegación
    @FXML private Button btnCerrarSesion;
    @FXML private Button btnActualizar;
    @FXML private MenuItem menuTriage;
    @FXML private MenuItem menuRegistro;
    @FXML private MenuItem menuTrabajoSocial;
    @FXML private MenuItem menuConsultaMedica;
    @FXML private MenuItem menuReportes;
    @FXML private MenuItem menuConfiguracion;
    
    // Componentes FXML - Estadísticas
    @FXML private Label lblTotalPacientes;
    @FXML private Label lblEsperandoTriage;
    @FXML private Label lblEsperandoMedico;
    @FXML private Label lblCompletados;
    
    // Componentes FXML - Tabla principal
    @FXML private TableView<PacienteSalaEspera> tablaPacientes;
    @FXML private TableColumn<PacienteSalaEspera, String> colFolio;
    @FXML private TableColumn<PacienteSalaEspera, String> colNombre;
    @FXML private TableColumn<PacienteSalaEspera, String> colEdad;
    @FXML private TableColumn<PacienteSalaEspera, String> colLlegada;
    @FXML private TableColumn<PacienteSalaEspera, String> colNivelTriage;
    @FXML private TableColumn<PacienteSalaEspera, String> colEstado;
    @FXML private TableColumn<PacienteSalaEspera, String> colTiempoEspera;
    @FXML private TableColumn<PacienteSalaEspera, String> colEspecialidad;
    @FXML private TableColumn<PacienteSalaEspera, String> colObservaciones;
    
    // Componentes FXML - Footer e información
    @FXML private Label lblUltimaActualizacion;
    @FXML private Label lblUsuarioActual;
    @FXML private Label lblEstadoConexion;
    @FXML private Label lblHoraActual;
    @FXML private Label lblFechaHeader;
    @FXML private Label lblRelojHeader;
    @FXML private Label lblActualizandose;
    
    // Servicios y datos
    private PacienteDAO pacienteDAO;
    private ObservableList<PacienteSalaEspera> datosPacientes;
    private ScheduledExecutorService scheduler;
    
    // Clase para representar datos de pacientes en la tabla
    public static class PacienteSalaEspera {
        private String folio;
        private String nombre;
        private String edad;
        private String horaLlegada;
        private String nivelTriage;
        private String estado;
        private String tiempoEspera;
        private String especialidad;
        private String observaciones;
        
        public PacienteSalaEspera(String folio, String nombre, String edad, String horaLlegada, 
                                 String nivelTriage, String estado, String tiempoEspera, 
                                 String especialidad, String observaciones) {
            this.folio = folio;
            this.nombre = nombre;
            this.edad = edad;
            this.horaLlegada = horaLlegada;
            this.nivelTriage = nivelTriage;
            this.estado = estado;
            this.tiempoEspera = tiempoEspera;
            this.especialidad = especialidad;
            this.observaciones = observaciones;
        }
        
        // Getters para JavaFX TableView
        public String getFolio() { return folio; }
        public String getNombre() { return nombre; }
        public String getEdad() { return edad; }
        public String getHoraLlegada() { return horaLlegada; }
        public String getNivelTriage() { return nivelTriage; }
        public String getEstado() { return estado; }
        public String getTiempoEspera() { return tiempoEspera; }
        public String getEspecialidad() { return especialidad; }
        public String getObservaciones() { return observaciones; }
    }
    
    public AdminSalaEsperaController() {
        this.pacienteDAO = new PacienteDAO();
        this.datosPacientes = FXCollections.observableArrayList();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🏥 Inicializando Panel de Administración - Sala de Espera...");
        
        // Configurar tabla
        configurarTabla();
        
        // Llamar métodos del BaseController
        super.initialize();
        
        // Cargar datos iniciales
        cargarDatosSalaEspera();
        
        // Iniciar actualización automática cada 30 segundos
        iniciarActualizacionAutomatica();
        
        // Iniciar reloj
        iniciarReloj();
    }
    
    @Override
    protected void configurarInterfaz() {
        try {
            // Verificar permisos de administrador
            if (authService != null) {
                authService.requireRole(Usuario.TipoUsuario.ADMINISTRADOR);
            }
            
            // Configurar información del usuario
            if (usuarioActual != null) {
                System.out.println("✅ Panel de administración configurado para: " + usuarioActual.getNombreCompleto());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al configurar interfaz de administración: " + e.getMessage());
        }
    }
    
    @Override
    protected void cargarDatos() {
        System.out.println("🔄 Cargando datos iniciales de sala de espera");
        cargarDatosSalaEspera();
    }
    
    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        try {
            colFolio.setCellValueFactory(new PropertyValueFactory<>("folio"));
            colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
            colEdad.setCellValueFactory(new PropertyValueFactory<>("edad"));
            colLlegada.setCellValueFactory(new PropertyValueFactory<>("horaLlegada"));
            colNivelTriage.setCellValueFactory(new PropertyValueFactory<>("nivelTriage"));
            colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
            colTiempoEspera.setCellValueFactory(new PropertyValueFactory<>("tiempoEspera"));
            colEspecialidad.setCellValueFactory(new PropertyValueFactory<>("especialidad"));
            colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
            
            // Configurar colores por nivel de triage
            colNivelTriage.setCellFactory(column -> new TableCell<PacienteSalaEspera, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setStyle(obtenerEstiloNivelTriage(item));
                    }
                }
            });
            
            tablaPacientes.setItems(datosPacientes);
            
            System.out.println("✅ Tabla de pacientes configurada correctamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error al configurar tabla: " + e.getMessage());
        }
    }
    
    /**
     * Carga los datos de la sala de espera
     */
    private void cargarDatosSalaEspera() {
        try {
            System.out.println("🔄 Actualizando datos de sala de espera...");
            
            datosPacientes.clear();
            
            // Obtener todos los registros activos del triage service
            if (triageService != null) {
                // Simular datos para demostración (en implementación real obtendría de la base de datos)
                List<PacienteSalaEspera> pacientesSimulados = generarDatosDemostracion();
                datosPacientes.addAll(pacientesSimulados);
                
                // Actualizar estadísticas
                actualizarEstadisticas(pacientesSimulados);
            }
            
            // Actualizar timestamp
            if (lblUltimaActualizacion != null) {
                lblUltimaActualizacion.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
            
            System.out.println("✅ Datos actualizados: " + datosPacientes.size() + " pacientes");
            
        } catch (Exception e) {
            System.err.println("❌ Error al cargar datos de sala de espera: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Genera datos de demostración para la sala de espera
     */
    private List<PacienteSalaEspera> generarDatosDemostracion() {
        List<PacienteSalaEspera> pacientes = new ArrayList<>();
        
        // Algunos pacientes de ejemplo con diferentes estados
        pacientes.add(new PacienteSalaEspera(
            "HSV-001", "María González López", "45 años", "08:30", 
            "ROJO", "EN ATENCION MEDICA", "2h 15m", "Cardiología", 
            "Dolor torácico agudo"
        ));
        
        pacientes.add(new PacienteSalaEspera(
            "HSV-002", "José Martínez Pérez", "32 años", "09:15", 
            "AMARILLO", "ESPERANDO MEDICO", "1h 30m", "Medicina General", 
            "Dolor abdominal moderado"
        ));
        
        pacientes.add(new PacienteSalaEspera(
            "HSV-003", "Ana Rodríguez Silva", "28 años", "09:45", 
            "VERDE", "ESPERANDO TRABAJO SOCIAL", "1h 00m", "Medicina General", 
            "Consulta de rutina"
        ));
        
        pacientes.add(new PacienteSalaEspera(
            "HSV-004", "Carlos Hernández Ruiz", "67 años", "10:20", 
            "NARANJA", "EN ATENCION MEDICA", "35m", "Neurología", 
            "Mareos y confusión"
        ));
        
        pacientes.add(new PacienteSalaEspera(
            "HSV-005", "Lucía Fernández Mora", "22 años", "10:30", 
            "AZUL", "ESPERANDO ASISTENTE", "25m", "Medicina General", 
            "Chequeo preventivo"
        ));
        
        return pacientes;
    }
    
    /**
     * Actualiza las estadísticas mostradas
     */
    private void actualizarEstadisticas(List<PacienteSalaEspera> pacientes) {
        try {
            int total = pacientes.size();
            int esperandoTriage = (int) pacientes.stream().filter(p -> p.getEstado().contains("ASISTENTE")).count();
            int esperandoMedico = (int) pacientes.stream().filter(p -> p.getEstado().contains("ESPERANDO MEDICO")).count();
            int completados = 8; // Simulado - completados hoy
            
            Platform.runLater(() -> {
                if (lblTotalPacientes != null) lblTotalPacientes.setText(String.valueOf(total));
                if (lblEsperandoTriage != null) lblEsperandoTriage.setText(String.valueOf(esperandoTriage));
                if (lblEsperandoMedico != null) lblEsperandoMedico.setText(String.valueOf(esperandoMedico));
                if (lblCompletados != null) lblCompletados.setText(String.valueOf(completados));
            });
            
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar estadísticas: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene el estilo CSS para el nivel de triage
     */
    private String obtenerEstiloNivelTriage(String nivel) {
        if (nivel == null) return "";
        
        switch (nivel.toUpperCase()) {
            case "ROJO":
                return "-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-font-weight: bold;";
            case "NARANJA":
                return "-fx-background-color: #FFF3E0; -fx-text-fill: #EF6C00; -fx-font-weight: bold;";
            case "AMARILLO":
                return "-fx-background-color: #FFFDE7; -fx-text-fill: #F57F17; -fx-font-weight: bold;";
            case "VERDE":
                return "-fx-background-color: #E8F5E8; -fx-text-fill: #2E7D32; -fx-font-weight: bold;";
            case "AZUL":
                return "-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0; -fx-font-weight: bold;";
            default:
                return "";
        }
    }
    
    /**
     * Inicia la actualización automática de datos
     */
    private void iniciarActualizacionAutomatica() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(this::cargarDatosSalaEspera);
        }, 30, 30, TimeUnit.SECONDS);
        
        System.out.println("🔄 Actualización automática iniciada (cada 30 segundos)");
    }
    
    /**
     * Inicia el reloj en tiempo real
     */
    private void iniciarReloj() {
        if (scheduler == null) {
            scheduler = Executors.newScheduledThreadPool(1);
        }
        
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                LocalDateTime ahora = LocalDateTime.now();
                String horaActual = ahora.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                String fechaActual = ahora.format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM yyyy", new java.util.Locale("es", "ES")));
                
                if (lblHoraActual != null) {
                    lblHoraActual.setText(horaActual);
                }
                if (lblRelojHeader != null) {
                    lblRelojHeader.setText(horaActual);
                }
                if (lblFechaHeader != null) {
                    lblFechaHeader.setText("📅 " + fechaActual);
                }
            });
        }, 0, 1, TimeUnit.SECONDS);
        
        System.out.println("🕐 Reloj en tiempo real iniciado");
    }
    
    /**
     * Maneja la actualización manual de datos
     */
    public void actualizarDatos() {
        try {
            System.out.println("🔄 Actualización manual solicitada");
            
            // Mostrar indicador de actualización
            mostrarIndicadorActualizacion(true);
            
            // Simular tiempo de carga
            new Thread(() -> {
                try {
                    Thread.sleep(1500); // Simular carga
                    Platform.runLater(() -> {
                        cargarDatosSalaEspera();
                        mostrarIndicadorActualizacion(false);
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            
        } catch (Exception e) {
            System.err.println("❌ Error en actualización manual: " + e.getMessage());
            mostrarIndicadorActualizacion(false);
        }
    }
    
    /**
     * Muestra/oculta el indicador de actualización
     */
    private void mostrarIndicadorActualizacion(boolean mostrar) {
        Platform.runLater(() -> {
            try {
                // Simular cambio del indicador (en implementación real usaría lblActualizandose)
                if (mostrar) {
                    System.out.println("💫 Mostrando indicador de actualización...");
                } else {
                    System.out.println("✅ Ocultando indicador de actualización");
                }
            } catch (Exception e) {
                System.err.println("Error al manejar indicador: " + e.getMessage());
            }
        });
    }
    
    // ==================== MÉTODOS DE NAVEGACIÓN ====================
    
    /**
     * Abre la pantalla de triage
     */
    @FXML
    private void abrirTriage() {
        System.out.println("🩺 Abriendo Evaluación de Triage...");
        abrirVentana("/ui/triage.fxml", "Evaluación de Triage - Hospital Santa Vida");
    }
    
    /**
     * Abre el registro de pacientes
     */
    @FXML
    private void abrirRegistroPaciente() {
        System.out.println("📋 Abriendo Registro de Pacientes...");
        abrirVentana("/ui/registro-paciente.fxml", "Registro de Pacientes - Hospital Santa Vida");
    }
    
    /**
     * Abre trabajo social
     */
    @FXML
    private void abrirTrabajoSocial() {
        System.out.println("🤝 Abriendo Trabajo Social...");
        abrirVentana("/ui/trabajo-social.fxml", "Trabajo Social - Hospital Santa Vida");
    }
    
    /**
     * Abre consulta médica
     */
    @FXML
    private void abrirConsultaMedica() {
        System.out.println("👨‍⚕️ Abriendo Consulta Médica...");
        abrirVentana("/ui/consulta-medica.fxml", "Consulta Médica - Hospital Santa Vida");
    }
    
    /**
     * Abre reportes (placeholder)
     */
    @FXML
    private void abrirReportes() {
        System.out.println("📊 Abriendo Reportes...");
        // Por ahora mostrar mensaje
        mostrarMensaje("🚧 Módulo de Reportes en desarrollo", "#FF9800");
    }
    
    /**
     * Abre configuración (placeholder)
     */
    @FXML
    private void abrirConfiguracion() {
        System.out.println("⚙️ Abriendo Configuración...");
        // Por ahora mostrar mensaje
        mostrarMensaje("🚧 Módulo de Configuración en desarrollo", "#FF9800");
    }
    
    /**
     * Método auxiliar para abrir ventanas
     */
    private void abrirVentana(String fxmlPath, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
            
            System.out.println("✅ Ventana abierta: " + titulo);
            
        } catch (IOException e) {
            System.err.println("❌ Error al abrir " + titulo + ": " + e.getMessage());
            mostrarMensaje("Error al abrir " + titulo, "#D32F2F");
        }
    }
    
    /**
     * Muestra un mensaje temporal
     */
    private void mostrarMensaje(String mensaje, String color) {
        System.out.println("💬 " + mensaje);
    }
    
    /**
     * Maneja el cierre de sesión
     */
    @FXML
    private void handleLogout() {
        try {
            System.out.println("🚪 Cerrando sesión de administrador...");
            
            // Detener scheduler si existe
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
            }
            
            // Obtener la ventana actual
            Stage currentStage = (Stage) btnCerrarSesion.getScene().getWindow();
            
            // Abrir pantalla de login
            LoginController.abrirLogin(currentStage);
            
            System.out.println("✅ Regresando al login...");
            
        } catch (Exception e) {
            System.err.println("❌ Error al cerrar sesión: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    protected void limpiarFormulario() {
        // No hay formulario que limpiar en esta pantalla
        System.out.println("🧹 Limpieza de datos de administrador");
        
        if (datosPacientes != null) {
            datosPacientes.clear();
        }
    }
    
    /**
     * Limpia recursos al cerrar
     */
    public void cleanup() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            System.out.println("🔄 Scheduler detenido");
        }
        super.cleanup();
    }
}