package org.asv.looplink.webDriver

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

object cuimsAPI {
    lateinit var uid: String
    lateinit var pass: String

    var student: studentInfo? = null
    val chrmOptions = ChromeOptions()
    var driver: ChromeDriver? = null


    var wait: WebDriverWait? = null

    val baseURL = "https://students.cuchd.in/"

    val endPoints = mapOf<String, String>(
        "Attendance" to "frmStudentCourseWiseAttendanceSummary.aspx?type=etgkYfqBdH1fSfc255iYGw==",
        "Profile" to "frmStudentProfile.aspx",
        "Marks" to "result.aspx"
    )

    suspend private fun initDriver() {
        if (driver == null) {
            withContext(Dispatchers.IO) {
                //        chrmOptions.addArguments("--headless")

                try {
                    driver = ChromeDriver(chrmOptions)
                    wait = WebDriverWait(driver!!, Duration.ofSeconds(2))
                } catch (e: Exception) {
                    driver?.quit()
                    throw Exception("Internet Error")
                }
            }

        }
    }

    suspend fun login(uid: String, pass: String): successLog {
        initDriver()
        this.uid = uid
        this.pass = pass
        student = studentInfo(uid, pass)

        return withContext(Dispatchers.IO)
        {
            try {
                driver!!.get(baseURL)
                wait!!.until {
                    val field = driver!!.findElement(By.id("txtUserId")).sendKeys(student!!.uid)
                }
                wait!!.until {
                    driver!!.findElement(By.id("btnNext")).click()
                }
                wait!!.until {
                    driver!!.findElement(By.id("txtLoginPassword")).sendKeys(student!!.pass)
                }
                successLog(true)
            } catch (e: TimeoutException) {
                successLog(false, errorsLL.timeout_error)
            }
        }
    }

    suspend fun getCaptcha(): Pair<successLog, ImageBitmap?> {
        var captchaImg: File = File("Null")
        var retMap: ImageBitmap? = null

        return withContext(Dispatchers.IO) {
            try {
                wait!!.until {
                    val imgField = driver!!.findElement(By.id("imgCaptcha"))
                    captchaImg = imgField.getScreenshotAs(org.openqa.selenium.OutputType.FILE)
                    retMap = Image.makeFromEncoded(captchaImg.readBytes()).toComposeImageBitmap()
                }
            } catch (e: TimeoutException) {
                return@withContext Pair(
                    successLog(
                        false,
                        errorsLL.timeout_error
                    ),
                    retMap
                )
            } catch (e: Exception) {
                return@withContext Pair(
                    successLog(
                        false,
                        errorsLL.captcha_error
                    ),
                    retMap
                )
            }
            return@withContext Pair(
                successLog(true),
                retMap
            )
        }
    }

    suspend fun fillCaptcha(captcha: String): successLog {
        return withContext(Dispatchers.IO) {
            try {
                wait!!.until {
                    driver!!.findElement(By.id("txtcaptcha")).sendKeys(captcha)
                    driver!!.findElement(By.id("btnLogin")).click()
                }
                var successLogO: successLog = successLog(true)
                wait!!.until {
                    if (driver!!.findElement(By.id("header")).isDisplayed) {
                        successLogO = successLog(true)
                    }
                    else if (driver!!.findElement(By.className("sweet-alert showSweetAlert visible")).isDisplayed) {
                        val errorMessage =
                            driver!!.findElement(By.className("sweet-alert showSweetAlert visible"))
                                .findElement(By.tagName("p")).text
                        successLogO = successLog(false, errorMessage)
                    }
                }
                return@withContext successLogO
            } catch (e: TimeoutException) {
                return@withContext successLog(false, errorsLL.timeout_error)
            } catch (e: Exception) {
                return@withContext successLog(false, errorsLL.captcha_error)
            }
        }
    }

    fun endSession() {
        driver?.quit()
    }

}