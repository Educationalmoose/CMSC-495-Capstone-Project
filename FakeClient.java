import java.io.ByteArrayOutputStream;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Scanner;

public class FakeClient {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.println("Enter the name of the image file to upload: ");
        String userInput = input.nextLine();

        String response = postImageToServer(Path.of(userInput));
        System.out.println("Response from server: " + response);
    }

    public static String postImageToServer(Path imagePath) {
        try {
            /*

            // This code is commented out because we don't have a drawing canvas in the fake client yet. But, this is what you would need to do to capture the contents of the drawing canvas as an image and send it to the server.

            // make a new image with the same dimensions as the drawing canvas
            int width = drawingCanvas.getWidth();
            int height = drawingCanvas.getHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            // draw the contents of the drawing canvas onto the image
            Graphics2D g2d = image.createGraphics();
            drawingCanvas.paint(g2d);
            g2d.dispose();

            // Convert the image to a byte array in PNG format
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            */

            // just read the file as bytes instead of creating an image from the canvas, since we don't have a canvas in the fake client
            byte[] imageBytes = Files.readAllBytes(imagePath);

            // Create an HTTP client and create a POST request with the payload
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:5000/predict"))
                    .header("Content-Type", "image/png")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(imageBytes))
                    .build();
            
            // Send the request and get the response
            System.out.println("Uploading " + imageBytes.length + " bytes to server");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();

        } catch (Exception e) {
            return "Error connecting to server." + e.getMessage();
        }
    }
}