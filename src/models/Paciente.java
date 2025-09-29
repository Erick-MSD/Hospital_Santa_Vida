package models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Clase modelo que representa a un paciente del hospital
 * Mapea directamente con la tabla 'pacientes' de la base de datos
 */
public class Paciente {
    private int id;
    private String numeroExpediente;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private LocalDate fechaNacimiento;
    private String sexo;
    private String curp;
    private String rfc;
    private String telefonoPrincipal;
    private String telefonoSecundario;
    private String email;
    private String direccionCalle;
    private String direccionNumero;
    private String direccionColonia;
    private String direccionCiudad;
    private String direccionEstado;
    private String direccionCp;
    private String seguroMedico;
    private String numeroPoliza;
    private String contactoEmergenciaNombre;
    private String contactoEmergenciaTelefono;
    private String contactoEmergenciaRelacion;
    private LocalDateTime fechaRegistro;
    
    // Campos médicos adicionales
    private String tipoSangre;
    private String alergias;
    private String enfermedadesCronicas;
    private String medicamentosActuales;
    private String observacionesMedicas;
    private EstadoPaciente estadoActual;
    private String numeroSeguro;
    private String condicionesPreexistentes;
    private TipoAlta tipoAlta;
    
    // Constructor vacío
    public Paciente() {}
    
    // Constructor básico
    public Paciente(String nombre, String apellidoPaterno, String apellidoMaterno, 
                   LocalDate fechaNacimiento, String sexo, String telefonoPrincipal) {
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.telefonoPrincipal = telefonoPrincipal;
        this.fechaRegistro = LocalDateTime.now();
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
    
    public String getSexo() {
        return sexo;
    }
    
    public void setSexo(String sexo) {
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
    
    // Nuevos getters y setters
    public String getNumeroExpediente() {
        return numeroExpediente;
    }
    
    public void setNumeroExpediente(String numeroExpediente) {
        this.numeroExpediente = numeroExpediente;
    }
    
    public String getTipoSangre() {
        return tipoSangre;
    }
    
    public void setTipoSangre(String tipoSangre) {
        this.tipoSangre = tipoSangre;
    }
    
    public String getAlergias() {
        return alergias;
    }
    
    public void setAlergias(String alergias) {
        this.alergias = alergias;
    }
    
    public String getEnfermedadesCronicas() {
        return enfermedadesCronicas;
    }
    
    public void setEnfermedadesCronicas(String enfermedadesCronicas) {
        this.enfermedadesCronicas = enfermedadesCronicas;
    }
    
    public String getMedicamentosActuales() {
        return medicamentosActuales;
    }
    
    public void setMedicamentosActuales(String medicamentosActuales) {
        this.medicamentosActuales = medicamentosActuales;
    }
    
    public String getObservacionesMedicas() {
        return observacionesMedicas;
    }
    
    public void setObservacionesMedicas(String observacionesMedicas) {
        this.observacionesMedicas = observacionesMedicas;
    }
    
    public EstadoPaciente getEstadoActual() {
        return estadoActual;
    }
    
    public void setEstadoActual(EstadoPaciente estadoActual) {
        this.estadoActual = estadoActual;
    }
    
    // Métodos de conveniencia adicionales
    public void setNombreCompleto(String nombreCompleto) {
        // Se puede implementar lógica para separar nombre completo
        // Por ahora, asignar al nombre principal
        this.nombre = nombreCompleto;
    }
    
    public void setGenero(String genero) {
        this.sexo = genero;
    }
    
    public String getGenero() {
        return this.sexo;
    }
    
    public void setTelefono(String telefono) {
        this.telefonoPrincipal = telefono;
    }
    
    public String getTelefono() {
        return this.telefonoPrincipal;
    }
    
    public void setDireccionCompleta(String direccionCompleta) {
        // Lógica simplificada - asignar a la calle
        this.direccionCalle = direccionCompleta;
    }
    
    public String getNumeroSeguro() {
        return numeroSeguro;
    }
    
    public void setNumeroSeguro(String numeroSeguro) {
        this.numeroSeguro = numeroSeguro;
    }
    
    public String getCondicionesPreexistentes() {
        return condicionesPreexistentes;
    }
    
    public void setCondicionesPreexistentes(String condicionesPreexistentes) {
        this.condicionesPreexistentes = condicionesPreexistentes;
    }
    
    public TipoAlta getTipoAlta() {
        return tipoAlta;
    }
    
    public void setTipoAlta(TipoAlta tipoAlta) {
        this.tipoAlta = tipoAlta;
    }
    
    // Métodos de utilidad
    public String getNombreCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append(nombre).append(" ").append(apellidoPaterno);
        if (apellidoMaterno != null && !apellidoMaterno.trim().isEmpty()) {
            sb.append(" ").append(apellidoMaterno);
        }
        return sb.toString();
    }
    
    public String getDireccionCompleta() {
        return String.format("%s %s, Col. %s, %s, %s, CP: %s",
                direccionCalle, direccionNumero, direccionColonia,
                direccionCiudad, direccionEstado, direccionCp);
    }
    
    public int getEdad() {
        if (fechaNacimiento == null) return 0;
        return LocalDate.now().getYear() - fechaNacimiento.getYear();
    }
    
    public boolean tieneCurp() {
        return curp != null && !curp.trim().isEmpty();
    }
    
    public boolean tieneSeguroMedico() {
        return seguroMedico != null && !seguroMedico.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return getNombreCompleto() + " - " + telefonoPrincipal;
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
    
    // Métodos adicionales requeridos por los controladores
    
    public boolean isActivo() {
        return estadoActual != null && estadoActual != EstadoPaciente.DADO_DE_ALTA;
    }
    
    public String getCodigoPostal() {
        return direccionCp;
    }
    
    public void setCodigoPostal(String codigoPostal) {
        this.direccionCp = codigoPostal;
    }
    
    public String getMedicamentos() {
        return medicamentosActuales;
    }
    
    public void setMedicamentos(String medicamentos) {
        this.medicamentosActuales = medicamentos;
    }
    
    public String getEnfermedadesPrevias() {
        return enfermedadesCronicas;
    }
    
    public void setEnfermedadesPrevias(String enfermedadesPrevias) {
        this.enfermedadesCronicas = enfermedadesPrevias;
    }
    
    public String getContactoEmergenciaParentesco() {
        return contactoEmergenciaRelacion;
    }
    
    public void setContactoEmergenciaParentesco(String parentesco) {
        this.contactoEmergenciaRelacion = parentesco;
    }
}