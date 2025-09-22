package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import models.EvaluacionTriage;
import dao.EvaluacionTriageDAO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TriageController {
    
    // Campos del formulario
    @FXML private TextField txtNumeroFolio;
    @FXML private TextField txtFechaIngreso;
    @FXML private TextArea txtMotivoConsulta;
    @FXML private TextField txtPresionArterial;
    @FXML private TextField txtFrecuenciaCardiaca;
    @FXML private TextField txtTemperatura;
    @FXML private TextField txtFrecuenciaRespiratoria;
    @FXML private TextField txtSaturacionO2;
    @FXML private TextField txtDolor;
    @FXML private TextArea txtObservacionesClinicas;
    
    // Labels del doctor
    @FXML private Label lblDoctorName;
    @FXML private Label lblDoctorRole;
    
    // Botones de triage
    @FXML private Button btnTriageRojo;
    @FXML private Button btnTriageNaranja;
    @FXML private Button btnTriageAmarillo;
    @FXML private Button btnTriageVerde;
    @FXML private Button btnTriageAzul;
    @FXML private Label lblTriageSeleccionado;
    
    // Botones de especialidades
    @FXML private Button btnEspecialidadGeneral;
    @FXML private Button btnEspecialidadCardiologia;
    @FXML private Button btnEspecialidadPediatria;
    @FXML private Button btnEspecialidadTraumatologia;
    @FXML private Button btnEspecialidadGinecologia;
    @FXML private Button btnEspecialidadNeurologia;
    @FXML private Button btnEspecialidadPsiquiatria;
    @FXML private Button btnEspecialidadDermatologia;
    @FXML private Label lblEspecialidadSeleccionada;
    
    // Botones de acción
    @FXML private Button btnCancelar;
    @FXML private Button btnCompletarEvaluacion;
    
    // Variables de estado
    private String nivelTriageSeleccionado = null;
    private String especialidadSeleccionada = null;
    private EvaluacionTriageDAO evaluacionDAO;
    
    // Método para inicializar
    @FXML
    private void initialize() {
        System.out.println("✅ Controlador de Triage inicializado correctamente");
        
        // Inicializar DAO
        evaluacionDAO = new EvaluacionTriageDAO();
        
        // Configurar información del doctor
        if (lblDoctorName != null) {
            lblDoctorName.setText("Dr. Ana García");
        }
        if (lblDoctorRole != null) {
            lblDoctorRole.setText("Médico Triage");
        }
        
        // Generar folio automático
        if (txtNumeroFolio != null) {
            txtNumeroFolio.setText("TRG-" + System.currentTimeMillis() % 100000);
        }
        
        // Configurar fecha actual
        if (txtFechaIngreso != null) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            txtFechaIngreso.setText(now.format(formatter));
        }
    }
    
    // ===== MÉTODOS PARA BOTONES DE TRIAGE =====
    
    @FXML
    private void handleTriageRojo(ActionEvent event) {
        seleccionarNivelTriage("ROJO", "ROJO - Crítico", btnTriageRojo);
    }
    
    @FXML
    private void handleTriageNaranja(ActionEvent event) {
        seleccionarNivelTriage("NARANJA", "NARANJA - Urgente", btnTriageNaranja);
    }
    
    @FXML
    private void handleTriageAmarillo(ActionEvent event) {
        seleccionarNivelTriage("AMARILLO", "AMARILLO - Menos Urgente", btnTriageAmarillo);
    }
    
    @FXML
    private void handleTriageVerde(ActionEvent event) {
        seleccionarNivelTriage("VERDE", "VERDE - No Urgente", btnTriageVerde);
    }
    
    @FXML
    private void handleTriageAzul(ActionEvent event) {
        seleccionarNivelTriage("AZUL", "AZUL - Consulta Externa", btnTriageAzul);
    }
    
    private void seleccionarNivelTriage(String nivel, String texto, Button botonSeleccionado) {
        nivelTriageSeleccionado = nivel;
        lblTriageSeleccionado.setText(texto);
        
        // Resetear estilos de todos los botones
        resetearEstilosTriageButtons();
        
        // Aplicar estilo seleccionado
        botonSeleccionado.setStyle(botonSeleccionado.getStyle() + "; -fx-border-color: #2C3E50; -fx-border-width: 3;");
        
        System.out.println("🎯 Nivel de triage seleccionado: " + nivel);
    }
    
    private void resetearEstilosTriageButtons() {
        Button[] botonesTriaje = {btnTriageRojo, btnTriageNaranja, btnTriageAmarillo, btnTriageVerde, btnTriageAzul};
        for (Button btn : botonesTriaje) {
            if (btn != null) {
                String estilo = btn.getStyle();
                estilo = estilo.replaceAll("; -fx-border-color: #[0-9A-F]{6}; -fx-border-width: [0-9];", "");
                btn.setStyle(estilo);
            }
        }
    }
    
    // ===== MÉTODOS PARA BOTONES DE ESPECIALIDADES =====
    
    @FXML
    private void handleEspecialidadGeneral(ActionEvent event) {
        seleccionarEspecialidad("Medicina General", btnEspecialidadGeneral);
    }
    
    @FXML
    private void handleEspecialidadCardiologia(ActionEvent event) {
        seleccionarEspecialidad("Cardiología", btnEspecialidadCardiologia);
    }
    
    @FXML
    private void handleEspecialidadPediatria(ActionEvent event) {
        seleccionarEspecialidad("Pediatría", btnEspecialidadPediatria);
    }
    
    @FXML
    private void handleEspecialidadTraumatologia(ActionEvent event) {
        seleccionarEspecialidad("Traumatología", btnEspecialidadTraumatologia);
    }
    
    @FXML
    private void handleEspecialidadGinecologia(ActionEvent event) {
        seleccionarEspecialidad("Ginecología", btnEspecialidadGinecologia);
    }
    
    @FXML
    private void handleEspecialidadNeurologia(ActionEvent event) {
        seleccionarEspecialidad("Neurología", btnEspecialidadNeurologia);
    }
    
    @FXML
    private void handleEspecialidadPsiquiatria(ActionEvent event) {
        seleccionarEspecialidad("Psiquiatría", btnEspecialidadPsiquiatria);
    }
    
    @FXML
    private void handleEspecialidadDermatologia(ActionEvent event) {
        seleccionarEspecialidad("Dermatología", btnEspecialidadDermatologia);
    }
    
    private void seleccionarEspecialidad(String especialidad, Button botonSeleccionado) {
        especialidadSeleccionada = especialidad;
        lblEspecialidadSeleccionada.setText("Especialidad: " + especialidad);
        
        // Resetear estilos de todos los botones
        resetearEstilosEspecialidadButtons();
        
        // Aplicar estilo seleccionado
        botonSeleccionado.setStyle(botonSeleccionado.getStyle() + "; -fx-border-color: #2C3E50; -fx-border-width: 2;");
        
        System.out.println("🏥 Especialidad seleccionada: " + especialidad);
    }
    
    private void resetearEstilosEspecialidadButtons() {
        Button[] botonesEspecialidad = {
            btnEspecialidadGeneral, btnEspecialidadCardiologia, btnEspecialidadPediatria, 
            btnEspecialidadTraumatologia, btnEspecialidadGinecologia, btnEspecialidadNeurologia, 
            btnEspecialidadPsiquiatria, btnEspecialidadDermatologia
        };
        for (Button btn : botonesEspecialidad) {
            if (btn != null) {
                String estilo = btn.getStyle();
                estilo = estilo.replaceAll("; -fx-border-color: #[0-9A-F]{6}; -fx-border-width: [0-9];", "");
                btn.setStyle(estilo);
            }
        }
    }
    
    // ===== MÉTODOS PARA BOTONES DE ACCIÓN =====
    
    @FXML
    private void handleCancelar(ActionEvent event) {
        System.out.println("🚫 Evaluación cancelada");
        limpiarFormulario();
    }
    
    @FXML
    private void handleCompletarEvaluacion(ActionEvent event) {
        System.out.println("✅ Iniciando proceso de completar evaluación...");
        
        // Validar campos obligatorios
        if (!validarCamposObligatorios()) {
            return;
        }
        
        try {
            // Crear objeto EvaluacionTriage
            EvaluacionTriage evaluacion = crearEvaluacionDesdeFormulario();
            
            // Guardar en base de datos
            boolean guardado = evaluacionDAO.insertarEvaluacion(evaluacion);
            
            if (guardado) {
                showAlert("Éxito", "✅ Evaluación de triage completada y guardada correctamente\n\n" +
                         "📋 Folio: " + evaluacion.getNumeroFolio() + "\n" +
                         "🎯 Nivel: " + evaluacion.getNivelTriage() + "\n" +
                         "🏥 Especialidad: " + evaluacion.getEspecialidad(), Alert.AlertType.INFORMATION);
                
                limpiarFormulario();
            } else {
                showAlert("Error", "❌ No se pudo guardar la evaluación en la base de datos", Alert.AlertType.ERROR);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al completar evaluación: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "❌ Error inesperado: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private boolean validarCamposObligatorios() {
        StringBuilder errores = new StringBuilder();
        
        if (txtMotivoConsulta.getText().trim().isEmpty()) {
            errores.append("• El motivo de consulta es obligatorio\n");
        }
        
        if (nivelTriageSeleccionado == null) {
            errores.append("• Debe seleccionar un nivel de triage\n");
        }
        
        if (especialidadSeleccionada == null) {
            errores.append("• Debe seleccionar una especialidad médica\n");
        }
        
        if (errores.length() > 0) {
            showAlert("Campos Obligatorios", "❌ Por favor complete los siguientes campos:\n\n" + errores.toString(), Alert.AlertType.WARNING);
            return false;
        }
        
        return true;
    }
    
    private EvaluacionTriage crearEvaluacionDesdeFormulario() {
        EvaluacionTriage evaluacion = new EvaluacionTriage();
        
        evaluacion.setNumeroFolio(txtNumeroFolio.getText().trim());
        
        // Parsear fecha
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        evaluacion.setFechaIngreso(LocalDateTime.parse(txtFechaIngreso.getText().trim(), formatter));
        
        evaluacion.setMotivoConsulta(txtMotivoConsulta.getText().trim());
        evaluacion.setPresionArterial(txtPresionArterial.getText().trim().isEmpty() ? null : txtPresionArterial.getText().trim());
        
        // Parsear números con validación
        evaluacion.setFrecuenciaCardiaca(parseIntegerField(txtFrecuenciaCardiaca.getText()));
        evaluacion.setTemperatura(parseDoubleField(txtTemperatura.getText()));
        evaluacion.setFrecuenciaRespiratoria(parseIntegerField(txtFrecuenciaRespiratoria.getText()));
        evaluacion.setSaturacionO2(parseIntegerField(txtSaturacionO2.getText()));
        evaluacion.setNivelDolor(parseIntegerField(txtDolor.getText()));
        
        evaluacion.setObservacionesClinicas(txtObservacionesClinicas.getText().trim());
        evaluacion.setNivelTriage(nivelTriageSeleccionado);
        evaluacion.setEspecialidad(especialidadSeleccionada);
        evaluacion.setDoctorId(1); // ID del doctor (debería venir del login)
        
        return evaluacion;
    }
    
    private Integer parseIntegerField(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Double parseDoubleField(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private void limpiarFormulario() {
        // Limpiar campos de texto
        txtMotivoConsulta.clear();
        txtPresionArterial.clear();
        txtFrecuenciaCardiaca.clear();
        txtTemperatura.clear();
        txtFrecuenciaRespiratoria.clear();
        txtSaturacionO2.clear();
        txtDolor.clear();
        txtObservacionesClinicas.clear();
        
        // Resetear selecciones
        nivelTriageSeleccionado = null;
        especialidadSeleccionada = null;
        lblTriageSeleccionado.setText("Selecciona un nivel");
        lblEspecialidadSeleccionada.setText("Selecciona una especialidad");
        
        // Resetear estilos de botones
        resetearEstilosTriageButtons();
        resetearEstilosEspecialidadButtons();
        
        // Generar nuevo folio
        txtNumeroFolio.setText("TRG-" + System.currentTimeMillis() % 100000);
        
        // Actualizar fecha
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        txtFechaIngreso.setText(now.format(formatter));
        
        System.out.println("🧹 Formulario limpiado y listo para nueva evaluación");
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showAlert(String title, String message) {
        showAlert(title, message, Alert.AlertType.INFORMATION);
    }
}