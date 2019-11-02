import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.fail;

public class AutoSignInTest {
    private AppiumDriver driver;
    private WebDriverWait webDriverWait;
    private int time_out_seconds = 30;

    private AutomationUtils utils;

    private String android_version =  "8.1.0";
    private String test_device_model = "Pixel2";

    private String package_name = "";
    private String launchable_activity = "";

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
        desiredCapabilities.setCapability(AndroidMobileCapabilityType.AUTO_GRANT_PERMISSIONS, true);
        desiredCapabilities.setCapability("automationName", "uiautomator2");
        desiredCapabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, 0);

        driver = new AndroidDriver(new URL("http://0.0.0.0:4723/wd/hub"), desiredCapabilities);
        webDriverWait = new WebDriverWait(driver, time_out_seconds);

        utils = new AutomationUtils(driver);
    }

    @Test
    public void login_test() throws IOException, InterruptedException {
        LoginAutomation login_tools = new LoginAutomation(driver);

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
    public void account_creation_test() throws FileNotFoundException, InterruptedException {
        AccountCreationAutomation creation_tools = new AccountCreationAutomation(driver);

        boolean found_account_creation = creation_tools.find_account_creation_screen();
        if(!found_account_creation) {
            fail("Did not find account creation. Found no known gui elements");
        }

        boolean is_account_creation_success;
        is_account_creation_success = creation_tools.create_account();

        if(!is_account_creation_success) {
            fail("Failed to create new account");
        }
    }

    @Test
    public void account_manager_test() throws IOException {
        AccountManager account_manager = new AccountManager();
        String login = "***REMOVED***";
        String password = "test_pass";

        account_manager.save_account(login, password, package_name);
    }
}
