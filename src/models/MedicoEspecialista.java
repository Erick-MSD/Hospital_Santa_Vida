package models;

/**
 * Clase que representa un médico especialista con su especialidad asignada
 * Se usa para la gestión de asignaciones médicas por especialidad
 */
public class MedicoEspecialista {
    private int usuarioId;
    private String nombreCompleto;
    private Especialidad especialidad;
    private boolean activo;
    private String turno; // MAÑANA, TARDE, MIXTO
    private int capacidadDiaria; // Máximo de pacientes por día
    
    /**
     * Constructor por defecto
     */
    public MedicoEspecialista() {}
    
    /**
     * Constructor completo
     */
    public MedicoEspecialista(int usuarioId, String nombreCompleto, Especialidad especialidad, 
                             boolean activo, String turno, int capacidadDiaria) {
        this.usuarioId = usuarioId;
        this.nombreCompleto = nombreCompleto;
        this.especialidad = especialidad;
        this.activo = activo;
        this.turno = turno;
        this.capacidadDiaria = capacidadDiaria;
    }
    
    // Getters
    public int getUsuarioId() { return usuarioId; }
    public String getNombreCompleto() { return nombreCompleto; }
    public Especialidad getEspecialidad() { return especialidad; }
    public boolean isActivo() { return activo; }
    public String getTurno() { return turno; }
    public int getCapacidadDiaria() { return capacidadDiaria; }
    
    // Setters
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public void setEspecialidad(Especialidad especialidad) { this.especialidad = especialidad; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public void setTurno(String turno) { this.turno = turno; }
    public void setCapacidadDiaria(int capacidadDiaria) { this.capacidadDiaria = capacidadDiaria; }
    
    /**
     * Convierte a Usuario para compatibilidad
     */
    public Usuario toUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setActivo(activo);
        usuario.setTipoUsuario(TipoUsuario.MEDICO);
        return usuario;
    }
    
    @Override
    public String toString() {
        return String.format("Dr. %s (%s) - %s", nombreCompleto, especialidad, 
                           activo ? "Activo" : "Inactivo");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MedicoEspecialista other = (MedicoEspecialista) obj;
        return usuarioId == other.usuarioId;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(usuarioId);
    }
}