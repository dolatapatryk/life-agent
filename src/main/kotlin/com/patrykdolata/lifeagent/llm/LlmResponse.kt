package com.patrykdolata.lifeagent.llm

sealed interface LlmResponse {

    data class Text(val content: String) : LlmResponse

    data class ToolCalls(
        val calls: List<ToolCall>
    ) : LlmResponse
}

data class ToolCall(
    val toolName: String,
    val arguments: Map<String, String>  = emptyMap(),
    val parsingError: String? = null
)
