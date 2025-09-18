package org.asv.looplink.webDriver

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.asv.looplink.errors.errorsLL
import org.jetbrains.skia.Image
import org.jsoup.Jsoup
import org.openqa.selenium.By
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Duration
import java.util.Base64
import javax.imageio.ImageIO

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    return Image.makeFromEncoded(this).toComposeImageBitmap()
}

actual class cuimsAPI {
    actual var uid: String? = null
    actual var pass: String? = null

    actual var student: studentInfo? = null
    val chrmOptions = ChromeOptions()
    var driver: ChromeDriver? = null


    var wait: WebDriverWait? = null

    actual val BASEURL = "https://students.cuchd.in/"

    actual val endPoints = mapOf(
        "Attendance" to "frmStudentCourseWiseAttendanceSummary.aspx?type=etgkYfqBdH1fSfc255iYGw==",
        "Profile" to "frmStudentProfile.aspx",
        "Marks" to "result.aspx"
    )

    actual suspend fun initDriver() {
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

    actual suspend fun login(uid: String, pass: String): successLog {
        initDriver()
        this.uid = uid
        this.pass = pass
        student = studentInfo(uid, pass)

        return withContext(Dispatchers.IO)
        {
            try {
                driver!!.get(BASEURL)
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

    actual suspend fun getCaptcha(): Pair<successLog, ImageBitmap?> {
        var captchaImg: File = File("Null")
        var retMap: ImageBitmap? = null

        return withContext(Dispatchers.IO) {
            try {
                wait!!.until {
                    val imgField = driver!!.findElement(By.id("imgCaptcha"))
                    captchaImg = imgField.getScreenshotAs(org.openqa.selenium.OutputType.FILE)
                    retMap = captchaImg.readBytes().toImageBitmap()
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

    actual suspend fun processCaptcha(imgBase64: String): String {
        var captcha: String? = null
        val base64Img = "data:image/png;base64,$imgBase64"


        val ocrURL = "https://api.ocr.space/parse/image"
        val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        val ocrData = mutableMapOf(
            "apikey" to System.getenv("ocrSpaceAPIKEY"),
            "language" to "eng",
            "base64Image" to base64Img,
            "isOverlayRequired" to "True"
        )

        return withContext(Dispatchers.Main) {
            while (captcha == null || captcha == "" || captcha == " " || !captcha.all { it.isLetterOrDigit() }) {
                val ocrRes = Jsoup.connect(ocrURL)
                    .userAgent(USER_AGENT)
                    .data(ocrData)
                    .ignoreContentType(true)
                    .timeout(50000)
                    .post()
                val jsonOcrRes = (ocrRes.body().text())

                captcha =
                    jsonOcrRes.substringAfter("ParsedText\":\"").substringBefore("\"")
                        .substringBefore("\\")
                ocrData["OCREngine"] = "2"
                println(jsonOcrRes)
                println(captcha)
            }
            return@withContext captcha
        }
    }

    actual suspend fun fillCaptcha(captcha: String): successLog {
        return withContext(Dispatchers.IO) {
            try {
                wait!!.until {
                    driver!!.findElement(By.id("txtcaptcha")).clear()
                    driver!!.findElement(By.id("txtcaptcha")).sendKeys(captcha)
                    driver!!.findElement(By.id("btnLogin")).click()
                }

                val successLocator = By.id("header")
                val errorLocator = By.cssSelector(".sweet-alert.showSweetAlert.visible")

                wait!!.until(
                    ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(successLocator),
                        ExpectedConditions.visibilityOfElementLocated(errorLocator)
                    )
                )

                val errElements = driver!!.findElements(errorLocator)
                if (errElements.isNotEmpty()) {
                    val errorMessage = errElements.first().findElement(By.tagName("p")).text
                    errElements.first().findElements(By.tagName("button")).last().click()
                    driver!!.findElement(By.id("txtLoginPassword")).sendKeys(student!!.pass)
                    return@withContext successLog(false, errorMessage)
                } else {
                    return@withContext successLog(true)
                }

            } catch (e: TimeoutException) {
                return@withContext successLog(false, errorsLL.timeout_error)
            } catch (e: Exception) {
                return@withContext successLog(false, errorsLL.captcha_error)
            }
        }
    }

    actual fun endSession() {
        driver?.quit()
        driver = null
    }

    actual suspend fun autoFillCaptcha(): successLog {
        return withContext(Dispatchers.IO) {
            val imageBitmap = getCaptcha()
            if (imageBitmap.second == null) return@withContext successLog(false, "Unable to fill captcha")
            val captcha = processCaptcha(imageBitmap.second!!.toBase64())
            fillCaptcha(captcha)
            return@withContext successLog(true)
        }
    }

    actual fun getWebView(): Any {
        TODO("Not yet implemented")
    }

    suspend fun loadProfile() {
        withContext(Dispatchers.IO) {
            driver!!.get(BASEURL + endPoints["Profile"])

            val eleList = mapOf<String, By>(
                "UID" to By.id("lbstuUID"),
                "Name" to By.id("ContentPlaceHolder1_lblName"),
                "Section" to By.id("ContentPlaceHolder1_lblCurrentSection"),
                "Program" to By.id("ContentPlaceHolder1_lblProgramCode"),
                "Contact" to By.id("ContentPlaceHolder1_gvStudentContacts_lblMobile_2"),
            )
            for (i in eleList) {
                try {
                    val ele = wait!!.until {
                        driver!!.findElement(i.value)
                    }
                    when (i.key) {
                        "UID" -> student!!.studentUID = ele.text
                        "Name" -> student!!.fullName = ele.text
                        "Section" -> student!!.currentSection = ele.text
                        "Program" -> student!!.programCode = ele.text
                        "Contact" -> student!!.studentContact = ele.text
                        else -> {}
                    }
                } catch (e: Exception) {
                    println(e)
                }
            }
        }
    }

    suspend fun loadResults() {
        withContext(Dispatchers.IO) {
            driver!!.get(BASEURL + endPoints["Marks"])
            val eleList = mapOf<String, By>(
                "CGPA" to By.id("ContentPlaceHolder1_wucResult1_lblCGPA"),
            )
            for (i in eleList) {
                try {
                    val ele = wait!!.until {
                        driver!!.findElement(i.value)
                    }
                    when (i.key) {
                        "CGPA" -> student!!.cGPA = ele.text
                        else -> {}
                    }
                } catch (e: Exception) {
                    println(e)
                }
            }
        }
    }

    actual suspend fun loadStudentData(): Pair<successLog, studentInfo?> {
        return withContext(Dispatchers.IO) {
            loadProfile()
            loadResults()
            return@withContext if (student == null) Pair(
                successLog(false, "Unable to load student data"),
                null
            ) else Pair(successLog(true), student)
        }
    }

    actual fun destroySession() {
        driver?.close()
    }
}

actual fun ImageBitmap.toBase64(): String {
    val bufferImage = this.toAwtImage()
    val outputStream = ByteArrayOutputStream()
    ImageIO.write(bufferImage, "png", outputStream)
    return Base64.getEncoder().encodeToString(outputStream.toByteArray())
}

@Composable
actual fun getWebViewer(webView: cuimsAPI, modifier: Modifier) {

}