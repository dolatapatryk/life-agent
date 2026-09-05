package com.patrykdolata.lifeagent.tool

import com.patrykdolata.lifeagent.tool.ToolResult.Success
import java.time.LocalDate

class GetCurrentDateTool : Tool {

    override val definition = ToolDefinition(
        name = "getCurrentDate",
        description = "Returns the current date.",
        parameters = emptyList()
    )

    override val parallelSafe: Boolean = true

    override fun execute(arguments: Map<String, String>): ToolResult {
        return Success(LocalDate.now().toString())
    }
}
