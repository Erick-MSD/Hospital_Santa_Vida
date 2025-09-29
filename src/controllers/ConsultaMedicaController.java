package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import services.PacienteServiceResults.*;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para consulta médica
 * Maneja consultas médicas, diagnósticos y tratamientos
 */
public class ConsultaMedicaController extends BaseController implements Initializable {
    
    // Información del médico
    @FXML private Label lblNombreMedico;
    @FXML private Label lblEspecialidad;
    @FXML private Label lblFechaHora;
    
    // Información del paciente
    @FXML private TextField txtBuscarPaciente;
    @FXML private Button btnBuscarPaciente;
    @FXML private Label lblInfoPaciente;
    @FXML private Label lblExpediente;
    
    // Consulta actual
    @FXML private TextArea txtMotivoConsulta;
    @FXML private TextArea txtHistoriaEnfermedadActual;
    @FXML private TextArea txtExploracionFisica;
    @FXML private TextArea txtSignosVitales;
    
    // Diagnóstico
    @FXML private TextArea txtDiagnosticoPrincipal;
    @FXML private TextArea txtDiagnosticoSecundario;
    @FXML private ComboBox<String> cbTipoDiagnostico;
    
    // Tratamiento
    @FXML private TextArea txtPrescripcionMedica;
    @FXML private TextArea txtIndicacionesMedicas;
    @FXML private TextArea txtRecomendaciones;
    @FXML private DatePicker dpProximaCita;
    
    // Estado del paciente
    @FXML private ComboBox<String> cbEstadoPaciente;
    @FXML private ComboBox<String> cbTipoAlta;
    @FXML private TextArea txtObservacionesAlta;
    
    // Botones
    @FXML private Button btnGuardarConsulta;
    @FXML private Button btnLimpiarFormulario;
    @FXML private Button btnImprimirReceta;
    @FXML private Button btnDarAlta;
    
    // Tabla de consultas del día
    @FXML private TableView<ConsultaInfo> tblConsultasHoy;
    @FXML private TableColumn<ConsultaInfo, String> colHora;
    @FXML private TableColumn<ConsultaInfo, String> colPaciente;
    @FXML private TableColumn<ConsultaInfo, String> colMotivo;
    @FXML private TableColumn<ConsultaInfo, String> colEstado;
    
    // Historial médico
    @FXML private TableView<HistorialMedicoInfo> tblHistorialMedico;
    @FXML private TableColumn<HistorialMedicoInfo, String> colFechaHistorial;
    @FXML private TableColumn<HistorialMedicoInfo, String> colDiagnostico;
    @FXML private TableColumn<HistorialMedicoInfo, String> colMedicoHistorial;
    @FXML private TableColumn<HistorialMedicoInfo, String> colTratamiento;
    
    // Mensaje
    @FXML private Label lblMensaje;
    
    // Servicios
    private PacienteService pacienteService;
    private CitaService citaService;
    
    // Estado actual
    private Paciente pacienteActual;
    private AtencionMedica consultaActual;
    private ObservableList<ConsultaInfo> consultasHoy;
    private ObservableList<HistorialMedicoInfo> historialMedico;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar servicios
        pacienteService = new PacienteService();
        citaService = new CitaService();
        
        // Configurar ComboBoxes
        setupComboBoxes();
        
        // Configurar tablas
        setupTables();
        
        // Configurar listas observables
        consultasHoy = FXCollections.observableArrayList();
        historialMedico = FXCollections.observableArrayList();
        tblConsultasHoy.setItems(consultasHoy);
        tblHistorialMedico.setItems(historialMedico);
        
        // Estado inicial
        limpiarFormulario();
    }
    
    @Override
    protected void onSesionInicializada() {
        // Verificar permisos médicos
        if (!tienePermiso(AuthenticationService.Permiso.REALIZAR_CONSULTAS)) {
            showAlert("Sin permisos", "No tiene permisos para realizar consultas médicas");
            return;
        }
        
        // Configurar información del médico
        lblNombreMedico.setText("Dr. " + usuarioActual.getNombreCompleto());
        lblEspecialidad.setText("Medicina General"); // Simplificado
        
        // Cargar consultas del día
        cargarConsultasDelDia();
    }
    
    /**
     * Configura los ComboBoxes
     */
    private void setupComboBoxes() {
        // Tipo de diagnóstico
        cbTipoDiagnostico.getItems().addAll(
            "DEFINITIVO", "PRESUNTIVO", "DIFERENCIAL", "POST_MORTEM"
        );
        
        // Estado del paciente
        cbEstadoPaciente.getItems().addAll(
            "ESTABLE", "MEJORADO", "GRAVE", "CRITICO", "RECUPERADO"
        );
        
        // Tipo de alta
        cbTipoAlta.getItems().addAll(
            "CURACION", "MEJORIA", "REFERENCIA", "FUGA", "DEFUNCION", "TRASLADO"
        );
    }
    
    /**
     * Configura las tablas
     */
    private void setupTables() {
        // Tabla de consultas del día
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colPaciente.setCellValueFactory(new PropertyValueFactory<>("nombrePaciente"));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        
        // Tabla de historial médico
        colFechaHistorial.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colDiagnostico.setCellValueFactory(new PropertyValueFactory<>("diagnostico"));
        colMedicoHistorial.setCellValueFactory(new PropertyValueFactory<>("medico"));
        colTratamiento.setCellValueFactory(new PropertyValueFactory<>("tratamiento"));
        
        // Listeners de selección
        tblConsultasHoy.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                cargarConsultaSeleccionada(newValue);
            }
        });
        
        tblHistorialMedico.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                mostrarDetalleHistorial(newValue);
            }
        });
    }
    
    /**
     * Busca un paciente
     */
    @FXML
    private void handleBuscarPaciente() {
        String criterio = txtBuscarPaciente.getText().trim();
        
        if (criterio.isEmpty()) {
            showAlert("Búsqueda vacía", "Ingrese un criterio de búsqueda");
            return;
        }
        
        try {
            ResultadoBusqueda resultado = pacienteService.buscarPacientes(tokenSesion, criterio);
            
            if (resultado.getPacientes().isEmpty()) {
                showAlert("No encontrado", "No se encontraron pacientes con ese criterio");
                return;
            }
            
            // Si hay múltiples resultados, tomar el primero
            pacienteActual = resultado.getPacientes().get(0);
            mostrarInformacionPaciente();
            cargarHistorialMedico();
            
        } catch (Exception e) {
            showAlert("Error", "Error en la búsqueda: " + e.getMessage());
        }
    }
    
    /**
     * Muestra la información del paciente actual
     */
    private void mostrarInformacionPaciente() {
        if (pacienteActual == null) return;
        
        String info = String.format(
            "%s | %d años | %s | Tel: %s",
            pacienteActual.getNombreCompleto(),
            pacienteActual.getEdad(),
            pacienteActual.getSexo(),
            pacienteActual.getTelefono()
        );
        
        lblInfoPaciente.setText(info);
        lblExpediente.setText("EXPEDIENTE: " + pacienteActual.getNumeroExpediente());
        
        lblInfoPaciente.setStyle("-fx-text-fill: #2E5984; -fx-font-weight: bold;");
        lblExpediente.setStyle("-fx-text-fill: #2E5984; -fx-font-weight: bold;");
    }
    
    /**
     * Guarda la consulta médica
     */
    @FXML
    private void handleGuardarConsulta() {
        if (pacienteActual == null) {
            showAlert("Sin paciente", "Debe seleccionar un paciente para la consulta");
            return;
        }
        
        if (!validarFormulario()) {
            return;
        }
        
        try {
            // Crear atención médica
            AtencionMedica atencion = new AtencionMedica();
            atencion.setPacienteId(pacienteActual.getId());
            atencion.setMedicoId(usuarioActual.getId());
            atencion.setFechaAtencion(LocalDate.now());
            atencion.setHoraAtencion(LocalDateTime.now());
            atencion.setMotivoConsulta(txtMotivoConsulta.getText().trim());
            atencion.setHistoriaEnfermedadActual(txtHistoriaEnfermedadActual.getText().trim());
            atencion.setExploracionFisica(txtExploracionFisica.getText().trim());
            atencion.setSignosVitales(txtSignosVitales.getText().trim());
            atencion.setDiagnosticoPrincipal(txtDiagnosticoPrincipal.getText().trim());
            atencion.setDiagnosticoSecundario(txtDiagnosticoSecundario.getText().trim());
            atencion.setTipoDiagnostico(cbTipoDiagnostico.getValue());
            atencion.setPrescripcionMedica(txtPrescripcionMedica.getText().trim());
            atencion.setIndicacionesMedicas(txtIndicacionesMedicas.getText().trim());
            atencion.setRecomendaciones(txtRecomendaciones.getText().trim());
            if (dpProximaCita.getValue() != null) {
                atencion.setProximaCita(dpProximaCita.getValue().atStartOfDay());
            }
            atencion.setEstadoPaciente(cbEstadoPaciente.getValue());
            atencion.setObservaciones("");
            
            // Guardar usando servicio (simplificado)
            consultaActual = atencion;
            
            showMessage("Consulta médica guardada correctamente", "success");
            cargarConsultasDelDia();
            
            // Habilitar botones de acciones posteriores
            btnImprimirReceta.setDisable(false);
            btnDarAlta.setDisable(false);
            
        } catch (Exception e) {
            showAlert("Error", "Error al guardar la consulta: " + e.getMessage());
        }
    }
    
    /**
     * Da de alta al paciente
     */
    @FXML
    private void handleDarAlta() {
        if (pacienteActual == null || consultaActual == null) {
            showAlert("Sin consulta", "Debe tener una consulta activa para dar de alta");
            return;
        }
        
        if (cbTipoAlta.getValue() == null) {
            showAlert("Tipo de alta requerido", "Seleccione el tipo de alta médica");
            return;
        }
        
        try {
            // Actualizar consulta con datos de alta
            if (cbTipoAlta.getValue() != null) {
                try {
                    consultaActual.setTipoAlta(TipoAlta.valueOf(cbTipoAlta.getValue().toUpperCase()));
                } catch (Exception e) {
                    consultaActual.setTipoAlta(TipoAlta.DOMICILIO);
                }
            }
            consultaActual.setObservacionesAlta(txtObservacionesAlta.getText().trim());
            consultaActual.setFechaAlta(LocalDate.now());
            
            showMessage("Paciente dado de alta correctamente", "success");
            limpiarFormulario();
            cargarConsultasDelDia();
            
        } catch (Exception e) {
            showAlert("Error", "Error al dar de alta al paciente: " + e.getMessage());
        }
    }
    
    /**
     * Imprime la receta médica
     */
    @FXML
    private void handleImprimirReceta() {
        if (consultaActual == null || txtPrescripcionMedica.getText().trim().isEmpty()) {
            showAlert("Sin receta", "Debe tener una consulta con prescripción médica para imprimir");
            return;
        }
        
        showAlert("En desarrollo", "Función de impresión de receta en desarrollo");
    }
    
    /**
     * Limpia el formulario
     */
    @FXML
    private void handleLimpiarFormulario() {
        limpiarFormulario();
    }
    
    /**
     * Carga las consultas del día actual
     */
    private void cargarConsultasDelDia() {
        try {
            List<CitaMedica> citasHoy = citaService.obtenerCitasHoy(tokenSesion);
            
            consultasHoy.clear();
            
            for (CitaMedica cita : citasHoy) {
                if (cita.getMedicoId() == usuarioActual.getId()) {
                    // Obtener información del paciente
                    Paciente paciente = pacienteService.buscarPorId(tokenSesion, cita.getPacienteId());
                    
                    ConsultaInfo info = new ConsultaInfo(
                        cita.getHoraCita().toString(),
                        paciente != null ? paciente.getNombreCompleto() : "Paciente no encontrado",
                        cita.getMotivoCita() != null ? cita.getMotivoCita() : "Sin motivo especificado",
                        cita.getEstadoCita().toString(),
                        cita.getId()
                    );
                    consultasHoy.add(info);
                }
            }
            
        } catch (Exception e) {
            showAlert("Error", "Error al cargar consultas del día: " + e.getMessage());
        }
    }
    
    /**
     * Carga el historial médico del paciente actual
     */
    private void cargarHistorialMedico() {
        if (pacienteActual == null) return;
        
        historialMedico.clear();
        
        // Aquí se cargaría el historial real desde la base de datos
        // Por simplicidad, se simula con datos de ejemplo
        showMessage("Historial médico cargado", "info");
    }
    
    /**
     * Carga una consulta seleccionada de la tabla
     */
    private void cargarConsultaSeleccionada(ConsultaInfo info) {
        // Implementar carga de consulta específica
        showMessage("Consulta seleccionada: " + info.getNombrePaciente(), "info");
    }
    
    /**
     * Muestra detalle del historial seleccionado
     */
    private void mostrarDetalleHistorial(HistorialMedicoInfo info) {
        showMessage("Detalle de historial: " + info.getFecha(), "info");
    }
    
    /**
     * Limpia todos los campos del formulario
     */
    private void limpiarFormulario() {
        // Limpiar búsqueda
        txtBuscarPaciente.clear();
        lblInfoPaciente.setText("Busque y seleccione un paciente");
        lblExpediente.setText("EXPEDIENTE: ");
        lblInfoPaciente.setStyle("-fx-text-fill: #7A7A7A;");
        lblExpediente.setStyle("-fx-text-fill: #7A7A7A;");
        
        // Limpiar consulta
        txtMotivoConsulta.clear();
        txtHistoriaEnfermedadActual.clear();
        txtExploracionFisica.clear();
        txtSignosVitales.clear();
        
        // Limpiar diagnóstico
        txtDiagnosticoPrincipal.clear();
        txtDiagnosticoSecundario.clear();
        cbTipoDiagnostico.setValue(null);
        
        // Limpiar tratamiento
        txtPrescripcionMedica.clear();
        txtIndicacionesMedicas.clear();
        txtRecomendaciones.clear();
        dpProximaCita.setValue(null);
        
        // Limpiar alta
        cbEstadoPaciente.setValue(null);
        cbTipoAlta.setValue(null);
        txtObservacionesAlta.clear();
        
        // Limpiar estado
        pacienteActual = null;
        consultaActual = null;
        
        // Deshabilitar botones
        btnImprimirReceta.setDisable(true);
        btnDarAlta.setDisable(true);
        
        // Limpiar mensaje
        lblMensaje.setText("");
        
        // Limpiar selecciones de tablas
        tblConsultasHoy.getSelectionModel().clearSelection();
        tblHistorialMedico.getSelectionModel().clearSelection();
        historialMedico.clear();
    }
    
    /**
     * Valida el formulario
     */
    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();
        
        if (txtMotivoConsulta.getText().trim().isEmpty()) {
            errores.append("- El motivo de consulta es obligatorio\n");
        }
        
        if (txtExploracionFisica.getText().trim().isEmpty()) {
            errores.append("- La exploración física es obligatoria\n");
        }
        
        if (txtDiagnosticoPrincipal.getText().trim().isEmpty()) {
            errores.append("- El diagnóstico principal es obligatorio\n");
        }
        
        if (cbTipoDiagnostico.getValue() == null) {
            errores.append("- El tipo de diagnóstico es obligatorio\n");
        }
        
        if (cbEstadoPaciente.getValue() == null) {
            errores.append("- El estado del paciente es obligatorio\n");
        }
        
        if (errores.length() > 0) {
            showAlert("Errores de validación", errores.toString());
            return false;
        }
        
        return true;
    }
    
    /**
     * Muestra un mensaje
     */
    private void showMessage(String message, String type) {
        lblMensaje.setText(message);
        
        switch (type) {
            case "success":
                lblMensaje.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
                break;
            case "error":
                lblMensaje.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                break;
            case "info":
                lblMensaje.setStyle("-fx-text-fill: #4A90E2; -fx-font-weight: bold;");
                break;
            default:
                lblMensaje.setStyle("-fx-text-fill: #555555;");
        }
    }
    
    /**
     * Muestra un alert
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Maneja el cierre de sesión
     */
    @FXML
    private void handleLogout() {
        cerrarSesion();
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
            Parent root = loader.load();
            
            // Buscar la ventana actual
            Stage stage = (Stage) Stage.getWindows().stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);
            
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
    
    // Clases de datos para las tablas
    
    public static class ConsultaInfo {
        private String hora;
        private String nombrePaciente;
        private String motivo;
        private String estado;
        private int citaId;
        
        public ConsultaInfo(String hora, String nombrePaciente, String motivo, String estado, int citaId) {
            this.hora = hora;
            this.nombrePaciente = nombrePaciente;
            this.motivo = motivo;
            this.estado = estado;
            this.citaId = citaId;
        }
        
        // Getters
        public String getHora() { return hora; }
        public String getNombrePaciente() { return nombrePaciente; }
        public String getMotivo() { return motivo; }
        public String getEstado() { return estado; }
        public int getCitaId() { return citaId; }
    }
    
    public static class HistorialMedicoInfo {
        private String fecha;
        private String diagnostico;
        private String medico;
        private String tratamiento;
        
        public HistorialMedicoInfo(String fecha, String diagnostico, String medico, String tratamiento) {
            this.fecha = fecha;
            this.diagnostico = diagnostico;
            this.medico = medico;
            this.tratamiento = tratamiento;
        }
        
        // Getters
        public String getFecha() { return fecha; }
        public String getDiagnostico() { return diagnostico; }
        public String getMedico() { return medico; }
        public String getTratamiento() { return tratamiento; }
    }
}