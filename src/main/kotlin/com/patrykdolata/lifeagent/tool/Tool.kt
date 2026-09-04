package com.patrykdolata.lifeagent.tool

interface Tool {
    val definition: ToolDefinition

    fun execute(arguments: Map<String, String>): String
}
