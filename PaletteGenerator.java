import java.awt.Color;
/**Suggests colors based on what the AI thinks you are drawing.
 */
public class PaletteGenerator {
    public static Color[] suggest(String aiLabel) {
        if (aiLabel.contains("Forest")) return new Color[]{Color.GREEN, Color.DARK_GRAY};
        if (aiLabel.contains("Ocean")) return new Color[]{Color.BLUE, Color.CYAN};
        return new Color[]{Color.BLACK, Color.WHITE};
    }
}
