package com.patrykdolata.lifeagent.task

import java.time.LocalDate

class InMemoryTaskRepository : TaskRepository {

    private val tasks = mutableListOf<Task>()
    private var nextId = 1

    override fun create(
        title: String,
        dueDate: LocalDate?
    ): Task {
        val task = Task(
            id = nextId++,
            title = title,
            dueDate = dueDate
        )

        tasks += task

        return task
    }

    override fun findAll(): List<Task> {
        return tasks.toList()
    }
}
