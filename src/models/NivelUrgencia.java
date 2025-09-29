package models;

/**
 * Enumeración que define los niveles de urgencia en el sistema de triage
 * El orden de prioridad es: ROJO > NARANJA > AMARILLO > VERDE > AZUL
 */
public enum NivelUrgencia {
    ROJO(1, "Muy urgente", "Atención inmediata (minutos)", "#DC3545", "Cuidados Intensivos"),
    NARANJA(2, "Urgente", "10-30 minutos", "#FF8C00", "Urgencias"),
    AMARILLO(3, "Menos urgente", "30-120 minutos", "#FFC107", "Consulta general"),
    VERDE(4, "No urgente", "2-4 horas", "#28A745", "Consulta ambulatoria"),
    AZUL(5, "Consulta ambulatoria", "Cita programada", "#007BFF", "Cita externa"),
    
    // Alias para compatibilidad con el código existente
    EMERGENCIA(1, "Emergencia", "Atención inmediata (minutos)", "#DC3545", "Cuidados Intensivos"),
    URGENTE(2, "Urgente", "10-30 minutos", "#FF8C00", "Urgencias"),
    MODERADA(3, "Moderada", "30-120 minutos", "#FFC107", "Consulta general"),
    BAJA(4, "Baja", "2-4 horas", "#28A745", "Consulta ambulatoria"),
    NO_URGENTE(5, "No urgente", "Cita programada", "#007BFF", "Cita externa"),
    
    // Valores adicionales para compatibilidad con controladores
    CRITICO(1, "Crítico", "Atención inmediata", "#DC3545", "Cuidados Intensivos"),
    ALTO(2, "Alto", "10-30 minutos", "#FF8C00", "Urgencias"),
    MEDIO(3, "Medio", "30-120 minutos", "#FFC107", "Consulta general"),
    BAJO(4, "Bajo", "2-4 horas", "#28A745", "Consulta ambulatoria");
    
    private final int prioridad;
    private final String nombre;
    private final String tiempoEspera;
    private final String colorHex;
    private final String tipoAtencion;
    
    NivelUrgencia(int prioridad, String nombre, String tiempoEspera, String colorHex, String tipoAtencion) {
        this.prioridad = prioridad;
        this.nombre = nombre;
        this.tiempoEspera = tiempoEspera;
        this.colorHex = colorHex;
        this.tipoAtencion = tipoAtencion;
    }
    
    public int getPrioridad() {
        return prioridad;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public String getTiempoEspera() {
        return tiempoEspera;
    }
    
    public String getColorHex() {
        return colorHex;
    }
    
    public String getTipoAtencion() {
        return tipoAtencion;
    }
    
    @Override
    public String toString() {
        return name() + " - " + nombre;
    }
}