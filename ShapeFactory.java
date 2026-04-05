import java.awt.*;
/**Handles complex shape rendering logic.
 */
public class ShapeFactory {
 public static void drawShape(Graphics2D g2d, ToolManager.Tool type, int x, int y, int w, int h) {
  switch (type) {
   case RECTANGLE -> g2d.drawRect(x, y, w, h);
   case CIRCLE -> g2d.drawOval(x, y, w, h);
  }
 }
}
