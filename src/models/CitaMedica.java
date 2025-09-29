package models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Clase modelo que representa una cita médica ambulatoria
 * Solo para pacientes de nivel AZUL que requieren cita programada
 * Mapea directamente con la tabla 'citas_medicas' de la base de datos
 */
public class CitaMedica {
    private int id;
    private int registroTriageId;
    private int pacienteId;  // Campo agregado
    private int medicoId;    // Campo agregado
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
    
    // Campos adicionales para compatibilidad
    private String motivoCita;
    private int usuarioCreacionId;
    private String pacienteNombre;
    private String numeroExpediente;
    private String medicoNombre;
    private String usuarioCreacionNombre;
    
    // Referencias a objetos relacionados
    private RegistroTriage registroTriage;
    private Usuario asistenteMedica;
    
    // Constructor vacío
    public CitaMedica() {
        this.fechaCreacion = LocalDateTime.now();
        this.estadoCita = EstadoCita.PROGRAMADA;
    }
    
    // Constructor básico
    public CitaMedica(int registroTriageId, int asistenteMedicaId, LocalDate fechaProgramada, 
                     LocalTime horaProgramada, String especialidad) {
        this();
        this.registroTriageId = registroTriageId;
        this.asistenteMedicaId = asistenteMedicaId;
        this.fechaProgramada = fechaProgramada;
        this.horaProgramada = horaProgramada;
        this.especialidad = especialidad;
    }
    
    // Enum para estados de cita
    public enum EstadoCita {
        PROGRAMADA("Programada"),
        CONFIRMADA("Confirmada"),
        CANCELADA("Cancelada"),
        COMPLETADA("Completada"),
        NO_ASISTIO("No asistió");
        
        private final String nombre;
        
        EstadoCita(String nombre) {
            this.nombre = nombre;
        }
        
        public String getNombre() {
            return nombre;
        }
        
        @Override
        public String toString() {
            return nombre;
        }
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
    
    public LocalTime getHoraCita() {
        return horaProgramada;
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
    
    // Métodos de compatibilidad para enum Especialidad
    public void setEspecialidad(Especialidad especialidad) {
        this.especialidad = especialidad != null ? especialidad.name() : null;
    }
    
    public Especialidad getEspecialidadEnum() {
        return especialidad != null ? Especialidad.valueOf(especialidad) : null;
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
    
    // Método de compatibilidad para String
    public void setEstadoCita(String estadoCita) {
        this.estadoCita = estadoCita != null ? EstadoCita.valueOf(estadoCita) : null;
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
    
    // Getters y setters para campos adicionales
    public String getMotivoCita() {
        return motivoCita;
    }
    
    public void setMotivoCita(String motivoCita) {
        this.motivoCita = motivoCita;
    }
    
    public int getUsuarioCreacionId() {
        return usuarioCreacionId;
    }
    
    public void setUsuarioCreacionId(int usuarioCreacionId) {
        this.usuarioCreacionId = usuarioCreacionId;
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
    
    public String getUsuarioCreacionNombre() {
        return usuarioCreacionNombre;
    }
    
    public void setUsuarioCreacionNombre(String usuarioCreacionNombre) {
        this.usuarioCreacionNombre = usuarioCreacionNombre;
    }
    
    // Métodos de conveniencia adicionales
    public void setFechaCita(LocalDate fecha) {
        this.fechaProgramada = fecha;
    }
    
    public LocalDate getFechaCita() {
        return this.fechaProgramada;
    }
    
    public void setHoraCita(LocalTime hora) {
        this.horaProgramada = hora;
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
    public String getFechaHoraFormateada() {
        if (fechaProgramada != null && horaProgramada != null) {
            return fechaProgramada.toString() + " " + horaProgramada.toString();
        }
        return "No programada";
    }
    
    public boolean esCitaVigente() {
        if (fechaProgramada == null) return false;
        LocalDate hoy = LocalDate.now();
        return !fechaProgramada.isBefore(hoy) && 
               (estadoCita == EstadoCita.PROGRAMADA || estadoCita == EstadoCita.CONFIRMADA);
    }
    
    public boolean esCitaHoy() {
        return fechaProgramada != null && fechaProgramada.equals(LocalDate.now());
    }
    
    public boolean esCitaPasada() {
        if (fechaProgramada == null) return false;
        return fechaProgramada.isBefore(LocalDate.now());
    }
    
    public boolean estaCancelada() {
        return estadoCita == EstadoCita.CANCELADA;
    }
    
    public boolean estaCompleta() {
        return estadoCita == EstadoCita.COMPLETADA;
    }
    
    public void cancelar(String motivo) {
        this.estadoCita = EstadoCita.CANCELADA;
        this.motivoCancelacion = motivo;
    }
    
    public void confirmar() {
        if (estadoCita == EstadoCita.PROGRAMADA) {
            this.estadoCita = EstadoCita.CONFIRMADA;
        }
    }
    
    public void completar() {
        this.estadoCita = EstadoCita.COMPLETADA;
    }
    
    public void marcarNoAsistencia() {
        this.estadoCita = EstadoCita.NO_ASISTIO;
    }
    
    @Override
    public String toString() {
        return "Cita " + especialidad + " - " + getFechaHoraFormateada() + 
               " [" + estadoCita.getNombre() + "]";
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