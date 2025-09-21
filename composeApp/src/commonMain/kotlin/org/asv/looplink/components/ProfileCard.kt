package org.asv.looplink.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.asv.looplink.DatabaseMng
import org.asv.looplink.operations.logout
import org.asv.looplink.ui.AvailableServicesScreen


data object userInfo {
    var name: String? = null
    var uid: String? = null
    var section: String? = null
    var program: String? = null
    var contact: String? = null
    var cGPA: String? = null
    var email: String? = null
    var pfpImage: Boolean? = null

    fun reset() {
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

fun loadUserInfo(database: DatabaseMng) = database.getUserData()


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
        BoxWithConstraints {
            if (this.maxWidth < 450.dp) {
                TallScreenLayout()
            } else {
                WideScreenLayout()
            }
        }
    }
}

@Composable
fun TallScreenLayout() {
    val database = LocalDatabase.current

    Column(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (userInfo.pfpImage == true) {
            GetProfileImage( // This composable needs to be defined
                database.getProfileImage(), // Example: Fetch image bytes
                modifier = Modifier
                    .clip(CircleShape)
                    .size(200.dp)
            )
        } else {
            // Placeholder for when there's no image
            Box(
                modifier = Modifier.size(200.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            ShowUserData()
        }
        SideButtons()
    }
}

@Composable
fun WideScreenLayout() {
    val database = LocalDatabase.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp).fillMaxWidth().height(IntrinsicSize.Max)
    ) {
        if (userInfo.pfpImage == true) {
            GetProfileImage( // This composable needs to be defined
                database.getProfileImage(), // Example: Fetch image bytes
                modifier = Modifier
                    .clip(CircleShape)
                    .size(200.dp)
            )
        } else {
            // Placeholder for when there's no image
            Box(
                modifier = Modifier.size(200.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ShowUserData()
        }
        Spacer(Modifier.weight(1f))
        SideButtons()
    }
}

class SettingsPage() : Screen {
    @Composable
    override fun Content() {
        val database = LocalDatabase.current
        LaunchedEffect(database) {
            loadUserInfo(database)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            UserProfileCard(modifier = Modifier.weight(1f, fill = false))
        }
    }
}

@Composable
fun SideButtons(){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.width(IntrinsicSize.Max).fillMaxHeight()
    ) {
        FindDevicesButton()
        LogoutButton()
    }
}
@Composable
fun LogoutButton(modifier: Modifier = Modifier) {
    val database = LocalDatabase.current
    val navigator = LocalNavigator.currentOrThrow
    Button(
        onClick = {
            logout(database)
            navigator.replaceAll(LoginFields())
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
        ),
        modifier = modifier.padding(vertical = 2.dp).fillMaxWidth()
    ) {
        Text("Logout")
    }
}

@Composable
fun FindDevicesButton(modifier: Modifier = Modifier) {
    val peerDiscoveryViewModel = LocalPeerDiscoveryViewModel.current
    val navigator = LocalNavigator.currentOrThrow

    if (peerDiscoveryViewModel != null) {
        Button(
            onClick = {
                navigator.push(AvailableServicesScreen(peerDiscoveryViewModel))
            },
            modifier = modifier.padding(vertical = 2.dp).fillMaxWidth()
        ) {
            Text("Find Devices")
        }
    } else {
        Text("Network discovery not available.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ShowUserData() {
    userInfo.name?.let {
        Text(
            it,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
    Text(
        "UID: ${userInfo.uid ?: "N/A"}",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold
    )
    Text("Section: ${userInfo.section ?: "N/A"}")
    Text("Program: ${userInfo.program ?: "N/A"}")
    Text("CGPA: ${userInfo.cGPA?.trim('"') ?: "N/A"}")
    Text("Contact: ${userInfo.contact ?: "N/A"}")
    Text("Email: ${userInfo.email ?: "N/A"}")
}