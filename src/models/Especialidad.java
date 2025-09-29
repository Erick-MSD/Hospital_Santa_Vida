package models;

/**
 * Enumeración que define las especialidades médicas disponibles
 * Actualizada según el blueprint del sistema
 */
public enum Especialidad {
    MEDICINA_INTERNA("Medicina Interna"),
    CARDIOLOGIA("Cardiología"),
    NEUROLOGIA("Neurología"),
    ORTOPEDIA("Ortopedia"),
    PEDIATRIA("Pediatría"),
    GINECOLOGIA("Ginecología"),
    CIRUGIA_GENERAL("Cirugía General"),
    UROLOGIA("Urología"),
    ONCOLOGIA("Oncología"),
    NEFROLOGIA("Nefrología"),
    OFTALMOLOGIA("Oftalmología"),
    NEUMOLOGIA("Neumología"),
    INFECTOLOGIA("Infectología"),
    CUIDADOS_INTENSIVOS("Cuidados Intensivos");
    
    private final String nombre;
    
    Especialidad(String nombre) {
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