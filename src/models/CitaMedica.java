package models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Modelo de datos para citas médicas ambulatorias
 * Solo para pacientes nivel AZUL que no requieren atención de urgencia
 */
public class CitaMedica {
    
    // Enumeración para estados de cita
    public enum EstadoCita {
        PROGRAMADA("Cita programada"),
        CONFIRMADA("Cita confirmada por paciente"),
        CANCELADA("Cita cancelada"),
        COMPLETADA("Cita completada"),
        NO_ASISTIO("Paciente no asistió");
        
        private final String descripcion;
        
        EstadoCita(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() { return descripcion; }
    }
    
    // Atributos principales
    private int id;
    private int registroTriageId;
    private int asistenteMedicaId;
    private LocalDate fechaProgramada;
    private LocalTime horaProgramada;
    private String especialidad;
    private String medicoAsignado;
    private String consultorio;
    private EstadoCita estadoCita;
    private String motivoCancelacion;
    private LocalDateTime fechaCreacion;
    private String observaciones;
    
    // Referencias a otros objetos
    private RegistroTriage registroTriage;
    private Usuario asistenteMedica;
    
    // Constructores
    public CitaMedica() {
        this.fechaCreacion = LocalDateTime.now();
        this.estadoCita = EstadoCita.PROGRAMADA;
    }
    
    public CitaMedica(int registroTriageId, int asistenteMedicaId, 
                     LocalDate fechaProgramada, LocalTime horaProgramada, String especialidad) {
        this();
        this.registroTriageId = registroTriageId;
        this.asistenteMedicaId = asistenteMedicaId;
        this.fechaProgramada = fechaProgramada;
        this.horaProgramada = horaProgramada;
        this.especialidad = especialidad;
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
    
    public int getAsistenteMedicaId() {
        return asistenteMedicaId;
    }
    
    public void setAsistenteMedicaId(int asistenteMedicaId) {
        this.asistenteMedicaId = asistenteMedicaId;
    }
    
    public LocalDate getFechaProgramada() {
        return fechaProgramada;
    }
    
    public void setFechaProgramada(LocalDate fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }
    
    public LocalTime getHoraProgramada() {
        return horaProgramada;
    }
    
    public void setHoraProgramada(LocalTime horaProgramada) {
        this.horaProgramada = horaProgramada;
    }
    
    public String getEspecialidad() {
        return especialidad;
    }
    
    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }
    
    public String getMedicoAsignado() {
        return medicoAsignado;
    }
    
    public void setMedicoAsignado(String medicoAsignado) {
        this.medicoAsignado = medicoAsignado;
    }
    
    public String getConsultorio() {
        return consultorio;
    }
    
    public void setConsultorio(String consultorio) {
        this.consultorio = consultorio;
    }
    
    public EstadoCita getEstadoCita() {
        return estadoCita;
    }
    
    public void setEstadoCita(EstadoCita estadoCita) {
        this.estadoCita = estadoCita;
    }
    
    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }
    
    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public RegistroTriage getRegistroTriage() {
        return registroTriage;
    }
    
    public void setRegistroTriage(RegistroTriage registroTriage) {
        this.registroTriage = registroTriage;
    }
    
    public Usuario getAsistenteMedica() {
        return asistenteMedica;
    }
    
    public void setAsistenteMedica(Usuario asistenteMedica) {
        this.asistenteMedica = asistenteMedica;
    }
    
    // Métodos de utilidad
    public LocalDateTime getFechaHoraCompleta() {
        if (fechaProgramada != null && horaProgramada != null) {
            return LocalDateTime.of(fechaProgramada, horaProgramada);
        }
        return null;
    }
    
    public boolean esCitaVigente() {
        LocalDateTime fechaHora = getFechaHoraCompleta();
        return fechaHora != null && fechaHora.isAfter(LocalDateTime.now()) &&
               (estadoCita == EstadoCita.PROGRAMADA || estadoCita == EstadoCita.CONFIRMADA);
    }
    
    public boolean esCitaVencida() {
        LocalDateTime fechaHora = getFechaHoraCompleta();
        return fechaHora != null && fechaHora.isBefore(LocalDateTime.now()) &&
               estadoCita != EstadoCita.COMPLETADA && estadoCita != EstadoCita.CANCELADA;
    }
    
    public boolean puedeConfirmarse() {
        return estadoCita == EstadoCita.PROGRAMADA && esCitaVigente();
    }
    
    public boolean puedeCancelarse() {
        return (estadoCita == EstadoCita.PROGRAMADA || estadoCita == EstadoCita.CONFIRMADA) && 
               esCitaVigente();
    }
    
    public boolean puedeCompletarse() {
        return estadoCita == EstadoCita.CONFIRMADA;
    }
    
    public long getDiasHastaCita() {
        if (fechaProgramada == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fechaProgramada);
    }
    
    public boolean esHoy() {
        return fechaProgramada != null && fechaProgramada.equals(LocalDate.now());
    }
    
    public boolean esMañana() {
        return fechaProgramada != null && fechaProgramada.equals(LocalDate.now().plusDays(1));
    }
    
    public boolean requiereRecordatorio() {
        long dias = getDiasHastaCita();
        return esCitaVigente() && (dias == 1 || dias == 3); // Recordar 1 y 3 días antes
    }
    
    public void confirmar() {
        if (puedeConfirmarse()) {
            setEstadoCita(EstadoCita.CONFIRMADA);
        }
    }
    
    public void cancelar(String motivo) {
        if (puedeCancelarse()) {
            setEstadoCita(EstadoCita.CANCELADA);
            setMotivoCancelacion(motivo);
        }
    }
    
    public void completar() {
        if (puedeCompletarse()) {
            setEstadoCita(EstadoCita.COMPLETADA);
        }
    }
    
    public void marcarNoAsistio() {
        if (estadoCita == EstadoCita.CONFIRMADA || estadoCita == EstadoCita.PROGRAMADA) {
            setEstadoCita(EstadoCita.NO_ASISTIO);
        }
    }
    
    public String getDescripcionCompleta() {
        StringBuilder desc = new StringBuilder();
        desc.append("Cita de ").append(especialidad);
        
        if (medicoAsignado != null && !medicoAsignado.trim().isEmpty()) {
            desc.append(" con ").append(medicoAsignado);
        }
        
        if (consultorio != null && !consultorio.trim().isEmpty()) {
            desc.append(" en ").append(consultorio);
        }
        
        return desc.toString();
    }
    
    @Override
    public String toString() {
        return "CitaMedica{" +
                "id=" + id +
                ", fechaProgramada=" + fechaProgramada +
                ", horaProgramada=" + horaProgramada +
                ", especialidad='" + especialidad + '\'' +
                ", medicoAsignado='" + medicoAsignado + '\'' +
                ", estadoCita=" + estadoCita +
                ", diasHastaCita=" + getDiasHastaCita() +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CitaMedica cita = (CitaMedica) obj;
        return id == cita.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}