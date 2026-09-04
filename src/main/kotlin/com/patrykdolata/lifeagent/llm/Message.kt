package com.patrykdolata.lifeagent.llm

data class Message(
    val role: Role,
    val content: String
) {

    companion object {

        fun userMessage(content: String): Message = Message(role = Role.USER, content = content)

        fun assistantMessage(content: String): Message = Message(role = Role.ASSISTANT, content = content)

        fun toolMessage(content: String): Message = Message(role = Role.TOOL, content = content)
    }
}

enum class Role {
    USER,
    ASSISTANT,
    TOOL
}
