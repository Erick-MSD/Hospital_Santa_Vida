package models;

import java.time.LocalDateTime;

/**
 * Modelo para evaluaciones de triage
 */
public class EvaluacionTriage {
    private int id;
    private String numeroFolio;
    private LocalDateTime fechaIngreso;
    private String motivoConsulta;
    private String presionArterial;
    private Integer frecuenciaCardiaca;
    private Double temperatura;
    private Integer frecuenciaRespiratoria;
    private Integer saturacionO2;
    private Integer nivelDolor;
    private String observacionesClinicas;
    private String nivelTriage;  // ROJO, NARANJA, AMARILLO, VERDE, AZUL
    private String especialidad;
    private int doctorId;
    private LocalDateTime fechaCreacion;

    // Constructor vacío
    public EvaluacionTriage() {}

    // Constructor completo
    public EvaluacionTriage(String numeroFolio, LocalDateTime fechaIngreso, String motivoConsulta,
                           String presionArterial, Integer frecuenciaCardiaca, Double temperatura,
                           Integer frecuenciaRespiratoria, Integer saturacionO2, Integer nivelDolor,
                           String observacionesClinicas, String nivelTriage, String especialidad, int doctorId) {
        this.numeroFolio = numeroFolio;
        this.fechaIngreso = fechaIngreso;
        this.motivoConsulta = motivoConsulta;
        this.presionArterial = presionArterial;
        this.frecuenciaCardiaca = frecuenciaCardiaca;
        this.temperatura = temperatura;
        this.frecuenciaRespiratoria = frecuenciaRespiratoria;
        this.saturacionO2 = saturacionO2;
        this.nivelDolor = nivelDolor;
        this.observacionesClinicas = observacionesClinicas;
        this.nivelTriage = nivelTriage;
        this.especialidad = especialidad;
        this.doctorId = doctorId;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroFolio() { return numeroFolio; }
    public void setNumeroFolio(String numeroFolio) { this.numeroFolio = numeroFolio; }

    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public String getMotivoConsulta() { return motivoConsulta; }
    public void setMotivoConsulta(String motivoConsulta) { this.motivoConsulta = motivoConsulta; }

    public String getPresionArterial() { return presionArterial; }
    public void setPresionArterial(String presionArterial) { this.presionArterial = presionArterial; }

    public Integer getFrecuenciaCardiaca() { return frecuenciaCardiaca; }
    public void setFrecuenciaCardiaca(Integer frecuenciaCardiaca) { this.frecuenciaCardiaca = frecuenciaCardiaca; }

    public Double getTemperatura() { return temperatura; }
    public void setTemperatura(Double temperatura) { this.temperatura = temperatura; }

    public Integer getFrecuenciaRespiratoria() { return frecuenciaRespiratoria; }
    public void setFrecuenciaRespiratoria(Integer frecuenciaRespiratoria) { this.frecuenciaRespiratoria = frecuenciaRespiratoria; }

    public Integer getSaturacionO2() { return saturacionO2; }
    public void setSaturacionO2(Integer saturacionO2) { this.saturacionO2 = saturacionO2; }

    public Integer getNivelDolor() { return nivelDolor; }
    public void setNivelDolor(Integer nivelDolor) { this.nivelDolor = nivelDolor; }

    public String getObservacionesClinicas() { return observacionesClinicas; }
    public void setObservacionesClinicas(String observacionesClinicas) { this.observacionesClinicas = observacionesClinicas; }

    public String getNivelTriage() { return nivelTriage; }
    public void setNivelTriage(String nivelTriage) { this.nivelTriage = nivelTriage; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    @Override
    public String toString() {
        return "EvaluacionTriage{" +
                "id=" + id +
                ", numeroFolio='" + numeroFolio + '\'' +
                ", fechaIngreso=" + fechaIngreso +
                ", motivoConsulta='" + motivoConsulta + '\'' +
                ", nivelTriage='" + nivelTriage + '\'' +
                ", especialidad='" + especialidad + '\'' +
                '}';
    }
}