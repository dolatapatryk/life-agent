package com.patrykdolata.lifeagent.tool

data class ToolParameter(
    val name: String,
    val description: String,
    val type: ToolParameterType,
    val required: Boolean
)

enum class ToolParameterType {
    STRING,
    INTEGER,
    BOOLEAN
}
