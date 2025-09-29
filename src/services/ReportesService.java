package services;

import dao.CitaMedicaDAO;
import dao.UsuarioDAO;
import dao.PacienteDAO;
import dao.RegistroTriageDAO;
import dao.AtencionMedicaDAO;
import dao.DatosSocialesDAO;
import models.TipoUsuario;
import models.Especialidad;
import models.NivelUrgencia;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Map;
import java.util.HashMap;

/**
 * Servicio de reportes y estadísticas del hospital
 * Genera reportes administrativos y médicos
 */
public class ReportesService {
    
    private final CitaMedicaDAO citaMedicaDAO;
    private final UsuarioDAO usuarioDAO;
    private final PacienteDAO pacienteDAO;
    private final RegistroTriageDAO triageDAO;
    private final AtencionMedicaDAO atencionDAO;
    private final DatosSocialesDAO datosSocialesDAO;
    private final AuthenticationService authService;
    
    /**
     * Constructor del servicio de reportes
     */
    public ReportesService() {
        this.citaMedicaDAO = new CitaMedicaDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.pacienteDAO = new PacienteDAO();
        this.triageDAO = new RegistroTriageDAO();
        this.atencionDAO = new AtencionMedicaDAO();
        this.datosSocialesDAO = new DatosSocialesDAO();
        this.authService = new AuthenticationService();
    }
    
    /**
     * Genera reporte general del sistema
     * @param tokenSesion Token de sesión
     * @return Reporte general del sistema
     */
    public ReporteGeneral generarReporteGeneral(String tokenSesion) {
        if (!authService.tienePermiso(tokenSesion, AuthenticationService.Permiso.VER_REPORTES_ADMINISTRATIVOS)) {
            return null;
        }
        
        try {
            ReporteGeneral reporte = new ReporteGeneral();
            reporte.setFechaGeneracion(LocalDateTime.now());
            
            // Estadísticas de usuarios
            int totalUsuarios = usuarioDAO.contarTotal();
            int usuariosActivos = usuarioDAO.contarActivos();
            Map<TipoUsuario, Integer> usuariosPorTipo = usuarioDAO.contarPorTipo();
            
            reporte.setTotalUsuarios(totalUsuarios);
            reporte.setUsuariosActivos(usuariosActivos);
            reporte.setUsuariosPorTipo(usuariosPorTipo);
            
            // Estadísticas de pacientes
            int totalPacientes = pacienteDAO.contarTotal();
            int pacientesActivosMes = pacienteDAO.contarActivosUltimoMes();
            
            reporte.setTotalPacientes(totalPacientes);
            reporte.setPacientesActivosMes(pacientesActivosMes);
            
            // Estadísticas de citas
            List<CitaMedicaDAO.ConteoEstado> citasPorEstado = citaMedicaDAO.contarPorEstado();
            int citasHoy = citaMedicaDAO.contarCitasHoy();
            int citasSemana = citaMedicaDAO.contarCitasSemana();
            
            reporte.setCitasPorEstado(citasPorEstado);
            reporte.setCitasHoy(citasHoy);
            reporte.setCitasSemana(citasSemana);
            
            // Estadísticas de triage
            Map<NivelUrgencia, Integer> triagePorUrgencia = triageDAO.contarPorUrgencia();
            int triageHoy = triageDAO.contarTriageHoy();
            
            reporte.setTriagePorUrgencia(triagePorUrgencia);
            reporte.setTriageHoy(triageHoy);
            
            // Estadísticas de atenciones médicas
            int atencionesHoy = atencionDAO.contarAtencionesHoy();
            int atencionesSemana = atencionDAO.contarAtencionesSemana();
            
            reporte.setAtencionesHoy(atencionesHoy);
            reporte.setAtencionesSemana(atencionesSemana);
            
            return reporte;
            
        } catch (SQLException e) {
            System.err.println("Error al generar reporte general: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Genera reporte de productividad médica
     * @param tokenSesion Token de sesión
     * @param fechaInicio Fecha de inicio del reporte
     * @param fechaFin Fecha fin del reporte
     * @return Reporte de productividad médica
     */
    public ReporteProductividadMedica generarReporteProductividad(String tokenSesion, 
                                                                LocalDate fechaInicio, 
                                                                LocalDate fechaFin) {
        if (!authService.tienePermiso(tokenSesion, AuthenticationService.Permiso.VER_REPORTES_MEDICOS)) {
            return null;
        }
        
        ReporteProductividadMedica reporte = new ReporteProductividadMedica();
        reporte.setFechaInicio(fechaInicio);
        reporte.setFechaFin(fechaFin);
        reporte.setFechaGeneracion(LocalDateTime.now());
        
        // Obtener estadísticas por médico
        List<dao.AtencionMedicaDAO.EstadisticasMedico> estadisticasMedicosDAO = atencionDAO.obtenerEstadisticasPorMedico(fechaInicio, fechaFin);
        List<EstadisticasMedico> estadisticasMedicos = new ArrayList<>();
        for (dao.AtencionMedicaDAO.EstadisticasMedico stat : estadisticasMedicosDAO) {
            estadisticasMedicos.add(new EstadisticasMedico(stat.getMedicoNombre(), stat.getTotalConsultas(), 0.0));
        }
        reporte.setEstadisticasPorMedico(estadisticasMedicos);
        
        // Estadísticas por especialidad
        try {
            List<AtencionMedicaDAO.ConteoEspecialidad> conteoLista = atencionDAO.contarPorEspecialidad();
            Map<Especialidad, Integer> atencionesPorEspecialidad = new HashMap<>();
            for (AtencionMedicaDAO.ConteoEspecialidad conteo : conteoLista) {
                atencionesPorEspecialidad.put(conteo.especialidad, conteo.cantidad);
            }
            reporte.setAtencionesPorEspecialidad(atencionesPorEspecialidad);
        } catch (SQLException e) {
            System.err.println("Error al obtener estadísticas por especialidad: " + e.getMessage());
        }
        
        // Promedios generales
        double promedioAtencionesporMedico = estadisticasMedicos.stream()
            .mapToInt(EstadisticasMedico::getTotalAtenciones)
            .average()
            .orElse(0.0);
        
        reporte.setPromedioAtencionesPorMedico(promedioAtencionesporMedico);
        
        return reporte;
    }
    
    /**
     * Genera reporte de triage
     * @param tokenSesion Token de sesión
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha fin
     * @return Reporte de triage
     */
    public ReporteTriage generarReporteTriage(String tokenSesion, 
                                            LocalDate fechaInicio, 
                                            LocalDate fechaFin) {
        if (!authService.tienePermiso(tokenSesion, AuthenticationService.Permiso.VER_REPORTES_MEDICOS)) {
            return null;
        }
        
        try {
            ReporteTriage reporte = new ReporteTriage();
            reporte.setFechaInicio(fechaInicio);
            reporte.setFechaFin(fechaFin);
            reporte.setFechaGeneracion(LocalDateTime.now());
            
            // Estadísticas por nivel de urgencia
            Map<NivelUrgencia, Integer> triagePorUrgencia = triageDAO.contarPorUrgencia(fechaInicio, fechaFin);
            reporte.setTriagePorUrgencia(triagePorUrgencia);
            
            // Tiempo promedio de espera por urgencia
            Map<NivelUrgencia, Double> tiempoPromedioPorUrgencia = triageDAO.calcularTiempoPromedioPorUrgencia(fechaInicio, fechaFin);
            reporte.setTiempoPromedioPorUrgencia(tiempoPromedioPorUrgencia);
            
            // Distribución por día de la semana
            Map<String, Integer> distribucionPorDia = triageDAO.obtenerDistribucionPorDia(fechaInicio, fechaFin);
            reporte.setDistribucionPorDia(distribucionPorDia);
            
            // Total de evaluaciones
            int totalEvaluaciones = triagePorUrgencia.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
            reporte.setTotalEvaluaciones(totalEvaluaciones);
            
            return reporte;
        } catch (Exception e) {
            System.err.println("Error al generar reporte de triage: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Genera reporte financiero básico
     * @param tokenSesion Token de sesión
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha fin
     * @return Reporte financiero
     */
    public ReporteFinanciero generarReporteFinanciero(String tokenSesion, 
                                                    LocalDate fechaInicio, 
                                                    LocalDate fechaFin) {
        if (!authService.tienePermiso(tokenSesion, AuthenticationService.Permiso.VER_REPORTES_ADMINISTRATIVOS)) {
            return null;
        }
        
        try {
            ReporteFinanciero reporte = new ReporteFinanciero();
            reporte.setFechaInicio(fechaInicio);
            reporte.setFechaFin(fechaFin);
            reporte.setFechaGeneracion(LocalDateTime.now());
            
            // Contar servicios prestados
            int consultasRealizadas = atencionDAO.contarAtenciones(fechaInicio, fechaFin);
            int evaluacionesTriage = triageDAO.contarEvaluaciones(fechaInicio, fechaFin);
            int evaluacionesSociales = datosSocialesDAO.contarEvaluaciones(fechaInicio, fechaFin);
            
            // Calcular ingresos estimados (valores ficticios para ejemplo)
            double tarifaConsulta = 500.0;
            double tarifaTriage = 200.0;
            double tarifaEvaluacionSocial = 300.0;
            
            double ingresoConsultas = consultasRealizadas * tarifaConsulta;
            double ingresoTriage = evaluacionesTriage * tarifaTriage;
            double ingresoEvaluaciones = evaluacionesSociales * tarifaEvaluacionSocial;
            
            double ingresoTotal = ingresoConsultas + ingresoTriage + ingresoEvaluaciones;
            
            reporte.setConsultasRealizadas(consultasRealizadas);
            reporte.setEvaluacionesTriage(evaluacionesTriage);
            reporte.setEvaluacionesSociales(evaluacionesSociales);
            reporte.setIngresoEstimado(ingresoTotal);
            
            return reporte;
        } catch (Exception e) {
            System.err.println("Error al generar reporte financiero: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Genera reporte de ocupación hospitalaria
     * @param tokenSesion Token de sesión
     * @return Reporte de ocupación
     */
    public ReporteOcupacion generarReporteOcupacion(String tokenSesion) {
        if (!authService.tienePermiso(tokenSesion, AuthenticationService.Permiso.VER_REPORTES_ADMINISTRATIVOS)) {
            return null;
        }
        
        try {
            ReporteOcupacion reporte = new ReporteOcupacion();
            reporte.setFechaGeneracion(LocalDateTime.now());
            
            // Citas programadas hoy
            int citasHoy = citaMedicaDAO.contarCitasHoy();
            
            // Pacientes en triage
            int pacientesTriage = triageDAO.contarPacientesEnEspera();
            
            // Consultas en progreso
            int consultasEnProgreso = atencionDAO.contarConsultasEnProgreso();
            
            // Capacidad estimada (valores ficticios)
            int capacidadMaximaCitas = 100;
            int capacidadMaximaTriage = 20;
            int capacidadMaximaConsultas = 30;
            
            double ocupacionCitas = (citasHoy * 100.0) / capacidadMaximaCitas;
            double ocupacionTriage = (pacientesTriage * 100.0) / capacidadMaximaTriage;
            double ocupacionConsultas = (consultasEnProgreso * 100.0) / capacidadMaximaConsultas;
            
            reporte.setCitasHoy(citasHoy);
            reporte.setPacientesTriage(pacientesTriage);
            reporte.setConsultasEnProgreso(consultasEnProgreso);
            reporte.setPorcentajeOcupacionCitas(ocupacionCitas);
            reporte.setPorcentajeOcupacionTriage(ocupacionTriage);
            reporte.setPorcentajeOcupacionConsultas(ocupacionConsultas);
            
            return reporte;
        } catch (Exception e) {
            System.err.println("Error al generar reporte de ocupación: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Exporta reporte a formato CSV
     * @param tokenSesion Token de sesión
     * @param tipoReporte Tipo de reporte a exportar
     * @param parametros Parámetros del reporte
     * @return String con contenido CSV
     */
    public String exportarReporteCSV(String tokenSesion, String tipoReporte, Map<String, Object> parametros) {
        if (!authService.tienePermiso(tokenSesion, AuthenticationService.Permiso.EXPORTAR_REPORTES)) {
            return null;
        }
        
        StringBuilder csv = new StringBuilder();
        
        try {
            switch (tipoReporte.toUpperCase()) {
                case "GENERAL":
                    ReporteGeneral reporteGeneral = generarReporteGeneral(tokenSesion);
                    if (reporteGeneral != null) {
                        csv = construirCSVReporteGeneral(reporteGeneral);
                    }
                    break;
                    
                case "PRODUCTIVIDAD":
                    LocalDate fechaInicio = (LocalDate) parametros.get("fechaInicio");
                    LocalDate fechaFin = (LocalDate) parametros.get("fechaFin");
                    ReporteProductividadMedica reporteProductividad = generarReporteProductividad(tokenSesion, fechaInicio, fechaFin);
                    if (reporteProductividad != null) {
                        csv = construirCSVProductividad(reporteProductividad);
                    }
                    break;
                    
                case "TRIAGE":
                    fechaInicio = (LocalDate) parametros.get("fechaInicio");
                    fechaFin = (LocalDate) parametros.get("fechaFin");
                    ReporteTriage reporteTriage = generarReporteTriage(tokenSesion, fechaInicio, fechaFin);
                    if (reporteTriage != null) {
                        csv = construirCSVTriage(reporteTriage);
                    }
                    break;
                    
                default:
                    return null;
            }
            
        } catch (Exception e) {
            System.err.println("Error al exportar reporte CSV: " + e.getMessage());
            return null;
        }
        
        return csv.toString();
    }
    
    // Métodos privados para construcción de CSV
    
    private StringBuilder construirCSVReporteGeneral(ReporteGeneral reporte) {
        StringBuilder csv = new StringBuilder();
        csv.append("REPORTE GENERAL DEL SISTEMA\n");
        csv.append("Fecha de Generación,").append(reporte.getFechaGeneracion()).append("\n\n");
        
        csv.append("USUARIOS\n");
        csv.append("Total Usuarios,").append(reporte.getTotalUsuarios()).append("\n");
        csv.append("Usuarios Activos,").append(reporte.getUsuariosActivos()).append("\n");
        
        csv.append("\nUSUARIOS POR TIPO\n");
        csv.append("Tipo,Cantidad\n");
        reporte.getUsuariosPorTipo().forEach((tipo, cantidad) -> 
            csv.append(tipo).append(",").append(cantidad).append("\n"));
        
        csv.append("\nPACIENTES\n");
        csv.append("Total Pacientes,").append(reporte.getTotalPacientes()).append("\n");
        csv.append("Pacientes Activos (Mes),").append(reporte.getPacientesActivosMes()).append("\n");
        
        return csv;
    }
    
    private StringBuilder construirCSVProductividad(ReporteProductividadMedica reporte) {
        StringBuilder csv = new StringBuilder();
        csv.append("REPORTE DE PRODUCTIVIDAD MÉDICA\n");
        csv.append("Período,").append(reporte.getFechaInicio()).append(" - ").append(reporte.getFechaFin()).append("\n\n");
        
        csv.append("ESTADÍSTICAS POR MÉDICO\n");
        csv.append("Médico,Total Atenciones,Promedio Diario\n");
        reporte.getEstadisticasPorMedico().forEach(estadistica -> 
            csv.append(estadistica.getNombreMedico()).append(",")
               .append(estadistica.getTotalAtenciones()).append(",")
               .append(estadistica.getPromedioDiario()).append("\n"));
        
        return csv;
    }
    
    private StringBuilder construirCSVTriage(ReporteTriage reporte) {
        StringBuilder csv = new StringBuilder();
        csv.append("REPORTE DE TRIAGE\n");
        csv.append("Período,").append(reporte.getFechaInicio()).append(" - ").append(reporte.getFechaFin()).append("\n\n");
        
        csv.append("EVALUACIONES POR URGENCIA\n");
        csv.append("Nivel de Urgencia,Cantidad\n");
        reporte.getTriagePorUrgencia().forEach((urgencia, cantidad) -> 
            csv.append(urgencia).append(",").append(cantidad).append("\n"));
        
        return csv;
    }
    
    // Clases de datos para reportes
    
    /**
     * Reporte general del sistema
     */
    public static class ReporteGeneral {
        private LocalDateTime fechaGeneracion;
        private int totalUsuarios;
        private int usuariosActivos;
        private Map<TipoUsuario, Integer> usuariosPorTipo;
        private int totalPacientes;
        private int pacientesActivosMes;
        private List<CitaMedicaDAO.ConteoEstado> citasPorEstado;
        private int citasHoy;
        private int citasSemana;
        private Map<NivelUrgencia, Integer> triagePorUrgencia;
        private int triageHoy;
        private int atencionesHoy;
        private int atencionesSemana;
        
        // Getters y setters
        public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
        public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
        
        public int getTotalUsuarios() { return totalUsuarios; }
        public void setTotalUsuarios(int totalUsuarios) { this.totalUsuarios = totalUsuarios; }
        
        public int getUsuariosActivos() { return usuariosActivos; }
        public void setUsuariosActivos(int usuariosActivos) { this.usuariosActivos = usuariosActivos; }
        
        public Map<TipoUsuario, Integer> getUsuariosPorTipo() { return usuariosPorTipo; }
        public void setUsuariosPorTipo(Map<TipoUsuario, Integer> usuariosPorTipo) { this.usuariosPorTipo = usuariosPorTipo; }
        
        public int getTotalPacientes() { return totalPacientes; }
        public void setTotalPacientes(int totalPacientes) { this.totalPacientes = totalPacientes; }
        
        public int getPacientesActivosMes() { return pacientesActivosMes; }
        public void setPacientesActivosMes(int pacientesActivosMes) { this.pacientesActivosMes = pacientesActivosMes; }
        
        public List<CitaMedicaDAO.ConteoEstado> getCitasPorEstado() { return citasPorEstado; }
        public void setCitasPorEstado(List<CitaMedicaDAO.ConteoEstado> citasPorEstado) { this.citasPorEstado = citasPorEstado; }
        
        public int getCitasHoy() { return citasHoy; }
        public void setCitasHoy(int citasHoy) { this.citasHoy = citasHoy; }
        
        public int getCitasSemana() { return citasSemana; }
        public void setCitasSemana(int citasSemana) { this.citasSemana = citasSemana; }
        
        public Map<NivelUrgencia, Integer> getTriagePorUrgencia() { return triagePorUrgencia; }
        public void setTriagePorUrgencia(Map<NivelUrgencia, Integer> triagePorUrgencia) { this.triagePorUrgencia = triagePorUrgencia; }
        
        public int getTriageHoy() { return triageHoy; }
        public void setTriageHoy(int triageHoy) { this.triageHoy = triageHoy; }
        
        public int getAtencionesHoy() { return atencionesHoy; }
        public void setAtencionesHoy(int atencionesHoy) { this.atencionesHoy = atencionesHoy; }
        
        public int getAtencionesSemana() { return atencionesSemana; }
        public void setAtencionesSemana(int atencionesSemana) { this.atencionesSemana = atencionesSemana; }
    }
    
    /**
     * Estadísticas de un médico específico
     */
    public static class EstadisticasMedico {
        private String nombreMedico;
        private int totalAtenciones;
        private double promedioDiario;
        
        public EstadisticasMedico(String nombreMedico, int totalAtenciones, double promedioDiario) {
            this.nombreMedico = nombreMedico;
            this.totalAtenciones = totalAtenciones;
            this.promedioDiario = promedioDiario;
        }
        
        public String getNombreMedico() { return nombreMedico; }
        public int getTotalAtenciones() { return totalAtenciones; }
        public double getPromedioDiario() { return promedioDiario; }
    }
    
    /**
     * Reporte de productividad médica
     */
    public static class ReporteProductividadMedica {
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private LocalDateTime fechaGeneracion;
        private List<EstadisticasMedico> estadisticasPorMedico;
        private Map<Especialidad, Integer> atencionesPorEspecialidad;
        private double promedioAtencionesPorMedico;
        
        // Getters y setters
        public LocalDate getFechaInicio() { return fechaInicio; }
        public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
        
        public LocalDate getFechaFin() { return fechaFin; }
        public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
        
        public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
        public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
        
        public List<EstadisticasMedico> getEstadisticasPorMedico() { return estadisticasPorMedico; }
        public void setEstadisticasPorMedico(List<EstadisticasMedico> estadisticasPorMedico) { this.estadisticasPorMedico = estadisticasPorMedico; }
        
        public Map<Especialidad, Integer> getAtencionesPorEspecialidad() { return atencionesPorEspecialidad; }
        public void setAtencionesPorEspecialidad(Map<Especialidad, Integer> atencionesPorEspecialidad) { this.atencionesPorEspecialidad = atencionesPorEspecialidad; }
        
        public double getPromedioAtencionesPorMedico() { return promedioAtencionesPorMedico; }
        public void setPromedioAtencionesPorMedico(double promedioAtencionesPorMedico) { this.promedioAtencionesPorMedico = promedioAtencionesPorMedico; }
    }
    
    /**
     * Reporte de triage
     */
    public static class ReporteTriage {
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private LocalDateTime fechaGeneracion;
        private Map<NivelUrgencia, Integer> triagePorUrgencia;
        private Map<NivelUrgencia, Double> tiempoPromedioPorUrgencia;
        private Map<String, Integer> distribucionPorDia;
        private int totalEvaluaciones;
        
        // Getters y setters
        public LocalDate getFechaInicio() { return fechaInicio; }
        public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
        
        public LocalDate getFechaFin() { return fechaFin; }
        public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
        
        public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
        public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
        
        public Map<NivelUrgencia, Integer> getTriagePorUrgencia() { return triagePorUrgencia; }
        public void setTriagePorUrgencia(Map<NivelUrgencia, Integer> triagePorUrgencia) { this.triagePorUrgencia = triagePorUrgencia; }
        
        public Map<NivelUrgencia, Double> getTiempoPromedioPorUrgencia() { return tiempoPromedioPorUrgencia; }
        public void setTiempoPromedioPorUrgencia(Map<NivelUrgencia, Double> tiempoPromedioPorUrgencia) { this.tiempoPromedioPorUrgencia = tiempoPromedioPorUrgencia; }
        
        public Map<String, Integer> getDistribucionPorDia() { return distribucionPorDia; }
        public void setDistribucionPorDia(Map<String, Integer> distribucionPorDia) { this.distribucionPorDia = distribucionPorDia; }
        
        public int getTotalEvaluaciones() { return totalEvaluaciones; }
        public void setTotalEvaluaciones(int totalEvaluaciones) { this.totalEvaluaciones = totalEvaluaciones; }
    }
    
    /**
     * Reporte financiero básico
     */
    public static class ReporteFinanciero {
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private LocalDateTime fechaGeneracion;
        private int consultasRealizadas;
        private int evaluacionesTriage;
        private int evaluacionesSociales;
        private double ingresoEstimado;
        
        // Getters y setters
        public LocalDate getFechaInicio() { return fechaInicio; }
        public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
        
        public LocalDate getFechaFin() { return fechaFin; }
        public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
        
        public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
        public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
        
        public int getConsultasRealizadas() { return consultasRealizadas; }
        public void setConsultasRealizadas(int consultasRealizadas) { this.consultasRealizadas = consultasRealizadas; }
        
        public int getEvaluacionesTriage() { return evaluacionesTriage; }
        public void setEvaluacionesTriage(int evaluacionesTriage) { this.evaluacionesTriage = evaluacionesTriage; }
        
        public int getEvaluacionesSociales() { return evaluacionesSociales; }
        public void setEvaluacionesSociales(int evaluacionesSociales) { this.evaluacionesSociales = evaluacionesSociales; }
        
        public double getIngresoEstimado() { return ingresoEstimado; }
        public void setIngresoEstimado(double ingresoEstimado) { this.ingresoEstimado = ingresoEstimado; }
    }
    
    /**
     * Reporte de ocupación hospitalaria
     */
    public static class ReporteOcupacion {
        private LocalDateTime fechaGeneracion;
        private int citasHoy;
        private int pacientesTriage;
        private int consultasEnProgreso;
        private double porcentajeOcupacionCitas;
        private double porcentajeOcupacionTriage;
        private double porcentajeOcupacionConsultas;
        
        // Getters y setters
        public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
        public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
        
        public int getCitasHoy() { return citasHoy; }
        public void setCitasHoy(int citasHoy) { this.citasHoy = citasHoy; }
        
        public int getPacientesTriage() { return pacientesTriage; }
        public void setPacientesTriage(int pacientesTriage) { this.pacientesTriage = pacientesTriage; }
        
        public int getConsultasEnProgreso() { return consultasEnProgreso; }
        public void setConsultasEnProgreso(int consultasEnProgreso) { this.consultasEnProgreso = consultasEnProgreso; }
        
        public double getPorcentajeOcupacionCitas() { return porcentajeOcupacionCitas; }
        public void setPorcentajeOcupacionCitas(double porcentajeOcupacionCitas) { this.porcentajeOcupacionCitas = porcentajeOcupacionCitas; }
        
        public double getPorcentajeOcupacionTriage() { return porcentajeOcupacionTriage; }
        public void setPorcentajeOcupacionTriage(double porcentajeOcupacionTriage) { this.porcentajeOcupacionTriage = porcentajeOcupacionTriage; }
        
        public double getPorcentajeOcupacionConsultas() { return porcentajeOcupacionConsultas; }
        public void setPorcentajeOcupacionConsultas(double porcentajeOcupacionConsultas) { this.porcentajeOcupacionConsultas = porcentajeOcupacionConsultas; }
    }
}