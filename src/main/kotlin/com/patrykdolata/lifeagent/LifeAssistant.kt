package com.patrykdolata.lifeagent

import com.patrykdolata.lifeagent.llm.LlmClient
import com.patrykdolata.lifeagent.llm.LlmResponse
import com.patrykdolata.lifeagent.llm.Message.Companion.assistantMessage
import com.patrykdolata.lifeagent.llm.Message.Companion.assistantToolCallMessage
import com.patrykdolata.lifeagent.llm.Message.Companion.systemMessage
import com.patrykdolata.lifeagent.llm.Message.Companion.toolMessage
import com.patrykdolata.lifeagent.llm.Message.Companion.userMessage
import com.patrykdolata.lifeagent.llm.ToolCallValidator
import com.patrykdolata.lifeagent.plan.Planner
import com.patrykdolata.lifeagent.tool.Tool
import com.patrykdolata.lifeagent.tool.ToolResult
import com.patrykdolata.lifeagent.tool.ToolResult.Error
import com.patrykdolata.lifeagent.tool.ToolResult.Success
import org.slf4j.LoggerFactory

class LifeAssistant(
    private val llmClient: LlmClient,
    private val planner: Planner,
    private val tools: List<Tool>,
    private val maxSteps: Int = 10
) {

    private val logger = LoggerFactory.getLogger(LifeAssistant::class.java)

    private val messages = mutableListOf(
        systemMessage(SYSTEM_PROMPT)
    )

    fun respond(message: String): String {
        val plan = planner.createPlan(
            request = message,
            tools = tools.map { it.definition }
        )

        logger.info("Plan:\n{}", plan)

        messages += userMessage(
            """
        Prośba użytkownika:
        $message

        Plan wykonania:
        $plan

        Wykonaj ten plan krok po kroku.
        Korzystaj z narzędzi zgodnie z potrzebą.
        """.trimIndent()
        )
//        messages += userMessage(message)

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
                        }

                        if (tool == null) {
                            messages += toolMessage(
                                toolName = toolCall.toolName,
                                content = "ERROR: Unknown tool '${toolCall.toolName}'"
                            )

                            return@forEach
                        }

                        if (toolCall.parsingError != null) {
                            messages += toolMessage(
                                toolName = toolCall.toolName,
                                content = "ERROR: ${toolCall.parsingError}"
                            )

                            return@forEach
                        }

                        val validationError = ToolCallValidator.validate(
                            toolCall = toolCall,
                            definition = tool.definition
                        )

                        if (validationError != null) {
                            messages += toolMessage(
                                toolName = toolCall.toolName,
                                content = "ERROR: $validationError"
                            )

                            return@forEach
                        }

                        val result = try {
                            tool.execute(toolCall.arguments)
                        } catch (e: Exception) {
                            Error("Tool execution failed: ${e.message}")
                        }
                        logger.info("Tool: {}, result: {}", toolCall.toolName, result)

                        messages += toolMessage(
                            toolName = toolCall.toolName,
                            content = result.toMessageContent()
                        )
                    }
                }
            }
        }

        error("Agent exceeded maximum number of steps: $maxSteps")
    }

    companion object {
        private const val SYSTEM_PROMPT = """
        Jesteś osobistym asystentem użytkownika.

        Używaj dostępnych narzędzi, gdy są potrzebne do odpowiedzi na prośbę użytkownika.

        Zasady:

        - Nigdy nie wymyślaj wyników, które można uzyskać za pomocą narzędzia.
        - Jeśli użytkownik pyta o aktualną datę lub godzinę, użyj odpowiedniego narzędzia.
        - Jeśli użytkownik prosi o utworzenie lub pobranie zadań, użyj odpowiedniego narzędzia.
        - Aktualna data i aktualny czas są stanem zewnętrznym. Nigdy ich nie zgaduj. Zawsze używaj odpowiedniego narzędzia, gdy są potrzebne — nawet pośrednio.
        - Jeśli prośba użytkownika zawiera względne określenie daty lub czasu, takie jak „dzisiaj”, „jutro”, „wczoraj”, „w przyszłym tygodniu” lub podobne, najpierw użyj odpowiedniego narzędzia do pobrania daty/czasu.
        - W razie potrzeby możesz użyć wielu narzędzi.
        - Nie wywołuj jednocześnie narzędzi, jeśli jedno z nich potrzebuje wyniku drugiego. Najpierw wykonaj pierwsze narzędzie, wykorzystaj jego wynik, a dopiero potem zdecyduj o następnym kroku.
        - Zanim zdecydujesz, co zrobić dalej, wykorzystaj wynik działania narzędzia.
        - Gdy masz już wystarczająco dużo informacji, odpowiedz użytkownikowi bezpośrednio.
        - Odpowiadaj po polsku
    """
    }
}

private fun ToolResult.toMessageContent(): String =
    when (this) {
        is Success -> content
        is Error -> "ERROR: $message"
    }
