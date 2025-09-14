package org.asv.looplink.errors

data object errorsLL {
    val timeout_error: String = "You got timed out, please try again or continue as guest"
    val captcha_error: String = "Captcha not found, API error"
    val internet_error: String = "Internet connection issue, try again or continue as guest"
    val unknownError: String = "Unknown Error occurred, try again or continue as guest"
}
