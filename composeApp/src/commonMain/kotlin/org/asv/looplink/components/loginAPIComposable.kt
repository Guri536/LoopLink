package org.asv.looplink.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.asv.looplink.errors.errorsLL
import org.asv.looplink.operations.insertUserDataFromProfile
import org.asv.looplink.webDriver.cuimsAPI
import org.asv.looplink.webDriver.getWebViewer
import org.asv.looplink.webDriver.studentInfo
import org.asv.looplink.webDriver.successLog
import kotlin.math.log
import kotlin.reflect.full.memberProperties

class LoginFields: Screen {

    @Composable
    override fun Content() {
        val cuimsAPI = LocalCuimsApi.current
        val database = LocalDatabase.current
        val navigator = LocalNavigator.currentOrThrow

        var uidField by remember { mutableStateOf("23BSC10022") }
        var passField by remember { mutableStateOf("19May2005!") }
        val interactionSource = remember { MutableInteractionSource() }
        var isUIDError by remember { mutableStateOf(false) }
        var isPassError by remember { mutableStateOf(false) }
        var isError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        val isHovered by interactionSource.collectIsHoveredAsState()

        var captchaFile by remember {
            mutableStateOf(
                ImageBitmap(
                    width = 110,
                    height = 48,
                )
            )
        }

        var captchaField by remember { mutableStateOf("") }
        var showCaptcha by remember { mutableStateOf(false) }
        var isCaptchaError by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        var webDriverInstance by remember { mutableStateOf(false) }

        val fontSize = 20.sp
        val fontFamily = FontFamily.Monospace

        val colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFFAFAFA),
            disabledContainerColor = Color.Gray,
            focusedLabelColor = Color.DarkGray,
            errorContainerColor = Color.White,
            unfocusedBorderColor = Color.Transparent
        )
        val textStyle = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            fontFamily = fontFamily
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
//            .verticalScroll(rememberScrollState())
            ,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier,
                Arrangement.Center,
                Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .width(IntrinsicSize.Max),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    CustomOutlinedTextField(
                        label = {
                            Text(
                                "UID", fontSize = fontSize,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily,
                            )
                        },
                        value = uidField,
                        placeholder = {
                            Text(
                                "Enter UID",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onValueChange = {
                            uidField = it
                        },
                        colors = colors,
                        singleLine = true,
                        textStyle = textStyle,
                        isError = isUIDError,
                        supportingText = {
                            if (isUIDError) {
                                TextFieldFooterErrorMsg("UID cannot be empty")
                            }
                        },
                        shape = MaterialTheme.shapes.large,
                        interactionSource = interactionSource
                    )
                    CustomOutlinedTextField(
                        label = {
                            Text(
                                "Password", fontSize = fontSize,
                                fontWeight = FontWeight.Bold,
                                fontFamily = fontFamily,
                            )
                        },
                        value = passField,
                        placeholder = {
                            Text(
                                "Enter Password",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        onValueChange = { it ->
                            passField = it
                        },
                        singleLine = true,
                        colors = colors,
                        visualTransformation = PasswordVisualTransformation(),
                        textStyle = textStyle,
                        isError = isPassError,
                        supportingText = {
                            if (isPassError) {
                                TextFieldFooterErrorMsg("Password cannot be empty")
                            }
                        },
                        shape = MaterialTheme.shapes.large
                    )
                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CustomOutlinedTextField(
                            label = {
                                Text(
                                    "Captcha", fontSize = fontSize,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontFamily,
                                )
                            },
                            value = captchaField,
                            placeholder = {
                                Text(
                                    "Enter Captcha",
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            onValueChange = { it ->
                                captchaField = it
                            },
                            singleLine = true,
                            colors = colors,
                            textStyle = textStyle,
                            isError = isCaptchaError,

                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 0.dp,
                                bottomEnd = 0.dp,
                                bottomStart = 16.dp
                            ),
                            modifier = Modifier
                                .height(49.dp)
                                .width(190.dp)
//                            .padding(0.dp)
                            ,
                            enabled = showCaptcha
                        )
                        Box(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .height(40.dp)
                                .width(90.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 0.dp,
                                        topEnd = 16.dp,
                                        bottomEnd = 16.dp,
                                        bottomStart = 0.dp
                                    )
                                )
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (!webDriverInstance) {

                                Image(
                                    bitmap = captchaFile,
                                    contentDescription = "Captcha Image",
                                    modifier = Modifier
                                        .fillMaxHeight()
                                )
                            } else {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .height(20.dp)
                                        .width(20.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }


            Box(
            ) {
                Button(
                    enabled = !webDriverInstance,
                    onClick = {
                        scope.launch {
                            webDriverInstance = true
                            isUIDError = false
                            isPassError = false
                            isError = false
                            isCaptchaError = false

                            if (!showCaptcha) {
                                if (uidField.isBlank()) {
                                    isUIDError = true
                                    errorMessage = "UID cannot be empty"
                                    webDriverInstance = false
                                    return@launch
                                }
                                if (passField.isBlank()) {
                                    isPassError = true
                                    errorMessage = "Password cannot be empty"
                                    webDriverInstance = false
                                    return@launch
                                }

                                try {
                                    val loginSuccess = cuimsAPI.login(uidField, passField)

                                    if (!loginSuccess.success) {
                                        isError = true
                                        errorMessage = loginSuccess.message
                                    } else {
                                        val imgFile = cuimsAPI.getCaptcha()
                                        if (!imgFile.first.success) {
                                            isError = true
                                            errorMessage = imgFile.first.message
                                        } else {
                                            captchaFile = imgFile.second!!
                                            showCaptcha = true
                                        }
                                    }
                                } catch (e: Exception) {
                                    isError = true
                                    errorMessage = errorsLL.internet_error + e.message
                                    throw (e)
                                }
                            } else {
                                if (captchaField.isBlank()) {
                                    isError = true
                                    isCaptchaError = true
                                    errorMessage = "Captcha cannot be empty"
                                    webDriverInstance = false
                                    return@launch
                                }
                                try {

                                    val success = cuimsAPI.fillCaptcha(captchaField)
                                    if (!success.success) {
                                        isError = true
                                        errorMessage = success.message
                                        webDriverInstance = false

                                        when (errorMessage) {
                                            "Invalid Captcha" -> {
                                                errorMessage = "Invalid Captcha"
                                                isCaptchaError = true
                                                val imgFile = cuimsAPI.getCaptcha()
                                                if (!imgFile.first.success) {
                                                    isError = true
                                                    errorMessage = imgFile.first.message
                                                } else {
                                                    captchaFile = imgFile.second!!
                                                    showCaptcha = true
                                                }
                                            }

                                            "User Id or Password In Correct" -> {
                                                errorMessage = "Either UID or Password is incorrect"
                                                isPassError = true
                                                isUIDError = true
                                                showCaptcha = false
                                                cuimsAPI.endSession()
                                            }

                                            else -> {
                                                isCaptchaError = true
                                            }
                                        }
                                        return@launch
                                    }

                                    val data = cuimsAPI.loadStudentData()
                                    if (data.first.success) {
                                        insertUserDataFromProfile(
                                            database,
                                            data.second!!)
                                    }
                                    navigator.replaceAll(SettingsPage())
                                    cuimsAPI.destroySession()
                                } catch (e: Exception) {
                                    isError = true
                                    errorMessage = errorsLL.unknownError
                                }
                            }
                            webDriverInstance = false
                        }
                    }
                ) {
                    Text(
                        "Submit",
                        fontSize = 20.sp
                    )
                }

            }
            Spacer(modifier = Modifier.height(16.dp))
            if (isError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                )
            }
//        Spacer(modifier = Modifier.height(16.dp))
//        getWebViewer(
//            cuimsAPI, modifier = Modifier
//                .fillMaxWidth()
//                .padding(30.dp)
//        )
        }

        DisposableEffect(Unit) {
            onDispose {
                cuimsAPI.endSession()
            }
        }
    }
}


@Composable
fun TextFieldFooterErrorMsg(text: String = "Error") {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Start,
        fontSize = 15.sp,
    )
}