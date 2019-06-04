import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.android.AndroidKeyCode;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;
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
    public void android_signin_test() {
        Pattern existing_login_pattern = Pattern.compile("log.in", Pattern.CASE_INSENSITIVE);
        //Find sign in text field or button

        //attempt sign-in
        //String pageSource = driver.getPageSource();

        // appium might have methods to get view elements rather than string mangling
        // Find app to simulate phone number
        List<WebElement> elements = driver.findElementsByXPath("//*");

        for(WebElement webElement : elements) {
            //System.out.println(webElement.getTagName() + "\t text: " + webElement.getText());

            Matcher matcher = existing_login_pattern.matcher(webElement.getText());
            if (matcher.find()) {
                System.out.println("Found match!");
                webElement.click();
                break;
            }
        }

        login();

        


    }

    private void login() {
        String user_name = "***REMOVED***";
        String password = "***REMOVED***";
        boolean entered_username = false;

//        List<WebElement> login_page_elements = driver.findElementsByXPath("//*");
//
//        System.out.println("Number of elments: " + login_page_elements.size());
//
//        for (WebElement login_element : login_page_elements) {
//            System.out.println(login_element.getTagName() + "\t text: " + login_element.getText());
//            if (login_element.getTagName().equals("android.widget.EditText")) {
//                //System.out.println("\t TEXT: " + login_element.getText());
//
//                if (!entered_username) {
//                    System.out.println("Hi USER_NAME");
//                    login_element.sendKeys(user_name);
//                    entered_username = true;
//                }
//                else if(entered_username) {
//
//                    System.out.println("hi PASSWORD");
//                    login_element.sendKeys(password);
//
//                    //login_element.sendKeys(Keys.ENTER);
//                    System.out.println("End of entering password");
//                    break;
//                }
//
//            }
//        }

        driver.findElementByXPath("//*[contains(@text, \'username\')]").sendKeys(user_name);

        driver.findElementByXPath("//*[contains(@text, \'Password\')]").sendKeys("hithere");
        driver.findElementByXPath("//*[contains(@text, \'Log In\')]").click();

    }

}
