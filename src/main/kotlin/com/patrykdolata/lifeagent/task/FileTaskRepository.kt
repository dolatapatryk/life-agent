package com.patrykdolata.lifeagent.task

import kotlinx.serialization.Serializable

class FileTaskRepository(
    private val
) {
}

@Serializable
data class TaskDto(
    val id: String,
    val title: String
)
