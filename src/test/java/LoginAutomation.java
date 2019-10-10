import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger logger = LogManager.getLogger(LoginAutomation.class);

    public LoginAutomation(AppiumDriver driver) {
        this.driver = driver;
        utils = new AutomationUtils(driver);
    }

    public boolean find_login_screen() throws InterruptedException {
        boolean found_login = false;
        boolean second_attempt = false;

        while(true) {
            // condition check
            List<WebElement> login_fields = utils.get_elements(".*user.*name.*");
            login_fields.addAll(utils.get_elements(".*email.*"));

            for(WebElement element : login_fields) {
                if(element.getTagName().equals("android.widget.EditText")) {
                    found_login = true;
                }
            }

            if(utils.get_element(".*google.*") != null) {
                found_login = true;
            }

            if(found_login) {
                break;
            }

            // Search through the screen to find elements
            try {
                utils.get_element(".*log.*in.*").click();
                logger.info("clicking log in");
                continue;
            }
            catch (NullPointerException e) {
            }

            try {
                utils.get_element(".*sign.in.*").click();
                logger.info("clicking sign in");
                continue;
            }
            catch (NullPointerException e) {
            }

            try {
                utils.get_element("next").click();
                logger.info("clicking next");
                continue;
            }
            catch (NullPointerException e) {
            }

            try {
                utils.get_element(".*[^a-z]ok[^a-z].*").click();
                logger.info("clicking ok");
                continue;
            } catch (NullPointerException e){
            }

            try {
                utils.get_element(".*yes.*").click();
                logger.info("clicking yes");
                continue;
            } catch (NullPointerException e){
            }

            if(utils.get_element(".*google smart lock.*") != null) {
                utils.get_element("none of the above");
                continue;
            }

            try {
                utils.get_element(".*got it.*").click();
                logger.info("clicking got it");

                /*TimeUnit.SECONDS.sleep(3);
                logger.info("slept");
                List<WebElement> tmp_elements = utils.get_elements(".*");

                for(WebElement element: tmp_elements) {
                    logger.info(element.getTagName() + "  text: " + element.getText());
                }
                logger.info("end of listing out elements");*/

                continue;
            } catch (NullPointerException e){
            }

            try {
                utils.get_single_clickable_element().click();
                logger.info("clicking lone clickable");
                continue;
            } catch (NullPointerException e) {
            }

            if(!second_attempt) {
                logger.info("Attempting again in case of app loading on start");
                second_attempt = true;
                TimeUnit.SECONDS.sleep(10);
                continue;
            }

            return false;
        }

        logger.info("Found login screen");
        return true;
    }

    public boolean login() throws IOException, InterruptedException {
        AccountManager accountManager = new AccountManager();
        JsonArray accounts;
        boolean is_password_on_same_screen;

        accounts = accountManager.getAccounts();

        // check for using another account to login (such as gmail)
        WebElement google_account_login = utils.get_element(".*google.*");
        if(google_account_login != null) {
            logger.info("clicking google element");
            google_account_login.click();

            // assuming desired google account is already on the list
            ProfileInformationLoader profile = new ProfileInformationLoader();
            String google_email = profile.get_google_email();

            try{
                utils.get_element(google_email).click();
                logger.info("selected provided profile email");
                utils.handle_google_radio_list();
            }catch (NullPointerException e){}

            if(google_email.isEmpty() || is_login_incorrect()) {
                logger.info("unable to use email from profile. Attempting any email listed");
                utils.get_element(".*@.*").click();

                utils.handle_google_radio_list();

                if(!is_login_incorrect()) {
                    logger.info("failed to login with google");
                    return false;
                }
            }

            // google permissions
            if(utils.get_element(".*wants to access your Google account.*") != null
               || utils.get_element(".*would like to:") != null) {
                utils.get_element("allow").click();
            }

            boolean reached_main_activity = get_to_main_activity();

            return reached_main_activity;
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

    private boolean get_to_main_activity() {
        while(!utils.is_at_main_activity()) {
            try {
                utils.get_element("skip").click();
                continue;
            } catch (NullPointerException e) {
            }

            return false;
        }

        return true;
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
