package structures;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Lista enlazada especializada para mantener el historial de eventos de un paciente
 * Permite agregar eventos cronológicamente y consultar el historial completo
 */
public class HistorialPaciente {
    
    /**
     * Nodo de la lista enlazada que representa un evento en el historial
     */
    private static class EventoNodo {
        LocalDateTime fechaHora;
        String evento;
        String detalles;
        int usuarioId;
        String nombreUsuario;
        EventoNodo siguiente;
        
        EventoNodo(LocalDateTime fechaHora, String evento, String detalles, 
                  int usuarioId, String nombreUsuario) {
            this.fechaHora = fechaHora;
            this.evento = evento;
            this.detalles = detalles;
            this.usuarioId = usuarioId;
            this.nombreUsuario = nombreUsuario;
            this.siguiente = null;
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s - %s (por: %s)", 
                    fechaHora.toString(), evento, detalles, nombreUsuario);
        }
    }
    
    // Atributos de la lista
    private EventoNodo cabeza;
    private EventoNodo cola;
    private int tamaño;
    private final int registroTriageId;
    
    public HistorialPaciente(int registroTriageId) {
        this.registroTriageId = registroTriageId;
        this.cabeza = null;
        this.cola = null;
        this.tamaño = 0;
    }
    
    /**
     * Agrega un nuevo evento al final del historial
     * Complejidad: O(1)
     */
    public synchronized void agregarEvento(String evento, String detalles, 
                                          int usuarioId, String nombreUsuario) {
        EventoNodo nuevoNodo = new EventoNodo(LocalDateTime.now(), evento, detalles, 
                                             usuarioId, nombreUsuario);
        
        if (cabeza == null) {
            // Primera entrada en el historial
            cabeza = nuevoNodo;
            cola = nuevoNodo;
        } else {
            // Agregar al final de la lista
            cola.siguiente = nuevoNodo;
            cola = nuevoNodo;
        }
        
        tamaño++;
        System.out.println("Evento agregado al historial: " + nuevoNodo);
    }
    
    /**
     * Agrega un evento con timestamp específico (para cargar datos históricos)
     */
    public synchronized void agregarEventoConFecha(LocalDateTime fechaHora, String evento, 
                                                  String detalles, int usuarioId, String nombreUsuario) {
        EventoNodo nuevoNodo = new EventoNodo(fechaHora, evento, detalles, usuarioId, nombreUsuario);
        
        // Si es el primer evento o es más reciente que el último
        if (cabeza == null || fechaHora.isAfter(cola.fechaHora)) {
            if (cabeza == null) {
                cabeza = nuevoNodo;
                cola = nuevoNodo;
            } else {
                cola.siguiente = nuevoNodo;
                cola = nuevoNodo;
            }
        } else {
            // Insertar en orden cronológico
            insertarOrdenado(nuevoNodo);
        }
        
        tamaño++;
    }
    
    /**
     * Inserta un nodo en orden cronológico en la lista
     * Complejidad: O(n)
     */
    private void insertarOrdenado(EventoNodo nuevoNodo) {
        // Si debe ir al principio
        if (nuevoNodo.fechaHora.isBefore(cabeza.fechaHora)) {
            nuevoNodo.siguiente = cabeza;
            cabeza = nuevoNodo;
            return;
        }
        
        // Buscar la posición correcta
        EventoNodo actual = cabeza;
        while (actual.siguiente != null && 
               actual.siguiente.fechaHora.isBefore(nuevoNodo.fechaHora)) {
            actual = actual.siguiente;
        }
        
        // Insertar después de 'actual'
        nuevoNodo.siguiente = actual.siguiente;
        actual.siguiente = nuevoNodo;
        
        // Actualizar cola si es necesario
        if (nuevoNodo.siguiente == null) {
            cola = nuevoNodo;
        }
    }
    
    /**
     * Obtiene todo el historial como lista ordenada cronológicamente
     * Complejidad: O(n)
     */
    public List<String> obtenerHistorialCompleto() {
        List<String> historial = new ArrayList<>();
        EventoNodo actual = cabeza;
        
        while (actual != null) {
            historial.add(actual.toString());
            actual = actual.siguiente;
        }
        
        return historial;
    }
    
    /**
     * Obtiene los últimos N eventos del historial
     * Complejidad: O(n)
     */
    public List<String> obtenerUltimosEventos(int cantidad) {
        List<String> eventos = obtenerHistorialCompleto();
        
        if (eventos.size() <= cantidad) {
            return eventos;
        }
        
        // Retornar los últimos 'cantidad' eventos
        return eventos.subList(eventos.size() - cantidad, eventos.size());
    }
    
    /**
     * Busca eventos que contengan una palabra clave específica
     * Complejidad: O(n)
     */
    public List<String> buscarEventos(String palabraClave) {
        List<String> eventos = new ArrayList<>();
        EventoNodo actual = cabeza;
        
        String busqueda = palabraClave.toLowerCase();
        
        while (actual != null) {
            if (actual.evento.toLowerCase().contains(busqueda) ||
                actual.detalles.toLowerCase().contains(busqueda)) {
                eventos.add(actual.toString());
            }
            actual = actual.siguiente;
        }
        
        return eventos;
    }
    
    /**
     * Obtiene eventos realizados por un usuario específico
     * Complejidad: O(n)
     */
    public List<String> obtenerEventosPorUsuario(int usuarioId) {
        List<String> eventos = new ArrayList<>();
        EventoNodo actual = cabeza;
        
        while (actual != null) {
            if (actual.usuarioId == usuarioId) {
                eventos.add(actual.toString());
            }
            actual = actual.siguiente;
        }
        
        return eventos;
    }
    
    /**
     * Obtiene eventos en un rango de fechas
     * Complejidad: O(n)
     */
    public List<String> obtenerEventosEnRango(LocalDateTime inicio, LocalDateTime fin) {
        List<String> eventos = new ArrayList<>();
        EventoNodo actual = cabeza;
        
        while (actual != null) {
            if (!actual.fechaHora.isBefore(inicio) && !actual.fechaHora.isAfter(fin)) {
                eventos.add(actual.toString());
            }
            actual = actual.siguiente;
        }
        
        return eventos;
    }
    
    /**
     * Obtiene el primer evento (llegada del paciente)
     */
    public String obtenerPrimerEvento() {
        return cabeza != null ? cabeza.toString() : null;
    }
    
    /**
     * Obtiene el último evento (más reciente)
     */
    public String obtenerUltimoEvento() {
        return cola != null ? cola.toString() : null;
    }
    
    /**
     * Calcula la duración total desde el primer hasta el último evento
     */
    public long getDuracionTotalMinutos() {
        if (cabeza == null || cola == null) {
            return 0;
        }
        return java.time.Duration.between(cabeza.fechaHora, cola.fechaHora).toMinutes();
    }
    
    /**
     * Verifica si el historial contiene un tipo específico de evento
     */
    public boolean contieneEvento(String tipoEvento) {
        EventoNodo actual = cabeza;
        
        while (actual != null) {
            if (actual.evento.equalsIgnoreCase(tipoEvento)) {
                return true;
            }
            actual = actual.siguiente;
        }
        
        return false;
    }
    
    /**
     * Obtiene el momento del primer evento de un tipo específico
     */
    public LocalDateTime obtenerFechaPrimerEvento(String tipoEvento) {
        EventoNodo actual = cabeza;
        
        while (actual != null) {
            if (actual.evento.equalsIgnoreCase(tipoEvento)) {
                return actual.fechaHora;
            }
            actual = actual.siguiente;
        }
        
        return null;
    }
    
    /**
     * Cuenta cuántas veces ha ocurrido un tipo de evento
     */
    public int contarEventos(String tipoEvento) {
        int contador = 0;
        EventoNodo actual = cabeza;
        
        while (actual != null) {
            if (actual.evento.equalsIgnoreCase(tipoEvento)) {
                contador++;
            }
            actual = actual.siguiente;
        }
        
        return contador;
    }
    
    /**
     * Obtiene un resumen del historial para mostrar en UI
     */
    public String obtenerResumen() {
        if (tamaño == 0) {
            return "Sin eventos registrados";
        }
        
        StringBuilder resumen = new StringBuilder();
        resumen.append("Historial del Registro: ").append(registroTriageId).append("\n");
        resumen.append("Total de eventos: ").append(tamaño).append("\n");
        resumen.append("Duración total: ").append(getDuracionTotalMinutos()).append(" minutos\n");
        
        if (cabeza != null) {
            resumen.append("Inicio: ").append(cabeza.fechaHora).append(" - ").append(cabeza.evento).append("\n");
        }
        
        if (cola != null && cola != cabeza) {
            resumen.append("Último evento: ").append(cola.fechaHora).append(" - ").append(cola.evento);
        }
        
        return resumen.toString();
    }
    
    /**
     * Limpia todo el historial
     */
    public synchronized void limpiar() {
        cabeza = null;
        cola = null;
        tamaño = 0;
    }
    
    /**
     * Verifica si el historial está vacío
     */
    public boolean estaVacio() {
        return tamaño == 0;
    }
    
    /**
     * Obtiene el tamaño del historial
     */
    public int getTamaño() {
        return tamaño;
    }
    
    /**
     * Obtiene el ID del registro de triage asociado
     */
    public int getRegistroTriageId() {
        return registroTriageId;
    }
    
    @Override
    public String toString() {
        return "HistorialPaciente{" +
                "registroTriageId=" + registroTriageId +
                ", eventos=" + tamaño +
                ", duracion=" + getDuracionTotalMinutos() + " min" +
                '}';
    }
    
    /**
     * Método para debuging - imprime todo el historial
     */
    public void imprimirHistorial() {
        System.out.println("=== HISTORIAL COMPLETO ===");
        System.out.println("Registro Triage ID: " + registroTriageId);
        System.out.println("Total eventos: " + tamaño);
        
        EventoNodo actual = cabeza;
        int contador = 1;
        
        while (actual != null) {
            System.out.println(contador + ". " + actual.toString());
            actual = actual.siguiente;
            contador++;
        }
        
        System.out.println("========================");
    }
}