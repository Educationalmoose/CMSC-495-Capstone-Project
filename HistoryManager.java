import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Stack;
/**Stores canvas states to allow undoing mistakes.
 */
public class HistoryManager {
    // A stack to hold previous versions of the canvas
    private final Stack<BufferedImage> history = new Stack<>();
    /**Saves a snapshot of the current canvas.@param current The BufferedImage from the DrawingEngine.
     */
     public void saveState(BufferedImage current) {
     if (current == null) return;
     // 1. Create a blank image of the same size and type
     BufferedImage copy = new BufferedImage(
     current.getWidth(),
     current.getHeight(),
     current.getType()
     );
     // 2. Use the Graphics class to draw the current canvas onto the copy
     // This is where the "Graphics" class is used!
     Graphics g = copy.getGraphics();
     g.drawImage(current, 0, 0, null);
     g.dispose();
     // Always dispose of graphics objects when finished
     // 3. Push the copy onto the stack
     history.push(copy);
     // Optional: Prevent memory issues by limiting history to 20 steps
     if (history.size() > 20) {
     history.remove(0);
     }
     }
     /**Returns the last saved state.
     */
    public BufferedImage undo() {
        if (history.isEmpty()) {
            return null;
        }
        return history.pop();
    }}
