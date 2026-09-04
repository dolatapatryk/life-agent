package com.patrykdolata.lifeagent.plan

import com.patrykdolata.lifeagent.llm.LlmClient
import com.patrykdolata.lifeagent.llm.LlmResponse.Text
import com.patrykdolata.lifeagent.llm.LlmResponse.ToolCalls
import com.patrykdolata.lifeagent.llm.Message.Companion.systemMessage
import com.patrykdolata.lifeagent.llm.Message.Companion.userMessage
import com.patrykdolata.lifeagent.tool.ToolDefinition

class LlmPlanner(
    private val llmClient: LlmClient
) : Planner {

    override fun createPlan(
        request: String,
        tools: List<ToolDefinition>
    ): String {

        val plannerPrompt = buildString {
            appendLine("Jesteś plannerem dla osobistego agenta AI.")
            appendLine()
            appendLine("Twoim zadaniem jest przygotowanie planu wykonania prośby użytkownika.")
            appendLine()
            appendLine("Dostępne możliwości agenta:")

            tools.forEach { tool ->
                appendLine("- ${tool.name}: ${tool.description}")
            }

            appendLine()
            appendLine("""
        Nie wykonuj żadnych działań.
        Nie używaj narzędzi.
        Nie odpowiadaj bezpośrednio użytkownikowi.
        Przygotuj tylko krótki plan krok po kroku.

        Zasady:
        - Uwzględnij zależności między krokami.
        - Jeśli jeden krok wymaga wyniku wcześniejszego kroku, musi wystąpić później.
        - Nie zgaduj danych, które powinny zostać pobrane za pomocą narzędzia.
        - Nie podawaj konkretnych wartości, których jeszcze nie znasz.
        - Plan powinien zawierać tylko kroki potrzebne do wykonania prośby.
    """.trimIndent())
        }

        val messages = listOf(
            systemMessage(plannerPrompt),
            userMessage(request)
        )

        return when (
            val response = llmClient.generate(messages)
        ) {
            is Text -> response.content
            is ToolCalls -> error("Planner should not call tools")
        }
    }
}
