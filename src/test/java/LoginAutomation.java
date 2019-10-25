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
        boolean found_login = false;
        boolean is_out_of_reattempts = false;
        int reattempts_remaining = 2;

        while(true) {
            TimeUnit.SECONDS.sleep(1);
            // condition check

            List<WebElement> login_fields = utils.get_elements("user name");
            login_fields.addAll(utils.get_elements("email"));

            for(WebElement element : login_fields) {
                if(element.getAttribute("class").equals("android.widget.EditText")) {
                    found_login = true;
                }
            }

            if(utils.get_element("google") != null
                    || utils.get_element_with_resource_id("google") != null) {
                found_login = true;
            }

            if(found_login) {
                break;
            }

            // Search through the screen to find elements
            try {
                utils.get_element("log in").click();
                logger.info("clicking log in");
                continue;
            }
            catch (NullPointerException e) {
            }

            try {
                utils.get_element("sign in").click();
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
                utils.get_element(" ok ").click();
                logger.info("clicking ok");
                continue;
            } catch (NullPointerException e){
            }

            try {
                utils.get_element("yes").click();
                logger.info("clicking yes");
                continue;
            } catch (NullPointerException e){
            }

            if(utils.get_element("google smart lock") != null) {
                utils.get_element("none of the above").click();
                continue;
            }

            try {
                utils.get_element("got it").click();
                logger.info("clicking got it");

                continue;
            } catch (NullPointerException e){
            }

            try {
                utils.get_single_clickable_element().click();
                logger.info("clicking lone clickable");
                continue;
            } catch (NullPointerException e) {
            }

            if(!is_out_of_reattempts) {
                logger.info("Attempting again in case of app loading on start");
                reattempts_remaining -= 1;
                if(reattempts_remaining < 1) {
                    is_out_of_reattempts = true;
                }
                TimeUnit.SECONDS.sleep(10);
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
            if(utils.get_element("wants to access your Google account") != null
               || utils.get_element("would like to:") != null) {
                utils.get_element("allow").click();
            }

            TimeUnit.SECONDS.sleep(utils.get_time_delay_for_network());

            boolean reached_main_activity = get_to_main_activity();

            return reached_main_activity;
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

            try {
                utils.get_element("continue").click();
                continue;
            } catch (NullPointerException e) {
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
}
