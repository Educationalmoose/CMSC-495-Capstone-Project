/**Keeps track of different AI models (e.g., Digit vs Object).
 */
public class ModelRegistry {
    private String activeEndpoint = "/predict/general";    public void switchModel(String type) {
        this.activeEndpoint = "/predict/" + type.toLowerCase();
    }    public String getEndpoint() { return activeEndpoint; }
}
