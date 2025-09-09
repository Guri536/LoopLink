package org.asv.looplink.webDriver

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.openqa.selenium.By
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.time.Duration
import org.asv.looplink.errors.errorsLL
import org.jetbrains.skia.Image

data class successLog constructor(
    val success: Boolean,
    val message: String = "Success"
)

data class studentInfo constructor(val uid: String, val pass: String) {
    lateinit var fullName: String
    lateinit var currentSection: String
    lateinit var programCode: String
    lateinit var studentContact: String
    var studentEmail: String = "$uid@cuchd.in"
    lateinit var cGPA: String
}

class cuimsAPI constructor() {
    lateinit var uid: String
    lateinit var pass: String

    constructor(uid: String, pass: String) : this() {
        this.uid = uid
        this.pass = pass
        student = studentInfo(uid, pass)
//        chrmOptions.addArguments("--headless")
        try {
            driver = ChromeDriver(chrmOptions)
        } catch (e: Exception) {
            throw Exception("Internet Error")
            driver?.quit()
        }
    }

    var student: studentInfo? = null
    val chrmOptions = ChromeOptions()
    var driver: ChromeDriver? = null
    var wait: WebDriverWait = WebDriverWait(driver, Duration.ofSeconds(2))

    val baseURL = "https://students.cuchd.in/"

    val endPoints = mapOf<String, String>(
        "Attendance" to "frmStudentCourseWiseAttendanceSummary.aspx?type=etgkYfqBdH1fSfc255iYGw==",
        "Profile" to "frmStudentProfile.aspx",
        "Marks" to "result.aspx"
    )

    fun login(): successLog {
        try {
            driver!!.get(baseURL)
            wait.until {
                val field = driver!!.findElement(By.id("txtUserId")).sendKeys(student!!.uid)
            }
            wait.until {
                driver!!.findElement(By.id("btnNext")).click()
            }
            wait.until {
                driver!!.findElement(By.id("txtLoginPassword")).sendKeys(student!!.pass)
            }
        } catch (e: TimeoutException) {
            return successLog(false, errorsLL.timeout_error)
        }
        return successLog(true)
    }

    fun getCaptcha(): Pair<successLog, ImageBitmap?> {
        var captchaImg: File = File("Null")
        var retMap: ImageBitmap? = null
        try {
            wait.until {
                val imgField = driver!!.findElement(By.id("imgCaptcha"))
                captchaImg = imgField.getScreenshotAs(org.openqa.selenium.OutputType.FILE)
                retMap = Image.makeFromEncoded(captchaImg.readBytes()).toComposeImageBitmap()
            }
        } catch (e: TimeoutException) {
            return Pair(
                successLog(
                    false,
                    errorsLL.timeout_error
                ),
                retMap
            )
        } catch (e: Exception) {
            return Pair(
                successLog(
                    false,
                    errorsLL.captcha_error
                ),
                retMap
            )
        }
        return Pair(
            successLog(true),
            retMap
        )
    }

    fun endSession() {
        driver?.quit()
    }


}