package models;

import java.time.LocalDateTime;

/**
 * Modelo de datos para usuarios del sistema
 * Representa a todos los empleados del hospital que pueden acceder al sistema
 */
public class Usuario {
    
    // Enumeración para tipos de usuario
    public enum TipoUsuario {
        ADMINISTRADOR,
        MEDICO_TRIAGE,
        ASISTENTE_MEDICA,
        TRABAJADOR_SOCIAL,
        MEDICO_URGENCIAS
    }
    
    // Atributos principales
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private TipoUsuario tipoUsuario;
    private String nombreCompleto;
    private String cedulaProfesional;
    private String especialidad;
    private String telefono;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoAcceso;
    private Integer createdBy;
    
    // Constructores
    public Usuario() {
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
    }
    
    public Usuario(String username, String email, String passwordHash, 
                   TipoUsuario tipoUsuario, String nombreCompleto) {
        this();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.tipoUsuario = tipoUsuario;
        this.nombreCompleto = nombreCompleto;
    }
    
    // Getters y Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }
    
    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }
    
    public String getNombreCompleto() {
        return nombreCompleto;
    }
    
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
    
    public String getCedulaProfesional() {
        return cedulaProfesional;
    }
    
    public void setCedulaProfesional(String cedulaProfesional) {
        this.cedulaProfesional = cedulaProfesional;
    }
    
    public String getEspecialidad() {
        return especialidad;
    }
    
    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }
    
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }
    
    public Integer getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }
    
    // Métodos de utilidad
    public boolean esMedico() {
        return tipoUsuario == TipoUsuario.MEDICO_TRIAGE || 
               tipoUsuario == TipoUsuario.MEDICO_URGENCIAS;
    }
    
    public boolean puedeCrearUsuarios() {
        return tipoUsuario == TipoUsuario.ADMINISTRADOR;
    }
    
    public boolean puedeRealizarTriage() {
        return tipoUsuario == TipoUsuario.MEDICO_TRIAGE;
    }
    
    public boolean puedeRegistrarPacientes() {
        return tipoUsuario == TipoUsuario.ASISTENTE_MEDICA;
    }
    
    public boolean puedeRealizarEntrevistaSocial() {
        return tipoUsuario == TipoUsuario.TRABAJADOR_SOCIAL;
    }
    
    public boolean puedeAtenderUrgencias() {
        return tipoUsuario == TipoUsuario.MEDICO_URGENCIAS;
    }
    
    public boolean puedeVerEstadisticas() {
        return tipoUsuario == TipoUsuario.ADMINISTRADOR || 
               tipoUsuario == TipoUsuario.MEDICO_URGENCIAS;
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", tipoUsuario=" + tipoUsuario +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", especialidad='" + especialidad + '\'' +
                ", activo=" + activo +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Usuario usuario = (Usuario) obj;
        return id == usuario.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}