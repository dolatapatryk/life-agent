package com.patrykdolata.lifeagent.task

import java.time.LocalDate
import java.util.UUID

data class Task(
    val id: UUID,
    val title: String,
    val dueDate: LocalDate? = null
)
