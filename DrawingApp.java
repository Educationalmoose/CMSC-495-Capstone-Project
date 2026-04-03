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
    private JButton clearButton;    // Logic to store the drawing points
    private List<List<Point>> shapes = new ArrayList<>();
    private List<Point> currentPath;

    private static final String SERVER_URL = "http://localhost:5000/predict";
    
    public DrawingApp() {
        setTitle("CMSC-495: AI Drawing Recognizer");
        setSize(500, 700);
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
        drawingCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setStroke(new BasicStroke(8)); // Thicker stroke for better AI recognition
                g2.setColor(Color.BLACK);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (List<Point> path : shapes) {
                    for (int i = 0; i < path.size() - 1; i++) {
                        Point p1 = path.get(i);
                        Point p2 = path.get(i + 1);
                        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                    }
                }
            }
        };
        drawingCanvas.setBackground(Color.WHITE);
        drawingCanvas.setBorder(BorderFactory.createLineBorder(new Color(218, 220, 224), 1));
        
        // Mouse Listeners for Drawing
        drawingCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                currentPath = new ArrayList<>();
                currentPath.add(e.getPoint());
                shapes.add(currentPath);
            }
        });

        drawingCanvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                currentPath.add(e.getPoint());
                drawingCanvas.repaint();
            }
        });

        add(drawingCanvas, BorderLayout.CENTER);
        
        // --- 3. THE BUTTONS (SOUTH) ---
        submitButton = new JButton("Analyze Drawing");
        clearButton = new JButton("Clear Canvas");
        submitButton.addActionListener(e -> {
            if (shapes.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please draw a shape first!");
                return;
            }
            postImageToServer();
        });
        
        clearButton.addActionListener(e -> {
            shapes.clear();
            drawingCanvas.repaint();
            resetUI();
        });
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(clearButton);
        buttonPanel.add(submitButton);
        add(buttonPanel, BorderLayout.SOUTH);
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));
        setLocationRelativeTo(null);
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
            // Simple logic to parse "message" from the JSON: {"status":"success","message":"..."}
            String message = "Analysis Received";
            try {
                if (response.contains("\"message\":\"")) {
                    int start = response.indexOf("\"message\":\"") + 11;
                    int end = response.indexOf("\"", start);
                    message = response.substring(start, end);
                }
            } catch (Exception e) {
                message = "Done";
            }
            mainPredictionLabel.setText("<html><div style='color: #1a73e8;'>" + message + "</div></html>");
            probabilityLabel.setText("Server processed drawing into 28x28 matrix.");
            resultsCard.setBackground(new Color(230, 244, 234)); // Success Green
        });
    }
    
    private void updateUIWithError() {
        SwingUtilities.invokeLater(() -> {
            mainPredictionLabel.setText("<html><div style='color: #d93025;'>Connection Error</div></html>");
            probabilityLabel.setText("Check if your Python server is running.");
            resultsCard.setBackground(new Color(252, 232, 230)); // Error Red
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
            submitButton.setText(isLocked ? "Analyzing..." : "Analyze Drawing");
        });
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DrawingApp().setVisible(true));
    }
}
