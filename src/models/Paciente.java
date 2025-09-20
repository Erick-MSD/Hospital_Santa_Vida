package models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo de datos para pacientes del hospital
 * Contiene información personal y de contacto que se mantiene entre visitas
 */
public class Paciente {
    
    // Enumeración para sexo
    public enum Sexo {
        MASCULINO,
        FEMENINO,
        OTRO
    }
    
    // Atributos principales
    private int id;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private LocalDate fechaNacimiento;
    private Sexo sexo;
    private String curp;
    private String rfc;
    private String telefonoPrincipal;
    private String telefonoSecundario;
    private String email;
    
    // Dirección
    private String direccionCalle;
    private String direccionNumero;
    private String direccionColonia;
    private String direccionCiudad;
    private String direccionEstado;
    private String direccionCp;
    
    // Seguro médico
    private String seguroMedico;
    private String numeroPoliza;
    
    // Contacto de emergencia
    private String contactoEmergenciaNombre;
    private String contactoEmergenciaTelefono;
    private String contactoEmergenciaRelacion;
    
    // Metadatos
    private LocalDateTime fechaRegistro;
    
    // Constructores
    public Paciente() {
        this.fechaRegistro = LocalDateTime.now();
    }
    
    public Paciente(String nombre, String apellidoPaterno, String apellidoMaterno,
                   LocalDate fechaNacimiento, Sexo sexo, String telefonoPrincipal) {
        this();
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.telefonoPrincipal = telefonoPrincipal;
    }
    
    // Getters y Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getApellidoPaterno() {
        return apellidoPaterno;
    }
    
    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }
    
    public String getApellidoMaterno() {
        return apellidoMaterno;
    }
    
    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }
    
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }
    
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
    
    public Sexo getSexo() {
        return sexo;
    }
    
    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }
    
    public String getCurp() {
        return curp;
    }
    
    public void setCurp(String curp) {
        this.curp = curp;
    }
    
    public String getRfc() {
        return rfc;
    }
    
    public void setRfc(String rfc) {
        this.rfc = rfc;
    }
    
    public String getTelefonoPrincipal() {
        return telefonoPrincipal;
    }
    
    public void setTelefonoPrincipal(String telefonoPrincipal) {
        this.telefonoPrincipal = telefonoPrincipal;
    }
    
    public String getTelefonoSecundario() {
        return telefonoSecundario;
    }
    
    public void setTelefonoSecundario(String telefonoSecundario) {
        this.telefonoSecundario = telefonoSecundario;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getDireccionCalle() {
        return direccionCalle;
    }
    
    public void setDireccionCalle(String direccionCalle) {
        this.direccionCalle = direccionCalle;
    }
    
    public String getDireccionNumero() {
        return direccionNumero;
    }
    
    public void setDireccionNumero(String direccionNumero) {
        this.direccionNumero = direccionNumero;
    }
    
    public String getDireccionColonia() {
        return direccionColonia;
    }
    
    public void setDireccionColonia(String direccionColonia) {
        this.direccionColonia = direccionColonia;
    }
    
    public String getDireccionCiudad() {
        return direccionCiudad;
    }
    
    public void setDireccionCiudad(String direccionCiudad) {
        this.direccionCiudad = direccionCiudad;
    }
    
    public String getDireccionEstado() {
        return direccionEstado;
    }
    
    public void setDireccionEstado(String direccionEstado) {
        this.direccionEstado = direccionEstado;
    }
    
    public String getDireccionCp() {
        return direccionCp;
    }
    
    public void setDireccionCp(String direccionCp) {
        this.direccionCp = direccionCp;
    }
    
    public String getSeguroMedico() {
        return seguroMedico;
    }
    
    public void setSeguroMedico(String seguroMedico) {
        this.seguroMedico = seguroMedico;
    }
    
    public String getNumeroPoliza() {
        return numeroPoliza;
    }
    
    public void setNumeroPoliza(String numeroPoliza) {
        this.numeroPoliza = numeroPoliza;
    }
    
    public String getContactoEmergenciaNombre() {
        return contactoEmergenciaNombre;
    }
    
    public void setContactoEmergenciaNombre(String contactoEmergenciaNombre) {
        this.contactoEmergenciaNombre = contactoEmergenciaNombre;
    }
    
    public String getContactoEmergenciaTelefono() {
        return contactoEmergenciaTelefono;
    }
    
    public void setContactoEmergenciaTelefono(String contactoEmergenciaTelefono) {
        this.contactoEmergenciaTelefono = contactoEmergenciaTelefono;
    }
    
    public String getContactoEmergenciaRelacion() {
        return contactoEmergenciaRelacion;
    }
    
    public void setContactoEmergenciaRelacion(String contactoEmergenciaRelacion) {
        this.contactoEmergenciaRelacion = contactoEmergenciaRelacion;
    }
    
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
    
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    // Métodos de utilidad
    public String getNombreCompleto() {
        StringBuilder nombreCompleto = new StringBuilder();
        nombreCompleto.append(nombre);
        
        if (apellidoPaterno != null && !apellidoPaterno.trim().isEmpty()) {
            nombreCompleto.append(" ").append(apellidoPaterno);
        }
        
        if (apellidoMaterno != null && !apellidoMaterno.trim().isEmpty()) {
            nombreCompleto.append(" ").append(apellidoMaterno);
        }
        
        return nombreCompleto.toString();
    }
    
    public String getDireccionCompleta() {
        return String.format("%s %s, %s, %s, %s, CP %s",
                direccionCalle != null ? direccionCalle : "",
                direccionNumero != null ? direccionNumero : "",
                direccionColonia != null ? direccionColonia : "",
                direccionCiudad != null ? direccionCiudad : "",
                direccionEstado != null ? direccionEstado : "",
                direccionCp != null ? direccionCp : "");
    }
    
    public int getEdad() {
        if (fechaNacimiento == null) {
            return 0;
        }
        return LocalDate.now().getYear() - fechaNacimiento.getYear();
    }
    
    public boolean tieneSeguroMedico() {
        return seguroMedico != null && !seguroMedico.trim().isEmpty();
    }
    
    public boolean datosCompletos() {
        return nombre != null && !nombre.trim().isEmpty() &&
               apellidoPaterno != null && !apellidoPaterno.trim().isEmpty() &&
               fechaNacimiento != null &&
               telefonoPrincipal != null && !telefonoPrincipal.trim().isEmpty() &&
               contactoEmergenciaNombre != null && !contactoEmergenciaNombre.trim().isEmpty() &&
               contactoEmergenciaTelefono != null && !contactoEmergenciaTelefono.trim().isEmpty() &&
               direccionCalle != null && !direccionCalle.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return "Paciente{" +
                "id=" + id +
                ", nombreCompleto='" + getNombreCompleto() + '\'' +
                ", edad=" + getEdad() +
                ", sexo=" + sexo +
                ", telefono='" + telefonoPrincipal + '\'' +
                ", seguroMedico='" + seguroMedico + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Paciente paciente = (Paciente) obj;
        return id == paciente.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}