package com.patrykdolata.lifeagent

import com.patrykdolata.lifeagent.llm.LlmClient
import com.patrykdolata.lifeagent.llm.local.LocalLlmClient
import com.patrykdolata.lifeagent.task.InMemoryTaskRepository
import com.patrykdolata.lifeagent.task.TaskRepository
import com.patrykdolata.lifeagent.tool.CreateTaskTool
import com.patrykdolata.lifeagent.tool.GetCurrentDateTool
import com.patrykdolata.lifeagent.tool.GetCurrentTimeTool
import com.patrykdolata.lifeagent.tool.ListTasksTool

fun main() {

    val llmClient: LlmClient = LocalLlmClient()
    val taskRepository: TaskRepository = InMemoryTaskRepository()
    val tools = listOf(
        GetCurrentTimeTool(),
        GetCurrentDateTool(),
        CreateTaskTool(taskRepository),
        ListTasksTool(taskRepository)
    )
    val assistant = LifeAssistant(llmClient, tools)

    while (true) {
        print("Ty: ")
        val input = readln()
        if (input == "exit") {
            break
        }

        val response = assistant.respond(input)

        println()
        println("AI:")
        println(response)
        println()
    }
}
