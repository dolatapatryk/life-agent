package com.patrykdolata.lifeagent.llm

import com.patrykdolata.lifeagent.tool.ToolDefinition

interface LlmClient {

    fun generate(messages: List<Message>, tools: List<ToolDefinition> = emptyList()): LlmResponse
}
