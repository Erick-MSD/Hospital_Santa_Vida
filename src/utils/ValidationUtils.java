package utils;

import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Clase utilitaria para validación de datos de entrada
 * Proporciona métodos de validación para diferentes tipos de campos
 * Incluye validación de CURP, RFC, teléfonos, emails y otros datos médicos
 */
public class ValidationUtils {
    
    // Patrones de expresiones regulares para validación
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"
    );
    
    private static final Pattern TELEFONO_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern RFC_PATTERN = Pattern.compile("^[A-ZÑ&]{3,4}[0-9]{2}(0[1-9]|1[0-2])(0[1-9]|[12][0-9]|3[01])[A-Z0-9]{2}[0-9A]$");
    private static final Pattern CURP_PATTERN = Pattern.compile("^[A-Z]{1}[AEIOUX]{1}[A-Z]{2}[0-9]{2}(0[1-9]|1[0-2])(0[1-9]|[12][0-9]|3[01])[HM]{1}(AS|BC|BS|CC|CH|CL|CM|CS|DF|DG|GR|GT|HG|JC|MC|MN|MS|NT|NL|OC|PL|QT|QR|SP|SL|SR|TC|TL|TS|VZ|YN|ZS|NE)[B-DF-HJ-NP-TV-Z]{3}[0-9A-Z]{1}[0-9]{1}$");
    private static final Pattern CODIGO_POSTAL_PATTERN = Pattern.compile("^[0-9]{5}$");
    private static final Pattern SOLO_LETRAS_PATTERN = Pattern.compile("^[a-zA-ZáéíóúñÁÉÍÓÚÑ\\s]+$");
    private static final Pattern ALFANUMERICO_PATTERN = Pattern.compile("^[a-zA-Z0-9áéíóúñÁÉÍÓÚÑ\\s]+$");
    
    // Formateadores de fecha
    private static final DateTimeFormatter FECHA_FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FECHA_HORA_FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Clase para encapsular resultados de validación
     */
    public static class ValidationResult {
        private boolean valido;
        private List<String> errores;
        private String valorNormalizado;
        
        public ValidationResult() {
            this.valido = true;
            this.errores = new ArrayList<>();
        }
        
        public ValidationResult(boolean valido, String error) {
            this();
            this.valido = valido;
            if (!valido && error != null) {
                this.errores.add(error);
            }
        }
        
        // Getters y Setters
        public boolean isValido() { return valido; }
        public void setValido(boolean valido) { this.valido = valido; }
        
        public List<String> getErrores() { return errores; }
        public void addError(String error) { 
            this.errores.add(error);
            this.valido = false;
        }
        
        public String getValorNormalizado() { return valorNormalizado; }
        public void setValorNormalizado(String valorNormalizado) { this.valorNormalizado = valorNormalizado; }
        
        public String getMensajeErrores() {
            return String.join(", ", errores);
        }
        
        @Override
        public String toString() {
            return "ValidationResult{valido=" + valido + ", errores=" + errores.size() + "}";
        }
    }
    
    /**
     * Valida una dirección de email
     */
    public static ValidationResult validarEmail(String email) {
        ValidationResult result = new ValidationResult();
        
        if (esVacioONulo(email)) {
            result.addError("El email es requerido");
            return result;
        }
        
        String emailLimpio = email.trim().toLowerCase();
        
        if (emailLimpio.length() > 100) {
            result.addError("El email no puede exceder 100 caracteres");
        }
        
        if (!EMAIL_PATTERN.matcher(emailLimpio).matches()) {
            result.addError("Formato de email inválido");
        }
        
        result.setValorNormalizado(emailLimpio);
        return result;
    }
    
    /**
     * Valida un número de teléfono mexicano
     */
    public static ValidationResult validarTelefono(String telefono) {
        ValidationResult result = new ValidationResult();
        
        if (esVacioONulo(telefono)) {
            result.addError("El teléfono es requerido");
            return result;
        }
        
        // Limpiar el teléfono removiendo espacios, guiones y paréntesis
        String telefonoLimpio = telefono.replaceAll("[\\s\\-\\(\\)]", "");
        
        // Remover código de país si está presente
        if (telefonoLimpio.startsWith("+52")) {
            telefonoLimpio = telefonoLimpio.substring(3);
        } else if (telefonoLimpio.startsWith("52") && telefonoLimpio.length() == 12) {
            telefonoLimpio = telefonoLimpio.substring(2);
        }
        
        if (!TELEFONO_PATTERN.matcher(telefonoLimpio).matches()) {
            result.addError("El teléfono debe tener 10 dígitos");
        }
        
        result.setValorNormalizado(telefonoLimpio);
        return result;
    }
    
    /**
     * Valida una CURP (Clave Única de Registro de Población)
     */
    public static ValidationResult validarCURP(String curp) {
        ValidationResult result = new ValidationResult();
        
        if (esVacioONulo(curp)) {
            result.addError("La CURP es requerida");
            return result;
        }
        
        String curpLimpia = curp.trim().toUpperCase();
        
        if (curpLimpia.length() != 18) {
            result.addError("La CURP debe tener exactamente 18 caracteres");
            return result;
        }
        
        if (!CURP_PATTERN.matcher(curpLimpia).matches()) {
            result.addError("Formato de CURP inválido");
            return result;
        }
        
        // Validación adicional del dígito verificador
        if (!validarDigitoVerificadorCURP(curpLimpia)) {
            result.addError("Dígito verificador de CURP inválido");
        }
        
        result.setValorNormalizado(curpLimpia);
        return result;
    }
    
    /**
     * Valida un RFC (Registro Federal de Contribuyentes)
     */
    public static ValidationResult validarRFC(String rfc) {
        ValidationResult result = new ValidationResult();
        
        if (esVacioONulo(rfc)) {
            // RFC es opcional en algunos casos
            return result;
        }
        
        String rfcLimpio = rfc.trim().toUpperCase();
        
        if (rfcLimpio.length() != 12 && rfcLimpio.length() != 13) {
            result.addError("El RFC debe tener 12 o 13 caracteres");
            return result;
        }
        
        if (!RFC_PATTERN.matcher(rfcLimpio).matches()) {
            result.addError("Formato de RFC inválido");
        }
        
        result.setValorNormalizado(rfcLimpio);
        return result;
    }
    
    /**
     * Valida un código postal mexicano
     */
    public static ValidationResult validarCodigoPostal(String cp) {
        ValidationResult result = new ValidationResult();
        
        if (esVacioONulo(cp)) {
            result.addError("El código postal es requerido");
            return result;
        }
        
        String cpLimpio = cp.trim();
        
        if (!CODIGO_POSTAL_PATTERN.matcher(cpLimpio).matches()) {
            result.addError("El código postal debe tener 5 dígitos");
        }
        
        result.setValorNormalizado(cpLimpio);
        return result;
    }
    
    /**
     * Valida un nombre (solo letras y espacios)
     */
    public static ValidationResult validarNombre(String nombre, String campo) {
        ValidationResult result = new ValidationResult();
        
        if (esVacioONulo(nombre)) {
            result.addError("El " + campo + " es requerido");
            return result;
        }
        
        String nombreLimpio = nombre.trim();
        
        if (nombreLimpio.length() < 2) {
            result.addError("El " + campo + " debe tener al menos 2 caracteres");
        }
        
        if (nombreLimpio.length() > 100) {
            result.addError("El " + campo + " no puede exceder 100 caracteres");
        }
        
        if (!SOLO_LETRAS_PATTERN.matcher(nombreLimpio).matches()) {
            result.addError("El " + campo + " solo puede contener letras y espacios");
        }
        
        // Normalizar: primera letra mayúscula, resto minúscula
        result.setValorNormalizado(capitalizarNombre(nombreLimpio));
        return result;
    }
    
    /**
     * Valida una fecha de nacimiento
     */
    public static ValidationResult validarFechaNacimiento(String fechaStr) {
        ValidationResult result = new ValidationResult();
        
        if (esVacioONulo(fechaStr)) {
            result.addError("La fecha de nacimiento es requerida");
            return result;
        }
        
        try {
            LocalDate fecha = LocalDate.parse(fechaStr, FECHA_FORMATO);
            
            // Validar que no sea fecha futura
            if (fecha.isAfter(LocalDate.now())) {
                result.addError("La fecha de nacimiento no puede ser futura");
            }
            
            // Validar edad mínima y máxima razonable
            int edad = LocalDate.now().getYear() - fecha.getYear();
            if (edad < 0) {
                result.addError("Fecha de nacimiento inválida");
            } else if (edad > 150) {
                result.addError("La edad no puede exceder 150 años");
            }
            
            result.setValorNormalizado(fecha.format(FECHA_FORMATO));
            
        } catch (DateTimeParseException e) {
            result.addError("Formato de fecha inválido (use YYYY-MM-DD)");
        }
        
        return result;
    }
    
    /**
     * Valida signos vitales - Presión arterial
     */
    public static ValidationResult validarPresionArterial(Integer sistolica, Integer diastolica) {
        ValidationResult result = new ValidationResult();
        
        if (sistolica == null || diastolica == null) {
            result.addError("Ambos valores de presión arterial son requeridos");
            return result;
        }
        
        if (sistolica < 50 || sistolica > 300) {
            result.addError("Presión sistólica debe estar entre 50 y 300 mmHg");
        }
        
        if (diastolica < 30 || diastolica > 200) {
            result.addError("Presión diastólica debe estar entre 30 y 200 mmHg");
        }
        
        if (sistolica <= diastolica) {
            result.addError("La presión sistólica debe ser mayor que la diastólica");
        }
        
        result.setValorNormalizado(sistolica + "/" + diastolica);
        return result;
    }
    
    /**
     * Valida frecuencia cardíaca
     */
    public static ValidationResult validarFrecuenciaCardiaca(Integer frecuencia) {
        ValidationResult result = new ValidationResult();
        
        if (frecuencia == null) {
            result.addError("La frecuencia cardíaca es requerida");
            return result;
        }
        
        if (frecuencia < 20 || frecuencia > 300) {
            result.addError("La frecuencia cardíaca debe estar entre 20 y 300 bpm");
        }
        
        result.setValorNormalizado(frecuencia.toString());
        return result;
    }
    
    /**
     * Valida temperatura corporal
     */
    public static ValidationResult validarTemperatura(Double temperatura) {
        ValidationResult result = new ValidationResult();
        
        if (temperatura == null) {
            result.addError("La temperatura es requerida");
            return result;
        }
        
        if (temperatura < 30.0 || temperatura > 45.0) {
            result.addError("La temperatura debe estar entre 30.0°C y 45.0°C");
        }
        
        result.setValorNormalizado(String.format("%.1f", temperatura));
        return result;
    }
    
    /**
     * Valida saturación de oxígeno
     */
    public static ValidationResult validarSaturacionOxigeno(Integer saturacion) {
        ValidationResult result = new ValidationResult();
        
        if (saturacion == null) {
            result.addError("La saturación de oxígeno es requerida");
            return result;
        }
        
        if (saturacion < 50 || saturacion > 100) {
            result.addError("La saturación de oxígeno debe estar entre 50% y 100%");
        }
        
        result.setValorNormalizado(saturacion + "%");
        return result;
    }
    
    /**
     * Valida escala de Glasgow
     */
    public static ValidationResult validarGlasgow(Integer glasgow) {
        ValidationResult result = new ValidationResult();
        
        if (glasgow == null) {
            result.addError("La escala de Glasgow es requerida");
            return result;
        }
        
        if (glasgow < 3 || glasgow > 15) {
            result.addError("La escala de Glasgow debe estar entre 3 y 15 puntos");
        }
        
        result.setValorNormalizado(glasgow.toString());
        return result;
    }
    
    /**
     * Valida un campo de texto general
     */
    public static ValidationResult validarTexto(String texto, String nombreCampo, int minLength, int maxLength, boolean requerido) {
        ValidationResult result = new ValidationResult();
        
        if (esVacioONulo(texto)) {
            if (requerido) {
                result.addError("El campo " + nombreCampo + " es requerido");
            }
            return result;
        }
        
        String textoLimpio = texto.trim();
        
        if (textoLimpio.length() < minLength) {
            result.addError("El campo " + nombreCampo + " debe tener al menos " + minLength + " caracteres");
        }
        
        if (textoLimpio.length() > maxLength) {
            result.addError("El campo " + nombreCampo + " no puede exceder " + maxLength + " caracteres");
        }
        
        result.setValorNormalizado(textoLimpio);
        return result;
    }
    
    /**
     * Valida múltiples campos a la vez
     */
    public static ValidationResult validarMultiplesCampos(ValidationResult... validaciones) {
        ValidationResult resultado = new ValidationResult();
        
        for (ValidationResult validacion : validaciones) {
            if (!validacion.isValido()) {
                resultado.setValido(false);
                resultado.getErrores().addAll(validacion.getErrores());
            }
        }
        
        return resultado;
    }
    
    // Métodos auxiliares privados
    
    private static boolean esVacioONulo(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
    
    private static String capitalizarNombre(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            return nombre;
        }
        
        String[] palabras = nombre.toLowerCase().split("\\s+");
        StringBuilder resultado = new StringBuilder();
        
        for (int i = 0; i < palabras.length; i++) {
            if (i > 0) {
                resultado.append(" ");
            }
            
            String palabra = palabras[i];
            if (!palabra.isEmpty()) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)));
                if (palabra.length() > 1) {
                    resultado.append(palabra.substring(1));
                }
            }
        }
        
        return resultado.toString();
    }
    
    private static boolean validarDigitoVerificadorCURP(String curp) {
        // Implementación simplificada del algoritmo de verificación de CURP
        String alfabeto = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int suma = 0;
        
        for (int i = 0; i < 17; i++) {
            char c = curp.charAt(i);
            int valor = alfabeto.indexOf(c);
            suma += valor * (18 - i);
        }
        
        int residuo = suma % 10;
        int digitoEsperado = (10 - residuo) % 10;
        
        char ultimoCaracter = curp.charAt(17);
        int digitoReal = Character.isDigit(ultimoCaracter) ? 
                        Character.getNumericValue(ultimoCaracter) : 0;
        
        return digitoReal == digitoEsperado;
    }
    
    /**
     * Normaliza un string removiendo acentos y caracteres especiales
     */
    public static String normalizarTexto(String texto) {
        if (texto == null) {
            return null;
        }
        
        return java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .trim();
    }
    
    /**
     * Valida formato de folio hospitalario
     */
    public static ValidationResult validarFolioHospital(String folio) {
        ValidationResult result = new ValidationResult();
        
        if (esVacioONulo(folio)) {
            result.addError("El folio es requerido");
            return result;
        }
        
        String folioLimpio = folio.trim().toUpperCase();
        
        // Formato esperado: TRG-YYYY-NNNN
        if (!folioLimpio.matches("^TRG-\\d{4}-\\d{4}$")) {
            result.addError("Formato de folio inválido (debe ser TRG-YYYY-NNNN)");
        }
        
        result.setValorNormalizado(folioLimpio);
        return result;
    }
    
    /**
     * Valida que una fecha/hora esté en el pasado o presente
     */
    public static ValidationResult validarFechaHoraPasado(String fechaHoraStr) {
        ValidationResult result = new ValidationResult();
        
        if (esVacioONulo(fechaHoraStr)) {
            return result; // Opcional
        }
        
        try {
            LocalDateTime fechaHora = LocalDateTime.parse(fechaHoraStr, FECHA_HORA_FORMATO);
            
            if (fechaHora.isAfter(LocalDateTime.now())) {
                result.addError("La fecha y hora no puede ser futura");
            }
            
            result.setValorNormalizado(fechaHora.format(FECHA_HORA_FORMATO));
            
        } catch (DateTimeParseException e) {
            result.addError("Formato de fecha y hora inválido (use YYYY-MM-DD HH:MM:SS)");
        }
        
        return result;
    }
    
    // Métodos de conveniencia que retornan boolean para compatibilidad
    
    /**
     * Versión simplificada de validarTexto que retorna boolean
     */
    public static boolean validarTexto(String texto, int minLength, int maxLength) {
        if (esVacioONulo(texto)) {
            return false;
        }
        String textoLimpio = texto.trim();
        return textoLimpio.length() >= minLength && textoLimpio.length() <= maxLength;
    }
    
    /**
     * Versiones de conveniencia para presión arterial
     */
    public static boolean validarPresionArterial(String presion) {
        if (esVacioONulo(presion)) return false;
        // Formato esperado: "120/80"
        return presion.matches("^\\d{2,3}/\\d{2,3}$");
    }
    
    /**
     * Versiones que retornan boolean para compatibilidad
     */
    public static boolean validarTelefonoBoolean(String telefono) {
        return validarTelefono(telefono).isValido();
    }
    
    public static boolean validarCURPBoolean(String curp) {
        return validarCURP(curp).isValido();
    }
    
    public static boolean validarRFCBoolean(String rfc) {
        return validarRFC(rfc).isValido();
    }
    
    public static boolean validarEmailBoolean(String email) {
        return validarEmail(email).isValido();
    }
    
    /**
     * Método para validar escala Glasgow
     */
    public static boolean validarEscalaGlasgow(int escala) {
        return escala >= 3 && escala <= 15;
    }
    
    /**
     * Versiones boolean adicionales para signos vitales
     */
    public static boolean validarFrecuenciaCardiacaBoolean(int frecuencia) {
        return validarFrecuenciaCardiaca(frecuencia).isValido();
    }
    
    public static boolean validarTemperaturaBoolean(double temperatura) {
        return validarTemperatura(temperatura).isValido();
    }
    
    public static boolean validarSaturacionOxigenoBoolean(int saturacion) {
        return validarSaturacionOxigeno(saturacion).isValido();
    }
    
    /**
     * Valida que una contraseña cumpla con los requisitos de seguridad (versión boolean para DAOs)
     * - Mínimo 8 caracteres
     * - Al menos una letra mayúscula
     * - Al menos una letra minúscula
     * - Al menos un número
     * - Al menos un carácter especial
     */
    public static boolean validarPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        
        String pass = password.trim();
        
        if (pass.length() < 8) {
            return false;
        }
        
        if (!pass.matches(".*[A-Z].*")) {
            return false;
        }
        
        if (!pass.matches(".*[a-z].*")) {
            return false;
        }
        
        if (!pass.matches(".*[0-9].*")) {
            return false;
        }
        
        if (!pass.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Versiones booleanas simplificadas para controladores
     */
    
    /**
     * Valida email (versión boolean)
     */
    public static boolean isValidEmail(String email) {
        return validarEmail(email).isValido();
    }
    
    /**
     * Valida teléfono (versión boolean)
     */
    public static boolean isValidTelefono(String telefono) {
        return validarTelefono(telefono).isValido();
    }
    
    /**
     * Valida CURP (versión boolean)
     */
    public static boolean isValidCURP(String curp) {
        return validarCURP(curp).isValido();
    }
    
    /**
     * Valida RFC (versión boolean)
     */
    public static boolean isValidRFC(String rfc) {
        return validarRFC(rfc).isValido();
    }
    
    /**
     * Valida que el texto sea solo letras (versión boolean)
     */
    public static boolean isValidSoloLetras(String texto) {
        return validarNombre(texto, "texto").isValido();
    }
    
    /**
     * Valida código postal (versión boolean)
     */
    public static boolean isValidCodigoPostal(String codigo) {
        return validarCodigoPostal(codigo).isValido();
    }
    
    /**
     * Valida que no esté vacío (versión boolean)
     */
    public static boolean isNotEmpty(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }
    
    /**
     * Valida rango numérico (versión boolean)
     */
    public static boolean isInRange(double valor, double min, double max) {
        return valor >= min && valor <= max;
    }
    
    /**
     * Valida rango entero (versión boolean)
     */
    public static boolean isInRange(int valor, int min, int max) {
        return valor >= min && valor <= max;
    }
}