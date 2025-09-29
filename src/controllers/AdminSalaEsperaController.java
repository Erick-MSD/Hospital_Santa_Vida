package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.Window;
import models.*;
import services.*;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Controlador para el panel administrativo - sala de espera
 * Muestra estado en tiempo real de pacientes y estadísticas generales
 */
public class AdminSalaEsperaController extends BaseController implements Initializable {
    
    // Elementos de estadísticas
    @FXML private Label lblTotalPacientes;
    @FXML private Label lblPacientesTriageHoy;
    @FXML private Label lblConsultasHoy;
    @FXML private Label lblPacientesEspera;
    
    // Tabla de pacientes en triage
    @FXML private TableView<PacienteTriageInfo> tblPacientesTriage;
    @FXML private TableColumn<PacienteTriageInfo, String> colNombrePaciente;
    @FXML private TableColumn<PacienteTriageInfo, String> colUrgencia;
    @FXML private TableColumn<PacienteTriageInfo, String> colTiempoEspera;
    @FXML private TableColumn<PacienteTriageInfo, String> colEstado;
    
    // Tabla de citas programadas
    @FXML private TableView<CitaInfo> tblCitasHoy;
    @FXML private TableColumn<CitaInfo, String> colPacienteCita;
    @FXML private TableColumn<CitaInfo, String> colMedicoCita;
    @FXML private TableColumn<CitaInfo, String> colHoraCita;
    @FXML private TableColumn<CitaInfo, String> colEspecialidad;
    @FXML private TableColumn<CitaInfo, String> colEstadoCita;
    
    // Menú de navegación
    @FXML private MenuItem menuTriage;
    @FXML private MenuItem menuRegistro;
    @FXML private MenuItem menuTrabajoSocial;
    @FXML private MenuItem menuConsultaMedica;
    @FXML private MenuItem menuReportes;
    @FXML private MenuItem menuConfiguracion;
    
    // Botones
    @FXML private Button btnCerrarSesion;
    @FXML private Button btnActualizar;
    
    // Servicios
    private TriageService triageService;
    private PacienteService pacienteService;
    private CitaService citaService;
    private ReportesService reportesService;
    
    // Timer para actualizaciones automáticas
    private Timer updateTimer;
    
    // Listas observables para las tablas
    private ObservableList<PacienteTriageInfo> pacientesTriage;
    private ObservableList<CitaInfo> citasHoy;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar servicios
        triageService = new TriageService();
        pacienteService = new PacienteService();
        citaService = new CitaService();
        reportesService = new ReportesService();
        
        // Configurar tablas
        setupTables();
        
        // Configurar listas observables
        pacientesTriage = FXCollections.observableArrayList();
        citasHoy = FXCollections.observableArrayList();
        
        tblPacientesTriage.setItems(pacientesTriage);
        tblCitasHoy.setItems(citasHoy);
    }
    
    @Override
    protected void onSesionInicializada() {
        // Verificar permisos de administrador
        if (!tienePermiso(AuthenticationService.Permiso.VER_DASHBOARD_ADMIN)) {
            showAlert("Sin permisos", "No tiene permisos para acceder al panel administrativo");
            // Usar Platform.runLater para asegurar que la UI esté completamente cargada
            Platform.runLater(() -> handleLogout());
            return;
        }
        
        // Cargar datos iniciales
        cargarDatos();
        
        // Iniciar actualizaciones automáticas cada 30 segundos
        startAutoUpdate();
    }
    
    /**
     * Configura las tablas
     */
    private void setupTables() {
        // Configurar tabla de triage
        colNombrePaciente.setCellValueFactory(new PropertyValueFactory<>("nombrePaciente"));
        colUrgencia.setCellValueFactory(new PropertyValueFactory<>("urgencia"));
        colTiempoEspera.setCellValueFactory(new PropertyValueFactory<>("tiempoEspera"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        
        // Configurar tabla de citas
        colPacienteCita.setCellValueFactory(new PropertyValueFactory<>("nombrePaciente"));
        colMedicoCita.setCellValueFactory(new PropertyValueFactory<>("nombreMedico"));
        colHoraCita.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colEspecialidad.setCellValueFactory(new PropertyValueFactory<>("especialidad"));
        colEstadoCita.setCellValueFactory(new PropertyValueFactory<>("estado"));
        
        // Personalizar celdas de urgencia con colores
        colUrgencia.setCellFactory(column -> new TableCell<PacienteTriageInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "CRITICO":
                            setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-font-weight: bold;");
                            break;
                        case "ALTO":
                            setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00; -fx-font-weight: bold;");
                            break;
                        case "MEDIO":
                            setStyle("-fx-background-color: #fff8e1; -fx-text-fill: #f57f17; -fx-font-weight: bold;");
                            break;
                        case "BAJO":
                            setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }
    
    /**
     * Carga todos los datos del dashboard
     */
    private void cargarDatos() {
        if (!haySesionActiva()) return;
        
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> cargarEstadisticas());
                Platform.runLater(() -> cargarPacientesTriage());
                Platform.runLater(() -> cargarCitasHoy());
                return null;
            }
        };
        
        new Thread(loadTask).start();
    }
    
    /**
     * Carga las estadísticas generales
     */
    private void cargarEstadisticas() {
        try {
            // Obtener estadísticas de triage
            TriageService.EstadisticasTriage estadisticasTriage = triageService.obtenerEstadisticas(tokenSesion);
            
            if (estadisticasTriage != null) {
                lblTotalPacientes.setText(String.valueOf(estadisticasTriage.getTotalPacientesHoy()));
                lblPacientesTriageHoy.setText(String.valueOf(estadisticasTriage.getPacientesEvaluadosHoy()));
                lblPacientesEspera.setText(String.valueOf(estadisticasTriage.getPacientesEnEspera()));
            }
            
            // Obtener estadísticas de citas
            CitaService.EstadisticasCitas estadisticasCitas = citaService.obtenerEstadisticas(tokenSesion);
            
            if (estadisticasCitas != null) {
                int citasHoyCount = estadisticasCitas.getConteosPorEstado().stream()
                    .mapToInt(conteo -> conteo.getConteo())
                    .sum();
                lblConsultasHoy.setText(String.valueOf(citasHoyCount));
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar estadísticas: " + e.getMessage());
        }
    }
    
    /**
     * Carga los pacientes en triage
     */
    private void cargarPacientesTriage() {
        try {
            List<TriageService.PacienteEnEspera> pacientesEnEspera = triageService.obtenerPacientesEnEspera(tokenSesion);
            
            pacientesTriage.clear();
            
            for (TriageService.PacienteEnEspera paciente : pacientesEnEspera) {
                PacienteTriageInfo info = new PacienteTriageInfo(
                    paciente.getNombreCompleto(),
                    paciente.getNivelUrgencia().toString(),
                    calcularTiempoEspera(paciente.getFechaRegistro()),
                    "EN ESPERA"
                );
                pacientesTriage.add(info);
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar pacientes de triage: " + e.getMessage());
        }
    }
    
    /**
     * Carga las citas programadas para hoy
     */
    private void cargarCitasHoy() {
        try {
            List<CitaMedica> citas = citaService.obtenerCitasHoy(tokenSesion);
            
            citasHoy.clear();
            
            for (CitaMedica cita : citas) {
                // Obtener información del paciente y médico
                Paciente paciente = pacienteService.buscarPorId(tokenSesion, cita.getPacienteId());
                
                CitaInfo info = new CitaInfo(
                    paciente != null ? paciente.getNombreCompleto() : "Paciente no encontrado",
                    "Dr. " + obtenerNombreMedico(cita.getMedicoId()),
                    cita.getHoraCita().format(DateTimeFormatter.ofPattern("HH:mm")),
                    cita.getEspecialidad() != null ? cita.getEspecialidad().toString() : "No especificada",
                    cita.getEstadoCita().toString()
                );
                citasHoy.add(info);
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar citas de hoy: " + e.getMessage());
        }
    }
    
    /**
     * Calcula el tiempo de espera desde una fecha
     */
    private String calcularTiempoEspera(LocalDateTime fechaInicio) {
        if (fechaInicio == null) return "N/A";
        
        LocalDateTime ahora = LocalDateTime.now();
        long minutos = java.time.Duration.between(fechaInicio, ahora).toMinutes();
        
        if (minutos < 60) {
            return minutos + " min";
        } else {
            long horas = minutos / 60;
            long minutosRestantes = minutos % 60;
            return horas + "h " + minutosRestantes + "m";
        }
    }
    
    /**
     * Obtiene el nombre del médico por ID
     */
    private String obtenerNombreMedico(int medicoId) {
        // Aquí podrías usar un servicio de usuarios para obtener el nombre
        return "Médico " + medicoId; // Simplificado para el ejemplo
    }
    
    /**
     * Inicia las actualizaciones automáticas
     */
    private void startAutoUpdate() {
        updateTimer = new Timer(true);
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> cargarDatos());
            }
        }, 30000, 30000); // Cada 30 segundos
    }
    
    /**
     * Para las actualizaciones automáticas
     */
    private void stopAutoUpdate() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }
    
    // Manejadores de eventos del menú
    
    @FXML
    private void abrirTriage() {
        navigateToInterface("/ui/triage.fxml", "Hospital Santa Vida - Triage");
    }
    
    @FXML
    private void abrirRegistroPaciente() {
        navigateToInterface("/ui/registro-paciente.fxml", "Hospital Santa Vida - Registro de Pacientes");
    }
    
    @FXML
    private void abrirTrabajoSocial() {
        navigateToInterface("/ui/trabajo-social.fxml", "Hospital Santa Vida - Trabajo Social");
    }
    
    @FXML
    private void abrirConsultaMedica() {
        navigateToInterface("/ui/consulta-medica.fxml", "Hospital Santa Vida - Consulta Médica");
    }
    
    @FXML
    private void abrirReportes() {
        // Implementar ventana de reportes
        showAlert("En desarrollo", "Funcionalidad de reportes en desarrollo");
    }
    
    @FXML
    private void abrirConfiguracion() {
        // Implementar ventana de configuración
        showAlert("En desarrollo", "Funcionalidad de configuración en desarrollo");
    }
    
    @FXML
    private void handleActualizar() {
        cargarDatos();
        showAlert("Actualizado", "Los datos han sido actualizados correctamente");
    }
    
    @FXML
    private void handleLogout() {
        stopAutoUpdate();
        cerrarSesion();
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
            Parent root = loader.load();
            
            // Verificar que btnCerrarSesion y su scene no sean null
            Stage stage = null;
            if (btnCerrarSesion != null && btnCerrarSesion.getScene() != null) {
                stage = (Stage) btnCerrarSesion.getScene().getWindow();
            } else {
                // Si no podemos obtener el stage desde el botón, buscar la ventana actual
                stage = (Stage) Stage.getWindows().stream()
                    .filter(Window::isShowing)
                    .findFirst()
                    .orElse(null);
            }
            
            if (stage != null) {
                stage.setTitle("Hospital Santa Vida - Login");
                stage.setScene(new Scene(root));
                stage.centerOnScreen();
                stage.setMaximized(false);
            }
            
        } catch (IOException e) {
            System.err.println("Error al cargar ventana de login: " + e.getMessage());
        }
    }
    
    /**
     * Navega a otra interfaz
     */
    private void navigateToInterface(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Object controller = loader.getController();
            if (controller instanceof BaseController) {
                ((BaseController) controller).inicializarSesion(usuarioActual, tokenSesion);
            }
            
            Stage stage = (Stage) btnCerrarSesion.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            
        } catch (IOException e) {
            showAlert("Error", "Error al cargar la interfaz: " + e.getMessage());
        }
    }
    
    /**
     * Muestra un alert
     */
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    // Clases de datos para las tablas
    
    public static class PacienteTriageInfo {
        private String nombrePaciente;
        private String urgencia;
        private String tiempoEspera;
        private String estado;
        
        public PacienteTriageInfo(String nombrePaciente, String urgencia, String tiempoEspera, String estado) {
            this.nombrePaciente = nombrePaciente;
            this.urgencia = urgencia;
            this.tiempoEspera = tiempoEspera;
            this.estado = estado;
        }
        
        // Getters
        public String getNombrePaciente() { return nombrePaciente; }
        public String getUrgencia() { return urgencia; }
        public String getTiempoEspera() { return tiempoEspera; }
        public String getEstado() { return estado; }
    }
    
    public static class CitaInfo {
        private String nombrePaciente;
        private String nombreMedico;
        private String hora;
        private String especialidad;
        private String estado;
        
        public CitaInfo(String nombrePaciente, String nombreMedico, String hora, String especialidad, String estado) {
            this.nombrePaciente = nombrePaciente;
            this.nombreMedico = nombreMedico;
            this.hora = hora;
            this.especialidad = especialidad;
            this.estado = estado;
        }
        
        // Getters
        public String getNombrePaciente() { return nombrePaciente; }
        public String getNombreMedico() { return nombreMedico; }
        public String getHora() { return hora; }
        public String getEspecialidad() { return especialidad; }
        public String getEstado() { return estado; }
    }
}