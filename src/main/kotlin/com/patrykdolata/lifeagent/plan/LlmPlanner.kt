package com.patrykdolata.lifeagent.plan

import com.patrykdolata.lifeagent.llm.LlmClient
import com.patrykdolata.lifeagent.llm.LlmResponse.Text
import com.patrykdolata.lifeagent.llm.LlmResponse.ToolCalls
import com.patrykdolata.lifeagent.llm.Message.Companion.systemMessage
import com.patrykdolata.lifeagent.llm.Message.Companion.userMessage
import com.patrykdolata.lifeagent.tool.ToolDefinition
import kotlinx.serialization.json.Json

class LlmPlanner(
    private val llmClient: LlmClient
) : Planner {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun createPlan(
        request: String,
        tools: List<ToolDefinition>
    ): Plan {

        val toolsDescription = tools.joinToString("\n") { tool ->
            "- ${tool.name}: ${tool.description}"
        }
        val plannerPrompt = """
            Jesteś plannerem osobistego agenta AI.

            Twoim zadaniem jest stworzenie planu wykonania prośby użytkownika.

            Nie wykonuj żadnych działań.
            Nie odpowiadaj użytkownikowi.
            Nie wywołuj narzędzi.

            Dostępne narzędzia:
            $toolsDescription

            Zwróć wyłącznie poprawny JSON w następującym formacie:

            {
              "steps": [
                {
                  "id": "1",
                  "description": "opis kroku"
                }
              ]
            }

            Zasady:
            - Każdy krok powinien reprezentować jedno logiczne działanie.
            - Kroki powinny być ułożone w kolejności wykonania.
            - Jeżeli krok wymaga danych uzyskanych wcześniej, najpierw zaplanuj krok pobierający te dane.
            - Nie zgaduj danych, które można pobrać za pomocą dostępnych narzędzi.
            - Nie dodawaj żadnego tekstu przed ani po JSON.
        """.trimIndent()

        val messages = listOf(
            systemMessage(plannerPrompt),
            userMessage(request)
        )

        val response = llmClient.generate(messages)
        val text = when (response) {
            is Text -> response.content
            is ToolCalls ->
                error("Planner should not call tools")
        }

        return json.decodeFromString(text)
    }
}
