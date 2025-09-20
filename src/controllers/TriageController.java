package controllers;

import models.Paciente;
import models.RegistroTriage;
import models.Usuario;
import dao.PacienteDAO;
import structures.TriageQueue;

/**
 * Controlador para la pantalla de triage médico
 * Permite al médico de triage evaluar pacientes y asignar prioridades
 */
public class TriageController extends BaseController {
    
    private PacienteDAO pacienteDAO;
    private TriageQueue triageQueue;
    
    // Campos de la interfaz (en JavaFX serían @FXML)
    private String txtNombrePaciente;
    private String txtApellidoPaciente;
    private String txtCurp;
    private String txtTelefono;
    private String txtDireccion;
    private String cmbGenero;
    private String txtSintomas;
    private String cmbNivelUrgencia;
    private String txtObservaciones;
    
    public TriageController() {
        super();
        this.pacienteDAO = new PacienteDAO();
        this.triageQueue = new TriageQueue(); // Crear instancia directamente
    }
    
    @Override
    protected void configurarInterfaz() {
        logAction("Configurando interfaz de triage");
        
        // Verificar permisos específicos
        Usuario usuarioActual = authService.getUsuarioActual();
        if (usuarioActual == null || usuarioActual.getTipoUsuario() != Usuario.TipoUsuario.MEDICO_TRIAGE) {
            showError("No tiene permisos para acceder al sistema de triage");
            navigateTo("/views/Login.fxml");
            return;
        }
        
        // Configurar campos y eventos
        configurarCamposFormulario();
        configurarComboBoxes();
        cargarPacientesEnEspera();
    }
    
    @Override
    protected void cargarDatos() {
        logAction("Cargando datos de triage");
        
        try {
            // Cargar estadísticas del día
            cargarEstadisticasDia();
            
            // Actualizar cola de triage
            actualizarColaTriage();
            
            // Cargar configuraciones del sistema
            cargarConfiguracionTriage();
            
        } catch (Exception e) {
            logAction("Error al cargar datos: " + e.getMessage());
            handleDatabaseError(e);
        }
    }
    
    @Override
    protected void limpiarFormulario() {
        txtNombrePaciente = "";
        txtApellidoPaciente = "";
        txtCurp = "";
        txtTelefono = "";
        txtDireccion = "";
        cmbGenero = "";
        txtSintomas = "";
        cmbNivelUrgencia = "";
        txtObservaciones = "";
    }
    
    /**
     * Registra un nuevo paciente en triage
     */
    public void handleRegistrarPaciente() {
        logAction("Iniciando registro de paciente en triage");
        
        if (!validarFormulario()) {
            return;
        }
        
        try {
            // Crear nuevo paciente
            Paciente paciente = crearPacienteDesdeFormulario();
            
            // Registrar en base de datos
            int pacienteId = pacienteDAO.crear(paciente);
            paciente.setId(pacienteId);
            
            if (pacienteId > 0) {
                // Crear registro de triage
                RegistroTriage registro = crearRegistroTriage(paciente);
                
                // Agregar a la cola de triage
                triageQueue.agregarPaciente(registro);
                
                showInfo("Paciente registrado exitosamente en triage");
                logAction("Paciente " + paciente.getNombreCompleto() + " agregado a cola " + 
                         registro.getNivelUrgencia());
                
                limpiarFormulario();
                actualizarColaTriage();
            } else {
                showError("Error al registrar paciente en la base de datos");
            }
            
        } catch (Exception e) {
            logAction("Error durante registro: " + e.getMessage());
            handleDatabaseError(e);
        }
    }
    
    /**
     * Busca un paciente existente por CURP
     */
    public void handleBuscarPaciente() {
        String curp = txtCurp;
        
        if (curp == null || curp.trim().isEmpty()) {
            showError("Ingrese el CURP del paciente para buscar");
            return;
        }
        
        try {
            logAction("Buscando paciente con CURP: " + curp);
            Paciente paciente = pacienteDAO.buscarPorCurp(curp);
            
            if (paciente != null) {
                cargarDatosPaciente(paciente);
                showInfo("Paciente encontrado: " + paciente.getNombreCompleto());
            } else {
                showInfo("No se encontró paciente con ese CURP. Se puede registrar como nuevo.");
            }
            
        } catch (Exception e) {
            logAction("Error al buscar paciente: " + e.getMessage());
            handleDatabaseError(e);
        }
    }
    
    /**
     * Atiende al siguiente paciente en la cola
     */
    public void handleAtenderSiguiente() {
        logAction("Atendiendo siguiente paciente en cola");
        
        try {
            RegistroTriage siguientePaciente = triageQueue.obtenerSiguientePaciente();
            
            if (siguientePaciente != null) {
                mostrarDatosPacienteParaAtencion(siguientePaciente);
                showInfo("Atendiendo a: " + siguientePaciente.getPaciente().getNombreCompleto());
                actualizarColaTriage();
            } else {
                showInfo("No hay pacientes en espera");
            }
            
        } catch (Exception e) {
            logAction("Error al atender paciente: " + e.getMessage());
            handleDatabaseError(e);
        }
    }
    
    /**
     * Actualiza el nivel de urgencia de un paciente
     */
    public void handleActualizarUrgencia() {
        // Implementar lógica para cambiar prioridad de paciente ya en cola
        logAction("Actualizando nivel de urgencia");
        showInfo("Función disponible próximamente");
    }
    
    /**
     * Muestra el historial médico de un paciente
     */
    public void handleVerHistorial() {
        String curp = txtCurp;
        
        if (curp == null || curp.trim().isEmpty()) {
            showError("Seleccione un paciente para ver su historial");
            return;
        }
        
        try {
            // En una implementación real abriría una nueva ventana
            logAction("Mostrando historial de paciente: " + curp);
            showInfo("Historial médico - Función disponible próximamente");
            
        } catch (Exception e) {
            logAction("Error al mostrar historial: " + e.getMessage());
            handleDatabaseError(e);
        }
    }
    
    /**
     * Crea un objeto Paciente desde los datos del formulario
     */
    private Paciente crearPacienteDesdeFormulario() {
        Paciente paciente = new Paciente();
        
        paciente.setNombre(txtNombrePaciente.trim());
        paciente.setApellidoPaterno(txtApellidoPaciente.trim().split(" ")[0]);
        if (txtApellidoPaciente.trim().split(" ").length > 1) {
            paciente.setApellidoMaterno(txtApellidoPaciente.trim().split(" ")[1]);
        }
        paciente.setCurp(txtCurp.trim().toUpperCase());
        paciente.setTelefonoPrincipal(txtTelefono.trim());
        paciente.setDireccionCalle(txtDireccion.trim());
        
        // Convertir string a enum
        if ("MASCULINO".equals(cmbGenero)) {
            paciente.setSexo(Paciente.Sexo.MASCULINO);
        } else if ("FEMENINO".equals(cmbGenero)) {
            paciente.setSexo(Paciente.Sexo.FEMENINO);
        } else {
            paciente.setSexo(Paciente.Sexo.OTRO);
        }
        
        // En una implementación real se convertiría la fecha del DatePicker
        // paciente.setFechaNacimiento(dtpFechaNacimiento.getValue());
        
        return paciente;
    }
    
    /**
     * Crea un registro de triage desde los datos del formulario
     */
    private RegistroTriage crearRegistroTriage(Paciente paciente) {
        RegistroTriage registro = new RegistroTriage();
        
        registro.setPaciente(paciente);
        registro.setMedicoTriage(authService.getUsuarioActual());
        registro.setSintomasPrincipales(txtSintomas.trim());
        registro.setObservacionesTriage(txtObservaciones.trim());
        
        // Convertir string a enum
        RegistroTriage.NivelUrgencia nivel = RegistroTriage.NivelUrgencia.valueOf(cmbNivelUrgencia);
        registro.setNivelUrgencia(nivel);
        
        return registro;
    }
    
    /**
     * Carga los datos de un paciente existente en el formulario
     */
    private void cargarDatosPaciente(Paciente paciente) {
        txtNombrePaciente = paciente.getNombre();
        txtApellidoPaciente = paciente.getApellidoPaterno() + " " + 
                             (paciente.getApellidoMaterno() != null ? paciente.getApellidoMaterno() : "");
        txtCurp = paciente.getCurp();
        txtTelefono = paciente.getTelefonoPrincipal();
        txtDireccion = paciente.getDireccionCalle();
        cmbGenero = paciente.getSexo().toString();
        // dtpFechaNacimiento = paciente.getFechaNacimiento();
    }
    
    /**
     * Configura los campos del formulario
     */
    private void configurarCamposFormulario() {
        // En JavaFX se configurarían los listeners y validaciones
        logAction("Configurando campos del formulario");
    }
    
    /**
     * Configura los ComboBox con opciones válidas
     */
    private void configurarComboBoxes() {
        // Configurar género
        // cmbGenero.getItems().addAll("MASCULINO", "FEMENINO", "OTRO");
        
        // Configurar niveles de urgencia
        // cmbNivelUrgencia.getItems().addAll("ROJO", "NARANJA", "AMARILLO", "VERDE", "AZUL");
        
        logAction("ComboBoxes configurados");
    }
    
    /**
     * Carga pacientes en espera
     */
    private void cargarPacientesEnEspera() {
        try {
            // En JavaFX actualizaría una TableView o ListView
            int pacientesEnEspera = triageQueue.getTotalPacientesEspera();
            logAction("Pacientes en espera: " + pacientesEnEspera);
            
        } catch (Exception e) {
            logAction("Error al cargar pacientes en espera: " + e.getMessage());
        }
    }
    
    /**
     * Carga estadísticas del día actual
     */
    private void cargarEstadisticasDia() {
        // Implementar carga de estadísticas
        logAction("Cargando estadísticas del día");
    }
    
    /**
     * Actualiza la visualización de la cola de triage
     */
    private void actualizarColaTriage() {
        try {
            // En JavaFX actualizaría la vista de la cola
            logAction("Actualizando vista de cola de triage");
            
            // Obtener estadísticas por nivel
            int rojos = triageQueue.getCantidadPorNivel(RegistroTriage.NivelUrgencia.ROJO);
            int naranjas = triageQueue.getCantidadPorNivel(RegistroTriage.NivelUrgencia.NARANJA);
            int amarillos = triageQueue.getCantidadPorNivel(RegistroTriage.NivelUrgencia.AMARILLO);
            int verdes = triageQueue.getCantidadPorNivel(RegistroTriage.NivelUrgencia.VERDE);
            int azules = triageQueue.getCantidadPorNivel(RegistroTriage.NivelUrgencia.AZUL);
            
            logAction("Cola actualizada - ROJO: " + rojos + ", NARANJA: " + naranjas + 
                     ", AMARILLO: " + amarillos + ", VERDE: " + verdes + ", AZUL: " + azules);
                     
        } catch (Exception e) {
            logAction("Error al actualizar cola: " + e.getMessage());
        }
    }
    
    /**
     * Carga configuración del sistema de triage
     */
    private void cargarConfiguracionTriage() {
        logAction("Cargando configuración de triage");
        // Cargar configuraciones específicas del hospital
    }
    
    /**
     * Muestra datos del paciente para atención
     */
    private void mostrarDatosPacienteParaAtencion(RegistroTriage registro) {
        // En JavaFX abriría un diálogo o nueva ventana
        logAction("Mostrando datos para atención: " + registro.getPaciente().getNombreCompleto());
    }
    
    /**
     * Valida el formulario antes de registrar
     */
    private boolean validarFormulario() {
        if (!validarCamposObligatorios(txtNombrePaciente, txtApellidoPaciente, txtCurp)) {
            return false;
        }
        
        if (!validarCurp()) {
            return false;
        }
        
        if (!validarNivelUrgencia()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida el formato del CURP
     */
    private boolean validarCurp() {
        if (txtCurp == null || txtCurp.length() != 18) {
            showError("El CURP debe tener exactamente 18 caracteres");
            return false;
        }
        return true;
    }
    
    /**
     * Valida que se haya seleccionado un nivel de urgencia
     */
    private boolean validarNivelUrgencia() {
        if (cmbNivelUrgencia == null || cmbNivelUrgencia.trim().isEmpty()) {
            showError("Debe seleccionar un nivel de urgencia");
            return false;
        }
        return true;
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        // Limpiar recursos específicos del triage
        limpiarFormulario();
        pacienteDAO = null;
    }
}