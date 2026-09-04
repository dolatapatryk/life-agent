package com.patrykdolata.lifeagent

import com.patrykdolata.lifeagent.llm.LlmClient
import com.patrykdolata.lifeagent.llm.local.LocalLlmClient
import com.patrykdolata.lifeagent.plan.LlmPlanner
import com.patrykdolata.lifeagent.task.FileTaskRepository
import com.patrykdolata.lifeagent.task.TaskRepository
import com.patrykdolata.lifeagent.tool.CreateTaskTool
import com.patrykdolata.lifeagent.tool.GetCurrentDateTool
import com.patrykdolata.lifeagent.tool.GetCurrentTimeTool
import com.patrykdolata.lifeagent.tool.ListTasksTool
import java.io.File

fun main() {

    val llmClient: LlmClient = LocalLlmClient()
    val taskRepository: TaskRepository = FileTaskRepository(File("tasks.json"))
    val tools = listOf(
        GetCurrentTimeTool(),
        GetCurrentDateTool(),
        CreateTaskTool(taskRepository),
        ListTasksTool(taskRepository)
    )
    val planner = LlmPlanner(llmClient)
    val assistant = LifeAssistant(llmClient, planner, tools)

    while (true) {
        print("You: ")
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
