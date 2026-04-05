import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
public class DrawingApp extends JFrame {
    private DrawingEngine engine;
    private ToolManager toolManager;
    private JLabel resultLabel;    
    public DrawingApp() {
        setTitle("AI Drawing Application");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        // Initialize Core Infrastructure
        engine = new DrawingEngine(800, 600);
        toolManager = new ToolManager();
        // 1. Create the Drawing Canvas (Interaction Layer)
        JPanel canvasPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(engine.getCanvas(), 0, 0, null);
            }
        };
        // 2. Mouse Listeners for Drawing
        canvasPanel.addMouseMotionListener(new MouseMotionAdapter() {
            private int lastX, lastY;            @Override
            public void mouseMoved(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
            }            @Override
            public void mouseDragged(MouseEvent e) {
                if (toolManager.getActiveTool() == ToolManager.Tool.BRUSH) {
                    engine.drawLine(lastX, lastY, e.getX(), e.getY());
                    lastX = e.getX();
                    lastY = e.getY();
                    canvasPanel.repaint();
                }
            }
        });
        // 3. Control Panel (UI Layer)
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));        
        JButton brushBtn = new JButton("Brush");
        // FIX: Use ToolManager.Tool.BRUSH (Enum) instead of "Brush" (String)
        brushBtn.addActionListener(e -> toolManager.setActiveTool(ToolManager.Tool.BRUSH));       
        JButton predictBtn = new JButton("Analyze Drawing");
        predictBtn.addActionListener(e -> {
            // FIX: Pass the BufferedImage from the engine to the client
            String prediction = InferenceClient.getPrediction(engine.getCanvas());
            resultLabel.setText("Prediction: " + prediction);
        });       
        resultLabel = new JLabel("Result: Draw something!");       
        controlPanel.add(brushBtn);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(predictBtn);
        controlPanel.add(resultLabel);
        // Assemble the frame
        add(canvasPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.WEST);        setLocationRelativeTo(null);
        setVisible(true);
    }    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DrawingApp());
    }
}
