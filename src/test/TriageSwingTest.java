package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TriageSwingTest extends JFrame {
    private String nivelSeleccionado = "";
    private String especialidadSeleccionada = "";
    
    public TriageSwingTest() {
        setTitle("Hospital Santa Vida - Triage (Demo)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(800, 600);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(245, 243, 240));
        JLabel titleLabel = new JLabel("Sistema de Triage - Hospital Santa Vida", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(46, 89, 132));
        headerPanel.add(titleLabel);
        
        // Panel de Triage
        JPanel triagePanel = new JPanel(new GridLayout(2, 3, 10, 10));
        triagePanel.setBorder(BorderFactory.createTitledBorder("Nivel de Triage"));
        
        // Botones de triage con colores
        JButton btnRojo = createTriageButton("ROJO - Crítico", new Color(229, 62, 62));
        JButton btnNaranja = createTriageButton("NARANJA - Urgente", new Color(255, 140, 66));
        JButton btnAmarillo = createTriageButton("AMARILLO - Menos Urgente", new Color(247, 215, 148));
        JButton btnVerde = createTriageButton("VERDE - No Urgente", new Color(88, 214, 141));
        JButton btnAzul = createTriageButton("AZUL - Consulta Externa", new Color(93, 173, 226));
        
        triagePanel.add(btnRojo);
        triagePanel.add(btnNaranja);
        triagePanel.add(btnAmarillo);
        triagePanel.add(btnVerde);
        triagePanel.add(btnAzul);
        
        // Panel de Especialidades
        JPanel especialidadPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        especialidadPanel.setBorder(BorderFactory.createTitledBorder("Especialidades Médicas"));
        
        String[] especialidades = {"Medicina General", "Cardiología", "Pediatría", "Traumatología", 
                                 "Ginecología", "Neurología", "Psiquiatría", "Dermatología"};
        
        for (String esp : especialidades) {
            JButton btn = new JButton(esp);
            btn.setBackground(new Color(78, 205, 196));
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Arial", Font.BOLD, 12));
            btn.addActionListener(e -> {
                especialidadSeleccionada = esp;
                showMessage("Especialidad seleccionada: " + esp);
            });
            especialidadPanel.add(btn);
        }
        
        mainPanel.add(headerPanel);
        mainPanel.add(triagePanel);
        mainPanel.add(especialidadPanel);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Panel de botones inferiores
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton btnTest = new JButton("Probar Conexión DB");
        btnTest.setBackground(new Color(52, 152, 219));
        btnTest.setForeground(Color.WHITE);
        btnTest.setFont(new Font("Arial", Font.BOLD, 14));
        btnTest.addActionListener(e -> testDatabase());
        
        bottomPanel.add(btnTest);
        add(bottomPanel, BorderLayout.SOUTH);
        
        setLocationRelativeTo(null);
    }
    
    private JButton createTriageButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(color.getRed() + color.getGreen() + color.getBlue() > 400 ? Color.BLACK : Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.addActionListener(e -> {
            nivelSeleccionado = text.split(" - ")[0];
            showMessage("Nivel de triage seleccionado: " + nivelSeleccionado);
        });
        return btn;
    }
    
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void testDatabase() {
        try {
            // Simular test de BD
            Class.forName("com.mysql.cj.jdbc.Driver");
            showMessage("✅ Driver MySQL encontrado!\n\n" +
                       "Nivel seleccionado: " + (nivelSeleccionado.isEmpty() ? "Ninguno" : nivelSeleccionado) + "\n" +
                       "Especialidad: " + (especialidadSeleccionada.isEmpty() ? "Ninguna" : especialidadSeleccionada) + "\n\n" +
                       "El sistema está listo para JavaFX!");
        } catch (ClassNotFoundException e) {
            showMessage("❌ Driver MySQL no encontrado.\nVerifica que mysql-connector-j-9.1.0.jar esté en lib/");
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TriageSwingTest().setVisible(true);
        });
    }
}