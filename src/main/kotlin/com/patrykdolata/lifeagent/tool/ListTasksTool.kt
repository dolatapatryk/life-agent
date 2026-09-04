package com.patrykdolata.lifeagent.tool

import com.patrykdolata.lifeagent.task.TaskRepository
import com.patrykdolata.lifeagent.tool.ToolResult.Success

class ListTasksTool(
    private val taskRepository: TaskRepository
) : Tool {

    override val definition = ToolDefinition(
        name = "listTasks",
        description = "Returns users tasks.",
        parameters = emptyList()
    )

    override fun execute(arguments: Map<String, String>): ToolResult {
        val tasks = taskRepository.findAll()

        if (tasks.isEmpty()) {
            return Success("No tasks found.")
        }

        return Success(
            tasks.joinToString("\n") { task ->
                """
            id=${task.id},
            title=${task.title},
            dueDate=${task.dueDate}
            """.trimIndent()
            }
        )
    }
}
