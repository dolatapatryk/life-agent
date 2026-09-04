package com.patrykdolata.lifeagent

import com.patrykdolata.lifeagent.llm.LlmClient
import com.patrykdolata.lifeagent.llm.LlmResponse
import com.patrykdolata.lifeagent.llm.Message
import com.patrykdolata.lifeagent.llm.Message.Companion.assistantMessage
import com.patrykdolata.lifeagent.llm.Message.Companion.toolMessage
import com.patrykdolata.lifeagent.llm.Message.Companion.userMessage
import com.patrykdolata.lifeagent.tool.Tool

class LifeAssistant(
    private val llmClient: LlmClient,
    private val tools: List<Tool>,
    private val maxSteps: Int = 10
) {

    private val messages = mutableListOf<Message>()

    fun respond(message: String): String {
        messages += userMessage(message)

        repeat(maxSteps) {
            val response = llmClient.generate(messages, tools.map { it.definition })

            when (response) {
                is LlmResponse.Text -> {
                    messages += assistantMessage(response.content)
                    return response.content
                }

                is LlmResponse.ToolCall -> {
                    val tool = tools.find {
                        it.definition.name == response.toolName
                    } ?: error("Unknown tool: ${response.toolName}")
                    val toolResult = tool.execute(response.arguments)
                    messages += toolMessage(toolName = tool.definition.name, content = toolResult)
                }
            }
        }

        error("Agent exceeded maximum number of steps: $maxSteps")
    }
}
