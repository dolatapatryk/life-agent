package com.patrykdolata.lifeagent.llm

import com.patrykdolata.lifeagent.llm.Role.ASSISTANT
import com.patrykdolata.lifeagent.llm.Role.TOOL
import com.patrykdolata.lifeagent.llm.Role.USER

data class Message(
    val role: Role,
    val content: String,
    val toolName: String? = null
) {

    companion object {

        fun userMessage(content: String): Message = Message(role = USER, content = content)

        fun assistantMessage(content: String): Message = Message(role = ASSISTANT, content = content)

        fun toolMessage(toolName: String, content: String): Message =
            Message(role = TOOL, content = content, toolName = toolName)
    }
}

enum class Role {
    USER,
    ASSISTANT,
    TOOL
}
