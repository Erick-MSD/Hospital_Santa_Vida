package controllers;

import dao.PacienteDAO;
import models.Paciente;
import models.RegistroTriage;
import models.Usuario;
import services.AuthenticationService;
import services.TriageService;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Controlador para el registro de pacientes (versión consola)
 * Solo accesible para Asistentes Médicas
 */
public class RegistroPacienteController2 extends BaseController {
    
    // Servicios
    private PacienteDAO pacienteDAO;
    private Scanner scanner;
    
    public RegistroPacienteController2() {
        this.pacienteDAO = new PacienteDAO();
        this.scanner = new Scanner(System.in);
    }
    
    @Override
    protected void configurarInterfaz() {
        // Verificar permisos
        try {
            authService.requireRole(Usuario.TipoUsuario.ASISTENTE_MEDICA);
            System.out.println("\n🏥 ===== REGISTRO DE PACIENTE =====");
            System.out.println("👩‍⚕️ Asistente Médica: " + usuarioActual.getNombreCompleto());
            System.out.println("=====================================\n");
        } catch (Exception e) {
            showError("No tiene permisos para acceder a esta funcionalidad");
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void cargarDatos() {
        // No hay datos específicos que cargar al iniciar
        mostrarMenu();
    }
    
    @Override
    protected void limpiarFormulario() {
        // No hay formulario que limpiar en la versión console
    }
    
    private void mostrarMenu() {
        boolean continuar = true;
        
        while (continuar) {
            System.out.println("\n📋 OPCIONES DE REGISTRO:");
            System.out.println("1. Registrar nuevo paciente");
            System.out.println("2. Buscar paciente existente");
            System.out.println("3. Ver últimos pacientes registrados");
            System.out.println("0. Salir");
            System.out.print("\nSeleccione una opción: ");
            
            try {
                int opcion = Integer.parseInt(scanner.nextLine());
                
                switch (opcion) {
                    case 1:
                        registrarNuevoPaciente();
                        break;
                    case 2:
                        buscarPacienteExistente();
                        break;
                    case 3:
                        verUltimosPacientes();
                        break;
                    case 0:
                        continuar = false;
                        break;
                    default:
                        System.out.println("❌ Opción no válida");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Por favor ingrese un número válido");
            }
        }
    }
    
    private void registrarNuevoPaciente() {
        System.out.println("\n👤 ==== REGISTRO DE NUEVO PACIENTE ====");
        
        try {
            Paciente paciente = capturarDatosPaciente();
            
            if (paciente != null) {
                // Guardar en base de datos
                int pacienteId = pacienteDAO.crear(paciente);
                paciente.setId(pacienteId);
                
                // Registrar en triage
                RegistroTriage registro = triageService.registrarLlegadaPaciente(
                    pacienteId, 
                    "Consulta de urgencias", 
                    "Paciente registrado por Asistente Médica"
                );
                
                if (registro != null) {
                    System.out.println("\n✅ PACIENTE REGISTRADO EXITOSAMENTE");
                    System.out.println("📋 Folio de Triage: " + registro.getFolio());
                    System.out.println("👤 Paciente: " + paciente.getNombreCompleto());
                    System.out.println("📅 Fecha de registro: " + registro.getFechaHoraLlegada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    System.out.println("\n🏥 NIVEL ASIGNADO: PENDIENTE DE EVALUACIÓN");
                    System.out.println("📝 El paciente será evaluado por el médico de triage\n");
                    
                    logAction("Paciente registrado: " + paciente.getNombreCompleto() + " - Folio: " + registro.getFolio());
                } else {
                    showError("Error al registrar el paciente en triage");
                }
            }
            
        } catch (Exception e) {
            showError("Error al registrar el paciente: " + e.getMessage());
        }
    }
    
    private Paciente capturarDatosPaciente() {
        Paciente paciente = new Paciente();
        
        try {
            // Datos básicos
            System.out.println("\n📋 DATOS DEL PACIENTE:");
            
            System.out.print("Nombre(s)*: ");
            String nombre = scanner.nextLine().trim();
            if (nombre.isEmpty()) {
                showError("El nombre es obligatorio");
                return null;
            }
            paciente.setNombre(nombre);
            
            System.out.print("Apellido Paterno*: ");
            String apellidoPaterno = scanner.nextLine().trim();
            if (apellidoPaterno.isEmpty()) {
                showError("El apellido paterno es obligatorio");
                return null;
            }
            paciente.setApellidoPaterno(apellidoPaterno);
            
            System.out.print("Apellido Materno: ");
            String apellidoMaterno = scanner.nextLine().trim();
            paciente.setApellidoMaterno(apellidoMaterno);
            
            System.out.print("CURP (18 caracteres)*: ");
            String curp = scanner.nextLine().trim().toUpperCase();
            if (curp.length() != 18) {
                showError("La CURP debe tener exactamente 18 caracteres");
                return null;
            }
            paciente.setCurp(curp);
            
            // Fecha de nacimiento
            System.out.print("Fecha de nacimiento (dd/mm/yyyy)*: ");
            String fechaNacStr = scanner.nextLine().trim();
            try {
                LocalDate fechaNacimiento = LocalDate.parse(fechaNacStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                if (fechaNacimiento.isAfter(LocalDate.now())) {
                    showError("La fecha de nacimiento no puede ser futura");
                    return null;
                }
                paciente.setFechaNacimiento(fechaNacimiento);
            } catch (Exception e) {
                showError("Formato de fecha incorrecto. Use dd/mm/yyyy");
                return null;
            }
            
            // Sexo
            System.out.print("Sexo (MASCULINO/FEMENINO/OTRO)*: ");
            String sexoStr = scanner.nextLine().trim().toUpperCase();
            try {
                Paciente.Sexo sexo = Paciente.Sexo.valueOf(sexoStr);
                paciente.setSexo(sexo);
            } catch (IllegalArgumentException e) {
                showError("Sexo no válido. Use MASCULINO, FEMENINO u OTRO");
                return null;
            }
            
            // Información de contacto
            System.out.println("\n📞 INFORMACIÓN DE CONTACTO:");
            
            System.out.print("Teléfono Principal*: ");
            String telefonoPrincipal = scanner.nextLine().trim();
            if (telefonoPrincipal.isEmpty()) {
                showError("El teléfono principal es obligatorio");
                return null;
            }
            paciente.setTelefonoPrincipal(telefonoPrincipal);
            
            System.out.print("Teléfono Secundario: ");
            String telefonoSecundario = scanner.nextLine().trim();
            paciente.setTelefonoSecundario(telefonoSecundario);
            
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            paciente.setEmail(email);
            
            // Dirección
            System.out.println("\n🏠 DIRECCIÓN:");
            
            System.out.print("Calle*: ");
            String calle = scanner.nextLine().trim();
            if (calle.isEmpty()) {
                showError("La calle es obligatoria");
                return null;
            }
            paciente.setDireccionCalle(calle);
            
            System.out.print("Número*: ");
            String numero = scanner.nextLine().trim();
            if (numero.isEmpty()) {
                showError("El número es obligatorio");
                return null;
            }
            paciente.setDireccionNumero(numero);
            
            System.out.print("Colonia*: ");
            String colonia = scanner.nextLine().trim();
            if (colonia.isEmpty()) {
                showError("La colonia es obligatoria");
                return null;
            }
            paciente.setDireccionColonia(colonia);
            
            System.out.print("Ciudad*: ");
            String ciudad = scanner.nextLine().trim();
            if (ciudad.isEmpty()) {
                showError("La ciudad es obligatoria");
                return null;
            }
            paciente.setDireccionCiudad(ciudad);
            
            System.out.print("Estado*: ");
            String estado = scanner.nextLine().trim();
            if (estado.isEmpty()) {
                showError("El estado es obligatorio");
                return null;
            }
            paciente.setDireccionEstado(estado);
            
            System.out.print("Código Postal (5 dígitos)*: ");
            String cp = scanner.nextLine().trim();
            if (cp.length() != 5 || !cp.matches("\\d{5}")) {
                showError("El código postal debe tener exactamente 5 dígitos");
                return null;
            }
            paciente.setDireccionCp(cp);
            
            // Contacto de emergencia
            System.out.println("\n🚨 CONTACTO DE EMERGENCIA:");
            
            System.out.print("Nombre completo*: ");
            String nombreContacto = scanner.nextLine().trim();
            if (nombreContacto.isEmpty()) {
                showError("El nombre del contacto de emergencia es obligatorio");
                return null;
            }
            paciente.setContactoEmergenciaNombre(nombreContacto);
            
            System.out.print("Teléfono*: ");
            String telefonoContacto = scanner.nextLine().trim();
            if (telefonoContacto.isEmpty()) {
                showError("El teléfono del contacto de emergencia es obligatorio");
                return null;
            }
            paciente.setContactoEmergenciaTelefono(telefonoContacto);
            
            System.out.print("Relación (Padre/Madre/Esposo/etc.)*: ");
            String relacionContacto = scanner.nextLine().trim();
            if (relacionContacto.isEmpty()) {
                showError("La relación del contacto de emergencia es obligatoria");
                return null;
            }
            paciente.setContactoEmergenciaRelacion(relacionContacto);
            
            // Seguro médico (opcional)
            System.out.println("\n🏥 SEGURO MÉDICO (Opcional):");
            System.out.print("Aseguradora: ");
            String seguroMedico = scanner.nextLine().trim();
            if (!seguroMedico.isEmpty() && !seguroMedico.equalsIgnoreCase("Sin Seguro")) {
                paciente.setSeguroMedico(seguroMedico);
                
                System.out.print("Número de póliza: ");
                String numeroPoliza = scanner.nextLine().trim();
                paciente.setNumeroPoliza(numeroPoliza);
            }
            
            // Mostrar resumen
            System.out.println("\n📋 RESUMEN DEL PACIENTE:");
            System.out.println("Nombre: " + paciente.getNombreCompleto());
            System.out.println("Edad: " + paciente.getEdad() + " años");
            System.out.println("CURP: " + paciente.getCurp());
            System.out.println("Teléfono: " + paciente.getTelefonoPrincipal());
            System.out.println("Dirección: " + paciente.getDireccionCompleta());
            System.out.println("Contacto emergencia: " + paciente.getContactoEmergenciaNombre() + " (" + paciente.getContactoEmergenciaRelacion() + ") - " + paciente.getContactoEmergenciaTelefono());
            
            System.out.print("\n¿Los datos son correctos? (s/n): ");
            String confirmacion = scanner.nextLine().trim().toLowerCase();
            
            if (confirmacion.equals("s") || confirmacion.equals("si") || confirmacion.equals("y") || confirmacion.equals("yes")) {
                return paciente;
            } else {
                System.out.println("❌ Registro cancelado");
                return null;
            }
            
        } catch (Exception e) {
            showError("Error al capturar los datos del paciente: " + e.getMessage());
            return null;
        }
    }
    
    private void buscarPacienteExistente() {
        System.out.println("\n🔍 ==== BUSCAR PACIENTE ====");
        System.out.println("1. Buscar por CURP");
        System.out.println("2. Buscar por nombre");
        System.out.print("Seleccione opción: ");
        
        try {
            int opcion = Integer.parseInt(scanner.nextLine());
            
            switch (opcion) {
                case 1:
                    buscarPorCurp();
                    break;
                case 2:
                    buscarPorNombre();
                    break;
                default:
                    System.out.println("❌ Opción no válida");
                    break;
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Por favor ingrese un número válido");
        }
    }
    
    private void buscarPorCurp() {
        System.out.print("Ingrese CURP: ");
        String curp = scanner.nextLine().trim().toUpperCase();
        
        try {
            Paciente paciente = pacienteDAO.buscarPorCurp(curp);
            if (paciente != null) {
                mostrarDatosPaciente(paciente);
            } else {
                System.out.println("❌ No se encontró ningún paciente con esa CURP");
            }
        } catch (Exception e) {
            showError("Error al buscar paciente: " + e.getMessage());
        }
    }
    
    private void buscarPorNombre() {
        System.out.print("Ingrese nombre (puede ser parcial): ");
        String nombre = scanner.nextLine().trim();
        
        try {
            var pacientes = pacienteDAO.buscarPorNombre(nombre);
            if (!pacientes.isEmpty()) {
                System.out.println("\n📋 PACIENTES ENCONTRADOS:");
                for (int i = 0; i < pacientes.size(); i++) {
                    Paciente p = pacientes.get(i);
                    System.out.println((i + 1) + ". " + p.getNombreCompleto() + " - " + p.getCurp() + " - " + p.getEdad() + " años");
                }
                
                System.out.print("\nSeleccione un paciente (número): ");
                int seleccion = Integer.parseInt(scanner.nextLine()) - 1;
                
                if (seleccion >= 0 && seleccion < pacientes.size()) {
                    mostrarDatosPaciente(pacientes.get(seleccion));
                } else {
                    System.out.println("❌ Selección no válida");
                }
            } else {
                System.out.println("❌ No se encontraron pacientes con ese nombre");
            }
        } catch (Exception e) {
            showError("Error al buscar pacientes: " + e.getMessage());
        }
    }
    
    private void mostrarDatosPaciente(Paciente paciente) {
        System.out.println("\n👤 ==== DATOS DEL PACIENTE ====");
        System.out.println("ID: " + paciente.getId());
        System.out.println("Nombre: " + paciente.getNombreCompleto());
        System.out.println("CURP: " + paciente.getCurp());
        System.out.println("Fecha de nacimiento: " + paciente.getFechaNacimiento());
        System.out.println("Edad: " + paciente.getEdad() + " años");
        System.out.println("Sexo: " + paciente.getSexo());
        System.out.println("Teléfono principal: " + paciente.getTelefonoPrincipal());
        System.out.println("Email: " + (paciente.getEmail() != null ? paciente.getEmail() : "No especificado"));
        System.out.println("Dirección: " + paciente.getDireccionCompleta());
        System.out.println("Contacto emergencia: " + paciente.getContactoEmergenciaNombre() + " (" + paciente.getContactoEmergenciaRelacion() + ") - " + paciente.getContactoEmergenciaTelefono());
        System.out.println("Seguro médico: " + (paciente.tieneSeguroMedico() ? paciente.getSeguroMedico() : "Sin seguro"));
        System.out.println("Fecha de registro: " + paciente.getFechaRegistro());
        System.out.println("===============================");
    }
    
    private void verUltimosPacientes() {
        System.out.println("\n📋 ==== ÚLTIMOS PACIENTES REGISTRADOS ====");
        // Esta funcionalidad requeriría un método adicional en PacienteDAO
        System.out.println("⏳ Funcionalidad en desarrollo...");
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        if (scanner != null) {
            scanner.close();
        }
        logAction("Cerrando registro de paciente");
    }
}