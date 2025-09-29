package models;

/**
 * Enumeración que define los tipos de alta médica
 */
public enum TipoAlta {
    DOMICILIO("Alta a domicilio", "El paciente puede irse a casa"),
    HOSPITALIZACION("Hospitalización", "Requiere internamiento"),
    REFERENCIA("Referencia", "Envío a otro hospital o especialista"),
    DEFUNCION("Defunción", "Fallecimiento del paciente");
    
    private final String nombre;
    private final String descripcion;
    
    TipoAlta(String nombre, String descripcion) {
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