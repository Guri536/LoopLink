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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import org.asv.looplink.data.repository.UserRepository
import org.asv.looplink.network.discovery.ServiceInfo
import org.asv.looplink.viewmodel.ChatViewModel
import org.asv.looplink.viewmodel.PeerDiscoveryViewModel
import org.koin.compose.koinInject
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

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
    val chatViewModel: ChatViewModel = koinInject()
    val userRepository: UserRepository = koinInject() // ADDED
    val allRooms by chatViewModel.roomsWithStatus.collectAsStateWithLifecycle()
    val currentUserId = userRepository.getUserIdAndName().first

    val availableContacts = allRooms.filter {
        !it.isGroup && it.id != 0
    }

    var groupName by remember { mutableStateOf("") }
    // We'll store the selected *RoomItems*
    val selectedMembers = remember { mutableStateListOf<RoomItem>() }
    var description by remember { mutableStateOf("") }
    var groupType by remember { mutableStateOf(GroupType.GENERAL) }
    val customTabs = remember { mutableStateListOf(GroupTabs(label = "Chat", type = TabType.CHAT)) }

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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp) // Adds spacing
            ) {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Group Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                var groupTypeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = groupTypeExpanded,
                    onExpandedChange = { groupTypeExpanded = !groupTypeExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = groupType.name,
                        onValueChange = {},
                        label = { Text("Group Type") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(groupTypeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = groupTypeExpanded,
                        onDismissRequest = { groupTypeExpanded = false }
                    ) {
                        GroupType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    groupType = type
                                    groupTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                HorizontalDivider()

                Text("Group Tabs", style = MaterialTheme.typography.titleMedium)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    customTabs.forEachIndexed { index, tab ->
                        CustomTabEditorRow(
                            tab = tab,
                            onLabelChange = { newLabel ->
                                customTabs[index] = tab.copy(label = newLabel)
                            },
                            onTypeChange = { newType ->
                                customTabs[index] = tab.copy(type = newType)
                            },
                            onDelete = { customTabs.removeAt(index) },
                            // Can't delete the main Chat tab
                            canDelete = tab.type != TabType.CHAT
                        )
                    }
                }
                TextButton(
                    onClick = { customTabs.add(GroupTabs(label = "Files", type = TabType.FILES)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Tab")
                    Spacer(Modifier.width(8.dp))
                    Text("Add a Tab")
                }

                HorizontalDivider()

                Spacer(modifier = Modifier.height(16.dp))

                Text("Select Members", style = MaterialTheme.typography.titleMedium)

                Column(modifier = Modifier.fillMaxWidth()) {
                    if (availableContacts.isEmpty()) {
                        Text(
                            "No contacts available to add.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    availableContacts.forEach { room ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectedMembers.contains(room)) {
                                        selectedMembers.remove(room)
                                    } else {
                                        selectedMembers.add(room)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedMembers.contains(room),
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        selectedMembers.add(room)
                                    } else {
                                        selectedMembers.remove(room)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(room.label)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    chatViewModel.createGroup(
                        groupName = groupName,
                        selectedMembers = selectedMembers.toList(),
                        description = description.ifBlank { null },
                        groupType = groupType,
                        tabs = customTabs.toList()
                    )
                    navigator.pop()
                },
                enabled = groupName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Group")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTabEditorRow(
    tab: GroupTabs,
    onLabelChange: (String) -> Unit,
    onTypeChange: (TabType) -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    var typeExpanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Tab Label
        OutlinedTextField(
            value = tab.label,
            onValueChange = onLabelChange,
            label = { Text("Label") },
            modifier = Modifier.weight(1f)
        )

        // Tab Type Dropdown
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = !typeExpanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = tab.type.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                TabType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.name) },
                        onClick = {
                            onTypeChange(type)
                            typeExpanded = false
                        }
                    )
                }
            }
        }

        // Delete Button
        IconButton(onClick = onDelete, enabled = canDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete Tab",
                tint = if (canDelete) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
    }
}
