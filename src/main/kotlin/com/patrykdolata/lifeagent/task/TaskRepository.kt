package com.patrykdolata.lifeagent.task

interface TaskRepository {

    fun create(task: Task): Task

    fun findAll(): List<Task>
}
