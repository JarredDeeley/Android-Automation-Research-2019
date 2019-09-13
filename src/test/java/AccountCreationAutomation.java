import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;

import java.io.FileNotFoundException;
import java.util.List;

public class AccountCreationAutomation {
    private AutomationUtils utils;
    private AppiumDriver driver;

    private enum login_type {
        THIRD_PARTY, PHONE, EMAIL
    }

    public AccountCreationAutomation(AppiumDriver driver) {
        this.driver = driver;
        utils = new AutomationUtils(driver);
    }

    public boolean find_account_creation_screen() {
        boolean found_account_creation = false;

        while (true) {
            // condition check and base case
            List<WebElement> creation_fields = utils.get_elements(".*sign up with google.*");
            creation_fields.addAll(utils.get_elements("phone"));

            for(WebElement element : creation_fields) {
                String tag_name = element.getTagName();

                System.out.println("element: " + element.getText());
                System.out.println("tag name: " + tag_name);

                if(tag_name.equals("android.widget.EditText") || tag_name.equals("android.widget.Button")) {
                    found_account_creation = true;
                }
            }
            System.out.println();

            if(found_account_creation) {
                break;
            }

            WebElement signup_button = utils.get_element(".*sign.*up.*");
            try {
                signup_button.click();
                continue;
            }
            catch (NullPointerException exception) {
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
        // TODO add option to specify which gmail account to use (maybe pull from profile.json)
        if(utils.get_element(".*sign up with google.*") != null) {
            create_account_through_google();
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
            boolean is_account_created = false;
            String current_activity = ((AndroidDriver<MobileElement>) driver).currentActivity();
            current_activity = current_activity.toLowerCase();

            // if the string search is too slow then check out region matches
            if(current_activity.contains("profile") || current_activity.contains("main")) {
                return true;
            }

            try {
                utils.get_element("next").click();
                continue;
            } catch(NullPointerException e) {
            }
            try {
                utils.get_element("continue").click();
                continue;
            } catch (NullPointerException e) {
            }

            break;
        }

        return false;
    }

    private void create_account_through_google() {
        WebElement google_button = utils.get_element(".*sign up with google.*");
        try {
            google_button.click();

            utils.get_element(".*@.*").click();

            if(utils.get_element(".*wants to access your Google account.*") != null) {
                utils.get_element("allow").click();
            }
        } catch (NullPointerException e) {
        }
    }
}
