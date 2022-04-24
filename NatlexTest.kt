import android.graphics.BitmapFactory
import android.graphics.Color
import android.system.Os.close
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.kaspersky.components.kautomator.component.common.views.UiView
import com.kaspersky.components.kautomator.component.edit.UiEditText
import com.kaspersky.components.kautomator.component.switch.UiSwitch
import com.kaspersky.components.kautomator.component.text.UiButton
import com.kaspersky.components.kautomator.component.text.UiTextView
import com.kaspersky.components.kautomator.screen.UiScreen
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.io.File


class NatlexTest : TestCase(
    kaspressoBuilder = Kaspresso.Builder.simple( // simple/advanced - it doesn't matter
        customize = {
            // storage support for Android API 30+
            UiDevice
                .getInstance(instrumentation)
                .executeShellCommand("appops set --uid ${InstrumentationRegistry.getInstrumentation().targetContext.packageName} MANAGE_EXTERNAL_STORAGE allow")
        }
    )
) {
    val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    object NatlextestScreen : UiScreen<NatlextestScreen>(){
        override val packageName: String = "com.example.natlextest"
        val search = UiButton {withId("android", "search_bar")}
        val searchtext = UiEditText {withId("android", "search_src_text")}
        val weather = UiView {withId( this@NatlextestScreen.packageName,"weather_main_window")}
        val searchedCity = UiTextView {withId( this@NatlextestScreen.packageName,"tv_city_name")}
        var searchedTemp = UiTextView {withId( this@NatlextestScreen.packageName,"tv_temp")}
        val switch = UiSwitch {withId( this@NatlextestScreen.packageName,"switch_ftoc")}
    }

    @Test
    fun natlextest() = before {
        //open app
        mDevice.pressHome()
        mDevice.swipe(10,1000,10,10,30) //open app menu
        val allAppsButton: UiObject = mDevice.findObject(UiSelector().description("NatlexTest"))
        allAppsButton.clickAndWaitForNewWindow()
    }.after {
        //mDevice.pressHome()
    }.run {
        NatlexTest.NatlextestScreen {
            val city = "Moscow"

            step("1. Find a city") {
                search.click()
                searchtext.replaceText(city)
                mDevice.pressEnter()
            }
            step("2. Check that the city appears in the main card with name, temperature and color") {
                weather.isDisplayed()
                searchedCity.hasText(city)
                searchedTemp.hasAnyText()
                switch.setChecked(true) //turn on C
                
                val path = "/sdcard/filename.png"
                mDevice.takeScreenshot(File(path))
                val temttxt: UiObject2 = mDevice.findObject(By.res("com.example.natlextest", "tv_temp"
                val bitmap = BitmapFactory.decodeFile(path)
                val color = bitmap.getColor(1000,300)
                
                if(temttxt.text.toInt() < 10) {
                    // Light Blue RGB color 41 182 246
                    val coldWeather = Color.valueOf(0.16078432F, 0.7137255F, 0.9647059F, 1.0F)
                    assertEquals(color,coldWeather)
                }
                else
                    if(temttxt.text.toInt() > 25) {
                        // Red RGB color 239 83 80
                        val hotWeather = Color.valueOf(0.9372549F, 0.3254902F, 0.3137255F, 1.0F)
                        assertEquals(color,hotWeather)
                    }
                    else {
                        // Orange RGB color 251 192 45
                        val normalWeather = Color.valueOf(0.9843137F, 0.7529412F, 0.1764706F, 1.0F)
                        assertEquals(color,normalWeather)
                    }
            }
            step("3. Check that the searched city appears in the city list") {
                val cityListCollection = UiScrollable (UiSelector().className("androidx.recyclerview.widget.RecyclerView"))
                val temttxt: UiObject2 = mDevice.findObject(By.res("com.example.natlextest", "tv_temp"))
                val findedCity = cityListCollection.getChildByText(UiSelector().className("android.widget.TextView"),city+", "+temttxt.text+" C", true) //with scrolling
                findedCity.isEnabled()
            }
        }
    }
}
