import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoginAutomation {
    private AutomationUtils utils;
    private AppiumDriver driver;

    private static final Logger logger = LogManager.getLogger(LoginAutomation.class);

    public LoginAutomation(AppiumDriver driver) {
        this.driver = driver;
        utils = new AutomationUtils(driver);
    }

    public boolean find_login_screen() throws InterruptedException {
        while(true) {
            TimeUnit.SECONDS.sleep(1);

            if(is_at_login_screen()) {
                break;
            }

            // Search through the screen to find elements
            String[] elements_to_click = {"log in", "sign in", "next", " ok ", "yes", "got it"};

            if(utils.click_elements(elements_to_click)) {
                continue;
            }

            try {
                utils.get_single_clickable_element().click();
                logger.info("clicking lone clickable");
                continue;
            } catch (NullPointerException e) {
            }

            if(!utils.is_out_of_reattempts()) {
                continue;
            }

            return false;
        }

        logger.info("Found login screen");
        return true;
    }

    public boolean login() throws IOException, InterruptedException {
        // check for using another account to login (such as gmail)
        WebElement google_account_login = utils.get_element("google");

        if(google_account_login == null) {
            google_account_login = utils.get_element_with_resource_id("google");
        }

        if(google_account_login != null) {
            logger.info("clicking google element");
            google_account_login.click();

            TimeUnit.SECONDS.sleep(utils.get_time_delay_for_network());

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
                utils.get_element("@").click();

                utils.handle_google_radio_list();

                if(!is_login_incorrect()) {
                    logger.info("failed to login with google");
                    return false;
                }
            }

            TimeUnit.SECONDS.sleep(utils.get_time_delay_for_network());

            // google permissions
            utils.handle_google_permissions();

            TimeUnit.SECONDS.sleep(utils.get_time_delay_for_network());

            boolean reached_main_activity = get_to_main_activity();

            return reached_main_activity;
        }

        return false;
    }

    private boolean get_to_main_activity() {
        while(!utils.is_at_main_activity()) {
            String[] elements_to_click = {"skip", "continue"};

            if(utils.click_elements(elements_to_click)) {
                continue;
            }

            return false;
        }

        return true;
    }

    private boolean is_login_incorrect() {
        List<WebElement> errors = utils.get_elements("can't find");
        errors.addAll(utils.get_elements("doesn't match"));
        errors.addAll(utils.get_elements("incorrect"));

        if(errors.size() > 0) {
            return true;
        }

        return false;
    }

    private boolean is_at_login_screen() {
        List<WebElement> login_fields = utils.get_elements("user name");
        login_fields.addAll(utils.get_elements("email"));

        for(WebElement element : login_fields) {
            if(element.getAttribute("class").equals("android.widget.EditText")) {
                return true;
            }
        }

        if(utils.get_element("google") != null
                || utils.get_element_with_resource_id("google") != null) {
            return true;
        }

        return false;
    }
}
