package models;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * Clase modelo que representa la atención médica final de un paciente
 * Mapea directamente con la tabla 'atencion_medica' de la base de datos
 */
public class AtencionMedica {
    private int id;
    private int registroTriageId;
    private int medicoUrgenciasId;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private String diagnosticoPrincipal;
    private String diagnosticosSecundarios;
    private String tratamientoAplicado;
    private String medicamentosPrescritos;
    private String instruccionesAlta;
    private TipoAlta tipoAlta;
    private String hospitalReferencia;
    private Integer tiempoTotalAtencion; // en minutos
    private boolean seguimientoRequerido;
    private LocalDate fechaSeguimiento;
    private String observacionesMedicas;
    
    // Referencias a objetos relacionados
    private RegistroTriage registroTriage;
    private Usuario medicoUrgencias;
    
    // Campos adicionales para compatibilidad con DAO
    private int pacienteId;
    private int medicoId;
    private LocalDateTime fechaConsulta;
    private Especialidad especialidadMedica;
    private String motivoConsulta;
    private String exploracionFisica;
    private String diagnostico;
    private String tratamientoPrescrito;
    private LocalDateTime proximaCita;
    private boolean requiereHospitalizacion;
    private boolean requiereCirugia;
    private boolean requiereInterconsulta;
    private Especialidad especialidadInterconsulta;
    private String pacienteNombre;
    private String numeroExpediente;
    private String medicoNombre;
    
    // Constructor vacío
    public AtencionMedica() {
        this.fechaHoraInicio = LocalDateTime.now();
        this.seguimientoRequerido = false;
    }
    
    // Constructor básico
    public AtencionMedica(int registroTriageId, int medicoUrgenciasId, String diagnosticoPrincipal, TipoAlta tipoAlta) {
        this();
        this.registroTriageId = registroTriageId;
        this.medicoUrgenciasId = medicoUrgenciasId;
        this.diagnosticoPrincipal = diagnosticoPrincipal;
        this.tipoAlta = tipoAlta;
    }
    
    // Getters y Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getRegistroTriageId() {
        return registroTriageId;
    }
    
    public void setRegistroTriageId(int registroTriageId) {
        this.registroTriageId = registroTriageId;
    }
    
    public int getMedicoUrgenciasId() {
        return medicoUrgenciasId;
    }
    
    public void setMedicoUrgenciasId(int medicoUrgenciasId) {
        this.medicoUrgenciasId = medicoUrgenciasId;
    }
    
    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }
    
    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }
    
    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }
    
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
        if (fechaHoraInicio != null && fechaHoraFin != null) {
            this.tiempoTotalAtencion = (int) java.time.Duration.between(fechaHoraInicio, fechaHoraFin).toMinutes();
        }
    }
    
    public String getDiagnosticoPrincipal() {
        return diagnosticoPrincipal;
    }
    
    public void setDiagnosticoPrincipal(String diagnosticoPrincipal) {
        this.diagnosticoPrincipal = diagnosticoPrincipal;
    }
    
    public String getDiagnosticosSecundarios() {
        return diagnosticosSecundarios;
    }
    
    public void setDiagnosticosSecundarios(String diagnosticosSecundarios) {
        this.diagnosticosSecundarios = diagnosticosSecundarios;
    }
    
    public String getTratamientoAplicado() {
        return tratamientoAplicado;
    }
    
    public void setTratamientoAplicado(String tratamientoAplicado) {
        this.tratamientoAplicado = tratamientoAplicado;
    }
    
    public String getMedicamentosPrescritos() {
        return medicamentosPrescritos;
    }
    
    public void setMedicamentosPrescritos(String medicamentosPrescritos) {
        this.medicamentosPrescritos = medicamentosPrescritos;
    }
    
    public String getInstruccionesAlta() {
        return instruccionesAlta;
    }
    
    public void setInstruccionesAlta(String instruccionesAlta) {
        this.instruccionesAlta = instruccionesAlta;
    }
    
    public TipoAlta getTipoAlta() {
        return tipoAlta;
    }
    
    public void setTipoAlta(TipoAlta tipoAlta) {
        this.tipoAlta = tipoAlta;
    }
    
    public String getHospitalReferencia() {
        return hospitalReferencia;
    }
    
    public void setHospitalReferencia(String hospitalReferencia) {
        this.hospitalReferencia = hospitalReferencia;
    }
    
    public Integer getTiempoTotalAtencion() {
        return tiempoTotalAtencion;
    }
    
    public void setTiempoTotalAtencion(Integer tiempoTotalAtencion) {
        this.tiempoTotalAtencion = tiempoTotalAtencion;
    }
    
    public boolean isSeguimientoRequerido() {
        return seguimientoRequerido;
    }
    
    public void setSeguimientoRequerido(boolean seguimientoRequerido) {
        this.seguimientoRequerido = seguimientoRequerido;
    }
    
    public LocalDate getFechaSeguimiento() {
        return fechaSeguimiento;
    }
    
    public void setFechaSeguimiento(LocalDate fechaSeguimiento) {
        this.fechaSeguimiento = fechaSeguimiento;
    }
    
    public String getObservacionesMedicas() {
        return observacionesMedicas;
    }
    
    public void setObservacionesMedicas(String observacionesMedicas) {
        this.observacionesMedicas = observacionesMedicas;
    }
    
    public RegistroTriage getRegistroTriage() {
        return registroTriage;
    }
    
    public void setRegistroTriage(RegistroTriage registroTriage) {
        this.registroTriage = registroTriage;
    }
    
    public Usuario getMedicoUrgencias() {
        return medicoUrgencias;
    }
    
    public void setMedicoUrgencias(Usuario medicoUrgencias) {
        this.medicoUrgencias = medicoUrgencias;
    }
    
    // Métodos de utilidad
    public boolean esAtencionCompletada() {
        return fechaHoraFin != null && diagnosticoPrincipal != null && 
               !diagnosticoPrincipal.trim().isEmpty() && tipoAlta != null;
    }
    
    public String getDuracionAtencionFormateada() {
        if (tiempoTotalAtencion == null) return "No calculado";
        
        int horas = tiempoTotalAtencion / 60;
        int minutos = tiempoTotalAtencion % 60;
        
        if (horas > 0) {
            return horas + "h " + minutos + "min";
        } else {
            return minutos + " minutos";
        }
    }
    
    public boolean tieneMedicamentosPrescritos() {
        return medicamentosPrescritos != null && !medicamentosPrescritos.trim().isEmpty() &&
               !medicamentosPrescritos.toLowerCase().contains("ninguno");
    }
    
    public boolean requiereReferencia() {
        return tipoAlta == TipoAlta.REFERENCIA;
    }
    
    public boolean requiereHospitalizacion() {
        return tipoAlta == TipoAlta.HOSPITALIZACION;
    }
    
    public boolean esAltaDomicilio() {
        return tipoAlta == TipoAlta.DOMICILIO;
    }
    
    public boolean tieneDiagnosticosSecundarios() {
        return diagnosticosSecundarios != null && !diagnosticosSecundarios.trim().isEmpty();
    }
    
    public void finalizarAtencion() {
        if (fechaHoraFin == null) {
            setFechaHoraFin(LocalDateTime.now());
        }
    }
    
    @Override
    public String toString() {
        return "Atención Médica - " + diagnosticoPrincipal + 
               " [" + (tipoAlta != null ? tipoAlta.getNombre() : "Sin alta") + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AtencionMedica atencion = (AtencionMedica) obj;
        return id == atencion.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
    
    // Getters y setters adicionales para compatibilidad con DAO
    public int getPacienteId() {
        return pacienteId;
    }
    
    public void setPacienteId(int pacienteId) {
        this.pacienteId = pacienteId;
    }
    
    public int getMedicoId() {
        return medicoId;
    }
    
    public void setMedicoId(int medicoId) {
        this.medicoId = medicoId;
    }
    
    public LocalDateTime getFechaConsulta() {
        return fechaConsulta;
    }
    
    public void setFechaConsulta(LocalDateTime fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }
    
    public Especialidad getEspecialidadMedica() {
        return especialidadMedica;
    }
    
    public void setEspecialidadMedica(Especialidad especialidadMedica) {
        this.especialidadMedica = especialidadMedica;
    }
    
    public String getMotivoConsulta() {
        return motivoConsulta;
    }
    
    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }
    
    public String getExploracionFisica() {
        return exploracionFisica;
    }
    
    public void setExploracionFisica(String exploracionFisica) {
        this.exploracionFisica = exploracionFisica;
    }
    
    public String getDiagnostico() {
        return diagnostico;
    }
    
    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }
    
    public String getTratamientoPrescrito() {
        return tratamientoPrescrito;
    }
    
    public void setTratamientoPrescrito(String tratamientoPrescrito) {
        this.tratamientoPrescrito = tratamientoPrescrito;
    }
    
    public LocalDateTime getProximaCita() {
        return proximaCita;
    }
    
    public void setProximaCita(LocalDateTime proximaCita) {
        this.proximaCita = proximaCita;
    }
    
    public boolean isRequiereHospitalizacion() {
        return requiereHospitalizacion;
    }
    
    public void setRequiereHospitalizacion(boolean requiereHospitalizacion) {
        this.requiereHospitalizacion = requiereHospitalizacion;
    }
    
    public boolean isRequiereCirugia() {
        return requiereCirugia;
    }
    
    public void setRequiereCirugia(boolean requiereCirugia) {
        this.requiereCirugia = requiereCirugia;
    }
    
    public boolean isRequiereInterconsulta() {
        return requiereInterconsulta;
    }
    
    public void setRequiereInterconsulta(boolean requiereInterconsulta) {
        this.requiereInterconsulta = requiereInterconsulta;
    }
    
    public Especialidad getEspecialidadInterconsulta() {
        return especialidadInterconsulta;
    }
    
    public void setEspecialidadInterconsulta(Especialidad especialidadInterconsulta) {
        this.especialidadInterconsulta = especialidadInterconsulta;
    }
    
    public String getPacienteNombre() {
        return pacienteNombre;
    }
    
    public void setPacienteNombre(String pacienteNombre) {
        this.pacienteNombre = pacienteNombre;
    }
    
    public String getNumeroExpediente() {
        return numeroExpediente;
    }
    
    public void setNumeroExpediente(String numeroExpediente) {
        this.numeroExpediente = numeroExpediente;
    }
    
    public String getMedicoNombre() {
        return medicoNombre;
    }
    
    public void setMedicoNombre(String medicoNombre) {
        this.medicoNombre = medicoNombre;
    }
    
    // Métodos adicionales requeridos por los controladores
    
    public void setFechaAtencion(LocalDate fechaAtencion) {
        this.fechaConsulta = fechaAtencion.atStartOfDay();
    }
    
    public void setHoraAtencion(LocalDateTime horaAtencion) {
        this.fechaConsulta = horaAtencion;
    }
    
    public void setHistoriaEnfermedadActual(String historia) {
        this.motivoConsulta = historia;
    }
    
    public void setSignosVitales(String signosVitales) {
        this.exploracionFisica = signosVitales;
    }
    
    public void setDiagnosticoSecundario(String diagnosticoSecundario) {
        this.diagnosticosSecundarios = diagnosticoSecundario;
    }
    
    public void setTipoDiagnostico(String tipoDiagnostico) {
        // Almacenar como parte del diagnóstico principal
        if (this.diagnosticoPrincipal == null) {
            this.diagnosticoPrincipal = "";
        }
        this.diagnosticoPrincipal = tipoDiagnostico + ": " + this.diagnosticoPrincipal;
    }
    
    public void setPrescripcionMedica(String prescripcionMedica) {
        this.medicamentosPrescritos = prescripcionMedica;
    }
    
    public void setIndicacionesMedicas(String indicacionesMedicas) {
        this.instruccionesAlta = indicacionesMedicas;
    }
    
    public void setRecomendaciones(String recomendaciones) {
        this.observacionesMedicas = recomendaciones;
    }
    
    public void setEstadoPaciente(String estadoPaciente) {
        // Convertir string a TipoAlta si es posible
        try {
            this.tipoAlta = TipoAlta.valueOf(estadoPaciente.toUpperCase());
        } catch (Exception e) {
            // Si no se puede convertir, usar DOMICILIO por defecto
            this.tipoAlta = TipoAlta.DOMICILIO;
        }
    }
    
    public void setObservaciones(String observaciones) {
        this.observacionesMedicas = observaciones;
    }
    
    public void setObservacionesAlta(String observacionesAlta) {
        this.observacionesMedicas = observacionesAlta;
    }
    
    public void setFechaAlta(LocalDate fechaAlta) {
        this.fechaHoraFin = fechaAlta.atTime(LocalDateTime.now().toLocalTime());
    }
}