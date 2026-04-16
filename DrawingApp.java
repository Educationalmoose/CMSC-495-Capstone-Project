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
public class DrawingApp extends JFrame {
    private JPanel drawingCanvas;
    private JLabel mainPredictionLabel;
    private JLabel probabilityLabel;
    private JPanel resultsCard;
    private JButton submitButton;
    private JButton clearButton;
    private JButton undoButton;
    private JSlider brushSlider;
    private JLabel previewLabel;     
    // Logic to store the drawing points
    private List<List<Point>> shapes = new ArrayList<>();
    private List<Integer> brushSizes = new ArrayList<>(); // Track size per stroke
    private List<Point> currentPath;
    private int currentBrushSize = 8;    

    // Process allows us to boostrap the server onto the application, so users don't have to manually start it in the terminal
    private static Process pythonServerProcess;
    private static final String SERVER_URL = "http://localhost:5000/predict";
    
    // Maintain a square resolution for AI compatibility
    private static final int CANVAS_SIZE = 400;
    
    public DrawingApp() {
        setTitle("CMSC-495: AI Drawing Recognizer");
        setSize(500, 850);
        setResizable(false); // Lock the window so it is not resizable
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(240, 242, 245));        
        // --- 1. THE RESULTS CARD (NORTH) ---
        resultsCard = new JPanel(new GridLayout(2, 1, 5, 5));
        resultsCard.setBackground(Color.WHITE);
        resultsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 220, 224), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));        
        mainPredictionLabel = new JLabel("<html><div style='text-align: center; color: #5f6368;'>Draw a shape below</div></html>", SwingConstants.CENTER);
        mainPredictionLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        probabilityLabel = new JLabel("<html><div style='text-align: center; color: #80868b;'>Click the button to analyze</div></html>", SwingConstants.CENTER);
        probabilityLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));        
        resultsCard.add(mainPredictionLabel);
        resultsCard.add(probabilityLabel);
        add(resultsCard, BorderLayout.NORTH);        
        // --- 2. THE DRAWING CANVAS (CENTER) ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);        
        drawingCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);                
                // Redraw all shapes
                for (int i = 0; i < shapes.size(); i++) {
                    List<Point> path = shapes.get(i);
                    g2.setStroke(new BasicStroke(brushSizes.get(i), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.setColor(Color.BLACK);
                    for (int j = 0; j < path.size() - 1; j++) {
                        Point p1 = path.get(j);
                        Point p2 = path.get(j + 1);
                        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                    }
                }
            }
        };        
        // Force square resolution
        drawingCanvas.setPreferredSize(new Dimension(CANVAS_SIZE, CANVAS_SIZE));
        drawingCanvas.setMaximumSize(new Dimension(CANVAS_SIZE, CANVAS_SIZE));
        drawingCanvas.setBackground(Color.WHITE);
        drawingCanvas.setBorder(BorderFactory.createLineBorder(new Color(218, 220, 224), 1));        
        // Mouse Listeners for Drawing
        drawingCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                currentPath = new ArrayList<>();
                currentPath.add(e.getPoint());
                shapes.add(currentPath);
                brushSizes.add(currentBrushSize);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                updateGhostPreview();
            }
        });        
        drawingCanvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                currentPath.add(e.getPoint());
                drawingCanvas.repaint();
                updateGhostPreview(); // Ghost Preview (Real-Time Pre-processing)
            }
        });        
        previewLabel = new JLabel("Scaled AI Input Preview (90x90)", SwingConstants.CENTER);
        previewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        previewLabel.setBorder(new EmptyBorder(10, 0, 10, 0));        
        centerPanel.add(drawingCanvas);
        centerPanel.add(previewLabel);
        add(centerPanel, BorderLayout.CENTER);        
        // --- 3. THE BUTTONS (SOUTH) ---
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.setOpaque(false);        
        // Brush Size Adjustment
        JPanel sliderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sliderPanel.setOpaque(false);
        sliderPanel.add(new JLabel("Brush Size: "));
        brushSlider = new JSlider(4, 32, 8);
        brushSlider.addChangeListener(e -> currentBrushSize = brushSlider.getValue());
        sliderPanel.add(brushSlider);        
        submitButton = new JButton("Analyze Drawing");
        clearButton = new JButton("Clear Canvas");
        undoButton = new JButton("Undo Last Stroke");        
        submitButton.addActionListener(e -> {
            if (shapes.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please draw a shape first!");
                return;
            }
            postImageToServer();
        });        
        clearButton.addActionListener(e -> {
            shapes.clear();
            brushSizes.clear();
            drawingCanvas.repaint();
            updateGhostPreview();
            resetUI();
        });
        undoButton.addActionListener(e -> {
            if (!shapes.isEmpty()) {
                shapes.remove(shapes.size() - 1);
                brushSizes.remove(brushSizes.size() - 1);
                drawingCanvas.repaint();
                updateGhostPreview();
            }
        });        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(undoButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(submitButton);        
        southPanel.add(sliderPanel);
        southPanel.add(buttonPanel);
        add(southPanel, BorderLayout.SOUTH);        
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));
        setLocationRelativeTo(null);
        updateGhostPreview();

        // start the server in a new thread so it doesn't block the UI from showing up
        new Thread(() -> startPythonServer()).start();
    } 
    
    // New: Real-time Pre-processing "Ghost" Preview
    private void updateGhostPreview() {
        int w = Math.max(drawingCanvas.getWidth(), CANVAS_SIZE);
        int h = Math.max(drawingCanvas.getHeight(), CANVAS_SIZE);        
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        drawingCanvas.paint(g2);
        g2.dispose();        
        // Downsample to 90x90 grayscale
        Image scaled = img.getScaledInstance(90, 90, Image.SCALE_SMOOTH);
        BufferedImage previewImg = new BufferedImage(90, 90, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D gPreview = previewImg.createGraphics();
        gPreview.drawImage(scaled, 0, 0, null);
        gPreview.dispose();        
        previewLabel.setIcon(new ImageIcon(previewImg.getScaledInstance(90, 90, Image.SCALE_REPLICATE)));
    }
    
    private void postImageToServer() {
        lockUI(true);
        new Thread(() -> {
            try {
                // make a new image with the same dimensions as the drawing canvas
                int width = drawingCanvas.getWidth();
                int height = drawingCanvas.getHeight();
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);                
                // draw the contents of the drawing canvas onto the image
                Graphics2D g2d = image.createGraphics();
                drawingCanvas.paint(g2d);
                g2d.dispose();                
                // Convert the image to a byte array in PNG format
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(image, "png", byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();                
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
            // Simple logic to parse "message" from the JSON
            String message = "Analysis Received";
            try {
                if (response.contains("\"message\": \"")) {
                    int start = response.indexOf("\"message\": \"") + 11;
                    int end = response.indexOf("\"", start);
                    message = response.substring(start, end);
                }
            } catch (Exception e) {
                message = "Done";
            }
            mainPredictionLabel.setText("<html><div style='color: #1a73e8;'>" + message + "</div></html>");
            probabilityLabel.setText("Server processed drawing into 180x180 matrix.");
            resultsCard.setBackground(new Color(230, 244, 234));
        });
    }
    
    private void updateUIWithError() {
        SwingUtilities.invokeLater(() -> {
            mainPredictionLabel.setText("<html><div style='color: #d93025;'>Connection Error</div></html>");
            probabilityLabel.setText("Check if your Python server is running.");
            resultsCard.setBackground(new Color(252, 232, 230));
        });
    }
    
    private void resetUI() {
        mainPredictionLabel.setText("<html><div style='color: #5f6368;'>Draw a shape below</div></html>");
        probabilityLabel.setText("Click the button to analyze");
        resultsCard.setBackground(Color.WHITE);
    }
    
    private void lockUI(boolean isLocked) {
        SwingUtilities.invokeLater(() -> {
            submitButton.setEnabled(!isLocked);
            clearButton.setEnabled(!isLocked);
            undoButton.setEnabled(!isLocked);
            submitButton.setText(isLocked ? "Analyzing..." : "Analyze Drawing");
        });
    }

    private void startPythonServer() {
        try {
            SwingUtilities.invokeLater(() -> {
                lockUI(true);
                mainPredictionLabel.setText("<html><div style='color: #f29900;'>Determining operating system...</div></html>");
                probabilityLabel.setText("Please wait a moment...");
            });
            
            // figure out which is being used OS to get to the python in venv
            String os = System.getProperty("os.name").toLowerCase();

            SwingUtilities.invokeLater(() -> {
                mainPredictionLabel.setText("<html><div style='color: #f29900;'>Locating Python...</div></html>");
            });

            String pythonPath = os.contains("win") ? "venv\\Scripts\\python.exe" : "venv/bin/python";

            SwingUtilities.invokeLater(() -> {
                mainPredictionLabel.setText("<html><div style='color: #f29900;'>Initializing Server...</div></html>");
            });

            // build the terminal command
            ProcessBuilder pb = new ProcessBuilder(pythonPath, "server.py");
            
            // merge Python's error messages with its normal output
            pb.redirectErrorStream(true); 

            // forward Python's terminal output to Java's terminal so we can read it
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT); 

            // launch the  terminal
            pythonServerProcess = pb.start();

            // kill the server when the user closes the Java window
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (pythonServerProcess != null) {
                    pythonServerProcess.destroy();
                }
            }));

            // wait 1.5 seconds for the server to finish setting up
            Thread.sleep(2000); 

            SwingUtilities.invokeLater(() -> {
                resetUI();
                lockUI(false);
            });

        } catch (Exception e) {
            System.out.println("Failed to start the server: " + e.getMessage());
            updateUIWithError();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DrawingApp().setVisible(true));
    }
}
