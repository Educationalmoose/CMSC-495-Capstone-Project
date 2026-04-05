import javax.swing.*;
import java.awt.*;
/**Manages the "Dark Mode" and "Light Mode" aesthetic.
 */
public class ThemeManager {
    public static void applyDarkMode(JFrame frame) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("control", new Color(45, 45, 45));
            UIManager.put("nimbusBase", new Color(18, 18, 18));
            SwingUtilities.updateComponentTreeUI(frame);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
