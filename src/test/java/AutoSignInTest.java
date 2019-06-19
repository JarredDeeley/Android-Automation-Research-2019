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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AutoSignInTest {
    private AppiumDriver driver;
    private WebDriverWait webDriverWait;
    private int time_out_seconds = 30;

    private String android_version =  "7.1.1";
    private String test_device_model = "Nexus6";

    private String package_name = "com.instagram.android";
    private String launchable_activity = "com.instagram.android.activity.MainTabActivity";

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
    public void android_signin_test() throws InterruptedException, IOException, GeneralSecurityException {
        boolean was_login_sucessful = false;

        ((AndroidDriver<?>) driver).findElementByAndroidUIAutomator(
                "new UiSelector().textMatches(\"(?i).*log.in.*\")").click();

        login();

        // check if successful
        was_login_sucessful = check_if_login_successful();

        if (was_login_sucessful) {
            System.out.println("Login was successful!");
            return;
        }

        // drop the error msg
        driver.navigate().back();

        // get back to main screen
        driver.navigate().back();

        ((AndroidDriver<?>) driver).findElementByAndroidUIAutomator(
                "new UiSelector().textMatches(\"(?i).*sign.*up.*\")").click();

        create_account();
    }

    private void login() {
        String user_name = "***REMOVED***";
        String password = "***REMOVED***";

        ((AndroidDriver<?>) driver).findElementByAndroidUIAutomator(
                "new UiSelector().textMatches(\"(?i).*user.*name.*\")").sendKeys(user_name);
        ((AndroidDriver<?>) driver).findElementByAndroidUIAutomator(
                "new UiSelector().textMatches(\"(?i).*password.*\")").sendKeys(password);
        ((AndroidDriver<?>) driver).findElementByAndroidUIAutomator(
                "new UiSelector().textMatches(\"(?i).*log.*in.*\")").click();
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
        String phone_number = "5052780459";

        ((AndroidDriver<?>) driver).findElementByAndroidUIAutomator(
                "new UiSelector().textMatches(\"(?i).*phone.*\")").sendKeys(phone_number);

        ((AndroidDriver<?>) driver).findElementByAndroidUIAutomator(
                "new UiSelector().textMatches(\"(?i).*next.*\")").click();

        // check if app is asking for confirm code
        WebElement confirmation_code = null;
        try {
            confirmation_code = ((AndroidDriver<?>) driver).findElementByAndroidUIAutomator(
                    "new UiSelector().textMatches(\"(?i).*incorrect.*\")");
        } catch (NoSuchElementException exception) {
        }

        if (confirmation_code != null) {
            System.out.println(get_confirmation_code());
        }

        // Go get code from message

        // enter confirm code edit box = "Confirmation Code" followed by next button
    }

    private String get_confirmation_code() throws IOException, GeneralSecurityException {
        String text_message = get_confirmation_code_from_gmail();
        Pattern code_pattern = Pattern.compile("(\\d.*\\d)");

        Matcher code_matcher = code_pattern.matcher(text_message);

        return code_matcher.group(1);
    }

    private String get_confirmation_code_from_gmail() throws IOException, GeneralSecurityException {
        GmailAccess gmail = new GmailAccess();

        String message = gmail.get_content_of_latest_email();

        return message;
    }

}
