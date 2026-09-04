package com.patrykdolata.lifeagent.task

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.util.*

class FileTaskRepository(
    private val file: File
) : TaskRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override fun create(task: Task): Task {
        val tasks = loadTasks().toMutableList()

        tasks += task

        saveTasks(tasks)
        return task
    }

    override fun findAll(): List<Task> {
        return loadTasks()
    }

    private fun loadTasks(): List<Task> {
        if (!file.exists()) {
            return emptyList()
        }

        val content = file.readText()

        if (content.isBlank()) {
            return emptyList()
        }

        return json
            .decodeFromString<List<TaskDto>>(content)
            .map { it.toDomain() }
    }

    private fun saveTasks(tasks: List<Task>) {
        val dtos = tasks.map { it.toDto() }

        file.writeText(
            json.encodeToString(dtos)
        )
    }
}

@Serializable
data class TaskDto(
    val id: String,
    val title: String,
    val dueDate: String? = null
)

private fun Task.toDto() =
    TaskDto(
        id = id.toString(),
        title = title,
        dueDate = dueDate?.toString()
    )

private fun TaskDto.toDomain() =
    Task(
        id = UUID.fromString(id),
        title = title,
        dueDate = dueDate?.let { LocalDate.parse(it) }
    )
