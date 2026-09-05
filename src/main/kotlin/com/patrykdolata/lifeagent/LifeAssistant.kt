package com.patrykdolata.lifeagent

import com.patrykdolata.lifeagent.llm.LlmClient
import com.patrykdolata.lifeagent.llm.LlmResponse.Text
import com.patrykdolata.lifeagent.llm.LlmResponse.ToolCalls
import com.patrykdolata.lifeagent.llm.Message
import com.patrykdolata.lifeagent.llm.Message.Companion.assistantMessage
import com.patrykdolata.lifeagent.llm.Message.Companion.assistantToolCallMessage
import com.patrykdolata.lifeagent.llm.Message.Companion.systemMessage
import com.patrykdolata.lifeagent.llm.Message.Companion.toolMessage
import com.patrykdolata.lifeagent.llm.Message.Companion.userMessage
import com.patrykdolata.lifeagent.llm.ToolCallValidator
import com.patrykdolata.lifeagent.plan.Plan
import com.patrykdolata.lifeagent.plan.PlanExecutor
import com.patrykdolata.lifeagent.plan.PlanStep
import com.patrykdolata.lifeagent.plan.Planner
import com.patrykdolata.lifeagent.tool.Tool
import com.patrykdolata.lifeagent.tool.ToolResult
import com.patrykdolata.lifeagent.tool.ToolResult.Error
import com.patrykdolata.lifeagent.tool.ToolResult.Success
import org.slf4j.LoggerFactory

class LifeAssistant(
    private val llmClient: LlmClient,
    private val planner: Planner,
    private val planExecutor: PlanExecutor,
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

        return planExecutor.execute(
            request = message,
            plan = plan,
            executeStep = ::executeStep
        )
    }

    private fun executeStep(originalRequest: String, step: PlanStep, previousResults: List<String>): String {
        val request = buildStepRequest(originalRequest, step, previousResults)
        logger.info("Executing plan step {}: {}", step.id, step.description)
        val stepMessages = mutableListOf(
            systemMessage(SYSTEM_PROMPT),
            userMessage(request)
        )
        val stepTool = step.toolName?.let { toolName ->
            tools.find { it.definition.name == toolName }
                ?: error("Unknown tool in plan: $toolName")
        }
        return runStepAgentLoop(stepMessages, stepTool)
    }

    private fun runStepAgentLoop(messages: MutableList<Message>, stepTool: Tool?): String {
        var toolExecuted = false

        repeat(maxSteps) {

            val availableTools = if (!toolExecuted && stepTool != null) {
                listOf(stepTool.definition)
            } else {
                emptyList()
            }

            val response = llmClient.generate(messages, availableTools)
            when (response) {
                is Text -> {
                    if (stepTool != null && !toolExecuted) {
                        logger.warn(
                            "Model returned text before required tool '{}' was executed",
                            stepTool.definition.name
                        )

                        messages += userMessage(
                            """
                            Nie wykonano jeszcze wymaganego narzędzia '${stepTool.definition.name}'.
                            Nie zgaduj wyniku.
                            Wywołaj teraz to narzędzie z argumentami zgodnymi z jego definicją.
                            """.trimIndent()
                        )

                        return@repeat
                    }
                    messages += assistantMessage(response.content)
                    return response.content
                }

                is ToolCalls -> {
                    if (stepTool == null) {
                        error(
                            "Model attempted to call a tool, " +
                                "but current plan step does not allow tools"
                        )
                    }

                    if (toolExecuted) {
                        error("Tool has already been executed for this plan step")
                    }

                    if (response.calls.size != 1) {
                        error(
                            "Expected exactly one tool call for plan step, " +
                                "but got ${response.calls.size}"
                        )
                    }

                    val toolCall = response.calls.single()
                    if (toolCall.toolName != stepTool.definition.name) {
                        error(
                            "Plan step allows '${stepTool.definition.name}', " +
                                "but model called '${toolCall.toolName}'"
                        )
                    }

                    messages += assistantToolCallMessage(response.calls)

                    if (toolCall.parsingError != null) {
                        messages += toolMessage(
                            toolName = toolCall.toolName,
                            content = "ERROR: ${toolCall.parsingError}"
                        )

                        return@repeat
                    }

                    val validationError = ToolCallValidator.validate(
                        toolCall = toolCall,
                        definition = stepTool.definition
                    )

                    if (validationError != null) {
                        messages += toolMessage(
                            toolName = toolCall.toolName,
                            content = "ERROR: $validationError"
                        )

                        return@repeat
                    }

                    val result = try {
                        stepTool.execute(toolCall.arguments)
                    } catch (e: Exception) {
                        Error("Tool execution failed: ${e.message}")
                    }
                    logger.info("Tool: {}, result: {}", toolCall.toolName, result)

                    messages += toolMessage(
                        toolName = toolCall.toolName,
                        content = result.toMessageContent()
                    )

                    toolExecuted = true
                }
            }
        }

        error("Agent exceeded maximum number of steps: $maxSteps")
    }

    private fun runAgentLoop(): String {
        repeat(maxSteps) {
            val response = llmClient.generate(
                messages,
                tools.map { it.definition }
            )

            when (response) {
                is Text -> {
                    messages += assistantMessage(response.content)
                    return response.content
                }

                is ToolCalls -> {
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

    private fun buildStepRequest(
        originalRequest: String,
        step: PlanStep,
        previousResults: List<String>
    ): String {
        val results = if (previousResults.isEmpty()) {
            "Brak."
        } else {
            previousResults.joinToString("\n")
        }

        return """
        Realizujesz jeden krok wcześniej przygotowanego planu.

        Oryginalna prośba użytkownika jest podana wyłącznie jako kontekst:
        $originalRequest

        Aktualny krok:
        ${step.id}. ${step.description}

        Wyniki wcześniej wykonanych kroków:
        $results

        Wykonaj WYŁĄCZNIE aktualny krok.

        Nie wykonuj żadnych działań należących do późniejszych kroków planu,
        nawet jeśli wynikają z oryginalnej prośby użytkownika.

        Jeśli do wykonania aktualnego kroku potrzebujesz narzędzia, użyj go.
        Po wykonaniu aktualnego kroku zwróć jego wynik.
    """.trimIndent()
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

private fun Plan.toPrompt(): String {
    return steps.joinToString("\n") { step ->
        "${step.id}. ${step.description}"
    }
}
