import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;
/**Service for exporting the drawing to external files.
 */
public class ExportService {
 public static void save(BufferedImage image, String filename) {
  try {
   File output = new File(filename + ".png");
   ImageIO.write(image, "PNG", output);
   System.out.println("Saved: " + output.getAbsolutePath());
  } catch (Exception e) {
   System.err.println("Export failed: " + e.getMessage());
  }
 }
}
