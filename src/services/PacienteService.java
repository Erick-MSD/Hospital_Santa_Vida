package services;

import dao.PacienteDAO;
import dao.RegistroTriageDAO;
import dao.DatosSocialesDAO;
import dao.AtencionMedicaDAO;
import models.Paciente;
import models.EstadoPaciente;
import models.RegistroTriage;
import models.DatosSociales;
import models.AtencionMedica;
import structures.PacienteBST;
import utils.ValidationUtils;
import services.PacienteServiceResults.*;
import controllers.BaseController;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

/**
 * Servicio de gestión integral de pacientes
 * Maneja registro, búsqueda, actualización y seguimiento de pacientes
 * Integra información médica, social y administrativa
 */
public class PacienteService {
    
    private final PacienteDAO pacienteDAO;
    private final RegistroTriageDAO triageDAO;
    private final DatosSocialesDAO datosSocialesDAO;
    private final AtencionMedicaDAO atencionMedicaDAO;
    private final PacienteBST indicePacientes;
    
    /**
     * Constructor del servicio de pacientes
     */
    public PacienteService() {
        this.pacienteDAO = new PacienteDAO();
        this.triageDAO = new RegistroTriageDAO();
        this.datosSocialesDAO = new DatosSocialesDAO();
        this.atencionMedicaDAO = new AtencionMedicaDAO();
        this.indicePacientes = new PacienteBST();
        
        // Cargar índice de pacientes
        cargarIndicePacientes();
    }
    
    /**
     * Registra un nuevo paciente en el sistema
     * @param tokenSesion Token de sesión del usuario
     * @param datosRegistro Datos del paciente a registrar
     * @return Resultado del registro
     */
    public ResultadoRegistroPaciente registrarPaciente(String tokenSesion, 
                                                      DatosRegistroPaciente datosRegistro) {
        try {
            // Verificar permisos
            if (!BaseController.getAuthService().tienePermiso(tokenSesion, AuthenticationService.Permiso.CREAR_PACIENTES)) {
                return new ResultadoRegistroPaciente(false, "Sin permisos para registrar pacientes", null);
            }
            
            // Validar datos
            String validacion = validarDatosRegistro(datosRegistro);
            if (validacion != null) {
                return new ResultadoRegistroPaciente(false, validacion, null);
            }
            
            // Verificar duplicados por CURP
            if (datosRegistro.getCurp() != null && !datosRegistro.getCurp().isEmpty()) {
                if (pacienteDAO.existeCurp(datosRegistro.getCurp())) {
                    return new ResultadoRegistroPaciente(false, 
                        "Ya existe un paciente registrado con ese CURP", null);
                }
            }
            
            // Crear paciente
            Paciente paciente = new Paciente();
            paciente.setNombreCompleto(datosRegistro.getNombreCompleto());
            paciente.setFechaNacimiento(datosRegistro.getFechaNacimiento());
            paciente.setGenero(datosRegistro.getGenero());
            paciente.setCurp(datosRegistro.getCurp());
            paciente.setRfc(datosRegistro.getRfc());
            paciente.setTelefono(datosRegistro.getTelefono());
            paciente.setEmail(datosRegistro.getEmail());
            paciente.setDireccionCompleta(datosRegistro.getDireccionCompleta());
            paciente.setContactoEmergenciaNombre(datosRegistro.getContactoEmergenciaNombre());
            paciente.setContactoEmergenciaTelefono(datosRegistro.getContactoEmergenciaTelefono());
            paciente.setContactoEmergenciaRelacion(datosRegistro.getContactoEmergenciaRelacion());
            paciente.setTipoSangre(datosRegistro.getTipoSangre());
            paciente.setAlergias(datosRegistro.getAlergias());
            paciente.setEnfermedadesCronicas(datosRegistro.getEnfermedadesCronicas());
            paciente.setMedicamentosActuales(datosRegistro.getMedicamentosActuales());
            paciente.setObservacionesMedicas(datosRegistro.getObservacionesMedicas());
            paciente.setEstadoActual(EstadoPaciente.REGISTRADO);
            paciente.setFechaRegistro(LocalDateTime.now());
            
            // Guardar en base de datos
            if (pacienteDAO.insertar(paciente)) {
                // Agregar al índice BST
                indicePacientes.insertar(paciente);
                
                return new ResultadoRegistroPaciente(true, 
                    "Paciente registrado exitosamente. Expediente: " + paciente.getNumeroExpediente(), 
                    paciente);
            } else {
                return new ResultadoRegistroPaciente(false, "Error al registrar paciente", null);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al registrar paciente: " + e.getMessage());
            return new ResultadoRegistroPaciente(false, "Error del sistema", null);
        }
    }
    
    /**
     * Busca pacientes por múltiples criterios
     * @param tokenSesion Token de sesión
     * @param criterios Criterios de búsqueda
     * @return Lista de pacientes encontrados
     */
    public List<Paciente> buscarPacientes(String tokenSesion, CriteriosBusquedaPaciente criterios) {
        if (!BaseController.getAuthService().tienePermiso(tokenSesion, AuthenticationService.Permiso.VER_PACIENTES)) {
            return new ArrayList<>();
        }
        
        try {
            // Búsqueda por número de expediente (más eficiente)
            if (criterios.getNumeroExpediente() != null && !criterios.getNumeroExpediente().isEmpty()) {
                Paciente paciente = pacienteDAO.buscarPorNumeroExpediente(criterios.getNumeroExpediente());
                List<Paciente> resultado = new ArrayList<>();
                if (paciente != null) {
                    resultado.add(paciente);
                }
                return resultado;
            }
            
            // Búsqueda por CURP
            if (criterios.getCurp() != null && !criterios.getCurp().isEmpty()) {
                Paciente paciente = pacienteDAO.buscarPorCurp(criterios.getCurp());
                List<Paciente> resultado = new ArrayList<>();
                if (paciente != null) {
                    resultado.add(paciente);
                }
                return resultado;
            }
            
            // Búsqueda por nombre (usar BST)
            if (criterios.getNombre() != null && !criterios.getNombre().isEmpty()) {
                return indicePacientes.buscarPorNombre(criterios.getNombre());
            }
            
            // Búsquedas más complejas en base de datos
            if (criterios.getFechaNacimiento() != null) {
                return pacienteDAO.buscarPorFechaNacimiento(criterios.getFechaNacimiento());
            }
            
            if (criterios.getEstado() != null) {
                return pacienteDAO.buscarPorEstado(criterios.getEstado());
            }
            
            // Si no hay criterios específicos, retornar lista vacía
            return new ArrayList<>();
            
        } catch (SQLException e) {
            System.err.println("Error al buscar pacientes: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Busca un paciente por su ID
     * @param tokenSesion Token de sesión
     * @param pacienteId ID del paciente
     * @return Paciente encontrado o null si no existe
     */
    public Paciente buscarPorId(String tokenSesion, int pacienteId) {
        if (!BaseController.getAuthService().tienePermiso(tokenSesion, AuthenticationService.Permiso.VER_PACIENTES)) {
            return null;
        }
        
        try {
            return pacienteDAO.buscarPorId(pacienteId);
        } catch (SQLException e) {
            System.err.println("Error al buscar paciente por ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtiene información completa de un paciente
     * @param tokenSesion Token de sesión
     * @param pacienteId ID del paciente
     * @return Información completa del paciente
     */
    public InformacionCompletaPaciente obtenerInformacionCompleta(String tokenSesion, int pacienteId) {
        if (!BaseController.getAuthService().tienePermiso(tokenSesion, AuthenticationService.Permiso.VER_PACIENTES)) {
            return null;
        }
        
        try {
            // Obtener datos básicos del paciente
            Paciente paciente = pacienteDAO.buscarPorId(pacienteId);
            if (paciente == null) {
                return null;
            }
            
            // Obtener historial de triage
            List<RegistroTriage> historialTriage = triageDAO.obtenerPorPaciente(pacienteId);
            
            // Obtener datos sociales
            List<DatosSociales> datosSociales = datosSocialesDAO.obtenerPorPaciente(pacienteId);
            
            // Obtener atenciones médicas
            List<AtencionMedica> atencionesMedicas = atencionMedicaDAO.obtenerPorPaciente(pacienteId);
            
            return new InformacionCompletaPaciente(paciente, historialTriage, 
                                                  datosSociales, atencionesMedicas);
            
        } catch (SQLException e) {
            System.err.println("Error al obtener información completa: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Actualiza los datos básicos de un paciente
     * @param tokenSesion Token de sesión
     * @param paciente Datos actualizados del paciente
     * @return true si se actualizó correctamente
     */
    public boolean actualizarPaciente(String tokenSesion, Paciente paciente) {
        if (!BaseController.getAuthService().tienePermiso(tokenSesion, AuthenticationService.Permiso.ACTUALIZAR_PACIENTES)) {
            return false;
        }
        
        try {
            boolean actualizado = pacienteDAO.actualizar(paciente);
            // TODO: Actualizar en el índice BST cuando se implemente el método
            // if (actualizado) {
            //     indicePacientes.actualizar(paciente);
            // }
            return actualizado;
        } catch (SQLException e) {
            System.err.println("Error al actualizar paciente: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cambia el estado de un paciente
     * @param tokenSesion Token de sesión
     * @param pacienteId ID del paciente
     * @param nuevoEstado Nuevo estado
     * @return true si se cambió correctamente
     */
    public boolean cambiarEstadoPaciente(String tokenSesion, int pacienteId, EstadoPaciente nuevoEstado) {
        if (!BaseController.getAuthService().tienePermiso(tokenSesion, AuthenticationService.Permiso.ACTUALIZAR_PACIENTES)) {
            return false;
        }
        
        try {
            // TipoAlta por defecto null para cambios de estado genéricos
            return pacienteDAO.actualizarEstado(pacienteId, nuevoEstado, null);
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado del paciente: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene pacientes por estado específico
     * @param tokenSesion Token de sesión
     * @param estado Estado a buscar
     * @return Lista de pacientes en ese estado
     */
    public List<Paciente> obtenerPacientesPorEstado(String tokenSesion, EstadoPaciente estado) {
        if (!BaseController.getAuthService().tienePermiso(tokenSesion, AuthenticationService.Permiso.VER_PACIENTES)) {
            return new ArrayList<>();
        }
        
        try {
            return pacienteDAO.buscarPorEstado(estado);
        } catch (SQLException e) {
            System.err.println("Error al obtener pacientes por estado: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene estadísticas generales de pacientes
     * @param tokenSesion Token de sesión
     * @return Estadísticas de pacientes
     */
    public EstadisticasPacientes obtenerEstadisticas(String tokenSesion) {
        if (!BaseController.getAuthService().tienePermiso(tokenSesion, AuthenticationService.Permiso.VER_REPORTES_MEDICOS)) {
            return null;
        }
        
        try {
            List<PacienteDAO.ConteoEstado> conteosPorEstado = pacienteDAO.contarPorEstado();
            List<PacienteDAO.ConteoGenero> conteosPorGenero = pacienteDAO.contarPorGenero();
            List<PacienteDAO.ConteoEdad> conteosPorEdad = pacienteDAO.contarPorRangoEdad();
            
            return new EstadisticasPacientes(conteosPorEstado, conteosPorGenero, conteosPorEdad);
            
        } catch (SQLException e) {
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Busca pacientes que requieren seguimiento
     * @param tokenSesion Token de sesión
     * @return Lista de pacientes para seguimiento
     */
    public List<Paciente> obtenerPacientesParaSeguimiento(String tokenSesion) {
        if (!BaseController.getAuthService().tienePermiso(tokenSesion, AuthenticationService.Permiso.VER_PACIENTES)) {
            return new ArrayList<>();
        }
        
        try {
            // Pacientes con citas próximas, hospitalizaciones, etc.
            return pacienteDAO.buscarQueRequierenSeguimiento();
        } catch (SQLException e) {
            System.err.println("Error al obtener pacientes para seguimiento: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene el historial médico resumido de un paciente
     * @param tokenSesion Token de sesión
     * @param pacienteId ID del paciente
     * @return Resumen del historial médico
     */
    public ResumenHistorialMedico obtenerResumenHistorial(String tokenSesion, int pacienteId) {
        if (!BaseController.getAuthService().tienePermiso(tokenSesion, AuthenticationService.Permiso.VER_PACIENTES)) {
            return null;
        }
        
        try {
            Paciente paciente = pacienteDAO.buscarPorId(pacienteId);
            if (paciente == null) {
                return null;
            }
            
            // Última atención médica
            AtencionMedica ultimaAtencion = atencionMedicaDAO.obtenerUltimaPorPaciente(pacienteId);
            
            // Último triage
            RegistroTriage ultimoTriage = triageDAO.obtenerUltimoPorPaciente(pacienteId);
            
            // Datos sociales actuales
            DatosSociales datosSociales = datosSocialesDAO.obtenerUltimoPorPaciente(pacienteId);
            
            // Conteo de atenciones
            int totalAtenciones = atencionMedicaDAO.obtenerPorPaciente(pacienteId).size();
            int totalTriages = triageDAO.obtenerPorPaciente(pacienteId).size();
            
            return new ResumenHistorialMedico(paciente, ultimaAtencion, ultimoTriage, 
                                            datosSociales, totalAtenciones, totalTriages);
            
        } catch (SQLException e) {
            System.err.println("Error al obtener resumen del historial: " + e.getMessage());
            return null;
        }
    }
    
    // Métodos privados auxiliares
    
    /**
     * Valida los datos de registro de un paciente
     */
    private String validarDatosRegistro(DatosRegistroPaciente datos) {
        if (datos == null) {
            return "Datos de registro son obligatorios";
        }
        
        if (!ValidationUtils.validarTexto(datos.getNombreCompleto(), 2, 100)) {
            return "Nombre completo debe tener entre 2 y 100 caracteres";
        }
        
        if (datos.getFechaNacimiento() == null) {
            return "Fecha de nacimiento es obligatoria";
        }
        
        if (datos.getFechaNacimiento().isAfter(LocalDate.now())) {
            return "Fecha de nacimiento no puede ser futura";
        }
        
        if (datos.getCurp() != null && !datos.getCurp().isEmpty()) {
            if (!ValidationUtils.validarCURPBoolean(datos.getCurp())) {
                return "CURP no es válido";
            }
        }
        
        if (datos.getRfc() != null && !datos.getRfc().isEmpty()) {
            if (!ValidationUtils.validarRFCBoolean(datos.getRfc())) {
                return "RFC no es válido";
            }
        }
        
        if (datos.getTelefono() != null && !datos.getTelefono().isEmpty()) {
            if (!ValidationUtils.validarTelefonoBoolean(datos.getTelefono())) {
                return "Teléfono no es válido";
            }
        }
        
        if (datos.getEmail() != null && !datos.getEmail().isEmpty()) {
            if (!ValidationUtils.validarEmailBoolean(datos.getEmail())) {
                return "Email no es válido";
            }
        }
        
        if (!ValidationUtils.validarTexto(datos.getContactoEmergenciaNombre(), 2, 100)) {
            return "Nombre de contacto de emergencia es obligatorio";
        }
        
        if (!ValidationUtils.validarTelefonoBoolean(datos.getContactoEmergenciaTelefono())) {
            return "Teléfono de contacto de emergencia no es válido";
        }
        
        return null; // Datos válidos
    }
    
    /**
     * Carga el índice BST con los pacientes existentes
     */
    private void cargarIndicePacientes() {
        try {
            List<Paciente> pacientes = pacienteDAO.obtenerTodos();
            for (Paciente paciente : pacientes) {
                indicePacientes.insertar(paciente);
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar índice de pacientes: " + e.getMessage());
        }
    }
    
    // Clases de datos
    
    /**
     * Datos necesarios para registrar un nuevo paciente
     */
    public static class DatosRegistroPaciente {
        private String nombreCompleto;
        private LocalDate fechaNacimiento;
        private String genero;
        private String curp;
        private String rfc;
        private String telefono;
        private String email;
        private String direccionCompleta;
        private String contactoEmergenciaNombre;
        private String contactoEmergenciaTelefono;
        private String contactoEmergenciaRelacion;
        private String tipoSangre;
        private String alergias;
        private String enfermedadesCronicas;
        private String medicamentosActuales;
        private String observacionesMedicas;
        
        // Getters y setters
        public String getNombreCompleto() { return nombreCompleto; }
        public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
        
        public LocalDate getFechaNacimiento() { return fechaNacimiento; }
        public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
        
        public String getGenero() { return genero; }
        public void setGenero(String genero) { this.genero = genero; }
        
        public String getCurp() { return curp; }
        public void setCurp(String curp) { this.curp = curp; }
        
        public String getRfc() { return rfc; }
        public void setRfc(String rfc) { this.rfc = rfc; }
        
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getDireccionCompleta() { return direccionCompleta; }
        public void setDireccionCompleta(String direccionCompleta) { this.direccionCompleta = direccionCompleta; }
        
        public String getContactoEmergenciaNombre() { return contactoEmergenciaNombre; }
        public void setContactoEmergenciaNombre(String contactoEmergenciaNombre) { this.contactoEmergenciaNombre = contactoEmergenciaNombre; }
        
        public String getContactoEmergenciaTelefono() { return contactoEmergenciaTelefono; }
        public void setContactoEmergenciaTelefono(String contactoEmergenciaTelefono) { this.contactoEmergenciaTelefono = contactoEmergenciaTelefono; }
        
        public String getContactoEmergenciaRelacion() { return contactoEmergenciaRelacion; }
        public void setContactoEmergenciaRelacion(String contactoEmergenciaRelacion) { this.contactoEmergenciaRelacion = contactoEmergenciaRelacion; }
        
        public String getTipoSangre() { return tipoSangre; }
        public void setTipoSangre(String tipoSangre) { this.tipoSangre = tipoSangre; }
        
        public String getAlergias() { return alergias; }
        public void setAlergias(String alergias) { this.alergias = alergias; }
        
        public String getEnfermedadesCronicas() { return enfermedadesCronicas; }
        public void setEnfermedadesCronicas(String enfermedadesCronicas) { this.enfermedadesCronicas = enfermedadesCronicas; }
        
        public String getMedicamentosActuales() { return medicamentosActuales; }
        public void setMedicamentosActuales(String medicamentosActuales) { this.medicamentosActuales = medicamentosActuales; }
        
        public String getObservacionesMedicas() { return observacionesMedicas; }
        public void setObservacionesMedicas(String observacionesMedicas) { this.observacionesMedicas = observacionesMedicas; }
    }
    
    /**
     * Criterios para búsqueda de pacientes
     */
    public static class CriteriosBusquedaPaciente {
        private String numeroExpediente;
        private String nombre;
        private String curp;
        private LocalDate fechaNacimiento;
        private EstadoPaciente estado;
        
        // Getters y setters
        public String getNumeroExpediente() { return numeroExpediente; }
        public void setNumeroExpediente(String numeroExpediente) { this.numeroExpediente = numeroExpediente; }
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public String getCurp() { return curp; }
        public void setCurp(String curp) { this.curp = curp; }
        
        public LocalDate getFechaNacimiento() { return fechaNacimiento; }
        public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
        
        public EstadoPaciente getEstado() { return estado; }
        public void setEstado(EstadoPaciente estado) { this.estado = estado; }
    }
    
    /**
     * Resultado del registro de un paciente
     */
    public static class ResultadoRegistroPaciente {
        private final boolean exitoso;
        private final String mensaje;
        private final Paciente paciente;
        
        public ResultadoRegistroPaciente(boolean exitoso, String mensaje, Paciente paciente) {
            this.exitoso = exitoso;
            this.mensaje = mensaje;
            this.paciente = paciente;
        }
        
        public boolean isExitoso() { return exitoso; }
        public String getMensaje() { return mensaje; }
        public Paciente getPaciente() { return paciente; }
    }
    
    /**
     * Información completa de un paciente
     */
    public static class InformacionCompletaPaciente {
        private final Paciente paciente;
        private final List<RegistroTriage> historialTriage;
        private final List<DatosSociales> datosSociales;
        private final List<AtencionMedica> atencionesMedicas;
        
        public InformacionCompletaPaciente(Paciente paciente, List<RegistroTriage> historialTriage,
                                          List<DatosSociales> datosSociales, List<AtencionMedica> atencionesMedicas) {
            this.paciente = paciente;
            this.historialTriage = historialTriage;
            this.datosSociales = datosSociales;
            this.atencionesMedicas = atencionesMedicas;
        }
        
        public Paciente getPaciente() { return paciente; }
        public List<RegistroTriage> getHistorialTriage() { return historialTriage; }
        public List<DatosSociales> getDatosSociales() { return datosSociales; }
        public List<AtencionMedica> getAtencionesMedicas() { return atencionesMedicas; }
    }
    
    /**
     * Estadísticas generales de pacientes
     */
    public static class EstadisticasPacientes {
        private final List<PacienteDAO.ConteoEstado> conteosPorEstado;
        private final List<PacienteDAO.ConteoGenero> conteosPorGenero;
        private final List<PacienteDAO.ConteoEdad> conteosPorEdad;
        
        public EstadisticasPacientes(List<PacienteDAO.ConteoEstado> conteosPorEstado,
                                   List<PacienteDAO.ConteoGenero> conteosPorGenero,
                                   List<PacienteDAO.ConteoEdad> conteosPorEdad) {
            this.conteosPorEstado = conteosPorEstado;
            this.conteosPorGenero = conteosPorGenero;
            this.conteosPorEdad = conteosPorEdad;
        }
        
        public List<PacienteDAO.ConteoEstado> getConteosPorEstado() { return conteosPorEstado; }
        public List<PacienteDAO.ConteoGenero> getConteosPorGenero() { return conteosPorGenero; }
        public List<PacienteDAO.ConteoEdad> getConteosPorEdad() { return conteosPorEdad; }
    }
    
    /**
     * Resumen del historial médico de un paciente
     */
    public static class ResumenHistorialMedico {
        private final Paciente paciente;
        private final AtencionMedica ultimaAtencion;
        private final RegistroTriage ultimoTriage;
        private final DatosSociales datosSociales;
        private final int totalAtenciones;
        private final int totalTriages;
        
        public ResumenHistorialMedico(Paciente paciente, AtencionMedica ultimaAtencion,
                                    RegistroTriage ultimoTriage, DatosSociales datosSociales,
                                    int totalAtenciones, int totalTriages) {
            this.paciente = paciente;
            this.ultimaAtencion = ultimaAtencion;
            this.ultimoTriage = ultimoTriage;
            this.datosSociales = datosSociales;
            this.totalAtenciones = totalAtenciones;
            this.totalTriages = totalTriages;
        }
        
        public Paciente getPaciente() { return paciente; }
        public AtencionMedica getUltimaAtencion() { return ultimaAtencion; }
        public RegistroTriage getUltimoTriage() { return ultimoTriage; }
        public DatosSociales getDatosSociales() { return datosSociales; }
        public int getTotalAtenciones() { return totalAtenciones; }
        public int getTotalTriages() { return totalTriages; }
    }
    
    // Métodos adicionales requeridos por los controladores
    
    /**
     * Busca pacientes y devuelve resultado encapsulado
     */
    public ResultadoBusqueda buscarPacientes(String tokenSesion, services.PacienteServiceResults.CriteriosBusquedaPaciente criterios) {
        try {
            // Validar sesión
            if (!BaseController.getAuthService().validarSesion(tokenSesion)) {
                return new ResultadoBusqueda(false, "Sesión inválida", new ArrayList<>(), 0);
            }
            
            // Crear criterios internos
            CriteriosBusquedaPaciente criteriosInternos = new CriteriosBusquedaPaciente();
            List<Paciente> resultados = buscarPacientesSegunCriterios(criterios);
            return new ResultadoBusqueda(true, "Búsqueda completada", resultados, resultados.size());
            
        } catch (Exception e) {
            return new ResultadoBusqueda(false, "Error en la búsqueda: " + e.getMessage(), new ArrayList<>(), 0);
        }
    }
    
    /**
     * Busca pacientes usando criterio de texto simple
     */
    public ResultadoBusqueda buscarPacientes(String tokenSesion, String criterio) {
        services.PacienteServiceResults.CriteriosBusquedaPaciente criterios = new services.PacienteServiceResults.CriteriosBusquedaPaciente();
        criterios.setNombreCompleto(criterio);
        criterios.setNumeroExpediente(criterio);
        criterios.setCurp(criterio);
        return buscarPacientes(tokenSesion, criterios);
    }
    
    /**
     * Método auxiliar para buscar según criterios externos
     */
    private List<Paciente> buscarPacientesSegunCriterios(services.PacienteServiceResults.CriteriosBusquedaPaciente criterios) {
        List<Paciente> resultados = new ArrayList<>();
        
        try {
            // Buscar por número de expediente
            if (criterios.getNumeroExpediente() != null && !criterios.getNumeroExpediente().isEmpty()) {
                Paciente p = pacienteDAO.buscarPorNumeroExpediente(criterios.getNumeroExpediente());
                if (p != null) resultados.add(p);
            }
            
            // Buscar por CURP
            if (criterios.getCurp() != null && !criterios.getCurp().isEmpty()) {
                Paciente p = pacienteDAO.obtenerPorCURP(criterios.getCurp());
                if (p != null && !resultados.contains(p)) resultados.add(p);
            }
            
            // Buscar por nombre
            if (criterios.getNombreCompleto() != null && !criterios.getNombreCompleto().isEmpty()) {
                List<Paciente> porNombre = pacienteDAO.buscarPorNombre(criterios.getNombreCompleto());
                for (Paciente p : porNombre) {
                    if (!resultados.contains(p)) resultados.add(p);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error en búsqueda: " + e.getMessage());
        }
        
        return resultados;
    }
    
    /**
     * Obtiene pacientes recientes
     */
    public ResultadoBusqueda obtenerPacientesRecientes(String tokenSesion, int limite) {
        try {
            // Validar sesión
            if (!BaseController.getAuthService().validarSesion(tokenSesion)) {
                return new ResultadoBusqueda(false, "Sesión inválida", new ArrayList<>(), 0);
            }
            
            List<Paciente> pacientes = pacienteDAO.obtenerTodos();
            // Limitar resultados
            if (pacientes.size() > limite) {
                pacientes = pacientes.subList(0, limite);
            }
            
            return new ResultadoBusqueda(true, "Pacientes obtenidos", pacientes, pacientes.size());
            
        } catch (Exception e) {
            return new ResultadoBusqueda(false, "Error al obtener pacientes: " + e.getMessage(), new ArrayList<>(), 0);
        }
    }
    
    /**
     * Registra un nuevo paciente (versión para controladores)
     */
    public ResultadoRegistro registrarPaciente(String tokenSesion, services.PacienteServiceResults.DatosRegistroPaciente datos) {
        try {
            // Validar sesión
            if (!BaseController.getAuthService().validarSesion(tokenSesion)) {
                return new ResultadoRegistro(false, "Sesión inválida", null, 0);
            }
            
            // Crear paciente desde datos externos
            Paciente paciente = crearPacienteDesdeDTO(datos);
            
            // Registrar paciente
            int id = pacienteDAO.crear(paciente);
            paciente.setId(id);
            
            return new ResultadoRegistro(true, "Paciente registrado exitosamente", paciente, id);
            
        } catch (Exception e) {
            return new ResultadoRegistro(false, "Error al registrar paciente: " + e.getMessage(), null, 0);
        }
    }
    
    /**
     * Actualiza un paciente con nuevos datos (versión para controladores)
     */
    public boolean actualizarPaciente(String tokenSesion, int pacienteId, services.PacienteServiceResults.DatosRegistroPaciente datos) {
        try {
            // Validar sesión
            if (!BaseController.getAuthService().validarSesion(tokenSesion)) {
                return false;
            }
            
            // Obtener paciente actual
            Paciente paciente = pacienteDAO.obtenerPorId(pacienteId);
            if (paciente == null) {
                return false;
            }
            
            // Actualizar campos desde datos externos
            actualizarPacienteDesdeDTO(paciente, datos);
            
            // Actualizar en base de datos
            return pacienteDAO.actualizar(paciente);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Convierte DatosRegistroPaciente externo a Paciente
     */
    private Paciente crearPacienteDesdeDTO(services.PacienteServiceResults.DatosRegistroPaciente datos) {
        Paciente paciente = new Paciente();
        actualizarPacienteDesdeDTO(paciente, datos);
        return paciente;
    }
    
    /**
     * Actualiza un paciente desde un DTO
     */
    private void actualizarPacienteDesdeDTO(Paciente paciente, services.PacienteServiceResults.DatosRegistroPaciente datos) {
        paciente.setNombre(datos.getNombre());
        paciente.setApellidoPaterno(datos.getApellidoPaterno());
        paciente.setApellidoMaterno(datos.getApellidoMaterno());
        paciente.setFechaNacimiento(datos.getFechaNacimiento());
        paciente.setSexo(datos.getSexo());
        paciente.setCurp(datos.getCurp());
        paciente.setRfc(datos.getRfc());
        paciente.setTelefonoPrincipal(datos.getTelefono());
        paciente.setEmail(datos.getEmail());
        paciente.setDireccionCalle(datos.getDireccionCalle());
        paciente.setDireccionNumero(datos.getDireccionNumero());
        paciente.setDireccionColonia(datos.getDireccionColonia());
        paciente.setDireccionCiudad(datos.getDireccionCiudad());
        paciente.setDireccionEstado(datos.getDireccionEstado());
        paciente.setCodigoPostal(datos.getCodigoPostal());
        paciente.setSeguroMedico(datos.getSeguroMedico());
        paciente.setNumeroSeguro(datos.getNumeroSeguro());
        paciente.setMedicamentos(datos.getMedicamentos());
        paciente.setEnfermedadesPrevias(datos.getEnfermedadesPrevias());
        paciente.setContactoEmergenciaNombre(datos.getContactoEmergenciaNombre());
        paciente.setContactoEmergenciaTelefono(datos.getContactoEmergenciaTelefono());
        paciente.setContactoEmergenciaParentesco(datos.getContactoEmergenciaParentesco());
    }
}