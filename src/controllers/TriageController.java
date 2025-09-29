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
    @FXML private Label lblEspecialidadSeleccionada;
    
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
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar servicios
        triageService = new TriageService();
        pacienteService = new PacienteService();
        
        // Configurar tabla - TEMPORALMENTE COMENTADO
        // setupTable();
        
        // Configurar lista observable - TEMPORALMENTE COMENTADO  
        // colaPacientes = FXCollections.observableArrayList();
        // tblColaTriage.setItems(colaPacientes);
        
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
            
            // Crear o actualizar el registro de triage
            if (evaluacionActual == null) {
                evaluacionActual = new RegistroTriage();
                evaluacionActual.setPacienteId(pacienteActual.getId());
                evaluacionActual.setMedicoTriageId(getUsuarioActual().getId());
            }
            
            // Establecer nivel de urgencia
            evaluacionActual.setNivelUrgencia(nivel);
            
            // Obtener signos vitales desde la interfaz
            try {
                // Presión arterial (puede estar en formato "120/80" o campos separados)
                if (!txtPresionArterial.getText().trim().isEmpty()) {
                    String presionText = txtPresionArterial.getText().trim();
                    if (presionText.contains("/")) {
                        String[] presion = presionText.split("/");
                        if (presion.length == 2) {
                            evaluacionActual.setPresionSistolica(Integer.parseInt(presion[0].trim()));
                            evaluacionActual.setPresionDiastolica(Integer.parseInt(presion[1].trim()));
                        }
                    } else {
                        // Solo sistólica
                        evaluacionActual.setPresionSistolica(Integer.parseInt(presionText));
                    }
                }
                
                if (!txtFrecuenciaCardiaca.getText().trim().isEmpty()) {
                    evaluacionActual.setFrecuenciaCardiaca(Integer.parseInt(txtFrecuenciaCardiaca.getText().trim()));
                }
                
                if (!txtTemperatura.getText().trim().isEmpty()) {
                    evaluacionActual.setTemperatura(Double.parseDouble(txtTemperatura.getText().trim()));
                }
                
                if (!txtFrecuenciaRespiratoria.getText().trim().isEmpty()) {
                    evaluacionActual.setFrecuenciaRespiratoria(Integer.parseInt(txtFrecuenciaRespiratoria.getText().trim()));
                }
                
                if (!txtSaturacionO2.getText().trim().isEmpty()) {
                    evaluacionActual.setSaturacionOxigeno(Integer.parseInt(txtSaturacionO2.getText().trim()));
                }
                
                if (!txtGlasgow.getText().trim().isEmpty()) {
                    evaluacionActual.setGlasgow(Integer.parseInt(txtGlasgow.getText().trim()));
                }
                
            } catch (NumberFormatException e) {
                showAlert("Error", "Por favor verifique que los valores numéricos sean correctos");
                return;
            }
            
            // Establecer observaciones
            evaluacionActual.setObservacionesTriage(txtObservacionesClinicas.getText());
            evaluacionActual.setMotivoConsulta(txtMotivoConsulta.getText());
            
            // Mostrar confirmación
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmar Clasificación");
            confirmAlert.setHeaderText("Clasificación de Triage: " + nivel.name());
            confirmAlert.setContentText("Paciente: " + pacienteActual.getNombreCompleto() + "\n" +
                                      "Nivel: " + descripcion + "\n\n" +
                                      "¿Confirma la clasificación?");
            
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    guardarEvaluacionTriage();
                }
            });
            
        } catch (Exception e) {
            showAlert("Error", "Error al clasificar paciente: " + e.getMessage());
            e.printStackTrace();
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
        if (evaluacionActual != null) {
            evaluacionActual.setEspecialidadAsignada(especialidad);
            lblEspecialidadSeleccionada.setText("Especialidad seleccionada: " + especialidad);
        } else {
            showAlert("Error", "Debe evaluar al paciente primero");
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