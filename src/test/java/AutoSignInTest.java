import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.fail;

public class AutoSignInTest {
    private AppiumDriver driver;
    private WebDriverWait webDriverWait;
    private int time_out_seconds = 30;

    private String android_version =  "8.1.0";
    private String test_device_model = "Pixel2";

    //private String package_name = "com.instagram.android";
    //private String launchable_activity = "com.instagram.android.activity.MainTabActivity";

    private String package_name = "org.wordpress.android";
    private String launchable_activity = "org.wordpress.android.ui.WPLaunchActivity";

    private int time_delay_for_network = 5;

    @Before
    public void setup() throws MalformedURLException {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setCapability(MobileCapabilityType.PLATFORM_NAME,
                "Android");
        desiredCapabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION,
                android_version);
        desiredCapabilities.setCapability(MobileCapabilityType.FULL_RESET, false);
        desiredCapabilities.setCapability(MobileCapabilityType.DEVICE_NAME,
                test_device_model);
        desiredCapabilities.setCapability(AndroidMobileCapabilityType.APP_PACKAGE,
                package_name);
        desiredCapabilities.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY,
                launchable_activity);

        driver = new AndroidDriver(new URL("http://0.0.0.0:4723/wd/hub"), desiredCapabilities);
        webDriverWait = new WebDriverWait(driver, time_out_seconds);
    }

    @Test
    public void login_test() throws IOException, InterruptedException {
        // component 1, find login screen
        boolean found_login_screen = find_login_screen();
        if(!found_login_screen) {
            fail("Did not find login screen. Found no known gui elements");
        }


    }

    @Test
    public void android_signin_test() throws InterruptedException, IOException, GeneralSecurityException {
        boolean was_login_sucessful = false;
        AccountManager accountManager = new AccountManager();
        JsonArray accounts;

        get_element(".*log.in.*").click();

        accounts = accountManager.getAccounts();

        for(int i = 0; i < accounts.size(); i++) {
            JsonObject account = accounts.getJsonObject(i);

            login(account.getString("login"), account.getString("password"));
            TimeUnit.SECONDS.sleep(time_delay_for_network);

            // check if successful
            was_login_sucessful = check_if_login_successful();

            if (was_login_sucessful) {
                System.out.println("Login was successful!");
                return;
            }

            // drop the error msg
            driver.navigate().back();
        }

        get_element(".*sign.*up.*").click();

        create_account();
    }

    private boolean find_login_screen() {
        boolean found_login = false;

        // TODO: Add a counter to fail if loop is repeated more than x times to break endless looping?
        while(true) {
            // condition check
            List<WebElement> username_fields = get_elements(".*user.*name.*");
            List<WebElement> email_fields = get_elements(".*email.*");

            for(WebElement element : username_fields) {
                if(element.getTagName().equals("android.widget.EditText")) {
                    found_login = true;
                }
            }
            for(WebElement element : email_fields) {
                if(element.getTagName().equals("android.widget.EditText")) {
                    found_login = true;
                }
            }

            if(found_login) {
                break;
            }

            // Search through the screen to find elements
            WebElement login_button = get_element(".*log.*in.*");
            if(login_button != null) {
                login_button.click();
                continue;
            }

            WebElement next_button = get_element("next");
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

    private void login(String user_name, String password) {
        get_element(".*user.*name.*").sendKeys(user_name);
        get_element(".*password.*").sendKeys(password);
        get_element(".*log.*in.*").click();
    }

    private boolean check_if_login_successful() {
        WebElement error_message = null;

        try {
            error_message = ((AndroidDriver<?>) driver).findElementByAndroidUIAutomator(
                    "new UiSelector().textMatches(\"(?i).*incorrect.*\")");

            return false;
        }
        catch (NoSuchElementException exception) {
        }

        return true;
    }

    private void create_account() throws IOException, GeneralSecurityException {
        // load json profile
        ProfileInformationLoader profile = new ProfileInformationLoader();

        String phone_number = profile.get_phone_number();
        String full_name = profile.get_full_name();

        get_element(".*phone.*").sendKeys(phone_number);

        get_element(".*next.*").click();


        //  tell sms permission from Instagram to sod off
        try {
            ((AndroidDriver<?>) driver).findElementByAndroidUIAutomator(
                    "new UiSelector().textMatches(\"(?i).*allow.*messages.*\")");
            ((AndroidDriver<?>) driver).findElementByAndroidUIAutomator(
                    "new UiSelector().textMatches(\"(?i)deny\")").click();
        } catch (NoSuchElementException exception) {
        }

        try {
            TimeUnit.SECONDS.sleep(time_delay_for_network);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // check if app is asking for confirm code
        WebElement confirmation_code_field = null;
        try {
            confirmation_code_field = ((AndroidDriver<?>) driver).findElementByAndroidUIAutomator(
                    "new UiSelector().textMatches(\"(?i).*confirm.*code.*\")");
        } catch (NoSuchElementException exception) {
            System.out.println("Did not find confirm code field");
        }

        if (confirmation_code_field != null) {
            String confirm_code = get_confirmation_code();
            confirmation_code_field.sendKeys(confirm_code);

            get_element("next").click();
        }

        // full name
        get_element(".*name").sendKeys(full_name);

        // password
        get_element("password").sendKeys(profile.get_password());

        get_element("next").click();

        // Pick user name or go with default
        get_element("next").click();

    }

    private String get_confirmation_code() throws IOException, GeneralSecurityException {
        String text_message = get_confirmation_code_from_gmail();
        String confirm_code = "";
        Pattern code_pattern = Pattern.compile("(\\d+.\\d+)");

        Matcher code_matcher = code_pattern.matcher(text_message);

        if (code_matcher.find()) {
            confirm_code = code_matcher.group(1).replaceAll("\\s+", "");
        }

        System.out.printf("Confirmation Code: %s \n", confirm_code);
        return confirm_code;
    }

    private String get_confirmation_code_from_gmail() throws IOException, GeneralSecurityException {
        GmailAccess gmail = new GmailAccess();

        String message = gmail.get_content_of_latest_email();

        return message;
    }

    @Test
    public void account_manager_test() throws IOException {
        AccountManager account_manager = new AccountManager();
        String login = "test2";
        String password = "test_pass";

        account_manager.save_account(login, password, package_name);
    }

    private WebElement get_element(String element_regex) {
        WebElement desired_element = null;
        String ui_selector = String.format("new UiSelector().textMatches(\"(?i)%s\")", element_regex);

        try {
            desired_element = ((AndroidDriver<?>) driver).findElementByAndroidUIAutomator(
                    ui_selector);
        } catch (NoSuchElementException e) {
        }

        return desired_element;
    }

    private List<WebElement> get_elements(String element_regex) {
        List<WebElement> desired_elements;
        String ui_selector = String.format("new UiSelector().textMatches(\"(?i)%s\")", element_regex);

        desired_elements = (List<WebElement>) ((AndroidDriver<?>) driver).findElementsByAndroidUIAutomator(ui_selector);

        return desired_elements;
    }

}
