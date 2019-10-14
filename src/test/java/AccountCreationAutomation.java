import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AccountCreationAutomation {
    private AutomationUtils utils;
    private AppiumDriver driver;

    private static final Logger logger = LogManager.getLogger(AccountCreationAutomation.class);

    private enum login_type {
        THIRD_PARTY, PHONE, EMAIL
    }

    public AccountCreationAutomation(AppiumDriver driver) {
        this.driver = driver;
        utils = new AutomationUtils(driver);
    }

    public boolean find_account_creation_screen() throws InterruptedException {
        boolean found_account_creation = false;
        boolean second_attempt = false;

        while (true) {
            // condition check and base case
            List<WebElement> creation_fields = utils.get_elements("google");

            for(WebElement element : creation_fields) {
                String class_name = element.getAttribute("class");

                logger.info("element: " + element.getText());
                logger.info("class name: " + class_name);

                if(class_name.equals("android.widget.EditText") || class_name.equals("android.widget.Button")) {
                    logger.info("Found account. Matching edittext or button");
                    found_account_creation = true;
                }

                if(element.getText().toLowerCase().contains("google")) {
                    logger.info("Found account. Matching 'google'");
                    found_account_creation = true;
                }
            }

            if(found_account_creation) {
                break;
            }

            WebElement signup_button = utils.get_element("sign up");
            try {
                signup_button.click();
                logger.info("clicking element with sign up text");
                continue;
            }
            catch (NullPointerException exception) {
            }

            try {
                utils.get_single_clickable_element().click();
                logger.info("Clicking lone clickable");
                continue;
            } catch (NullPointerException e) {
            }

            if(!second_attempt) {
                logger.info("Failed to find element. Attempting again after sleep on off chance app needs to load");
                second_attempt = true;
                TimeUnit.SECONDS.sleep(10);
                continue;
            }

            return false;
        }

        return found_account_creation;
    }

    public boolean create_account() throws FileNotFoundException, InterruptedException {
        boolean found_creation_method = false;
        login_type selected_login_type = login_type.THIRD_PARTY;
        ProfileInformationLoader profile = new ProfileInformationLoader();

        // login type
        if(utils.get_element("google") != null) {
            logger.info("Found google element");
            create_account_through_google(profile);
            selected_login_type = login_type.THIRD_PARTY;

            TimeUnit.SECONDS.sleep(utils.get_time_delay_for_network());
        } else {
            return false;
        }

        // confirmation
        switch (selected_login_type) {
            case THIRD_PARTY:
                logger.info("Third-party account method. No confirmation needed");
                break;
            case PHONE:
                break;
            case EMAIL:
                break;
        }

        //      These can be in any order
        // display name
        // password
        // profile info (full name)
        while(true) {
            // base case is when we found main activity
            if(utils.is_at_main_activity()) {
                return true;
            }

            TimeUnit.SECONDS.sleep(utils.get_time_delay_for_network());

            try {
                utils.get_element("next").click();
                logger.info("clicking next");
                continue;
            } catch(NullPointerException e) {
            }

            try {
                utils.get_element("continue").click();
                logger.info("clicking continue");
                continue;
            } catch (NullPointerException e) {
            }

            try {
                utils.get_element("skip").click();
                logger.info("clicking skip");
                continue;
            } catch (NullPointerException e) {
            }

            break;
        }

        return false;
    }

    private void create_account_through_google(ProfileInformationLoader profile) {
        WebElement google_button = utils.get_element("google");

        String google_email = profile.get_google_email();

        try {
            logger.info("Clicking google element");
            google_button.click();

            if(google_email.isEmpty()) {
                utils.get_element("@").click();
            } else {
                utils.get_element(google_email).click();
            }

            utils.handle_google_radio_list();

            if(utils.get_element("wants to access your Google account") != null) {
                utils.get_element("allow").click();
            }
        } catch (NullPointerException e) {
        }
    }
}
