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
    }

    public String get_phone_number() {
        return profile.getString("phone_number");
    }

    public String get_first_name() {
        return profile.getString("first_name");
    }

    public String get_last_name() {
        return profile.getString("last_name");
    }

    public String get_full_name() {
        return get_first_name() + " " + get_last_name();
    }

    // Not a secure way of handling passwords
    public String get_password() {
        return profile.getString("password");
    }
}
