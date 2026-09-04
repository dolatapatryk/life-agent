package com.patrykdolata.lifeagent.llm

import com.patrykdolata.lifeagent.llm.Role.ASSISTANT
import com.patrykdolata.lifeagent.llm.Role.SYSTEM
import com.patrykdolata.lifeagent.llm.Role.TOOL
import com.patrykdolata.lifeagent.llm.Role.USER

data class Message(
    val role: Role,
    val content: String,
    val toolCalls: List<ToolCall> = emptyList(),
    val toolName: String? = null
) {

    companion object {

        fun systemMessage(content: String) = Message(role = SYSTEM, content = content)

        fun userMessage(content: String): Message = Message(role = USER, content = content)

        fun assistantMessage(content: String): Message = Message(role = ASSISTANT, content = content)

        fun assistantToolCallMessage(toolCalls: List<ToolCall>) =
            Message(
                role = ASSISTANT,
                content = "",
                toolCalls = toolCalls
            )

        fun toolMessage(toolName: String, content: String): Message =
            Message(role = TOOL, content = content, toolName = toolName)
    }
}

enum class Role {
    SYSTEM,
    USER,
    ASSISTANT,
    TOOL
}
