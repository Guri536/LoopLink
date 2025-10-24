package org.asv.looplink.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.currentOrThrow
import org.asv.looplink.data.repository.UserRespository
import org.asv.looplink.di.koinMainViewModel
import org.asv.looplink.ui.AvailableServicesScreen
import org.asv.looplink.viewmodel.MainViewModel
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel
import org.koin.compose.koinInject

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
    val user: UserRespository = koinInject()
    val userInfo = user.currentUser.collectAsState()

    Column(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (userInfo.value?.picture != null) {
            GetProfileImage(
                userInfo.value?.picture,
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
    val user: UserRespository = koinInject()
    val userInfo = user.currentUser.collectAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp).fillMaxWidth().height(IntrinsicSize.Max)
    ) {
        if (userInfo.value?.picture != null) {
            GetProfileImage( // This composable needs to be defined
                userInfo.value?.picture, // Example: Fetch image bytes
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
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalAppNavigator.currentOrThrow

        Scaffold(topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(.95f)
                )
            )
        }) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                UserProfileCard(modifier = Modifier.weight(1f, fill = false))
            }
        }
    }
}

@Composable
fun SideButtons() {
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
    val navigator = LocalAppNavigator.currentOrThrow
    val mainViewModel: MainViewModel = koinMainViewModel()

    Button(
        onClick = {
            mainViewModel.logoutUser()
            navigator.navigator.replaceAll(
                LoginFields(onLoginSuccess = {
                    mainViewModel.startP2PServices()
                }
                )
            )
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
        modifier = modifier.padding(vertical = 2.dp).fillMaxWidth()
    ) {
        Text(
            "Logout",
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun FindDevicesButton(modifier: Modifier = Modifier) {
    val peerDiscoveryViewModel: PeerDiscoveryViewModel = koinInject()
    val navigator = LocalAppNavigator.currentOrThrow

    Button(
        onClick = {
            navigator.pushScreen(AvailableServicesScreen(peerDiscoveryViewModel))
        },
        modifier = modifier.padding(vertical = 2.dp).fillMaxWidth()
    ) {
        Text("Find Devices")
    }
}

@Composable
fun ShowUserData() {
    val user: UserRespository = koinInject()
    val userInfo = user.currentUser.collectAsState()

    userInfo.value?.name?.let {
        Text(
            it,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
    Text(
        "UID: ${userInfo.value?.uid ?: "N/A"}",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold
    )
    Text("Section: ${userInfo.value?.section ?: "N/A"}")
    Text("Program: ${userInfo.value?.program ?: "N/A"}")
    Text("CGPA: ${userInfo.value?.cGPA?.trim('"') ?: "N/A"}")
    Text("Contact: ${userInfo.value?.contact ?: "N/A"}")
    Text("Email: ${userInfo.value?.email ?: "N/A"}")
}