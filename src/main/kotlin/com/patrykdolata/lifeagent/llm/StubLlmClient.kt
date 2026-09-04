package com.patrykdolata.lifeagent.llm

import com.patrykdolata.lifeagent.tool.ToolDefinition

class StubLlmClient : LlmClient {

    override fun generate(
        messages: List<Message>,
        tools: List<ToolDefinition>
    ): LlmResponse {
        val lastMessage = messages.last()

        if (lastMessage.role == Role.TOOL) {
            return LlmResponse.Text(
                content = "Operacja została wykonana. Wynik: ${lastMessage.content}"
            )
        }

        val lastUserMessage = messages.last { it.role == Role.USER }.content

        return when {
            lastUserMessage.contains("dodaj zadanie", ignoreCase = true) -> {
                requireToolExists(
                    tools,
                    "createTask"
                )
                LlmResponse.ToolCall(
                    toolName = "createTask",
                    arguments = mapOf(
                        "title" to "Kupić mleko",
                        "dueDate" to "2026-09-05"
                    )
                )
            }

            lastUserMessage.contains(
                "pokaż zadania",
                ignoreCase = true
            ) -> {
                LlmResponse.ToolCall(
                    toolName = "listTasks"
                )
            }

            lastUserMessage.contains("godzina", ignoreCase = true) -> {
                requireToolExists(
                    tools,
                    "getCurrentTime"
                )
                LlmResponse.ToolCall(
                    toolName = "getCurrentTime"
                )
            }

            else -> {
                LlmResponse.Text(
                    content = "Nie potrzebuję narzędzia."
                )
            }
        }
    }

    private fun requireToolExists(
        tools: List<ToolDefinition>,
        name: String
    ) {
        check(tools.any { it.name == name }) {
            "Tool $name is not available"
        }
    }
}
