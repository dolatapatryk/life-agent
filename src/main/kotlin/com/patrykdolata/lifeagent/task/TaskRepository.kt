package com.patrykdolata.lifeagent.task

import java.time.LocalDate

interface TaskRepository {

    fun create(title: String, dueDate: LocalDate?): Task

    fun findAll(): List<Task>
}
