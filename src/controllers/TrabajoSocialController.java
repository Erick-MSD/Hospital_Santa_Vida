package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.DatosSociales;
import models.Paciente;
import models.RegistroTriage;
import models.Usuario;
import dao.DatosSocialesDAO;
import services.TriageService;
import services.AuthenticationService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para la interfaz de Trabajo Social
 * Maneja la evaluación social integral de los pacientes usando el modelo DatosSociales
 */
public class TrabajoSocialController extends BaseController implements Initializable {
    
    // Información del usuario
    @FXML private Label lblUsuarioNombre;
    
    // Información del paciente
    @FXML private Label lblNombrePaciente;
    @FXML private Label lblClasificacion;
    @FXML private Label lblMotivo;
    
    // Campos del formulario
    @FXML private TextArea txtAntecedentesFamiliares;
    @FXML private TextArea txtSituacionSocioeconomica;
    @FXML private TextArea txtMedicamentosActuales;
    @FXML private TextArea txtAlergiasConocidas;
    @FXML private TextArea txtEnfermedadesCronicas;
    @FXML private TextArea txtCirugiasPrevias;
    @FXML private TextArea txtHospitalizacionesPrevias;
    @FXML private TextArea txtHabitosToxicos;
    @FXML private TextArea txtVacunasRecientes;
    @FXML private TextArea txtObservacionesAdicionales;
    
    // Botones
    @FXML private Button btnCancelar;
    @FXML private Button btnGuardarEvaluacion;
    @FXML private Button btnVerHistorial;
    @FXML private Button btnNotasRapidas;
    @FXML private Button btnCerrarSesion;
    
    // Variables del controlador
    private RegistroTriage registroTriageActual;
    private DatosSocialesDAO datosSocialesDAO;
    private TriageService triageService;
    private AuthenticationService authService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        inicializarServicios();
        configurarInterfaz();
        cargarPacientePendiente();
    }
    
    /**
     * Método requerido por BaseController
     */
    @Override
    protected void cargarDatos() {
        cargarPacientePendiente();
    }
    
    /**
     * Inicializa los servicios necesarios
     */
    private void inicializarServicios() {
        datosSocialesDAO = new DatosSocialesDAO();
        authService = new AuthenticationService();
        triageService = new TriageService(authService);
    }
    
    /**
     * Configura la interfaz inicial
     */
    @Override
    protected void configurarInterfaz() {
        // Configurar usuario actual (obtener del contexto de sesión)
        lblUsuarioNombre.setText("Trabajadora Social Ana López");
        
        // Configurar texto de ejemplo si no hay paciente
        if (registroTriageActual == null) {
            lblNombrePaciente.setText("Cargando...");
            lblClasificacion.setText("...");
            lblMotivo.setText("...");
        }
    }
    
    /**
     * Carga el siguiente paciente pendiente de evaluación social
     */
    private void cargarPacientePendiente() {
        try {
            // Obtener el siguiente paciente que necesita evaluación social
            registroTriageActual = triageService.obtenerSiguientePacienteParaTrabajoSocial();
            
            if (registroTriageActual != null) {
                actualizarInformacionPaciente();
            } else {
                mostrarSinPacientes();
            }
            
        } catch (Exception e) {
            mostrarError("Error al cargar paciente", "No se pudo cargar la información del paciente: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Actualiza la información del paciente en la interfaz
     */
    private void actualizarInformacionPaciente() {
        if (registroTriageActual != null && registroTriageActual.getPaciente() != null) {
            Paciente paciente = registroTriageActual.getPaciente();
            
            lblNombrePaciente.setText(paciente.getNombre() + " " + paciente.getApellidoPaterno());
            lblClasificacion.setText("Verde"); // Temporal hasta que se arregle el getter
            lblMotivo.setText(registroTriageActual.getMotivoConsulta());
            
            // Cargar datos sociales existentes si los hay
            cargarDatosSocialesExistentes();
        }
    }
    
    /**
     * Carga datos sociales existentes del paciente si los hay
     */
    private void cargarDatosSocialesExistentes() {
        try {
            DatosSociales datosExistentes = datosSocialesDAO.obtenerPorRegistroTriage(registroTriageActual.getId());
            
            if (datosExistentes != null) {
                // Cargar datos existentes en los campos
                txtAntecedentesFamiliares.setText(datosExistentes.getAntecedentesFamiliares());
                txtSituacionSocioeconomica.setText(datosExistentes.getSituacionSocioeconomica());
                txtMedicamentosActuales.setText(datosExistentes.getMedicamentosActuales());
                txtAlergiasConocidas.setText(datosExistentes.getAlergiasConocidas());
                txtEnfermedadesCronicas.setText(datosExistentes.getEnfermedadesCronicas());
                txtCirugiasPrevias.setText(datosExistentes.getCirugiasPrevias());
                txtHospitalizacionesPrevias.setText(datosExistentes.getHospitalizacionesPrevias());
                txtHabitosToxicos.setText(datosExistentes.getHabitosToxicos());
                txtVacunasRecientes.setText(datosExistentes.getVacunasRecientes());
                txtObservacionesAdicionales.setText(datosExistentes.getObservacionesAdicionales());
            }
            
        } catch (Exception e) {
            System.out.println("No se encontraron datos sociales previos para este paciente");
        }
    }
    
    /**
     * Muestra mensaje cuando no hay pacientes pendientes
     */
    private void mostrarSinPacientes() {
        lblNombrePaciente.setText("Sin pacientes pendientes");
        lblClasificacion.setText("N/A");
        lblMotivo.setText("N/A");
        
        // Deshabilitar botón de guardar
        btnGuardarEvaluacion.setDisable(true);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sin pacientes");
        alert.setHeaderText("No hay pacientes pendientes");
        alert.setContentText("Actualmente no hay pacientes esperando evaluación social.");
        alert.showAndWait();
    }
    
    /**
     * Maneja la acción de cancelar
     */
    @FXML
    private void handleCancelar(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar cancelación");
        alert.setHeaderText("¿Está seguro de que desea cancelar?");
        alert.setContentText("Se perderán todos los datos ingresados.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                limpiarFormulario();
                cargarPacientePendiente(); // Recargar el paciente
            }
        });
    }
    
    /**
     * Maneja la acción de guardar la evaluación
     */
    @FXML
    private void handleGuardarEvaluacion(ActionEvent event) {
        if (registroTriageActual == null) {
            mostrarError("Error", "No hay paciente seleccionado");
            return;
        }
        
        if (validarFormulario()) {
            DatosSociales datosSociales = crearDatosSociales();
            
            if (guardarDatosSociales(datosSociales)) {
                mostrarExitoGuardado();
                avanzarSiguientePaso();
            } else {
                mostrarErrorGuardado();
            }
        }
    }
    
    /**
     * Valida que los campos requeridos estén completos
     */
    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();
        
        // Validar campos obligatorios
        if (txtObservacionesAdicionales.getText().trim().isEmpty()) {
            errores.append("- Las observaciones del trabajador social son obligatorias\n");
        }
        
        if (txtAntecedentesFamiliares.getText().trim().isEmpty()) {
            errores.append("- Los antecedentes familiares son requeridos\n");
        }
        
        if (errores.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Campos incompletos");
            alert.setHeaderText("Por favor complete los siguientes campos:");
            alert.setContentText(errores.toString());
            alert.showAndWait();
            return false;
        }
        
        return true;
    }
    
    /**
     * Crea el objeto DatosSociales con los datos del formulario
     */
    private DatosSociales crearDatosSociales() {
        DatosSociales datos = new DatosSociales(
            registroTriageActual.getId(),
            1 // ID temporal del trabajador social
        );
        
        // Asignar todos los campos
        datos.setAntecedentesFamiliares(txtAntecedentesFamiliares.getText());
        datos.setEnfermedadesCronicas(txtEnfermedadesCronicas.getText());
        datos.setMedicamentosActuales(txtMedicamentosActuales.getText());
        datos.setAlergiasConocidas(txtAlergiasConocidas.getText());
        datos.setCirugiasPrevias(txtCirugiasPrevias.getText());
        datos.setHospitalizacionesPrevias(txtHospitalizacionesPrevias.getText());
        datos.setVacunasRecientes(txtVacunasRecientes.getText());
        datos.setHabitosToxicos(txtHabitosToxicos.getText());
        datos.setSituacionSocioeconomica(txtSituacionSocioeconomica.getText());
        datos.setObservacionesAdicionales(txtObservacionesAdicionales.getText());
        
        return datos;
    }
    
    /**
     * Guarda los datos sociales en la base de datos
     */
    private boolean guardarDatosSociales(DatosSociales datos) {
        try {
            boolean resultado = datosSocialesDAO.insertar(datos);
            
            if (resultado) {
                // Actualizar el estado del registro de triage
                triageService.marcarEvaluacionSocialCompleta(registroTriageActual.getId());
                System.out.println("Datos sociales guardados exitosamente para registro: " + registroTriageActual.getId());
            }
            
            return resultado;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Muestra el mensaje de éxito al guardar
     */
    private void mostrarExitoGuardado() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Evaluación guardada");
        alert.setHeaderText("¡Éxito!");
        alert.setContentText("La evaluación social se ha guardado correctamente. El paciente ha sido derivado a atención médica.");
        alert.showAndWait();
    }
    
    /**
     * Avanza al siguiente paso del flujo
     */
    private void avanzarSiguientePaso() {
        limpiarFormulario();
        cargarPacientePendiente(); // Cargar el siguiente paciente
    }
    
    /**
     * Muestra el mensaje de error al guardar
     */
    private void mostrarErrorGuardado() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error al guardar");
        alert.setHeaderText("No se pudo guardar la evaluación");
        alert.setContentText("Ocurrió un error al intentar guardar la evaluación. Por favor intente nuevamente.");
        alert.showAndWait();
    }
    
    /**
     * Limpia todos los campos del formulario
     */
    @Override
    protected void limpiarFormulario() {
        txtAntecedentesFamiliares.clear();
        txtSituacionSocioeconomica.clear();
        txtMedicamentosActuales.clear();
        txtAlergiasConocidas.clear();
        txtEnfermedadesCronicas.clear();
        txtCirugiasPrevias.clear();
        txtHospitalizacionesPrevias.clear();
        txtHabitosToxicos.clear();
        txtVacunasRecientes.clear();
        txtObservacionesAdicionales.clear();
    }
    
    /**
     * Maneja la acción de ver historial
     */
    @FXML
    private void handleVerHistorial(ActionEvent event) {
        if (registroTriageActual != null) {
            mostrarInfo("Historial", "Funcionalidad de historial médico en desarrollo");
        }
    }
    
    /**
     * Maneja la acción de notas rápidas
     */
    @FXML
    private void handleNotasRapidas(ActionEvent event) {
        mostrarInfo("Notas", "Funcionalidad de notas rápidas en desarrollo");
    }
    
    /**
     * Maneja la acción de cerrar sesión
     */
    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        try {
            // Cargar la pantalla de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
            Scene loginScene = new Scene(loader.load());
            
            Stage currentStage = (Stage) btnCerrarSesion.getScene().getWindow();
            currentStage.setScene(loginScene);
            currentStage.setTitle("Iniciar Sesión - Hospital Santa Vida");
            currentStage.centerOnScreen();
            
        } catch (IOException e) {
            System.err.println("Error al cerrar sesión: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Establece el registro de triage actual
     */
    public void setRegistroTriage(RegistroTriage registro) {
        this.registroTriageActual = registro;
        actualizarInformacionPaciente();
    }
    
    /**
     * Muestra mensaje de error
     */
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText("Error");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    /**
     * Muestra mensaje informativo
     */
    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText("Información");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}