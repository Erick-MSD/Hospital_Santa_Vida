package services;

import dao.CitaMedicaDAO;
import dao.UsuarioDAO;
import dao.PacienteDAO;
import models.CitaMedica;
import models.Especialidad;
import models.TipoUsuario;
import models.Usuario;
import models.Paciente;
import structures.EspecialidadHashMap;
import utils.ValidationUtils;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Servicio de gestión de citas médicas
 * Maneja programación, reprogramación, cancelación y seguimiento de citas
 * Incluye asignación inteligente de médicos por especialidad
 */
public class CitaService {
    
    private final CitaMedicaDAO citaMedicaDAO;
    private final UsuarioDAO usuarioDAO;
    private final PacienteDAO pacienteDAO;
    private final EspecialidadHashMap asignacionMedicos;
    private final AuthenticationService authService;
    
    // Horarios de atención
    private static final LocalTime HORA_INICIO_MANANA = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN_MANANA = LocalTime.of(14, 0);
    private static final LocalTime HORA_INICIO_TARDE = LocalTime.of(15, 0);
    private static final LocalTime HORA_FIN_TARDE = LocalTime.of(20, 0);
    private static final int DURACION_CITA_MINUTOS = 30;
    
    /**
     * Constructor del servicio de citas
     */
    public CitaService() {
        this.citaMedicaDAO = new CitaMedicaDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.pacienteDAO = new PacienteDAO();
        this.asignacionMedicos = new EspecialidadHashMap();
        this.authService = new AuthenticationService();
        
        // Cargar médicos por especialidad
        cargarMedicosPorEspecialidad();
    }
    
    /**
     * Programa una nueva cita médica
     * @param tokenSesion Token de sesión del usuario
     * @param datosCita Datos de la cita a programar
     * @return Resultado de la programación
     */
    public ResultadoProgramacionCita programarCita(String tokenSesion, DatosProgramacionCita datosCita) {
        try {
            // Verificar permisos
            if (!authService.tienePermiso(tokenSesion, AuthenticationService.Permiso.CREAR_CITAS)) {
                return new ResultadoProgramacionCita(false, "Sin permisos para programar citas", null);
            }
            
            // Obtener usuario actual
            Usuario usuarioActual = authService.obtenerUsuarioPorToken(tokenSesion);
            if (usuarioActual == null) {
                return new ResultadoProgramacionCita(false, "Sesión inválida", null);
            }
            
            // Validar datos
            String validacion = validarDatosProgramacion(datosCita);
            if (validacion != null) {
                return new ResultadoProgramacionCita(false, validacion, null);
            }
            
            // Verificar que el paciente existe
            Paciente paciente = pacienteDAO.buscarPorId(datosCita.getPacienteId());
            if (paciente == null) {
                return new ResultadoProgramacionCita(false, "Paciente no encontrado", null);
            }
            
            // Asignar médico si no se especifica
            int medicoId = datosCita.getMedicoId();
            if (medicoId <= 0) {
                medicoId = asignarMedicoAutomaticamente(datosCita.getEspecialidad(), 
                                                       datosCita.getFechaCita(), datosCita.getHoraCita());
                if (medicoId <= 0) {
                    return new ResultadoProgramacionCita(false, 
                        "No hay médicos disponibles para esa especialidad en la fecha solicitada", null);
                }
            }
            
            // Verificar disponibilidad del médico
            if (!citaMedicaDAO.verificarDisponibilidadMedico(medicoId, datosCita.getFechaCita(), 
                                                            datosCita.getHoraCita())) {
                return new ResultadoProgramacionCita(false, 
                    "El médico no está disponible en esa fecha y hora", null);
            }
            
            // Crear cita médica
            CitaMedica cita = new CitaMedica();
            cita.setPacienteId(datosCita.getPacienteId());
            cita.setMedicoId(medicoId);
            cita.setFechaCita(datosCita.getFechaCita());
            cita.setHoraCita(datosCita.getHoraCita());
            cita.setEspecialidad(datosCita.getEspecialidad());
            cita.setMotivoCita(datosCita.getMotivoCita());
            cita.setObservaciones(datosCita.getObservaciones());
            cita.setEstadoCita("PROGRAMADA");
            cita.setFechaCreacion(LocalDateTime.now());
            cita.setUsuarioCreacionId(usuarioActual.getId());
            
            // Guardar en base de datos
            if (citaMedicaDAO.insertar(cita)) {
                return new ResultadoProgramacionCita(true, 
                    "Cita programada exitosamente", cita);
            } else {
                return new ResultadoProgramacionCita(false, "Error al programar la cita", null);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al programar cita: " + e.getMessage());
            return new ResultadoProgramacionCita(false, "Error del sistema", null);
        }
    }
    
    /**
     * Reprograma una cita existente
     * @param tokenSesion Token de sesión
     * @param citaId ID de la cita a reprogramar
     * @param nuevaFecha Nueva fecha
     * @param nuevaHora Nueva hora
     * @return true si se reprogramó correctamente
     */
    public boolean reprogramarCita(String tokenSesion, int citaId, LocalDate nuevaFecha, LocalTime nuevaHora) {
        if (!authService.tienePermiso(tokenSesion, AuthenticationService.Permiso.ACTUALIZAR_CITAS)) {
            return false;
        }
        
        try {
            // Buscar cita existente
            CitaMedica cita = citaMedicaDAO.buscarPorId(citaId);
            if (cita == null) {
                return false;
            }
            
            // Validar nueva fecha y hora
            if (nuevaFecha.isBefore(LocalDate.now()) || 
                (nuevaFecha.equals(LocalDate.now()) && nuevaHora.isBefore(LocalTime.now()))) {
                return false;
            }
            
            // Verificar disponibilidad en nueva fecha/hora
            if (!citaMedicaDAO.verificarDisponibilidadMedico(cita.getMedicoId(), nuevaFecha, nuevaHora)) {
                return false;
            }
            
            // Actualizar cita
            cita.setFechaCita(nuevaFecha);
            cita.setHoraCita(nuevaHora);
            
            return citaMedicaDAO.actualizar(cita);
            
        } catch (SQLException e) {
            System.err.println("Error al reprogramar cita: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cancela una cita médica
     * @param tokenSesion Token de sesión
     * @param citaId ID de la cita a cancelar
     * @param motivo Motivo de cancelación
     * @return true si se canceló correctamente
     */
    public boolean cancelarCita(String tokenSesion, int citaId, String motivo) {
        if (!authService.tienePermiso(tokenSesion, AuthenticationService.Permiso.CANCELAR_CITAS)) {
            return false;
        }
        
        try {
            // Buscar cita
            CitaMedica cita = citaMedicaDAO.buscarPorId(citaId);
            if (cita == null) {
                return false;
            }
            
            // Actualizar estado y agregar motivo a observaciones
            String observacionesActualizadas = cita.getObservaciones() != null ? 
                cita.getObservaciones() + "\n\nCANCELADA: " + motivo : 
                "CANCELADA: " + motivo;
                
            cita.setObservaciones(observacionesActualizadas);
            
            return citaMedicaDAO.actualizarEstado(citaId, "CANCELADA");
            
        } catch (SQLException e) {
            System.err.println("Error al cancelar cita: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene citas de un paciente específico
     * @param tokenSesion Token de sesión
     * @param pacienteId ID del paciente
     * @return Lista de citas del paciente
     */
    public List<CitaMedica> obtenerCitasPorPaciente(String tokenSesion, int pacienteId) {
        if (!authService.tienePermiso(tokenSesion, AuthenticationService.Permiso.VER_CITAS)) {
            return new ArrayList<>();
        }
        
        try {
            return citaMedicaDAO.obtenerPorPaciente(pacienteId);
        } catch (SQLException e) {
            System.err.println("Error al obtener citas por paciente: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene citas de un médico específico
     * @param tokenSesion Token de sesión
     * @param medicoId ID del médico
     * @return Lista de citas del médico
     */
    public List<CitaMedica> obtenerCitasPorMedico(String tokenSesion, int medicoId) {
        if (!authService.tienePermiso(tokenSesion, AuthenticationService.Permiso.VER_CITAS)) {
            return new ArrayList<>();
        }
        
        try {
            return citaMedicaDAO.obtenerPorMedico(medicoId);
        } catch (SQLException e) {
            System.err.println("Error al obtener citas por médico: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene citas programadas para una fecha específica
     * @param tokenSesion Token de sesión
     * @param fecha Fecha a consultar
     * @return Lista de citas del día
     */
    public List<CitaMedica> obtenerCitasPorFecha(String tokenSesion, LocalDate fecha) {
        if (!authService.tienePermiso(tokenSesion, AuthenticationService.Permiso.VER_CITAS)) {
            return new ArrayList<>();
        }
        
        try {
            return citaMedicaDAO.obtenerPorFecha(fecha);
        } catch (SQLException e) {
            System.err.println("Error al obtener citas por fecha: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene citas programadas para hoy
     * @param tokenSesion Token de sesión
     * @return Lista de citas de hoy
     */
    public List<CitaMedica> obtenerCitasHoy(String tokenSesion) {
        if (!authService.tienePermiso(tokenSesion, AuthenticationService.Permiso.VER_CITAS)) {
            return new ArrayList<>();
        }
        
        try {
            return citaMedicaDAO.obtenerProgramadasHoy();
        } catch (SQLException e) {
            System.err.println("Error al obtener citas de hoy: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene las próximas citas programadas
     * @param tokenSesion Token de sesión
     * @param limite Número máximo de citas
     * @return Lista de próximas citas
     */
    public List<CitaMedica> obtenerProximasCitas(String tokenSesion, int limite) {
        if (!authService.tienePermiso(tokenSesion, AuthenticationService.Permiso.VER_CITAS)) {
            return new ArrayList<>();
        }
        
        try {
            return citaMedicaDAO.obtenerProximas(limite);
        } catch (SQLException e) {
            System.err.println("Error al obtener próximas citas: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene horarios disponibles para una fecha y especialidad
     * @param tokenSesion Token de sesión
     * @param fecha Fecha a consultar
     * @param especialidad Especialidad médica
     * @return Lista de horarios disponibles
     */
    public List<HorarioDisponible> obtenerHorariosDisponibles(String tokenSesion, 
                                                             LocalDate fecha, 
                                                             Especialidad especialidad) {
        if (!authService.tienePermiso(tokenSesion, AuthenticationService.Permiso.VER_CITAS)) {
            return new ArrayList<>();
        }
        
        List<HorarioDisponible> horariosDisponibles = new ArrayList<>();
        
        try {
            // Obtener médicos de la especialidad
            List<Usuario> medicosEspecialidad = asignacionMedicos.obtenerMedicosComoUsuario(especialidad);
            
            // Generar horarios disponibles
            for (Usuario medico : medicosEspecialidad) {
                List<LocalTime> horariosLibres = generarHorariosLibres(medico.getId(), fecha);
                for (LocalTime hora : horariosLibres) {
                    horariosDisponibles.add(new HorarioDisponible(fecha, hora, medico.getId(), 
                                                                 medico.getNombreCompleto()));
                }
            }
            
            // Ordenar por hora
            horariosDisponibles.sort((h1, h2) -> h1.getHora().compareTo(h2.getHora()));
            
        } catch (Exception e) {
            System.err.println("Error al obtener horarios disponibles: " + e.getMessage());
        }
        
        return horariosDisponibles;
    }
    
    /**
     * Marca una cita como completada
     * @param tokenSesion Token de sesión
     * @param citaId ID de la cita
     * @return true si se marcó correctamente
     */
    public boolean marcarCitaCompletada(String tokenSesion, int citaId) {
        if (!authService.tienePermiso(tokenSesion, AuthenticationService.Permiso.ACTUALIZAR_CITAS)) {
            return false;
        }
        
        try {
            return citaMedicaDAO.actualizarEstado(citaId, "COMPLETADA");
        } catch (SQLException e) {
            System.err.println("Error al marcar cita como completada: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene estadísticas de citas
     * @param tokenSesion Token de sesión
     * @return Estadísticas de citas
     */
    public EstadisticasCitas obtenerEstadisticas(String tokenSesion) {
        if (!authService.tienePermiso(tokenSesion, AuthenticationService.Permiso.VER_REPORTES_MEDICOS)) {
            return null;
        }
        
        try {
            List<CitaMedicaDAO.ConteoEstado> conteosPorEstado = citaMedicaDAO.contarPorEstado();
            
            return new EstadisticasCitas(conteosPorEstado);
            
        } catch (SQLException e) {
            System.err.println("Error al obtener estadísticas de citas: " + e.getMessage());
            return null;
        }
    }
    
    // Métodos privados auxiliares
    
    /**
     * Asigna automáticamente un médico disponible para una especialidad
     */
    private int asignarMedicoAutomaticamente(Especialidad especialidad, LocalDate fecha, LocalTime hora) {
        try {
            List<Usuario> medicosEspecialidad = asignacionMedicos.obtenerMedicosComoUsuario(especialidad);
            
            for (Usuario medico : medicosEspecialidad) {
                if (citaMedicaDAO.verificarDisponibilidadMedico(medico.getId(), fecha, hora)) {
                    return medico.getId();
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al asignar médico: " + e.getMessage());
        }
        
        return -1; // No hay médicos disponibles
    }
    
    /**
     * Genera lista de horarios libres para un médico en una fecha
     */
    private List<LocalTime> generarHorariosLibres(int medicoId, LocalDate fecha) throws SQLException {
        List<LocalTime> horariosLibres = new ArrayList<>();
        
        // Obtener citas existentes del médico en esa fecha
        List<CitaMedica> citasExistentes = citaMedicaDAO.obtenerPorMedico(medicoId).stream()
            .filter(cita -> cita.getFechaCita().equals(fecha))
            .filter(cita -> !"CANCELADA".equals(cita.getEstadoCita()))
            .toList();
        
        // Generar horarios de la mañana
        LocalTime horaActual = HORA_INICIO_MANANA;
        while (horaActual.isBefore(HORA_FIN_MANANA)) {
            final LocalTime horaFinal = horaActual;
            boolean ocupado = citasExistentes.stream()
                .anyMatch(cita -> cita.getHoraCita().equals(horaFinal));
            
            if (!ocupado) {
                horariosLibres.add(horaActual);
            }
            
            horaActual = horaActual.plusMinutes(DURACION_CITA_MINUTOS);
        }
        
        // Generar horarios de la tarde
        horaActual = HORA_INICIO_TARDE;
        while (horaActual.isBefore(HORA_FIN_TARDE)) {
            final LocalTime horaFinal = horaActual;
            boolean ocupado = citasExistentes.stream()
                .anyMatch(cita -> cita.getHoraCita().equals(horaFinal));
            
            if (!ocupado) {
                horariosLibres.add(horaActual);
            }
            
            horaActual = horaActual.plusMinutes(DURACION_CITA_MINUTOS);
        }
        
        return horariosLibres;
    }
    
    /**
     * Valida los datos de programación de cita
     */
    private String validarDatosProgramacion(DatosProgramacionCita datos) {
        if (datos == null) {
            return "Datos de programación son obligatorios";
        }
        
        if (datos.getPacienteId() <= 0) {
            return "ID de paciente es obligatorio";
        }
        
        if (datos.getFechaCita() == null) {
            return "Fecha de cita es obligatoria";
        }
        
        if (datos.getFechaCita().isBefore(LocalDate.now())) {
            return "La fecha de cita no puede ser en el pasado";
        }
        
        if (datos.getHoraCita() == null) {
            return "Hora de cita es obligatoria";
        }
        
        // Validar horario de atención
        boolean enHorarioManana = !datos.getHoraCita().isBefore(HORA_INICIO_MANANA) && 
                                 datos.getHoraCita().isBefore(HORA_FIN_MANANA);
        boolean enHorarioTarde = !datos.getHoraCita().isBefore(HORA_INICIO_TARDE) && 
                                datos.getHoraCita().isBefore(HORA_FIN_TARDE);
        
        if (!enHorarioManana && !enHorarioTarde) {
            return String.format("Horario fuera de atención. Horarios: %s-%s y %s-%s",
                HORA_INICIO_MANANA, HORA_FIN_MANANA, HORA_INICIO_TARDE, HORA_FIN_TARDE);
        }
        
        if (datos.getEspecialidad() == null) {
            return "Especialidad es obligatoria";
        }
        
        if (!ValidationUtils.validarTexto(datos.getMotivoCita(), 5, 200)) {
            return "Motivo de cita debe tener entre 5 y 200 caracteres";
        }
        
        return null; // Datos válidos
    }
    
    /**
     * Carga los médicos por especialidad en el HashMap
     */
    private void cargarMedicosPorEspecialidad() {
        try {
            List<Usuario> medicos = usuarioDAO.obtenerPorTipo(TipoUsuario.MEDICO);
            
            for (Usuario medico : medicos) {
                if (medico.isActivo()) {
                    // Por simplicidad, asignamos médicos a todas las especialidades
                    // En un sistema real, esto vendría de una tabla de especialidades_medicos
                    for (Especialidad especialidad : Especialidad.values()) {
                        asignacionMedicos.asignarMedico(especialidad, medico);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al cargar médicos por especialidad: " + e.getMessage());
        }
    }
    
    // Clases de datos
    
    /**
     * Datos necesarios para programar una cita
     */
    public static class DatosProgramacionCita {
        private int pacienteId;
        private int medicoId; // Opcional, se puede asignar automáticamente
        private LocalDate fechaCita;
        private LocalTime horaCita;
        private Especialidad especialidad;
        private String motivoCita;
        private String observaciones;
        
        // Getters y setters
        public int getPacienteId() { return pacienteId; }
        public void setPacienteId(int pacienteId) { this.pacienteId = pacienteId; }
        
        public int getMedicoId() { return medicoId; }
        public void setMedicoId(int medicoId) { this.medicoId = medicoId; }
        
        public LocalDate getFechaCita() { return fechaCita; }
        public void setFechaCita(LocalDate fechaCita) { this.fechaCita = fechaCita; }
        
        public LocalTime getHoraCita() { return horaCita; }
        public void setHoraCita(LocalTime horaCita) { this.horaCita = horaCita; }
        
        public Especialidad getEspecialidad() { return especialidad; }
        public void setEspecialidad(Especialidad especialidad) { this.especialidad = especialidad; }
        
        public String getMotivoCita() { return motivoCita; }
        public void setMotivoCita(String motivoCita) { this.motivoCita = motivoCita; }
        
        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    }
    
    /**
     * Resultado de programación de cita
     */
    public static class ResultadoProgramacionCita {
        private final boolean exitoso;
        private final String mensaje;
        private final CitaMedica cita;
        
        public ResultadoProgramacionCita(boolean exitoso, String mensaje, CitaMedica cita) {
            this.exitoso = exitoso;
            this.mensaje = mensaje;
            this.cita = cita;
        }
        
        public boolean isExitoso() { return exitoso; }
        public String getMensaje() { return mensaje; }
        public CitaMedica getCita() { return cita; }
    }
    
    /**
     * Horario disponible para citas
     */
    public static class HorarioDisponible {
        private final LocalDate fecha;
        private final LocalTime hora;
        private final int medicoId;
        private final String nombreMedico;
        
        public HorarioDisponible(LocalDate fecha, LocalTime hora, int medicoId, String nombreMedico) {
            this.fecha = fecha;
            this.hora = hora;
            this.medicoId = medicoId;
            this.nombreMedico = nombreMedico;
        }
        
        public LocalDate getFecha() { return fecha; }
        public LocalTime getHora() { return hora; }
        public int getMedicoId() { return medicoId; }
        public String getNombreMedico() { return nombreMedico; }
    }
    
    /**
     * Estadísticas de citas médicas
     */
    public static class EstadisticasCitas {
        private final List<CitaMedicaDAO.ConteoEstado> conteosPorEstado;
        
        public EstadisticasCitas(List<CitaMedicaDAO.ConteoEstado> conteosPorEstado) {
            this.conteosPorEstado = conteosPorEstado;
        }
        
        public List<CitaMedicaDAO.ConteoEstado> getConteosPorEstado() { return conteosPorEstado; }
    }
}