package org.asv.looplink.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import org.asv.looplink.components.LocalAppNavigator
import org.asv.looplink.network.discovery.ServiceInfo
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel
import org.koin.compose.koinInject

class GroupCreationScreen : Screen {
    @Composable
    override fun Content() {
        GroupCreationPanel()
    }
}

class GroupCreationTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Filled.GroupAdd)
            return remember {
                TabOptions(
                    index = 2u,
                    title = "Add Group Members",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        GroupCreationPanel()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCreationPanel() {
    val peerDiscoveryViewModel: PeerDiscoveryViewModel = koinInject()
    val availableSessions by peerDiscoveryViewModel.discoveredServices.collectAsStateWithLifecycle()

    var groupName by remember { mutableStateOf("") }
    val selectedMembers = remember { mutableStateListOf<ServiceInfo>() }

    val navigator = LocalAppNavigator.currentOrThrow

    Scaffold(
        modifier = Modifier
            .onKeyEvent {
                if (it.key == Key.Escape && it.type == KeyEventType.KeyUp) {
                    navigator.pop()
                    true
                } else {
                    false
                }
            },
        topBar = {
            TopAppBar(
                title = { Text("Create a Group") }, navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding).padding(16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Select Members", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(availableSessions) { service ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedMembers.contains(service)) {
                                    selectedMembers.remove(service)
                                } else {
                                    selectedMembers.add(service)
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedMembers.contains(service),
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    selectedMembers.add(service)
                                } else {
                                    selectedMembers.remove(service)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(service.serviceName)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // TODO: Implement group creation logic
                    navigator.pop()
                },
                enabled = groupName.isNotBlank() && selectedMembers.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Group")
            }
        }
    }
}
