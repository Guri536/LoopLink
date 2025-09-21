package org.asv.looplink.webDriver

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.ExpectedConditions

data class successLog constructor(
    val success: Boolean,
    val message: String = "Success"
)

expect fun ByteArray.toImageBitmap(): ImageBitmap
expect fun ImageBitmap.toBase64(): String

data class studentInfo(val uid: String, val pass: String) {
    lateinit var studentUID: String
    lateinit var fullName: String
    lateinit var currentSection: String
    lateinit var programCode: String
    lateinit var studentContact: String
    var studentEmail: String = "$uid@cuchd.in"
    lateinit var cGPA: String
    lateinit var pfpBytes: ByteArray
}

expect class cuimsAPI{
    var uid: String?
    var pass: String?

    var student: studentInfo?

    val BASEURL: String

    val endPoints: Map<String, String>

    suspend fun initDriver()
    suspend fun login(uid: String, pass: String): successLog
    suspend fun getCaptcha(): Pair<successLog, ImageBitmap?>
    suspend fun fillCaptcha(captcha: String): successLog
    fun endSession()
    suspend fun processCaptcha(imgBase64: String): String
    suspend fun autoFillCaptcha(): successLog
    fun getWebView(): Any
    suspend fun loadStudentData(): Pair<successLog, studentInfo?>
    fun destroySession()
}

@Composable
expect fun getWebViewer(webView: cuimsAPI, modifier: Modifier)