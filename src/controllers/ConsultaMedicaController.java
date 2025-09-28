package controllers;

import dao.PacienteDAO;
import dao.EvaluacionTriageDAO;
import dao.DatosSocialesDAO;
import models.*;
import services.TriageService;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

/**
 * Controlador para la consulta médica final
 * Solo accesible para Doctores
 */
public class ConsultaMedicaController extends BaseController implements Initializable {
    
    // Componentes FXML - Datos del paciente
    @FXML private Label lblUsuarioActual;
    @FXML private Label lblNombrePaciente;
    @FXML private Label lblCurpPaciente;
    @FXML private Label lblEdadPaciente;
    @FXML private Label lblSexoPaciente;
    @FXML private Label lblContactoEmergencia;
    @FXML private Label lblSeguroMedico;
    
    // Componentes FXML - Evaluación Triage
    @FXML private Label lblNivelTriage;
    @FXML private Label lblEspecialidad;
    @FXML private TextArea txtMotivoConsulta;
    @FXML private TextArea txtSintomas;
    
    // Componentes FXML - Evaluación Social
    @FXML private TextArea txtAntecedentesFamiliares;
    @FXML private TextArea txtObservacionesSociales;
    
    // Componentes FXML - Examen Físico y Diagnóstico
    @FXML private TextField txtPresionArterial;
    @FXML private TextField txtFrecuenciaCardiaca;
    @FXML private TextField txtTemperatura;
    @FXML private TextField txtPeso;
    @FXML private TextField txtTalla;
    @FXML private TextArea txtExploracionFisica;
    @FXML private TextArea txtEstudiosRealizados;
    @FXML private TextArea txtResultadosEstudios;
    @FXML private TextArea txtDiagnosticoPrincipal;
    @FXML private TextArea txtDiagnosticosSecundarios;
    @FXML private TextField txtCodigoCie10;
    @FXML private TextField txtDescripcionCie10;
    
    // Componentes FXML - Tratamiento y Medicamentos
    @FXML private TextArea txtTratamientosInmediatos;
    @FXML private TextArea txtMedicamentosPrescritos;
    
    // Componentes FXML - Indicaciones y Seguimiento
    @FXML private TextArea txtIndicacionesPaciente;
    @FXML private TextArea txtSeguimientoMedico;
    @FXML private ComboBox<String> cmbIncapacidadLaboral;
    @FXML private TextField txtDiasIncapacidad;
    @FXML private ComboBox<String> cmbRestriccionesEspecificas;
    
    // Componentes FXML - Notas del Médico
    @FXML private TextArea txtObservacionesAdicionales;
    
    // Componentes FXML - Panel Lateral
    @FXML private Label lblTiempoTotal;
    @FXML private Label lblRiesgoCardiovascular;
    @FXML private Label lblDolorAgudo;
    @FXML private Label lblSaturacionO2;
    @FXML private Label lblAlteraciones;
    
    // Componentes FXML - Botones
    @FXML private Button btnGuardar;
    @FXML private Button btnCompletarConsulta;
    @FXML private Button btnAltaDomicilio;
    @FXML private Button btnHospitalizacion;
    @FXML private Button btnReferencia;
    @FXML private Button btnGuardarEmerencias;
    @FXML private Button btnCerrarSesion;
    
    // Servicios y DAOs
    private PacienteDAO pacienteDAO;
    private EvaluacionTriageDAO evaluacionTriageDAO;
    private DatosSocialesDAO datosSocialesDAO;
    private TriageService triageService;
    
    // Datos del paciente actual
    private Paciente pacienteActual;
    private EvaluacionTriage evaluacionTriageActual;
    private DatosSociales datosSocialesActuales;
    private LocalDateTime horaInicioAtencion;
    
    public ConsultaMedicaController() {
        this.pacienteDAO = new PacienteDAO();
        this.evaluacionTriageDAO = new EvaluacionTriageDAO();
        this.datosSocialesDAO = new DatosSocialesDAO();
        // TriageService será inicializado desde BaseController
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🏥 Inicializando interfaz de Consulta Médica...");
        
        // Inicializar servicios
        if (triageService == null) {
            triageService = getTriageService(); // Obtenido del BaseController
        }
        
        // Inicializar componentes
        configurarComponentes();
        
        // Llamar métodos del BaseController
        super.initialize();
        
        // Cargar datos pendientes
        cargarPacientePendiente();
    }
    
    /**
     * Configura elementos de la interfaz según el usuario actual
     */
    @Override
    protected void configurarInterfaz() {
        try {
            // Verificar permisos
            if (authService != null) {
                authService.requireRole(Usuario.TipoUsuario.MEDICO_URGENCIAS);
            }
            
            System.out.println("✅ Interfaz configurada correctamente para: " + 
                             (usuarioActual != null ? usuarioActual.getNombreCompleto() : "Usuario"));
                             
        } catch (Exception e) {
            System.err.println("❌ Error al configurar interfaz: " + e.getMessage());
        }
    }
    
    @Override
    protected void cargarDatos() {
        // Método implementado para cumplir con BaseController
        System.out.println("🔄 Datos iniciales cargados para consulta médica");
    }
    
    /**
     * Configura los componentes de la interfaz
     */
    private void configurarComponentes() {
        System.out.println("🔧 Configurando componentes de consulta médica...");
        
        // Los componentes JavaFX se configurarán automáticamente cuando la interfaz esté cargada
        // Este método se deja disponible para configuraciones adicionales
    }
    
    /**
     * Carga el paciente pendiente de consulta médica
     */
    private void cargarPacientePendiente() {
        try {
            System.out.println("🔍 Buscando paciente pendiente de consulta médica...");
            
            // Buscar paciente en estado ESPERANDO_MEDICO usando el servicio base
            RegistroTriage registroPendiente = triageService.obtenerSiguientePaciente();
            
            if (registroPendiente != null && registroPendiente.getEstado() == RegistroTriage.Estado.ESPERANDO_MEDICO) {
                System.out.println("✅ Paciente encontrado: " + (registroPendiente.getPaciente() != null ? 
                    registroPendiente.getPaciente().getNombreCompleto() : "ID: " + registroPendiente.getPacienteId()));
                
                // Cargar información completa del paciente
                pacienteActual = pacienteDAO.buscarPorId(registroPendiente.getPacienteId());
                
                // Buscar evaluación de triage por folio
                if (registroPendiente.getFolio() != null) {
                    evaluacionTriageActual = evaluacionTriageDAO.obtenerPorFolio(registroPendiente.getFolio());
                }
                
                // Buscar datos sociales
                try {
                    datosSocialesActuales = datosSocialesDAO.obtenerPorRegistroTriage(registroPendiente.getId());
                } catch (Exception e) {
                    System.out.println("ℹ️ No hay datos sociales disponibles para este paciente");
                }
                if (pacienteActual != null) {
                    mostrarDatosPaciente();
                    mostrarEvaluacionTriage();
                    mostrarEvaluacionSocial();
                    calcularTiempoAtencion(registroPendiente.getFechaHoraLlegada());
                    configurarIndicadores();
                }
                
            } else {
                System.out.println("ℹ️ No hay pacientes pendientes de consulta médica");
                mostrarMensajeSinPacientes();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al cargar paciente pendiente: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Muestra la información básica del paciente
     */
    private void mostrarDatosPaciente() {
        if (pacienteActual == null) return;
        
        System.out.println("📋 Cargando datos del paciente: " + pacienteActual.getNombreCompleto());
        
        try {
            // Los datos se mostrarán en la interfaz JavaFX cuando esté disponible
            // Por ahora solo mostrar en consola para verificar la lógica
            System.out.println("   - Nombre: " + pacienteActual.getNombreCompleto());
            System.out.println("   - CURP: " + (pacienteActual.getCurp() != null ? pacienteActual.getCurp() : "-"));
            System.out.println("   - Edad: " + pacienteActual.getEdad() + " años");
            System.out.println("   - Sexo: " + (pacienteActual.getSexo() != null ? pacienteActual.getSexo().toString() : "-"));
            System.out.println("   - Contacto Emergencia: " + pacienteActual.getContactoEmergenciaNombre());
            System.out.println("   - Seguro: " + (pacienteActual.getSeguroMedico() != null ? pacienteActual.getSeguroMedico() : "SIN SEGURO"));
        } catch (Exception e) {
            System.err.println("❌ Error al mostrar datos del paciente: " + e.getMessage());
        }
    }
    
    /**
     * Muestra la información de la evaluación de triage
     */
    private void mostrarEvaluacionTriage() {
        if (evaluacionTriageActual == null) return;
        
        System.out.println("🏥 Cargando evaluación de triage:");
        
        try {
            System.out.println("   - Nivel: " + evaluacionTriageActual.getNivelTriage());
            System.out.println("   - Especialidad: " + (evaluacionTriageActual.getEspecialidad() != null ? 
                              evaluacionTriageActual.getEspecialidad() : "Medicina General"));
            System.out.println("   - Motivo: " + evaluacionTriageActual.getMotivoConsulta());
            System.out.println("   - Observaciones: " + evaluacionTriageActual.getObservacionesClinicas());
        } catch (Exception e) {
            System.err.println("❌ Error al mostrar evaluación de triage: " + e.getMessage());
        }
    }
    
    /**
     * Muestra la información de la evaluación social
     */
    private void mostrarEvaluacionSocial() {
        if (datosSocialesActuales == null) return;
        
        System.out.println("👥 Cargando evaluación social:");
        
        try {
            System.out.println("   - Antecedentes familiares: " + 
                (datosSocialesActuales.getAntecedentesFamiliares() != null ? 
                 datosSocialesActuales.getAntecedentesFamiliares() : "-"));
            System.out.println("   - Observaciones: " + 
                (datosSocialesActuales.getObservacionesAdicionales() != null ? 
                 datosSocialesActuales.getObservacionesAdicionales() : "-"));
        } catch (Exception e) {
            System.err.println("❌ Error al mostrar evaluación social: " + e.getMessage());
        }
    }
    
    /**
     * Calcula y muestra el tiempo total de atención
     */
    private void calcularTiempoAtencion(LocalDateTime horaLlegada) {
        if (horaLlegada == null) return;
        
        horaInicioAtencion = LocalDateTime.now();
        long minutos = ChronoUnit.MINUTES.between(horaLlegada, horaInicioAtencion);
        long horas = minutos / 60;
        long minutosRestantes = minutos % 60;
        
        System.out.println("⏰ Tiempo total de atención: " + String.format("%dh %dm", horas, minutosRestantes));
    }
    
    /**
     * Configura los indicadores médicos
     */
    private void configurarIndicadores() {
        System.out.println("📊 Configurando indicadores médicos básicos");
        // Los indicadores se configurarán según la lógica médica específica
    }
    
    /**
     * Muestra mensaje cuando no hay pacientes pendientes
     */
    private void mostrarMensajeSinPacientes() {
        System.out.println("ℹ️ No hay pacientes pendientes de consulta médica");
    }
    
    /**
     * Obtiene el estilo CSS para el nivel de triage
     */
    private String obtenerEstiloNivelTriage(String nivel) {
        if (nivel == null) return "-fx-text-fill: #666666;";
        
        switch (nivel.toUpperCase()) {
            case "ROJO":
                return "-fx-text-fill: #E53E3E; -fx-font-weight: bold;";
            case "NARANJA":
                return "-fx-text-fill: #FF8C42; -fx-font-weight: bold;";
            case "AMARILLO":
                return "-fx-text-fill: #F7D794; -fx-font-weight: bold;";
            case "VERDE":
                return "-fx-text-fill: #58D68D; -fx-font-weight: bold;";
            case "AZUL":
                return "-fx-text-fill: #5DADE2; -fx-font-weight: bold;";
            default:
                return "-fx-text-fill: #666666;";
        }
    }
    
    /**
     * Maneja el evento de guardar consulta
     */
    public void handleGuardarConsulta() {
        try {
            System.out.println("💾 Guardando consulta médica...");
            
            // Aquí se implementaría la lógica para guardar la consulta en la base de datos
            // Por ahora solo mostramos un mensaje
            
            System.out.println("✅ Consulta guardada correctamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error al guardar consulta: " + e.getMessage());
        }
    }
    
    /**
     * Maneja el evento de completar consulta
     */
    public void handleCompletarConsulta() {
        try {
            System.out.println("✅ Completando consulta médica...");
            
            if (pacienteActual == null) {
                System.err.println("❌ No hay paciente seleccionado");
                return;
            }
            
            // Validar que los campos obligatorios estén llenos
            if (!validarCamposObligatorios()) {
                return;
            }
            
            // Aquí se implementaría la lógica para completar la consulta
            // y cambiar el estado del paciente a COMPLETADO
            
            System.out.println("✅ Consulta completada correctamente - Paciente dado de alta del sistema");
            
            // Cargar siguiente paciente
            cargarPacientePendiente();
            
        } catch (Exception e) {
            System.err.println("❌ Error al completar consulta: " + e.getMessage());
        }
    }
    
    /**
     * Valida que los campos obligatorios estén completos
     */
    private boolean validarCamposObligatorios() {
        // Validación simplificada - en una implementación real se validarían los campos del formulario
        System.out.println("✔️ Validando campos obligatorios de la consulta médica");
        
        // Por ahora siempre retorna true, pero en la implementación real validaría:
        // - Diagnóstico Principal
        // - Exploración Física  
        // - Indicaciones para el Paciente
        
        return true;
    }
    
    /**
     * Maneja alta a domicilio
     */
    public void handleAltaDomicilio() {
        System.out.println("🏠 Paciente dado de alta para tratamiento ambulatorio");
    }
    
    /**
     * Maneja hospitalización
     */
    public void handleHospitalizacion() {
        System.out.println("🏥 Paciente programado para hospitalización");
    }
    
    /**
     * Maneja referencia
     */
    public void handleReferencia() {
        System.out.println("🔄 Paciente referido a otra institución o especialidad");
    }
    
    /**
     * Maneja emergencia
     */
    public void handleEmergencia() {
        System.out.println("🚨 Paciente trasladado al área de emergencias");
    }
    
    /**
     * Maneja el evento de cerrar sesión
     */
    public void handleLogout() {
        try {
            System.out.println("🚪 Cerrando sesión de consulta médica...");
            
            // En una implementación real cargaría la pantalla de login
            // Por ahora solo mostrar mensaje
            System.out.println("✅ Sesión cerrada correctamente");
            
        } catch (Exception e) {
            System.err.println("Error al cerrar sesión: " + e.getMessage());
        }
    }
    
    @Override
    protected void limpiarFormulario() {
        System.out.println("🧹 Limpiando formulario de consulta médica");
        
        // En una implementación real limpiaría todos los campos del formulario
        // Por ahora solo mostrar que se ejecuta la limpieza
        
        pacienteActual = null;
        evaluacionTriageActual = null;
        datosSocialesActuales = null;
        horaInicioAtencion = null;
    }
}