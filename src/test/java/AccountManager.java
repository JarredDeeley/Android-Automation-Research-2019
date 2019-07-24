import javax.json.*;
import java.io.*;

public class AccountManager {
    String accounts_file_path = "accounts.json";
    boolean file_was_created = false;

    public AccountManager() throws IOException {
        File accounts_file = new File(accounts_file_path);

        if(!accounts_file.exists()) {
            accounts_file.createNewFile();
            file_was_created = true;
        }
    }

    public void save_account(String login, String password, String app_package) throws FileNotFoundException {
        JsonReader reader;
        JsonArray accounts;
        JsonArrayBuilder accounts_builder;

        // create new json object
        JsonObject account = Json.createObjectBuilder().add("login", login)
                .add("password", password)
                .add("app_package", app_package)
                .build();

        // get json array from file
        accounts_builder = Json.createArrayBuilder();
        if(!isFileEmpty(accounts_file_path)) {
            InputStream accounts_file_stream = new FileInputStream(accounts_file_path);
            reader = Json.createReader(accounts_file_stream);

            JsonArray previous_accounts = reader.readArray();

            reader.close();

            for(JsonValue previous_account : previous_accounts) {
                accounts_builder.add(previous_account);
            }

        }

        // append new json object to array
        accounts_builder.add(account);

        accounts = accounts_builder.build();

        // overwrite save array to file
        JsonWriter writer = Json.createWriter(new FileOutputStream((accounts_file_path)));
        writer.writeArray(accounts);
        writer.close();
    }

    public JsonArray getAccounts() throws FileNotFoundException {
        JsonReader reader;
        JsonArray accounts;

        if(!isFileEmpty(accounts_file_path)) {
            InputStream accounts_file_stream = new FileInputStream(accounts_file_path);
            reader = Json.createReader(accounts_file_stream);

            accounts = reader.readArray();
            reader.close();
        } else {
            accounts = Json.createArrayBuilder().build();
        }

        return accounts;
    }

    private boolean isFileEmpty(String file_to_be_checked) throws FileNotFoundException {
        File loaded_file = new File(file_to_be_checked);

        if(loaded_file.isFile()) {
            long size = loaded_file.length();

            if(size != 0) {
                return false;
            } else {
                return true;
            }
        }

        throw new FileNotFoundException();
    }
}
