package com.patrykdolata.lifeagent.task

import java.time.LocalDate

data class Task(
    val id: Int,
    val title: String,
    val dueDate: LocalDate?
)
