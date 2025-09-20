package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo de datos para registros de triage
 * Cada visita a urgencias genera un nuevo registro
 */
public class RegistroTriage {
    
    // Enumeraciones
    public enum NivelUrgencia {
        ROJO("Crítico", "Resucitación inmediata", 1),
        NARANJA("Muy urgente", "Atención en 10 minutos", 2),
        AMARILLO("Urgente", "Atención en 60 minutos", 3),
        VERDE("Menos urgente", "Atención en 120 minutos", 4),
        AZUL("No urgente", "Cita médica ambulatoria", 5);
        
        private final String descripcion;
        private final String tiempoEsperado;
        private final int prioridad;
        
        NivelUrgencia(String descripcion, String tiempoEsperado, int prioridad) {
            this.descripcion = descripcion;
            this.tiempoEsperado = tiempoEsperado;
            this.prioridad = prioridad;
        }
        
        public String getDescripcion() { return descripcion; }
        public String getTiempoEsperado() { return tiempoEsperado; }
        public int getPrioridad() { return prioridad; }
    }
    
    public enum Estado {
        ESPERANDO_ASISTENTE("Esperando registro de datos"),
        ESPERANDO_TRABAJO_SOCIAL("Esperando entrevista social"),
        ESPERANDO_MEDICO("Esperando atención médica"),
        EN_ATENCION("Siendo atendido por médico"),
        COMPLETADO("Atención completada"),
        CITA_PROGRAMADA("Cita médica programada");
        
        private final String descripcion;
        
        Estado(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() { return descripcion; }
    }
    
    // Atributos principales
    private int id;
    private String folio;
    private int pacienteId;
    private int medicoTriageId;
    private LocalDateTime fechaHoraLlegada;
    private LocalDateTime fechaHoraTriage;
    private String motivoConsulta;
    private String sintomasPrincipales;
    
    // Signos vitales
    private Integer presionSistolica;
    private Integer presionDiastolica;
    private Integer frecuenciaCardiaca;
    private Integer frecuenciaRespiratoria;
    private BigDecimal temperatura;
    private Integer saturacionOxigeno;
    private Integer glasgow;
    
    // Clasificación de triage
    private NivelUrgencia nivelUrgencia;
    private String especialidadAsignada;
    private String observacionesTriage;
    
    // Estado del proceso
    private Estado estado;
    private Integer prioridadOrden;
    private LocalDateTime fechaUltimaActualizacion;
    
    // Referencias a otros objetos (no persistidas directamente)
    private Paciente paciente;
    private Usuario medicoTriage;
    
    // Constructores
    public RegistroTriage() {
        this.fechaHoraLlegada = LocalDateTime.now();
        this.fechaUltimaActualizacion = LocalDateTime.now();
        this.estado = Estado.ESPERANDO_ASISTENTE;
    }
    
    public RegistroTriage(int pacienteId, int medicoTriageId, String motivoConsulta) {
        this();
        this.pacienteId = pacienteId;
        this.medicoTriageId = medicoTriageId;
        this.motivoConsulta = motivoConsulta;
    }
    
    // Getters y Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getFolio() {
        return folio;
    }
    
    public void setFolio(String folio) {
        this.folio = folio;
    }
    
    public int getPacienteId() {
        return pacienteId;
    }
    
    public void setPacienteId(int pacienteId) {
        this.pacienteId = pacienteId;
    }
    
    public int getMedicoTriageId() {
        return medicoTriageId;
    }
    
    public void setMedicoTriageId(int medicoTriageId) {
        this.medicoTriageId = medicoTriageId;
    }
    
    public LocalDateTime getFechaHoraLlegada() {
        return fechaHoraLlegada;
    }
    
    public void setFechaHoraLlegada(LocalDateTime fechaHoraLlegada) {
        this.fechaHoraLlegada = fechaHoraLlegada;
    }
    
    public LocalDateTime getFechaHoraTriage() {
        return fechaHoraTriage;
    }
    
    public void setFechaHoraTriage(LocalDateTime fechaHoraTriage) {
        this.fechaHoraTriage = fechaHoraTriage;
    }
    
    public String getMotivoConsulta() {
        return motivoConsulta;
    }
    
    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }
    
    public String getSintomasPrincipales() {
        return sintomasPrincipales;
    }
    
    public void setSintomasPrincipales(String sintomasPrincipales) {
        this.sintomasPrincipales = sintomasPrincipales;
    }
    
    public Integer getPresionSistolica() {
        return presionSistolica;
    }
    
    public void setPresionSistolica(Integer presionSistolica) {
        this.presionSistolica = presionSistolica;
    }
    
    public Integer getPresionDiastolica() {
        return presionDiastolica;
    }
    
    public void setPresionDiastolica(Integer presionDiastolica) {
        this.presionDiastolica = presionDiastolica;
    }
    
    public Integer getFrecuenciaCardiaca() {
        return frecuenciaCardiaca;
    }
    
    public void setFrecuenciaCardiaca(Integer frecuenciaCardiaca) {
        this.frecuenciaCardiaca = frecuenciaCardiaca;
    }
    
    public Integer getFrecuenciaRespiratoria() {
        return frecuenciaRespiratoria;
    }
    
    public void setFrecuenciaRespiratoria(Integer frecuenciaRespiratoria) {
        this.frecuenciaRespiratoria = frecuenciaRespiratoria;
    }
    
    public BigDecimal getTemperatura() {
        return temperatura;
    }
    
    public void setTemperatura(BigDecimal temperatura) {
        this.temperatura = temperatura;
    }
    
    public Integer getSaturacionOxigeno() {
        return saturacionOxigeno;
    }
    
    public void setSaturacionOxigeno(Integer saturacionOxigeno) {
        this.saturacionOxigeno = saturacionOxigeno;
    }
    
    public Integer getGlasgow() {
        return glasgow;
    }
    
    public void setGlasgow(Integer glasgow) {
        this.glasgow = glasgow;
    }
    
    public NivelUrgencia getNivelUrgencia() {
        return nivelUrgencia;
    }
    
    public void setNivelUrgencia(NivelUrgencia nivelUrgencia) {
        this.nivelUrgencia = nivelUrgencia;
    }
    
    public String getEspecialidadAsignada() {
        return especialidadAsignada;
    }
    
    public void setEspecialidadAsignada(String especialidadAsignada) {
        this.especialidadAsignada = especialidadAsignada;
    }
    
    public String getObservacionesTriage() {
        return observacionesTriage;
    }
    
    public void setObservacionesTriage(String observacionesTriage) {
        this.observacionesTriage = observacionesTriage;
    }
    
    public Estado getEstado() {
        return estado;
    }
    
    public void setEstado(Estado estado) {
        this.estado = estado;
        this.fechaUltimaActualizacion = LocalDateTime.now();
    }
    
    public Integer getPrioridadOrden() {
        return prioridadOrden;
    }
    
    public void setPrioridadOrden(Integer prioridadOrden) {
        this.prioridadOrden = prioridadOrden;
    }
    
    public LocalDateTime getFechaUltimaActualizacion() {
        return fechaUltimaActualizacion;
    }
    
    public void setFechaUltimaActualizacion(LocalDateTime fechaUltimaActualizacion) {
        this.fechaUltimaActualizacion = fechaUltimaActualizacion;
    }
    
    public Paciente getPaciente() {
        return paciente;
    }
    
    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }
    
    public Usuario getMedicoTriage() {
        return medicoTriage;
    }
    
    public void setMedicoTriage(Usuario medicoTriage) {
        this.medicoTriage = medicoTriage;
    }
    
    // Métodos de utilidad
    public String getPresionArterial() {
        if (presionSistolica != null && presionDiastolica != null) {
            return presionSistolica + "/" + presionDiastolica;
        }
        return null;
    }
    
    public boolean tieneSignosVitalesCompletos() {
        return presionSistolica != null && presionDiastolica != null &&
               frecuenciaCardiaca != null && frecuenciaRespiratoria != null &&
               temperatura != null && saturacionOxigeno != null;
    }
    
    public boolean esTriageCompleto() {
        return nivelUrgencia != null && 
               especialidadAsignada != null && !especialidadAsignada.trim().isEmpty() &&
               tieneSignosVitalesCompletos();
    }
    
    public long getMinutosEspera() {
        if (fechaHoraLlegada == null) return 0;
        return java.time.Duration.between(fechaHoraLlegada, LocalDateTime.now()).toMinutes();
    }
    
    public boolean esNivelCritico() {
        return nivelUrgencia == NivelUrgencia.ROJO;
    }
    
    public boolean esNivelAmbulatorio() {
        return nivelUrgencia == NivelUrgencia.AZUL;
    }
    
    public boolean puedeAtenderseAmbulatorio() {
        return esNivelAmbulatorio() && estado == Estado.ESPERANDO_ASISTENTE;
    }
    
    public boolean requiereTrabajoSocial() {
        return !esNivelAmbulatorio() && estado == Estado.ESPERANDO_TRABAJO_SOCIAL;
    }
    
    public boolean estaEnProceso() {
        return estado != Estado.COMPLETADO && estado != Estado.CITA_PROGRAMADA;
    }
    
    public void avanzarEstado() {
        switch (estado) {
            case ESPERANDO_ASISTENTE:
                if (esNivelAmbulatorio()) {
                    setEstado(Estado.CITA_PROGRAMADA);
                } else {
                    setEstado(Estado.ESPERANDO_TRABAJO_SOCIAL);
                }
                break;
            case ESPERANDO_TRABAJO_SOCIAL:
                setEstado(Estado.ESPERANDO_MEDICO);
                break;
            case ESPERANDO_MEDICO:
                setEstado(Estado.EN_ATENCION);
                break;
            case EN_ATENCION:
                setEstado(Estado.COMPLETADO);
                break;
            case COMPLETADO:
            case CITA_PROGRAMADA:
                // Estados finales, no se pueden avanzar más
                break;
        }
    }
    
    @Override
    public String toString() {
        return "RegistroTriage{" +
                "folio='" + folio + '\'' +
                ", nivelUrgencia=" + nivelUrgencia +
                ", estado=" + estado +
                ", minutosEspera=" + getMinutosEspera() +
                ", especialidad='" + especialidadAsignada + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RegistroTriage registro = (RegistroTriage) obj;
        return id == registro.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}