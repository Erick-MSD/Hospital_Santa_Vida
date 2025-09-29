package models;

import java.time.LocalDateTime;

/**
 * Clase modelo que representa un registro de triage hospitalario
 * Mapea directamente con la tabla 'registros_triage' de la base de datos
 */
public class RegistroTriage {
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
    private Double temperatura;
    private Integer saturacionOxigeno;
    private Integer glasgow;
    
    // Clasificación
    private NivelUrgencia nivelUrgencia;
    private String especialidadAsignada;
    private String observacionesTriage;
    
    // Estado del proceso
    private EstadoPaciente estado;
    private Integer prioridadOrden;
    private LocalDateTime fechaUltimaActualizacion;
    
    // Referencias a objetos relacionados (no persistidas directamente)
    private Paciente paciente;
    private Usuario medicoTriage;
    
    // Campos adicionales para compatibilidad con DAOs
    private String pacienteNombre;
    private String numeroExpediente;
    private String usuarioNombre;
    private Integer nivelDolor;
    private Integer tiempoEstimadoAtencion;
    
    // Constructor vacío
    public RegistroTriage() {
        this.fechaHoraLlegada = LocalDateTime.now();
        this.estado = EstadoPaciente.ESPERANDO_ASISTENTE;
        this.fechaUltimaActualizacion = LocalDateTime.now();
    }
    
    // Constructor básico
    public RegistroTriage(int pacienteId, String motivoConsulta) {
        this();
        this.pacienteId = pacienteId;
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
    
    public Double getTemperatura() {
        return temperatura;
    }
    
    public void setTemperatura(Double temperatura) {
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
    
    public EstadoPaciente getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoPaciente estado) {
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
            return presionSistolica + "/" + presionDiastolica + " mmHg";
        }
        return "No registrada";
    }
    
    public boolean tieneSignosVitalesCompletos() {
        return presionSistolica != null && presionDiastolica != null &&
               frecuenciaCardiaca != null && frecuenciaRespiratoria != null &&
               temperatura != null && saturacionOxigeno != null;
    }
    
    public long getMinutosEspera() {
        if (fechaHoraLlegada == null) return 0;
        return java.time.Duration.between(fechaHoraLlegada, LocalDateTime.now()).toMinutes();
    }
    
    public boolean requiereAtencionInmediata() {
        return nivelUrgencia == NivelUrgencia.ROJO;
    }
    
    public boolean esTriageCompleto() {
        return fechaHoraTriage != null && nivelUrgencia != null && 
               especialidadAsignada != null && tieneSignosVitalesCompletos();
    }
    
    // Métodos de conveniencia adicionales para compatibilidad
    public void setUsuarioTriageId(int usuarioId) {
        this.medicoTriageId = usuarioId;
    }
    
    public void setFechaTriage(LocalDateTime fechaTriage) {
        this.fechaHoraTriage = fechaTriage;
    }
    
    public void setSignosVitalesPresion(String presion) {
        // Parsear presión en formato "120/80"
        if (presion != null && presion.contains("/")) {
            String[] partes = presion.split("/");
            if (partes.length == 2) {
                try {
                    this.presionSistolica = Integer.parseInt(partes[0]);
                    this.presionDiastolica = Integer.parseInt(partes[1]);
                } catch (NumberFormatException e) {
                    // Ignorar si no se puede parsear
                }
            }
        }
    }
    
    public void setSignosVitalesPulso(int pulso) {
        this.frecuenciaCardiaca = pulso;
    }
    
    public void setSignosVitalesTemperatura(double temp) {
        this.temperatura = temp;
    }
    
    public void setSignosVitalesRespiracion(int respiracion) {
        this.frecuenciaRespiratoria = respiracion;
    }
    
    public void setSignosVitalesSaturacion(int saturacion) {
        this.saturacionOxigeno = saturacion;
    }
    
    public void setNivelDolor(int nivelDolor) {
        this.nivelDolor = nivelDolor;
    }
    
    public void setEscalaGlasgow(int glasgow) {
        this.glasgow = glasgow;
    }
    
    public void setTiempoEstimadoAtencion(int tiempoMinutos) {
        this.tiempoEstimadoAtencion = tiempoMinutos;
    }
    
    public void setPrioridadNumerica(int prioridadNumerica) {
        this.prioridadOrden = prioridadNumerica;
    }
    
    // Métodos getter adicionales para compatibilidad con DAOs
    public Integer getUsuarioTriageId() {
        return this.medicoTriageId;
    }
    
    public LocalDateTime getFechaTriage() {
        return this.fechaHoraTriage;
    }
    
    public String getSignosVitalesPresion() {
        if (presionSistolica != null && presionDiastolica != null) {
            return presionSistolica + "/" + presionDiastolica;
        }
        return null;
    }
    
    public Integer getSignosVitalesPulso() {
        return this.frecuenciaCardiaca;
    }
    
    public Double getSignosVitalesTemperatura() {
        return this.temperatura;
    }
    
    public Integer getSignosVitalesRespiracion() {
        return this.frecuenciaRespiratoria;
    }
    
    public Integer getSignosVitalesSaturacion() {
        return this.saturacionOxigeno;
    }
    
    public Integer getNivelDolor() {
        return this.nivelDolor;
    }
    
    public Integer getEscalaGlasgow() {
        return this.glasgow;
    }
    
    public Integer getTiempoEstimadoAtencion() {
        return this.tiempoEstimadoAtencion;
    }
    
    public Integer getPrioridadNumerica() {
        return this.prioridadOrden;
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
    
    public String getUsuarioNombre() {
        return usuarioNombre;
    }
    
    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }
    
    @Override
    public String toString() {
        return folio + " - " + (paciente != null ? paciente.getNombreCompleto() : "Paciente ID: " + pacienteId) +
               " [" + (nivelUrgencia != null ? nivelUrgencia.name() : "Sin nivel") + "]";
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