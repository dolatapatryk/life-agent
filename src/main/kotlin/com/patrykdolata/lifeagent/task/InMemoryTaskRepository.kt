package com.patrykdolata.lifeagent.task

class InMemoryTaskRepository : TaskRepository {

    private val tasks = mutableListOf<Task>()

    override fun create(task: Task): Task {
        tasks += task
        return task
    }

    override fun findAll(): List<Task> {
        return tasks.toList()
    }
}
