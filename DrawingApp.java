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

    private JPanel topScoresPanel;
    private JTextArea topScoresArea;
    private JTextArea drawableObjectsArea;

    private List<List<Point>> shapes = new ArrayList<>();
    private List<Integer> brushSizes = new ArrayList<>();
    private List<Point> currentPath;
    private int currentBrushSize = 24;

    private static Process pythonServerProcess;
    private static final String SERVER_URL = "http://localhost:5000/predict";

    private static final int CANVAS_SIZE = 400;

    public DrawingApp() {
        setTitle("CMSC-495: AI Drawing Recognizer");
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(675, (int)(screen.height * 0.75));
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(240, 242, 245));

        // --- 1. RESULTS CARD ---
        resultsCard = new JPanel(new GridLayout(2, 1, 5, 5));
        resultsCard.setBackground(Color.WHITE);
        resultsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 220, 224), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        mainPredictionLabel = new JLabel(
                "<html><div style='text-align: center; color: #5f6368;'>Draw a shape below</div></html>",
                SwingConstants.CENTER
        );
        mainPredictionLabel.setFont(new Font("SansSerif", Font.BOLD, 22));

        probabilityLabel = new JLabel(
                "<html><div style='text-align: center; color: #80868b;'>Click the button to analyze</div></html>",
                SwingConstants.CENTER
        );
        probabilityLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        resultsCard.add(mainPredictionLabel);
        resultsCard.add(probabilityLabel);
        add(resultsCard, BorderLayout.NORTH);

        // --- 2. DRAWING CANVAS ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        drawingCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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

        drawingCanvas.setPreferredSize(new Dimension(CANVAS_SIZE, CANVAS_SIZE));
        drawingCanvas.setMaximumSize(new Dimension(CANVAS_SIZE, CANVAS_SIZE));
        drawingCanvas.setBackground(Color.WHITE);
        drawingCanvas.setBorder(BorderFactory.createLineBorder(new Color(218, 220, 224), 1));

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
                updateGhostPreview();
            }
        });

        previewLabel = new JLabel("Scaled AI Input Preview (28x28)", SwingConstants.CENTER);
        previewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        previewLabel.setBorder(new EmptyBorder(10, 0, 10, 0));

        centerPanel.add(drawingCanvas);
        centerPanel.add(previewLabel);
        add(centerPanel, BorderLayout.CENTER);

        // --- 3. RIGHT-SIDE INFORMATION PANEL ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(230, 500));

        topScoresPanel = new JPanel(new BorderLayout());
        topScoresPanel.setBackground(Color.WHITE);
        topScoresPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 220, 224), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));
        topScoresPanel.setMaximumSize(new Dimension(230, 180));

        JLabel topScoresTitle = new JLabel("Top 5 Predictions", SwingConstants.CENTER);
        topScoresTitle.setFont(new Font("SansSerif", Font.BOLD, 15));

        topScoresArea = new JTextArea();
        topScoresArea.setEditable(false);
        topScoresArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        topScoresArea.setLineWrap(true);
        topScoresArea.setWrapStyleWord(true);
        topScoresArea.setBackground(Color.WHITE);
        topScoresArea.setText("No analysis yet.");

        JScrollPane scoreScrollPane = new JScrollPane(topScoresArea);
        scoreScrollPane.setPreferredSize(new Dimension(200, 110));

        topScoresPanel.add(topScoresTitle, BorderLayout.NORTH);
        topScoresPanel.add(scoreScrollPane, BorderLayout.CENTER);

        JPanel objectsPanel = new JPanel(new BorderLayout());
        objectsPanel.setBackground(Color.WHITE);
        objectsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 220, 224), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));
        objectsPanel.setMaximumSize(new Dimension(230, 310));

        JLabel objectsTitle = new JLabel("Objects You Can Draw", SwingConstants.CENTER);
        objectsTitle.setFont(new Font("SansSerif", Font.BOLD, 15));

        drawableObjectsArea = new JTextArea();
        drawableObjectsArea.setEditable(false);
        drawableObjectsArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        drawableObjectsArea.setLineWrap(true);
        drawableObjectsArea.setWrapStyleWord(true);
        drawableObjectsArea.setText(
                "apple\nbanana\nbaseball\nbee\nbird\nbutterfly\ncamera\ncar\ncat\ncircle\n" +
                "clock\ncloud\ncookie\ndog\ndonut\nduck\nenvelope\neye\neyeglasses\nface\n" +
                "fish\nflower\nfoot\nfork\nfrog\ngiraffe\ngrapes\nguitar\nhand\nhat\nhorse\n" +
                "house\nice cream\nleaf\nleg\nlight bulb\nlighthouse\nlightning\nlollipop\nmoon\n" +
                "mountain\noctopus\npalm tree\npants\npeanut\nscissors\nshark\nshoe\nshovel\n" +
                "smiley face\nsnail\nsnowflake\nsnowman\nsquare\nstar\nstrawberry\nsun\nsword\n" +
                "tooth\ntree\ntriangle\nwheel\nwindmill"
        );

        JScrollPane objectsScrollPane = new JScrollPane(drawableObjectsArea);
        objectsScrollPane.setPreferredSize(new Dimension(200, 240));

        objectsPanel.add(objectsTitle, BorderLayout.NORTH);
        objectsPanel.add(objectsScrollPane, BorderLayout.CENTER);

        rightPanel.add(topScoresPanel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        rightPanel.add(objectsPanel);

        add(rightPanel, BorderLayout.EAST);

        // --- 4. BUTTONS ---
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.setOpaque(false);

        JPanel sliderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sliderPanel.setOpaque(false);
        sliderPanel.add(new JLabel("Brush Size: "));

        brushSlider = new JSlider(16, 32, 24);
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

        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        setLocationRelativeTo(null);
        updateGhostPreview();

        new Thread(() -> startPythonServer()).start();
    }

    private void updateGhostPreview() {
        int w = Math.max(drawingCanvas.getWidth(), CANVAS_SIZE);
        int h = Math.max(drawingCanvas.getHeight(), CANVAS_SIZE);

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        drawingCanvas.paint(g2);
        g2.dispose();

        Image scaled = img.getScaledInstance(28, 28, Image.SCALE_SMOOTH);
        BufferedImage previewImg = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D gPreview = previewImg.createGraphics();
        gPreview.drawImage(scaled, 0, 0, null);
        gPreview.dispose();

        previewLabel.setIcon(new ImageIcon(previewImg.getScaledInstance(28, 28, Image.SCALE_REPLICATE)));
    }

    private void postImageToServer() {
        lockUI(true);

        new Thread(() -> {
            try {
                int width = drawingCanvas.getWidth();
                int height = drawingCanvas.getHeight();

                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

                Graphics2D g2d = image.createGraphics();
                drawingCanvas.paint(g2d);
                g2d.dispose();

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
            String prediction = "Analysis Received";
            String confidence = "0";
            String uncertainty = "100";
            String className = "Unknown";

            java.util.Map<String, Double> allScores = new java.util.HashMap<>();

            try {
                if (response.contains("\"class\": \"")) {
                    int start = response.indexOf("\"class\": \"") + 10;
                    int end = response.indexOf("\"", start);
                    className = response.substring(start, end);
                }

                if (response.contains("\"confidence_percent\": ")) {
                    int start = response.indexOf("\"confidence_percent\": ") + 22;
                    int end = response.indexOf(",", start);

                    if (end == -1 || (response.indexOf("\n", start) < end && response.indexOf("\n", start) != -1)) {
                        end = response.indexOf("\n", start);
                    }

                    if (end == -1) {
                        end = response.indexOf("}", start);
                    }

                    confidence = response.substring(start, end).trim();
                }

                if (response.contains("\"uncertainty_percent\": ")) {
                    int start = response.indexOf("\"uncertainty_percent\": ") + 23;
                    int end = response.indexOf(",", start);

                    if (end == -1 || (response.indexOf("\n", start) < end && response.indexOf("\n", start) != -1)) {
                        end = response.indexOf("\n", start);
                    }

                    if (end == -1) {
                        end = response.indexOf("}", start);
                    }

                    uncertainty = response.substring(start, end).trim();
                }

                if (response.contains("\"all_scores_percent\": {")) {
                    int start = response.indexOf("\"all_scores_percent\": {") + 23;
                    int end = response.indexOf("}", start);

                    if (start != 22 && end != -1) {
                        String dictString = response.substring(start, end).trim();
                        String[] pairs = dictString.split(",");

                        for (String pair : pairs) {
                            String[] keyValue = pair.split(":");

                            if (keyValue.length == 2) {
                                String key = keyValue[0].replaceAll("\"", "").trim();
                                String valueStr = keyValue[1].trim();

                                try {
                                    allScores.put(key, Double.parseDouble(valueStr));
                                } catch (NumberFormatException e) {
                                    // Ignore values that cannot be parsed
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                prediction = "Done";
            }

            prediction = String.format("Did you draw a %s?", className);

            mainPredictionLabel.setText(
                    "<html><div style='text-align: center; color: #1a73e8;'>" + prediction + "</div></html>"
            );

            probabilityLabel.setText(
                    "<html>AI Confidence: " + confidence + "%<br>Uncertainty: " + uncertainty + "%</html>"
            );

            resultsCard.setBackground(new Color(230, 244, 234));

            updateTopScoresDisplay(allScores);
        });
    }

    private void updateTopScoresDisplay(java.util.Map<String, Double> allScores) {
        java.util.List<java.util.Map.Entry<String, Double>> sortedScores =
                new java.util.ArrayList<>(allScores.entrySet());

        sortedScores.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        StringBuilder sb = new StringBuilder();
        int limit = Math.min(5, sortedScores.size());

        if (limit == 0) {
            topScoresArea.setText("No score data available.");
            return;
        }

        for (int i = 0; i < limit; i++) {
            java.util.Map.Entry<String, Double> entry = sortedScores.get(i);

            sb.append(i + 1)
              .append(". ")
              .append(entry.getKey())
              .append(" - ")
              .append(String.format("%.2f%%", entry.getValue()))
              .append("\n");
        }

        topScoresArea.setText(sb.toString());
    }

    private void updateUIWithError() {
        SwingUtilities.invokeLater(() -> {
            mainPredictionLabel.setText("<html><div style='color: #d93025;'>Connection Error</div></html>");
            probabilityLabel.setText("Check if your Python server is running.");
            resultsCard.setBackground(new Color(252, 232, 230));
            topScoresArea.setText("Could not load predictions.");
        });
    }

    private void resetUI() {
        mainPredictionLabel.setText("<html><div style='color: #5f6368;'>Draw a shape below</div></html>");
        probabilityLabel.setText("Click the button to analyze");
        resultsCard.setBackground(Color.WHITE);
        topScoresArea.setText("No analysis yet.");
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

            String os = System.getProperty("os.name").toLowerCase();

            SwingUtilities.invokeLater(() -> {
                mainPredictionLabel.setText("<html><div style='color: #f29900;'>Locating Python...</div></html>");
            });

            String pythonPath = os.contains("win") ? "venv\\Scripts\\python.exe" : "venv/bin/python";

            SwingUtilities.invokeLater(() -> {
                mainPredictionLabel.setText("<html><div style='color: #f29900;'>Initializing Server...</div></html>");
            });

            ProcessBuilder pb = new ProcessBuilder(pythonPath, "server.py");
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);

            pythonServerProcess = pb.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (pythonServerProcess != null) {
                    pythonServerProcess.destroy();
                }
            }));

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