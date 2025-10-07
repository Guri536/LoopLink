package org.asv.looplink.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.asv.looplink.network.discovery.ServiceInfo
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel


class AvailableServicesScreen(
    private val viewModel: PeerDiscoveryViewModel
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val discoveredServices by viewModel.discoveredServices.collectAsState()
        val isDiscovering by viewModel.isDiscovering.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.startDiscovery()
        }

        DisposableEffect(Unit) {
            onDispose {
                // Consider if you want to automatically stop discovery when leaving the screen
                // viewModel.stopDiscovery()
            }
        }

        Scaffold(topBar = {
            TopAppBar(title = { Text("Available Devices") }, navigationIcon = {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ))
        }, floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = {
                    if (isDiscovering) {
                        viewModel.stopDiscovery()
                    } else {
                        viewModel.startDiscovery()
                    }
                },
            ) {
                Text(
                    if (isDiscovering) "Stop Scan" else "Scan",
                    modifier = Modifier.width(155.dp),
                    textAlign = TextAlign.Center
                )
            }
        }) { paddingValues ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (isDiscovering && discoveredServices.isEmpty()) {
                    item {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                        Text("Scanning for devices...")
                    }
                } else if (!isDiscovering && discoveredServices.isEmpty()) {
                    item { Text("No devices found. Try scanning again.") }
                } else {
                    items(discoveredServices) { service ->
                        ServiceListItem(service) {
                            viewModel.connectToService(service)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(
                            Modifier, DividerDefaults.Thickness, DividerDefaults.color
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (isDiscovering) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                            Text("Scanning for devices...")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ServiceListItem(
    serviceInfo: ServiceInfo, onConnectClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            .clickable { onConnectClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = serviceInfo.instanceName, style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Host: ${serviceInfo.hostAddress}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Port: ${serviceInfo.port}", style = MaterialTheme.typography.bodySmall
                )
                if (serviceInfo.attributes.isNotEmpty()) {
                    Text(
                        text = "Platform: ${serviceInfo.attributes["platform"] ?: "Unknown"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onConnectClick) {
                Text("Connect")
            }
        }
    }
}