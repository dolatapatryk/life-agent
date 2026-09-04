package com.patrykdolata.lifeagent.tool

data class ToolDefinition(
    val name: String,
    val description: String,
    val parameters: List<ToolParameter>
)
