import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;public class FakeClient {
    private static final String SERVER_URL = "http://127.0.0.1:5000/predict";    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter the name of the image file to upload: ");
        if (input.hasNextLine()) {
            String userInput = input.nextLine();
            System.out.println("Response: " + postImageToServer(Path.of(userInput)));
        }
        input.close();
    }    public static String postImageToServer(Path imagePath) {
        try {
            byte[] imageBytes = Files.readAllBytes(imagePath);
            return postImageToServer(imageBytes);
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }    public static String postImageToServer(byte[] imageBytes) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL))
                    .header("Content-Type", "image/png")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(imageBytes))
                    .build();            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            return "Server Error: " + e.getMessage();
        }
    }
}
