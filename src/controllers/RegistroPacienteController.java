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
import utils.ValidationUtils;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para el registro de pacientes
 * Maneja el alta de nuevos pacientes y búsqueda/edición de existentes
 */
public class RegistroPacienteController extends BaseController implements Initializable {
    
    // Información del usuario
    @FXML private Label lblUsuarioActual;
    @FXML private Label lblFechaHora;
    
    // Formulario de registro
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidoPaterno;
    @FXML private TextField txtApellidoMaterno;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private ComboBox<String> cbSexo;
    @FXML private TextField txtCurp;
    @FXML private TextField txtRfc;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    
    // Dirección
    @FXML private TextField txtCalle;
    @FXML private TextField txtNumero;
    @FXML private TextField txtColonia;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtEstado;
    @FXML private TextField txtCodigoPostal;
    
    // Información médica
    @FXML private TextArea txtAlergias;
    @FXML private TextArea txtMedicamentos;
    @FXML private TextArea txtEnfermedadesPrevias;
    @FXML private TextArea txtObservacionesMedicas;
    
    // Contacto de emergencia
    @FXML private TextField txtNombreEmergencia;
    @FXML private TextField txtTelefonoEmergencia;
    @FXML private TextField txtParentesco;
    
    // Botones
    @FXML private Button btnGuardar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnBuscar;
    @FXML private Button btnNuevo;
    
    // Búsqueda
    @FXML private TextField txtBusqueda;
    @FXML private TableView<PacienteInfo> tblPacientes;
    @FXML private TableColumn<PacienteInfo, String> colExpediente;
    @FXML private TableColumn<PacienteInfo, String> colNombre;
    @FXML private TableColumn<PacienteInfo, String> colFechaNacimiento;
    @FXML private TableColumn<PacienteInfo, String> colTelefono;
    @FXML private TableColumn<PacienteInfo, String> colEstado;
    
    // Mensajes
    @FXML private Label lblMensaje;
    
    // Servicios
    private PacienteService pacienteService;
    
    // Estado actual
    private Paciente pacienteActual;
    private boolean modoEdicion = false;
    private ObservableList<PacienteInfo> listaPacientes;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar servicios
        pacienteService = new PacienteService();
        
        // Configurar ComboBox de sexo
        cbSexo.getItems().addAll("MASCULINO", "FEMENINO", "OTRO");
        
        // Configurar tabla
        setupTable();
        
        // Configurar lista observable
        listaPacientes = FXCollections.observableArrayList();
        tblPacientes.setItems(listaPacientes);
        
        // Configurar validaciones
        setupValidations();
        
        // Estado inicial
        limpiarFormulario();
    }
    
    @Override
    protected void onSesionInicializada() {
        // Verificar permisos
        if (!tienePermiso(AuthenticationService.Permiso.REGISTRAR_PACIENTES)) {
            showAlert("Sin permisos", "No tiene permisos para registrar pacientes");
            return;
        }
        
        // Configurar información del usuario
        lblUsuarioActual.setText(usuarioActual.getNombreCompleto());
        
        // Cargar lista de pacientes recientes
        cargarPacientesRecientes();
    }
    
    /**
     * Configura la tabla de pacientes
     */
    private void setupTable() {
        colExpediente.setCellValueFactory(new PropertyValueFactory<>("expediente"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colFechaNacimiento.setCellValueFactory(new PropertyValueFactory<>("fechaNacimiento"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        
        // Listener para selección
        tblPacientes.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                cargarPacienteEnFormulario(newValue.getPacienteId());
            }
        });
    }
    
    /**
     * Configura las validaciones de campos
     */
    private void setupValidations() {
        // Validación de CURP en tiempo real
        txtCurp.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (!ValidationUtils.isValidCURP(newValue)) {
                    txtCurp.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 1px;");
                } else {
                    txtCurp.setStyle("");
                }
            }
        });
        
        // Validación de RFC en tiempo real
        txtRfc.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (!ValidationUtils.isValidRFC(newValue)) {
                    txtRfc.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 1px;");
                } else {
                    txtRfc.setStyle("");
                }
            }
        });
        
        // Validación de email
        txtEmail.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (!ValidationUtils.isValidEmail(newValue)) {
                    txtEmail.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 1px;");
                } else {
                    txtEmail.setStyle("");
                }
            }
        });
        
        // Solo números en teléfono
        txtTelefono.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*")) {
                txtTelefono.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        txtTelefonoEmergencia.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*")) {
                txtTelefonoEmergencia.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }
    
    /**
     * Carga los pacientes registrados recientemente
     */
    private void cargarPacientesRecientes() {
        try {
            ResultadoBusqueda resultado = pacienteService.obtenerPacientesRecientes(tokenSesion, 50);
            
            listaPacientes.clear();
            
            for (Paciente paciente : resultado.getPacientes()) {
                PacienteInfo info = new PacienteInfo(
                    paciente.getId(),
                    paciente.getNumeroExpediente(),
                    paciente.getNombreCompleto(),
                    paciente.getFechaNacimiento().toString(),
                    paciente.getTelefono(),
                    paciente.isActivo() ? "ACTIVO" : "INACTIVO"
                );
                listaPacientes.add(info);
            }
            
        } catch (Exception e) {
            showAlert("Error", "Error al cargar pacientes: " + e.getMessage());
        }
    }
    
    /**
     * Busca pacientes según criterio
     */
    @FXML
    private void handleBuscar() {
        String criterio = txtBusqueda.getText().trim();
        
        if (criterio.isEmpty()) {
            cargarPacientesRecientes();
            return;
        }
        
        try {
            ResultadoBusqueda resultado = pacienteService.buscarPacientes(tokenSesion, criterio);
            
            listaPacientes.clear();
            
            for (Paciente paciente : resultado.getPacientes()) {
                PacienteInfo info = new PacienteInfo(
                    paciente.getId(),
                    paciente.getNumeroExpediente(),
                    paciente.getNombreCompleto(),
                    paciente.getFechaNacimiento().toString(),
                    paciente.getTelefono(),
                    paciente.isActivo() ? "ACTIVO" : "INACTIVO"
                );
                listaPacientes.add(info);
            }
            
            if (listaPacientes.isEmpty()) {
                showMessage("No se encontraron pacientes con ese criterio", "info");
            }
            
        } catch (Exception e) {
            showAlert("Error", "Error en la búsqueda: " + e.getMessage());
        }
    }
    
    /**
     * Carga un paciente en el formulario para edición
     */
    private void cargarPacienteEnFormulario(int pacienteId) {
        try {
            pacienteActual = pacienteService.buscarPorId(tokenSesion, pacienteId);
            
            if (pacienteActual != null) {
                // Cargar datos básicos
                txtNombre.setText(pacienteActual.getNombre());
                txtApellidoPaterno.setText(pacienteActual.getApellidoPaterno());
                txtApellidoMaterno.setText(pacienteActual.getApellidoMaterno());
                dpFechaNacimiento.setValue(pacienteActual.getFechaNacimiento());
                cbSexo.setValue(pacienteActual.getSexo());
                txtCurp.setText(pacienteActual.getCurp());
                txtRfc.setText(pacienteActual.getRfc());
                txtTelefono.setText(pacienteActual.getTelefono());
                txtEmail.setText(pacienteActual.getEmail());
                
                // Cargar dirección
                txtCalle.setText(pacienteActual.getDireccionCalle());
                txtNumero.setText(pacienteActual.getDireccionNumero());
                txtColonia.setText(pacienteActual.getDireccionColonia());
                txtCiudad.setText(pacienteActual.getDireccionCiudad());
                txtEstado.setText(pacienteActual.getDireccionEstado());
                txtCodigoPostal.setText(pacienteActual.getCodigoPostal());
                
                // Cargar información médica
                txtAlergias.setText(pacienteActual.getAlergias());
                txtMedicamentos.setText(pacienteActual.getMedicamentos());
                txtEnfermedadesPrevias.setText(pacienteActual.getEnfermedadesPrevias());
                txtObservacionesMedicas.setText(pacienteActual.getObservacionesMedicas());
                
                // Cargar contacto de emergencia
                txtNombreEmergencia.setText(pacienteActual.getContactoEmergenciaNombre());
                txtTelefonoEmergencia.setText(pacienteActual.getContactoEmergenciaTelefono());
                txtParentesco.setText(pacienteActual.getContactoEmergenciaParentesco());
                
                modoEdicion = true;
                btnGuardar.setText("Actualizar Paciente");
                showMessage("Paciente cargado para edición", "success");
            }
            
        } catch (Exception e) {
            showAlert("Error", "Error al cargar paciente: " + e.getMessage());
        }
    }
    
    /**
     * Guarda o actualiza un paciente
     */
    @FXML
    private void handleGuardar() {
        if (!validarFormulario()) {
            return;
        }
        
        try {
            // Crear datos de registro
            DatosRegistroPaciente datos = new DatosRegistroPaciente();
            datos.setNombre(txtNombre.getText().trim());
            datos.setApellidoPaterno(txtApellidoPaterno.getText().trim());
            datos.setApellidoMaterno(txtApellidoMaterno.getText().trim());
            datos.setFechaNacimiento(dpFechaNacimiento.getValue());
            datos.setSexo(cbSexo.getValue());
            datos.setCurp(txtCurp.getText().trim().toUpperCase());
            datos.setRfc(txtRfc.getText().trim().toUpperCase());
            datos.setTelefono(txtTelefono.getText().trim());
            datos.setEmail(txtEmail.getText().trim());
            
            // Dirección
            datos.setDireccionCalle(txtCalle.getText().trim());
            datos.setDireccionNumero(txtNumero.getText().trim());
            datos.setDireccionColonia(txtColonia.getText().trim());
            datos.setDireccionCiudad(txtCiudad.getText().trim());
            datos.setDireccionEstado(txtEstado.getText().trim());
            datos.setCodigoPostal(txtCodigoPostal.getText().trim());
            
            // Información médica
            datos.setAlergias(txtAlergias.getText().trim());
            datos.setMedicamentos(txtMedicamentos.getText().trim());
            datos.setEnfermedadesPrevias(txtEnfermedadesPrevias.getText().trim());
            datos.setObservacionesMedicas(txtObservacionesMedicas.getText().trim());
            
            // Contacto de emergencia
            datos.setContactoEmergenciaNombre(txtNombreEmergencia.getText().trim());
            datos.setContactoEmergenciaTelefono(txtTelefonoEmergencia.getText().trim());
            datos.setContactoEmergenciaParentesco(txtParentesco.getText().trim());
            
            if (modoEdicion && pacienteActual != null) {
                // Actualizar paciente existente
                boolean actualizado = pacienteService.actualizarPaciente(tokenSesion, pacienteActual.getId(), datos);
                
                if (actualizado) {
                    showMessage("Paciente actualizado correctamente", "success");
                    cargarPacientesRecientes();
                    limpiarFormulario();
                } else {
                    showMessage("Error al actualizar el paciente", "error");
                }
                
            } else {
                // Registrar nuevo paciente
                ResultadoRegistro resultado = pacienteService.registrarPaciente(tokenSesion, datos);
                
                if (resultado.isExitoso()) {
                    showMessage("Paciente registrado exitosamente. ID: " + resultado.getPacienteId(), "success");
                    cargarPacientesRecientes();
                    limpiarFormulario();
                } else {
                    showMessage("Error al registrar: " + resultado.getMensaje(), "error");
                }
            }
            
        } catch (Exception e) {
            showAlert("Error", "Error al guardar paciente: " + e.getMessage());
        }
    }
    
    /**
     * Prepara el formulario para un nuevo paciente
     */
    @FXML
    private void handleNuevo() {
        limpiarFormulario();
    }
    
    /**
     * Limpia el formulario
     */
    @FXML
    private void handleLimpiar() {
        limpiarFormulario();
    }
    
    /**
     * Limpia todos los campos del formulario
     */
    private void limpiarFormulario() {
        // Limpiar campos básicos
        txtNombre.clear();
        txtApellidoPaterno.clear();
        txtApellidoMaterno.clear();
        dpFechaNacimiento.setValue(null);
        cbSexo.setValue(null);
        txtCurp.clear();
        txtRfc.clear();
        txtTelefono.clear();
        txtEmail.clear();
        
        // Limpiar dirección
        txtCalle.clear();
        txtNumero.clear();
        txtColonia.clear();
        txtCiudad.clear();
        txtEstado.clear();
        txtCodigoPostal.clear();
        
        // Limpiar información médica
        txtAlergias.clear();
        txtMedicamentos.clear();
        txtEnfermedadesPrevias.clear();
        txtObservacionesMedicas.clear();
        
        // Limpiar contacto de emergencia
        txtNombreEmergencia.clear();
        txtTelefonoEmergencia.clear();
        txtParentesco.clear();
        
        // Limpiar estilos de validación
        limpiarEstilosValidacion();
        
        // Reset estado
        pacienteActual = null;
        modoEdicion = false;
        btnGuardar.setText("Guardar Paciente");
        
        // Limpiar mensaje
        lblMensaje.setText("");
        
        // Limpiar selección de tabla
        tblPacientes.getSelectionModel().clearSelection();
        
        // Focus al primer campo
        Platform.runLater(() -> txtNombre.requestFocus());
    }
    
    /**
     * Valida el formulario antes de guardar
     */
    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();
        
        // Validar campos obligatorios
        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("- El nombre es obligatorio\n");
        }
        
        if (txtApellidoPaterno.getText().trim().isEmpty()) {
            errores.append("- El apellido paterno es obligatorio\n");
        }
        
        if (dpFechaNacimiento.getValue() == null) {
            errores.append("- La fecha de nacimiento es obligatoria\n");
        } else if (dpFechaNacimiento.getValue().isAfter(LocalDate.now())) {
            errores.append("- La fecha de nacimiento no puede ser futura\n");
        }
        
        if (cbSexo.getValue() == null) {
            errores.append("- El sexo es obligatorio\n");
        }
        
        if (txtTelefono.getText().trim().isEmpty()) {
            errores.append("- El teléfono es obligatorio\n");
        }
        
        // Validar CURP si se proporciona
        if (!txtCurp.getText().trim().isEmpty() && !ValidationUtils.isValidCURP(txtCurp.getText().trim())) {
            errores.append("- El CURP no tiene el formato correcto\n");
        }
        
        // Validar RFC si se proporciona
        if (!txtRfc.getText().trim().isEmpty() && !ValidationUtils.isValidRFC(txtRfc.getText().trim())) {
            errores.append("- El RFC no tiene el formato correcto\n");
        }
        
        // Validar email si se proporciona
        if (!txtEmail.getText().trim().isEmpty() && !ValidationUtils.isValidEmail(txtEmail.getText().trim())) {
            errores.append("- El email no tiene el formato correcto\n");
        }
        
        if (errores.length() > 0) {
            showAlert("Errores de validación", errores.toString());
            return false;
        }
        
        return true;
    }
    
    /**
     * Limpia los estilos de validación
     */
    private void limpiarEstilosValidacion() {
        txtCurp.setStyle("");
        txtRfc.setStyle("");
        txtEmail.setStyle("");
    }
    
    /**
     * Muestra un mensaje en la etiqueta de mensajes
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
        
        // Limpiar mensaje después de 5 segundos
        Platform.runLater(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> lblMensaje.setText(""));
            } catch (InterruptedException e) {
                // Ignorar
            }
        });
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
    
    // Clase de datos para la tabla
    public static class PacienteInfo {
        private int pacienteId;
        private String expediente;
        private String nombreCompleto;
        private String fechaNacimiento;
        private String telefono;
        private String estado;
        
        public PacienteInfo(int pacienteId, String expediente, String nombreCompleto, 
                          String fechaNacimiento, String telefono, String estado) {
            this.pacienteId = pacienteId;
            this.expediente = expediente;
            this.nombreCompleto = nombreCompleto;
            this.fechaNacimiento = fechaNacimiento;
            this.telefono = telefono;
            this.estado = estado;
        }
        
        // Getters
        public int getPacienteId() { return pacienteId; }
        public String getExpediente() { return expediente; }
        public String getNombreCompleto() { return nombreCompleto; }
        public String getFechaNacimiento() { return fechaNacimiento; }
        public String getTelefono() { return telefono; }
        public String getEstado() { return estado; }
    }
}