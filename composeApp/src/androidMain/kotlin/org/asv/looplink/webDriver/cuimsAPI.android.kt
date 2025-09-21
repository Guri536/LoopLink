package org.asv.looplink.webDriver

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.asv.looplink.errors.errorsLL
import kotlin.coroutines.resume
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import org.asv.looplink.secrets.APIKeys
import org.jsoup.Jsoup
import org.openqa.selenium.By
import java.io.ByteArrayOutputStream
import kotlin.io.encoding.Base64


actual fun ByteArray.toImageBitmap(): ImageBitmap {
    return BitmapFactory.decodeByteArray(this, 0, size).asImageBitmap()
}

actual class cuimsAPI(private val webView: WebView) {
    actual var uid: String? = null
    actual var pass: String? = null
    actual var student: studentInfo? = null
    actual val BASEURL = "https://students.cuchd.in/"
    actual val endPoints = mapOf(
        "Attendance" to "frmStudentCourseWiseAttendanceSummary.aspx?type=etgkYfqBdH1fSfc255iYGw==",
        "Profile" to "frmStudentProfile.aspx",
        "Marks" to "result.aspx"
    )

    private var pageLoadDeferred: CompletableDeferred<Unit>? = null

    init {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                pageLoadDeferred?.complete(Unit)
            }
        }
    }

    actual fun getWebView(): Any = webView

    actual suspend fun initDriver() {
        withContext(Dispatchers.Main) {
            try {
                webView.settings.javaScriptEnabled = true
                webView.settings.domStorageEnabled = true
                webView.loadUrl("https://www.google.com")
            } catch (e: Exception) {
                throw Exception("Internet Error")
            }
        }
    }

    private suspend fun loadUrlAndWait(url: String) {
        withContext(Dispatchers.Main) {
            pageLoadDeferred = CompletableDeferred()
            webView.loadUrl(url)
        }
        // This will suspend the coroutine until onPageFinished completes the deferred.
        pageLoadDeferred?.await()
    }

    private suspend fun eval(js: String): String = suspendCancellableCoroutine { cont ->
        webView.evaluateJavascript(js) { result ->
            cont.resume(result ?: "")
        }
    }

    actual suspend fun login(uid: String, pass: String): successLog {
        initDriver()
        this.uid = uid
        this.pass = pass
        student = studentInfo(uid, pass)
//        return withContext(Dispatchers.Main) {
        try {
            loadUrlAndWait(BASEURL)
//            println(webView.height)
            if (!waitForElement("txtUserId")) return successLog(false, errorsLL.internet_error)

            withContext(Dispatchers.Main) {
                eval("document.getElementById('txtUserId').value = '$uid';")
                eval("document.getElementById('btnNext').click();")
            }

            pageLoadDeferred = CompletableDeferred()
            pageLoadDeferred?.await()

            if (!waitForElement("txtLoginPassword")) return successLog(
                false,
                "Password field not found."
            )
            withContext(Dispatchers.Main)
            { eval("document.getElementById('txtLoginPassword').value = '$pass';") }

            return successLog(true)
        } catch (e: Exception) {
            e.printStackTrace()
            return successLog(false, errorsLL.timeout_error)
//                throw(e)
        }
//        }
    }

    actual suspend fun getCaptcha(): Pair<successLog, ImageBitmap?> {
        return try {
            // Use our robust function to capture just the captcha image element
            val captchaBitmap = captureWebView("imgCaptcha")

            if (captchaBitmap != null) {
                Pair(successLog(true, "Captcha captured."), captchaBitmap)
            } else {
                Pair(successLog(false, "Failed to find or capture captcha image."), null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(successLog(false, errorsLL.captcha_error), null)
        }
    }

    private enum class WaitResult { SUCCESS, ERROR, TIMEOUT }

    private suspend fun waitForEitherElement(
        successCssSelector: String,
        errorCssSelector: String,
        timeout: Long = 10000L
    ): WaitResult {
        val startTime = System.currentTimeMillis()
        val checkScript = """
    (function() {
        if (document.querySelector('$successCssSelector')) return "SUCCESS";
        if (document.querySelector('$errorCssSelector')) return "ERROR";
        return "NONE";
    })();
""".trimIndent()
        while (System.currentTimeMillis() - startTime < timeout) {
            val result = withContext(Dispatchers.Main) { eval(checkScript) }
            when (result.trim('"')) {
                "SUCCESS" -> return WaitResult.SUCCESS
                "ERROR" -> return WaitResult.ERROR
            }
            kotlinx.coroutines.delay(200)
        }
        return WaitResult.TIMEOUT
    }

    actual suspend fun fillCaptcha(captcha: String): successLog {
        try {
            if (!waitForElement("txtcaptcha")) return successLog(false, "Captcha input not found.")
            if (!waitForElement("btnLogin")) return successLog(false, "Login button not found.")

            withContext(Dispatchers.Main) {
                eval("document.getElementById('txtcaptcha').value = '$captcha';")
                eval("document.getElementById('btnLogin').click();")
            }

            val successSelector = "header"
            val errorSelector = ".sweet-alert.showSweetAlert.visible"

            val result = waitForEitherElement(successSelector, errorSelector)

            return when (result) {
                WaitResult.SUCCESS -> {
                    successLog(true, "Login successful!")
                }

                WaitResult.ERROR -> {
                    withContext(Dispatchers.Main) {
                        val errMsg =
                            eval("document.querySelector(\".sweet-alert.showSweetAlert.visible p\").innerText || ''")
                        eval("document.querySelectorAll(\".sweet-alert.showSweetAlert.visible button\")[1].click();")

                        if (!waitForElement("txtLoginPassword")) successLog(
                            false,
                            "Password field not found."
                        )
                        withContext(Dispatchers.Main)
                        { eval("document.getElementById('txtLoginPassword').value = '$pass';") }
                        successLog(false, errMsg.trim('"'))
                    }
                }

                WaitResult.TIMEOUT -> {
                    successLog(false, "Operation timed out. No response from server.")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return successLog(false, errorsLL.captcha_error)
        }
    }

    actual fun endSession() {
        webView.loadUrl("about:blank")
    }

    private suspend fun captureWebView(elementId: String): ImageBitmap? {

        val virtualWidth = 1080
        val virtualHeight = 1920

        return withContext(Dispatchers.Main) {

            webView.measure(
                View.MeasureSpec.makeMeasureSpec(virtualWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(virtualHeight, View.MeasureSpec.EXACTLY)
            )
            webView.layout(0, 0, virtualWidth, virtualHeight)

            println("Finding Element $elementId")
            if (!waitForElement(elementId, timeout = 3000L)) {
                println("Element with ID '$elementId' not found after waiting.")
                return@withContext null
            }
            println("Found Element")

            val js = """
                (function() {
                    var elem = document.getElementById('$elementId');
                    if (!elem) return null;
                    var rect = elem.getBoundingClientRect();
                    return JSON.stringify({ x: rect.left, y: rect.top, width: rect.width, height: rect.height });
                })();
            """.trimIndent()

            val jsonResult = eval(js).trim('"').replace("\\\"", "\"")
            if (jsonResult == "null" || jsonResult.isEmpty()) return@withContext null

            val bounds = jsonResult.let {
                val map = mutableMapOf<String, Float>()
                it.removeSurrounding("{", "}").split(",").forEach { pair ->
                    val (key, value) = pair.split(":")
                    map[key.trim().trim('"')] = value.trim().toFloat()
                }
                map
            }

            val webViewBitmap = createBitmap(webView.width, webView.height).also {
                val canvas = android.graphics.Canvas(it)
                webView.draw(canvas)
            }

            try {
                val density = webView.context.resources.displayMetrics.density
                val x = (bounds["x"]!! * density).toInt()
                val y = (bounds["y"]!! * density).toInt()
                val width = (bounds["width"]!! * density).toInt()
                val height = (bounds["height"]!! * density).toInt()
//                println("$x; $y; $width; $height; ${webViewBitmap.width}; ${webViewBitmap.height}")
                if (x + width > webViewBitmap.width || y + height > webViewBitmap.height) {
                    return@withContext null
                }

                val croppedBitmap = Bitmap.createBitmap(webViewBitmap, x, y, width, height)
                croppedBitmap.asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private suspend fun waitForElement(
        ele: String,
        timeout: Long = 5000L,
    ): Boolean {
        val startTime = System.currentTimeMillis()
        val checkEle = "document.getElementById('$ele') != null"
        while (System.currentTimeMillis() - startTime < timeout) {
            val result = withContext(Dispatchers.Main) { eval(checkEle) }
            if (result == "true") return true
            kotlinx.coroutines.delay(100)
        }
        return false
    }

    actual suspend fun processCaptcha(imgBase64: String): String {
        var captcha: String? = null
        val base64Img = "data:image/png;base64,$imgBase64"

        val ocrURL = "https://api.ocr.space/parse/image"
        val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
        val ocrData = mutableMapOf(
            "apikey" to APIKeys.ocrKey,
            "language" to "eng",
            "base64Image" to base64Img,
            "isOverlayRequired" to "True"
        )
        return withContext(Dispatchers.IO) {
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

    actual suspend fun autoFillCaptcha(): successLog {
        return withContext(Dispatchers.Main) {
            val imageBitmap = getCaptcha()
            if (imageBitmap.second == null) return@withContext successLog(
                false,
                "Unable to fill captcha"
            )
            val captcha = processCaptcha(imageBitmap.second!!.toBase64())
            fillCaptcha(captcha)
            return@withContext successLog(true)
        }
    }

    suspend fun loadResults() {
        webView.loadUrl(BASEURL + endPoints["Marks"])
        pageLoadDeferred = CompletableDeferred()
        pageLoadDeferred?.await()
        val eleList = mapOf<String, String>(
            "CGPA" to ("ContentPlaceHolder1_wucResult1_lblCGPA"),
        )
        for (i in eleList) {
            try {
                var ele: String = ""
                withContext(Dispatchers.Main) {
                    waitForElement(i.value)
                    ele = eval("document.getElementById('${i.value}').innerText")
                }
                when (i.key) {
                    "CGPA" -> student!!.cGPA = ele
                    else -> {}
                }
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    suspend fun loadProfile() {
        webView.loadUrl(BASEURL + endPoints["Profile"])
        pageLoadDeferred = CompletableDeferred()
        pageLoadDeferred?.await()

        val eleList = mapOf<String, String>(
            "UID" to ("lbstuUID"),
            "Name" to ("ContentPlaceHolder1_lblName"),
            "Section" to ("ContentPlaceHolder1_lblCurrentSection"),
            "Program" to ("ContentPlaceHolder1_lblProgramCode"),
            "Contact" to ("ContentPlaceHolder1_gvStudentContacts_lblMobile_2"),
        )
        for (i in eleList) {
            try {
                var ele: String = ""

                withContext(Dispatchers.Main) {
                    waitForElement(i.value)
                    ele = eval("document.getElementById('${i.value}')?.innerText || ''").trim('"')
                }
                when (i.key) {
                    "UID" -> student!!.studentUID = ele
                    "Name" -> student!!.fullName = ele
                    "Section" -> student!!.currentSection = ele
                    "Program" -> student!!.programCode = ele
                    "Contact" -> student!!.studentContact = ele
                    else -> {}
                }
            } catch (e: Exception) {
                println(e)
            }
        }

        val pfpImageEle = "ContentPlaceHolder1_imgStu"
        withContext(Dispatchers.Main){
            val pfpBase64 = eval("document.getElementById('$pfpImageEle')?.src || ''").trim('"')
            student!!.pfpBytes = Base64.decode(pfpBase64.removePrefix("data:image/png;base64,"))
        }
    }

    actual suspend fun loadStudentData(): Pair<successLog, studentInfo?> {
        return withContext(Dispatchers.Main) {
            loadProfile()
            loadResults()
            return@withContext if (student == null) Pair(
                successLog(false, "Unable to load student data"),
                null
            ) else Pair(successLog(true), student)
        }
    }

    actual fun destroySession() {
        webView.destroy()
    }

}

actual fun ImageBitmap.toBase64(): String {
    val bitmap = this.asAndroidBitmap()
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return android.util.Base64.encodeToString(
        outputStream.toByteArray(),
        android.util.Base64.DEFAULT
    )
}

@Composable
actual fun getWebViewer(webView: cuimsAPI, modifier: Modifier) {
    AndroidView<WebView>(
        modifier = modifier,
        factory = {
            (webView.getWebView() as WebView).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                if (url.isNullOrEmpty()) {
                    loadUrl("about:blank")
                }
            }
        }
    )
}
