package models;

/**
 * Enumeración que define los estados posibles de una cita médica
 */
public enum EstadoCita {
    PROGRAMADA("Programada", "Cita programada y confirmada"),
    CONFIRMADA("Confirmada", "Cita confirmada por el paciente"),
    EN_PROCESO("En proceso", "Cita en desarrollo"),
    COMPLETADA("Completada", "Cita finalizada exitosamente"),
    CANCELADA("Cancelada", "Cita cancelada"),
    NO_ASISTIO("No asistió", "Paciente no se presentó a la cita"),
    REAGENDADA("Reagendada", "Cita reagendada para otra fecha");
    
    private final String nombre;
    private final String descripcion;
    
    EstadoCita(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}