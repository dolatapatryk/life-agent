package com.patrykdolata.lifeagent.tool

sealed interface ToolResult {

    data class Success(val content: String) : ToolResult

    data class Error(val message: String) : ToolResult
}
