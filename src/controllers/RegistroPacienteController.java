package controllers;

import dao.PacienteDAO;
import models.Paciente;
import models.Usuario;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import java.net.URL;
import java.util.ResourceBundle;
import java.time.LocalDate;

/**
 * Controlador para el registro de pacientes (interfaz FXML)
 * Solo accesible para Asistentes Médicas
 */
public class RegistroPacienteController extends BaseController implements Initializable {
    
    // Componentes FXML - campos del formulario
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidoPaterno;
    @FXML private TextField txtApellidoMaterno;
    @FXML private TextField txtCurp;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField txtEdad;
    @FXML private ComboBox<String> cmbSexo;
    @FXML private TextField txtTelefonoPrincipal;
    @FXML private TextField txtTelefonoSecundario;
    @FXML private TextField txtEmail;
    @FXML private TextField txtCalle;
    @FXML private TextField txtNumero;
    @FXML private TextField txtColonia;
    @FXML private ComboBox<String> cmbDireccionCiudad;
    @FXML private ComboBox<String> cmbDireccionEstado;
    @FXML private TextField txtCodigoPostal;
    @FXML private TextField txtContactoEmergencia;
    @FXML private TextField txtTelefonoEmergencia;
    @FXML private ComboBox<String> cmbContactoEmergenciaRelacion;
    @FXML private ComboBox<String> cmbSeguroMedico;
    @FXML private TextField txtNumeroPoliza;
    
    // Componentes de la interfaz
    @FXML private Button btnGuardar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCancelar;
    @FXML private Label lblMensaje;
    @FXML private Label lblUsuarioActual;
    @FXML private Label lblEstado;
    @FXML private AnchorPane mainPane;
    
    // Servicios
    private PacienteDAO pacienteDAO;
    
    public RegistroPacienteController() {
        this.pacienteDAO = new PacienteDAO();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🏥 Inicializando interfaz de Registro de Pacientes...");
        
        // Inicializar componentes
        configurarComponentes();
        
        // Llamar métodos del BaseController
        super.initialize();
    }
    
    @Override
    protected void configurarInterfaz() {
        try {
            // Verificar permisos
            if (authService != null) {
                authService.requireRole(Usuario.TipoUsuario.ASISTENTE_MEDICA);
            }
            
            // Configurar información del usuario
            if (usuarioActual != null && lblUsuarioActual != null) {
                lblUsuarioActual.setText("👩‍⚕️ " + usuarioActual.getNombreCompleto());
            }
            
            // Configurar estado
            if (lblEstado != null) {
                lblEstado.setText("📋 PENDIENTE DE EVALUACIÓN");
                lblEstado.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
            }
            
            System.out.println("✅ Interfaz configurada correctamente para: " + 
                             (usuarioActual != null ? usuarioActual.getNombreCompleto() : "Usuario desconocido"));
            
        } catch (Exception e) {
            System.err.println("❌ Error de permisos: " + e.getMessage());
            mostrarMensaje("Error: Sin permisos para acceder a esta función", "#D32F2F");
        }
    }
    
    @Override
    protected void cargarDatos() {
        // Método implementado para cumplir con BaseController
        System.out.println("🔄 Datos iniciales cargados para registro de pacientes");
    }
    
    /**
     * Configura los componentes de la interfaz
     */
    private void configurarComponentes() {
        // Configurar ComboBox de sexo
        if (cmbSexo != null) {
            cmbSexo.getItems().addAll("MASCULINO", "FEMENINO", "OTRO");
        }
        
        // Configurar ComboBox de relación de contacto de emergencia
        if (cmbContactoEmergenciaRelacion != null) {
            cmbContactoEmergenciaRelacion.getItems().addAll(
                "Esposo/a", "Hijo/a", "Padre/Madre", "Hermano/a", 
                "Abuelo/a", "Tío/a", "Primo/a", "Amigo/a", 
                "Conocido", "Pareja", "Otro"
            );
        }
        
        // Configurar listener para calcular edad automáticamente
        if (dpFechaNacimiento != null && txtEdad != null) {
            dpFechaNacimiento.valueProperty().addListener((obs, oldDate, newDate) -> {
                if (newDate != null) {
                    int edad = java.time.Period.between(newDate, java.time.LocalDate.now()).getYears();
                    txtEdad.setText(String.valueOf(edad));
                }
            });
        }
        
        // Configurar ComboBox de estados mexicanos
        if (cmbDireccionEstado != null) {
            cmbDireccionEstado.getItems().addAll(
                "Aguascalientes", "Baja California", "Baja California Sur", "Campeche", 
                "Chiapas", "Chihuahua", "Ciudad de México", "Coahuila", "Colima", 
                "Durango", "Estado de México", "Guanajuato", "Guerrero", "Hidalgo", 
                "Jalisco", "Michoacán", "Morelos", "Nayarit", "Nuevo León", "Oaxaca", 
                "Puebla", "Querétaro", "Quintana Roo", "San Luis Potosí", "Sinaloa", 
                "Sonora", "Tabasco", "Tamaulipas", "Tlaxcala", "Veracruz", "Yucatán", "Zacatecas"
            );
            
            // Listener para actualizar ciudades cuando cambie el estado
            cmbDireccionEstado.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && cmbDireccionCiudad != null) {
                    actualizarCiudades(newVal);
                }
            });
        }
        
        // Configurar ComboBox de aseguradoras mexicanas
        if (cmbSeguroMedico != null) {
            cmbSeguroMedico.getItems().addAll(
                "SIN SEGURO", "GNP Seguros", "AXA Seguros", "MetLife México", 
                "Monterrey New York Life", "Seguros Inbursa", "Allianz México", 
                "MAPFRE México", "Zurich México", "Bupa México", "Plan Seguro", 
                "Atlas Seguros", "HDI Seguros", "Qualitas Compañía de Seguros", 
                "IMSS", "ISSSTE", "Seguro Popular", "PEMEX", "SEDENA", "SEMAR"
            );
            cmbSeguroMedico.setValue("SIN SEGURO");
        }
    }
    
    /**
     * Actualiza las ciudades disponibles según el estado seleccionado
     */
    private void actualizarCiudades(String estado) {
        if (cmbDireccionCiudad == null) return;
        
        cmbDireccionCiudad.getItems().clear();
        
        // Agregar ciudades principales según el estado
        switch (estado) {
            case "Nuevo León":
                cmbDireccionCiudad.getItems().addAll("Monterrey", "Guadalupe", "San Nicolás de los Garza", 
                    "Apodaca", "General Escobedo", "Santa Catarina", "San Pedro Garza García");
                break;
            case "Ciudad de México":
                cmbDireccionCiudad.getItems().addAll("Álvaro Obregón", "Azcapotzalco", "Benito Juárez", 
                    "Coyoacán", "Cuauhtémoc", "Gustavo A. Madero", "Iztacalco", "Iztapalapa");
                break;
            case "Jalisco":
                cmbDireccionCiudad.getItems().addAll("Guadalajara", "Zapopan", "Tlaquepaque", 
                    "Tonalá", "Puerto Vallarta", "Tlajomulco de Zúñiga");
                break;
            default:
                // Para otros estados, agregar una opción genérica
                cmbDireccionCiudad.getItems().addAll("Capital del Estado", "Ciudad Principal", "Otra Ciudad");
                break;
        }
    }
    
    /**
     * Maneja el evento de guardar paciente
     */
    @FXML
    private void handleGuardarPaciente() {
        try {
            System.out.println("💾 Iniciando guardado de paciente...");
            
            // Validar campos obligatorios
            if (!validarCamposObligatorios()) {
                return;
            }
            
            // Crear objeto paciente
            Paciente nuevoPaciente = crearPacienteDesdeFormulario();
            
            // Guardar en base de datos
            int idPaciente = pacienteDAO.crear(nuevoPaciente);
            
            if (idPaciente > 0) {
                nuevoPaciente.setId(idPaciente);
                mostrarMensaje("✅ Paciente registrado exitosamente! ID: " + idPaciente, "#4CAF50");
                System.out.println("✅ Paciente guardado con ID: " + idPaciente);
                
                // Limpiar el formulario
                limpiarFormulario();
                
            } else {
                mostrarMensaje("❌ Error al registrar el paciente. Intente nuevamente.", "#D32F2F");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al guardar paciente: " + e.getMessage());
            e.printStackTrace();
            mostrarMensaje("❌ Error durante el registro: " + e.getMessage(), "#D32F2F");
        }
    }
    
    /**
     * Maneja el evento de guardar y continuar (alias para guardar)
     */
    @FXML
    private void handleGuardarContinuar() {
        // Mismo comportamiento que guardar paciente
        handleGuardarPaciente();
    }
    
    /**
     * Maneja el evento de limpiar formulario
     */
    @FXML
    private void handleLimpiarFormulario() {
        limpiarFormulario();
    }
    
    /**
     * Maneja el evento de cancelar
     */
    @FXML
    private void handleCancelar() {
        // Cerrar la ventana
        if (mainPane != null && mainPane.getScene() != null) {
            mainPane.getScene().getWindow().hide();
        }
    }
    
    /**
     * Valida que los campos obligatorios estén llenos
     */
    private boolean validarCamposObligatorios() {
        if (txtNombre == null || txtNombre.getText().trim().isEmpty()) {
            mostrarMensaje("❌ El nombre es obligatorio", "#D32F2F");
            if (txtNombre != null) txtNombre.requestFocus();
            return false;
        }
        
        if (txtApellidoPaterno == null || txtApellidoPaterno.getText().trim().isEmpty()) {
            mostrarMensaje("❌ El apellido paterno es obligatorio", "#D32F2F");
            if (txtApellidoPaterno != null) txtApellidoPaterno.requestFocus();
            return false;
        }
        
        if (dpFechaNacimiento == null || dpFechaNacimiento.getValue() == null) {
            mostrarMensaje("❌ La fecha de nacimiento es obligatoria", "#D32F2F");
            if (dpFechaNacimiento != null) dpFechaNacimiento.requestFocus();
            return false;
        }
        
        if (txtTelefonoPrincipal == null || txtTelefonoPrincipal.getText().trim().isEmpty()) {
            mostrarMensaje("❌ El teléfono principal es obligatorio", "#D32F2F");
            if (txtTelefonoPrincipal != null) txtTelefonoPrincipal.requestFocus();
            return false;
        }
        
        if (txtContactoEmergencia == null || txtContactoEmergencia.getText().trim().isEmpty()) {
            mostrarMensaje("❌ El contacto de emergencia es obligatorio", "#D32F2F");
            if (txtContactoEmergencia != null) txtContactoEmergencia.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Crea un objeto Paciente a partir de los datos del formulario
     */
    private Paciente crearPacienteDesdeFormulario() {
        Paciente paciente = new Paciente();
        
        // Datos personales
        paciente.setNombre(txtNombre.getText().trim());
        paciente.setApellidoPaterno(txtApellidoPaterno.getText().trim());
        paciente.setApellidoMaterno(txtApellidoMaterno != null ? txtApellidoMaterno.getText().trim() : null);
        paciente.setCurp(txtCurp != null ? txtCurp.getText().trim().toUpperCase() : null);
        paciente.setFechaNacimiento(dpFechaNacimiento.getValue());
        
        // Sexo
        if (cmbSexo != null && cmbSexo.getValue() != null) {
            switch (cmbSexo.getValue()) {
                case "MASCULINO":
                    paciente.setSexo(Paciente.Sexo.MASCULINO);
                    break;
                case "FEMENINO":
                    paciente.setSexo(Paciente.Sexo.FEMENINO);
                    break;
                default:
                    paciente.setSexo(Paciente.Sexo.OTRO);
                    break;
            }
        }
        
        // Contacto
        paciente.setTelefonoPrincipal(txtTelefonoPrincipal.getText().trim());
        paciente.setTelefonoSecundario(txtTelefonoSecundario != null ? txtTelefonoSecundario.getText().trim() : null);
        paciente.setEmail(txtEmail != null && !txtEmail.getText().trim().isEmpty() ? txtEmail.getText().trim() : null);
        
        // Dirección
        paciente.setDireccionCalle(txtCalle != null ? txtCalle.getText().trim() : null);
        paciente.setDireccionNumero(txtNumero != null ? txtNumero.getText().trim() : null);
        paciente.setDireccionColonia(txtColonia != null ? txtColonia.getText().trim() : null);
        paciente.setDireccionCiudad(cmbDireccionCiudad != null ? cmbDireccionCiudad.getValue() : null);
        paciente.setDireccionEstado(cmbDireccionEstado != null ? cmbDireccionEstado.getValue() : null);
        paciente.setDireccionCp(txtCodigoPostal != null ? txtCodigoPostal.getText().trim() : null);
        
        // Contacto de emergencia
        paciente.setContactoEmergenciaNombre(txtContactoEmergencia.getText().trim());
        paciente.setContactoEmergenciaTelefono(txtTelefonoEmergencia != null ? txtTelefonoEmergencia.getText().trim() : null);
        paciente.setContactoEmergenciaRelacion(cmbContactoEmergenciaRelacion != null ? cmbContactoEmergenciaRelacion.getValue() : null);
        
        // Seguro médico
        paciente.setSeguroMedico(cmbSeguroMedico != null && cmbSeguroMedico.getValue() != null ? cmbSeguroMedico.getValue() : "SIN SEGURO");
        paciente.setNumeroPoliza(txtNumeroPoliza != null && !txtNumeroPoliza.getText().trim().isEmpty() ? txtNumeroPoliza.getText().trim() : null);
        
        return paciente;
    }
    
    /**
     * Limpiar todos los campos del formulario
     */
    @Override
    protected void limpiarFormulario() {
        if (txtNombre != null) txtNombre.clear();
        if (txtApellidoPaterno != null) txtApellidoPaterno.clear();
        if (txtApellidoMaterno != null) txtApellidoMaterno.clear();
        if (txtCurp != null) txtCurp.clear();
        if (dpFechaNacimiento != null) dpFechaNacimiento.setValue(null);
        if (txtEdad != null) txtEdad.clear();
        if (cmbSexo != null) cmbSexo.setValue(null);
        if (txtTelefonoPrincipal != null) txtTelefonoPrincipal.clear();
        if (txtTelefonoSecundario != null) txtTelefonoSecundario.clear();
        if (txtEmail != null) txtEmail.clear();
        if (txtCalle != null) txtCalle.clear();
        if (txtNumero != null) txtNumero.clear();
        if (txtColonia != null) txtColonia.clear();
        if (cmbDireccionCiudad != null) cmbDireccionCiudad.setValue(null);
        if (cmbDireccionEstado != null) cmbDireccionEstado.setValue(null);
        if (txtCodigoPostal != null) txtCodigoPostal.clear();
        if (txtContactoEmergencia != null) txtContactoEmergencia.clear();
        if (txtTelefonoEmergencia != null) txtTelefonoEmergencia.clear();
        if (cmbContactoEmergenciaRelacion != null) cmbContactoEmergenciaRelacion.setValue(null);
        if (cmbSeguroMedico != null) cmbSeguroMedico.setValue("SIN SEGURO");
        if (txtNumeroPoliza != null) txtNumeroPoliza.clear();
        
        mostrarMensaje("🧹 Formulario limpiado", "#4CAF50");
    }
    
    /**
     * Muestra un mensaje en la interfaz
     */
    private void mostrarMensaje(String mensaje, String color) {
        if (lblMensaje != null) {
            lblMensaje.setText(mensaje);
            lblMensaje.setStyle("-fx-text-fill: " + color + ";");
        }
        System.out.println("💬 " + mensaje);
    }
    
    /**
     * Maneja el evento de cerrar sesión
     */
    @FXML
    public void handleLogout() {
        try {
            // Cargar la pantalla de login
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/ui/login.fxml"));
            javafx.scene.Scene loginScene = new javafx.scene.Scene(loader.load());
            
            javafx.stage.Stage currentStage = (javafx.stage.Stage) btnCancelar.getScene().getWindow();
            currentStage.setScene(loginScene);
            currentStage.setTitle("Iniciar Sesión - Hospital Santa Vida");
            currentStage.centerOnScreen();
            
        } catch (Exception e) {
            mostrarMensaje("Error al cerrar sesión: " + e.getMessage(), "red");
        }
    }
}