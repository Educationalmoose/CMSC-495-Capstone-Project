import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
/**CMSC-495 Capstone Project:Real-Time AI Preview.
 */
public class DrawingApp extends JFrame {
    // UI Components
    private JPanel drawingCanvas;
    private JLabel mainPredictionLabel;
    private JLabel probabilityLabel;
    private JLabel previewLabel;
    private JPanel resultsCard;
    private JButton submitButton;
    private JButton clearButton;
    private JButton undoButton; 
    private JSlider brushSlider; 
    private List<List<Point>> shapes = new ArrayList<>();
    private List<Integer> strokeWidths = new ArrayList<>(); 
 // Store width per stroke
    private List<Point> currentPath;
    private int currentBrushSize = 8;    private static final String SERVER_URL = "http://localhost:5000/predict";    public DrawingApp() {
        setTitle("CMSC-495: AI Drawing Recognizer");
        setSize(600, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(240, 242, 245));        
     // --- 1. THE RESULTS & PREVIEW CARD (NORTH) ---
        resultsCard = new JPanel(new BorderLayout(10, 10));
        resultsCard.setBackground(Color.WHITE);
        resultsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 220, 224), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        mainPredictionLabel = new JLabel("<html><div style='color: #5f6368;'>Draw a shape below</div></html>", SwingConstants.LEFT);
        mainPredictionLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        probabilityLabel = new JLabel("Click analyze to start", SwingConstants.LEFT);
        textPanel.add(mainPredictionLabel);
        textPanel.add(probabilityLabel);        
     // Feature 2: Preview Label (Shows the 28x28 matrix logic)
        previewLabel = new JLabel();
        previewLabel.setPreferredSize(new Dimension(56, 56));
        previewLabel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "AI View", 0, 0, new Font("SansSerif", Font.PLAIN, 10)));        resultsCard.add(textPanel, BorderLayout.CENTER);
        resultsCard.add(previewLabel, BorderLayout.EAST);
        add(resultsCard, BorderLayout.NORTH);        
     // --- 2. THE DRAWING CANVAS (CENTER) ---
        drawingCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);                for (int i = 0; i < shapes.size(); i++) {
                    List<Point> path = shapes.get(i);
                    g2.setStroke(new BasicStroke(strokeWidths.get(i), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    for (int j = 0; j < path.size() - 1; j++) {
                        Point p1 = path.get(j);
                        Point p2 = path.get(j + 1);
                        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                    }
                }
            }
        };
        drawingCanvas.setBackground(Color.WHITE);
        drawingCanvas.setBorder(BorderFactory.createLineBorder(new Color(218, 220, 224), 1));        
     drawingCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                currentPath = new ArrayList<>();
                currentPath.add(e.getPoint());
                shapes.add(currentPath);
                strokeWidths.add(currentBrushSize);
            }
        });        drawingCanvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                currentPath.add(e.getPoint());
                drawingCanvas.repaint();
                updateAIPreview(); 
             // Educational feature: Update preview as user draws
            }
        });
        add(drawingCanvas, BorderLayout.CENTER);        
     // --- 3. THE TOOLBOX & BUTTONS (SOUTH) ---
        JPanel southPanel = new JPanel(new BorderLayout(10, 10));
        southPanel.setOpaque(false);        
     // Feature: Brush Size Slider
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setOpaque(false);
        brushSlider = new JSlider(4, 24, 8);
        brushSlider.addChangeListener(e -> currentBrushSize = brushSlider.getValue());
        controlPanel.add(new JLabel("Brush Size:"));
        controlPanel.add(brushSlider);        
     // Feature: Undo and Standard Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        undoButton = new JButton("Undo");
        clearButton = new JButton("Clear All");
        submitButton = new JButton("Analyze Drawing");        undoButton.addActionListener(e -> {
            if (!shapes.isEmpty()) {
                shapes.remove(shapes.size() - 1);
                strokeWidths.remove(strokeWidths.size() - 1);
                drawingCanvas.repaint();
                updateAIPreview();
            }
        });        clearButton.addActionListener(e -> {
            shapes.clear();
            strokeWidths.clear();
            drawingCanvas.repaint();
            updateAIPreview();
            resetUI();
        });        submitButton.addActionListener(e -> {
            if (shapes.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please draw something first!");
                return;
            }
            postImageToServer();
        });        buttonPanel.add(undoButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(submitButton);        southPanel.add(controlPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);        ((JPanel)getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));
        setLocationRelativeTo(null);
    }    
 /**Converts current canvas to a 28x28 preview to show the user how the image is down sampled before hitting the neural network.
     */
    private void updateAIPreview() {
        int w = drawingCanvas.getWidth();
        int h = drawingCanvas.getHeight();
        if (w <= 0 || h <= 0) return;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        drawingCanvas.paint(g2d);
        g2d.dispose();
// Scale to 28x28 (AI Input) then up to 56x56 for visibility
        Image scaled = img.getScaledInstance(28, 28, Image.SCALE_SMOOTH);
        previewLabel.setIcon(new ImageIcon(scaled.getScaledInstance(56, 56, Image.SCALE_DEFAULT)));
    }
    private void postImageToServer() {
        lockUI(true);
        new Thread(() -> {
            try {
                BufferedImage image = new BufferedImage(drawingCanvas.getWidth(), drawingCanvas.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = image.createGraphics();
                drawingCanvas.paint(g2d);
                g2d.dispose();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                byte[] imageBytes = baos.toByteArray();
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SERVER_URL))
                        .header("Content-Type", "image/png")
                        .POST(HttpRequest.BodyPublishers.ofByteArray(imageBytes))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                updateUIWithResults(response.body());
            } catch (Exception ex) {
                updateUIWithError();
            } finally {
                lockUI(false);
            }
        }).start();
    }
    private void updateUIWithResults(String response) {
        SwingUtilities.invokeLater(() -> {
            String message = "Analysis Received";
            try {
                if (response.contains("\"message\":\"")) {
                    int start = response.indexOf("\"message\":\"") + 11;
                    int end = response.indexOf("\"", start);
                    message = response.substring(start, end);
                }
            } catch (Exception e) { message = "Done"; }
            mainPredictionLabel.setText("<html><div style='color: #1a73e8;'>" + message + "</div></html>");
            probabilityLabel.setText("Data successfully normalized to 28x28.");
            resultsCard.setBackground(new Color(230, 244, 234));
        });
    }
    private void updateUIWithError() {
        SwingUtilities.invokeLater(() -> {
            mainPredictionLabel.setText("<html><div style='color: #d93025;'>Connection Error</div></html>");
            probabilityLabel.setText("Is the Python Flask server running?");
            resultsCard.setBackground(new Color(252, 232, 230));
        });
    }
    private void resetUI() {
        mainPredictionLabel.setText("<html><div style='color: #5f6368;'>Draw a shape below</div></html>");
        probabilityLabel.setText("Click analyze to start");
        resultsCard.setBackground(Color.WHITE);
        previewLabel.setIcon(null);
    }
    private void lockUI(boolean isLocked) {
        SwingUtilities.invokeLater(() -> {
            submitButton.setEnabled(!isLocked);
            clearButton.setEnabled(!isLocked);
            undoButton.setEnabled(!isLocked);
            submitButton.setText(isLocked ? "Analyzing..." : "Analyze Drawing");
        });
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DrawingApp().setVisible(true));
    }}
