package com.patrykdolata.lifeagent.llm.local

import com.patrykdolata.lifeagent.llm.LlmClient
import com.patrykdolata.lifeagent.llm.LlmResponse
import com.patrykdolata.lifeagent.llm.Message
import com.patrykdolata.lifeagent.llm.Role
import com.patrykdolata.lifeagent.llm.Role.ASSISTANT
import com.patrykdolata.lifeagent.llm.Role.SYSTEM
import com.patrykdolata.lifeagent.llm.Role.TOOL
import com.patrykdolata.lifeagent.llm.Role.USER
import com.patrykdolata.lifeagent.llm.ToolCall
import com.patrykdolata.lifeagent.tool.ToolDefinition
import com.patrykdolata.lifeagent.tool.ToolParameter
import com.patrykdolata.lifeagent.tool.ToolParameterType
import com.patrykdolata.lifeagent.tool.ToolParameterType.BOOLEAN
import com.patrykdolata.lifeagent.tool.ToolParameterType.INTEGER
import com.patrykdolata.lifeagent.tool.ToolParameterType.STRING
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.slf4j.LoggerFactory

class LocalLlmClient(
    private val model: String = "qwen3:1.7b"
) : LlmClient {

    private val logger = LoggerFactory.getLogger(LocalLlmClient::class.java)

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 120_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 120_000
        }
    }

    override fun generate(
        messages: List<Message>,
        tools: List<ToolDefinition>
    ): LlmResponse = runBlocking {
        val request = OllamaChatRequest(
            model = model,
            messages = messages.map { it.toOllamaMessage() },
            tools = tools.map { it.toOllamaTool() },
            stream = false
        )

        val start = System.currentTimeMillis()
        val response = httpClient.post(
            "http://localhost:11434/api/chat"
        ) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<OllamaChatResponse>()
        val duration = System.currentTimeMillis() - start
        logger.info("LLM response: {}, took: {}ms", response, duration)

        val toolCalls = response.message.toolCalls.orEmpty()
        if (toolCalls.isNotEmpty()) {
            LlmResponse.ToolCalls(
                calls = toolCalls.map { toolCall ->
                    ToolCall(
                        toolName = toolCall.function.name,
                        arguments = toolCall.function.arguments.toMap()
                    )
                }
            )
        } else {
            LlmResponse.Text(
                content = response.message.content
            )
        }
    }
}

private fun Role.toOllamaRole(): String =
    when (this) {
        SYSTEM -> "system"
        USER -> "user"
        ASSISTANT -> "assistant"
        TOOL -> "tool"
    }

private fun ToolDefinition.toOllamaTool(): OllamaTool =
    OllamaTool(
        function = OllamaFunction(
            name = name,
            description = description,
            parameters = parameters.toOllamaParameters()
        )
    )

private fun JsonObject.toMap(): Map<String, String> {
    return mapValues { (_, value) -> value.jsonPrimitive.content }
}

private fun List<ToolParameter>.toOllamaParameters(): JsonObject {
    return buildJsonObject {
        put("type", "object")

        putJsonObject("properties") {
            forEach { parameter ->
                putJsonObject(parameter.name) {
                    put("type", parameter.type.toJsonSchemaType())
                    put("description", parameter.description)
                }
            }
        }

        putJsonArray("required") {
            filter { it.required }
                .forEach { parameter ->
                    add(parameter.name)
                }
        }
    }
}

private fun ToolParameterType.toJsonSchemaType(): String =
    when (this) {
        STRING -> "string"
        INTEGER -> "integer"
        BOOLEAN -> "boolean"
    }

private fun Message.toOllamaMessage(): OllamaMessage =
    OllamaMessage(
        role = role.toOllamaRole(),
        content = content,
        toolCalls = toolCalls
            .takeIf { it.isNotEmpty() }
            ?.map { toolCall ->
                OllamaToolCall(
                    function = OllamaToolCallFunction(
                        name = toolCall.toolName,
                        arguments = toolCall.arguments.toJsonObject()
                    )
                )
            },
        toolName = toolName
    )

private fun Map<String, String>.toJsonObject(): JsonObject =
    buildJsonObject {
        this@toJsonObject.forEach { (key, value) ->
            put(key, value)
        }
    }
