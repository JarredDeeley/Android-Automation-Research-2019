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

    public AccountCreationAutomation(AppiumDriver driver) {
        this.driver = driver;
        utils = new AutomationUtils(driver);
    }

    public boolean find_account_creation_screen() throws InterruptedException {
        while (true) {
            TimeUnit.SECONDS.sleep(1);

            // condition check and base case
            if(is_at_account_creation_screen()) {
                break;
            }

            try {
                List<WebElement> sign_up_elementsutils = utils.get_elements("sign up");

                for(WebElement element: sign_up_elementsutils) {
                    logger.trace("sign up element's class: " + element.getAttribute("class"));

                    if(element.getAttribute("class").equalsIgnoreCase("android.widget.Button")) {
                        element.click();
                        break;
                    }
                }
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

            String[] elements_to_click = {"got it"};
            if(utils.click_elements(elements_to_click)) {
                continue;
            }

            if(!utils.is_out_of_reattempts()) {
                continue;
            }

            return false;
        }

        return true;
    }

    private boolean is_at_account_creation_screen() {
        List<WebElement> creation_fields = utils.get_elements("google");

        for(WebElement element : creation_fields) {
            String class_name = element.getAttribute("class");

            logger.info("element: " + element.getText());
            logger.info("class name: " + class_name);

            if(class_name.equals("android.widget.EditText") || class_name.equals("android.widget.Button")) {
                logger.info("Found account. Matching edittext or button");
                return true;
            }

            if(element.getText().toLowerCase().contains("google")) {
                logger.info("Found account. Matching 'google'");
                return true;
            }
        }

        return false;
    }

    public boolean create_account() throws FileNotFoundException, InterruptedException {
        ProfileInformationLoader profile = new ProfileInformationLoader();

        // login type
        if(utils.get_element("google") != null) {
            logger.info("Found google element");
            create_account_through_google(profile);

            TimeUnit.SECONDS.sleep(utils.get_time_delay_for_network());
        } else {
            return false;
        }

        while(true) {
            // base case is when we found main activity
            if(utils.is_at_main_activity()) {
                return true;
            }

            TimeUnit.SECONDS.sleep(utils.get_time_delay_for_network());

            // fill in profile info
            if(utils.get_element("basic info") != null) {
                fill_in_profile_info(profile);
            }

            String[] elements_to_click = {"next", "continue", "skip"};
            if(utils.click_elements(elements_to_click)) {
                continue;
            }

            break;
        }

        return false;
    }

    private void create_account_through_google(ProfileInformationLoader profile) throws InterruptedException {
        WebElement google_button = utils.get_element("google");

        String google_email = profile.get_google_email();

        try {
            logger.info("Clicking google element");
            google_button.click();
            TimeUnit.SECONDS.sleep(utils.get_time_delay_for_network());

            if(google_email.isEmpty()) {
                utils.get_element("@").click();
            } else {
                utils.get_element(google_email).click();
            }

            utils.handle_google_radio_list();

            TimeUnit.SECONDS.sleep(utils.get_time_delay_for_network());

            if(utils.get_element("wants to access") != null) {
                logger.trace("clicking allow for google permissions");
                utils.get_element("allow").click();
            }
        } catch (NullPointerException e) {
        }
    }

    private void fill_in_profile_info(ProfileInformationLoader profile) {
        for(WebElement element : utils.get_elements_with_resource_id("age")) {
            if(element.getAttribute("class").equalsIgnoreCase("android.widget.EditText")) {
                element.sendKeys(profile.get_age());
                break;
            }
        }

        for(WebElement element : utils.get_elements_with_resource_id("weight")) {
            if(element.getAttribute("class").equalsIgnoreCase("android.widget.EditText")) {
                element.sendKeys(profile.get_weight_kg());
                break;
            }
        }

        for(WebElement element : utils.get_elements_with_resource_id("feet")) {
            if(element.getAttribute("class").equalsIgnoreCase("android.widget.EditText")) {
                element.sendKeys(profile.get_height());
                break;
            }
        }

        if(utils.get_element_with_resource_id("gender") != null) {
            utils.get_element_with_resource_id(profile.get_gender()).click();
        }

        WebElement daily_activity = utils.get_element_with_resource_id("tv_lifestyle");
        if(daily_activity != null) {
            daily_activity.click();
            utils.get_element("less than").click();
        }

    }
}
