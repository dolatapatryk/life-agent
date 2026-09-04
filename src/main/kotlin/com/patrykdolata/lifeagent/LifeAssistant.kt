package com.patrykdolata.lifeagent

import com.patrykdolata.lifeagent.llm.LlmClient
import com.patrykdolata.lifeagent.llm.LlmResponse
import com.patrykdolata.lifeagent.llm.Message
import com.patrykdolata.lifeagent.llm.Message.Companion.assistantMessage
import com.patrykdolata.lifeagent.llm.Message.Companion.assistantToolCallMessage
import com.patrykdolata.lifeagent.llm.Message.Companion.toolMessage
import com.patrykdolata.lifeagent.llm.Message.Companion.userMessage
import com.patrykdolata.lifeagent.tool.Tool
import org.slf4j.LoggerFactory

class LifeAssistant(
    private val llmClient: LlmClient,
    private val tools: List<Tool>,
    private val maxSteps: Int = 10
) {

    private val logger = LoggerFactory.getLogger(LifeAssistant::class.java)

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

                is LlmResponse.ToolCalls -> {
                    messages += assistantToolCallMessage(response.calls)
                    response.calls.forEach { toolCall ->
                        val tool = tools.find {
                            it.definition.name == toolCall.toolName
                        } ?: error("Unknown tool: ${toolCall.toolName}")

                        val toolResult = tool.execute(toolCall.arguments)
                        logger.info("Tool: {}, result: {}", toolCall.toolName, toolResult)

                        messages += toolMessage(
                            toolName = toolCall.toolName,
                            content = toolResult
                        )
                    }
                }
            }
        }

        error("Agent exceeded maximum number of steps: $maxSteps")
    }
}
