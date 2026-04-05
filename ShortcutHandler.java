import javax.swing.*;
import java.awt.event.ActionEvent;
/**Maps keyboard inputs (Ctrl+Z, Ctrl+S) to app functions.
 */
public class ShortcutHandler {
    public static void register(JComponent component, String stroke, Runnable action) {
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(stroke), stroke);
        component.getActionMap().put(stroke, new AbstractAction() {
            public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }
}
