package com.patrykdolata.lifeagent.tool

interface Tool {
    val definition: ToolDefinition

    val parallelSafe: Boolean
        get() = false

    fun execute(arguments: Map<String, String>): ToolResult
}
