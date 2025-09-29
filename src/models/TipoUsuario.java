package models;

/**
 * Enumeración que define los tipos de usuario del sistema hospitalario
 * Cada tipo tiene permisos específicos y acceso a diferentes interfaces
 */
public enum TipoUsuario {
    ADMINISTRADOR("Administrador", "Dashboard administrativo y supervisión general"),
    MEDICO_TRIAGE("Médico de Triage", "Evaluación inicial y clasificación de pacientes"),
    ENFERMERO_TRIAGE("Enfermero de Triage", "Apoyo en evaluación inicial y clasificación"),
    ASISTENTE_MEDICA("Asistente Médica", "Registro completo de datos del paciente"),
    RECEPCIONISTA("Recepcionista", "Registro de pacientes y gestión administrativa"),
    TRABAJADOR_SOCIAL("Trabajador Social", "Evaluación social y antecedentes médicos"),
    MEDICO_URGENCIAS("Médico de Urgencias", "Diagnóstico final y tratamiento"),
    MEDICO("Médico", "Médico general del sistema"),
    ENFERMERO("Enfermero", "Personal de enfermería del sistema");
    
    private final String nombre;
    private final String descripcion;
    
    TipoUsuario(String nombre, String descripcion) {
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