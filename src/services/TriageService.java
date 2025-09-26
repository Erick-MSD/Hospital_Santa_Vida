package services;

import models.RegistroTriage;
import models.Usuario;
import structures.TriageQueue;
import structures.HistorialPaciente;
import utils.DatabaseConnection;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio principal del sistema de triage
 * Maneja las estructuras de datos en memoria y las operaciones de triage
 */
public class TriageService {
    
    // Estructuras de datos principales
    private final TriageQueue colasTriage;
    private final Map<Integer, HistorialPaciente> historiales;
    
    // DAOs para acceso a datos
    private final DatabaseConnection dbConnection;
    
    // Servicio de autenticación
    private final AuthenticationService authService;
    
    // Contadores y estadísticas
    private int totalPacientesAtendidosHoy;
    private final Map<RegistroTriage.NivelUrgencia, Integer> contadorPorNivel;
    
    public TriageService(AuthenticationService authService) {
        this.colasTriage = new TriageQueue();
        this.historiales = new ConcurrentHashMap<>();
        this.dbConnection = DatabaseConnection.getInstance();
        this.authService = authService;
        this.totalPacientesAtendidosHoy = 0;
        this.contadorPorNivel = new EnumMap<>(RegistroTriage.NivelUrgencia.class);
        
        // Inicializar contadores
        for (RegistroTriage.NivelUrgencia nivel : RegistroTriage.NivelUrgencia.values()) {
            contadorPorNivel.put(nivel, 0);
        }
        
        // Cargar datos pendientes al inicializar
        cargarDatosPendientes();
    }
    
    /**
     * Registra un nuevo paciente en triage
     */
    public RegistroTriage registrarLlegadaPaciente(int pacienteId, String motivoConsulta, String sintomas) {
        try {
            authService.requireRole(Usuario.TipoUsuario.MEDICO_TRIAGE);
            
            Usuario medicoTriage = authService.getUsuarioActual();
            
            // Crear nuevo registro de triage
            RegistroTriage registro = new RegistroTriage(pacienteId, medicoTriage.getId(), motivoConsulta);
            registro.setSintomasPrincipales(sintomas);
            
            // Insertar en base de datos
            int registroId = insertarRegistroTriage(registro);
            registro.setId(registroId);
            
            // Crear historial para este paciente
            HistorialPaciente historial = new HistorialPaciente(registroId);
            historial.agregarEvento("LLEGADA", 
                    "Paciente llega a urgencias - Motivo: " + motivoConsulta,
                    medicoTriage.getId(), 
                    medicoTriage.getNombreCompleto());
            
            historiales.put(registroId, historial);
            
            System.out.println("Paciente registrado en triage: " + registro.getFolio());
            return registro;
            
        } catch (SQLException e) {
            System.err.println("Error al registrar llegada: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Completa la evaluación de triage asignando nivel y signos vitales
     */
    public boolean completarTriage(int registroId, RegistroTriage.NivelUrgencia nivel,
                                 String especialidad, int presionSist, int presionDiast,
                                 int frecCardiaca, int frecRespiratoria, 
                                 BigDecimal temperatura, int saturacion, 
                                 int glasgow, String observaciones) {
        try {
            authService.requireRole(Usuario.TipoUsuario.MEDICO_TRIAGE);
            
            Usuario medicoTriage = authService.getUsuarioActual();
            
            // Buscar registro en base de datos
            RegistroTriage registro = buscarRegistroPorId(registroId);
            if (registro == null) {
                System.err.println("Registro de triage no encontrado: " + registroId);
                return false;
            }
            
            // Actualizar datos del triage
            registro.setNivelUrgencia(nivel);
            registro.setEspecialidadAsignada(especialidad);
            registro.setPresionSistolica(presionSist);
            registro.setPresionDiastolica(presionDiast);
            registro.setFrecuenciaCardiaca(frecCardiaca);
            registro.setFrecuenciaRespiratoria(frecRespiratoria);
            registro.setTemperatura(temperatura);
            registro.setSaturacionOxigeno(saturacion);
            registro.setGlasgow(glasgow);
            registro.setObservacionesTriage(observaciones);
            registro.setFechaHoraTriage(LocalDateTime.now());
            
            // Actualizar en base de datos
            if (!actualizarRegistroTriage(registro)) {
                return false;
            }
            
            // Agregar a las colas de triage
            colasTriage.agregarPaciente(registro);
            
            // Actualizar historial
            HistorialPaciente historial = historiales.get(registroId);
            if (historial != null) {
                historial.agregarEvento("TRIAGE_COMPLETADO",
                        String.format("Nivel %s asignado - Especialidad: %s - PA: %d/%d", 
                                nivel, especialidad, presionSist, presionDiast),
                        medicoTriage.getId(),
                        medicoTriage.getNombreCompleto());
            }
            
            // Actualizar contadores
            contadorPorNivel.merge(nivel, 1, Integer::sum);
            
            System.out.println("Triage completado para " + registro.getFolio() + " - Nivel: " + nivel);
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error al completar triage: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene el siguiente paciente que debe ser atendido
     */
    public RegistroTriage obtenerSiguientePaciente() {
        try {
            authService.requireAuthentication();
            
            RegistroTriage siguiente = colasTriage.obtenerSiguientePaciente();
            
            if (siguiente != null) {
                // Actualizar historial
                HistorialPaciente historial = historiales.get(siguiente.getId());
                if (historial != null) {
                    historial.agregarEvento("LLAMADO_ATENCION",
                            "Paciente llamado para atención médica",
                            authService.getUsuarioActual().getId(),
                            authService.getUsuarioActual().getNombreCompleto());
                }
                
                // Actualizar estado en base de datos
                actualizarEstadoRegistro(siguiente.getId(), RegistroTriage.Estado.EN_ATENCION);
            }
            
            return siguiente;
            
        } catch (Exception e) {
            System.err.println("Error al obtener siguiente paciente: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtiene el siguiente paciente nivel AZUL para cita ambulatoria
     */
    public RegistroTriage obtenerSiguientePacienteAzul() {
        try {
            authService.requireRole(Usuario.TipoUsuario.ASISTENTE_MEDICA);
            
            RegistroTriage siguiente = colasTriage.obtenerSiguientePacienteAzul();
            
            if (siguiente != null) {
                // Actualizar historial
                HistorialPaciente historial = historiales.get(siguiente.getId());
                if (historial != null) {
                    historial.agregarEvento("PREPARACION_CITA",
                            "Preparando cita médica ambulatoria",
                            authService.getUsuarioActual().getId(),
                            authService.getUsuarioActual().getNombreCompleto());
                }
            }
            
            return siguiente;
            
        } catch (Exception e) {
            System.err.println("Error al obtener paciente azul: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Busca un paciente por folio
     */
    public RegistroTriage buscarPacientePorFolio(String folio) {
        // Primero buscar en memoria
        RegistroTriage registro = colasTriage.buscarPorFolio(folio);
        
        // Si no está en memoria, buscar en base de datos
        if (registro == null) {
            try {
                registro = buscarRegistroPorFolio(folio);
            } catch (SQLException e) {
                System.err.println("Error al buscar por folio: " + e.getMessage());
            }
        }
        
        return registro;
    }
    
    /**
     * Obtiene la lista actual de sala de espera
     */
    public List<RegistroTriage> obtenerSalaEspera() {
        return colasTriage.obtenerSalaEspera();
    }
    
    /**
     * Obtiene pacientes nivel AZUL esperando cita
     */
    public List<RegistroTriage> obtenerPacientesAzul() {
        return colasTriage.obtenerPacientesAzul();
    }
    
    /**
     * Calcula tiempo estimado de espera para un nivel específico
     */
    public int calcularTiempoEspera(RegistroTriage.NivelUrgencia nivel) {
        return colasTriage.calcularTiempoEsperaEstimado(nivel);
    }
    
    /**
     * Obtiene el historial completo de un paciente
     */
    public List<String> obtenerHistorialPaciente(int registroTriageId) {
        HistorialPaciente historial = historiales.get(registroTriageId);
        if (historial != null) {
            return historial.obtenerHistorialCompleto();
        }
        return new ArrayList<>();
    }
    
    /**
     * Completa la atención de un paciente (lo remueve de las colas)
     */
    public boolean completarAtencionPaciente(int registroTriageId) {
        try {
            authService.requireAuthentication();
            
            // Buscar registro
            RegistroTriage registro = colasTriage.buscarPorId(registroTriageId);
            if (registro == null) {
                registro = buscarRegistroPorId(registroTriageId);
            }
            
            if (registro != null) {
                // Remover de colas
                colasTriage.removerPaciente(registro);
                
                // Actualizar historial
                HistorialPaciente historial = historiales.get(registroTriageId);
                if (historial != null) {
                    historial.agregarEvento("ATENCION_COMPLETADA",
                            "Atención médica completada",
                            authService.getUsuarioActual().getId(),
                            authService.getUsuarioActual().getNombreCompleto());
                }
                
                // Actualizar estado en base de datos
                actualizarEstadoRegistro(registroTriageId, RegistroTriage.Estado.COMPLETADO);
                
                totalPacientesAtendidosHoy++;
                System.out.println("Atención completada para: " + registro.getFolio());
                return true;
            }
            
        } catch (Exception e) {
            System.err.println("Error al completar atención: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Obtiene estadísticas actuales del sistema
     */
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("pacientesEnEspera", colasTriage.getTotalPacientesEspera());
        stats.put("pacientesAzul", colasTriage.getTotalPacientesAzul());
        stats.put("pacientesAtendidosHoy", totalPacientesAtendidosHoy);
        stats.put("estadisticasPorNivel", colasTriage.getEstadisticasPorNivel());
        stats.put("hayPacientesCriticos", colasTriage.hayPacientesCriticos());
        
        // Agregar información del paciente con mayor tiempo de espera
        RegistroTriage mayorEspera = colasTriage.obtenerPacienteMayorEspera();
        if (mayorEspera != null) {
            stats.put("mayorTiempoEspera", mayorEspera.getMinutosEspera());
            stats.put("pacienteMayorEspera", mayorEspera.getFolio());
        }
        
        return stats;
    }
    
    /**
     * Carga registros pendientes desde la base de datos al inicializar
     */
    private void cargarDatosPendientes() {
        try {
            System.out.println("🔍 Iniciando carga de datos pendientes...");
            
            // Primero, verificar qué pacientes hay en total
            String sqlVerificacion = """
                SELECT COUNT(*) as total_registros FROM registros_triage
                """;
            try (ResultSet rsVerif = dbConnection.executeQuery(sqlVerificacion)) {
                if (rsVerif.next()) {
                    int totalRegistros = rsVerif.getInt("total_registros");
                    System.out.println("📊 Total de registros en registros_triage: " + totalRegistros);
                }
            }
            
            // Verificar pacientes de cualquier fecha
            String sqlTodos = """
                SELECT rt.estado, DATE(rt.fecha_hora_llegada) as fecha, COUNT(*) as cantidad
                FROM registros_triage rt
                GROUP BY rt.estado, DATE(rt.fecha_hora_llegada)
                ORDER BY fecha DESC
                """;
            try (ResultSet rsTodos = dbConnection.executeQuery(sqlTodos)) {
                System.out.println("📅 Pacientes por fecha y estado:");
                while (rsTodos.next()) {
                    System.out.println("   - " + rsTodos.getString("fecha") + 
                                     " | Estado: " + rsTodos.getString("estado") + 
                                     " | Cantidad: " + rsTodos.getInt("cantidad"));
                }
            }
            
            // NUEVA CONSULTA: Ver todos los registros sin filtros
            String sqlTodosRegistros = """
                SELECT rt.id, rt.paciente_id, rt.estado, rt.fecha_hora_llegada, rt.motivo_consulta
                FROM registros_triage rt
                ORDER BY rt.id DESC
                LIMIT 5
                """;
            try (ResultSet rsTodosRegs = dbConnection.executeQuery(sqlTodosRegistros)) {
                System.out.println("🔍 Últimos 5 registros en base de datos:");
                while (rsTodosRegs.next()) {
                    System.out.println("   - ID: " + rsTodosRegs.getInt("id") + 
                                     " | Paciente: " + rsTodosRegs.getInt("paciente_id") +
                                     " | Estado: " + rsTodosRegs.getString("estado") +
                                     " | Fecha: " + rsTodosRegs.getTimestamp("fecha_hora_llegada") +
                                     " | Motivo: " + rsTodosRegs.getString("motivo_consulta"));
                }
            }
            
            String sql = """
                SELECT * FROM registros_triage rt
                LEFT JOIN pacientes p ON rt.paciente_id = p.id
                WHERE rt.estado IN ('ESPERANDO_ASISTENTE', 'ESPERANDO_TRABAJO_SOCIAL', 'ESPERANDO_MEDICO')
                AND DATE(rt.fecha_hora_llegada) = CURDATE()
                ORDER BY rt.fecha_hora_llegada
                """;
            
            System.out.println("📅 Buscando pacientes de la fecha: " + java.time.LocalDate.now());
            System.out.println("🔎 SQL Query: " + sql.replaceAll("\\s+", " ").trim());
            
            try (ResultSet rs = dbConnection.executeQuery(sql)) {
                int pacientesEncontrados = 0;
                int pacientesAgregados = 0;
                
                while (rs.next()) {
                    pacientesEncontrados++;
                    RegistroTriage registro = mapearRegistroTriage(rs);
                    
                    System.out.println("👤 Paciente encontrado: " + registro.getPacienteId() + 
                                     " | Estado: " + registro.getEstado() + 
                                     " | Fecha: " + registro.getFechaHoraLlegada() +
                                     " | Triage completo: " + registro.esTriageCompleto());
                    
                    // Solo agregar a colas si tiene triage completo
                    if (registro.esTriageCompleto()) {
                        colasTriage.agregarPaciente(registro);
                        pacientesAgregados++;
                        System.out.println("✅ Paciente agregado a cola: " + registro.getPacienteId());
                    } else {
                        System.out.println("⚠️ Paciente NO agregado (triage incompleto): " + registro.getPacienteId());
                    }
                    
                    // Crear historial básico
                    HistorialPaciente historial = new HistorialPaciente(registro.getId());
                    historial.agregarEventoConFecha(registro.getFechaHoraLlegada(),
                            "LLEGADA", "Paciente llegó a urgencias (datos cargados del sistema)",
                            registro.getMedicoTriageId(), "Sistema");
                    
                    if (registro.getFechaHoraTriage() != null) {
                        historial.agregarEventoConFecha(registro.getFechaHoraTriage(),
                                "TRIAGE_COMPLETADO", "Evaluación de triage completada (datos cargados)",
                                registro.getMedicoTriageId(), "Sistema");
                    }
                    
                    historiales.put(registro.getId(), historial);
                }
                
                System.out.println("📊 Resumen de carga:");
                System.out.println("   - Pacientes encontrados en BD: " + pacientesEncontrados);
                System.out.println("   - Pacientes agregados a colas: " + pacientesAgregados);
                System.out.println("   - Total en colas de espera: " + colasTriage.getTotalPacientesEspera());
            }
            
            System.out.println("Datos pendientes cargados: " + colasTriage.getTotalPacientesEspera() + " pacientes");
            
        } catch (SQLException e) {
            System.err.println("Error al cargar datos pendientes: " + e.getMessage());
        }
    }
    
    // Métodos privados de base de datos
    
    private int insertarRegistroTriage(RegistroTriage registro) throws SQLException {
        String sql = """
            INSERT INTO registros_triage (paciente_id, medico_triage_id, motivo_consulta, 
                                        sintomas_principales, estado) 
            VALUES (?, ?, ?, ?, ?)
            """;
        
        return dbConnection.executeInsertWithGeneratedKey(sql,
                registro.getPacienteId(),
                registro.getMedicoTriageId(),
                registro.getMotivoConsulta(),
                registro.getSintomasPrincipales(),
                registro.getEstado().name()
        );
    }
    
    private boolean actualizarRegistroTriage(RegistroTriage registro) throws SQLException {
        String sql = """
            UPDATE registros_triage SET 
                fecha_hora_triage = ?, nivel_urgencia = ?, especialidad_asignada = ?,
                presion_sistolica = ?, presion_diastolica = ?, frecuencia_cardiaca = ?,
                frecuencia_respiratoria = ?, temperatura = ?, saturacion_oxigeno = ?,
                glasgow = ?, observaciones_triage = ?, estado = ?
            WHERE id = ?
            """;
        
        int filasAfectadas = dbConnection.executeUpdate(sql,
                registro.getFechaHoraTriage(),
                registro.getNivelUrgencia() != null ? registro.getNivelUrgencia().name() : null,
                registro.getEspecialidadAsignada(),
                registro.getPresionSistolica(),
                registro.getPresionDiastolica(),
                registro.getFrecuenciaCardiaca(),
                registro.getFrecuenciaRespiratoria(),
                registro.getTemperatura(),
                registro.getSaturacionOxigeno(),
                registro.getGlasgow(),
                registro.getObservacionesTriage(),
                registro.getEstado().name(),
                registro.getId()
        );
        
        return filasAfectadas > 0;
    }
    
    private boolean actualizarEstadoRegistro(int registroId, RegistroTriage.Estado nuevoEstado) throws SQLException {
        String sql = "UPDATE registros_triage SET estado = ? WHERE id = ?";
        int filasAfectadas = dbConnection.executeUpdate(sql, nuevoEstado.name(), registroId);
        return filasAfectadas > 0;
    }
    
    private RegistroTriage buscarRegistroPorId(int id) throws SQLException {
        String sql = "SELECT * FROM registros_triage WHERE id = ?";
        
        try (ResultSet rs = dbConnection.executeQuery(sql, id)) {
            if (rs.next()) {
                return mapearRegistroTriage(rs);
            }
        }
        return null;
    }
    
    private RegistroTriage buscarRegistroPorFolio(String folio) throws SQLException {
        String sql = "SELECT * FROM registros_triage WHERE folio = ?";
        
        try (ResultSet rs = dbConnection.executeQuery(sql, folio)) {
            if (rs.next()) {
                return mapearRegistroTriage(rs);
            }
        }
        return null;
    }
    
    private RegistroTriage mapearRegistroTriage(ResultSet rs) throws SQLException {
        RegistroTriage registro = new RegistroTriage();
        
        registro.setId(rs.getInt("id"));
        registro.setFolio(rs.getString("folio"));
        registro.setPacienteId(rs.getInt("paciente_id"));
        registro.setMedicoTriageId(rs.getInt("medico_triage_id"));
        registro.setMotivoConsulta(rs.getString("motivo_consulta"));
        registro.setSintomasPrincipales(rs.getString("sintomas_principales"));
        
        // Fechas
        Timestamp llegada = rs.getTimestamp("fecha_hora_llegada");
        if (llegada != null) {
            registro.setFechaHoraLlegada(llegada.toLocalDateTime());
        }
        
        Timestamp triage = rs.getTimestamp("fecha_hora_triage");
        if (triage != null) {
            registro.setFechaHoraTriage(triage.toLocalDateTime());
        }
        
        // Signos vitales
        registro.setPresionSistolica(rs.getObject("presion_sistolica", Integer.class));
        registro.setPresionDiastolica(rs.getObject("presion_diastolica", Integer.class));
        registro.setFrecuenciaCardiaca(rs.getObject("frecuencia_cardiaca", Integer.class));
        registro.setFrecuenciaRespiratoria(rs.getObject("frecuencia_respiratoria", Integer.class));
        registro.setTemperatura(rs.getBigDecimal("temperatura"));
        registro.setSaturacionOxigeno(rs.getObject("saturacion_oxigeno", Integer.class));
        registro.setGlasgow(rs.getObject("glasgow", Integer.class));
        
        // Triage
        String nivelStr = rs.getString("nivel_urgencia");
        if (nivelStr != null) {
            registro.setNivelUrgencia(RegistroTriage.NivelUrgencia.valueOf(nivelStr));
        }
        
        registro.setEspecialidadAsignada(rs.getString("especialidad_asignada"));
        registro.setObservacionesTriage(rs.getString("observaciones_triage"));
        
        // Estado
        String estadoStr = rs.getString("estado");
        if (estadoStr != null) {
            registro.setEstado(RegistroTriage.Estado.valueOf(estadoStr));
        }
        
        return registro;
    }
    
    @Override
    public String toString() {
        return "TriageService{" +
                "pacientesEnEspera=" + colasTriage.getTotalPacientesEspera() +
                ", pacientesAzul=" + colasTriage.getTotalPacientesAzul() +
                ", atendidosHoy=" + totalPacientesAtendidosHoy +
                ", hayPacientesCriticos=" + colasTriage.hayPacientesCriticos() +
                '}';
    }
}