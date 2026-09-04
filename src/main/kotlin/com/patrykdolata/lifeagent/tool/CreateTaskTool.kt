package com.patrykdolata.lifeagent.tool

import com.patrykdolata.lifeagent.task.Task
import com.patrykdolata.lifeagent.task.TaskRepository
import java.time.LocalDate
import java.util.UUID.randomUUID

class CreateTaskTool(
    private val taskRepository: TaskRepository
) : Tool {

    override val definition = ToolDefinition(
        name = "createTask",
        description = "Creates a new task for the user.",
        parameters = listOf(
            ToolParameter(
                name = "title",
                description = "Title of the task.",
                type = ToolParameterType.STRING,
                required = true
            ),
            ToolParameter(
                name = "dueDate",
                description = "Due date in yyyy-MM-dd format.",
                type = ToolParameterType.STRING,
                required = false
            )
        )
    )

    override fun execute(arguments: Map<String, String>): String {
        val title = arguments["title"]
            ?: error("Missing argument: title")

        val dueDate = arguments["dueDate"]
            ?.let(LocalDate::parse)

        val task = taskRepository.create(
            Task(id = randomUUID(), title = title, dueDate = dueDate)
        )

        return """
            Created task:
            id=${task.id}
            title=${task.title}
            dueDate=${task.dueDate}
        """.trimIndent()
    }
}
