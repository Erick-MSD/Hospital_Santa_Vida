package models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo de datos para atención médica final
 * Representa el diagnóstico y tratamiento dado por el médico de urgencias
 */
public class AtencionMedica {
    
    // Enumeración para tipos de alta
    public enum TipoAlta {
        DOMICILIO("Alta a domicilio"),
        HOSPITALIZACION("Hospitalización"),
        REFERENCIA("Referencia a otro hospital"),
        DEFUNCION("Defunción");
        
        private final String descripcion;
        
        TipoAlta(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() { return descripcion; }
    }
    
    // Atributos principales
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
    private Integer tiempoTotalAtencion; // minutos
    private boolean seguimientoRequerido;
    private LocalDate fechaSeguimiento;
    private String observacionesMedicas;
    
    // Referencias a otros objetos
    private RegistroTriage registroTriage;
    private Usuario medicoUrgencias;
    
    // Constructores
    public AtencionMedica() {
        this.fechaHoraInicio = LocalDateTime.now();
        this.seguimientoRequerido = false;
    }
    
    public AtencionMedica(int registroTriageId, int medicoUrgenciasId, 
                         String diagnosticoPrincipal, String tratamientoAplicado) {
        this();
        this.registroTriageId = registroTriageId;
        this.medicoUrgenciasId = medicoUrgenciasId;
        this.diagnosticoPrincipal = diagnosticoPrincipal;
        this.tratamientoAplicado = tratamientoAplicado;
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
    public boolean estaCompleta() {
        return diagnosticoPrincipal != null && !diagnosticoPrincipal.trim().isEmpty() &&
               tratamientoAplicado != null && !tratamientoAplicado.trim().isEmpty() &&
               tipoAlta != null &&
               fechaHoraFin != null;
    }
    
    public boolean requiereHospitalizacion() {
        return tipoAlta == TipoAlta.HOSPITALIZACION;
    }
    
    public boolean esReferencia() {
        return tipoAlta == TipoAlta.REFERENCIA;
    }
    
    public boolean esAltaDomicilio() {
        return tipoAlta == TipoAlta.DOMICILIO;
    }
    
    public boolean esDefuncion() {
        return tipoAlta == TipoAlta.DEFUNCION;
    }
    
    public void completarAtencion() {
        if (fechaHoraFin == null) {
            setFechaHoraFin(LocalDateTime.now());
            calcularTiempoTotal();
        }
    }
    
    public void calcularTiempoTotal() {
        if (registroTriage != null && registroTriage.getFechaHoraLlegada() != null && fechaHoraFin != null) {
            long minutos = java.time.Duration.between(registroTriage.getFechaHoraLlegada(), fechaHoraFin).toMinutes();
            setTiempoTotalAtencion((int) minutos);
        }
    }
    
    public long getDuracionConsulta() {
        if (fechaHoraInicio != null && fechaHoraFin != null) {
            return java.time.Duration.between(fechaHoraInicio, fechaHoraFin).toMinutes();
        }
        return 0;
    }
    
    public boolean enProceso() {
        return fechaHoraInicio != null && fechaHoraFin == null;
    }
    
    public boolean finalizada() {
        return fechaHoraFin != null;
    }
    
    public boolean prescribeMedicamentos() {
        return medicamentosPrescritos != null && !medicamentosPrescritos.trim().isEmpty() &&
               !medicamentosPrescritos.toLowerCase().contains("ninguno") &&
               !medicamentosPrescritos.toLowerCase().contains("no se prescriben");
    }
    
    public boolean tieneDiagnosticosSecundarios() {
        return diagnosticosSecundarios != null && !diagnosticosSecundarios.trim().isEmpty();
    }
    
    public String getResumenDiagnostico() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("Principal: ").append(diagnosticoPrincipal);
        
        if (tieneDiagnosticosSecundarios()) {
            resumen.append(" | Secundarios: ").append(diagnosticosSecundarios);
        }
        
        return resumen.toString();
    }
    
    public String getResumenTratamiento() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("Tratamiento: ").append(tratamientoAplicado);
        
        if (prescribeMedicamentos()) {
            resumen.append(" | Medicamentos: ").append(medicamentosPrescritos);
        }
        
        if (tipoAlta != null) {
            resumen.append(" | Alta: ").append(tipoAlta.getDescripcion());
        }
        
        return resumen.toString();
    }
    
    public void validarReferencia() throws IllegalStateException {
        if (esReferencia() && (hospitalReferencia == null || hospitalReferencia.trim().isEmpty())) {
            throw new IllegalStateException("Se debe especificar el hospital de referencia");
        }
    }
    
    public void validarSeguimiento() throws IllegalStateException {
        if (seguimientoRequerido && fechaSeguimiento == null) {
            throw new IllegalStateException("Se debe especificar la fecha de seguimiento");
        }
    }
    
    @Override
    public String toString() {
        return "AtencionMedica{" +
                "id=" + id +
                ", diagnosticoPrincipal='" + diagnosticoPrincipal + '\'' +
                ", tipoAlta=" + tipoAlta +
                ", duracionConsulta=" + getDuracionConsulta() + " min" +
                ", tiempoTotal=" + tiempoTotalAtencion + " min" +
                ", finalizada=" + finalizada() +
                ", seguimientoRequerido=" + seguimientoRequerido +
                '}';
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
}