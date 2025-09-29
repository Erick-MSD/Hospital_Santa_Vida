package structures;

import models.Especialidad;
import models.Usuario;
import java.util.*;
import java.time.LocalTime;

/**
 * HashMap especializado para gestionar especialidades médicas y médicos disponibles
 * Proporciona mapeo rápido O(1) de especialidades a listas de médicos
 * Incluye funcionalidad para horarios, disponibilidad y asignación automática
 */
public class EspecialidadHashMap {
    
    /**
     * Clase interna que representa información detallada de un médico especialista
     */
    public static class MedicoEspecialista {
        private Usuario medico;
        private boolean disponible;
        private LocalTime horaInicioTurno;
        private LocalTime horaFinTurno;
        private int pacientesAsignados;
        private int capacidadMaxima;
        private double calificacion;
        private List<String> subespecialidades;
        
        public MedicoEspecialista(Usuario medico) {
            this.medico = medico;
            this.disponible = true;
            this.horaInicioTurno = LocalTime.of(8, 0);  // 8:00 AM por defecto
            this.horaFinTurno = LocalTime.of(18, 0);    // 6:00 PM por defecto
            this.pacientesAsignados = 0;
            this.capacidadMaxima = 8; // 8 pacientes por turno por defecto
            this.calificacion = 5.0;
            this.subespecialidades = new ArrayList<>();
        }
        
        // Getters y Setters
        public Usuario getMedico() { return medico; }
        public void setMedico(Usuario medico) { this.medico = medico; }
        
        public boolean isDisponible() { return disponible; }
        public void setDisponible(boolean disponible) { this.disponible = disponible; }
        
        public LocalTime getHoraInicioTurno() { return horaInicioTurno; }
        public void setHoraInicioTurno(LocalTime horaInicioTurno) { this.horaInicioTurno = horaInicioTurno; }
        
        public LocalTime getHoraFinTurno() { return horaFinTurno; }
        public void setHoraFinTurno(LocalTime horaFinTurno) { this.horaFinTurno = horaFinTurno; }
        
        public int getPacientesAsignados() { return pacientesAsignados; }
        public void setPacientesAsignados(int pacientesAsignados) { this.pacientesAsignados = pacientesAsignados; }
        
        public int getCapacidadMaxima() { return capacidadMaxima; }
        public void setCapacidadMaxima(int capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }
        
        public double getCalificacion() { return calificacion; }
        public void setCalificacion(double calificacion) { this.calificacion = calificacion; }
        
        public List<String> getSubespecialidades() { return subespecialidades; }
        public void setSubespecialidades(List<String> subespecialidades) { this.subespecialidades = subespecialidades; }
        
        // Métodos de utilidad
        public boolean estaEnTurno() {
            LocalTime ahora = LocalTime.now();
            return !ahora.isBefore(horaInicioTurno) && !ahora.isAfter(horaFinTurno);
        }
        
        public boolean tieneCupo() {
            return pacientesAsignados < capacidadMaxima;
        }
        
        public boolean estaDisponibleParaAsignacion() {
            return disponible && estaEnTurno() && tieneCupo();
        }
        
        public void asignarPaciente() {
            if (tieneCupo()) {
                pacientesAsignados++;
            }
        }
        
        public void liberarPaciente() {
            if (pacientesAsignados > 0) {
                pacientesAsignados--;
            }
        }
        
        public int getCuposDisponibles() {
            return Math.max(0, capacidadMaxima - pacientesAsignados);
        }
        
        @Override
        public String toString() {
            return medico.getNombreCompleto() + 
                   " (Disponible: " + disponible + 
                   ", Cupos: " + getCuposDisponibles() + "/" + capacidadMaxima + 
                   ", Turno: " + horaInicioTurno + "-" + horaFinTurno + ")";
        }
    }
    
    // HashMap principal: Especialidad -> Lista de médicos especialistas
    private Map<Especialidad, List<MedicoEspecialista>> especialidades;
    
    // HashMap secundario para búsqueda rápida por nombre de especialidad
    private Map<String, Especialidad> nombreAEspecialidad;
    
    // Estadísticas y métricas
    private Map<Especialidad, Integer> contadorAsignaciones;
    private Map<Especialidad, Double> tiempoPromedioAtencion;
    
    /**
     * Constructor que inicializa el HashMap con todas las especialidades
     */
    public EspecialidadHashMap() {
        this.especialidades = new HashMap<>();
        this.nombreAEspecialidad = new HashMap<>();
        this.contadorAsignaciones = new HashMap<>();
        this.tiempoPromedioAtencion = new HashMap<>();
        
        inicializarEspecialidades();
    }
    
    /**
     * Inicializa todas las especialidades con listas vacías
     */
    private void inicializarEspecialidades() {
        for (Especialidad esp : Especialidad.values()) {
            especialidades.put(esp, new ArrayList<>());
            nombreAEspecialidad.put(esp.getNombre().toLowerCase(), esp);
            contadorAsignaciones.put(esp, 0);
            tiempoPromedioAtencion.put(esp, 30.0); // 30 minutos por defecto
        }
    }
    
    /**
     * Agrega un médico a una especialidad específica
     * @param especialidad La especialidad médica
     * @param medico El usuario médico
     * @return true si se agregó exitosamente
     */
    public boolean agregarMedico(Especialidad especialidad, Usuario medico) {
        if (especialidad == null || medico == null) {
            return false;
        }
        
        List<MedicoEspecialista> medicos = especialidades.get(especialidad);
        if (medicos == null) {
            medicos = new ArrayList<>();
            especialidades.put(especialidad, medicos);
        }
        
        // Verificar si el médico ya está en la especialidad
        boolean yaExiste = medicos.stream()
                .anyMatch(me -> me.getMedico().getId() == medico.getId());
        
        if (!yaExiste) {
            medicos.add(new MedicoEspecialista(medico));
            return true;
        }
        
        return false;
    }
    
    /**
     * Agrega un médico por nombre de especialidad
     * @param nombreEspecialidad Nombre de la especialidad
     * @param medico El usuario médico
     * @return true si se agregó exitosamente
     */
    public boolean agregarMedico(String nombreEspecialidad, Usuario medico) {
        Especialidad especialidad = buscarEspecialidad(nombreEspecialidad);
        return especialidad != null && agregarMedico(especialidad, medico);
    }
    
    /**
     * Obtiene todos los médicos de una especialidad
     * @param especialidad La especialidad médica
     * @return Lista de médicos especialistas
     */
    public List<MedicoEspecialista> obtenerMedicos(Especialidad especialidad) {
        return new ArrayList<>(especialidades.getOrDefault(especialidad, new ArrayList<>()));
    }
    
    /**
     * Obtiene médicos disponibles para asignación de una especialidad
     * @param especialidad La especialidad médica
     * @return Lista de médicos disponibles
     */
    public List<MedicoEspecialista> obtenerMedicosDisponibles(Especialidad especialidad) {
        List<MedicoEspecialista> medicos = especialidades.get(especialidad);
        if (medicos == null) return new ArrayList<>();
        
        return medicos.stream()
                .filter(MedicoEspecialista::estaDisponibleParaAsignacion)
                .sorted((m1, m2) -> {
                    // Ordenar por: 1) Menos pacientes asignados, 2) Mayor calificación
                    int comparacionCarga = Integer.compare(m1.getPacientesAsignados(), m2.getPacientesAsignados());
                    if (comparacionCarga != 0) return comparacionCarga;
                    return Double.compare(m2.getCalificacion(), m1.getCalificacion());
                })
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Obtiene todos los médicos de una especialidad como Usuario
     * @param especialidad La especialidad médica
     * @return Lista de usuarios médicos
     */
    public List<Usuario> obtenerMedicosComoUsuario(Especialidad especialidad) {
        List<MedicoEspecialista> medicos = obtenerMedicos(especialidad);
        List<Usuario> usuarios = new ArrayList<>();
        for (MedicoEspecialista med : medicos) {
            usuarios.add(med.getMedico());
        }
        return usuarios;
    }
    
    /**
     * Asigna un médico a una especialidad
     * @param especialidad La especialidad
     * @param medico El médico usuario a asignar
     */
    public void asignarMedico(Especialidad especialidad, Usuario medico) {
        List<MedicoEspecialista> medicos = especialidades.computeIfAbsent(especialidad, _ -> new ArrayList<>());
        
        // Verificar si el médico ya está asignado
        boolean yaAsignado = medicos.stream()
                .anyMatch(m -> m.getMedico().getId() == medico.getId());
        
        if (!yaAsignado) {
            medicos.add(new MedicoEspecialista(medico));
        }
    }
    
    /**
     * Asigna automáticamente el mejor médico disponible para una especialidad
     * @param especialidad La especialidad requerida
     * @return El médico asignado, o null si no hay disponibles
     */
    public MedicoEspecialista asignarMedicoAutomatico(Especialidad especialidad) {
        List<MedicoEspecialista> disponibles = obtenerMedicosDisponibles(especialidad);
        
        if (!disponibles.isEmpty()) {
            MedicoEspecialista asignado = disponibles.get(0);
            asignado.asignarPaciente();
            
            // Actualizar estadísticas
            contadorAsignaciones.put(especialidad, contadorAsignaciones.get(especialidad) + 1);
            
            return asignado;
        }
        
        return null;
    }
    
    /**
     * Busca una especialidad por nombre (case-insensitive)
     * @param nombre Nombre de la especialidad
     * @return La especialidad encontrada, o null si no existe
     */
    public Especialidad buscarEspecialidad(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return null;
        }
        
        return nombreAEspecialidad.get(nombre.toLowerCase().trim());
    }
    
    /**
     * Obtiene todas las especialidades con médicos disponibles
     * @return Lista de especialidades con al menos un médico disponible
     */
    public List<Especialidad> obtenerEspecialidadesConMedicosDisponibles() {
        List<Especialidad> disponibles = new ArrayList<>();
        
        for (Especialidad esp : especialidades.keySet()) {
            if (!obtenerMedicosDisponibles(esp).isEmpty()) {
                disponibles.add(esp);
            }
        }
        
        return disponibles;
    }
    
    /**
     * Configura la disponibilidad de un médico específico
     * @param medicoId ID del médico
     * @param disponible true para disponible, false para no disponible
     * @return true si se actualizó exitosamente
     */
    public boolean configurarDisponibilidadMedico(int medicoId, boolean disponible) {
        for (List<MedicoEspecialista> medicos : especialidades.values()) {
            for (MedicoEspecialista me : medicos) {
                if (me.getMedico().getId() == medicoId) {
                    me.setDisponible(disponible);
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Configura el horario de un médico específico
     * @param medicoId ID del médico
     * @param horaInicio Hora de inicio del turno
     * @param horaFin Hora de fin del turno
     * @return true si se configuró exitosamente
     */
    public boolean configurarHorarioMedico(int medicoId, LocalTime horaInicio, LocalTime horaFin) {
        for (List<MedicoEspecialista> medicos : especialidades.values()) {
            for (MedicoEspecialista me : medicos) {
                if (me.getMedico().getId() == medicoId) {
                    me.setHoraInicioTurno(horaInicio);
                    me.setHoraFinTurno(horaFin);
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Libera un paciente de un médico específico (cuando termina la consulta)
     * @param medicoId ID del médico
     * @return true si se liberó exitosamente
     */
    public boolean liberarPacienteDeMedico(int medicoId) {
        for (List<MedicoEspecialista> medicos : especialidades.values()) {
            for (MedicoEspecialista me : medicos) {
                if (me.getMedico().getId() == medicoId) {
                    me.liberarPaciente();
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Obtiene estadísticas detalladas por especialidad
     * @return Map con estadísticas completas
     */
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        int totalMedicos = 0;
        int medicosDisponibles = 0;
        int totalAsignaciones = 0;
        
        for (Especialidad esp : Especialidad.values()) {
            List<MedicoEspecialista> medicos = especialidades.get(esp);
            int medicosEsp = medicos.size();
            long disponiblesEsp = medicos.stream()
                    .filter(MedicoEspecialista::estaDisponibleParaAsignacion)
                    .count();
            int asignacionesEsp = contadorAsignaciones.get(esp);
            
            totalMedicos += medicosEsp;
            medicosDisponibles += disponiblesEsp;
            totalAsignaciones += asignacionesEsp;
            
            Map<String, Object> infoEspecialidad = new HashMap<>();
            infoEspecialidad.put("total_medicos", medicosEsp);
            infoEspecialidad.put("medicos_disponibles", disponiblesEsp);
            infoEspecialidad.put("total_asignaciones", asignacionesEsp);
            infoEspecialidad.put("tiempo_promedio_atencion", tiempoPromedioAtencion.get(esp));
            
            if (medicosEsp > 0) {
                double cargaPromedio = medicos.stream()
                        .mapToInt(MedicoEspecialista::getPacientesAsignados)
                        .average().orElse(0.0);
                infoEspecialidad.put("carga_promedio_medicos", cargaPromedio);
            }
            
            estadisticas.put(esp.getNombre(), infoEspecialidad);
        }
        
        // Estadísticas generales
        estadisticas.put("resumen_total_medicos", totalMedicos);
        estadisticas.put("resumen_medicos_disponibles", medicosDisponibles);
        estadisticas.put("resumen_total_asignaciones", totalAsignaciones);
        
        // Especialidad más demandada
        Especialidad masDemandada = contadorAsignaciones.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        
        estadisticas.put("especialidad_mas_demandada", 
                        masDemandada != null ? masDemandada.getNombre() : "N/A");
        
        return estadisticas;
    }
    
    /**
     * Obtiene información de carga de trabajo por especialidad
     * @return Map con información de carga
     */
    public Map<Especialidad, Double> obtenerCargaTrabajo() {
        Map<Especialidad, Double> carga = new HashMap<>();
        
        for (Especialidad esp : especialidades.keySet()) {
            List<MedicoEspecialista> medicos = especialidades.get(esp);
            
            if (!medicos.isEmpty()) {
                double cargaPromedio = medicos.stream()
                        .mapToDouble(me -> (double) me.getPacientesAsignados() / me.getCapacidadMaxima() * 100)
                        .average().orElse(0.0);
                carga.put(esp, cargaPromedio);
            } else {
                carga.put(esp, 0.0);
            }
        }
        
        return carga;
    }
    
    /**
     * Reinicia las asignaciones de todos los médicos (nuevo turno)
     */
    public void reiniciarAsignaciones() {
        for (List<MedicoEspecialista> medicos : especialidades.values()) {
            for (MedicoEspecialista me : medicos) {
                me.setPacientesAsignados(0);
            }
        }
        
        // Reiniciar contadores de asignaciones
        for (Especialidad esp : contadorAsignaciones.keySet()) {
            contadorAsignaciones.put(esp, 0);
        }
    }
    
    /**
     * Obtiene recomendaciones de especialidad basadas en síntomas
     * @param sintomas Lista de síntomas del paciente
     * @return Lista de especialidades recomendadas ordenadas por relevancia
     */
    public List<Especialidad> recomendarEspecialidades(List<String> sintomas) {
        // Implementación básica de recomendación basada en palabras clave
        Map<Especialidad, Integer> puntuaciones = new HashMap<>();
        
        for (Especialidad esp : Especialidad.values()) {
            puntuaciones.put(esp, 0);
        }
        
        // Lógica simple de matching por palabras clave
        for (String sintoma : sintomas) {
            String s = sintoma.toLowerCase();
            
            // Cardiología
            if (s.contains("corazón") || s.contains("pecho") || s.contains("cardiaco")) {
                puntuaciones.put(Especialidad.CARDIOLOGIA, puntuaciones.get(Especialidad.CARDIOLOGIA) + 3);
            }
            
            // Neurología
            if (s.contains("cabeza") || s.contains("mareo") || s.contains("neurológico")) {
                puntuaciones.put(Especialidad.NEUROLOGIA, puntuaciones.get(Especialidad.NEUROLOGIA) + 3);
            }
            
            // Neumología
            if (s.contains("respiración") || s.contains("pulmón") || s.contains("tos")) {
                puntuaciones.put(Especialidad.NEUMOLOGIA, puntuaciones.get(Especialidad.NEUMOLOGIA) + 3);
            }
            
            // Añadir más reglas según necesidades...
        }
        
        // Ordenar por puntuación y filtrar especialidades con médicos disponibles
        return puntuaciones.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .filter(entry -> !obtenerMedicosDisponibles(entry.getKey()).isEmpty())
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .map(Map.Entry::getKey)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EspecialidadHashMap - ").append(Especialidad.values().length).append(" especialidades:\\n");
        
        for (Especialidad esp : Especialidad.values()) {
            List<MedicoEspecialista> medicos = especialidades.get(esp);
            long disponibles = medicos.stream().filter(MedicoEspecialista::estaDisponibleParaAsignacion).count();
            
            sb.append("  ").append(esp.getNombre())
              .append(": ").append(medicos.size()).append(" médicos")
              .append(" (").append(disponibles).append(" disponibles)")
              .append("\\n");
        }
        
        return sb.toString();
    }
}