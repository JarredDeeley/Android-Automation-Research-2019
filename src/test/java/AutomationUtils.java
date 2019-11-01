import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class AutomationUtils {
    private AppiumDriver driver;
    private int time_delay_for_network = 7;
    private int reattempts_remaining = 2;
    private static final Logger logger = LogManager.getLogger(AutomationUtils.class);

    public AutomationUtils(AppiumDriver driver) {
        this.driver = driver;
    }

    public WebElement get_element(String search_string) {
        WebElement desired_element = null;

        String xpath_selector = String.format(
                "//*[contains(translate(@text, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), \"%s\")]",
                search_string);

        try {
            desired_element = driver.findElement(By.xpath(xpath_selector));
        } catch (NoSuchElementException e) {
            logger.trace("Did not find element: " + search_string);
        }

        logger.trace(desired_element);

        return desired_element;
    }

    public List<WebElement> get_elements(String search_string) {
        List<WebElement> desired_elements;
        String xpath_selector = String.format(
                "//*[contains(translate(@text, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), \"%s\")]",
                search_string);

        desired_elements = driver.findElements(By.xpath(xpath_selector));

        return desired_elements;
    }

    public WebElement get_single_clickable_element() {
        List<WebElement> clickable_elements;
        String ui_clickable_selector = "new UiSelector().clickable(true)";

        clickable_elements = (List<WebElement>)
                ((AndroidDriver<?>) driver).findElementsByAndroidUIAutomator(ui_clickable_selector);

        if(clickable_elements.size() == 1) {
            return clickable_elements.get(0);
        } else {
            return null;
        }
    }

    public void handle_google_radio_list() {
        if(this.get_element("@") != null) {
            try {
                this.get_element("ok").click();
            } catch (NullPointerException e) {
            }
        }
    }

    public int get_time_delay_for_network() {
        return time_delay_for_network;
    }

    public boolean is_at_main_activity() {
        String current_activity = ((AndroidDriver<MobileElement>) driver).currentActivity();
        current_activity = current_activity.toLowerCase();

        logger.info("Current activity: " + current_activity);

        if(current_activity.contains("profile") || current_activity.contains("main")
                || current_activity.contains("navigation") || current_activity.contains("landing")
                || current_activity.contains("searchformspager") || current_activity.contains("home")
                || current_activity.contains("sellersnearbyactivity") || current_activity.contains("searchactivity")
                || current_activity.contains("newtargetweightactivity") || current_activity.contains("dashboardactivity")) {
            return true;
        }

        return false;
    }

    public WebElement get_element_with_resource_id(String search_string) {
        WebElement desired_element = null;

        String xpath_selector = String.format(
                "//*[contains(translate(@resource-id, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), \"%s\")]",
                search_string);

        try {
            desired_element = driver.findElement(By.xpath(xpath_selector));
        } catch (NoSuchElementException e) {
            logger.trace("Did not find element: " + search_string);
        }

        logger.trace(desired_element);

        return desired_element;
    }

    public List<WebElement> get_elements_with_resource_id(String search_string) {
        List<WebElement> desired_elements;
        String xpath_selector = String.format(
                "//*[contains(translate(@resource-id, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), \"%s\")]",
                search_string);

        desired_elements = driver.findElements(By.xpath(xpath_selector));

        return desired_elements;
    }

    public boolean is_out_of_reattempts() throws InterruptedException {
        logger.info("Failed to find element. Attempting again after sleep on off chance app needs to load");
        reattempts_remaining -= 1;
        if(reattempts_remaining < 1) {
            return true;
        }
        TimeUnit.SECONDS.sleep(10);

        return false;
    }

    public boolean click_elements(String[] elements_to_click) {
        for(String search_string : elements_to_click) {
            try {
                get_element(search_string).click();
                logger.info("clicking " + search_string);

                return true;
            } catch (NullPointerException e) {
            }
        }

        return false;
    }

    public void handle_google_permissions() {
        if(get_element_with_ui_selector(".*wants to access your Google account.*") != null
                || get_element_with_ui_selector(".*would like to:.*") != null) {
            get_element("allow").click();
        }
    }

    private WebElement get_element_with_ui_selector(String search_regex) {
        WebElement desired_element = null;
        String ui_selector = String.format("new UiSelector().textMatches(\"(?i)%s\")", search_regex);

        try {
            desired_element = ((AndroidDriver<?>) driver).findElementByAndroidUIAutomator(ui_selector);
        } catch (NoSuchElementException e) {
            logger.trace("Did not find element: " + search_regex + " with uiSelector");
        }

        return desired_element;
    }
}
