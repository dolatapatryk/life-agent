package com.patrykdolata.lifeagent.tool

import com.patrykdolata.lifeagent.task.TaskRepository

class ListTasksTool(
    private val taskRepository: TaskRepository
) : Tool {

    override val definition = ToolDefinition(
        name = "listTasks",
        description = "Returns users tasks.",
        parameters = emptyList()
    )

    override fun execute(arguments: Map<String, String>): String {
        val tasks = taskRepository.findAll()

        if (tasks.isEmpty()) {
            return "No tasks found."
        }

        return tasks.joinToString("\n") { task ->
            """
            id=${task.id},
            title=${task.title},
            dueDate=${task.dueDate}
            """.trimIndent()
        }
    }
}
