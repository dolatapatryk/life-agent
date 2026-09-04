package com.patrykdolata.lifeagent.llm.local

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class OllamaChatRequest(
    val model: String,
    val messages: List<OllamaMessage>,
    val tools: List<OllamaTool>,
    val stream: Boolean
)

@Serializable
data class OllamaMessage(
    val role: String,
    val content: String = "",
    @SerialName("tool_name")
    val toolName: String? = null,
    @SerialName("tool_calls")
    val toolCalls: List<OllamaToolCall>? = null
)

@Serializable
data class OllamaChatResponse(
    val message: OllamaMessage
)

@Serializable
data class OllamaTool(
    val type: String = "function",
    val function: OllamaFunction
)

@Serializable
data class OllamaFunction(
    val name: String,
    val description: String,
    val parameters: JsonObject
)

@Serializable
data class OllamaToolCall(
    val function: OllamaToolCallFunction
)

@Serializable
data class OllamaToolCallFunction(
    val name: String,
    val arguments: JsonObject
)
