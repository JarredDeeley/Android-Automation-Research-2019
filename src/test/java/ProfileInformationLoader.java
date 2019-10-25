import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class ProfileInformationLoader {
    JsonReader reader;
    JsonObject profile;

    public ProfileInformationLoader() throws FileNotFoundException {
        reader = Json.createReader(new FileReader("profile.json"));
        profile = reader.readObject();

        reader.close();
    }

    public String get_google_email() {
        String google_email = "";

        try{
            google_email = profile.getString("google_email");
        } catch (NullPointerException e){}

        return google_email;
    }

    public String get_age() { return profile.getString("age"); }

    public String get_weight_kg() { return profile.getString("weight_kg"); }

    public String get_height() { return profile.getString("height"); }

    public String get_gender() { return profile.getString("gender"); }
}
