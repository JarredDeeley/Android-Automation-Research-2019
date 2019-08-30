import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoginAutomation {
    private int time_delay_for_network = 3;
    private AutomationUtils utils;
    private AppiumDriver driver;

    public LoginAutomation(AppiumDriver driver) {
        this.driver = driver;
        utils = new AutomationUtils(driver);
    }

    public boolean find_login_screen() {
        boolean found_login = false;

        // TODO: Add a counter to fail if loop is repeated more than x times to break endless looping?
        while(true) {
            // condition check
            List<WebElement> login_fields = utils.get_elements(".*user.*name.*");
            login_fields.addAll(utils.get_elements(".*email.*"));

            for(WebElement element : login_fields) {
                if(element.getTagName().equals("android.widget.EditText")) {
                    found_login = true;
                }
            }

            if(found_login) {
                break;
            }

            // Search through the screen to find elements
            WebElement login_button = utils.get_element(".*log.*in.*");
            if(login_button != null) {
                login_button.click();
                continue;
            }

            WebElement next_button = utils.get_element("next");
            if(next_button != null) {
                next_button.click();
                continue;
            }

            // TODO: How to handle this case of not finding a suitable element?
            // new exception class?
            // junit fail? Would be a problem if tool ran not as a test
            return false;
        }

        System.out.println("Found login screen");
        return true;
    }

    public boolean login() throws IOException, InterruptedException {
        AccountManager accountManager = new AccountManager();
        JsonArray accounts;
        boolean is_password_on_same_screen;

        accounts = accountManager.getAccounts();

        // check for using another account to login (such as gmail)
        WebElement google_account_login = utils.get_element("log in.*google.*");
        if(google_account_login != null) {
            google_account_login.click();

            /*List<WebElement> elements = utils.get_elements(".*");

            for(WebElement element : elements) {
                System.out.println('\n' + element.getText());
                System.out.println(element.getTagName());
            }*/

            // assuming desired google account is already on the list
            utils.get_element(".*@.*").click();

            if(is_login_incorrect()) {
                return false;
            }

            return true;
        }

        if(accounts.size() == 0) {
            System.out.println("No saved accounts found");
            return false;
        }

        // start looping through account list
        for(int i = 0; i < accounts.size(); i++) {
            JsonObject account = accounts.getJsonObject(i);

            // wordpress edge case. prompt on selecting email field
            close_sign_in_prompt();

            enter_username(account.getString("login"));

            // check if pass is on same screen. if not, press next button then error check
            is_password_on_same_screen = is_password_on_screen();
            if(!is_password_on_same_screen) {
                // press next button to get to password field
                utils.get_element(".*next.*").click();

                TimeUnit.SECONDS.sleep(time_delay_for_network);

                // error check to see if error with username
                if(is_login_incorrect()) {
                    continue;
                }
            }

            enter_password(account.getString("password"));

            List<WebElement> login_elements = utils.get_elements("next");
            login_elements.addAll(utils.get_elements("log.*in"));

            login_elements.get(0).click();

            TimeUnit.SECONDS.sleep(time_delay_for_network);

            if(is_login_incorrect()) {
                continue;
            }

            return true;
        }

        return false;
    }

    private void close_sign_in_prompt() {
        List<WebElement> username_fields = utils.get_elements(".*user.*name.*");
        username_fields.addAll(utils.get_elements(".*email.*"));

        for(WebElement element : username_fields) {
            if(element.getTagName().equals("android.widget.EditText")) {
                element.click();
                break;
            }
        }

        WebElement prompt = utils.get_element(".*continue.*with");
        if(prompt != null) {
            driver.navigate().back();
        }
    }

    private void enter_username(String username) {
        List<WebElement> username_fields = utils.get_elements(".*user.*name.*");
        username_fields.addAll(utils.get_elements(".*email.*"));

        for(WebElement element : username_fields) {
            if(element.getTagName().equals("android.widget.EditText")) {
                System.out.println("Enter username: " + username);
                element.sendKeys(username);
                break;
            }
        }
    }

    private boolean is_password_on_screen() {
        List<WebElement> password_fields = utils.get_elements(".*password.*");

        for(WebElement element : password_fields) {
            if(element.getTagName().equals("android.widget.EditText")) {
                return true;
            }
        }

        return false;
    }

    private boolean is_login_incorrect() {
        List<WebElement> errors = utils.get_elements(".*can't find.*");
        errors.addAll(utils.get_elements(".*doesn't match.*"));
        errors.addAll(utils.get_elements(".*incorrect.*"));

        if(errors.size() > 0) {
            return true;
        }

        return false;
    }

    private void find_password_field() {

    }

    private void enter_password(String password) {
        List<WebElement> password_fields = utils.get_elements(".*password.*");

        for(WebElement element : password_fields) {
            if(element.getTagName().equals("android.widget.EditText")) {
                element.sendKeys(password);
                break;
            }
        }
    }
}
