package org.asv.looplink.viewmodel

import kotlinx.serialization.Serializable

@Serializable
data class RoomItem(
    val id: Int,
    val label: String,
    val unread: Int = 0,
    val isGroup: Boolean = false,
    val members: List<String> = emptyList()
)
