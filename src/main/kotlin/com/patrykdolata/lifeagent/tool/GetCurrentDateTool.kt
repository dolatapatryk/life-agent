package com.patrykdolata.lifeagent.tool

import java.time.LocalDate

class GetCurrentDateTool : Tool {

    override val definition = ToolDefinition(
        name = "getCurrentDate",
        description = "Returns the current date.",
        parameters = emptyList()
    )

    override fun execute(arguments: Map<String, String>): String {
        return LocalDate.now().toString()
    }
}
