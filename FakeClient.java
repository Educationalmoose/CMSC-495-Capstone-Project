import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.Gson;

public class FakeClient {

    public static void main(String[] args) {
        // payload represents the JSON data that we're sending to the flask server for analysis
        int[][] payload = {
                {1, 2, 3},
                {3, 4, 5},
                {5, 6, 7}
        };

        // send a POST request to the flask server with the payload and print the response
        String serverResponse = getServerResponse(payload);
        System.out.println("Server responded with:\n" + serverResponse);
    }

    public static String getServerResponse(int[][] matrix) {
        try {
            // Convert the matrix to JSON format using Gson
            Gson gson = new Gson();
            String jsonPayload = gson.toJson(matrix);

            // Wrap the JSON payload with the name "matrix" to match the expected format on the server
            String finalJson = "{ \"matrix\": " + jsonPayload + " }";

            // Create an HTTP client and create a POST request with the payload
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:5000/predict"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(finalJson))
                    .build();
            
            // Send the request and get the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();

        } catch (Exception e) {
            return "Error connecting to server." + e.getMessage();
        }
    }
}