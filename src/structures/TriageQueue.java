package structures;

import models.RegistroTriage;
import java.util.*;

/**
 * Cola de prioridad especializada para el sistema de triage
 * Maneja diferentes niveles de urgencia con diferentes estructuras de datos según la especificación:
 * - ROJO: Stack (LIFO) - último en llegar es el primero en atenderse
 * - NARANJA, AMARILLO, VERDE: Queue (FIFO) - primero en llegar es el primero en atenderse  
 * - AZUL: Queue especial para citas ambulatorias
 */
public class TriageQueue {
    
    // Stack para pacientes nivel ROJO (críticos)
    private final Stack<RegistroTriage> nivelRojo;
    
    // Queues para pacientes urgentes pero no críticos
    private final Queue<RegistroTriage> nivelNaranja;
    private final Queue<RegistroTriage> nivelAmarillo;
    private final Queue<RegistroTriage> nivelVerde;
    
    // Queue especial para pacientes ambulatorios
    private final Queue<RegistroTriage> nivelAzul;
    
    // HashMap para búsquedas rápidas O(1)
    private final Map<String, RegistroTriage> registrosPorFolio;
    private final Map<Integer, RegistroTriage> registrosPorId;
    
    // Contador para estadísticas
    private int totalPacientesAtendidos;
    private final Map<RegistroTriage.NivelUrgencia, Integer> contadorPorNivel;
    
    public TriageQueue() {
        // Inicializar estructuras de datos
        this.nivelRojo = new Stack<>();
        this.nivelNaranja = new LinkedList<>();
        this.nivelAmarillo = new LinkedList<>();
        this.nivelVerde = new LinkedList<>();
        this.nivelAzul = new LinkedList<>();
        
        // HashMaps para búsquedas rápidas
        this.registrosPorFolio = new HashMap<>();
        this.registrosPorId = new HashMap<>();
        
        // Contadores
        this.totalPacientesAtendidos = 0;
        this.contadorPorNivel = new EnumMap<>(RegistroTriage.NivelUrgencia.class);
        for (RegistroTriage.NivelUrgencia nivel : RegistroTriage.NivelUrgencia.values()) {
            contadorPorNivel.put(nivel, 0);
        }
    }
    
    /**
     * Agrega un nuevo paciente a la cola correspondiente según su nivel de urgencia
     * Complejidad: O(1)
     */
    public synchronized void agregarPaciente(RegistroTriage registro) {
        if (registro == null || registro.getNivelUrgencia() == null) {
            throw new IllegalArgumentException("Registro y nivel de urgencia no pueden ser nulos");
        }
        
        // Agregar a los HashMap para búsquedas rápidas
        if (registro.getFolio() != null) {
            registrosPorFolio.put(registro.getFolio(), registro);
        }
        registrosPorId.put(registro.getId(), registro);
        
        // Agregar a la estructura correspondiente según el nivel
        switch (registro.getNivelUrgencia()) {
            case ROJO:
                nivelRojo.push(registro); // Stack - LIFO
                break;
            case NARANJA:
                nivelNaranja.offer(registro); // Queue - FIFO
                break;
            case AMARILLO:
                nivelAmarillo.offer(registro); // Queue - FIFO
                break;
            case VERDE:
                nivelVerde.offer(registro); // Queue - FIFO
                break;
            case AZUL:
                nivelAzul.offer(registro); // Queue especial - FIFO
                break;
        }
        
        // Incrementar contador
        contadorPorNivel.merge(registro.getNivelUrgencia(), 1, Integer::sum);
        
        System.out.println("Paciente agregado: " + registro.getFolio() + 
                          " - Nivel " + registro.getNivelUrgencia());
    }
    
    /**
     * Obtiene el siguiente paciente que debe ser atendido según prioridad
     * Orden de prioridad: ROJO > NARANJA > AMARILLO > VERDE
     * AZUL se maneja por separado (citas ambulatorias)
     * Complejidad: O(1)
     */
    public synchronized RegistroTriage obtenerSiguientePaciente() {
        RegistroTriage siguiente = null;
        
        // Prioridad 1: ROJO (Stack - último en llegar)
        if (!nivelRojo.isEmpty()) {
            siguiente = nivelRojo.pop();
        }
        // Prioridad 2: NARANJA (Queue - primero en llegar)
        else if (!nivelNaranja.isEmpty()) {
            siguiente = nivelNaranja.poll();
        }
        // Prioridad 3: AMARILLO (Queue - primero en llegar)
        else if (!nivelAmarillo.isEmpty()) {
            siguiente = nivelAmarillo.poll();
        }
        // Prioridad 4: VERDE (Queue - primero en llegar)
        else if (!nivelVerde.isEmpty()) {
            siguiente = nivelVerde.poll();
        }
        
        if (siguiente != null) {
            // Actualizar estado del paciente
            siguiente.setEstado(RegistroTriage.Estado.EN_ATENCION);
            totalPacientesAtendidos++;
            
            System.out.println("Siguiente paciente: " + siguiente.getFolio() + 
                              " - Nivel " + siguiente.getNivelUrgencia());
        }
        
        return siguiente;
    }
    
    /**
     * Obtiene el siguiente paciente nivel AZUL para cita ambulatoria
     * Complejidad: O(1)
     */
    public synchronized RegistroTriage obtenerSiguientePacienteAzul() {
        if (!nivelAzul.isEmpty()) {
            RegistroTriage siguiente = nivelAzul.poll();
            siguiente.setEstado(RegistroTriage.Estado.CITA_PROGRAMADA);
            totalPacientesAtendidos++;
            
            System.out.println("Paciente AZUL para cita: " + siguiente.getFolio());
            return siguiente;
        }
        return null;
    }
    
    /**
     * Busca un paciente por folio
     * Complejidad: O(1)
     */
    public RegistroTriage buscarPorFolio(String folio) {
        return registrosPorFolio.get(folio);
    }
    
    /**
     * Busca un paciente por ID
     * Complejidad: O(1)
     */
    public RegistroTriage buscarPorId(int id) {
        return registrosPorId.get(id);
    }
    
    /**
     * Remueve un paciente de todas las estructuras (cuando se completa su atención)
     */
    public synchronized void removerPaciente(RegistroTriage registro) {
        if (registro == null) return;
        
        // Remover de HashMap
        if (registro.getFolio() != null) {
            registrosPorFolio.remove(registro.getFolio());
        }
        registrosPorId.remove(registro.getId());
        
        // Remover de la estructura correspondiente
        switch (registro.getNivelUrgencia()) {
            case ROJO:
                nivelRojo.remove(registro);
                break;
            case NARANJA:
                nivelNaranja.remove(registro);
                break;
            case AMARILLO:
                nivelAmarillo.remove(registro);
                break;
            case VERDE:
                nivelVerde.remove(registro);
                break;
            case AZUL:
                nivelAzul.remove(registro);
                break;
        }
        
        System.out.println("Paciente removido: " + registro.getFolio());
    }
    
    /**
     * Obtiene la lista de pacientes en sala de espera ordenada por prioridad
     */
    public synchronized List<RegistroTriage> obtenerSalaEspera() {
        List<RegistroTriage> salaEspera = new ArrayList<>();
        
        // Agregar en orden de prioridad (sin remover de las colas)
        salaEspera.addAll(nivelRojo);
        salaEspera.addAll(nivelNaranja);
        salaEspera.addAll(nivelAmarillo);
        salaEspera.addAll(nivelVerde);
        
        return salaEspera;
    }
    
    /**
     * Obtiene la lista de pacientes nivel AZUL esperando cita
     */
    public synchronized List<RegistroTriage> obtenerPacientesAzul() {
        return new ArrayList<>(nivelAzul);
    }
    
    // Métodos de estadísticas
    public int getTotalPacientesEspera() {
        return nivelRojo.size() + nivelNaranja.size() + 
               nivelAmarillo.size() + nivelVerde.size();
    }
    
    public int getTotalPacientesAzul() {
        return nivelAzul.size();
    }
    
    public int getTotalPacientesAtendidos() {
        return totalPacientesAtendidos;
    }
    
    public int getCantidadPorNivel(RegistroTriage.NivelUrgencia nivel) {
        switch (nivel) {
            case ROJO: return nivelRojo.size();
            case NARANJA: return nivelNaranja.size();
            case AMARILLO: return nivelAmarillo.size();
            case VERDE: return nivelVerde.size();
            case AZUL: return nivelAzul.size();
            default: return 0;
        }
    }
    
    public Map<RegistroTriage.NivelUrgencia, Integer> getEstadisticasPorNivel() {
        Map<RegistroTriage.NivelUrgencia, Integer> stats = new EnumMap<>(RegistroTriage.NivelUrgencia.class);
        stats.put(RegistroTriage.NivelUrgencia.ROJO, nivelRojo.size());
        stats.put(RegistroTriage.NivelUrgencia.NARANJA, nivelNaranja.size());
        stats.put(RegistroTriage.NivelUrgencia.AMARILLO, nivelAmarillo.size());
        stats.put(RegistroTriage.NivelUrgencia.VERDE, nivelVerde.size());
        stats.put(RegistroTriage.NivelUrgencia.AZUL, nivelAzul.size());
        return stats;
    }
    
    /**
     * Calcula el tiempo estimado de espera para un nuevo paciente según su nivel
     */
    public int calcularTiempoEsperaEstimado(RegistroTriage.NivelUrgencia nivel) {
        // Tiempos promedio estimados por consulta (en minutos)
        final int TIEMPO_CONSULTA_ROJO = 45;
        final int TIEMPO_CONSULTA_NARANJA = 30;
        final int TIEMPO_CONSULTA_AMARILLO = 20;
        final int TIEMPO_CONSULTA_VERDE = 15;
        
        int tiempoEstimado = 0;
        
        // Calcular basado en pacientes por delante según prioridad
        switch (nivel) {
            case ROJO:
                // Solo cuenta otros pacientes ROJO por delante
                tiempoEstimado = nivelRojo.size() * TIEMPO_CONSULTA_ROJO;
                break;
            case NARANJA:
                // Cuenta ROJOS + NARANJAS por delante
                tiempoEstimado = (nivelRojo.size() * TIEMPO_CONSULTA_ROJO) +
                               (nivelNaranja.size() * TIEMPO_CONSULTA_NARANJA);
                break;
            case AMARILLO:
                // Cuenta ROJOS + NARANJAS + AMARILLOS por delante
                tiempoEstimado = (nivelRojo.size() * TIEMPO_CONSULTA_ROJO) +
                               (nivelNaranja.size() * TIEMPO_CONSULTA_NARANJA) +
                               (nivelAmarillo.size() * TIEMPO_CONSULTA_AMARILLO);
                break;
            case VERDE:
                // Cuenta todos los niveles por delante
                tiempoEstimado = (nivelRojo.size() * TIEMPO_CONSULTA_ROJO) +
                               (nivelNaranja.size() * TIEMPO_CONSULTA_NARANJA) +
                               (nivelAmarillo.size() * TIEMPO_CONSULTA_AMARILLO) +
                               (nivelVerde.size() * TIEMPO_CONSULTA_VERDE);
                break;
            case AZUL:
                // Para citas ambulatorias, el tiempo es diferente
                return -1; // Indicar que se programa cita
        }
        
        return tiempoEstimado;
    }
    
    /**
     * Verifica si hay pacientes críticos esperando
     */
    public boolean hayPacientesCriticos() {
        return !nivelRojo.isEmpty();
    }
    
    /**
     * Obtiene el paciente que lleva más tiempo esperando (solo para estadísticas)
     */
    public RegistroTriage obtenerPacienteMayorEspera() {
        RegistroTriage mayorEspera = null;
        long maxMinutos = 0;
        
        // Buscar en todas las colas
        List<RegistroTriage> todosPacientes = obtenerSalaEspera();
        
        for (RegistroTriage registro : todosPacientes) {
            long minutosEspera = registro.getMinutosEspera();
            if (minutosEspera > maxMinutos) {
                maxMinutos = minutosEspera;
                mayorEspera = registro;
            }
        }
        
        return mayorEspera;
    }
    
    /**
     * Limpia todas las colas (para reiniciar el sistema)
     */
    public synchronized void limpiarTodo() {
        nivelRojo.clear();
        nivelNaranja.clear();
        nivelAmarillo.clear();
        nivelVerde.clear();
        nivelAzul.clear();
        registrosPorFolio.clear();
        registrosPorId.clear();
        totalPacientesAtendidos = 0;
        
        for (RegistroTriage.NivelUrgencia nivel : RegistroTriage.NivelUrgencia.values()) {
            contadorPorNivel.put(nivel, 0);
        }
        
        System.out.println("Sistema de colas reiniciado");
    }
    
    @Override
    public String toString() {
        return "TriageQueue{" +
                "ROJO=" + nivelRojo.size() +
                ", NARANJA=" + nivelNaranja.size() +
                ", AMARILLO=" + nivelAmarillo.size() +
                ", VERDE=" + nivelVerde.size() +
                ", AZUL=" + nivelAzul.size() +
                ", totalAtendidos=" + totalPacientesAtendidos +
                '}';
    }
}