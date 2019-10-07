import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;

public class AutomationUtils {
    private AppiumDriver driver;

    public AutomationUtils(AppiumDriver driver) {
        this.driver = driver;
    }

    public WebElement get_element(String element_regex) {
        WebElement desired_element = null;
        String ui_selector = String.format("new UiSelector().textMatches(\"(?i)%s\")", element_regex);

        try {
            desired_element = ((AndroidDriver<?>) driver).findElementByAndroidUIAutomator(
                    ui_selector);
        } catch (NoSuchElementException e) {
        }

        return desired_element;
    }

    public List<WebElement> get_elements(String element_regex) {
        List<WebElement> desired_elements;
        String ui_selector = String.format("new UiSelector().textMatches(\"(?i)%s\")", element_regex);

        desired_elements = (List<WebElement>) ((AndroidDriver<?>) driver).findElementsByAndroidUIAutomator(ui_selector);

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
}
