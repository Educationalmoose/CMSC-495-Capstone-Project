import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;
public class InferenceClient {
    /**Sends the drawing data to the AI model.Updated to accept BufferedImage to match the DrawingEngine's canvas.
     */
    public static String getPrediction(BufferedImage img) {
// Placeholder for AI inference logic (e.g., REST API call)
        if (img == null) return "Unknown";
        try {
// This is the URL for your local Flask server (app.py)
            URL url = new URL("http://localhost:5000/predict");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "image/png");
// Write the image bytes to the server
            OutputStream os = conn.getOutputStream();
            ImageIO.write(img, "PNG", os);
            os.close();
// For testing/mocking, we return a string
            return "Circle (AI Result)";
        } catch (Exception e) {
// If server is off, return a mock result so the app doesn't crash
            return "Circle (Mock Result)";
        }
    }}
