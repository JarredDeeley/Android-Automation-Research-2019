import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
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
            List<WebElement> creation_fields = utils.get_elements(".*google.*");
            // creation_fields.addAll(utils.get_elements("phone"));

            for(WebElement element : creation_fields) {
                String tag_name = element.getTagName();

                // System.out.println("element: " + element.getText());
                // System.out.println("tag name: " + tag_name);
                logger.info("element: " + element.getText());
                logger.info("tag name: " + tag_name);

                if(tag_name.equals("android.widget.EditText") || tag_name.equals("android.widget.Button")) {
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

            WebElement signup_button = utils.get_element(".*sign.up.*");
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

    public boolean create_account() throws FileNotFoundException {
        boolean found_creation_method = false;
        login_type selected_login_type = login_type.THIRD_PARTY;
        ProfileInformationLoader profile = new ProfileInformationLoader();

        // login type
        if(utils.get_element(".*google.*") != null) {
            logger.info("Found google element");
            create_account_through_google(profile);
            selected_login_type = login_type.THIRD_PARTY;
        } else if(utils.get_element("Phone") != null) {
            WebElement phone_field = utils.get_element("Phone");
            if(phone_field.getTagName().equals("android.widget.EditText")) {
                selected_login_type = login_type.PHONE;
                phone_field.sendKeys(profile.get_phone_number());

                try {
                    utils.get_element("next").click();
                } catch (NullPointerException e) {
                }

                // dismiss sms permission
                if(utils.get_element(".*allow.*to send and view SMS messages.*") != null) {
                    utils.get_element(".*deny.*").click();
                }
            }
        } else {
            return false;
        }

        // confirmation
        switch (selected_login_type) {
            case THIRD_PARTY:
                logger.info("Third-party account method. No confirmation needed");
                break;
            case PHONE:
                // go get code from google voice
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
            String current_activity = ((AndroidDriver<MobileElement>) driver).currentActivity();
            current_activity = current_activity.toLowerCase();

            logger.info("Current activity: " + current_activity);

            // if the string search is too slow then check out region matches
            if(current_activity.contains("profile") || current_activity.contains("main")
             || current_activity.contains("navigation") || current_activity.contains("landing")
            || current_activity.contains("searchformspager")) {
                return true;
            }

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

            break;
        }

        return false;
    }

    private void create_account_through_google(ProfileInformationLoader profile) {
        WebElement google_button = utils.get_element(".*google.*");

        String google_email = profile.get_google_email();

        try {
            logger.info("Clicking google element");
            google_button.click();

            if(google_email.isEmpty()) {
                utils.get_element(".*@.*").click();
            } else {
                utils.get_element(google_email).click();
            }

            utils.handle_google_radio_list();

            if(utils.get_element(".*wants to access your Google account.*") != null) {
                utils.get_element("allow").click();
            }
        } catch (NullPointerException e) {
        }
    }
}
