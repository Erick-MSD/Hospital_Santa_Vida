package models;

/**
 * Enumeración que define los estados posibles de un paciente en el sistema
 * Representa el flujo completo desde la llegada hasta el alta
 */
public enum EstadoPaciente {
    REGISTRADO("Registrado", "Paciente registrado en el sistema"),
    ESPERANDO_ASISTENTE("Esperando registro", "El paciente llegó y espera ser registrado"),
    ESPERANDO_ATENCION("Esperando atención", "Paciente en sala de espera"),
    ESPERANDO_TRABAJO_SOCIAL("Esperando trabajo social", "Datos registrados, esperando evaluación social"),
    ESPERANDO_MEDICO("Esperando médico", "Evaluación completa, esperando consulta médica"),
    EN_CONSULTA("En consulta", "El paciente está en consulta médica"),
    EN_ATENCION("En atención médica", "El paciente está siendo atendido por el médico"),
    COMPLETADO("Proceso completado", "Alta médica otorgada"),
    CITA_PROGRAMADA("Cita programada", "Paciente nivel AZUL con cita ambulatoria"),
    DADO_DE_ALTA("Dado de alta", "Paciente dado de alta del hospital");
    
    private final String nombre;
    private final String descripcion;
    
    EstadoPaciente(String nombre, String descripcion) {
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