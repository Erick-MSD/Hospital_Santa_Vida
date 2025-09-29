package services;

import models.RegistroTriage;
import models.NivelUrgencia;

/**
 * Clases de resultado para TriageService
 * Encapsulan los resultados de las operaciones del servicio de triage
 */
public class TriageServiceResults {
    
    /**
     * Resultado de evaluación de triage
     */
    public static class ResultadoEvaluacion {
        private final boolean exitoso;
        private final String mensaje;
        private final RegistroTriage registroTriage;
        private final NivelUrgencia nivelAsignado;
        private final int posicionEnCola;
        private final String recomendaciones;
        
        public ResultadoEvaluacion(boolean exitoso, String mensaje, RegistroTriage registroTriage, 
                                  NivelUrgencia nivelAsignado, int posicionEnCola, String recomendaciones) {
            this.exitoso = exitoso;
            this.mensaje = mensaje;
            this.registroTriage = registroTriage;
            this.nivelAsignado = nivelAsignado;
            this.posicionEnCola = posicionEnCola;
            this.recomendaciones = recomendaciones;
        }
        
        public boolean isExitoso() { return exitoso; }
        public String getMensaje() { return mensaje; }
        public RegistroTriage getRegistroTriage() { return registroTriage; }
        public NivelUrgencia getNivelAsignado() { return nivelAsignado; }
        public int getPosicionEnCola() { return posicionEnCola; }
        public String getRecomendaciones() { return recomendaciones; }
        
        // Métodos adicionales para compatibilidad con controladores
        public String getJustificacion() { return recomendaciones; } // Alias
    }
    
    /**
     * Datos para evaluación de triage
     */
    public static class DatosEvaluacionTriage {
        private int pacienteId;
        private int usuarioTriageId;
        private String signosVitales;
        private String presionArterial;
        private double temperatura;
        private int frecuenciaCardiaca;
        private int frecuenciaRespiratoria;
        private double saturacionOxigeno;
        private String motivoConsulta;
        private String sintomasPrincipales;
        private String alergias;
        private String medicamentosActuales;
        private String antecedentesRelevantes;
        private String dolorEscala;
        private String estadoConciencia;
        private String observaciones;
        private NivelUrgencia nivelUrgenciaSugerido;
        private boolean requiereAtencionInmediata;
        private String tiempoEsperaEstimado;
        
        public DatosEvaluacionTriage() {}
        
        // Getters y Setters
        public int getPacienteId() { return pacienteId; }
        public void setPacienteId(int pacienteId) { this.pacienteId = pacienteId; }
        
        public int getUsuarioTriageId() { return usuarioTriageId; }
        public void setUsuarioTriageId(int usuarioTriageId) { this.usuarioTriageId = usuarioTriageId; }
        
        public String getSignosVitales() { return signosVitales; }
        public void setSignosVitales(String signosVitales) { this.signosVitales = signosVitales; }
        
        public String getPresionArterial() { return presionArterial; }
        public void setPresionArterial(String presionArterial) { this.presionArterial = presionArterial; }
        
        public double getTemperatura() { return temperatura; }
        public void setTemperatura(double temperatura) { this.temperatura = temperatura; }
        
        public int getFrecuenciaCardiaca() { return frecuenciaCardiaca; }
        public void setFrecuenciaCardiaca(int frecuenciaCardiaca) { this.frecuenciaCardiaca = frecuenciaCardiaca; }
        
        public int getFrecuenciaRespiratoria() { return frecuenciaRespiratoria; }
        public void setFrecuenciaRespiratoria(int frecuenciaRespiratoria) { this.frecuenciaRespiratoria = frecuenciaRespiratoria; }
        
        public double getSaturacionOxigeno() { return saturacionOxigeno; }
        public void setSaturacionOxigeno(double saturacionOxigeno) { this.saturacionOxigeno = saturacionOxigeno; }
        
        public String getMotivoConsulta() { return motivoConsulta; }
        public void setMotivoConsulta(String motivoConsulta) { this.motivoConsulta = motivoConsulta; }
        
        public String getSintomasPrincipales() { return sintomasPrincipales; }
        public void setSintomasPrincipales(String sintomasPrincipales) { this.sintomasPrincipales = sintomasPrincipales; }
        
        public String getAlergias() { return alergias; }
        public void setAlergias(String alergias) { this.alergias = alergias; }
        
        public String getMedicamentosActuales() { return medicamentosActuales; }
        public void setMedicamentosActuales(String medicamentosActuales) { this.medicamentosActuales = medicamentosActuales; }
        
        public String getAntecedentesRelevantes() { return antecedentesRelevantes; }
        public void setAntecedentesRelevantes(String antecedentesRelevantes) { this.antecedentesRelevantes = antecedentesRelevantes; }
        
        public String getDolorEscala() { return dolorEscala; }
        public void setDolorEscala(String dolorEscala) { this.dolorEscala = dolorEscala; }
        
        public String getEstadoConciencia() { return estadoConciencia; }
        public void setEstadoConciencia(String estadoConciencia) { this.estadoConciencia = estadoConciencia; }
        
        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
        
        public NivelUrgencia getNivelUrgenciaSugerido() { return nivelUrgenciaSugerido; }
        public void setNivelUrgenciaSugerido(NivelUrgencia nivelUrgenciaSugerido) { this.nivelUrgenciaSugerido = nivelUrgenciaSugerido; }
        
        public boolean isRequiereAtencionInmediata() { return requiereAtencionInmediata; }
        public void setRequiereAtencionInmediata(boolean requiereAtencionInmediata) { this.requiereAtencionInmediata = requiereAtencionInmediata; }
        
        public String getTiempoEsperaEstimado() { return tiempoEsperaEstimado; }
        public void setTiempoEsperaEstimado(String tiempoEsperaEstimado) { this.tiempoEsperaEstimado = tiempoEsperaEstimado; }
        
        // Métodos adicionales para compatibilidad con controladores
        public void setPresionSistolica(Integer sistolica) {
            this.presionArterial = sistolica + "/" + (this.presionArterial != null && this.presionArterial.contains("/") ? 
                this.presionArterial.split("/")[1] : "80");
        }
        
        public void setPresionDiastolica(Integer diastolica) {
            this.presionArterial = (this.presionArterial != null && this.presionArterial.contains("/") ? 
                this.presionArterial.split("/")[0] : "120") + "/" + diastolica;
        }
        
        public void setNivelDolor(int nivel) {
            this.dolorEscala = String.valueOf(nivel);
        }
        
        public void setEscalaGlasgow(int escala) {
            this.estadoConciencia = "Glasgow: " + escala;
        }
    }
}