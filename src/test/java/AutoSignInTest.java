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
    private int time_delay_for_network = 3;

    private AutomationUtils utils;

    private String android_version =  "8.1.0";
    private String test_device_model = "Pixel2";

    //private String package_name = "com.instagram.android";
    //private String launchable_activity = "com.instagram.android.activity.MainTabActivity";

    private String package_name = "org.wordpress.android";
    private String launchable_activity = "org.wordpress.android.ui.WPLaunchActivity";

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

        utils = new AutomationUtils(driver);
    }

    @Test
    public void login_test() throws IOException, InterruptedException {
        LoginAutomation login_tools = new LoginAutomation(driver);

        // component 1, find login screen
        boolean found_login_screen = login_tools.find_login_screen();
        if(!found_login_screen) {
            fail("Did not find login screen. Found no known gui elements");
        }

        boolean is_login_success;
        is_login_success = login_tools.login();

        if(!is_login_success) {
            fail("Failed to login");
        }


    }

    @Test
    public void account_creation_test() {

    }

    @Test
    public void android_signin_test() throws InterruptedException, IOException, GeneralSecurityException {
        create_account();
    }


    private void create_account() throws IOException, GeneralSecurityException {
        // load json profile
        ProfileInformationLoader profile = new ProfileInformationLoader();

        String phone_number = profile.get_phone_number();
        String full_name = profile.get_full_name();

        utils.get_element(".*phone.*").sendKeys(phone_number);

        utils.get_element(".*next.*").click();


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

            utils.get_element("next").click();
        }

        // full name
        utils.get_element(".*name").sendKeys(full_name);

        // password
        utils.get_element("password").sendKeys(profile.get_password());

        utils.get_element("next").click();

        // Pick user name or go with default
        utils.get_element("next").click();

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
        String login = "***REMOVED***";
        String password = "test_pass";

        account_manager.save_account(login, password, package_name);
    }
}
