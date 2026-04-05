import java.awt.*;
import java.awt.image.BufferedImage;
/**The core rendering engine. Manages the pixel data and provides methods to modify the canvas.
 */
public class DrawingEngine {
    private BufferedImage canvas;
    private Graphics2D g2d;
    private Color currentColor = Color.BLACK;
    private int strokeWidth = 5;    public DrawingEngine(int width, int height) {
        // Create an ARGB image for transparency support
        canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = canvas.createGraphics();
        // Enable Anti-Aliasing for smooth lines
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        resetCanvas();
    }    public void resetCanvas() {
        g2d.setComposite(AlphaComposite.Src);
        g2d.setColor(Color.WHITE);
        // Default background
        g2d.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g2d.setColor(currentColor);
    }    public void drawLine(int x1, int y1, int x2, int y2) {
        g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(x1, y1, x2, y2);
    }
    // Accessors
    public BufferedImage getCanvas() { return canvas; }
    public Graphics2D getContext() { return g2d; }
    public void setColor(Color c) { this.currentColor = c; g2d.setColor(c); }
    public void setStrokeWidth(int w) { this.strokeWidth = w; }
}
