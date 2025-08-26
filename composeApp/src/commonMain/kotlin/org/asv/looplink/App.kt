package org.asv.looplink

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import looplink.composeapp.generated.resources.Res
import looplink.composeapp.generated.resources.compose_multiplatform
//import com.jetbrains.looplink.database

@Composable
@Preview
fun App(database: DatabaseMng) {

    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        val showSize = remember { mutableStateOf("Press") }

        fun updateShow(){
            if(showContent) {
                showContent = false
                showContent = true
            }
        }
        fun updateSize(){
            showSize.value = database.getSize().toString()
        }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val plat = getPlatform()
            Text("Hello there, you are on ${plat.name}\nLets see how this goes",
                modifier = Modifier,
                textAlign = TextAlign.Center
            )

            Button(onClick = {
                database.deleteUser()
                updateShow()
                updateSize()
            }){
                Text("Delete Content")
            }
            Button(onClick = { showContent = !showContent }) {
                Text("Show Content")
            }
            Button(onClick = {
                database.insertIntoDatabase("A", database.getAllFromDatabase().size.toString())
                updateShow()
                updateSize()
            }) {
                Text("Add Content")
            }
            Button(onClick = { showSize.value = database.getSize().toString() }) {
                Text("Current Size: ${showSize.value}")
            }

            if (showContent){
                Text("Let's see")
                val data = database.getAllFromDatabase()
                Text(data.toString().prependIndent("What: "))
            }

        }
    }
}