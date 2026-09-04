package com.patrykdolata.lifeagent.tool

import com.patrykdolata.lifeagent.task.Task
import com.patrykdolata.lifeagent.task.TaskRepository
import com.patrykdolata.lifeagent.tool.ToolResult.Error
import com.patrykdolata.lifeagent.tool.ToolResult.Success
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

    override fun execute(arguments: Map<String, String>): ToolResult {
        val title = arguments["title"]
            ?: return Error("Missing argument 'title'")

        val dueDate = arguments["dueDate"]
            ?: return Error("Missing argument 'dueDate'")

        val parsedDate = runCatching {
            LocalDate.parse(dueDate)
        }.getOrElse {
            return Error("Invalid dueDate '$dueDate'. Expected yyyy-MM-dd.")
        }

        val task = taskRepository.create(
            Task(id = randomUUID(), title = title, dueDate = parsedDate)
        )

        return Success(
            """
            Created task:
            id=${task.id}
            title=${task.title}
            dueDate=${task.dueDate}
        """.trimIndent()
        )
    }
}
