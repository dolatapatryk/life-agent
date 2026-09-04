package com.patrykdolata.lifeagent.tool

import java.time.LocalTime

class GetCurrentTimeTool : Tool {

    override val definition = ToolDefinition(
        name = "getCurrentTime",
        description = "Returns the current time.",
        parameters = emptyList()
    )

    override fun execute(arguments: Map<String, String>): String {
        return LocalTime.now().toString();
    }
}
