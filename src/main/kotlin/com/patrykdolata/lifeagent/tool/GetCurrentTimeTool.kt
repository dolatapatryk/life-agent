package com.patrykdolata.lifeagent.tool

import com.patrykdolata.lifeagent.tool.ToolResult.Success
import java.time.LocalTime

class GetCurrentTimeTool : Tool {

    override val definition = ToolDefinition(
        name = "getCurrentTime",
        description = "Returns the current time.",
        parameters = emptyList()
    )

    override val parallelSafe: Boolean = true

    override fun execute(arguments: Map<String, String>): ToolResult {
        return Success(LocalTime.now().toString())
    }
}
