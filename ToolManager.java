/**Manages the active state of drawing tools.
 */
public class ToolManager {
    public enum Tool { BRUSH, ERASER, BUCKET, RECTANGLE, CIRCLE }
    private Tool activeTool = Tool.BRUSH;
    public void setActiveTool(Tool tool) {
        this.activeTool = tool;
    }    public Tool getActiveTool() {
        return activeTool;
    }
}
