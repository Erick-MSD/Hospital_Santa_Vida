package structures;

import models.Paciente;
import java.util.ArrayList;
import java.util.List;

/**
 * Árbol Binario de Búsqueda especializado para pacientes
 * Permite búsqueda rápida por CURP en O(log n) tiempo promedio
 * Mantiene los pacientes ordenados alfabéticamente por CURP
 */
public class PacienteBST {
    
    /**
     * Nodo interno del árbol binario
     */
    private static class Nodo {
        Paciente paciente;
        Nodo izquierdo;
        Nodo derecho;
        int altura; // Para balanceo AVL
        
        Nodo(Paciente paciente) {
            this.paciente = paciente;
            this.izquierdo = null;
            this.derecho = null;
            this.altura = 1;
        }
    }
    
    private Nodo raiz;
    private int tamaño;
    
    /**
     * Constructor del árbol vacío
     */
    public PacienteBST() {
        this.raiz = null;
        this.tamaño = 0;
    }
    
    /**
     * Inserta un paciente en el árbol
     * @param paciente El paciente a insertar
     * @return true si se insertó correctamente, false si ya existía
     */
    public boolean insertar(Paciente paciente) {
        if (paciente == null || paciente.getCurp() == null || paciente.getCurp().trim().isEmpty()) {
            throw new IllegalArgumentException("El paciente y su CURP no pueden ser nulos o vacíos");
        }
        
        int tamañoAnterior = tamaño;
        raiz = insertarRecursivo(raiz, paciente);
        
        return tamaño > tamañoAnterior; // Retorna true si realmente se insertó
    }
    
    /**
     * Método recursivo para insertar manteniendo balance AVL
     */
    private Nodo insertarRecursivo(Nodo nodo, Paciente paciente) {
        // Inserción estándar BST
        if (nodo == null) {
            tamaño++;
            return new Nodo(paciente);
        }
        
        String curpNuevo = paciente.getCurp().toUpperCase();
        String curpNodo = nodo.paciente.getCurp().toUpperCase();
        
        int comparacion = curpNuevo.compareTo(curpNodo);
        
        if (comparacion < 0) {
            nodo.izquierdo = insertarRecursivo(nodo.izquierdo, paciente);
        } else if (comparacion > 0) {
            nodo.derecho = insertarRecursivo(nodo.derecho, paciente);
        } else {
            // CURP ya existe, actualizar información del paciente
            nodo.paciente = paciente;
            return nodo;
        }
        
        // Actualizar altura del nodo actual
        nodo.altura = 1 + Math.max(obtenerAltura(nodo.izquierdo), obtenerAltura(nodo.derecho));
        
        // Obtener factor de balance
        int balance = obtenerBalance(nodo);
        
        // Rotaciones para mantener balance AVL
        
        // Rotación derecha (caso izquierda-izquierda)
        if (balance > 1 && curpNuevo.compareTo(nodo.izquierdo.paciente.getCurp().toUpperCase()) < 0) {
            return rotacionDerecha(nodo);
        }
        
        // Rotación izquierda (caso derecha-derecha)
        if (balance < -1 && curpNuevo.compareTo(nodo.derecho.paciente.getCurp().toUpperCase()) > 0) {
            return rotacionIzquierda(nodo);
        }
        
        // Rotación izquierda-derecha (caso izquierda-derecha)
        if (balance > 1 && curpNuevo.compareTo(nodo.izquierdo.paciente.getCurp().toUpperCase()) > 0) {
            nodo.izquierdo = rotacionIzquierda(nodo.izquierdo);
            return rotacionDerecha(nodo);
        }
        
        // Rotación derecha-izquierda (caso derecha-izquierda)
        if (balance < -1 && curpNuevo.compareTo(nodo.derecho.paciente.getCurp().toUpperCase()) < 0) {
            nodo.derecho = rotacionDerecha(nodo.derecho);
            return rotacionIzquierda(nodo);
        }
        
        return nodo;
    }
    
    /**
     * Busca un paciente por su CURP
     * @param curp La CURP del paciente a buscar
     * @return El paciente encontrado, o null si no existe
     */
    public Paciente buscar(String curp) {
        if (curp == null || curp.trim().isEmpty()) {
            return null;
        }
        
        return buscarRecursivo(raiz, curp.toUpperCase());
    }
    
    /**
     * Método recursivo para búsqueda
     */
    private Paciente buscarRecursivo(Nodo nodo, String curp) {
        if (nodo == null) {
            return null;
        }
        
        String curpNodo = nodo.paciente.getCurp().toUpperCase();
        int comparacion = curp.compareTo(curpNodo);
        
        if (comparacion == 0) {
            return nodo.paciente;
        } else if (comparacion < 0) {
            return buscarRecursivo(nodo.izquierdo, curp);
        } else {
            return buscarRecursivo(nodo.derecho, curp);
        }
    }
    
    /**
     * Busca pacientes por nombre (búsqueda parcial)
     * @param nombre El nombre a buscar (puede ser parcial)
     * @return Lista de pacientes que coinciden
     */
    public List<Paciente> buscarPorNombre(String nombre) {
        List<Paciente> resultados = new ArrayList<>();
        if (nombre == null || nombre.trim().isEmpty()) {
            return resultados;
        }
        
        String nombreBusqueda = nombre.toLowerCase().trim();
        buscarPorNombreRecursivo(raiz, nombreBusqueda, resultados);
        
        return resultados;
    }
    
    /**
     * Método recursivo para búsqueda por nombre
     */
    private void buscarPorNombreRecursivo(Nodo nodo, String nombre, List<Paciente> resultados) {
        if (nodo == null) {
            return;
        }
        
        // Buscar en subárbol izquierdo
        buscarPorNombreRecursivo(nodo.izquierdo, nombre, resultados);
        
        // Verificar nodo actual
        String nombreCompleto = nodo.paciente.getNombreCompleto().toLowerCase();
        if (nombreCompleto.contains(nombre)) {
            resultados.add(nodo.paciente);
        }
        
        // Buscar en subárbol derecho
        buscarPorNombreRecursivo(nodo.derecho, nombre, resultados);
    }
    
    /**
     * Elimina un paciente del árbol
     * @param curp La CURP del paciente a eliminar
     * @return true si se eliminó, false si no se encontró
     */
    public boolean eliminar(String curp) {
        if (curp == null || curp.trim().isEmpty()) {
            return false;
        }
        
        int tamañoAnterior = tamaño;
        raiz = eliminarRecursivo(raiz, curp.toUpperCase());
        
        return tamaño < tamañoAnterior;
    }
    
    /**
     * Método recursivo para eliminación
     */
    private Nodo eliminarRecursivo(Nodo nodo, String curp) {
        if (nodo == null) {
            return null;
        }
        
        String curpNodo = nodo.paciente.getCurp().toUpperCase();
        int comparacion = curp.compareTo(curpNodo);
        
        if (comparacion < 0) {
            nodo.izquierdo = eliminarRecursivo(nodo.izquierdo, curp);
        } else if (comparacion > 0) {
            nodo.derecho = eliminarRecursivo(nodo.derecho, curp);
        } else {
            // Nodo a eliminar encontrado
            tamaño--;
            
            // Caso 1: Nodo hoja o con un solo hijo
            if (nodo.izquierdo == null || nodo.derecho == null) {
                Nodo temp = (nodo.izquierdo != null) ? nodo.izquierdo : nodo.derecho;
                
                if (temp == null) {
                    // Sin hijos
                    temp = nodo;
                    nodo = null;
                } else {
                    // Un hijo
                    nodo = temp;
                }
            } else {
                // Caso 2: Nodo con dos hijos
                // Encontrar sucesor inorden (menor en subárbol derecho)
                Nodo temp = encontrarMinimo(nodo.derecho);
                
                // Copiar datos del sucesor
                nodo.paciente = temp.paciente;
                
                // Eliminar sucesor
                nodo.derecho = eliminarRecursivo(nodo.derecho, temp.paciente.getCurp());
                tamaño++; // Compensar la reducción extra
            }
        }
        
        if (nodo == null) {
            return nodo;
        }
        
        // Actualizar altura y rebalancear
        nodo.altura = 1 + Math.max(obtenerAltura(nodo.izquierdo), obtenerAltura(nodo.derecho));
        
        int balance = obtenerBalance(nodo);
        
        // Rotaciones de rebalance
        if (balance > 1 && obtenerBalance(nodo.izquierdo) >= 0) {
            return rotacionDerecha(nodo);
        }
        
        if (balance > 1 && obtenerBalance(nodo.izquierdo) < 0) {
            nodo.izquierdo = rotacionIzquierda(nodo.izquierdo);
            return rotacionDerecha(nodo);
        }
        
        if (balance < -1 && obtenerBalance(nodo.derecho) <= 0) {
            return rotacionIzquierda(nodo);
        }
        
        if (balance < -1 && obtenerBalance(nodo.derecho) > 0) {
            nodo.derecho = rotacionDerecha(nodo.derecho);
            return rotacionIzquierda(nodo);
        }
        
        return nodo;
    }
    
    /**
     * Obtiene todos los pacientes en orden alfabético
     * @return Lista ordenada de pacientes
     */
    public List<Paciente> obtenerTodosOrdenados() {
        List<Paciente> lista = new ArrayList<>();
        inOrdenRecursivo(raiz, lista);
        return lista;
    }
    
    /**
     * Recorrido inorden recursivo
     */
    private void inOrdenRecursivo(Nodo nodo, List<Paciente> lista) {
        if (nodo != null) {
            inOrdenRecursivo(nodo.izquierdo, lista);
            lista.add(nodo.paciente);
            inOrdenRecursivo(nodo.derecho, lista);
        }
    }
    
    /**
     * Verifica si existe un paciente con la CURP dada
     * @param curp La CURP a verificar
     * @return true si existe, false si no
     */
    public boolean existe(String curp) {
        return buscar(curp) != null;
    }
    
    /**
     * Obtiene el número de pacientes en el árbol
     * @return El tamaño del árbol
     */
    public int tamaño() {
        return tamaño;
    }
    
    /**
     * Verifica si el árbol está vacío
     * @return true si está vacío
     */
    public boolean estaVacio() {
        return tamaño == 0;
    }
    
    /**
     * Limpia completamente el árbol
     */
    public void limpiar() {
        raiz = null;
        tamaño = 0;
    }
    
    // Métodos auxiliares para balanceo AVL
    
    private int obtenerAltura(Nodo nodo) {
        return (nodo == null) ? 0 : nodo.altura;
    }
    
    private int obtenerBalance(Nodo nodo) {
        return (nodo == null) ? 0 : obtenerAltura(nodo.izquierdo) - obtenerAltura(nodo.derecho);
    }
    
    private Nodo rotacionDerecha(Nodo y) {
        Nodo x = y.izquierdo;
        Nodo T2 = x.derecho;
        
        // Realizar rotación
        x.derecho = y;
        y.izquierdo = T2;
        
        // Actualizar alturas
        y.altura = 1 + Math.max(obtenerAltura(y.izquierdo), obtenerAltura(y.derecho));
        x.altura = 1 + Math.max(obtenerAltura(x.izquierdo), obtenerAltura(x.derecho));
        
        return x;
    }
    
    private Nodo rotacionIzquierda(Nodo x) {
        Nodo y = x.derecho;
        Nodo T2 = y.izquierdo;
        
        // Realizar rotación
        y.izquierdo = x;
        x.derecho = T2;
        
        // Actualizar alturas
        x.altura = 1 + Math.max(obtenerAltura(x.izquierdo), obtenerAltura(x.derecho));
        y.altura = 1 + Math.max(obtenerAltura(y.izquierdo), obtenerAltura(y.derecho));
        
        return y;
    }
    
    private Nodo encontrarMinimo(Nodo nodo) {
        while (nodo.izquierdo != null) {
            nodo = nodo.izquierdo;
        }
        return nodo;
    }
    
    /**
     * Obtiene información de diagnóstico del árbol
     * @return String con información del árbol
     */
    public String obtenerInformacionArbol() {
        StringBuilder sb = new StringBuilder();
        sb.append("PacienteBST - Información del Árbol:\\n");
        sb.append("Tamaño: ").append(tamaño).append("\\n");
        sb.append("Altura: ").append(obtenerAltura(raiz)).append("\\n");
        sb.append("Balanceado: ").append(estaBalanceado() ? "Sí" : "No").append("\\n");
        
        return sb.toString();
    }
    
    /**
     * Verifica si el árbol está balanceado
     */
    private boolean estaBalanceado() {
        return estaBalanceadoRecursivo(raiz);
    }
    
    private boolean estaBalanceadoRecursivo(Nodo nodo) {
        if (nodo == null) {
            return true;
        }
        
        int balance = Math.abs(obtenerBalance(nodo));
        
        return balance <= 1 && 
               estaBalanceadoRecursivo(nodo.izquierdo) && 
               estaBalanceadoRecursivo(nodo.derecho);
    }
    
    @Override
    public String toString() {
        if (estaVacio()) {
            return "PacienteBST vacío";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("PacienteBST - ").append(tamaño).append(" pacientes:\\n");
        
        List<Paciente> ordenados = obtenerTodosOrdenados();
        for (int i = 0; i < Math.min(5, ordenados.size()); i++) {
            Paciente p = ordenados.get(i);
            sb.append("  ").append(p.getCurp()).append(" - ").append(p.getNombreCompleto()).append("\\n");
        }
        
        if (ordenados.size() > 5) {
            sb.append("  ... y ").append(ordenados.size() - 5).append(" más");
        }
        
        return sb.toString();
    }
}