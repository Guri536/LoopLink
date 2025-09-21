package org.asv.looplink.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.asv.looplink.DatabaseMng
import org.asv.looplink.operations.insertUserDataFromProfile
import org.asv.looplink.operations.logout
import org.asv.looplink.webDriver.cuimsAPI

data object userInfo {
    var name: String? = null
    var uid: String? = null
    var section: String? = null
    var program: String? = null
    var contact: String? = null
    var cGPA: String? = null
    var email: String? = null
    var pfpImage: Boolean? = null

    fun reset(){
        name = null
        uid = null
        section = null
        program = null
        contact = null
        cGPA = null
        email = null
        pfpImage = null
    }
}

fun loadUserInfo(database: DatabaseMng) {
    val data = database.getUserData()
}

@Composable
fun UserProfileCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp).fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Profile image if available
            GetProfileImage(
                LocalDatabase.current.getProfileImage(),
                modifier = Modifier
                    .clip(CircleShape)
                    .size(200.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                userInfo.name?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "UID: ${userInfo.uid}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text("Section: ${userInfo.section}")
                Text("Program: ${userInfo.program}")
                Text("CGPA: ${userInfo.cGPA}")
                Text("Contact: ${userInfo.contact}")
                Text("Email: ${userInfo.email}")
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    ,
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                val database = LocalDatabase.current
                val cuimsAPI = LocalCuimsApi.current
                val navigator = LocalNavigator.currentOrThrow
                Button(
                    onClick = {
                        logout(database)
                        database.getAllFromDatabase()
                        navigator.replaceAll(LoginFields(
                            cuimsAPI = cuimsAPI,
                            loginSuccess = { it ->
                                insertUserDataFromProfile(
                                    database, it
                                )
                            }
                        ))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                    modifier = Modifier
                ){
                    Text("Logout")
                }
            }
        }
    }
}

class SettingsPage(): Screen {

    @Composable
    override fun Content(){
        loadUserInfo(LocalDatabase.current)
        val navigator = LocalNavigator.currentOrThrow
        UserProfileCard()
    }
}