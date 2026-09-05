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
                  "description": "opis kroku",
                  "toolName": "nazwaNarzędzia",
                  "dependsOn": []
                }
              ]
            }

            Zasady:
            - Każdy krok powinien reprezentować jedno logiczne działanie.
            - Jeśli krok wymaga narzędzia, ustaw toolName dokładnie na nazwę jednego z dostępnych narzędzi.
            - Jeden krok może używać maksymalnie jednego narzędzia.
            - Jeśli krok nie wymaga narzędzia, ustaw toolName na null.
            - Nie zgaduj danych, które można pobrać za pomocą narzędzi.
            - Aktualna data i czas są stanem zewnętrznym i nigdy nie mogą być zgadywane.
            - Jeśli prośba zawiera względne określenie czasu lub daty, takie jak:
              "dzisiaj", "jutro", "wczoraj", "za tydzień", "w przyszłym tygodniu"
              lub podobne, plan MUSI najpierw zawierać krok pobierający aktualną datę lub czas
              za pomocą odpowiedniego narzędzia.
            - Jeśli argument narzędzia w późniejszym kroku zależy od wyniku wcześniejszego kroku,
              najpierw zaplanuj krok pobierający tę wartość.
            - Nigdy nie twórz kroku createTask z datą względną typu "jutro".
              Najpierw pobierz aktualną datę, a dopiero później utwórz zadanie z konkretną datą.
            - Pole dependsOn zawiera identyfikatory kroków, które muszą zostać
              zakończone przed wykonaniem danego kroku.
            - Jeśli krok potrzebuje wyniku wcześniejszego kroku, dodaj identyfikator
              tego kroku do dependsOn.
            - Jeśli kolejność działań ma znaczenie ze względu na efekt uboczny,
              również użyj dependsOn.
            - Nie dodawaj zależności tylko dlatego, że krok znajduje się wcześniej
              na liście.
            - Krok bez zależności powinien mieć dependsOn = [].
            - dependsOn może wskazywać wyłącznie istniejące kroki planu.
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
