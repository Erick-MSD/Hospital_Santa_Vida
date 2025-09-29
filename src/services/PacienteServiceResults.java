package services;

import models.Paciente;
import java.util.List;

/**
 * Clases de resultado para PacienteService
 * Encapsulan los resultados de las operaciones del servicio
 */
public class PacienteServiceResults {
    
    /**
     * Resultado de búsqueda de pacientes
     */
    public static class ResultadoBusqueda {
        private final boolean exitoso;
        private final String mensaje;
        private final List<Paciente> pacientes;
        private final int totalEncontrados;
        
        public ResultadoBusqueda(boolean exitoso, String mensaje, List<Paciente> pacientes, int totalEncontrados) {
            this.exitoso = exitoso;
            this.mensaje = mensaje;
            this.pacientes = pacientes;
            this.totalEncontrados = totalEncontrados;
        }
        
        public boolean isExitoso() { return exitoso; }
        public String getMensaje() { return mensaje; }
        public List<Paciente> getPacientes() { return pacientes; }
        public int getTotalEncontrados() { return totalEncontrados; }
    }
    
    /**
     * Resultado de registro de paciente
     */
    public static class ResultadoRegistro {
        private final boolean exitoso;
        private final String mensaje;
        private final Paciente paciente;
        private final int pacienteId;
        
        public ResultadoRegistro(boolean exitoso, String mensaje, Paciente paciente, int pacienteId) {
            this.exitoso = exitoso;
            this.mensaje = mensaje;
            this.paciente = paciente;
            this.pacienteId = pacienteId;
        }
        
        public boolean isExitoso() { return exitoso; }
        public String getMensaje() { return mensaje; }
        public Paciente getPaciente() { return paciente; }
        public int getPacienteId() { return pacienteId; }
    }
    
    /**
     * Criterios de búsqueda de paciente
     */
    public static class CriteriosBusquedaPaciente {
        private String nombreCompleto;
        private String numeroExpediente;
        private String curp;
        private String rfc;
        private String telefono;
        private String email;
        private java.time.LocalDate fechaNacimiento;
        private String genero;
        private int limite = 50;
        
        public CriteriosBusquedaPaciente() {}
        
        public CriteriosBusquedaPaciente(String criterio) {
            // Constructor simple que toma un criterio general
            this.nombreCompleto = criterio;
            this.numeroExpediente = criterio;
            this.curp = criterio;
            this.telefono = criterio;
        }
        
        // Getters y Setters
        public String getNombreCompleto() { return nombreCompleto; }
        public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
        
        public String getNumeroExpediente() { return numeroExpediente; }
        public void setNumeroExpediente(String numeroExpediente) { this.numeroExpediente = numeroExpediente; }
        
        public String getCurp() { return curp; }
        public void setCurp(String curp) { this.curp = curp; }
        
        public String getRfc() { return rfc; }
        public void setRfc(String rfc) { this.rfc = rfc; }
        
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public java.time.LocalDate getFechaNacimiento() { return fechaNacimiento; }
        public void setFechaNacimiento(java.time.LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
        
        public String getGenero() { return genero; }
        public void setGenero(String genero) { this.genero = genero; }
        
        public int getLimite() { return limite; }
        public void setLimite(int limite) { this.limite = limite; }
    }
    
    /**
     * Datos para registro de paciente
     */
    public static class DatosRegistroPaciente {
        private String nombre;
        private String apellidoPaterno;
        private String apellidoMaterno;
        private java.time.LocalDate fechaNacimiento;
        private String sexo;
        private String curp;
        private String rfc;
        private String telefono;
        private String email;
        private String direccionCalle;
        private String direccionNumero;
        private String direccionColonia;
        private String direccionCiudad;
        private String direccionEstado;
        private String codigoPostal;
        private String seguroMedico;
        private String numeroSeguro;
        private String medicamentos;
        private String enfermedadesPrevias;
        private String contactoEmergenciaNombre;
        private String contactoEmergenciaTelefono;
        private String contactoEmergenciaParentesco;
        
        public DatosRegistroPaciente() {}
        
        // Getters y Setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public String getApellidoPaterno() { return apellidoPaterno; }
        public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }
        
        public String getApellidoMaterno() { return apellidoMaterno; }
        public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }
        
        public java.time.LocalDate getFechaNacimiento() { return fechaNacimiento; }
        public void setFechaNacimiento(java.time.LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
        
        public String getSexo() { return sexo; }
        public void setSexo(String sexo) { this.sexo = sexo; }
        
        public String getCurp() { return curp; }
        public void setCurp(String curp) { this.curp = curp; }
        
        public String getRfc() { return rfc; }
        public void setRfc(String rfc) { this.rfc = rfc; }
        
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getDireccionCalle() { return direccionCalle; }
        public void setDireccionCalle(String direccionCalle) { this.direccionCalle = direccionCalle; }
        
        public String getDireccionNumero() { return direccionNumero; }
        public void setDireccionNumero(String direccionNumero) { this.direccionNumero = direccionNumero; }
        
        public String getDireccionColonia() { return direccionColonia; }
        public void setDireccionColonia(String direccionColonia) { this.direccionColonia = direccionColonia; }
        
        public String getDireccionCiudad() { return direccionCiudad; }
        public void setDireccionCiudad(String direccionCiudad) { this.direccionCiudad = direccionCiudad; }
        
        public String getDireccionEstado() { return direccionEstado; }
        public void setDireccionEstado(String direccionEstado) { this.direccionEstado = direccionEstado; }
        
        public String getCodigoPostal() { return codigoPostal; }
        public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }
        
        public String getSeguroMedico() { return seguroMedico; }
        public void setSeguroMedico(String seguroMedico) { this.seguroMedico = seguroMedico; }
        
        public String getNumeroSeguro() { return numeroSeguro; }
        public void setNumeroSeguro(String numeroSeguro) { this.numeroSeguro = numeroSeguro; }
        
        public String getMedicamentos() { return medicamentos; }
        public void setMedicamentos(String medicamentos) { this.medicamentos = medicamentos; }
        
        public String getEnfermedadesPrevias() { return enfermedadesPrevias; }
        public void setEnfermedadesPrevias(String enfermedadesPrevias) { this.enfermedadesPrevias = enfermedadesPrevias; }
        
        public String getContactoEmergenciaNombre() { return contactoEmergenciaNombre; }
        public void setContactoEmergenciaNombre(String contactoEmergenciaNombre) { this.contactoEmergenciaNombre = contactoEmergenciaNombre; }
        
        public String getContactoEmergenciaTelefono() { return contactoEmergenciaTelefono; }
        public void setContactoEmergenciaTelefono(String contactoEmergenciaTelefono) { this.contactoEmergenciaTelefono = contactoEmergenciaTelefono; }
        
        public String getContactoEmergenciaParentesco() { return contactoEmergenciaParentesco; }
        public void setContactoEmergenciaParentesco(String contactoEmergenciaParentesco) { this.contactoEmergenciaParentesco = contactoEmergenciaParentesco; }
        
        // Métodos adicionales que necesitan los controladores
        public String getAlergias() { return medicamentos; } // Usar medicamentos como alergias temporalmente
        public void setAlergias(String alergias) { this.medicamentos = alergias; }
        
        public String getObservacionesMedicas() { return enfermedadesPrevias; }
        public void setObservacionesMedicas(String observaciones) { this.enfermedadesPrevias = observaciones; }
    }
}