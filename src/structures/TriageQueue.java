package structures;

import models.RegistroTriage;
import models.NivelUrgencia;
import java.util.*;
import java.time.LocalDateTime;

/**
 * Cola de prioridad especializada para el sistema de triage hospitalario
 * Implementa una cola que prioriza por nivel de urgencia y tiempo de llegada
 * ROJO > NARANJA > AMARILLO > VERDE > AZUL
 * Dentro del mismo nivel, se aplica FIFO (First In, First Out)
 */
public class TriageQueue {
    
    // Cola de prioridad principal usando un comparador personalizado
    private PriorityQueue<RegistroTriage> cola;
    
    // Mapas para acceso rápido por nivel y estadísticas
    private Map<NivelUrgencia, List<RegistroTriage>> porNivel;
    private Map<String, RegistroTriage> porFolio;
    
    // Estadísticas de la cola
    private int totalPacientes;
    private LocalDateTime ultimaActualizacion;
    
    /**
     * Constructor que inicializa la cola con el comparador de prioridad
     */
    public TriageQueue() {
        // Comparador personalizado: primero por prioridad (menor número = mayor prioridad),
        // luego por tiempo de llegada (más temprano = mayor prioridad)
        this.cola = new PriorityQueue<>((r1, r2) -> {
            // Comparar primero por prioridad de urgencia
            int prioridadComparacion = Integer.compare(
                r1.getNivelUrgencia().getPrioridad(), 
                r2.getNivelUrgencia().getPrioridad()
            );
            
            if (prioridadComparacion != 0) {
                return prioridadComparacion;
            }
            
            // Si tienen la misma prioridad, el que llegó primero tiene prioridad
            return r1.getFechaHoraLlegada().compareTo(r2.getFechaHoraLlegada());
        });
        
        this.porNivel = new HashMap<>();
        this.porFolio = new HashMap<>();
        
        // Inicializar listas por nivel
        for (NivelUrgencia nivel : NivelUrgencia.values()) {
            porNivel.put(nivel, new ArrayList<>());
        }
        
        this.totalPacientes = 0;
        this.ultimaActualizacion = LocalDateTime.now();
    }
    
    /**
     * Añade un paciente a la cola de triage
     * @param registro El registro de triage del paciente
     */
    public void encolar(RegistroTriage registro) {
        if (registro == null || registro.getNivelUrgencia() == null) {
            throw new IllegalArgumentException("El registro y su nivel de urgencia no pueden ser nulos");
        }
        
        cola.offer(registro);
        porNivel.get(registro.getNivelUrgencia()).add(registro);
        porFolio.put(registro.getFolio(), registro);
        
        totalPacientes++;
        ultimaActualizacion = LocalDateTime.now();
        
        // Actualizar orden de prioridad para colas internas
        actualizarPrioridades();
    }
    
    /**
     * Extrae el paciente con mayor prioridad de la cola
     * @return El registro de triage del paciente con mayor prioridad, o null si está vacía
     */
    public RegistroTriage desencolar() {
        RegistroTriage registro = cola.poll();
        
        if (registro != null) {
            porNivel.get(registro.getNivelUrgencia()).remove(registro);
            porFolio.remove(registro.getFolio());
            totalPacientes--;
            ultimaActualizacion = LocalDateTime.now();
        }
        
        return registro;
    }
    
    /**
     * Ve el siguiente paciente sin removerlo de la cola
     * @return El registro de triage del próximo paciente, o null si está vacía
     */
    public RegistroTriage verSiguiente() {
        return cola.peek();
    }
    
    /**
     * Busca un paciente por su folio
     * @param folio El folio del paciente
     * @return El registro de triage, o null si no se encuentra
     */
    public RegistroTriage buscarPorFolio(String folio) {
        return porFolio.get(folio);
    }
    
    /**
     * Obtiene todos los pacientes de un nivel específico de urgencia
     * @param nivel El nivel de urgencia
     * @return Lista de registros de ese nivel
     */
    public List<RegistroTriage> obtenerPorNivel(NivelUrgencia nivel) {
        return new ArrayList<>(porNivel.get(nivel));
    }
    
    /**
     * Remueve un paciente específico de la cola (cuando pasa a la siguiente etapa)
     * @param folio El folio del paciente a remover
     * @return true si se removió exitosamente, false si no se encontró
     */
    public boolean remover(String folio) {
        RegistroTriage registro = porFolio.get(folio);
        
        if (registro != null) {
            cola.remove(registro);
            porNivel.get(registro.getNivelUrgencia()).remove(registro);
            porFolio.remove(folio);
            totalPacientes--;
            ultimaActualizacion = LocalDateTime.now();
            return true;
        }
        
        return false;
    }
    
    /**
     * Actualiza el nivel de urgencia de un paciente en la cola
     * @param folio El folio del paciente
     * @param nuevoNivel El nuevo nivel de urgencia
     * @return true si se actualizó exitosamente
     */
    public boolean actualizarNivelUrgencia(String folio, NivelUrgencia nuevoNivel) {
        RegistroTriage registro = porFolio.get(folio);
        
        if (registro != null) {
            // Remover del nivel anterior
            porNivel.get(registro.getNivelUrgencia()).remove(registro);
            
            // Actualizar el nivel
            registro.setNivelUrgencia(nuevoNivel);
            
            // Añadir al nuevo nivel
            porNivel.get(nuevoNivel).add(registro);
            
            // Reconstruir la cola para mantener el orden correcto
            reconstruirCola();
            
            ultimaActualizacion = LocalDateTime.now();
            return true;
        }
        
        return false;
    }
    
    /**
     * Obtiene estadísticas de la cola
     * @return Map con las estadísticas actuales
     */
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        estadisticas.put("total_pacientes", totalPacientes);
        estadisticas.put("ultima_actualizacion", ultimaActualizacion);
        
        // Contar por nivel
        for (NivelUrgencia nivel : NivelUrgencia.values()) {
            estadisticas.put("nivel_" + nivel.name().toLowerCase(), porNivel.get(nivel).size());
        }
        
        // Tiempo de espera promedio por nivel
        for (NivelUrgencia nivel : NivelUrgencia.values()) {
            List<RegistroTriage> registros = porNivel.get(nivel);
            if (!registros.isEmpty()) {
                double promedioEspera = registros.stream()
                    .mapToLong(RegistroTriage::getMinutosEspera)
                    .average()
                    .orElse(0.0);
                estadisticas.put("tiempo_promedio_" + nivel.name().toLowerCase(), promedioEspera);
            }
        }
        
        return estadisticas;
    }
    
    /**
     * Obtiene una lista ordenada de todos los pacientes en la cola
     * @return Lista ordenada por prioridad
     */
    public List<RegistroTriage> obtenerTodosOrdenados() {
        List<RegistroTriage> lista = new ArrayList<>(cola);
        return lista;
    }
    
    /**
     * Obtiene los pacientes más urgentes (nivel ROJO)
     * @return Lista de pacientes con nivel ROJO
     */
    public List<RegistroTriage> obtenerUrgentes() {
        return obtenerPorNivel(NivelUrgencia.ROJO);
    }
    
    /**
     * Verifica si la cola está vacía
     * @return true si no hay pacientes en espera
     */
    public boolean estaVacia() {
        return cola.isEmpty();
    }
    
    /**
     * Obtiene el tamaño actual de la cola
     * @return Número de pacientes en espera
     */
    public int tamaño() {
        return totalPacientes;
    }
    
    /**
     * Limpia completamente la cola
     */
    public void limpiar() {
        cola.clear();
        porFolio.clear();
        for (List<RegistroTriage> lista : porNivel.values()) {
            lista.clear();
        }
        totalPacientes = 0;
        ultimaActualizacion = LocalDateTime.now();
    }
    
    /**
     * Actualiza las prioridades numéricas de los registros
     */
    private void actualizarPrioridades() {
        int orden = 1;
        
        // Actualizar orden por prioridad
        for (NivelUrgencia nivel : NivelUrgencia.values()) {
            List<RegistroTriage> registros = porNivel.get(nivel);
            registros.sort((r1, r2) -> r1.getFechaHoraLlegada().compareTo(r2.getFechaHoraLlegada()));
            
            for (RegistroTriage registro : registros) {
                registro.setPrioridadOrden(orden++);
            }
        }
    }
    
    /**
     * Reconstruye la cola manteniendo el orden correcto
     */
    private void reconstruirCola() {
        List<RegistroTriage> temp = new ArrayList<>(cola);
        cola.clear();
        
        for (RegistroTriage registro : temp) {
            cola.offer(registro);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TriageQueue - Total: ").append(totalPacientes).append(" pacientes\\n");
        
        for (NivelUrgencia nivel : NivelUrgencia.values()) {
            int cantidad = porNivel.get(nivel).size();
            if (cantidad > 0) {
                sb.append("  ").append(nivel.name()).append(": ").append(cantidad).append("\\n");
            }
        }
        
        return sb.toString();
    }
    
    // Métodos adicionales para compatibilidad con servicios
    
    /**
     * Alias de encolar para compatibilidad
     */
    public void agregar(RegistroTriage registro) {
        encolar(registro);
    }
    
    /**
     * Obtiene el siguiente paciente sin removerlo (alias de verSiguiente)
     */
    public RegistroTriage siguiente() {
        return verSiguiente();
    }
    
    /**
     * Obtiene el siguiente paciente sin removerlo (alias de siguiente)
     */
    public RegistroTriage obtenerSiguiente() {
        return siguiente();
    }
    
    /**
     * Obtiene todos los registros (alias de obtenerTodosOrdenados)
     */
    public List<RegistroTriage> obtenerTodos() {
        return obtenerTodosOrdenados();
    }
    
    /**
     * Obtiene el tamaño de la cola
     */
    public int size() {
        return totalPacientes;
    }
    
    /**
     * Actualiza un registro existente
     */
    public void actualizar(RegistroTriage registro) {
        if (registro != null && porFolio.containsKey(registro.getFolio())) {
            // Remover y volver a agregar para mantener orden correcto
            remover(registro.getFolio());
            agregar(registro);
        }
    }
    
    /**
     * Remover por ID (convertir int a String)
     */
    public void remover(int registroId) {
        // Buscar por ID y remover
        RegistroTriage aRemover = null;
        for (RegistroTriage registro : cola) {
            if (registro.getId() == registroId) {
                aRemover = registro;
                break;
            }
        }
        if (aRemover != null) {
            remover(aRemover.getFolio());
        }
    }
    
    /**
     * Obtiene conteos por nivel de urgencia
     */
    public Map<NivelUrgencia, Integer> obtenerConteos() {
        Map<NivelUrgencia, Integer> conteos = new HashMap<>();
        for (NivelUrgencia nivel : NivelUrgencia.values()) {
            conteos.put(nivel, porNivel.get(nivel).size());
        }
        return conteos;
    }
}