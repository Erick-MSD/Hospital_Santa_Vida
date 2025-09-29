package structures;

import models.EstadoPaciente;
import java.util.*;

/**
 * Grafo dirigido que modela el flujo de pacientes a través del sistema hospitalario
 * Cada nodo representa un estado del paciente, las aristas representan transiciones válidas
 * Incluye pesos en las aristas que representan tiempos promedio de transición
 */
public class HospitalGraph {
    
    /**
     * Clase interna que representa una arista del grafo
     */
    private static class Arista {
        EstadoPaciente destino;
        int peso; // Tiempo promedio en minutos
        String descripcion;
        boolean esValida; // Permite habilitar/deshabilitar transiciones
        
        Arista(EstadoPaciente destino, int peso, String descripcion) {
            this.destino = destino;
            this.peso = peso;
            this.descripcion = descripcion;
            this.esValida = true;
        }
    }
    
    // Estructura principal del grafo: cada estado tiene una lista de transiciones válidas
    private Map<EstadoPaciente, List<Arista>> grafo;
    
    // Estadísticas de transiciones
    private Map<String, Integer> contadorTransiciones;
    private Map<String, List<Integer>> tiemposTransicion;
    
    /**
     * Constructor que inicializa el grafo con las transiciones del sistema hospitalario
     */
    public HospitalGraph() {
        this.grafo = new HashMap<>();
        this.contadorTransiciones = new HashMap<>();
        this.tiemposTransicion = new HashMap<>();
        
        inicializarGrafo();
    }
    
    /**
     * Inicializa el grafo con todas las transiciones válidas del sistema
     */
    private void inicializarGrafo() {
        // Inicializar todos los estados
        for (EstadoPaciente estado : EstadoPaciente.values()) {
            grafo.put(estado, new ArrayList<>());
        }
        
        // Definir transiciones válidas según el flujo del sistema
        
        // ESPERANDO_ASISTENTE -> ESPERANDO_TRABAJO_SOCIAL
        // (Después de que el médico de triage evalúa al paciente)
        agregarArista(EstadoPaciente.ESPERANDO_ASISTENTE, 
                     EstadoPaciente.ESPERANDO_TRABAJO_SOCIAL, 
                     15, "Evaluación de triage completada");
        
        // ESPERANDO_ASISTENTE -> CITA_PROGRAMADA
        // (Para pacientes nivel AZUL que requieren cita ambulatoria)
        agregarArista(EstadoPaciente.ESPERANDO_ASISTENTE, 
                     EstadoPaciente.CITA_PROGRAMADA, 
                     10, "Paciente nivel AZUL - cita ambulatoria");
        
        // ESPERANDO_TRABAJO_SOCIAL -> ESPERANDO_MEDICO
        // (Después de la evaluación social)
        agregarArista(EstadoPaciente.ESPERANDO_TRABAJO_SOCIAL, 
                     EstadoPaciente.ESPERANDO_MEDICO, 
                     20, "Evaluación social completada");
        
        // ESPERANDO_MEDICO -> EN_ATENCION
        // (Cuando el médico de urgencias comienza la consulta)
        agregarArista(EstadoPaciente.ESPERANDO_MEDICO, 
                     EstadoPaciente.EN_ATENCION, 
                     5, "Inicio de consulta médica");
        
        // EN_ATENCION -> COMPLETADO
        // (Consulta médica terminada con alta)
        agregarArista(EstadoPaciente.EN_ATENCION, 
                     EstadoPaciente.COMPLETADO, 
                     30, "Alta médica otorgada");
        
        // Transiciones especiales de emergencia
        
        // ESPERANDO_TRABAJO_SOCIAL -> EN_ATENCION
        // (Para casos urgentes que no pueden esperar)
        agregarArista(EstadoPaciente.ESPERANDO_TRABAJO_SOCIAL, 
                     EstadoPaciente.EN_ATENCION, 
                     2, "Emergencia - atención inmediata");
        
        // ESPERANDO_ASISTENTE -> EN_ATENCION
        // (Para casos críticos nivel ROJO)
        agregarArista(EstadoPaciente.ESPERANDO_ASISTENTE, 
                     EstadoPaciente.EN_ATENCION, 
                     1, "Crítico nivel ROJO - atención inmediata");
    }
    
    /**
     * Agrega una arista al grafo
     */
    private void agregarArista(EstadoPaciente origen, EstadoPaciente destino, int peso, String descripcion) {
        grafo.get(origen).add(new Arista(destino, peso, descripcion));
        
        String clave = origen.name() + "_" + destino.name();
        contadorTransiciones.put(clave, 0);
        tiemposTransicion.put(clave, new ArrayList<>());
    }
    
    /**
     * Verifica si una transición es válida
     * @param origen Estado origen
     * @param destino Estado destino
     * @return true si la transición es válida
     */
    public boolean esTransicionValida(EstadoPaciente origen, EstadoPaciente destino) {
        List<Arista> aristas = grafo.get(origen);
        if (aristas == null) return false;
        
        return aristas.stream()
                .anyMatch(arista -> arista.destino == destino && arista.esValida);
    }
    
    /**
     * Obtiene las transiciones válidas desde un estado
     * @param origen Estado origen
     * @return Lista de estados destino válidos
     */
    public List<EstadoPaciente> obtenerTransicionesValidas(EstadoPaciente origen) {
        List<EstadoPaciente> transiciones = new ArrayList<>();
        List<Arista> aristas = grafo.get(origen);
        
        if (aristas != null) {
            for (Arista arista : aristas) {
                if (arista.esValida) {
                    transiciones.add(arista.destino);
                }
            }
        }
        
        return transiciones;
    }
    
    /**
     * Registra una transición realizada y actualiza estadísticas
     * @param origen Estado origen
     * @param destino Estado destino
     * @param tiempoReal Tiempo real que tomó la transición en minutos
     */
    public void registrarTransicion(EstadoPaciente origen, EstadoPaciente destino, int tiempoReal) {
        String clave = origen.name() + "_" + destino.name();
        
        if (contadorTransiciones.containsKey(clave)) {
            contadorTransiciones.put(clave, contadorTransiciones.get(clave) + 1);
            tiemposTransicion.get(clave).add(tiempoReal);
        }
    }
    
    /**
     * Calcula la ruta más rápida entre dos estados
     * @param origen Estado origen
     * @param destino Estado destino
     * @return Lista de estados que forman la ruta más rápida, null si no hay ruta
     */
    public List<EstadoPaciente> calcularRutaMasRapida(EstadoPaciente origen, EstadoPaciente destino) {
        // Implementación del algoritmo de Dijkstra
        Map<EstadoPaciente, Integer> distancias = new HashMap<>();
        Map<EstadoPaciente, EstadoPaciente> predecesores = new HashMap<>();
        Set<EstadoPaciente> visitados = new HashSet<>();
        PriorityQueue<EstadoPaciente> cola = new PriorityQueue<>((a, b) -> 
            Integer.compare(distancias.get(a), distancias.get(b)));
        
        // Inicializar distancias
        for (EstadoPaciente estado : EstadoPaciente.values()) {
            distancias.put(estado, Integer.MAX_VALUE);
        }
        distancias.put(origen, 0);
        cola.offer(origen);
        
        while (!cola.isEmpty()) {
            EstadoPaciente actual = cola.poll();
            
            if (visitados.contains(actual)) continue;
            visitados.add(actual);
            
            if (actual == destino) break;
            
            List<Arista> aristas = grafo.get(actual);
            if (aristas != null) {
                for (Arista arista : aristas) {
                    if (!arista.esValida) continue;
                    
                    int nuevaDistancia = distancias.get(actual) + arista.peso;
                    if (nuevaDistancia < distancias.get(arista.destino)) {
                        distancias.put(arista.destino, nuevaDistancia);
                        predecesores.put(arista.destino, actual);
                        cola.offer(arista.destino);
                    }
                }
            }
        }
        
        // Reconstruir ruta
        if (distancias.get(destino) == Integer.MAX_VALUE) {
            return null; // No hay ruta
        }
        
        List<EstadoPaciente> ruta = new ArrayList<>();
        EstadoPaciente actual = destino;
        
        while (actual != null) {
            ruta.add(0, actual);
            actual = predecesores.get(actual);
        }
        
        return ruta;
    }
    
    /**
     * Obtiene el tiempo estimado para una transición específica
     * @param origen Estado origen
     * @param destino Estado destino
     * @return Tiempo estimado en minutos, -1 si la transición no es válida
     */
    public int obtenerTiempoEstimado(EstadoPaciente origen, EstadoPaciente destino) {
        List<Arista> aristas = grafo.get(origen);
        if (aristas == null) return -1;
        
        for (Arista arista : aristas) {
            if (arista.destino == destino && arista.esValida) {
                // Usar tiempo promedio real si hay datos, sino usar peso base
                String clave = origen.name() + "_" + destino.name();
                List<Integer> tiempos = tiemposTransicion.get(clave);
                
                if (tiempos != null && !tiempos.isEmpty()) {
                    return (int) tiempos.stream().mapToInt(Integer::intValue).average().orElse(arista.peso);
                }
                
                return arista.peso;
            }
        }
        
        return -1;
    }
    
    /**
     * Calcula el tiempo total estimado para completar el proceso desde un estado dado
     * @param estadoActual Estado actual del paciente
     * @return Tiempo estimado en minutos hasta COMPLETADO
     */
    public int calcularTiempoTotalEstimado(EstadoPaciente estadoActual) {
        List<EstadoPaciente> ruta = calcularRutaMasRapida(estadoActual, EstadoPaciente.COMPLETADO);
        
        if (ruta == null || ruta.size() < 2) return -1;
        
        int tiempoTotal = 0;
        for (int i = 0; i < ruta.size() - 1; i++) {
            int tiempo = obtenerTiempoEstimado(ruta.get(i), ruta.get(i + 1));
            if (tiempo == -1) return -1;
            tiempoTotal += tiempo;
        }
        
        return tiempoTotal;
    }
    
    /**
     * Obtiene estadísticas de transiciones
     * @return Map con estadísticas detalladas
     */
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Estadísticas por transición
        for (Map.Entry<String, Integer> entry : contadorTransiciones.entrySet()) {
            String transicion = entry.getKey();
            int cantidad = entry.getValue();
            
            if (cantidad > 0) {
                List<Integer> tiempos = tiemposTransicion.get(transicion);
                double tiempoPromedio = tiempos.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                
                Map<String, Object> infoTransicion = new HashMap<>();
                infoTransicion.put("cantidad", cantidad);
                infoTransicion.put("tiempo_promedio", tiempoPromedio);
                infoTransicion.put("tiempo_minimo", tiempos.stream().mapToInt(Integer::intValue).min().orElse(0));
                infoTransicion.put("tiempo_maximo", tiempos.stream().mapToInt(Integer::intValue).max().orElse(0));
                
                estadisticas.put(transicion, infoTransicion);
            }
        }
        
        // Estadísticas generales
        int totalTransiciones = contadorTransiciones.values().stream().mapToInt(Integer::intValue).sum();
        estadisticas.put("total_transiciones", totalTransiciones);
        
        // Estado con más transiciones de salida
        EstadoPaciente estadoMasActivo = null;
        int maxTransiciones = 0;
        
        for (EstadoPaciente estado : EstadoPaciente.values()) {
            int transicionesEstado = 0;
            for (String clave : contadorTransiciones.keySet()) {
                if (clave.startsWith(estado.name() + "_")) {
                    transicionesEstado += contadorTransiciones.get(clave);
                }
            }
            
            if (transicionesEstado > maxTransiciones) {
                maxTransiciones = transicionesEstado;
                estadoMasActivo = estado;
            }
        }
        
        estadisticas.put("estado_mas_activo", estadoMasActivo != null ? estadoMasActivo.name() : "N/A");
        estadisticas.put("transiciones_estado_mas_activo", maxTransiciones);
        
        return estadisticas;
    }
    
    /**
     * Habilita o deshabilita una transición específica
     * @param origen Estado origen
     * @param destino Estado destino
     * @param habilitada true para habilitar, false para deshabilitar
     */
    public void configurarTransicion(EstadoPaciente origen, EstadoPaciente destino, boolean habilitada) {
        List<Arista> aristas = grafo.get(origen);
        if (aristas != null) {
            for (Arista arista : aristas) {
                if (arista.destino == destino) {
                    arista.esValida = habilitada;
                    break;
                }
            }
        }
    }
    
    /**
     * Obtiene información detallada de una transición
     * @param origen Estado origen
     * @param destino Estado destino
     * @return String con información de la transición
     */
    public String obtenerInfoTransicion(EstadoPaciente origen, EstadoPaciente destino) {
        List<Arista> aristas = grafo.get(origen);
        if (aristas == null) return "Transición no encontrada";
        
        for (Arista arista : aristas) {
            if (arista.destino == destino) {
                StringBuilder sb = new StringBuilder();
                sb.append("Transición: ").append(origen.name()).append(" -> ").append(destino.name()).append("\\n");
                sb.append("Descripción: ").append(arista.descripcion).append("\\n");
                sb.append("Peso base: ").append(arista.peso).append(" minutos\\n");
                sb.append("Válida: ").append(arista.esValida ? "Sí" : "No").append("\\n");
                
                String clave = origen.name() + "_" + destino.name();
                int cantidad = contadorTransiciones.getOrDefault(clave, 0);
                sb.append("Veces ejecutada: ").append(cantidad).append("\\n");
                
                if (cantidad > 0) {
                    List<Integer> tiempos = tiemposTransicion.get(clave);
                    double promedio = tiempos.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                    sb.append("Tiempo promedio real: ").append(String.format("%.1f", promedio)).append(" minutos");
                }
                
                return sb.toString();
            }
        }
        
        return "Transición no encontrada";
    }
    
    /**
     * Reinicia todas las estadísticas
     */
    public void reiniciarEstadisticas() {
        for (String clave : contadorTransiciones.keySet()) {
            contadorTransiciones.put(clave, 0);
            tiemposTransicion.get(clave).clear();
        }
    }
    
    /**
     * Obtiene una representación visual del grafo
     * @return String con la representación del grafo
     */
    public String obtenerRepresentacionGrafo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Hospital Flow Graph:\\n");
        sb.append("==================\\n");
        
        for (EstadoPaciente estado : EstadoPaciente.values()) {
            List<Arista> aristas = grafo.get(estado);
            if (aristas != null && !aristas.isEmpty()) {
                sb.append(estado.name()).append(" ->\\n");
                for (Arista arista : aristas) {
                    sb.append("  ├─ ").append(arista.destino.name())
                      .append(" (").append(arista.peso).append("min)")
                      .append(arista.esValida ? "" : " [DESHABILITADA]")
                      .append("\\n");
                }
                sb.append("\\n");
            }
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        int totalTransiciones = contadorTransiciones.values().stream().mapToInt(Integer::intValue).sum();
        return "HospitalGraph - " + EstadoPaciente.values().length + " estados, " + 
               contadorTransiciones.size() + " transiciones posibles, " +
               totalTransiciones + " transiciones ejecutadas";
    }
}