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
    @FXML private ComboBox<String> cbSexo;
    @FXML private TextField txtTelefonoPrincipal;
    @FXML private TextField txtTelefonoSecundario;
    @FXML private TextField txtEmail;
    @FXML private TextField txtCalle;
    @FXML private TextField txtNumero;
    @FXML private TextField txtColonia;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtEstado;
    @FXML private TextField txtCodigoPostal;
    @FXML private TextField txtContactoEmergencia;
    @FXML private TextField txtTelefonoEmergencia;
    @FXML private TextField txtRelacionEmergencia;
    @FXML private TextField txtSeguroMedico;
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
        if (cbSexo != null) {
            cbSexo.getItems().addAll("MASCULINO", "FEMENINO", "OTRO");
        }
        
        // Configurar campos con valores por defecto
        if (txtSeguroMedico != null) {
            txtSeguroMedico.setText("SIN SEGURO");
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
        if (cbSexo != null && cbSexo.getValue() != null) {
            switch (cbSexo.getValue()) {
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
        paciente.setDireccionCiudad(txtCiudad != null ? txtCiudad.getText().trim() : null);
        paciente.setDireccionEstado(txtEstado != null ? txtEstado.getText().trim() : null);
        paciente.setDireccionCp(txtCodigoPostal != null ? txtCodigoPostal.getText().trim() : null);
        
        // Contacto de emergencia
        paciente.setContactoEmergenciaNombre(txtContactoEmergencia.getText().trim());
        paciente.setContactoEmergenciaTelefono(txtTelefonoEmergencia != null ? txtTelefonoEmergencia.getText().trim() : null);
        paciente.setContactoEmergenciaRelacion(txtRelacionEmergencia != null ? txtRelacionEmergencia.getText().trim() : null);
        
        // Seguro médico
        paciente.setSeguroMedico(txtSeguroMedico != null && !txtSeguroMedico.getText().trim().isEmpty() ? txtSeguroMedico.getText().trim() : "SIN SEGURO");
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
        if (cbSexo != null) cbSexo.setValue(null);
        if (txtTelefonoPrincipal != null) txtTelefonoPrincipal.clear();
        if (txtTelefonoSecundario != null) txtTelefonoSecundario.clear();
        if (txtEmail != null) txtEmail.clear();
        if (txtCalle != null) txtCalle.clear();
        if (txtNumero != null) txtNumero.clear();
        if (txtColonia != null) txtColonia.clear();
        if (txtCiudad != null) txtCiudad.clear();
        if (txtEstado != null) txtEstado.clear();
        if (txtCodigoPostal != null) txtCodigoPostal.clear();
        if (txtContactoEmergencia != null) txtContactoEmergencia.clear();
        if (txtTelefonoEmergencia != null) txtTelefonoEmergencia.clear();
        if (txtRelacionEmergencia != null) txtRelacionEmergencia.clear();
        if (txtSeguroMedico != null) txtSeguroMedico.setText("SIN SEGURO");
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
}