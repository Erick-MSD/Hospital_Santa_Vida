package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.*;
import services.*;
import services.PacienteServiceResults.*;
import services.TriageServiceResults.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la interfaz de triage
 * Maneja evaluación de pacientes y clasificación por urgencia
 */
public class TriageController extends BaseController implements Initializable {
    
    // Información del doctor
    @FXML private Label lblDoctorName;
    @FXML private Label lblDateTime;
    @FXML private Label lblTotalPacientes;
    @FXML private Label lblDoctorRole;
    
    // Información básica
    @FXML private TextField txtNumeroFolio;
    @FXML private TextField txtFechaIngreso;
    @FXML private TextArea txtMotivoConsulta;
    
    // Formulario de evaluación
    @FXML private TextField txtBuscarPaciente;
    @FXML private Button btnBuscarPaciente;
    @FXML private Label lblPacienteInfo;
    
    // Signos vitales - nombres corregidos según FXML
    @FXML private TextField txtPresionArterial;
    @FXML private TextField txtFrecuenciaCardiaca;
    @FXML private TextField txtTemperatura;
    @FXML private TextField txtFrecuenciaRespiratoria;
    @FXML private TextField txtSaturacionO2;
    @FXML private TextField txtDolor;
    @FXML private TextField txtGlasgow;
    
    // Observaciones clínicas
    @FXML private TextArea txtObservacionesClinicas;
    
    // Evaluación clínica
    @FXML private Slider sliderDolor;
    @FXML private Slider sliderGlasgow;
    @FXML private TextArea txtSintomas;
    
    // Resultado de evaluación
    @FXML private Label lblNivelUrgencia;
    @FXML private Label lblJustificacion;
    
    // Botones de triage
    @FXML private Button btnTriageRojo;
    @FXML private Button btnTriageNaranja;
    @FXML private Button btnTriageAmarillo;
    @FXML private Button btnTriageVerde;
    @FXML private Button btnTriageAzul;
    @FXML private Label lblTriageSeleccionado;
    
    // Botones de especialidades
    @FXML private Button btnEspecialidadGeneral;
    @FXML private Button btnEspecialidadCardiologia;
    @FXML private Button btnEspecialidadPediatria;
    @FXML private Button btnEspecialidadTraumatologia;
    @FXML private Button btnEspecialidadGinecologia;
    @FXML private Button btnEspecialidadNeurologia;
    @FXML private Button btnEspecialidadPsiquiatria;
    @FXML private Button btnEspecialidadDermatologia;
    
    // Botones de acción
    @FXML private Button btnCancelar;
    @FXML private Button btnCompletarEvaluacion;
    @FXML private Button btnCerrarSesion;
    @FXML private Button btnEvaluar;
    @FXML private Button btnGuardar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnSiguientePaciente;
    
    // Cola de triage
    @FXML private TableView<PacienteColaInfo> tblColaTriage;
    @FXML private TableColumn<PacienteColaInfo, String> colNombre;
    @FXML private TableColumn<PacienteColaInfo, String> colExpediente;
    @FXML private TableColumn<PacienteColaInfo, String> colUrgencia;
    @FXML private TableColumn<PacienteColaInfo, String> colTiempo;
    
    // Servicios
    private TriageService triageService;
    private PacienteService pacienteService;
    
    // Estado actual
    private Paciente pacienteActual;
    private RegistroTriage evaluacionActual;
    private ObservableList<PacienteColaInfo> colaPacientes;
    
    // Selecciones temporales (sin guardar hasta confirmar)
    private NivelUrgencia nivelSeleccionado;
    private String especialidadSeleccionada;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar servicios
        triageService = new TriageService();
        pacienteService = new PacienteService();
        
        // Configurar tabla - TEMPORALMENTE COMENTADO
        // setupTable();
        
        // Configurar lista observable
        colaPacientes = FXCollections.observableArrayList();
        // tblColaTriage.setItems(colaPacientes); // Tabla comentada por ahora
        
        // Configurar sliders
        setupSliders();
        
        // Actualizar fecha/hora
        actualizarFechaHora();
        
        // Limpiar formulario inicial
        limpiarFormulario();
    }
    
    @Override
    protected void onSesionInicializada() {
        // Verificar permisos de triage
        if (!tienePermiso(AuthenticationService.Permiso.REALIZAR_TRIAGE)) {
            showAlert("Sin permisos", "No tiene permisos para realizar evaluaciones de triage");
            return;
        }
        
        // Configurar información del usuario
        lblDoctorName.setText(usuarioActual.getNombreCompleto());
        
        // Cargar cola de triage - TEMPORALMENTE COMENTADO
        // cargarColaTriage();
    }
    
    /**
     * Configura la tabla de cola de triage
     */
    private void setupTable() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colExpediente.setCellValueFactory(new PropertyValueFactory<>("expediente"));
        colUrgencia.setCellValueFactory(new PropertyValueFactory<>("urgencia"));
        colTiempo.setCellValueFactory(new PropertyValueFactory<>("tiempoEspera"));
        
        // Personalizar celda de urgencia con colores
        colUrgencia.setCellFactory(column -> new TableCell<PacienteColaInfo, String>() {
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
                            setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828; -fx-font-weight: bold;");
                            break;
                        case "ALTO":
                            setStyle("-fx-background-color: #ffe0b2; -fx-text-fill: #ef6c00; -fx-font-weight: bold;");
                            break;
                        case "MEDIO":
                            setStyle("-fx-background-color: #fff9c4; -fx-text-fill: #f57f17; -fx-font-weight: bold;");
                            break;
                        case "BAJO":
                            setStyle("-fx-background-color: #c8e6c9; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                            break;
                        case "PENDIENTE":
                            setStyle("-fx-background-color: #e1f5fe; -fx-text-fill: #0277bd; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });
        
        // Listener para selección en tabla
        tblColaTriage.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                cargarPacienteDesdeTabla(newValue);
            }
        });
    }
    
    /**
     * Configura los sliders
     */
    private void setupSliders() {
        // Slider de dolor (0-10)
        sliderDolor.setMin(0);
        sliderDolor.setMax(10);
        sliderDolor.setValue(0);
        sliderDolor.setMajorTickUnit(2);
        sliderDolor.setMinorTickCount(1);
        sliderDolor.setShowTickLabels(true);
        sliderDolor.setShowTickMarks(true);
        
        // Slider de Glasgow (3-15)
        sliderGlasgow.setMin(3);
        sliderGlasgow.setMax(15);
        sliderGlasgow.setValue(15);
        sliderGlasgow.setMajorTickUnit(3);
        sliderGlasgow.setMinorTickCount(2);
        sliderGlasgow.setShowTickLabels(true);
        sliderGlasgow.setShowTickMarks(true);
    }
    
    /**
     * Carga la cola de triage
     */
    private void cargarColaTriage() {
        try {
            // Verificar que la lista esté inicializada
            if (colaPacientes == null) {
                colaPacientes = FXCollections.observableArrayList();
            }
            
            List<TriageService.PacienteEnEspera> pacientesEnEspera = triageService.obtenerPacientesEnEspera(tokenSesion);
            
            colaPacientes.clear();
            
            for (TriageService.PacienteEnEspera paciente : pacientesEnEspera) {
                PacienteColaInfo info = new PacienteColaInfo(
                    paciente.getNombreCompleto(),
                    paciente.getNumeroExpediente(),
                    paciente.getNivelUrgencia() != null ? paciente.getNivelUrgencia().toString() : "PENDIENTE",
                    calcularTiempoEspera(paciente.getFechaRegistro()),
                    paciente.getId()
                );
                colaPacientes.add(info);
            }
            
            lblTotalPacientes.setText(String.valueOf(colaPacientes.size()));
            
        } catch (Exception e) {
            showAlert("Error", "Error al cargar la cola de triage: " + e.getMessage());
        }
    }
    
    /**
     * Carga un paciente desde la tabla
     */
    private void cargarPacienteDesdeTabla(PacienteColaInfo info) {
        try {
            pacienteActual = pacienteService.buscarPorId(tokenSesion, info.getPacienteId());
            
            if (pacienteActual != null) {
                mostrarInformacionPaciente();
                limpiarEvaluacion();
            }
            
        } catch (Exception e) {
            showAlert("Error", "Error al cargar información del paciente: " + e.getMessage());
        }
    }
    
    /**
     * Busca un paciente por expediente o nombre
     */
    @FXML
    private void handleBuscarPaciente() {
        String criterio = txtBuscarPaciente.getText().trim();
        
        if (criterio.isEmpty()) {
            showAlert("Búsqueda vacía", "Ingrese un número de expediente o nombre para buscar");
            return;
        }
        
        try {
            ResultadoBusqueda resultado = pacienteService.buscarPacientes(tokenSesion, criterio);
            
            if (resultado.getPacientes().isEmpty()) {
                showAlert("No encontrado", "No se encontraron pacientes con ese criterio");
                return;
            }
            
            // Si hay múltiples resultados, tomar el primero por simplicidad
            pacienteActual = resultado.getPacientes().get(0);
            mostrarInformacionPaciente();
            limpiarEvaluacion();
            
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
            "EXPEDIENTE: %s | NOMBRE: %s | EDAD: %d años | SEXO: %s",
            pacienteActual.getNumeroExpediente(),
            pacienteActual.getNombreCompleto(),
            pacienteActual.getEdad(),
            pacienteActual.getSexo()
        );
        
        lblPacienteInfo.setText(info);
        lblPacienteInfo.setStyle("-fx-text-fill: #2E5984; -fx-font-weight: bold;");
    }
    
    /**
     * Evalúa la urgencia del paciente
     */
    @FXML
    private void handleEvaluar() {
        if (pacienteActual == null) {
            showAlert("Sin paciente", "Debe seleccionar un paciente para evaluar");
            return;
        }
        
        try {
            // Crear datos de evaluación
            DatosEvaluacionTriage datos = new DatosEvaluacionTriage();
            datos.setPacienteId(pacienteActual.getId());
            // Presion arterial manejada como un solo campo con formato "120/80"
            String presionText = txtPresionArterial.getText().trim();
            if (presionText.contains("/")) {
                String[] presion = presionText.split("/");
                datos.setPresionSistolica(Integer.valueOf(presion[0].trim()));
                datos.setPresionDiastolica(Integer.valueOf(presion[1].trim()));
            }
            // Presion diastolica ya manejada arriba
            datos.setFrecuenciaCardiaca(Integer.valueOf(txtFrecuenciaCardiaca.getText().trim()));
            datos.setTemperatura(Double.valueOf(txtTemperatura.getText().trim()));
            datos.setSaturacionOxigeno(Integer.valueOf(txtSaturacionO2.getText().trim()));
            datos.setFrecuenciaRespiratoria(Integer.valueOf(txtFrecuenciaRespiratoria.getText().trim()));
            datos.setNivelDolor((int) sliderDolor.getValue());
            datos.setEscalaGlasgow((int) sliderGlasgow.getValue());
            datos.setSintomasPrincipales(txtSintomas.getText().trim());
                        datos.setObservaciones(txtObservacionesClinicas.getText().trim());
            
            // Evaluar urgencia
            ResultadoEvaluacion resultado = triageService.evaluarUrgencia(tokenSesion, datos);
            
            if (resultado.isExitoso()) {
                evaluacionActual = resultado.getRegistroTriage();
                mostrarResultadoEvaluacion(resultado);
                if (btnGuardar != null) {
                    btnGuardar.setDisable(false);
                }
            } else {
                showAlert("Error en evaluación", resultado.getMensaje());
            }
            
        } catch (NumberFormatException e) {
            showAlert("Datos inválidos", "Verifique que todos los campos numéricos tengan valores válidos");
        } catch (Exception e) {
            showAlert("Error", "Error durante la evaluación: " + e.getMessage());
        }
    }
    
    /**
     * Muestra el resultado de la evaluación
     */
    private void mostrarResultadoEvaluacion(TriageServiceResults.ResultadoEvaluacion resultado) {
        NivelUrgencia urgencia = resultado.getRegistroTriage().getNivelUrgencia();
        
        lblNivelUrgencia.setText(urgencia.toString());
        lblJustificacion.setText(resultado.getJustificacion());
        
        // Configurar color según urgencia
        String colorStyle;
        switch (urgencia) {
            case CRITICO:
                colorStyle = "-fx-text-fill: #c62828; -fx-font-weight: bold; -fx-font-size: 18px;";
                break;
            case ALTO:
                colorStyle = "-fx-text-fill: #ef6c00; -fx-font-weight: bold; -fx-font-size: 18px;";
                break;
            case MEDIO:
                colorStyle = "-fx-text-fill: #f57f17; -fx-font-weight: bold; -fx-font-size: 18px;";
                break;
            case BAJO:
                colorStyle = "-fx-text-fill: #2e7d32; -fx-font-weight: bold; -fx-font-size: 18px;";
                break;
            default:
                colorStyle = "-fx-text-fill: #0277bd; -fx-font-weight: bold; -fx-font-size: 18px;";
        }
        
        lblNivelUrgencia.setStyle(colorStyle);
        lblJustificacion.setStyle("-fx-text-fill: #555555; -fx-font-style: italic;");
    }
    
    /**
     * Guarda la evaluación de triage
     */
    @FXML
    private void handleGuardar() {
        if (evaluacionActual == null) {
            showAlert("Sin evaluación", "Debe evaluar al paciente antes de guardar");
            return;
        }
        
        try {
            boolean guardado = triageService.guardarEvaluacion(tokenSesion, evaluacionActual);
            
            if (guardado) {
                showAlert("Guardado exitoso", "La evaluación de triage ha sido guardada correctamente");
                cargarColaTriage(); // Actualizar cola
                limpiarFormulario();
                btnSiguientePaciente.setDisable(false);
            } else {
                showAlert("Error al guardar", "No se pudo guardar la evaluación");
            }
            
        } catch (Exception e) {
            showAlert("Error", "Error al guardar la evaluación: " + e.getMessage());
        }
    }
    
    /**
     * Limpia el formulario completo
     */
    @FXML
    private void handleLimpiar() {
        limpiarFormulario();
    }
    
    /**
     * Pasa al siguiente paciente en la cola
     */
    @FXML
    private void handleSiguientePaciente() {
        if (!colaPacientes.isEmpty()) {
            // Seleccionar el primer paciente pendiente
            for (PacienteColaInfo paciente : colaPacientes) {
                if ("PENDIENTE".equals(paciente.getUrgencia())) {
                    tblColaTriage.getSelectionModel().select(paciente);
                    cargarPacienteDesdeTabla(paciente);
                    break;
                }
            }
        }
    }
    
    /**
     * Limpia el formulario de evaluación
     */
    private void limpiarFormulario() {
        // Limpiar campos de paciente
        txtBuscarPaciente.clear();
        lblPacienteInfo.setText("Seleccione un paciente para evaluar");
        lblPacienteInfo.setStyle("-fx-text-fill: #7A7A7A;");
        
        limpiarEvaluacion();
        
        pacienteActual = null;
        evaluacionActual = null;
        
        // Limpiar selecciones temporales
        nivelSeleccionado = null;
        especialidadSeleccionada = null;
        lblTriageSeleccionado.setText("Nivel: No seleccionado");
        lblTriageSeleccionado.setStyle("-fx-text-fill: #7A7A7A;");
    }
    
    /**
     * Limpia solo los campos de evaluación
     */
    private void limpiarEvaluacion() {
        // Limpiar signos vitales
        txtPresionArterial.clear();
        txtFrecuenciaCardiaca.clear();
        txtTemperatura.clear();
        txtSaturacionO2.clear();
        txtFrecuenciaRespiratoria.clear();
        
        // Resetear sliders
        sliderDolor.setValue(0);
        sliderGlasgow.setValue(15);
        
        // Limpiar texto
        txtSintomas.clear();
                txtObservacionesClinicas.clear();
        
        // Limpiar resultado
        lblNivelUrgencia.setText("PENDIENTE");
        lblNivelUrgencia.setStyle("-fx-text-fill: #0277bd; -fx-font-weight: bold; -fx-font-size: 18px;");
        lblJustificacion.setText("");
        
        // Deshabilitar botones si existen
        if (btnGuardar != null) {
            btnGuardar.setDisable(true);
        }
        if (btnSiguientePaciente != null) {
            btnSiguientePaciente.setDisable(true);
        }
    }
    
    /**
     * Actualiza la fecha y hora actual
     */
    private void actualizarFechaHora() {
        String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        lblDateTime.setText(fechaHora);
    }
    
    /**
     * Calcula el tiempo de espera
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
    
    /**
     * Manejadores para los botones de clasificación de triage
     */
    @FXML
    private void handleTriageRojo() {
        clasificarPaciente(NivelUrgencia.ROJO, "Crítico - Atención Inmediata");
    }
    
    @FXML
    private void handleTriageNaranja() {
        clasificarPaciente(NivelUrgencia.NARANJA, "Urgente - 15 minutos");
    }
    
    @FXML
    private void handleTriageAmarillo() {
        clasificarPaciente(NivelUrgencia.AMARILLO, "Menos Urgente - 30 minutos");
    }
    
    @FXML
    private void handleTriageVerde() {
        clasificarPaciente(NivelUrgencia.VERDE, "No Urgente - 60 minutos");
    }
    
    @FXML
    private void handleTriageAzul() {
        clasificarPaciente(NivelUrgencia.AZUL, "Sin Urgencia - Programar cita");
    }
    
    /**
     * Método auxiliar para clasificar paciente con el nivel de urgencia seleccionado
     */
    private void clasificarPaciente(NivelUrgencia nivel, String descripcion) {
        try {
            if (pacienteActual == null) {
                showAlert("Error", "Debe seleccionar un paciente primero");
                return;
            }
            
            // Solo marcar la selección, no guardar todavía
            nivelSeleccionado = nivel;
            lblTriageSeleccionado.setText("Nivel: " + nivel.name() + " - " + descripcion);
            lblTriageSeleccionado.setStyle("-fx-text-fill: " + obtenerColorNivel(nivel) + ";");
            
            // Mostrar mensaje informativo
            showAlert("Nivel seleccionado", "Nivel de triage seleccionado. Ahora seleccione la especialidad médica.");
            
        } catch (Exception e) {
            showAlert("Error", "Error al seleccionar nivel de triage: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Obtiene el color CSS para cada nivel de triage
     */
    private String obtenerColorNivel(NivelUrgencia nivel) {
        switch (nivel) {
            case ROJO: return "#FF0000";
            case NARANJA: return "#FF8C00";
            case AMARILLO: return "#FFD700";
            case VERDE: return "#32CD32";
            case AZUL: return "#0080FF";
            default: return "#000000";
        }
    }
    
    /**
     * Guarda la evaluación de triage en la base de datos
     */
    private void guardarEvaluacionTriage() {
        try {
            boolean resultado = triageService.guardarEvaluacion(getTokenSesion(), evaluacionActual);
            
            if (resultado) {
                showAlert("Éxito", "Evaluación de triage guardada correctamente");
                limpiarFormulario();
                cargarColaTriage();
            } else {
                showAlert("Error", "Error al guardar la evaluación de triage");
            }
            
        } catch (Exception e) {
            showAlert("Error", "Error al guardar evaluación: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Manejadores para los botones de especialidades médicas
     */
    @FXML
    private void handleEspecialidadGeneral() {
        seleccionarEspecialidad("Medicina General");
    }
    
    @FXML
    private void handleEspecialidadCardiologia() {
        seleccionarEspecialidad("Cardiología");
    }
    
    @FXML
    private void handleEspecialidadPediatria() {
        seleccionarEspecialidad("Pediatría");
    }
    
    @FXML
    private void handleEspecialidadTraumatologia() {
        seleccionarEspecialidad("Traumatología");
    }
    
    @FXML
    private void handleEspecialidadGinecologia() {
        seleccionarEspecialidad("Ginecología");
    }
    
    @FXML
    private void handleEspecialidadNeurologia() {
        seleccionarEspecialidad("Neurología");
    }
    
    @FXML
    private void handleEspecialidadPsiquiatria() {
        seleccionarEspecialidad("Psiquiatría");
    }
    
    @FXML
    private void handleEspecialidadDermatologia() {
        seleccionarEspecialidad("Dermatología");
    }
    
    /**
     * Método auxiliar para seleccionar especialidad
     */
    private void seleccionarEspecialidad(String especialidad) {
        if (pacienteActual == null) {
            showAlert("Error", "Debe seleccionar un paciente primero");
            return;
        }
        
        if (nivelSeleccionado == null) {
            showAlert("Error", "Debe seleccionar un nivel de triage primero");
            return;
        }
        
        // Marcar especialidad seleccionada
        especialidadSeleccionada = especialidad;
        
        // Mostrar confirmación para guardar
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Evaluación de Triage");
        confirmAlert.setHeaderText("Resumen de Clasificación");
        confirmAlert.setContentText("Paciente: " + pacienteActual.getNombreCompleto() + "\n" +
                                  "Nivel de Urgencia: " + nivelSeleccionado.name() + "\n" +
                                  "Especialidad: " + especialidad + "\n\n" +
                                  "¿Confirma la evaluación de triage?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                completarEvaluacionTriage();
            }
        });
    }
    
    /**
     * Completa y guarda la evaluación de triage
     */
    private void completarEvaluacionTriage() {
        try {
            // Crear el registro de triage completo
            if (evaluacionActual == null) {
                evaluacionActual = new RegistroTriage();
            }
            
            evaluacionActual.setPacienteId(pacienteActual.getId());
            
            // Establecer ID de usuario de triage - con verificación robusta
            int usuarioId = 0;
            try {
                if (getUsuarioActual() != null && getUsuarioActual().getId() > 0) {
                    usuarioId = getUsuarioActual().getId();
                } else {
                    // Si no hay usuario actual válido, usar un ID por defecto
                    usuarioId = 1; // ID por defecto
                    System.err.println("Usando ID de usuario por defecto para triage");
                }
            } catch (Exception e) {
                usuarioId = 1; // ID por defecto en caso de error
                System.err.println("Error al obtener usuario actual, usando ID por defecto: " + e.getMessage());
            }
            evaluacionActual.setUsuarioTriageId(usuarioId);
            
            evaluacionActual.setNivelUrgencia(nivelSeleccionado);
            evaluacionActual.setEspecialidadAsignada(especialidadSeleccionada);
            evaluacionActual.setFechaHoraTriage(LocalDateTime.now());
            
            // Establecer prioridad numérica basada en el nivel de urgencia
            int prioridadNumerica = calcularPrioridadNumerica(nivelSeleccionado);
            evaluacionActual.setPrioridadNumerica(prioridadNumerica);
            
            // Establecer tiempo estimado de atención basado en el nivel de urgencia
            int tiempoEstimado = calcularTiempoEstimadoAtencion(nivelSeleccionado);
            evaluacionActual.setTiempoEstimadoAtencion(tiempoEstimado);
            
            // Obtener signos vitales desde la interfaz
            try {
                if (!txtPresionArterial.getText().trim().isEmpty()) {
                    evaluacionActual.setSignosVitalesPresion(txtPresionArterial.getText().trim());
                }
                
                if (!txtFrecuenciaCardiaca.getText().trim().isEmpty()) {
                    evaluacionActual.setSignosVitalesPulso(Integer.parseInt(txtFrecuenciaCardiaca.getText().trim()));
                }
                
                if (!txtTemperatura.getText().trim().isEmpty()) {
                    evaluacionActual.setSignosVitalesTemperatura(Double.parseDouble(txtTemperatura.getText().trim()));
                }
                
                if (!txtFrecuenciaRespiratoria.getText().trim().isEmpty()) {
                    evaluacionActual.setSignosVitalesRespiracion(Integer.parseInt(txtFrecuenciaRespiratoria.getText().trim()));
                }
                
                if (!txtSaturacionO2.getText().trim().isEmpty()) {
                    evaluacionActual.setSignosVitalesSaturacion(Integer.parseInt(txtSaturacionO2.getText().trim()));
                }
                
                if (!txtGlasgow.getText().trim().isEmpty()) {
                    evaluacionActual.setEscalaGlasgow(Integer.parseInt(txtGlasgow.getText().trim()));
                }
                
            } catch (NumberFormatException e) {
                showAlert("Error", "Por favor verifique que los valores numéricos sean correctos");
                return;
            }
            
            // Establecer observaciones y síntomas
            evaluacionActual.setObservacionesTriage(txtObservacionesClinicas.getText());
            
            // Establecer motivo de consulta - requerido por la validación
            String motivoConsulta = txtMotivoConsulta.getText().trim();
            if (motivoConsulta.isEmpty() || motivoConsulta.length() < 5) {
                motivoConsulta = "Evaluación de triage - nivel " + nivelSeleccionado.toString();
            }
            evaluacionActual.setMotivoConsulta(motivoConsulta);
            
            // Establecer síntomas principales - requerido por la validación
            String sintomas = txtSintomas.getText().trim();
            if (sintomas.isEmpty()) {
                // Si no hay síntomas, usar el motivo de consulta como síntomas principales
                sintomas = motivoConsulta;
                if (sintomas.length() < 5) {
                    sintomas = "Síntomas reportados durante evaluación de triage";
                }
            }
            evaluacionActual.setSintomasPrincipales(sintomas);
            
            // Establecer el estado del registro de triage
            evaluacionActual.setEstado(EstadoPaciente.ESPERANDO_TRABAJO_SOCIAL);
            
            // Debug: mostrar valores antes de guardar
            System.out.println("=== DEBUG TRIAGE ===");
            System.out.println("Paciente ID: " + evaluacionActual.getPacienteId());
            System.out.println("Usuario Triage ID: " + evaluacionActual.getUsuarioTriageId());
            System.out.println("Motivo consulta: '" + evaluacionActual.getMotivoConsulta() + "' (length: " + evaluacionActual.getMotivoConsulta().length() + ")");
            System.out.println("Síntomas principales: '" + evaluacionActual.getSintomasPrincipales() + "' (length: " + evaluacionActual.getSintomasPrincipales().length() + ")");
            System.out.println("Tiempo estimado: " + evaluacionActual.getTiempoEstimadoAtencion());
            System.out.println("Prioridad numérica: " + evaluacionActual.getPrioridadNumerica());
            System.out.println("Nivel urgencia: " + evaluacionActual.getNivelUrgencia());
            System.out.println("Especialidad: " + evaluacionActual.getEspecialidadAsignada());
            System.out.println("==================");
            
            // Guardar en la base de datos
            guardarEvaluacionTriage();
            
        } catch (Exception e) {
            showAlert("Error", "Error al completar evaluación de triage: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Otros manejadores de eventos
     */
    @FXML
    private void handleCancelar() {
        limpiarFormulario();
    }
    
    @FXML
    private void handleCompletarEvaluacion() {
        if (evaluacionActual == null) {
            showAlert("Error", "Debe evaluar al paciente primero");
            return;
        }
        
        if (evaluacionActual.getNivelUrgencia() == null) {
            showAlert("Error", "Debe seleccionar un nivel de triage");
            return;
        }
        
        guardarEvaluacionTriage();
    }
    
    @FXML
    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Cierre de Sesión");
        confirmAlert.setHeaderText("¿Está seguro de que desea cerrar la sesión?");
        confirmAlert.setContentText("Se perderán los datos no guardados.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                cerrarSesion();
                // Aquí deberías navegar de vuelta al login
                // Por ahora solo cerramos la ventana
                Platform.exit();
            }
        });
    }
    
    /**
     * Calcula la prioridad numérica basada en el nivel de urgencia
     */
    private int calcularPrioridadNumerica(NivelUrgencia nivelUrgencia) {
        if (nivelUrgencia == null) {
            return 5; // Por defecto menor prioridad
        }
        
        switch (nivelUrgencia) {
            case ROJO:      // Crítico/Resucitación
                return 1;   // Máxima prioridad
            case NARANJA:   // Urgente  
                return 2;   // Alta prioridad
            case AMARILLO:  // Menos urgente
                return 3;   // Prioridad media
            case VERDE:     // No urgente
                return 4;   // Prioridad baja
            case AZUL:      // Cita ambulatoria
                return 5;   // Menor prioridad
            default:
                return 5;   // Por defecto menor prioridad
        }
    }
    
    /**
     * Calcula el tiempo estimado de atención basado en el nivel de urgencia
     */
    private int calcularTiempoEstimadoAtencion(NivelUrgencia nivelUrgencia) {
        if (nivelUrgencia == null) {
            return 60; // Por defecto 60 minutos
        }
        
        switch (nivelUrgencia) {
            case ROJO:      // Crítico/Resucitación
                return 0;   // Inmediato
            case NARANJA:   // Urgente  
                return 10;  // 10 minutos
            case AMARILLO:  // Menos urgente
                return 60;  // 1 hora
            case VERDE:     // No urgente
                return 120; // 2 horas
            case AZUL:      // Cita ambulatoria
                return 240; // 4 horas o cita programada
            default:
                return 60;  // Por defecto 60 minutos
        }
    }

    
    // Clase de datos para la tabla de cola
    public static class PacienteColaInfo {
        private String nombre;
        private String expediente;
        private String urgencia;
        private String tiempoEspera;
        private int pacienteId;
        
        public PacienteColaInfo(String nombre, String expediente, String urgencia, String tiempoEspera, int pacienteId) {
            this.nombre = nombre;
            this.expediente = expediente;
            this.urgencia = urgencia;
            this.tiempoEspera = tiempoEspera;
            this.pacienteId = pacienteId;
        }
        
        // Getters
        public String getNombre() { return nombre; }
        public String getExpediente() { return expediente; }
        public String getUrgencia() { return urgencia; }
        public String getTiempoEspera() { return tiempoEspera; }
        public int getPacienteId() { return pacienteId; }
    }
}