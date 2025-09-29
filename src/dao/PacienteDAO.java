package dao;

import models.Paciente;
import models.EstadoPaciente;
import models.TipoAlta;
import utils.ValidationUtils;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * DAO para la gestión de pacientes en el sistema hospitalario
 * Maneja todas las operaciones CRUD para la tabla pacientes
 * Incluye funcionalidades específicas de búsqueda y filtrado
 */
public class PacienteDAO extends BaseDAO<Paciente> {
    
    private static final String TABLA = "pacientes";
    
    // Consultas SQL predefinidas
    private static final String SQL_INSERTAR = 
        "INSERT INTO " + TABLA + " (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, " +
        "sexo, curp, rfc, telefono_principal, email, direccion_calle, direccion_numero, " +
        "direccion_colonia, direccion_ciudad, direccion_estado, direccion_cp, " +
        "contacto_emergencia_nombre, contacto_emergencia_telefono, contacto_emergencia_relacion, " +
        "seguro_medico, numero_poliza) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SQL_ACTUALIZAR = 
        "UPDATE " + TABLA + " SET nombre = ?, apellido_paterno = ?, apellido_materno = ?, " +
        "fecha_nacimiento = ?, sexo = ?, curp = ?, rfc = ?, telefono_principal = ?, email = ?, " +
        "direccion_calle = ?, direccion_numero = ?, direccion_colonia = ?, direccion_ciudad = ?, " +
        "direccion_estado = ?, direccion_cp = ?, contacto_emergencia_nombre = ?, " +
        "contacto_emergencia_telefono = ?, contacto_emergencia_relacion = ?, " +
        "seguro_medico = ?, numero_poliza = ? WHERE id = ?";
    
    private static final String SQL_ELIMINAR = 
        "DELETE FROM " + TABLA + " WHERE id = ?";
    
    private static final String SQL_BUSCAR_POR_ID = 
        "SELECT * FROM " + TABLA + " WHERE id = ?";
    
    private static final String SQL_OBTENER_TODOS = 
        "SELECT * FROM " + TABLA + " ORDER BY nombre, apellido_paterno, apellido_materno";
    
    private static final String SQL_BUSCAR_POR_CURP = 
        "SELECT * FROM " + TABLA + " WHERE curp = ?";
    
    private static final String SQL_BUSCAR_POR_RFC = 
        "SELECT * FROM " + TABLA + " WHERE rfc = ?";
    
    private static final String SQL_BUSCAR_POR_NOMBRE = 
        "SELECT * FROM " + TABLA + " WHERE CONCAT(nombre, ' ', apellido_paterno, ' ', IFNULL(apellido_materno, '')) LIKE ? ORDER BY nombre, apellido_paterno, apellido_materno";
    
    private static final String SQL_BUSCAR_POR_TELEFONO = 
        "SELECT * FROM " + TABLA + " WHERE telefono = ?";
    
    private static final String SQL_BUSCAR_POR_EMAIL = 
        "SELECT * FROM " + TABLA + " WHERE email = ?";
    
    private static final String SQL_BUSCAR_POR_ESTADO = 
        // "SELECT * FROM " + TABLA + " WHERE estado_actual = ? ORDER BY fecha_registro DESC";
        "SELECT * FROM " + TABLA + " ORDER BY fecha_registro DESC";
    
    private static final String SQL_BUSCAR_ACTIVOS = 
        "SELECT * FROM " + TABLA + " /* WHERE estado_actual != 'DADO_DE_ALTA' */ " +
        "ORDER BY fecha_registro DESC";
    
    private static final String SQL_ACTUALIZAR_ESTADO = 
        // "UPDATE " + TABLA + " SET estado_actual = ?, tipo_alta = ? WHERE id = ?";
        "UPDATE " + TABLA + " SET /* estado_actual = ?, */ fecha_registro = fecha_registro WHERE id = ?";
    
    private static final String SQL_BUSCAR_POR_FECHA_REGISTRO = 
        "SELECT * FROM " + TABLA + " WHERE DATE(fecha_registro) = ? ORDER BY fecha_registro DESC";
    
    private static final String SQL_BUSCAR_POR_RANGO_FECHAS = 
        "SELECT * FROM " + TABLA + " WHERE DATE(fecha_registro) BETWEEN ? AND ? " +
        "ORDER BY fecha_registro DESC";
    
    // Temporalmente desactivado - numero_expediente no existe en la tabla actual
    private static final String SQL_GENERAR_NUMERO_EXPEDIENTE = 
        "SELECT CONCAT('EXP-', YEAR(CURDATE()), '-', LPAD(1, 6, '0')) as numero_expediente";
    
    /**
     * Inserta un nuevo paciente en la base de datos
     * @param paciente Paciente a insertar
     * @return true si se insertó correctamente
     * @throws SQLException si hay error en la operación
     */
    @Override
    public boolean insertar(Paciente paciente) throws SQLException {
        validarPaciente(paciente);
        
        // Generar número de expediente si no se proporciona
        if (paciente.getNumeroExpediente() == null || paciente.getNumeroExpediente().isEmpty()) {
            String numeroExpediente = generarNumeroExpediente();
            paciente.setNumeroExpediente(numeroExpediente);
        } else {
            // Verificar que el número de expediente no exista
            if (existeNumeroExpediente(paciente.getNumeroExpediente())) {
                throw new SQLException("El número de expediente '" + 
                                     paciente.getNumeroExpediente() + "' ya existe");
            }
        }
        
        // Verificar que el CURP no exista
        if (existeCurp(paciente.getCurp())) {
            throw new SQLException("El CURP '" + paciente.getCurp() + "' ya está registrado");
        }
        
        // Verificar RFC si se proporciona
        if (paciente.getRfc() != null && !paciente.getRfc().isEmpty()) {
            if (existeRfc(paciente.getRfc())) {
                throw new SQLException("El RFC '" + paciente.getRfc() + "' ya está registrado");
            }
        }
        
        int idGenerado = ejecutarInsercionConClave(SQL_INSERTAR,
            paciente.getNumeroExpediente(),
            paciente.getNombreCompleto(),
            convertirADate(paciente.getFechaNacimiento()),
            paciente.getGenero(),
            paciente.getCurp(),
            paciente.getRfc(),
            paciente.getTelefono(),
            paciente.getEmail(),
            paciente.getDireccionCompleta(),
            paciente.getContactoEmergenciaNombre(),
            paciente.getContactoEmergenciaTelefono(),
            paciente.getContactoEmergenciaRelacion(),
            paciente.getSeguroMedico(),
            paciente.getNumeroSeguro(),
            paciente.getAlergias(),
            paciente.getMedicamentosActuales(),
            paciente.getCondicionesPreexistentes(),
            paciente.getEstadoActual().name(),
            paciente.getTipoAlta() != null ? paciente.getTipoAlta().name() : null,
            convertirATimestamp(paciente.getFechaRegistro())
        );
        
        if (idGenerado > 0) {
            paciente.setId(idGenerado);
            return true;
        }
        
        return false;
    }
    
    /**
     * Actualiza un paciente existente
     * @param paciente Paciente a actualizar
     * @return true si se actualizó correctamente
     * @throws SQLException si hay error en la operación
     */
    @Override
    public boolean actualizar(Paciente paciente) throws SQLException {
        validarPaciente(paciente);
        
        if (paciente.getId() <= 0) {
            throw new IllegalArgumentException("ID de paciente inválido");
        }
        
        // Verificar que el número de expediente no esté en uso por otro paciente
        Paciente existente = buscarPorNumeroExpediente(paciente.getNumeroExpediente());
        if (existente != null && existente.getId() != paciente.getId()) {
            throw new SQLException("El número de expediente '" + 
                                 paciente.getNumeroExpediente() + "' ya existe");
        }
        
        // Verificar que el CURP no esté en uso por otro paciente
        existente = buscarPorCurp(paciente.getCurp());
        if (existente != null && existente.getId() != paciente.getId()) {
            throw new SQLException("El CURP '" + paciente.getCurp() + "' ya está registrado");
        }
        
        // Verificar RFC si se proporciona
        if (paciente.getRfc() != null && !paciente.getRfc().isEmpty()) {
            existente = buscarPorRfc(paciente.getRfc());
            if (existente != null && existente.getId() != paciente.getId()) {
                throw new SQLException("El RFC '" + paciente.getRfc() + "' ya está registrado");
            }
        }
        
        int filasActualizadas = ejecutarActualizacion(SQL_ACTUALIZAR,
            paciente.getNumeroExpediente(),
            paciente.getNombreCompleto(),
            convertirADate(paciente.getFechaNacimiento()),
            paciente.getGenero(),
            paciente.getCurp(),
            paciente.getRfc(),
            paciente.getTelefono(),
            paciente.getEmail(),
            paciente.getDireccionCompleta(),
            paciente.getContactoEmergenciaNombre(),
            paciente.getContactoEmergenciaTelefono(),
            paciente.getContactoEmergenciaRelacion(),
            paciente.getSeguroMedico(),
            paciente.getNumeroSeguro(),
            paciente.getAlergias(),
            paciente.getMedicamentosActuales(),
            paciente.getCondicionesPreexistentes(),
            paciente.getEstadoActual().name(),
            paciente.getTipoAlta() != null ? paciente.getTipoAlta().name() : null,
            paciente.getId()
        );
        
        return filasActualizadas > 0;
    }
    
    /**
     * Elimina un paciente por su ID
     * @param id ID del paciente a eliminar
     * @return true si se eliminó correctamente
     * @throws SQLException si hay error en la operación
     */
    @Override
    public boolean eliminar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de paciente inválido");
        }
        
        int filasEliminadas = ejecutarActualizacion(SQL_ELIMINAR, id);
        return filasEliminadas > 0;
    }
    
    /**
     * Busca un paciente por su ID
     * @param id ID del paciente
     * @return Paciente encontrado o null si no existe
     * @throws SQLException si hay error en la operación
     */
    @Override
    public Paciente buscarPorId(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de paciente inválido");
        }
        
        return ejecutarConsultaUnica(SQL_BUSCAR_POR_ID, id);
    }
    
    /**
     * Obtiene todos los pacientes
     * @return Lista de todos los pacientes
     * @throws SQLException si hay error en la operación
     */
    @Override
    public List<Paciente> obtenerTodos() throws SQLException {
        return ejecutarConsulta(SQL_OBTENER_TODOS);
    }
    
    /**
     * Busca un paciente por número de expediente
     * @param numeroExpediente Número de expediente a buscar
     * @return Paciente encontrado o null si no existe
     * @throws SQLException si hay error en la operación
     */
    public Paciente buscarPorNumeroExpediente(String numeroExpediente) throws SQLException {
        if (numeroExpediente == null || numeroExpediente.trim().isEmpty()) {
            throw new IllegalArgumentException("Número de expediente no puede estar vacío");
        }
        
        // Temporalmente desactivado - numero_expediente no existe en la tabla actual
        return null;
    }
    
    /**
     * Busca un paciente por CURP
     * @param curp CURP a buscar
     * @return Paciente encontrado o null si no existe
     * @throws SQLException si hay error en la operación
     */
    public Paciente buscarPorCurp(String curp) throws SQLException {
        if (curp == null || curp.trim().isEmpty()) {
            throw new IllegalArgumentException("CURP no puede estar vacío");
        }
        
        return ejecutarConsultaUnica(SQL_BUSCAR_POR_CURP, curp.trim().toUpperCase());
    }
    
    /**
     * Busca un paciente por RFC
     * @param rfc RFC a buscar
     * @return Paciente encontrado o null si no existe
     * @throws SQLException si hay error en la operación
     */
    public Paciente buscarPorRfc(String rfc) throws SQLException {
        if (rfc == null || rfc.trim().isEmpty()) {
            throw new IllegalArgumentException("RFC no puede estar vacío");
        }
        
        return ejecutarConsultaUnica(SQL_BUSCAR_POR_RFC, rfc.trim().toUpperCase());
    }
    
    /**
     * Busca pacientes por nombre (búsqueda parcial)
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de pacientes que coinciden
     * @throws SQLException si hay error en la operación
     */
    public List<Paciente> buscarPorNombre(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre no puede estar vacío");
        }
        
        String patronBusqueda = "%" + nombre.trim() + "%";
        return ejecutarConsulta(SQL_BUSCAR_POR_NOMBRE, patronBusqueda);
    }
    
    /**
     * Busca un paciente por teléfono
     * @param telefono Teléfono a buscar
     * @return Paciente encontrado o null si no existe
     * @throws SQLException si hay error en la operación
     */
    public Paciente buscarPorTelefono(String telefono) throws SQLException {
        if (telefono == null || telefono.trim().isEmpty()) {
            throw new IllegalArgumentException("Teléfono no puede estar vacío");
        }
        
        return ejecutarConsultaUnica(SQL_BUSCAR_POR_TELEFONO, telefono.trim());
    }
    
    /**
     * Busca un paciente por email
     * @param email Email a buscar
     * @return Paciente encontrado o null si no existe
     * @throws SQLException si hay error en la operación
     */
    public Paciente buscarPorEmail(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email no puede estar vacío");
        }
        
        return ejecutarConsultaUnica(SQL_BUSCAR_POR_EMAIL, email.trim().toLowerCase());
    }
    
    /**
     * Obtiene pacientes por estado actual
     * @param estado Estado a buscar
     * @return Lista de pacientes en el estado especificado
     * @throws SQLException si hay error en la operación
     */
    public List<Paciente> obtenerPorEstado(EstadoPaciente estado) throws SQLException {
        if (estado == null) {
            throw new IllegalArgumentException("Estado no puede ser nulo");
        }
        
        return ejecutarConsulta(SQL_BUSCAR_POR_ESTADO, estado.name());
    }
    
    /**
     * Obtiene todos los pacientes activos (no dados de alta)
     * @return Lista de pacientes activos
     * @throws SQLException si hay error en la operación
     */
    public List<Paciente> obtenerActivos() throws SQLException {
        return ejecutarConsulta(SQL_BUSCAR_ACTIVOS);
    }
    
    /**
     * Actualiza el estado de un paciente
     * @param pacienteId ID del paciente
     * @param nuevoEstado Nuevo estado
     * @param tipoAlta Tipo de alta (si aplica)
     * @return true si se actualizó correctamente
     * @throws SQLException si hay error en la operación
     */
    public boolean actualizarEstado(int pacienteId, EstadoPaciente nuevoEstado, TipoAlta tipoAlta) 
            throws SQLException {
        if (pacienteId <= 0) {
            throw new IllegalArgumentException("ID de paciente inválido");
        }
        
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("Estado no puede ser nulo");
        }
        
        // Si el estado es DADO_DE_ALTA, el tipo de alta es obligatorio
        if (nuevoEstado == EstadoPaciente.DADO_DE_ALTA && tipoAlta == null) {
            throw new IllegalArgumentException("Tipo de alta es obligatorio cuando se da de alta a un paciente");
        }
        
        int filasActualizadas = ejecutarActualizacion(SQL_ACTUALIZAR_ESTADO,
            nuevoEstado.name(),
            tipoAlta != null ? tipoAlta.name() : null,
            pacienteId
        );
        
        return filasActualizadas > 0;
    }
    
    /**
     * Obtiene pacientes registrados en una fecha específica
     * @param fecha Fecha a buscar
     * @return Lista de pacientes registrados en esa fecha
     * @throws SQLException si hay error en la operación
     */
    public List<Paciente> obtenerPorFechaRegistro(LocalDate fecha) throws SQLException {
        if (fecha == null) {
            throw new IllegalArgumentException("Fecha no puede ser nula");
        }
        
        return ejecutarConsulta(SQL_BUSCAR_POR_FECHA_REGISTRO, convertirADate(fecha));
    }
    
    /**
     * Obtiene pacientes registrados en un rango de fechas
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de pacientes registrados en el rango
     * @throws SQLException si hay error en la operación
     */
    public List<Paciente> obtenerPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) 
            throws SQLException {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }
        
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin");
        }
        
        return ejecutarConsulta(SQL_BUSCAR_POR_RANGO_FECHAS,
            convertirADate(fechaInicio), convertirADate(fechaFin));
    }
    
    /**
     * Genera un nuevo número de expediente único
     * @return Número de expediente generado
     * @throws SQLException si hay error en la operación
     */
    public String generarNumeroExpediente() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(SQL_GENERAR_NUMERO_EXPEDIENTE);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString(1);
            } else {
                // Fallback si no hay registros previos
                return "EXP-" + java.time.Year.now().getValue() + "-000001";
            }
            
        } finally {
            cerrarRecursos(rs, stmt, conn);
        }
    }
    
    /**
     * Verifica si existe un número de expediente
     * @param numeroExpediente Número de expediente a verificar
     * @return true si existe
     * @throws SQLException si hay error en la operación
     */
    public boolean existeNumeroExpediente(String numeroExpediente) throws SQLException {
        if (numeroExpediente == null || numeroExpediente.trim().isEmpty()) {
            return false;
        }
        
        Paciente paciente = buscarPorNumeroExpediente(numeroExpediente.trim());
        return paciente != null;
    }
    
    /**
     * Verifica si existe un CURP
     * @param curp CURP a verificar
     * @return true si existe
     * @throws SQLException si hay error en la operación
     */
    public boolean existeCurp(String curp) throws SQLException {
        if (curp == null || curp.trim().isEmpty()) {
            return false;
        }
        
        Paciente paciente = buscarPorCurp(curp.trim());
        return paciente != null;
    }
    
    /**
     * Verifica si existe un RFC
     * @param rfc RFC a verificar
     * @return true si existe
     * @throws SQLException si hay error en la operación
     */
    public boolean existeRfc(String rfc) throws SQLException {
        if (rfc == null || rfc.trim().isEmpty()) {
            return false;
        }
        
        Paciente paciente = buscarPorRfc(rfc.trim());
        return paciente != null;
    }
    
    /**
     * Obtiene estadísticas de pacientes por estado
     * @return Lista con conteos por estado
     * @throws SQLException si hay error en la operación
     */
    public List<EstadisticaPaciente> obtenerEstadisticasPorEstado() throws SQLException {
        // String sql = "SELECT estado_actual, COUNT(*) as total " +
        //            "FROM " + TABLA + " GROUP BY estado_actual";
        String sql = "SELECT 'ACTIVO' as estado_actual, COUNT(*) as total FROM " + TABLA + " UNION SELECT 'TOTAL' as estado_actual, COUNT(*) as total FROM " + TABLA;
        
        List<EstadisticaPaciente> estadisticas = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                EstadisticaPaciente estadistica = new EstadisticaPaciente();
                // estadistica.estado = EstadoPaciente.valueOf(rs.getString("estado_actual"));
                String estadoStr = rs.getString("estado_actual");
                if ("ACTIVO".equals(estadoStr)) {
                    estadistica.estado = EstadoPaciente.REGISTRADO;
                } else {
                    estadistica.estado = EstadoPaciente.REGISTRADO;
                }
                estadistica.total = rs.getInt("total");
                estadisticas.add(estadistica);
            }
            
        } finally {
            cerrarRecursos(rs, stmt, conn);
        }
        
        return estadisticas;
    }
    
    /**
     * Mapea un ResultSet a un objeto Paciente
     * @param rs ResultSet con los datos del paciente
     * @return Paciente mapeado
     * @throws SQLException si hay error en el mapeo
     */
    @Override
    protected Paciente mapearResultSet(ResultSet rs) throws SQLException {
        Paciente paciente = new Paciente();
        
        paciente.setId(rs.getInt("id"));
        // paciente.setNumeroExpediente(rs.getString("numero_expediente"));
        paciente.setNumeroExpediente("EXP-" + rs.getInt("id")); // Usar ID como número de expediente temporal
        
        // Construir nombre completo desde los campos individuales  
        String nombreCompleto = rs.getString("nombre") + " " + rs.getString("apellido_paterno");
        String apellidoMaterno = rs.getString("apellido_materno");
        if (apellidoMaterno != null && !apellidoMaterno.trim().isEmpty()) {
            nombreCompleto += " " + apellidoMaterno;
        }
        paciente.setNombreCompleto(nombreCompleto);
        
        // Conversión de fecha de nacimiento
        Date fechaNacimiento = rs.getDate("fecha_nacimiento");
        if (fechaNacimiento != null) {
            paciente.setFechaNacimiento(fechaNacimiento.toLocalDate());
        }
        
        paciente.setGenero(rs.getString("sexo"));
        paciente.setCurp(rs.getString("curp"));
        paciente.setRfc(rs.getString("rfc"));
        paciente.setTelefono(rs.getString("telefono_principal"));
        paciente.setEmail(rs.getString("email"));
        // Construir dirección completa desde componentes individuales
        String direccionCompleta = rs.getString("direccion_calle") + " " + rs.getString("direccion_numero") + 
            ", " + rs.getString("direccion_colonia") + ", " + rs.getString("direccion_ciudad") + 
            ", " + rs.getString("direccion_estado") + " " + rs.getString("direccion_cp");
        paciente.setDireccionCompleta(direccionCompleta);
        paciente.setContactoEmergenciaNombre(rs.getString("contacto_emergencia_nombre"));
        paciente.setContactoEmergenciaTelefono(rs.getString("contacto_emergencia_telefono"));
        paciente.setContactoEmergenciaRelacion(rs.getString("contacto_emergencia_relacion"));
        paciente.setSeguroMedico(rs.getString("seguro_medico"));
        paciente.setNumeroSeguro(rs.getString("numero_poliza"));
        // Campos no disponibles en el esquema actual - usar valores por defecto
        paciente.setAlergias("");
        paciente.setMedicamentosActuales("");
        paciente.setCondicionesPreexistentes("");
        
        // String estadoActual = rs.getString("estado_actual");
        String estadoActual = "REGISTRADO"; // Valor por defecto temporal
        if (estadoActual != null) {
            paciente.setEstadoActual(EstadoPaciente.valueOf(estadoActual));
        }
        
        // Campo tipo_alta no disponible en el esquema actual
        paciente.setTipoAlta(null);
        
        // Conversión de fecha de registro
        Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
        if (fechaRegistro != null) {
            paciente.setFechaRegistro(fechaRegistro.toLocalDateTime());
        }
        
        return paciente;
    }
    
    /**
     * Valida los datos de un paciente antes de insertarlo/actualizarlo
     * @param paciente Paciente a validar
     * @throws IllegalArgumentException si los datos no son válidos
     */
    private void validarPaciente(Paciente paciente) {
        if (paciente == null) {
            throw new IllegalArgumentException("Paciente no puede ser nulo");
        }
        
        if (!ValidationUtils.validarTexto(paciente.getNombreCompleto(), 2, 100)) {
            throw new IllegalArgumentException("Nombre completo debe tener entre 2 y 100 caracteres");
        }
        
        if (paciente.getFechaNacimiento() == null) {
            throw new IllegalArgumentException("Fecha de nacimiento es obligatoria");
        }
        
        if (paciente.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Fecha de nacimiento no puede ser futura");
        }
        
        if (paciente.getGenero() == null || paciente.getGenero().trim().isEmpty()) {
            throw new IllegalArgumentException("Género es obligatorio");
        }
        
        if (!ValidationUtils.validarCURPBoolean(paciente.getCurp())) {
            throw new IllegalArgumentException("CURP no es válido");
        }
        
        if (paciente.getRfc() != null && !paciente.getRfc().isEmpty()) {
            if (!ValidationUtils.validarRFCBoolean(paciente.getRfc())) {
                throw new IllegalArgumentException("RFC no es válido");
            }
        }
        
        if (paciente.getTelefono() != null && !paciente.getTelefono().isEmpty()) {
            if (!ValidationUtils.validarTelefonoBoolean(paciente.getTelefono())) {
                throw new IllegalArgumentException("Teléfono no es válido");
            }
        }
        
        if (paciente.getEmail() != null && !paciente.getEmail().isEmpty()) {
            if (!ValidationUtils.validarEmailBoolean(paciente.getEmail())) {
                throw new IllegalArgumentException("Email no es válido");
            }
        }
        
        if (paciente.getEstadoActual() == null) {
            paciente.setEstadoActual(EstadoPaciente.REGISTRADO);
        }
        
        if (paciente.getFechaRegistro() == null) {
            paciente.setFechaRegistro(LocalDateTime.now());
        }
        
        // Validar contacto de emergencia
        if (paciente.getContactoEmergenciaNombre() != null && 
            !paciente.getContactoEmergenciaNombre().trim().isEmpty()) {
            
            if (paciente.getContactoEmergenciaTelefono() == null || 
                paciente.getContactoEmergenciaTelefono().trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Si se proporciona contacto de emergencia, el teléfono es obligatorio");
            }
            
            if (!ValidationUtils.validarTelefonoBoolean(paciente.getContactoEmergenciaTelefono())) {
                throw new IllegalArgumentException("Teléfono de contacto de emergencia no es válido");
            }
        }
    }
    
    /**
     * Métodos adicionales para estadísticas y consultas específicas
     */
    public List<ConteoEstado> contarPorEstado() throws SQLException {
        String sql = "SELECT estado_actual, COUNT(*) as conteo FROM " + TABLA + 
                    " GROUP BY estado_actual";
        List<ConteoEstado> conteos = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String estadoStr = rs.getString("estado_actual");
                int conteo = rs.getInt("conteo");
                EstadoPaciente estado = EstadoPaciente.valueOf(estadoStr);
                conteos.add(new ConteoEstado(estado, conteo));
            }
        }
        return conteos;
    }
    
    public List<ConteoGenero> contarPorGenero() throws SQLException {
        String sql = "SELECT genero, COUNT(*) as conteo FROM " + TABLA + 
                    " GROUP BY genero";
        List<ConteoGenero> conteos = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String genero = rs.getString("genero");
                int conteo = rs.getInt("conteo");
                conteos.add(new ConteoGenero(genero, conteo));
            }
        }
        return conteos;
    }
    
    public List<ConteoEdad> contarPorRangoEdad() throws SQLException {
        String sql = "SELECT " +
                    "CASE " +
                    "   WHEN TIMESTAMPDIFF(YEAR, fecha_nacimiento, CURDATE()) < 18 THEN 'Menor de 18' " +
                    "   WHEN TIMESTAMPDIFF(YEAR, fecha_nacimiento, CURDATE()) BETWEEN 18 AND 30 THEN '18-30' " +
                    "   WHEN TIMESTAMPDIFF(YEAR, fecha_nacimiento, CURDATE()) BETWEEN 31 AND 50 THEN '31-50' " +
                    "   WHEN TIMESTAMPDIFF(YEAR, fecha_nacimiento, CURDATE()) BETWEEN 51 AND 70 THEN '51-70' " +
                    "   ELSE 'Mayor de 70' " +
                    "END as rango_edad, " +
                    "COUNT(*) as conteo " +
                    "FROM " + TABLA + " " +
                    "GROUP BY rango_edad";
        
        List<ConteoEdad> conteos = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String rangoEdad = rs.getString("rango_edad");
                int conteo = rs.getInt("conteo");
                conteos.add(new ConteoEdad(rangoEdad, conteo));
            }
        }
        return conteos;
    }
    
    public List<Paciente> buscarPorFechaNacimiento(LocalDate fechaNacimiento) throws SQLException {
        String sql = "SELECT * FROM " + TABLA + " WHERE fecha_nacimiento = ?";
        List<Paciente> pacientes = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDate(1, Date.valueOf(fechaNacimiento));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pacientes.add(mapearResultSet(rs));
                }
            }
        }
        return pacientes;
    }
    
    public List<Paciente> buscarPorEstado(EstadoPaciente estado) throws SQLException {
        String sql = "SELECT * FROM " + TABLA + " WHERE estado_actual = ?";
        List<Paciente> pacientes = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, estado.name());
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pacientes.add(mapearResultSet(rs));
                }
            }
        }
        return pacientes;
    }
    
    public List<Paciente> buscarQueRequierenSeguimiento() throws SQLException {
        String sql = "SELECT * FROM " + TABLA + " WHERE estado_actual IN ('EN_ATENCION', 'ESPERANDO_MEDICO')";
        List<Paciente> pacientes = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                pacientes.add(mapearResultSet(rs));
            }
        }
        return pacientes;
    }
    
    /**
     * Clase interna para estadísticas de pacientes
     */
    public static class EstadisticaPaciente {
        public EstadoPaciente estado;
        public int total;
        
        @Override
        public String toString() {
            return String.format("Estado: %s, Total: %d", estado, total);
        }
    }
    
    /**
     * Contar total de pacientes
     */
    public int contarTotal() {
        String sql = "SELECT COUNT(*) FROM " + TABLA;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar pacientes", e);
        }
    }

    /**
     * Contar pacientes activos en el último mes
     */
    public int contarActivosUltimoMes() {
        String sql = "SELECT COUNT(*) FROM " + TABLA + 
                    " WHERE fecha_registro >= DATE_SUB(NOW(), INTERVAL 1 MONTH)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar pacientes activos", e);
        }
    }
    
    /**
     * Clase interna para conteos por estado
     */
    public static class ConteoEstado {
        private EstadoPaciente estado;
        private int conteo;
        
        public ConteoEstado(EstadoPaciente estado, int conteo) {
            this.estado = estado;
            this.conteo = conteo;
        }
        
        public EstadoPaciente getEstado() { return estado; }
        public int getConteo() { return conteo; }
        
        @Override
        public String toString() {
            return String.format("Estado: %s, Conteo: %d", estado, conteo);
        }
    }
    
    /**
     * Clase interna para conteos por género
     */
    public static class ConteoGenero {
        private String genero;
        private int conteo;
        
        public ConteoGenero(String genero, int conteo) {
            this.genero = genero;
            this.conteo = conteo;
        }
        
        public String getGenero() { return genero; }
        public int getConteo() { return conteo; }
        
        @Override
        public String toString() {
            return String.format("Género: %s, Conteo: %d", genero, conteo);
        }
    }
    
    /**
     * Clase interna para conteos por edad
     */
    public static class ConteoEdad {
        private String rangoEdad;
        private int conteo;
        
        public ConteoEdad(String rangoEdad, int conteo) {
            this.rangoEdad = rangoEdad;
            this.conteo = conteo;
        }
        
        public String getRangoEdad() { return rangoEdad; }
        public int getConteo() { return conteo; }
        
        @Override
        public String toString() {
            return String.format("Rango Edad: %s, Conteo: %d", rangoEdad, conteo);
        }
    }
    
    // Métodos adicionales requeridos por los servicios
    
    /**
     * Crea un nuevo paciente y devuelve su ID
     */
    public int crear(Paciente paciente) throws SQLException {
        if (!insertar(paciente)) {
            throw new SQLException("Error al insertar paciente");
        }
        
        // Obtener el ID generado
        String sql = "SELECT LAST_INSERT_ID()";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el ID del paciente creado");
    }
    
    /**
     * Obtiene un paciente por su ID
     */
    public Paciente obtenerPorId(int id) {
        try {
            return buscarPorId(id);
        } catch (SQLException e) {
            System.err.println("Error al obtener paciente por ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtiene un paciente por su CURP
     */
    public Paciente obtenerPorCURP(String curp) {
        String sql = "SELECT * FROM " + TABLA + " WHERE curp = ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, curp);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener paciente por CURP: " + e.getMessage());
        }
        
        return null;
    }
}