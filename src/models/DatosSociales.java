package models;

import java.time.LocalDateTime;

/**
 * Modelo de datos para información social de pacientes
 * Capturada por trabajadores sociales para antecedentes médicos familiares
 */
public class DatosSociales {
    
    // Atributos principales
    private int id;
    private int registroTriageId;
    private int trabajadorSocialId;
    private LocalDateTime fechaHoraEntrevista;
    
    // Información médica y social
    private String antecedentesFamiliares;
    private String enfermedadesCronicas;
    private String medicamentosActuales;
    private String alergiasConocidas;
    private String cirugiasPrevias;
    private String hospitalizacionesPrevias;
    private String vacunasRecientes;
    private String habitosToxicos;
    private String situacionSocioeconomica;
    private String observacionesAdicionales;
    
    // Referencias a otros objetos
    private RegistroTriage registroTriage;
    private Usuario trabajadorSocial;
    
    // Constructores
    public DatosSociales() {
        this.fechaHoraEntrevista = LocalDateTime.now();
    }
    
    public DatosSociales(int registroTriageId, int trabajadorSocialId) {
        this();
        this.registroTriageId = registroTriageId;
        this.trabajadorSocialId = trabajadorSocialId;
    }
    
    // Getters y Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getRegistroTriageId() {
        return registroTriageId;
    }
    
    public void setRegistroTriageId(int registroTriageId) {
        this.registroTriageId = registroTriageId;
    }
    
    public int getTrabajadorSocialId() {
        return trabajadorSocialId;
    }
    
    public void setTrabajadorSocialId(int trabajadorSocialId) {
        this.trabajadorSocialId = trabajadorSocialId;
    }
    
    public LocalDateTime getFechaHoraEntrevista() {
        return fechaHoraEntrevista;
    }
    
    public void setFechaHoraEntrevista(LocalDateTime fechaHoraEntrevista) {
        this.fechaHoraEntrevista = fechaHoraEntrevista;
    }
    
    public String getAntecedentesFamiliares() {
        return antecedentesFamiliares;
    }
    
    public void setAntecedentesFamiliares(String antecedentesFamiliares) {
        this.antecedentesFamiliares = antecedentesFamiliares;
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
    
    public String getAlergiasConocidas() {
        return alergiasConocidas;
    }
    
    public void setAlergiasConocidas(String alergiasConocidas) {
        this.alergiasConocidas = alergiasConocidas;
    }
    
    public String getCirugiasPrevias() {
        return cirugiasPrevias;
    }
    
    public void setCirugiasPrevias(String cirugiasPrevias) {
        this.cirugiasPrevias = cirugiasPrevias;
    }
    
    public String getHospitalizacionesPrevias() {
        return hospitalizacionesPrevias;
    }
    
    public void setHospitalizacionesPrevias(String hospitalizacionesPrevias) {
        this.hospitalizacionesPrevias = hospitalizacionesPrevias;
    }
    
    public String getVacunasRecientes() {
        return vacunasRecientes;
    }
    
    public void setVacunasRecientes(String vacunasRecientes) {
        this.vacunasRecientes = vacunasRecientes;
    }
    
    public String getHabitosToxicos() {
        return habitosToxicos;
    }
    
    public void setHabitosToxicos(String habitosToxicos) {
        this.habitosToxicos = habitosToxicos;
    }
    
    public String getSituacionSocioeconomica() {
        return situacionSocioeconomica;
    }
    
    public void setSituacionSocioeconomica(String situacionSocioeconomica) {
        this.situacionSocioeconomica = situacionSocioeconomica;
    }
    
    public String getObservacionesAdicionales() {
        return observacionesAdicionales;
    }
    
    public void setObservacionesAdicionales(String observacionesAdicionales) {
        this.observacionesAdicionales = observacionesAdicionales;
    }
    
    public RegistroTriage getRegistroTriage() {
        return registroTriage;
    }
    
    public void setRegistroTriage(RegistroTriage registroTriage) {
        this.registroTriage = registroTriage;
    }
    
    public Usuario getTrabajadorSocial() {
        return trabajadorSocial;
    }
    
    public void setTrabajadorSocial(Usuario trabajadorSocial) {
        this.trabajadorSocial = trabajadorSocial;
    }
    
    // Métodos de utilidad
    public boolean tieneAntecedentesRelevantes() {
        return (antecedentesFamiliares != null && !antecedentesFamiliares.trim().isEmpty()) ||
               (enfermedadesCronicas != null && !enfermedadesCronicas.trim().isEmpty()) ||
               (alergiasConocidas != null && !alergiasConocidas.trim().isEmpty());
    }
    
    public boolean tomaMedicamentos() {
        return medicamentosActuales != null && !medicamentosActuales.trim().isEmpty() &&
               !medicamentosActuales.toLowerCase().contains("ninguno") &&
               !medicamentosActuales.toLowerCase().contains("no toma");
    }
    
    public boolean tieneHabitosToxicos() {
        return habitosToxicos != null && !habitosToxicos.trim().isEmpty() &&
               !habitosToxicos.toLowerCase().contains("ninguno") &&
               !habitosToxicos.toLowerCase().contains("no tiene");
    }
    
    public boolean tieneCirugiasPrevias() {
        return cirugiasPrevias != null && !cirugiasPrevias.trim().isEmpty() &&
               !cirugiasPrevias.toLowerCase().contains("ninguna") &&
               !cirugiasPrevias.toLowerCase().contains("no ha tenido");
    }
    
    public boolean entrevistaCompleta() {
        // Verificar que al menos los campos esenciales tengan información
        return antecedentesFamiliares != null && !antecedentesFamiliares.trim().isEmpty() &&
               medicamentosActuales != null && !medicamentosActuales.trim().isEmpty() &&
               alergiasConocidas != null && !alergiasConocidas.trim().isEmpty();
    }
    
    public String getResumenAntecedentes() {
        StringBuilder resumen = new StringBuilder();
        
        if (tieneAntecedentesRelevantes()) {
            resumen.append("ANTECEDENTES: ").append(antecedentesFamiliares).append(" | ");
        }
        
        if (tomaMedicamentos()) {
            resumen.append("MEDICAMENTOS: ").append(medicamentosActuales).append(" | ");
        }
        
        if (alergiasConocidas != null && !alergiasConocidas.trim().isEmpty()) {
            resumen.append("ALERGIAS: ").append(alergiasConocidas).append(" | ");
        }
        
        if (tieneHabitosToxicos()) {
            resumen.append("HÁBITOS: ").append(habitosToxicos);
        }
        
        return resumen.toString();
    }
    
    @Override
    public String toString() {
        return "DatosSociales{" +
                "id=" + id +
                ", registroTriageId=" + registroTriageId +
                ", fechaEntrevista=" + fechaHoraEntrevista +
                ", entrevistaCompleta=" + entrevistaCompleta() +
                ", tieneAntecedentes=" + tieneAntecedentesRelevantes() +
                ", tomaMedicamentos=" + tomaMedicamentos() +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DatosSociales datos = (DatosSociales) obj;
        return id == datos.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}