package org.asv.looplink.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun textField(
    onvalueChange: (String) -> Unit = {},
    radius: Int = 8,
    paddingH: Int = 8,
    paddingV: Int = 4,
    placeholder: String = "Enter"
) {
    var text by remember { mutableStateOf("") }
    val shape = RoundedCornerShape(radius.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black, shape)
            .background(Color.White, shape)
            .padding(paddingH.dp, paddingV.dp)
    ) {
        BasicTextField(
            value = text,
            onValueChange = { it ->
                text = it
                onvalueChange(it)
            },
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            cursorBrush = SolidColor(Color.Black),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (text.isEmpty()) {
            Text(
                placeholder,
                color = Color.LightGray,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
    }
}